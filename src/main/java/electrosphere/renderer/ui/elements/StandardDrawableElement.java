package electrosphere.renderer.ui.elements;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.FocusableElement;

/**
 * A standard element that is drawable
 */
public class StandardDrawableElement extends StandardElement implements DrawableElement, FocusableElement {

    /**
     * Visibility status
     */
    boolean visible = true;

    /**
     * Focus status
     */
    boolean isFocused = false;

    @Override
    public boolean getVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean draw) {
        this.visible = draw;
    }

    @Override
    public void draw(RenderPipelineState renderPipelineState, OpenGLState openGLState, Framebuffer framebuffer, int framebufferPosX, int framebufferPosY) {
        throw new UnsupportedOperationException("Unimplemented method 'draw'");
    }

    @Override
    public boolean isFocused() {
        return this.isFocused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.isFocused = focused;
    }

    @Override
    public void setOnFocus(FocusEventCallback callback) {
        throw new UnsupportedOperationException("Unimplemented method 'setOnFocus'");
    }

    @Override
    public void setOnLoseFocus(FocusEventCallback callback) {
        throw new UnsupportedOperationException("Unimplemented method 'setOnLoseFocus'");
    }
    
}
