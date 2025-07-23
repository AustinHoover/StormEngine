package electrosphere.renderer.actor.instance;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;

/**
 * Effectively controls the block of data that is passed to the gpu each time this instance is drawn.
 * Does priority management to draw the instanced of higher priority if there happens to be greater than the capacity number of items to draw.
 */
public interface InstanceData {

    /**
     * Adds an actor to be sorted in the queue
     * @param actor The actor to be sorted
     */
    public void addInstance(InstancedActor actor);

    /**
     * Gets the number of entries that are to be drawn
     * @return The number of entries to be drawn
     */
    public int getDrawCount();

    /**
     * Clears the queue
     */
    public void clearDrawQueue();

    /**
     * Fills the buffers for the upcoming render call. The intention is to make this emberassingly parallel.
     */
    public void fillBuffers();

    /**
     * Flips the buffer(s)
     */
    public void flip();

    /**
     * Gets the vertex shader associated with this data
     * @return The vertex shader
     */
    public String getVertexShader();

    /**
     * Gets the fragment shader associated with this data
     * @return The fragment shader
     */
    public String getFragmentShader();

    /**
     * Uploads the instance data
     */
    public void upload(OpenGLState openGLState, RenderPipelineState renderPipelineState);

    /**
     * Destroys the data
     */
    public void destroy();

}
