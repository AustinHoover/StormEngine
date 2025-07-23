package electrosphere.renderer.pipelines;

import java.util.Set;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL40;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.Actor;

/**
 * Updates the volume buffer
 */
public class VolumeBufferPipeline implements RenderPipeline {

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("VolumeBufferPipeline.render");

        Set<Entity> depthEntities = Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_VOLUMETIC_DEPTH_PASS);

        if(depthEntities != null && depthEntities.size() > 0){
            Matrix4d modelTransformMatrix = new Matrix4d();
            
            //set the viewport to shadow map size
            openGLState.glViewport(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            openGLState.glDepthTest(true);
            openGLState.glDepthFunc(GL40.GL_LESS);
            GL40.glDepthMask(true);
            
            //stop rendering front faces
            GL40.glEnable(GL40.GL_CULL_FACE);
            GL40.glCullFace(GL40.GL_FRONT);

            //setup rendering for back faces
            openGLState.setActiveShader(renderPipelineState, RenderingEngine.volumeDepthShaderProgram);
            RenderingEngine.volumeDepthBackfaceFramebuffer.bind(openGLState);
            GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);
            openGLState.glActiveTexture(GL40.GL_TEXTURE0);

            GL40.glUniformMatrix4fv(GL40.glGetUniformLocation(openGLState.getActiveShader().getId(), "view"), false, Globals.renderingEngine.getViewMatrix().get(new float[16]));
            GL40.glUniformMatrix4fv(GL40.glGetUniformLocation(openGLState.getActiveShader().getId(), "projection"), false, RenderingEngine.nearVolumeProjectionMatrix.get(new float[16]));
            GL40.glUniform1f(GL40.glGetUniformLocation(openGLState.getActiveShader().getId(), "linearCoef"), RenderingEngine.volumeDepthLinearCoef);
            GL40.glUniform1f(GL40.glGetUniformLocation(openGLState.getActiveShader().getId(), "quadCoef"), RenderingEngine.volumeDepthQuadCoef);
            GL40.glUniform1f(GL40.glGetUniformLocation(openGLState.getActiveShader().getId(), "near"), 0.1f);
            GL40.glUniform1f(GL40.glGetUniformLocation(openGLState.getActiveShader().getId(), "far"), 100f);
            
            //
            // Set render pipeline state
            //
            renderPipelineState.setUseMeshShader(false);
            renderPipelineState.setBufferStandardUniforms(false);
            renderPipelineState.setBufferNonStandardUniforms(true);
            renderPipelineState.setUseMaterial(false);
            renderPipelineState.setUseShadowMap(false);
            renderPipelineState.setUseBones(true);
            renderPipelineState.setUseLight(false);


            //
            //     D R A W     A L L     E N T I T I E S
            //
            for(Entity currentEntity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_VOLUMETIC_DEPTH_PASS)){
                Vector3d position = EntityUtils.getPosition(currentEntity);
                if(
                    currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)!=null
                ){
                    //fetch actor
                    Actor currentActor = EntityUtils.getActor(currentEntity);
                    //calculate camera-modified vector3d
                    Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    modelTransformMatrix = modelTransformMatrix.identity();
                    modelTransformMatrix.translate(cameraModifiedPosition);
                    modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                    modelTransformMatrix.scale(new Vector3d(EntityUtils.getScale(currentEntity)));
                    currentActor.applySpatialData(modelTransformMatrix,position);
                    currentActor.draw(renderPipelineState,openGLState);
                }
            }

            //
            //Draw front faces of all non-volumetrics
            //
            for(Entity currentEntity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_VOLUMETIC_SOLIDS_PASS)){
                Vector3d position = EntityUtils.getPosition(currentEntity);
                if(
                    (boolean)currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)
                ){
                    //fetch actor
                    Actor currentActor = EntityUtils.getActor(currentEntity);
                    //calculate camera-modified vector3d
                    Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    //set projection matrix
                    modelTransformMatrix = modelTransformMatrix.identity();
                    modelTransformMatrix.translate(cameraModifiedPosition);
                    modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                    modelTransformMatrix.scale(new Vector3d(EntityUtils.getScale(currentEntity)));
                    currentActor.applySpatialData(modelTransformMatrix,position);
                    currentActor.draw(renderPipelineState,openGLState);
                }
            }



            //stop rendering front faces
            GL40.glEnable(GL40.GL_CULL_FACE);
            GL40.glCullFace(GL40.GL_BACK);

            //setup state for depth testing front faces
            openGLState.setActiveShader(renderPipelineState, RenderingEngine.volumeDepthShaderProgram);
            RenderingEngine.volumeDepthFrontfaceFramebuffer.bind(openGLState);
            GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);
            openGLState.glActiveTexture(GL40.GL_TEXTURE0);

            

            //
            //     D R A W     A L L     E N T I T I E S
            //
            for(Entity currentEntity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_VOLUMETIC_DEPTH_PASS)){
                Vector3d position = EntityUtils.getPosition(currentEntity);
                if(
                    (boolean)currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)
                ){
                    //fetch actor
                    Actor currentActor = EntityUtils.getActor(currentEntity);
                    //calculate camera-modified vector3d
                    Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    //calculate and apply model transform
                    modelTransformMatrix = modelTransformMatrix.identity();
                    modelTransformMatrix.translate(cameraModifiedPosition);
                    modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                    modelTransformMatrix.scale(new Vector3d(EntityUtils.getScale(currentEntity)));
                    currentActor.applySpatialData(modelTransformMatrix,position);
                    //draw
                    currentActor.draw(renderPipelineState,openGLState);
                }
            }

            GL40.glCullFace(GL40.GL_BACK);
            //now cull back faces
            
            //reset texture
            openGLState.glActiveTexture(GL40.GL_TEXTURE0);
            openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
            //bind default framebuffer
            openGLState.glBindFramebuffer(GL40.GL_FRAMEBUFFER,0);
            //resume culling backface
            GL40.glDisable(GL40.GL_CULL_FACE);
        }

        Globals.profiler.endCpuSample();
    }
    
}
