package electrosphere.renderer.ui.elements;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.ClickableElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.FocusableElement;
import electrosphere.renderer.ui.elementtypes.KeyEventElement;
import electrosphere.renderer.ui.elementtypes.ValueElement;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.FocusEvent;
import electrosphere.renderer.ui.events.KeyboardEvent;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.renderer.ui.font.Font;
import electrosphere.renderer.ui.frame.UIFrameUtils;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.regex.Pattern;

/**
 * A Text input
 */
public class TextInput extends StandardContainerElement implements DrawableElement, FocusableElement, KeyEventElement, ClickableElement, ValueElement {

    /**
     * Default padding applied to text inputs
     */
    static final int DEFAULT_PADDING = Button.DEFAULT_PADDING;

    /**
     * The color of the background element of the input
     */
    Vector4f backgroundColor = new Vector4f(0.2f,0.2f,0.2f,1.0f);

    /**
     * Stores visibility status
     */
    private boolean visible = false;

    /**
     * Stores focused status
     */
    private boolean focused = false;

    /**
     * Optional callback for gaining focus
     */
    private FocusEventCallback onFocusCallback;

    /**
     * Optional callback for losing focus
     */
    private FocusEventCallback onLoseFocusCallback;

    /**
     * Optional callback for key presses
     */
    private KeyboardEventCallback onKeyPressCallback;

    /**
     * Optional callback for mouse clicks
     */
    private ClickEventCallback onClickCallback;

    /**
     * Optional callback for value changes
     */
    private ValueChangeEventCallback onValueChangeCallback;

    /**
     * The color of the text input
     */
    private Vector4f color;

    /**
     * The content of the text input
     */
    private String text = "";
    
    /**
     * The size of the font for the text input
     */
    private float fontSize = Label.DEFAULT_FONT_SIZE;
    
    /**
     * The font to use with the text input
     */
    private Font font;

    /**
     * Audio path played when typing into the input
     */
    private String audioPathOnType = AssetDataStrings.UI_TONE_CURSOR_SECONDARY;

    /**
     * Audio path played when typing into the input
     */
    private String audioPathOnTypeError = AssetDataStrings.UI_TONE_ERROR_SECONDARY;

    /**
     * Creates a text input element using the default font size
     * @return The text input
     */
    public static TextInput createTextInput(){
        TextInput rVal = new TextInput(Label.DEFAULT_FONT_SIZE);
        return rVal;
    }
    
    /**
     * Private constructor
     * @param fontSize
     */
    private TextInput(float fontSize){
        super();
        this.font = Globals.fontManager.getFont("default");
        this.fontSize = fontSize;
        this.color = new Vector4f(1,1,1,1);
        this.setHeight((int)(font.getFontHeight() * fontSize) + DEFAULT_PADDING * 2);
        this.setFlexDirection(YogaFlexDirection.Row);
        this.setMinWidth(1);
        this.setPaddingBottom(DEFAULT_PADDING);
        this.setPaddingLeft(DEFAULT_PADDING);
        this.setPaddingRight(DEFAULT_PADDING);
        this.setPaddingTop(DEFAULT_PADDING);
    }
    
