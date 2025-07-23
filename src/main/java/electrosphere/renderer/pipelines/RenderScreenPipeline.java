package electrosphere.renderer.pipelines;

import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;

/**
 * Renders the screen
 */
public class RenderScreenPipeline implements RenderPipeline {

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("RenderScreenPipeline.render");
        //bind default FBO
        openGLState.glBindFramebuffer(GL45.GL_FRAMEBUFFER,0);
        
        GL45.glClearColor(1.0f, 1.0f, 1.0f, 1.0f); 
        GL45.glClear(GL45.GL_COLOR_BUFFER_BIT);
        //
        //unbind texture channels
        //
        //What does this mean?
        //essentially there are two channels we're using to draw mesh textures
        //we have to glBindTexture to pointer 0 for BOTH channels, otherwise
        //the leftover texture gets used to draw the screen framebuffer quad
        //which doesnt work
        openGLState.glActiveTexture(GL40.GL_TEXTURE0);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE1);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE2);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE3);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE0);
        
        openGLState.glDepthTest(false);
        openGLState.glViewport(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        
        
        
        //render full screen quad
        openGLState.setActiveShader(renderPipelineState, RenderingEngine.screenTextureShaders);
        openGLState.glBindVertexArray(RenderingEngine.screenTextureVAO);
        //aaa
        switch(RenderingEngine.outputFramebuffer){
            case 0: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, Globals.renderingEngine.getPostProcessingPipeline().getFramebuffer().getTexture().getTexturePointer());
            } break;
            case 1: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.lightDepthBuffer.getDepthTexture().getTexturePointer());
            } break;
            case 2: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.volumeDepthBackfaceTexture.getTexturePointer());
            } break;
            case 3: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.volumeDepthFrontfaceTexture.getTexturePointer());
            } break;
            case 4: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.transparencyAccumulatorTexture.getTexturePointer());
            } break;
            case 5: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.gameImageNormalsTexture.getTexturePointer());
            } break;
            case 6: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.normalsOutlineTexture.getTexturePointer());
            } break;
            case 7: {
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.screenFramebuffer.getTexture().getTexturePointer());
            } break;
            case 8: {
                openGLState.setActiveShader(renderPipelineState, RenderingEngine.drawChannel);
                GL40.glUniform1f(GL40.glGetUniformLocation(openGLState.getActiveShader().getId(), "channel"),4);
                openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.screenTextureDepth.getTexturePointer());
            } break;
        }
        GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, 6);
        openGLState.glBindVertexArray(0);

        Globals.profiler.endCpuSample();
    }
    
}
