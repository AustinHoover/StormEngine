package electrosphere.renderer.ui.elements;

import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.ClickableElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.FocusableElement;
import electrosphere.renderer.ui.elementtypes.HoverableElement;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.FocusEvent;
import electrosphere.renderer.ui.events.HoverEvent;
import electrosphere.renderer.ui.events.MouseEvent;
import electrosphere.renderer.ui.frame.UIFrameUtils;

/**
 * A button element
 */
public class Button extends StandardContainerElement implements DrawableElement, FocusableElement, ClickableElement, HoverableElement {

    /**
     * Color for when the button is unfocused
     */
    static Vector4f COLOR_UNFOCUSED = new Vector4f(0.97f,0.97f,0.98f,1.0f);

    /**
     * Color for when the button is focused
     */
    static Vector4f COLOR_FOCUSED = new Vector4f(0.42f,0.46f,0.49f,1.0f);

    /**
     * Default button color
     */
    static Vector4f COLOR_DEFAULT = new Vector4f(COLOR_UNFOCUSED);

    /**
     * Default color for focused frame
     */
    static Vector4f COLOR_FRAME_FOCUSED_DEFAULT = new Vector4f(0.914f, 0.925f, 0.937f, 0.7f);

    /**
     * Default color for unfocused frame
     */
    static Vector4f COLOR_FRAME_UNFOCUSED_DEFAULT = new Vector4f(0.089f, 0.105f, 0.121f, 0.7f);

    /**
     * Default padding applied to buttons
     */
    static final int DEFAULT_PADDING = 10;

    /**
     * The color of the backing element
     */
    Vector4f color = new Vector4f(COLOR_DEFAULT);

    /**
     * The color of the frame
     */
    Vector4f frameColor = new Vector4f(COLOR_FRAME_FOCUSED_DEFAULT);

    /**
     * The color of the background of the frame
     */
    Vector4f frameBackgroundColor = new Vector4f(COLOR_FRAME_UNFOCUSED_DEFAULT);


    boolean visible = false;
    boolean focused = false;

    /**
     * Controls whether the button draws its decorative frame or not
     */
    boolean drawFrame = true;

    FocusEventCallback onFocusCallback;
    FocusEventCallback onLoseFocusCallback;
    ClickEventCallback clickCallback;
    HoverEventCallback hoverEventCallback;

    static final Vector3f windowDrawDebugColor = new Vector3f(1.0f,1.0f,1.0f);

    /**
     * Audio path played on clicking the button
     */
    String audioPathOnClick = AssetDataStrings.UI_TONE_CONFIRM_PRIMARY;

    
    public Button(){
        super();
    }

    /**
     * Creates a button with 
     * @param text
     * @return
     */
    public static Button createButton(String text, ClickableElement.ClickEventCallback callback){
        Button rVal = new Button();
        rVal.setPaddingTop(DEFAULT_PADDING);
        rVal.setPaddingRight(DEFAULT_PADDING);
        rVal.setPaddingLeft(DEFAULT_PADDING);
        rVal.setPaddingBottom(DEFAULT_PADDING);
        Label rValLabel = Label.createLabel(text);
        rValLabel.setText(text);
        rValLabel.setColor(rVal.color);
        rVal.addChild(rValLabel);
        rVal.setOnClick(callback);
        rVal.setAlignSelf(YogaAlignment.Start);
        return rVal;
    }

