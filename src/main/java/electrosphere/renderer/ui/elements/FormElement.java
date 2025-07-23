package electrosphere.renderer.ui.elements;

import org.lwjgl.util.yoga.Yoga;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.Event;

public class FormElement extends StandardContainerElement implements DrawableElement {

    public boolean visible = false;

    public boolean focused = false;

    public FormElement(){
        super();
        Yoga.YGNodeStyleSetDisplay(yogaNode, Yoga.YGDisplayFlex);
    }

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ) {
        for(Element child : childList){
            if(child instanceof DrawableElement){
                DrawableElement drawableChild = (DrawableElement) child;
                drawableChild.draw(renderPipelineState,openGLState,framebuffer, framebufferPosX, framebufferPosY);
            }
        }
    }

    public void onFocus(){
    }

    public boolean getVisible() {
        return visible;
    }

    public boolean isFocused(){
        return focused;
    }

    public void setVisible(boolean draw) {
        this.visible = draw;
        for(Element child : childList){
            if(child instanceof DrawableElement){
                DrawableElement drawableChild = (DrawableElement) child;
                drawableChild.setVisible(draw);
            }
        }
    }
    
    public void setFocused(boolean focused){
        this.focused = focused;
    }

    public boolean handleEvent(Event event){
        return true;
    }
    
}
