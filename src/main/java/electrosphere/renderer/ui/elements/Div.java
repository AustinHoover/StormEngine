package electrosphere.renderer.ui.elements;

import org.joml.Vector3f;
import org.lwjgl.util.yoga.Yoga;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.ClickableElement;
import electrosphere.renderer.ui.elementtypes.DraggableElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.NavigableElement;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.DragEvent;
import electrosphere.renderer.ui.events.DragEvent.DragEventType;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.NavigationEvent;

public class Div extends StandardContainerElement implements ClickableElement,DraggableElement,DrawableElement,NavigableElement {

    ClickEventCallback onClick;
    DragEventCallback onDragStart;
    DragEventCallback onDrag;
    DragEventCallback onDragRelease;
    NavigationEventCallback onNavigate;
    boolean focused = false;

    
    public boolean visible = false;

    static final Vector3f windowDrawDebugColor = new Vector3f(1.0f,1.0f,1.0f);

    /**
     * Creates a new div
     * @return The div
     */
    public static Div createDiv(){
        return new Div();
    }

    /**
     * Creates a div that will behave like a row
     * @param children The elements to include within the row
     * @return The div
     */
    public static Div createRow(Element ... children){
        Div rVal = new Div();
        rVal.setFlexDirection(YogaFlexDirection.Row);
        if(children != null && children.length > 0){
            for(Element child : children){
                rVal.addChild(child);
            }
        }
        return rVal;
    }

    /**
     * Creates a div that will behave like a column
     * @param children the elements to include within the column
     * @return The div
     */
    public static Div createCol(Element ... children){
        Div rVal = new Div();
        rVal.setFlexDirection(YogaFlexDirection.Column);
        if(children != null && children.length > 0){
            for(Element child : children){
                rVal.addChild(child);
            }
        }
        return rVal;
    }

    /**
     * Creates a wrapper div
     * @param child The child to wrap
     * @param width The width of the wrapper
     * @param height The height of the wrapper
     * @return The wrapper element
     */
    public static Div createWrapper(Element child, int width, int height){
        Div rVal = new Div();
        rVal.setWidth(width);
        rVal.setHeight(height);
        if(child != null){
            rVal.addChild(child);
        }
        return rVal;
    }

    /**
     * Constructor
     */
    private Div(){
        super();
        Yoga.YGNodeStyleSetDisplay(yogaNode, Yoga.YGDisplayFlex);
    }


    public void setFocus(boolean focus){
        this.focused = focus;
    }

    @Override
    public void setOnDragStart(DragEventCallback callback) {
        this.onDragStart = callback;
    }

    @Override
    public void setOnDrag(DragEventCallback callback) {
        this.onDrag = callback;
    }

    @Override
    public void setOnDragRelease(DragEventCallback callback) {
        this.onDragRelease = callback;
    }

    @Override
    public void setOnClick(ClickEventCallback callback) {
        this.onClick = callback;
    }




    @Override
    public boolean handleEvent(Event event) {
        boolean propagate = true;
        if(onClick != null){
            if(event instanceof ClickEvent){
                if(!onClick.execute((ClickEvent)event)){
                    propagate = false;
                }
            }
        }
        if(event instanceof DragEvent){
            if(onDragStart != null && ((DragEvent)event).getType() == DragEventType.START){
                if(!onDragStart.execute((DragEvent)event)){
                    propagate = false;
                }
            }
            if(onDrag != null && ((DragEvent)event).getType() == DragEventType.DRAG){
                if(!onDrag.execute((DragEvent)event)){
                    propagate = false;
                }
            }
            if(onDragRelease != null && ((DragEvent)event).getType() == DragEventType.RELEASE){
                if(!onDragRelease.execute((DragEvent)event)){
                    propagate = false;
                }
            }
        }
        if(event instanceof NavigationEvent){
            if(onNavigate != null){
                if(onNavigate.execute((NavigationEvent)event)){
                    propagate = false;
                }
            }
        }
        return propagate;
    }

    @Override
    public boolean getVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean draw) {
        this.visible = draw;
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
                drawableChild.draw(renderPipelineState,openGLState,framebuffer,framebufferPosX,framebufferPosY);
            }
        }
    }

    @Override
    public void setOnNavigationCallback(NavigationEventCallback callback) {
        onNavigate = callback;
    }







    
}
