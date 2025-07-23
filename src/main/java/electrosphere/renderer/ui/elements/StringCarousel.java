package electrosphere.renderer.ui.elements;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.util.yoga.Yoga;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.FocusableElement;
import electrosphere.renderer.ui.elementtypes.MenuEventElement;
import electrosphere.renderer.ui.elementtypes.ValueElement;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.FocusEvent;
import electrosphere.renderer.ui.events.MenuEvent;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.renderer.ui.events.MenuEvent.MenuEventType;
import electrosphere.renderer.ui.font.Font;

public class StringCarousel extends StandardContainerElement implements DrawableElement, MenuEventElement, FocusableElement, ValueElement {
    
    public boolean visible = false;

    MenuEventCallback onMenuEventCallback;
    ValueChangeEventCallback onValueChange;

    boolean focused = false;
    FocusEventCallback onFocusCallback;
    FocusEventCallback onLoseFocusCallback;

    List<String> options = new LinkedList<String>();
    int currentOption = -1;
    String textCurrent = "";
    int textPixelWidth = 0;
    
    float fontSize = 1.0f;
    
    Font font;

    /**
     * The default width of an actor panel
     */
    public static final int DEFAULT_WIDTH = 200;

    /**
     * The default height of an actor panel
     */
    public static final int DEFAULT_HEIGHT = 32;
    
    public StringCarousel(int x, int y, float fontSize){
        super();
        this.font = Globals.fontManager.getFont("default");
        this.fontSize = fontSize;
        Yoga.YGNodeStyleSetMinHeight(this.yogaNode, font.getFontHeight() * fontSize);
        Yoga.YGNodeStyleSetMinWidth(this.yogaNode, 1);
    }

    /**
     * Constructor
     */
    private StringCarousel(){
        super();
        this.font = Globals.fontManager.getFont("default");
        Yoga.YGNodeStyleSetMinHeight(this.yogaNode, font.getFontHeight() * fontSize);
        Yoga.YGNodeStyleSetMinWidth(this.yogaNode, 1);
    }

    /**
     * Creates a string carousel element
     * @param options The options for the carousel
     * @return The carousel
     */
    public static StringCarousel create(List<String> options, Consumer<ValueChangeEvent> callback){
        StringCarousel rVal = new StringCarousel();
        rVal.setOnValueChangeCallback(new ValueChangeEventCallback() {public void execute(ValueChangeEvent event) {
            callback.accept(event);
        }});
        rVal.options = options;
        rVal.currentOption = 0;
        rVal.setText(options.get(0));
        rVal.setFlexDirection(YogaFlexDirection.Row);
        return rVal;
    }

    public void addOption(String option){
        options.add(option);
        if(currentOption == -1){
            currentOption = 0;
            setText(option);
            if(onValueChange != null){
                onValueChange.execute(new ValueChangeEvent(option));
            }
        }
    }

    public List<String> getOptions(){
        return options;
    }

    public void removeOption(String option){
        options.remove(option);
        if(currentOption > options.size() - 1){
            currentOption = options.size() - 1;
        }
    }

    public void setOption(int index){
        this.currentOption = index;
        this.setText(this.options.get(index));
    }

    public int getCurrentOption(){
        return this.currentOption;
    }
    
    void generateLetters(){
        for(Element el : getChildren()){
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, el);
        }
        this.clearChildren();
        int accumulatingWidth = 0;
        for(int i = 0; i < textCurrent.length(); i++){
            char toDraw = textCurrent.charAt(i);
            Vector3f bitMapDimension = this.font.getDimensionOfCharacterDiscrete(toDraw);
            BitmapCharacter newLetter = new BitmapCharacter(this.font,(int)(bitMapDimension.x * fontSize), this.getHeight(), fontSize, toDraw);
            accumulatingWidth += bitMapDimension.x * fontSize;
            addChild(newLetter);
        }
        Yoga.YGNodeStyleSetWidth(yogaNode, accumulatingWidth);
    }
    
    public void setText(String text){
        this.textCurrent = text;
        textPixelWidth = 0;
        for(int i = 0; i < text.length(); i++){
            Vector3f bitMapDimension = this.font.getDimensionOfCharacterDiscrete(text.charAt(i));
            textPixelWidth = textPixelWidth + (int)bitMapDimension.x;
        }
        generateLetters();
        if(focused){
            setColor(new Vector4f(1,0,0,1));
        }
        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, this);
    }

    public void setColor(Vector4f color){
        for(Element character : childList){
            ((BitmapCharacter)character).setColor(color);
        }
    }
    
    public String getText(){
        return textCurrent;
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
            ((DrawableElement)child).draw(renderPipelineState, openGLState, framebuffer, framebufferPosX, framebufferPosY);
        }
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean draw) {
        this.visible = draw;
    }

    /**
     * The default menu event handler
     * @param event
     * @return
     */
    private boolean defaultMenuEventHandler(MenuEvent event){
        if(event.getType() == MenuEventType.INCREMENT){
            if(options.size() > 0){
                currentOption++;
                if(currentOption > options.size() - 1){
                    currentOption = 0;
                }
                String newOption = options.get(currentOption);
                this.setText(newOption);
                if(onValueChange != null){
                    onValueChange.execute(new ValueChangeEvent(newOption));
                }
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY_ROOT,this);
            }
        } else if(event.getType() == MenuEventType.DECREMENT){
            if(options.size() > 0){
                currentOption--;
                if(currentOption < 0){
                    currentOption = options.size() - 1;
                }
                String newOption = options.get(currentOption);
                this.setText(newOption);
                if(onValueChange != null){
                    onValueChange.execute(new ValueChangeEvent(newOption));
                }
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY_ROOT,this);
            }
        }
        return false;
    }
    
    /**
     * Handles an event
     * @param event The event
     */
    public boolean handleEvent(Event event){
        boolean propagate = true;
        if(event instanceof MenuEvent){
            MenuEvent menuEvent = (MenuEvent)event;
            if(onMenuEventCallback != null){
                propagate = onMenuEventCallback.execute(menuEvent);
            } else {
                //default behavior
                propagate = this.defaultMenuEventHandler(menuEvent);
            }
        } else if(event instanceof FocusEvent){
            FocusEvent focusEvent = (FocusEvent) event;
            if(focusEvent.isFocused()){
                this.focused = true;
                if(onFocusCallback != null){
                    propagate = onFocusCallback.execute(focusEvent);
                } else {
                    //default behavior
                    propagate = false;
                    setColor(new Vector4f(1,0,0,1));
                }
            } else {
                this.focused = false;
                if(onLoseFocusCallback != null){
                    propagate = onLoseFocusCallback.execute(focusEvent);
                } else {
                    //default behavior
                    propagate = false;
                    setColor(new Vector4f(1,1,1,1));
                }
            }
        }
        return propagate;
    }

    @Override
    public void setOnMenuEventCallback(MenuEventCallback callback) {
        onMenuEventCallback = callback;
    }

    @Override
    public void setOnValueChangeCallback(ValueChangeEventCallback callback) {
        onValueChange = callback;
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
    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

}
