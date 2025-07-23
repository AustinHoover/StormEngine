package electrosphere.renderer.actor;

import electrosphere.engine.Globals;
import electrosphere.mem.JomlPool;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.actor.mask.ActorAnimationData;
import electrosphere.renderer.actor.mask.ActorMeshMask;
import electrosphere.renderer.actor.mask.ActorTextureMask;
import electrosphere.renderer.actor.mask.ActorUniformMap;
import electrosphere.renderer.actor.mask.ActorUniformMap.UniformValue;
import electrosphere.renderer.model.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4d;
import org.joml.Sphered;
import org.joml.Vector3d;

/**
 * An actor
 * Container for a model that keeps track of:
 * - Animation state
 * - Mesh overrides
 * - Shader overrides
 * - Texture overrides
 * - Bone overrides
 * - Static Morphs (think scaling something, but from creatures.json)
 * - Static uniforms per mesh
 */
public class Actor {

    /**
     * full-resolution lod level
     */
    public static final int LOD_LEVEL_FULL = 2;

    /**
     * lower-resolution lod level
     */
    public static final int LOD_LEVEL_LOWER = 1;

    /**
     * Performs static draw
     */
    public static final int LOD_LEVEL_STATIC = 0;





    //
    //
    //              CORE   RENDER    VARS
    //
    //

    /**
     * The LOD level of the actor
     */
    private int lodLevel = Actor.LOD_LEVEL_FULL;

    /**
     * Controls whether the actor should obey frustum culling
     */
    private boolean frustumCull = true;

    /**
     * Sets scaling applied at the actor level
     */
    private float scale = 1.0f;

    /**
     * The transform matrix
     */
    private Matrix4d modelMatrix = new Matrix4d();

    /**
     * The world position vector
     */
    private Vector3d worldPos = new Vector3d();








    //
    //
    //             MODEL     AND      MESH     DATA
    //
    //



    /**
     * the model path of the model backing the actor
     */
    private final String baseModelPath;

    /**
     * Path to the low res model
     */
    private String lowResBaseModelPath;

    /**
     * The animation data for the actor
     */
    private final ActorAnimationData animationData;

    /**
     * Mask for overwriting meshes in a given actor
     */
    private ActorMeshMask meshMask = new ActorMeshMask();

    /**
     * optional overrides for textures
     */
    private Map<String,ActorTextureMask> textureMap = null;

    /**
     * A map of mesh -> uniforms to apply to the mesh
     */
    private ActorUniformMap uniformMap = new ActorUniformMap();
    










    /**
     * Creates an achor
     * @param modelPath The path of the model associated with the actor
     */
    public Actor(String modelPath){
        this.baseModelPath = modelPath;
        this.animationData = new ActorAnimationData(this);
    }
    
    /**
     * Applies spatial data on the actor, this includes the model matrix as well as the real world position
     * @param modelMatrix The model matrix
     * @param worldPos The world position of the model
     */
    public void applySpatialData(Matrix4d modelMatrix, Vector3d worldPos){
        Model model = Globals.assetManager.fetchModel(baseModelPath);
        if(model != null){
            this.modelMatrix.set(modelMatrix);
            this.worldPos.set(worldPos);
        }
    }
    
