package electrosphere.renderer.pipelines;

import org.lwjgl.opengl.GL40;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.shader.VisualShader;

/**
 * Post processing pipeline
 */
public class OutlineNormalsPipeline implements RenderPipeline {

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("OutlineNormalsPipeline.render");
        //
        //      Outline normals
        //
        
        RenderingEngine.normalsOutlineFrambuffer.bind(openGLState);
        VisualShader program = Globals.assetManager.fetchShader("Shaders/core/anime/outlineNormals.vs", "Shaders/core/anime/outlineNormals.fs");
        if(program != null){
            openGLState.setActiveShader(renderPipelineState, program);

            openGLState.glBindVertexArray(RenderingEngine.screenTextureVAO);

            openGLState.glActiveTexture(GL40.GL_TEXTURE0);
            openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
            openGLState.glActiveTexture(GL40.GL_TEXTURE1);
            openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
            openGLState.glActiveTexture(GL40.GL_TEXTURE2);
            openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
            openGLState.glActiveTexture(GL40.GL_TEXTURE3);
            openGLState.glBindTexture(GL40.GL_TEXTURE_2D, 0);
            openGLState.glActiveTexture(GL40.GL_TEXTURE0);
            openGLState.glBindTexture(GL40.GL_TEXTURE_2D, RenderingEngine.gameImageNormalsTexture.getTexturePointer());

            GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, 6);
            openGLState.glBindVertexArray(0);
        }

        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
        Globals.profiler.endCpuSample();
    }
    
}
