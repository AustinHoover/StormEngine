package electrosphere.renderer.ui.elements;

import org.joml.Vector4f;
import org.lwjgl.util.yoga.Yoga;

import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.frame.UIFrameUtils;

/**
 * A panel that should contain other elements
 */
public class Panel extends StandardContainerElement implements DrawableElement {
    

    static final Vector4f windowDrawDebugColor = new Vector4f(1.0f,1.0f,1.0f,1.0f);

    /**
     * Default padding applied to buttons
     */
    static final int DEFAULT_PADDING = 10;

    /**
     * The default color of the frame
     */
    static float COLOR_FRAME_FOCUSED_DEFAULT = 0.1f;

    /**
     * The color of the frame
     */
    Vector4f frameColor = new Vector4f(COLOR_FRAME_FOCUSED_DEFAULT);

    /**
     * Creates a label element
     * @return the label element
     */
    public static Panel createPanel(){
        Panel rVal = new Panel();
        rVal.setPaddingTop(DEFAULT_PADDING);
        rVal.setPaddingRight(DEFAULT_PADDING);
        rVal.setPaddingLeft(DEFAULT_PADDING);
        rVal.setPaddingBottom(DEFAULT_PADDING);
        return rVal;
    }

    /**
     * Creates a label element
     * @param children The child elements
     * @return the label element
     */
    public static Panel createPanel(Element ... children){
        Panel rVal = new Panel();
        rVal.setPaddingTop(DEFAULT_PADDING);
        rVal.setPaddingRight(DEFAULT_PADDING);
        rVal.setPaddingLeft(DEFAULT_PADDING);
        rVal.setPaddingBottom(DEFAULT_PADDING);
        for(Element child : children){
            rVal.addChild(child);
        }
        return rVal;
    }

    /**
     * Constructor
     */
    private Panel(){
        super();
        Yoga.YGNodeStyleSetFlexDirection(this.yogaNode, Yoga.YGFlexDirectionRow);
    }

    /**
     * Sets the color of the panel
     * @param color The color
     */
    public void setColor(Vector4f color){
        this.frameColor = color;
    }

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ) {

        UIFrameUtils.drawFrame(
            openGLState,
            AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_3, frameColor, 48, 12,
            this.getAbsoluteX(), this.getAbsoluteY(), this.getWidth(), this.getHeight(), 
            framebuffer, framebufferPosX, framebufferPosY
        );

        for(Element child : childList){
            ((DrawableElement)child).draw(
                renderPipelineState,
                openGLState,
                framebuffer,
                framebufferPosX,
                framebufferPosY
            );
        }
    }
    
    @Override
    public boolean handleEvent(Event event){
        return true;
    }

}