    /**
     * Draws an actor
     * @param renderPipelineState The render pipeline state to draw within
     */
    public void draw(RenderPipelineState renderPipelineState, OpenGLState openGLState){
        Globals.profiler.beginAggregateCpuSample("Actor.draw");

        //
        //fetch the model
        String pathToFetch = this.baseModelPath;
        if(this.lodLevel <= Actor.LOD_LEVEL_LOWER && this.lowResBaseModelPath != null){
            pathToFetch = this.lowResBaseModelPath;
        }
        Model model = Globals.assetManager.fetchModel(pathToFetch);
        if(model == null){
            Globals.profiler.endCpuSample();
            return;
        }

        //
        //update core data on the model
        model.setModelMatrix(this.modelMatrix);
        model.setWorldPos(this.worldPos);

        //
        //frustum cull
        if(!Actor.isWithinFrustumBox(renderPipelineState,model,frustumCull)){
            Globals.profiler.endCpuSample();
            return;
        }

        //
        //main draw logic
        this.animationData.applyAnimationMasks(model);
        meshMask.processMeshMaskQueue();
        model.setMeshMask(meshMask);
        model.setTextureMask(textureMap);
        this.animationData.calculateNodeTransforms(model);
        //apply uniform overrides
        if(this.uniformMap.getMeshes() != null && this.uniformMap.getMeshes().size() > 0){
            for(String meshName : this.uniformMap.getMeshes()){
                List<UniformValue> uniforms = this.uniformMap.getUniforms(meshName);
                for(UniformValue uniform : uniforms){
                    model.pushUniformToMesh(meshName, uniform.getUniformName(), uniform.getValue());
                }
            }
        }
        model.draw(renderPipelineState,openGLState);
        model.setTextureMask(null);
        Globals.profiler.endCpuSample();
    }

    /**
     * <p>
     * Checks if the call to draw this actor would be a standard call.
     * </p>
     * <p>
     * IE, does it apply a mesh mask, animation, shader mark, etc? If so, it's not a static draw call
     * </p>
     * @return true if it is a static call, false otherwise
     */
    public boolean isStaticDrawCall(){
        return 
        //is low lod level
        this.lodLevel == Actor.LOD_LEVEL_STATIC ||
        //actor doesn't have anything complicated render-wise (animations, custom textures, etc)
        (
            this.animationData.isPlayingAnimation() &&
            this.meshMask.getBlockedMeshes().size() == 0 &&
            this.textureMap == null &&
            this.uniformMap.isEmpty() &&
            this.animationData.getStaticMorph() == null
        )
        ;
    }

    /**
     * <p>
     * Checks if the call to draw this actor would be a standard call.
     * </p>
     * <p>
     * IE, does it apply a mesh mask, animation, shader mark, etc? If so, it's not a static draw call
     * </p>
     * @return true if it is a static call, false otherwise
     */
    public String getStatisDrawStatus(){
        String rVal = "";
        rVal = rVal + this.isStaticDrawCall() + "\n";
        rVal = rVal + this.animationData.isPlayingAnimation() + "\n";
        rVal = rVal + (this.meshMask.getBlockedMeshes().size() == 0) + "\n";
        rVal = rVal + (this.textureMap == null) + "\n";
        rVal = rVal + this.uniformMap.isEmpty() + "\n";
        rVal = rVal + (this.animationData.getStaticMorph() == null) + "\n";
        return rVal;
    }

    /**
     * Frustum tests the actor at a given position
     * @param renderPipelineState The render pipeline state
     * @param position The position to test at
     * @return true if it should draw, false otherwise
     */
    public boolean frustumTest(RenderPipelineState renderPipelineState, Vector3d position){
        Model model = Globals.assetManager.fetchModel(baseModelPath);
        if(model != null && Actor.isWithinFrustumBox(renderPipelineState,model,position,frustumCull)){
            return true;
        }
        return false;
    }
    
    /**
     * Draws this actor in the ui pipeline
     */
    public void drawUI(){
        Model model = Globals.assetManager.fetchModel(baseModelPath);
        if(model != null){
            model.drawUI();
        }
    }
    
    /**
     * Gets the base model's path
     * @return The base model's path
     */
    public String getBaseModelPath(){
        return baseModelPath;
    }

