package electrosphere.renderer.pipelines;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;

/**
 * A render pipeline
 */
public interface RenderPipeline {
    
    /**
     * Executes the pipeline
     * @param renderPipelineState the current state of the rendering engine
     */
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState);

}
