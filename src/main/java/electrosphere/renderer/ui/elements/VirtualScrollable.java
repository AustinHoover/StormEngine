package electrosphere.renderer.ui.elements;

import org.lwjgl.util.yoga.Yoga;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.ScrollableElement;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.ScrollEvent;

/**
 * A scrollable container that renders to it's parent framebuffer instead of a dedicated one
 */
public class VirtualScrollable extends StandardContainerElement implements DrawableElement, ScrollableElement {

    /**
     * Default scaling applied to scrolling
     */
    static final float DEFAULT_SCROLL_SCALE = 5.0f;

    /**
     * the current amount of scroll applied to this element
     */
    double scroll = 0;

    /**
     * the scrollable callback
     */
    ScrollEventCallback callback;

    /**
     * should we draw this element
     */
    boolean visible = true;

    /**
     * The child div
     */
    Div childDiv;

    /**
     * Constructor
     */
    public VirtualScrollable(int width, int height){
        super();
        this.setWidth(width);
        this.setHeight(height);
        Yoga.YGNodeStyleSetOverflow(this.yogaNode, Yoga.YGOverflowScroll);
        this.childDiv = Div.createCol();
        this.childDiv.setPositionType(YogaPositionType.Relative);
        super.addChild(this.childDiv);
    }

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
        ) {
        for(Element child : this.childDiv.getChildren()){
            if(child instanceof DrawableElement){
                DrawableElement drawableChild = (DrawableElement) child;
                if(this.childIsInBounds(drawableChild)){
                    drawableChild.draw(
                        renderPipelineState,
                        openGLState,
                        framebuffer,
                        framebufferPosX,
                        framebufferPosY
                    );
                }
            }
        }
    }

    /**
     * Checks if a given child element should be visible
     * @param element the element
     * @return true if visible, false otherwise
     */
    private boolean childIsInBounds(DrawableElement element){
        boolean rVal = true;
        if(element.getAbsoluteY() < this.getAbsoluteY() || element.getAbsoluteY() > this.getHeight() + this.getAbsoluteY()){
            return false; 
        }
        return rVal;
    }

    @Override
    public boolean getVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean draw) {
        this.visible = draw;
    }

    @Override
    public int getChildOffsetY(){
        return (int)scroll;
    }

    @Override
    public void setOnScrollCallback(ScrollEventCallback callback) {
        this.callback = callback;
    }

    /**
     * Default handling for the scroll event
     * @param event The scroll event
     */
    private void defaultScrollHandling(ScrollEvent event){
        scroll = scroll + event.getScrollAmount() * DEFAULT_SCROLL_SCALE;
        if(scroll > 0){
            scroll = 0;
        }
        //calculate max scroll
        double maxScroll = 0;
        maxScroll = this.childDiv.getHeight() - this.getHeight();
        if(scroll < - maxScroll){
            scroll = -maxScroll;
        }
        this.childDiv.setPositionY((int)scroll);
        // this.setPositionY((int)scroll);
    }

    @Override
    public void addChild(Element child) {
        this.childDiv.addChild(child);
    }

    @Override
    public void removeChild(Element child) {
        this.childDiv.removeChild(child);
    }

    @Override
    public void clearChildren(){
        this.childDiv.clearChildren();
    }

    @Override
    public boolean handleEvent(Event event){
        boolean propagate = true;
        if(event instanceof ScrollEvent){
            ScrollEvent scrollEvent = (ScrollEvent)event;
            if(callback != null){
                propagate = callback.execute(scrollEvent);
            } else {
                this.defaultScrollHandling(scrollEvent);
                propagate = false;
            }
        }
        return propagate;
    }
    
}
