package electrosphere.renderer.ui.elements;

import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.ui.elementtypes.ClickableElement;
import electrosphere.renderer.ui.elementtypes.DraggableElement;
import electrosphere.renderer.ui.elementtypes.MenuEventElement;
import electrosphere.renderer.ui.elementtypes.ValueElement;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.DragEvent;
import electrosphere.renderer.ui.events.DragEvent.DragEventType;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.FocusEvent;
import electrosphere.renderer.ui.events.MenuEvent;
import electrosphere.renderer.ui.events.ValueChangeEvent;

/**
 * A ui element that is a slider that lets you pick between a range of values
 */
public class Slider extends StandardDrawableElement implements ClickableElement, DraggableElement, MenuEventElement, ValueElement {

    FocusEventCallback onFocusCallback;
    FocusEventCallback onLoseFocusCallback;
    DragEventCallback onDragStart;
    DragEventCallback onDrag;
    DragEventCallback onDragRelease;
    ClickEventCallback onClick;
    MenuEventCallback onMenuEvent;
    ValueChangeEventCallback onValueChange;

    float min = 0.0f;
    float max = 1.0f;
    float value = 0.5f;

    Vector4f colorBackground = new Vector4f(0.2f,0.2f,0.2f,1);
    Vector4f colorForeground = new Vector4f(1,1,1,1);

    static Material mat;

    int drawMarginX = 5;
    int drawMarginY = 5;



    static final int idealMargin = 5; //5 pixels margin ideally

    static final Vector3f windowDrawDebugColor = new Vector3f(1.0f,1.0f,1.0f);

    /**
     * The default width of a slider
     */
    static final int DEFAULT_HEIGHT = 20;

    /**
     * The default height of a slider
     */
    static final int DEFAULT_WIDTH = 100;


    /**
     * Creates a slider element
     * @param callback the Logic to fire when the slider changes value
     * @return the slider element
     */
    public static Slider createSlider(Consumer<ValueChangeEvent> callback){
        Slider slider = new Slider();
        slider.setOnValueChangeCallback(new ValueChangeEventCallback() {public void execute(ValueChangeEvent event) {
            callback.accept(event);
        }});
        return slider;
    }

    /**
     * Private constructor
     */
    private Slider(){
        super();
        if(mat == null){
            mat = Material.create("Textures/ui/square.png");
        }
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }


