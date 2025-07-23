package electrosphere.renderer.pipelines;

import org.lwjgl.opengl.GL40;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;

public class CompositePipeline implements RenderPipeline {

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("CompositePipeline.render");
        //
        //Setup to render screen textures & bind screen framebuffer
        //
        openGLState.glDepthFunc(GL40.GL_ALWAYS);
        // glDepthMask(false);
        openGLState.glBlend(true);
        openGLState.glBlendFunc(GL40.GL_SRC_ALPHA, GL40.GL_ONE_MINUS_SRC_ALPHA);
        
        RenderingEngine.screenFramebuffer.bind(openGLState);
        
        openGLState.glBindVertexArray(RenderingEngine.screenTextureVAO);


        //
        //Draw anime outline
        //
        openGLState.setActiveShader(renderPipelineState, RenderingEngine.compositeAnimeOutline);

        openGLState.glActiveTexture(GL40.GL_TEXTURE0);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE1);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE2);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE3);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE0);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.normalsOutlineTexture.getTexturePointer());

        GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, 6);

        //
        //Composite transparency on top of solids
        //
        openGLState.setActiveShader(renderPipelineState, RenderingEngine.oitCompositeProgram);

        openGLState.glActiveTexture(GL40.GL_TEXTURE0);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE1);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE2);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE3);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
        openGLState.glActiveTexture(GL40.GL_TEXTURE0);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.transparencyAccumulatorTexture.getTexturePointer());
        openGLState.glActiveTexture(GL40.GL_TEXTURE1);
        openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.transparencyRevealageTexture.getTexturePointer());

        GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, 6);







        //
        //Close down pipeline
        //
        openGLState.glBindVertexArray(0);
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);


        Globals.profiler.endCpuSample();
    }
    
}