    /**
     * Creates a button that fires a callback when clicked
     * @param text The text for the button label
     * @param callback The callback
     * @return The button
     */
    public static Button createButton(String text, Runnable callback){
        Button rVal = new Button();
        rVal.setPaddingTop(DEFAULT_PADDING);
        rVal.setPaddingRight(DEFAULT_PADDING);
        rVal.setPaddingLeft(DEFAULT_PADDING);
        rVal.setPaddingBottom(DEFAULT_PADDING);
        Label rValLabel = Label.createLabel(text);
        rValLabel.setText(text);
        rValLabel.setColor(rVal.color);
        rVal.addChild(rValLabel);
        rVal.setOnClick(new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            callback.run();
            if(Globals.audioEngine.virtualAudioSourceManager != null && rVal.audioPathOnClick != null){
                Globals.audioEngine.virtualAudioSourceManager.createUI(rVal.audioPathOnClick);
            }
            return false;
        }});
        rVal.setAlignSelf(YogaAlignment.Start);
        return rVal;
    }

    /**
     * Creates a button that fires a callback when clicked
     * @param text The text for the button label
     * @param callback The callback
     * @return The button
     */
    public static Button createButtonCentered(String text, Runnable callback){
        Button rVal = new Button();
        rVal.setPaddingTop(DEFAULT_PADDING);
        rVal.setPaddingRight(DEFAULT_PADDING);
        rVal.setPaddingLeft(DEFAULT_PADDING);
        rVal.setPaddingBottom(DEFAULT_PADDING);
        Label rValLabel = Label.createLabel(text);
        rValLabel.setText(text);
        rValLabel.setColor(rVal.color);
        rVal.addChild(rValLabel);
        rVal.setOnClick(new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            callback.run();
            if(Globals.audioEngine.virtualAudioSourceManager != null && rVal.audioPathOnClick != null){
                Globals.audioEngine.virtualAudioSourceManager.createUI(rVal.audioPathOnClick);
            }
            return false;
        }});
        rVal.setAlignSelf(YogaAlignment.Center);
        return rVal;
    }

    /**
     * Creates a button that fires a callback when clicked
     * @param text The text for the button label
     * @param fontSize The size of the font for the label
     * @param callback The callback
     * @return The button
     */
    public static Button createButtonCentered(String text, float fontSize, Runnable callback){
        Button rVal = new Button();
        rVal.setPaddingTop(DEFAULT_PADDING);
        rVal.setPaddingRight(DEFAULT_PADDING);
        rVal.setPaddingLeft(DEFAULT_PADDING);
        rVal.setPaddingBottom(DEFAULT_PADDING);
        Label rValLabel = Label.createLabel(text, fontSize);
        rValLabel.setText(text);
        rValLabel.setColor(rVal.color);
        rVal.addChild(rValLabel);
        rVal.setOnClick(new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            callback.run();
            if(Globals.audioEngine.virtualAudioSourceManager != null && rVal.audioPathOnClick != null){
                Globals.audioEngine.virtualAudioSourceManager.createUI(rVal.audioPathOnClick);
            }
            return false;
        }});
        rVal.setAlignSelf(YogaAlignment.Center);
        return rVal;
    }

    /**
     * Creates a button with no label inside
     * @param callback The callback to fire when the button is clicked
     * @return The button
     */
    public static Button createEmptyButton(Runnable callback){
        Button rVal = new Button();
        rVal.setPaddingTop(DEFAULT_PADDING);
        rVal.setPaddingRight(DEFAULT_PADDING);
        rVal.setPaddingLeft(DEFAULT_PADDING);
        rVal.setPaddingBottom(DEFAULT_PADDING);
        rVal.setOnClick(new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            callback.run();
            if(Globals.audioEngine != null && rVal.audioPathOnClick != null){
                Globals.audioEngine.virtualAudioSourceManager.createUI(rVal.audioPathOnClick);
            }
            return false;
        }});
        rVal.setAlignSelf(YogaAlignment.Start);
        return rVal;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean draw) {
        this.visible = draw;
    }
    
    @Override
    public boolean isFocused() {
        return focused;
    }

    private void onFocus(FocusEvent event) {
        if(onFocusCallback != null){
            onFocusCallback.execute(event);
        } else {
            for(Element child : childList){
                if(child instanceof Label){
                    Label childLabel = (Label) child;
                    childLabel.setColor(new Vector4f(COLOR_UNFOCUSED));
                }
            }
        }
    }

    void onLoseFocus(FocusEvent event) {
        if(onLoseFocusCallback != null){
            onLoseFocusCallback.execute(event);
        } else {
            for(Element child : childList){
                if(child instanceof Label){
                    Label childLabel = (Label) child;
                    childLabel.setColor(new Vector4f(COLOR_UNFOCUSED));
                }
            }
        }
    }

    /**
     * Default hover event handling
     * @param event the hover event
     */
    private void onHoverEvent(HoverEvent event){
        if(event.isHovered()){
            for(Element child : childList){
                if(child instanceof Label){
                    Label childLabel = (Label) child;
                    childLabel.setColor(new Vector4f(COLOR_FOCUSED));
                }
            }
        } else {
            for(Element child : childList){
                if(child instanceof Label){
                    Label childLabel = (Label) child;
                    childLabel.setColor(new Vector4f(COLOR_UNFOCUSED));
                }
            }
        }
    }

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ) {

        //this call binds the screen as the "texture" we're rendering to
        //have to call before actually rendering
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

        //render background of window
        if(this.drawFrame){
            if(this.isFocused()){
                UIFrameUtils.drawFrame(
                    openGLState,
                    AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_3, frameColor, 48, 12,
                    this.getAbsoluteX(), this.getAbsoluteY(), this.getWidth(), this.getHeight(), 
                    framebuffer, framebufferPosX, framebufferPosY
                );
            } else {
                UIFrameUtils.drawFrame(
                    openGLState,
                    AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_3, frameBackgroundColor, 48, 12,
                    this.getAbsoluteX(), this.getAbsoluteY(), this.getWidth(), this.getHeight(), 
                    framebuffer, framebufferPosX, framebufferPosY
                );
            }
        }

        //
        //Draw children elements
        for(Element child : childList){
            if(child instanceof DrawableElement){
                DrawableElement drawableChild = (DrawableElement) child;
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

    @Override
    public void setOnFocus(FocusEventCallback callback) {
        onFocusCallback = callback;
    }

    /**
     * Custom method to register a consumer as a callback for focus
     * @param callback The callback
     */
    public void setOnFocus(Consumer<FocusEvent> callback){
        onFocusCallback = new FocusEventCallback() {
            @Override
            public boolean execute(FocusEvent event) {
                callback.accept(event);
                return false;
            }
        };
    }

    @Override
    public void setOnLoseFocus(FocusEventCallback callback) {
        onLoseFocusCallback = callback;
    }

    @Override
    public void setOnClick(ClickEventCallback callback) {
        clickCallback = callback;
    }

    public boolean handleEvent(Event event){
        if(event instanceof MouseEvent){
            return false;
        }
        if(event instanceof FocusEvent){
            FocusEvent focusEvent = (FocusEvent) event;
            if(focusEvent.isFocused()){
                this.focused = true;
                this.onFocus(focusEvent);
            } else {
                this.focused = false;
                this.onLoseFocus(focusEvent);
            }
            return false;
        }
        if(event instanceof ClickEvent){
            if(clickCallback != null){
                clickCallback.execute((ClickEvent)event);
            }
        }
        if(event instanceof HoverEvent){
            if(hoverEventCallback != null){
                hoverEventCallback.execute((HoverEvent)event);
            } else {
                //default hover handling
                this.onHoverEvent((HoverEvent)event);
            }
        }
        return true;
    }

    @Override
    public void setOnHoverCallback(HoverEventCallback callback) {
        this.hoverEventCallback = callback;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /**
     * Sets the audio path to play on click
     * @param audioPath The audio path
     */
    public Button setOnClickAudio(String audioPath){
        this.audioPathOnClick = audioPath;
        return this;
    }

    /**
     * Sets the background color of this element
     * @param color The color
     */
    public void setColor(Vector4f color){
        this.color.set(color);
    }

    /**
     * Sets whether the button should draw its frame or not
     * @param drawFrame true if it should draw its frame, false otherwise
     */
    public void setDrawFrame(boolean drawFrame){
        this.drawFrame = drawFrame;
    }

    /**
     * Gets whether the button should draw its frame or not
     * @return true if it should draw its frame, false otherwise
     */
    public boolean getDrawFrame(){
        return this.drawFrame;
    }
    
}