    /**
     * Generate letter elements
     */
    private void generateLetters(){
        for(Element el : getChildren()){
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, el);
        }
        this.clearChildren();
        for(int i = 0; i < text.length(); i++){
            char toDraw = text.charAt(i);
            Vector3f bitMapDimension = this.font.getDimensionOfCharacterDiscrete(toDraw);
            BitmapCharacter newLetter = new BitmapCharacter(this.font,(int)(bitMapDimension.x * fontSize), this.getHeight() - DEFAULT_PADDING * 2, fontSize, toDraw);
            newLetter.setColor(color);
            this.addChild(newLetter);
        }
    }
    
    /**
     * Sets the content of the text input
     * @param text The content
     */
    public void setText(String text){
        this.text = text;
        this.generateLetters();
    }

    /**
     * Sets the color of the text input
     * @param color The color
     */
    public void setColor(Vector4f color){
        this.color.set(color);
        for(Element character : childList){
            ((BitmapCharacter)character).setColor(color);
        }
    }
    
    /**
     * Gets the current contents of the text input
     * @return The contents
     */
    public String getText(){
        return text;
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
        if(this.isFocused()){
            UIFrameUtils.drawFrame(
                openGLState,
                AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_3, backgroundColor, 48, 12,
                this.getAbsoluteX(), this.getAbsoluteY(), this.getWidth(), this.getHeight(),
                framebuffer, framebufferPosX, framebufferPosY
            );
        } else {
            UIFrameUtils.drawFrame(
                openGLState,
                AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_3, backgroundColor, 48, 12,
                this.getAbsoluteX(), this.getAbsoluteY(), this.getWidth(), this.getHeight(),
                framebuffer, framebufferPosX, framebufferPosY
            );
        }


        //
        //Draw children elements
        for(Element child : childList){
            ((DrawableElement)child).draw(renderPipelineState, openGLState, framebuffer, framebufferPosX, framebufferPosY);
        }
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
    public boolean handleEvent(Event event){
        boolean propagate = true;
        if(event instanceof FocusEvent){
            FocusEvent focusEvent = (FocusEvent)event;
            if(focusEvent.isFocused()){
                if(this.onFocusCallback != null){
                    this.onFocusCallback.execute(focusEvent);
                } else {
                    this.focused = true;
                    this.setColor(new Vector4f(1,0,0,1));
                    propagate = false;
                }
            } else {
                if(this.onLoseFocusCallback != null){
                    this.onLoseFocusCallback.execute(focusEvent);
                } else {
                    this.focused = false;
                    this.setColor(new Vector4f(1,1,1,1));
                    propagate = false;
                }
            }
        } else if(event instanceof KeyboardEvent){
            KeyboardEvent keyEvent = (KeyboardEvent)event;
            if(onKeyPressCallback != null){
                onKeyPressCallback.execute(keyEvent);
            } else {
                propagate = defaultKeyHandling(keyEvent);
            }
        } else if(event instanceof ClickEvent){
            ClickEvent clickEvent = (ClickEvent)event;
            if(onClickCallback != null){
                onClickCallback.execute(clickEvent);
            } else {
                Globals.elementService.focusElement(this);
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

    /**
     * The default handling for a keyboard event
     * @param keyEvent the event
     * @return whether to propagate or not
     */
    public boolean defaultKeyHandling(KeyboardEvent keyEvent){
        if(keyEvent.getKey().matches(Pattern.quote("bs"))){
            if(this.text.length() > 0){
                this.setText(this.text.substring(0, this.text.length() - 1));
                if(Globals.audioEngine != null && this.audioPathOnType != null){
                    Globals.audioEngine.virtualAudioSourceManager.createUI(this.audioPathOnType);
                }
            } else {
                if(Globals.audioEngine != null && this.audioPathOnTypeError != null){
                    Globals.audioEngine.virtualAudioSourceManager.createUI(this.audioPathOnTypeError);
                }
            }
        } else {
            String newVal = this.text + keyEvent.getKey();
            this.setText(newVal);
            if(Globals.audioEngine != null && this.audioPathOnType != null){
                Globals.audioEngine.virtualAudioSourceManager.createUI(this.audioPathOnType);
            }
        }
        //apply yoga at the window level
        Element parent = this.getParent();
        while(parent.getParent() != null){
            parent = parent.getParent();
        }
        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, parent);
        //fire value change event
        Globals.elementService.fireEventNoPosition(new ValueChangeEvent(text), this);
        return false;
    }

    @Override
    public boolean isFocused() {
        return focused;
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
    public void setOnPress(KeyboardEventCallback callback) {
        onKeyPressCallback = callback;
    }

    @Override
    public void setOnClick(ClickEventCallback callback) {
        onClickCallback = callback;
    }

    @Override
    public void setOnValueChangeCallback(ValueChangeEventCallback callback) {
        this.onValueChangeCallback = callback;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
    
}
