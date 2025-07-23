package electrosphere.renderer.pipelines;

import org.lwjgl.opengl.GL40;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.framebuffer.FramebufferUtils;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.renderer.texture.Texture;

/**
 * 
 */
public class PostProcessingPipeline implements RenderPipeline {

    /**
     * The shader to render with
     */
    VisualShader postProcessingShader;

    /**
     * The buffer to render post processing effects to
     */
    Framebuffer postProcessBuffer;

    /**
     * Controls whether blur is applied or not
     */
    boolean applyBlur = false;

    /**
     * Init the pipeline
     */
    public void init(OpenGLState openGLState){
        postProcessingShader = VisualShader.loadSpecificShader("Shaders/core/postprocessing/postprocessing.vs", "Shaders/core/postprocessing/postprocessing.fs");
        Texture screenTextureColor = FramebufferUtils.generateScreenTextureColorAlpha(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
        Texture screenTextureDepth = FramebufferUtils.generateScreenTextureDepth(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
        try {
            postProcessBuffer = FramebufferUtils.generateScreenTextureFramebuffer(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY(), screenTextureColor, screenTextureDepth);
        } catch (Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }
    }

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("PostProcessingPipeline.render");
        //
        //Setup to render screen textures & bind screen framebuffer
        //
        openGLState.glDepthTest(false);
        openGLState.glBlend(false);
        openGLState.glViewport(Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());


        postProcessBuffer.bind(openGLState);
        openGLState.setActiveShader(renderPipelineState, postProcessingShader);
        RenderingEngine.screenFramebuffer.getTexture().bind(openGLState);


        //
        //Set post processing data
        //
        if(applyBlur){
            openGLState.getActiveShader().setUniform(openGLState, "applyBlur", 1);
        } else {
            openGLState.getActiveShader().setUniform(openGLState, "applyBlur", 0);
        }

        //
        //Draw
        //
        openGLState.glBindVertexArray(RenderingEngine.screenTextureVAO);
        GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, 6);







        //
        //Close down pipeline
        //
        openGLState.glBindVertexArray(0);
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);


        Globals.profiler.endCpuSample();
    }

    /**
     * Gets the framebuffer for this pipeline step
     * @return The framebuffer
     */
    public Framebuffer getFramebuffer(){
        return this.postProcessBuffer;
    }

    /**
     * Gets whether blur is being applied or not
     * @return true if bluring, false otherwise
     */
    public boolean isApplyingBlur(){
        return applyBlur;
    }

    /**
     * Sets whether blur will be applied or not
     * @param applyBlur true to blur, false otherwise
     */
    public void setApplyBlur(boolean applyBlur){
        this.applyBlur = applyBlur;
    }
    
}
