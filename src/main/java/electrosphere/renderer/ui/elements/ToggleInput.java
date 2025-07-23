package electrosphere.renderer.ui.elements;

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
import electrosphere.renderer.ui.elementtypes.ValueElement;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.ValueChangeEvent;

/**
 * A toggle input
 */
public class ToggleInput extends StandardDrawableElement implements ClickableElement, ValueElement {

    /**
     * Click callback
     */
    ClickEventCallback onClickCallback = null;
    
    /**
     * Value change callback
     */
    ValueChangeEventCallback onValueChangeCallback = null;

    /**
     * The value of the toggle
     */
    boolean value = false;

    /**
     * Material for drawing the circle
     */
    static Material circleMat = null;

    /**
     * The width/height of the circle
     */
    private static final float CIRCLE_WIDTH = 0.4f;

    /**
     * The offset from the center of the bar to place the circle
     */
    private static final float CIRCLE_OFFSET_FROM_CENTER = 0.25f;

    /**
     * The color of the circle
     */
    Vector4f circleColor = new Vector4f(0.8f,0.8f,0.8f,1);

    /**
     * Material for drawing the connecting bar between the circle positions
     */
    static Material barMat = null;

    /**
     * The height of the bar relative to the total drawable height
     */
    private static final float BAR_HEIGHT = 0.6f;

    /**
     * The color of the bar
     */
    Vector4f barColor = new Vector4f(0.3f,0.3f,0.3f,1);

    /**
     * The default width of the toggle in pixels
     */
    private static final int TOGGLE_PIXEL_WIDTH_DEFAULT = 60;

    /**
     * The default height of the toggle in pixels
     */
    private static final int TOGGLE_PIXEL_HEIGHT_DEFAULT = 38;

    /**
     * Creates a toggle input
     * @return The toggle input
     */
    public static ToggleInput createToggleInput(){
        return new ToggleInput();
    }

    /**
     * Constructor
     */
    private ToggleInput(){
        //material work
        if(circleMat == null){
            circleMat = Material.create("Textures/ui/circle.png");
        }
        if(barMat == null){
            barMat = Material.create("Textures/ui/square.png");
        }
        
        this.setWidth(TOGGLE_PIXEL_WIDTH_DEFAULT);
        this.setHeight(TOGGLE_PIXEL_HEIGHT_DEFAULT);
    }
    

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ){
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());


        float ndcWidth =  (float)getWidth()/framebuffer.getWidth();
        float ndcHeight = (float)getHeight()/framebuffer.getHeight();
        float ndcX =      (float)this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX)/framebuffer.getWidth();
        float ndcY =      (float)this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY)/framebuffer.getHeight();
        
        Vector3f boxPosition = new Vector3f(ndcX,ndcY,0);
        Vector3f boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);

        //getInternalX() and getInternalY() are the top left corner of the drawable space
        //getInternalWidth() and getInternalHeight() are the width and height of the drawable space

        //the actual offset from the center (with appropriate sign based on value)
        float circleOffsetActual = 0;
        if(value){
            circleColor.set(0.9f, 0.9f, 0.9f, 1.0f);
            barColor.set(0.5f, 0.9f, 0.5f, 1.0f);
            circleOffsetActual = CIRCLE_OFFSET_FROM_CENTER;
        } else {
            circleColor.set(0.9f, 0.9f, 0.9f, 1.0f);
            barColor.set(0.9f, 0.5f, 0.5f, 1.0f);
            circleOffsetActual = -CIRCLE_OFFSET_FROM_CENTER;
        }
        //ratio to adjust the circlewidth by to always show a circle and not a deformed oval
        float circleRatio = getWidth() / (float)getHeight();

        Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
        if(planeModel != null){
            //draw bar
            ndcX = (float)(this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX) + (getWidth() * ((1.0f - CIRCLE_WIDTH)/2.0f)))/framebuffer.getWidth();
            ndcY = (float)(this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY) + (getHeight() * ((1.0f - BAR_HEIGHT) / 2.0f)))/framebuffer.getHeight();
            ndcWidth = (float)((getWidth()) - (getWidth() * ((1.0f - CIRCLE_WIDTH))))/framebuffer.getWidth();
            ndcHeight = (float)(getHeight() * BAR_HEIGHT)/framebuffer.getHeight();
            boxPosition = new Vector3f(ndcX,ndcY,0);
            boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
            planeModel.getMeshes().get(0).setMaterial(barMat);
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", new Vector3f(0,0,0));
            planeModel.pushUniformToMesh("plane", "tDimension", new Vector3f(1,1,0));
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", barColor);
            planeModel.drawUI();

            //draw circle
            ndcX = (float)(this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX) + (getWidth() * ((1.0f - CIRCLE_WIDTH) / 2.0f)) + (getWidth() * circleOffsetActual))/framebuffer.getWidth();
            ndcY = (float)(this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY) + (getHeight() * ((1.0f - (CIRCLE_WIDTH * circleRatio)) / 2.0f)))/framebuffer.getHeight();
            ndcWidth = (float)((getWidth() * CIRCLE_WIDTH))/framebuffer.getWidth();
            ndcHeight = (float)(getHeight() * (CIRCLE_WIDTH * circleRatio))/framebuffer.getHeight();
            boxPosition = new Vector3f(ndcX,ndcY,0);
            boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
            planeModel.getMeshes().get(0).setMaterial(circleMat);
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", new Vector3f(0,0,0));
            planeModel.pushUniformToMesh("plane", "tDimension", new Vector3f(1,1,0));
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", circleColor);
            planeModel.drawUI();
        } else {
            LoggerInterface.loggerRenderer.ERROR("Window unable to find plane model!!", new Exception());
        }
    }

    /**
     * Handles an event
     */
    public boolean handleEvent(Event event){
        boolean propagate = true;
        if(event instanceof ClickEvent){
            ClickEvent clickEvent = (ClickEvent)event;
            if(onClickCallback != null){
                onClickCallback.execute(clickEvent);
            } else {
                Globals.elementService.focusElement(this);
                this.value = !this.value;
                Globals.elementService.fireEventNoPosition(new ValueChangeEvent(this.value), this);
                propagate = false;
            }
        } else if(event instanceof ValueChangeEvent){
            ValueChangeEvent valueEvent = (ValueChangeEvent)event;
            if(this.onValueChangeCallback != null){
                this.onValueChangeCallback.execute(valueEvent);
            }
        }
        return propagate;
    }


    @Override
    public void setOnValueChangeCallback(ValueChangeEventCallback callback) {
        this.onValueChangeCallback = callback;
    }

    @Override
    public void setOnClick(ClickEventCallback callback) {
        this.onClickCallback = callback;
    }

    /**
     * Sets the value of the toggle
     * @param value The value to set the toggle to
     */
    public void setValue(boolean value){
        this.value = value;
    }
    
}
