package electrosphere.renderer.actor.instance;

import java.util.List;

import org.joml.Matrix4d;
import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.queue.QueuedTexture;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.actor.mask.ActorUniformMap;
import electrosphere.renderer.actor.mask.ActorUniformMap.UniformValue;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.renderer.texture.Texture;

/**
 * An actor that will trigger an instance call when you draw the model; however it uses a texture to store data about its instances
 */
public class TextureInstancedActor {

    /**
     * Uniform location for the row size uniform
     */
    static final String uniformRowSize = "rowSize";

    //path of the model that this instanced actor uses
    String modelPath;

    //the material that will contain the data about the model
    Material material;

    //the draw count of the texture instanced actor
    int drawCount;

    /**
     * Size of a row of data
     * Used because textures are packed row-major, not column-major
     */
    int rowSize = 1;

    /**
     * The queued texture
     */
    QueuedTexture queuedTexture;

    /**
     * Set the queued texture pointer to the material
     */
    boolean setQueuedTexturePointer = false;

    //shader paths
    String vertexShaderPath;
    String fragmentShaderPath;

    /**
     * A map of mesh -> uniforms to apply to the mesh
     */
    ActorUniformMap uniformMap = new ActorUniformMap();

    /**
     * Creates an instanced actor
     * @param modelPath The path of the model this actor uses
     */
    protected TextureInstancedActor(String modelPath, String vertexShaderPath, String fragmentShaderPath, Texture dataTexture, int drawCount){
        this.modelPath = modelPath;
        this.material = new Material();
        this.material.setDiffuse(dataTexture);
        this.drawCount = drawCount;
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
    }

    /**
     * Creates an instanced actor
     * @param modelPath The path of the model this actor uses
     */
    protected TextureInstancedActor(String modelPath, String vertexShaderPath, String fragmentShaderPath, QueuedTexture dataTexture, int drawCount, int rowSize){
        this.modelPath = modelPath;
        this.material = new Material();
        this.queuedTexture = dataTexture;
        this.drawCount = drawCount;
        this.rowSize = rowSize;
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
    }

    /**
     * Attaches a TextureInstancedActor to an entity
     * @param parent The entity
     * @param modelPath The path to the model for this instanced actor
     * @param dataTexture The data texture containing data for this actor
     */
    public static void attachTextureInstancedActor(Entity parent, String modelPath, String vertexShaderPath, String fragmentShaderPath, Texture dataTexture, int drawCount){
        TextureInstancedActor newActor = new TextureInstancedActor(modelPath, vertexShaderPath, fragmentShaderPath, dataTexture, drawCount);
        parent.putData(EntityDataStrings.TEXTURE_INSTANCED_ACTOR, newActor);
    }

    /**
     * Attaches a TextureInstancedActor to an entity
     * @param parent The entity
     * @param modelPath The path to the model for this instanced actor
     * @param vertexShaderPath The path to the vertex shader to use
     * @param fragmentShaderPath The path to the fragment shader to use
     * @param dataTexture The data texture containing data for this actor
     * @param drawCount The number of instances to draw
     * @param rowSize The size of a row of data
     * @return The TextureInstancedActor that was attached to the entity
     */
    public static TextureInstancedActor attachTextureInstancedActor(Entity parent, String modelPath, String vertexShaderPath, String fragmentShaderPath, QueuedTexture dataTexture, int drawCount, int rowSize){
        TextureInstancedActor newActor = new TextureInstancedActor(modelPath, vertexShaderPath, fragmentShaderPath, dataTexture, drawCount, rowSize);
        parent.putData(EntityDataStrings.TEXTURE_INSTANCED_ACTOR, newActor);
        return newActor;
    }
    