    public Slider(int positionX, int positionY, int width, int height, Vector4f colorBackground, Vector4f colorForeground){
        super();
        if(mat == null){
            mat = Material.create("Textures/ui/square.png");
        }
        setPositionX(positionX);
        setPositionY(positionY);
        setWidth(width);
        setHeight(height);
        this.colorBackground.set(colorBackground);
        this.colorForeground.set(colorForeground);
    }



    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ) {
        
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

        int drawMarginX = Math.max(this.getWidth() - idealMargin * 2, 0);
        if(drawMarginX < idealMargin){
            drawMarginX = 0;
        } else {
            drawMarginX = idealMargin;
        }
        int drawMarginY = Math.max(this.getHeight() - idealMargin * 2, 0);
        if(drawMarginY < idealMargin){
            drawMarginY = 0;
        } else {
            drawMarginY = idealMargin;
        }


        float ndcWidth =  (float)getWidth()/framebuffer.getWidth();
        float ndcHeight = (float)getHeight()/framebuffer.getHeight();
        float ndcX =      (float)this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX)/framebuffer.getWidth();
        float ndcY =      (float)this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY)/framebuffer.getHeight();
        
        Vector3f boxPosition = new Vector3f(ndcX,ndcY,0);
        Vector3f boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);

        Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
        if(planeModel != null){
            //bounding box/margin
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", colorBackground);
            planeModel.getMeshes().get(0).setMaterial(mat);
            planeModel.drawUI();

            //actual slider
            ndcWidth = (float)((getWidth() - drawMarginX * 2) * getValueAsPercentage())/framebuffer.getWidth();
            ndcHeight = (float)(getHeight() - drawMarginY * 2)/framebuffer.getHeight();
            ndcX = (float)(this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX) + drawMarginX)/framebuffer.getWidth();
            ndcY = (float)(this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY) + drawMarginY)/framebuffer.getHeight();
            boxPosition = new Vector3f(ndcX,ndcY,0);
            boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", colorForeground);
            planeModel.getMeshes().get(0).setMaterial(mat);
            planeModel.drawUI();
        } else {
            LoggerInterface.loggerRenderer.ERROR("Window unable to find plane model!!", new Exception());
        }
    }



    public float getMinimum(){
        return min;
    }

    public void setMinimum(float min){
        this.min = min;
    }

    public float getMaximum(){
        return max;
    }

    public void setMaximum(float max){
        this.max = max;
    }

    public float getValue(){
        return value;
    }

    public void setValue(float value){
        this.value = value;
    }

    float getValueAsPercentage(){
        return (value - min) / (max - min);
    }
    
    @Override
    public void setOnFocus(FocusEventCallback callback) {
        onFocusCallback = callback;
    }

    @Override
    public void setOnLoseFocus(FocusEventCallback callback) {
        onLoseFocusCallback = callback;
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
    public void setOnMenuEventCallback(MenuEventCallback callback) {
        onMenuEvent = callback;
    }

    @Override
    public void setOnValueChangeCallback(ValueChangeEventCallback callback) {
        onValueChange = callback;
    }

    /**
     * Gets the current value from the percentage
     * @param percentage The percentage
     * @return The value
     */
    private float valueFromPercentage(float percentage){
        return (percentage * (max - min) + min);
    }


    @Override
    public boolean handleEvent(Event event) {
        boolean propagate = true;
        if(event instanceof FocusEvent){
            FocusEvent focusEvent = (FocusEvent)event;
            if(focusEvent.isFocused()){
                if(this.onFocusCallback != null){
                    propagate = this.onFocusCallback.execute(focusEvent);
                } else {
                    //default behavior/
                    colorForeground = new Vector4f(1,0.5f,0.5f,1);
                    propagate = true;
                }
            } else {
                if(this.onLoseFocusCallback != null){
                    propagate = this.onLoseFocusCallback.execute(focusEvent);
                } else {
                    //default behavior
                    colorForeground = new Vector4f(1,1,1,1);
                    propagate = true;
                }
            }
        } else if(event instanceof MenuEvent){
            MenuEvent menuEvent = (MenuEvent) event;
            if(onMenuEvent != null){
                onMenuEvent.execute(menuEvent);
            } else {
                //default behavior
                switch(menuEvent.getType()){
                    case INCREMENT:
                        value = Math.min(value + ((max - min) * 0.01f),max);
                        value = this.valueFromPercentage(value);
                        if(onValueChange != null){
                            onValueChange.execute(new ValueChangeEvent(value));
                        }
                        propagate = false;
                    break;
                    case DECREMENT:
                        value = Math.max(value - ((max - min) * 0.01f),min);
                        value = this.valueFromPercentage(value);
                        if(onValueChange != null){
                            onValueChange.execute(new ValueChangeEvent(value));
                        }
                        propagate = false;
                    break;
                }
            }
        } else if(event instanceof DragEvent){
            DragEvent dragEvent = (DragEvent) event;
            if(dragEvent.getType() == DragEventType.START){
                if(onDragStart != null){
                    propagate = onDragStart.execute(dragEvent);
                } else {
                    //default behavior
                    propagate = true;
                }
            } else if(dragEvent.getType() == DragEventType.DRAG){
                if(onDrag != null){
                    propagate = onDrag.execute(dragEvent);
                } else {
                    //default behavior
                    int percentage = dragEvent.getCurrentX() - getAbsoluteX();
                    int max = getWidth();
                    value = Math.max(Math.min((float)percentage/max,1.0f),0.0f);
                    value = this.valueFromPercentage(value);
                    if(onValueChange != null){
                        onValueChange.execute(new ValueChangeEvent(value));
                    }
                    propagate = false;
                }
            } else if(dragEvent.getType() == DragEventType.RELEASE){
                if(onDragRelease != null){
                    propagate = onDragRelease.execute(dragEvent);
                } else {
                    //default behavior
                    int percentage = dragEvent.getCurrentX() - getAbsoluteX();
                    int max = getWidth();
                    value = Math.max(Math.min((float)percentage/max,1.0f),0.0f);
                    value = this.valueFromPercentage(value);
                    if(onValueChange != null){
                        onValueChange.execute(new ValueChangeEvent(value));
                    }
                    propagate = true;
                }
            }
        } else if(event instanceof ClickEvent){
            ClickEvent clickEvent = (ClickEvent) event;
            if(clickEvent.getButton1()){
                if(this.onClick != null){
                    propagate = this.onClick.execute((ClickEvent)event);
                } else {
                    //default behavior
                    int percentage = clickEvent.getRelativeX() - getAbsoluteX();
                    int max = getWidth();
                    value = Math.max(Math.min((float)percentage/max,1.0f),0.0f);
                    value = this.valueFromPercentage(value);
                    if(onValueChange != null){
                        onValueChange.execute(new ValueChangeEvent(value));
                    }
                    propagate = false;
                }
            }
        }
        return propagate;
    }
    
}
