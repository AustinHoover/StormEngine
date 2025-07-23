package electrosphere.renderer.pipelines;

import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL40;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.target.DrawTargetAccumulator;
import electrosphere.renderer.target.DrawTargetAccumulator.ModelAccumulatorData;

/**
 * Draws normals for generating character outlines
 */
public class NormalsForOutlinePipeline implements RenderPipeline {

    /**
     * Cutoff for adding to shadow map pipeline draw accumulator
     */
    public static final double DRAW_CUTOFF_DIST = 30f;

    /**
     * The draw target accumulator
     */
    private DrawTargetAccumulator drawTargetAccumulator = new DrawTargetAccumulator();

    /**
     * The queue for non-static entities to draw
     */
    private List<Entity> standardDrawCall = new LinkedList<Entity>();

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("NormalsForOutlinePipeline.render");
        /*
        gameImageNormalsTexture;
    static Framebuffer gameImageNormalsFramebuffer;
    static ShaderProgram renderNormalsShader;
        */

        //bind screen fbo
        RenderingEngine.gameImageNormalsFramebuffer.bind(openGLState);
        openGLState.glDepthTest(true);
        openGLState.glBlend(false);
        openGLState.glBlendFunc(GL40.GL_SRC_ALPHA, GL40.GL_ONE_MINUS_SRC_ALPHA);
        openGLState.glDepthFunc(GL40.GL_LESS);
        GL40.glDepthMask(true);
        
        openGLState.glViewport(Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
        
        ///
        ///     R E N D E R I N G      S T U F F
        ///
        //Sets the background color.
        GL40.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);



        //
        // Set render pipeline state
        //
        renderPipelineState.setUseMeshShader(false);
        renderPipelineState.setBufferStandardUniforms(true);
        renderPipelineState.setBufferNonStandardUniforms(false);
        renderPipelineState.setUseMaterial(true);
        renderPipelineState.setUseShadowMap(true);
        renderPipelineState.setUseBones(true);
        renderPipelineState.setUseLight(true);



        Matrix4d modelTransformMatrix = new Matrix4d();

        openGLState.setActiveShader(renderPipelineState, RenderingEngine.renderNormalsShader);

        modelTransformMatrix = new Matrix4d();
        Vector3d posVec = new Vector3d();
        Vector3d scaleVec = new Vector3d();
        for(Entity currentEntity : this.standardDrawCall){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(
                currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)!=null
            ){
                //fetch actor
                Actor currentActor = EntityUtils.getActor(currentEntity);
                //calculate camera-modified vector3d
                Vector3d cameraCenter = scaleVec.set(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Vector3d cameraModifiedPosition = posVec.set(position).sub(cameraCenter);
                //calculate and apply model transform
                modelTransformMatrix = modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                modelTransformMatrix.scale(scaleVec.set(EntityUtils.getScale(currentEntity)));
                currentActor.applySpatialData(modelTransformMatrix,position);
                //draw
                currentActor.draw(renderPipelineState,openGLState);
            }
        }
        renderPipelineState.setUseBones(false);
        for(ModelAccumulatorData accumulator : this.drawTargetAccumulator.getCalls()){
            Model model = Globals.assetManager.fetchModel(accumulator.getModelPath());
            if(model != null){
                int count = accumulator.getCount();
                List<Matrix4d> transforms = accumulator.getTransforms();
                List<Vector3d> positions = accumulator.getPositions();
                model.setMeshMask(null);
                for(int meshIndex = 0; meshIndex < model.getMeshCount(); meshIndex++){
                    for(int i = 0; i < count; i++){
                        Vector3d position = positions.get(i);
                        Matrix4d transform = transforms.get(i);
                        model.setWorldPos(position);
                        model.setModelMatrix(transform);
                        model.drawMesh(renderPipelineState, openGLState, meshIndex);
                    }
                }
            }
        }

        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);

        Globals.profiler.endCpuSample();
    }

    /**
     * Checks if the entity should be drawn
     * @param entity The entity
     * @return true if should draw, false otherwise
     */
    public static boolean shouldDraw(Entity entity){
        return
        (
            (boolean)entity.getData(EntityDataStrings.DATA_STRING_DRAW) && 
            entity.getData(EntityDataStrings.DRAW_SOLID_PASS) != null && 
            entity.getData(EntityDataStrings.DRAW_OUTLINE) != null
        ) &&
        (
            !entityBlacklist(entity)
        )
        ;
    }

    /**
     * Checks whether the entity is on the blacklist for drawing in main pipeline or not
     * @param entity The entity
     * @return True if in blacklist, false otherwise
     */
    static boolean entityBlacklist(Entity entity){
        return 
        //don't draw first person view in this pipeline ever
        entity == Globals.clientState.firstPersonEntity ||

        //don't draw third person view if camera is first person
        (
            entity == Globals.clientState.playerEntity &&
            !Globals.controlHandler.cameraIsThirdPerson()
        ) ||
        (
            !Globals.controlHandler.cameraIsThirdPerson() &&
            AttachUtils.getParent(entity) != null &&
            AttachUtils.getParent(entity) == Globals.clientState.playerEntity
        ) ||

        //don't draw items if they're attached to viewmodel
        (
            Globals.clientState.firstPersonEntity != null &&
            !Globals.controlHandler.cameraIsThirdPerson() &&
            AttachUtils.hasParent(entity) &&
            AttachUtils.getParent(entity) == Globals.clientState.firstPersonEntity
        )
        ;
    }

    /**
     * Gets the draw target accumulator
     * @return The draw target accumulator
     */
    public DrawTargetAccumulator getDrawTargetAccumulator(){
        return drawTargetAccumulator;
    }

    /**
     * Gets the queue of standard entities to draw
     * @return The queue of standard entites
     */
    public List<Entity> getStandardEntityQueue(){
        return standardDrawCall;
    }
    
}
