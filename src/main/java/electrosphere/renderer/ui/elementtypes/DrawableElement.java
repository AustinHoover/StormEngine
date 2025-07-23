package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;

/**
 * A UI Element that is actually drawable to the screen
 */
public interface DrawableElement extends Element {
    
    /**
     * Gets if the drawable is currently set to visible
     * @return true if visible, false otherwise
     */
    public boolean getVisible();

    /**
     * Sets the visibility status of the element
     * @param draw true for visible, false otherwise
     */
    public void setVisible(boolean draw);

    /**
     * Draws the element
     * @param renderPipelineState The render pipeline state
     * @param openGLState The opengl state
     * @param framebuffer The framebuffer to render to
     */
    public abstract void draw(RenderPipelineState renderPipelineState, OpenGLState openGLState, Framebuffer framebuffer, int framebufferPosX, int framebufferPosY);

    
}
