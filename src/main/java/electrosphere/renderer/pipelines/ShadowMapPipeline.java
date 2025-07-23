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
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.target.DrawTargetAccumulator;
import electrosphere.renderer.target.DrawTargetAccumulator.ModelAccumulatorData;
import electrosphere.util.math.SpatialMathUtils;

/**
 * Shadow map pipeline
 */
public class ShadowMapPipeline implements RenderPipeline {

    /**
     * The resolution of the shadow map
     */
    public static final int SHADOW_MAP_RESOLUTION = 4096;

    /**
     * Cutoff for adding to shadow map pipeline draw accumulator
     */
    public static final double DRAW_CUTOFF_DIST = 30f;

    /**
     * The eye of the camera that is used to render the shadow map
     */
    Vector3d cameraEye = new Vector3d(-1,20,-5.5);

    /**
     * The near plane distance
     */
    float nearPlane = 1f;

    /**
     * The far plane
     */
    float farPlane = 40f;

    /**
     * Sides of the orthagonal box
     */
    float sideLength = 50f;

    /**
     * Sets whether the far plane should update based on camera location or not
     */
    boolean updateFarPlane = true;

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
        Globals.profiler.beginCpuSample("ShadowMapPipeline.render");
        Matrix4d modelTransformMatrix = new Matrix4d();
        
        //set the viewport to shadow map size
        openGLState.glViewport(SHADOW_MAP_RESOLUTION, SHADOW_MAP_RESOLUTION);
        openGLState.glDepthTest(true);
        openGLState.glDepthFunc(GL40.GL_LEQUAL);
        
        openGLState.setActiveShader(renderPipelineState, RenderingEngine.lightDepthShaderProgram);
        RenderingEngine.lightDepthBuffer.bind(openGLState);

        GL40.glClearDepth(1.0);
        GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);
        openGLState.glActiveTexture(GL40.GL_TEXTURE0);

        float eyeX = (float)cameraEye.x;
        float eyeY = (float)cameraEye.y;
        float eyeZ = (float)cameraEye.z;
        // float eyeDist = (float)cameraEye.length();
        // float farPlane = eyeDist + 10.0f;
        // float sidesMagnitude = (float)Math.sqrt(eyeDist);
        float sidesMagnitude = sideLength;
        //set matrices for light render
        Matrix4d lightProjection = new Matrix4d().setOrtho(-sidesMagnitude, sidesMagnitude, -sidesMagnitude, sidesMagnitude, nearPlane, farPlane);//glm::ortho(-10.0f, 10.0f, -10.0f, 10.0f, near_plane, far_plane); 
        Matrix4d lightView = new Matrix4d().setLookAt(
                new Vector3d(eyeX, eyeY, eyeZ),
                new Vector3d( 0.0f, 0.0f,  0.0f),
                SpatialMathUtils.getUpVector()
        );
        Globals.renderingEngine.getLightDepthMatrix().set(lightProjection.mul(lightView));

        openGLState.getActiveShader().setUniform(openGLState, "lightSpaceMatrix", Globals.renderingEngine.getLightDepthMatrix());

    //    glCullFace(GL_FRONT);

        //
        // Set render pipeline state
        //
        renderPipelineState.setUseMeshShader(false);
        renderPipelineState.setBufferStandardUniforms(true);
        renderPipelineState.setBufferNonStandardUniforms(true);
        renderPipelineState.setUseMaterial(false);
        renderPipelineState.setUseShadowMap(false);
        renderPipelineState.setUseBones(true);
        renderPipelineState.setUseLight(false);

        //
        //     D R A W     A L L     E N T I T I E S
        //
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
        for(Entity currentEntity : this.drawTargetAccumulator.getTerrainEntities()){
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
                currentActor.applySpatialData(new Matrix4d(modelTransformMatrix),new Vector3d(position));
                //draw
                currentActor.draw(renderPipelineState,openGLState);
            }
        }
        for(Entity currentEntity : this.drawTargetAccumulator.getBlockEntities()){
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
                currentActor.applySpatialData(new Matrix4d(modelTransformMatrix),new Vector3d(position));
                //draw
                currentActor.draw(renderPipelineState,openGLState);
            }
        }


        
        //reset texture
        openGLState.glActiveTexture(GL40.GL_TEXTURE0);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        //bind default framebuffer
        openGLState.glBindFramebuffer(GL40.GL_FRAMEBUFFER,0);
        //reset the viewport to screen size
        openGLState.glViewport(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        //resume culling backface
//        glCullFace(GL_BACK);

        Globals.profiler.endCpuSample();
    }

    /**
     * Sets the far plane value
     * @param farPlane The far plane value
     */
    public void setFarPlane(float farPlane){
        this.farPlane = farPlane;
    }

    /**
     * Sets whether the far plane should update based on camera location or not
     * @param updateFarPlane true if should update, false otherwise
     */
    public void setUpdateFarPlane(boolean updateFarPlane){
        this.updateFarPlane = updateFarPlane;
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