    /**
     * Checks if the model is loaded for this actor
     * @return true if it is loaded, false otherwise
     */
    public boolean modelIsLoaded(){
        Model model = Globals.assetManager.fetchModel(baseModelPath);
        if(model != null){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the mesh mask for this actor
     * @return The mesh mask
     */
    public ActorMeshMask getMeshMask(){
        return meshMask;
    }

    /**
     * Adds a texture mask to this actor
     * @param textureMask The texture mask
     */
    public void addTextureMask(ActorTextureMask textureMask){
        if(textureMap == null){
            textureMap = new HashMap<String,ActorTextureMask>();
        }
        textureMap.put(textureMask.getMeshName(),textureMask);
    }

    /**
     * Sets the value of a uniform on a given mesh within this actor
     * @param meshName The name of the mesh
     * @param uniformName The name of the uniform
     * @param value The value of the uniform
     */
    public void setUniformOnMesh(String meshName, String uniformName, Object value){
        this.uniformMap.setUniform(meshName, uniformName, value);
    }
    
    /**
     * Sets whether this actor should frustum cull or not
     * @param frustumCull true to frustum cull, false to skip frustum culling
     */
    public void setFrustumCull(boolean frustumCull){
        this.frustumCull = frustumCull;
    }

    /**
     * Gets whether this actor should frustum cull or not
     * @return true to frustum cull, false otherwise
     */
    public boolean getFrustumCull(){
        return frustumCull;
    }

    

    /**
     * Checks if a given model is within the render pipeline state's frustum box
     * @param renderPipelineState The render pipeline state
     * @param model The model
     * @param frustumCull Controls whether the frustum cull should actually be executed or not
     * @return true if it is within the box, false otherwise
     */
    private static boolean isWithinFrustumBox(RenderPipelineState renderPipelineState, Model model, boolean frustumCull){
        if(!frustumCull){
            return true;
        }
        Globals.profiler.beginAggregateCpuSample("Actor.isWithinFrustumBox");
        Sphered sphere = model.getBoundingSphere();
        Vector3d modelPosition = model.getModelMatrix().getTranslation(new Vector3d());
        boolean check = renderPipelineState.getFrustumIntersection().testSphere((float)(sphere.x + modelPosition.x), (float)(sphere.y + modelPosition.y), (float)(sphere.z + modelPosition.z), (float)sphere.r);
        JomlPool.release(modelPosition);
        Globals.profiler.endCpuSample();
        return check;
    }

    /**
     * Checks if a given model is within the render pipeline state's frustum box
     * @param renderPipelineState The render pipeline state
     * @param model The model
     * @param position The position of the model
     * @param frustumCull Controls whether the frustum cull should actually be executed or not
     * @return true if it is within the box, false otherwise
     */
    private static boolean isWithinFrustumBox(RenderPipelineState renderPipelineState, Model model, Vector3d position, boolean frustumCull){
        if(!frustumCull){
            return true;
        }
        Globals.profiler.beginAggregateCpuSample("Actor.isWithinFrustumBox");
        Sphered sphere = model.getBoundingSphere();
        boolean check = renderPipelineState.getFrustumIntersection().testSphere((float)(sphere.x + position.x), (float)(sphere.y + position.y), (float)(sphere.z + position.z), (float)sphere.r);
        Globals.profiler.endCpuSample();
        return check;
    }

    /**
     * Gets the scaling applied by the actor
     * @return The scaling
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets the scaling applied by the actor
     * @param scale The scaling
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Gets the path to the low res model
     * @return Path to the low res model
     */
    public String getLowResBaseModelPath() {
        return lowResBaseModelPath;
    }

    /**
     * Sets the path to the low res model
     * @param lowResPath The path to the low res model
     */
    public void setLowResBaseModelPath(String lowResPath) {
        this.lowResBaseModelPath = lowResPath;
    }
    
    /**
     * Gets the lod level
     * @return The lod level
     */
    public int getLodLevel(){
        return this.lodLevel;
    }

    /**
     * Sets the lod level of the actor
     * @param lodLevel The lod level
     */
    public void setLodLevel(int lodLevel){
        this.lodLevel = lodLevel;
    }

    /**
     * Gets the animation data for the actor
     * @return
     */
    public ActorAnimationData getAnimationData(){
        return this.animationData;
    }


}
