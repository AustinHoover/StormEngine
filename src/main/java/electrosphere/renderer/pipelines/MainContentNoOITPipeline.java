package electrosphere.renderer.pipelines;

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
 * The main content render pipeline without oit passes
 */
public class MainContentNoOITPipeline implements RenderPipeline {

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("MainContentNoOITPipeline.render");

        //bind screen fbo
        RenderingEngine.screenFramebuffer.bind(openGLState);
        openGLState.glDepthTest(true);
        openGLState.glDepthFunc(GL40.GL_LESS);
        GL40.glDepthMask(true);
        openGLState.glViewport(Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());

        openGLState.glBlend(true);
        openGLState.glBlendFunci(0, GL40.GL_ONE, GL40.GL_ONE);
        openGLState.glBlendFunci(1, GL40.GL_ZERO, GL40.GL_ONE_MINUS_SRC_COLOR);
		GL40.glBlendEquation(GL40.GL_FUNC_ADD);
        
        ///
        ///     R E N D E R I N G      S T U F F
        ///
        //Sets the background color.
        GL40.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);


        //
        // Set render pipeline state
        //
        renderPipelineState.setUseMeshShader(true);
        renderPipelineState.setBufferStandardUniforms(true);
        renderPipelineState.setBufferNonStandardUniforms(false);
        renderPipelineState.setUseMaterial(true);
        renderPipelineState.setUseShadowMap(true);
        renderPipelineState.setUseBones(true);
        renderPipelineState.setUseLight(true);

        //
        //     D R A W     A L L     E N T I T I E S
        //
        for(Entity currentEntity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAWABLE)){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(
                (boolean)currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)
                ){
                //fetch actor
                Actor currentActor = EntityUtils.getActor(currentEntity);
                //calculate camera-modified vector3d
                Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                //calculate and apply model transform
                RenderingEngine.modelTransformMatrix.identity();
                RenderingEngine.modelTransformMatrix.translate(cameraModifiedPosition);
                RenderingEngine.modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                RenderingEngine.modelTransformMatrix.scale(new Vector3d(EntityUtils.getScale(currentEntity)));
                currentActor.applySpatialData(RenderingEngine.modelTransformMatrix,position);
                //draw
                currentActor.draw(renderPipelineState,openGLState);
            }
        }

        Globals.profiler.endCpuSample();
    }
    
}