    /**
     * Draws the instanced actor. Should be called normally in a loop as if this was a regular actor.
     * @param renderPipelineState The pipeline state of the instanced actor
     * @param position The position used for frustum checking
     */
    public void draw(RenderPipelineState renderPipelineState, OpenGLState openGLState){
        Model model = Globals.assetManager.fetchModel(modelPath);
        VisualShader shader = Globals.assetManager.fetchShader(vertexShaderPath, fragmentShaderPath);
        if(queuedTexture != null && !setQueuedTexturePointer && queuedTexture.getTexture() != null){
            this.material.setDiffuse(queuedTexture.getTexture());
            setQueuedTexturePointer = true;
        }
        if(
            model != null &&
            shader != null &&
            (
                queuedTexture == null ||
                (
                    queuedTexture != null &&
                    queuedTexture.getTexture() != null
                )
            )
        ){
            //setup render pipeline
            boolean instancedState = renderPipelineState.getInstanced();
            boolean materialState = renderPipelineState.getUseMaterial();
            boolean useShader = renderPipelineState.getUseMeshShader();
            boolean bufferStandardUniforms = renderPipelineState.getBufferStandardUniforms();
            boolean bufferNonStandardUniforms = renderPipelineState.getBufferNonStandardUniforms();
            boolean useLight = renderPipelineState.getUseLight();
            renderPipelineState.setInstanced(true);
            renderPipelineState.setUseMaterial(false);
            renderPipelineState.setUseMeshShader(false);
            renderPipelineState.setInstanceCount(drawCount);
            renderPipelineState.setBufferStandardUniforms(true);
            renderPipelineState.setBufferNonStandardUniforms(true);
            renderPipelineState.setUseLight(true);
            renderPipelineState.setInstanceData(null); //need to set the instance data to null otherwise it will overwrite what we currently have set (ie overwrite draw calls count, etc)


            openGLState.setActiveShader(renderPipelineState, shader);
            shader.setUniform(openGLState, uniformRowSize, rowSize);
            this.material.applyMaterial(openGLState);

            //apply uniform overrides
            if(this.uniformMap.getMeshes() != null && this.uniformMap.getMeshes().size() > 0){
                for(String meshName : this.uniformMap.getMeshes()){
                    List<UniformValue> uniforms = this.uniformMap.getUniforms(meshName);
                    for(UniformValue uniform : uniforms){
                        model.pushUniformToMesh(meshName, uniform.getUniformName(), uniform.getValue());
                    }
                }
            }

            model.draw(renderPipelineState, openGLState);

            //reset render pipeline state
            renderPipelineState.setInstanced(instancedState);
            renderPipelineState.setUseMaterial(materialState);
            renderPipelineState.setUseMeshShader(useShader);
            renderPipelineState.setBufferStandardUniforms(bufferStandardUniforms);
            renderPipelineState.setBufferNonStandardUniforms(bufferNonStandardUniforms);
            renderPipelineState.setUseLight(useLight);
        }
    }

    /**
     * Gets the path of the model packing this instanced actore
     * @return The path of the model
     */
    protected String getModelPath(){
        return this.modelPath;
    }

    /**
     * Gets the texture instanced actor attached to this entity
     * @param parent The parent entity
     * @return The texture instanced actor if it exists
     */
    public static TextureInstancedActor getTextureInstancedActor(Entity parent){
        return (TextureInstancedActor)parent.getData(EntityDataStrings.TEXTURE_INSTANCED_ACTOR);
    }

    /**
     * Applies spatial data on the actor, this includes the model matrix as well as the real world position
     * @param modelMatrix The model matrix
     * @param worldPos the real world coordinates of the model
     */
    public void applySpatialData(Matrix4d modelMatrix, Vector3d worldPos){
        Model model = Globals.assetManager.fetchModel(modelPath);
        if(model != null){
            model.setModelMatrix(modelMatrix);
            model.setWorldPos(worldPos);
        }
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
     * Frees the texture instanced actor
     */
    public void free(){
        if(this.queuedTexture != null && this.queuedTexture.getTexture() != null){
            Globals.assetManager.queueTextureForDeletion(this.queuedTexture.getTexture().getPath());
        }
    }
}
