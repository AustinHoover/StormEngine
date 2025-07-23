package electrosphere.renderer.ui.elements;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.font.Font;

/**
 * A wrapping text box
 */
public class TextBox extends StandardDrawableContainerElement {

    /**
     * The default percentage width of the textbox element
     */
    public static final float DEFAULT_MIN_WIDTH_PERCENT = 100.0f;

    /**
     * The default percentage max height of the textbox element
     */
    public static final float DEFAULT_MAX_HEIGHT_PERCENT = 100.0f;
    
    /**
     * The text contained in this box
     */
    String text;

    /**
     * The editable status of this text box
     */
    boolean editable;

    /**
     * The font for the textbox
     */
    Font font;

    /**
     * Creates a textbox element
     * @param content The content to prepopulate it with
     * @param editable The editable status
     * @return The textbox element
     */
    public static TextBox createTextBox(String content, boolean editable){
        return new TextBox(content, editable);
    }
    
    /**
     * Constructor
     * @param text The content to prepopulate it with
     * @param editable The editable status
     */
    private TextBox(String text, boolean editable) {
        super();
        this.text = text;
        this.font = Globals.fontManager.getFont("default");
        setFlexDirection(YogaFlexDirection.Row);
        setAlignItems(YogaAlignment.Start);
        setJustifyContent(YogaJustification.Start);
        setMinHeight((int)(font.getFontHeight() * Label.DEFAULT_FONT_SIZE));
        setMaxHeightPercent(DEFAULT_MAX_HEIGHT_PERCENT);
        setMaxWidthPercent(DEFAULT_MIN_WIDTH_PERCENT);
        setWrap(YogaWrap.WRAP);
        setOverflow(YogaOverflow.Hidden);
        generateLetters();
    }

    /**
     * Gets the text contained in the textbox
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text contained in the textbox
     * @param text The text
     */
    public void setText(String text) {
        this.text = text;
        generateLetters();
        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,this);
    }
    
    /**
     * Gets the editable status of the textbox
     * @return True if editable, false otherwise
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the editable status of the textbox
     * @param editable true to make it editable, false otherwise
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Generates the individual character elements
     */
    void generateLetters(){
        //free children
        for(Element child : childList){
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, child);
        }
        this.clearChildren();
        String[] words = text.split(" ");
        for(int i = 0; i < words.length - 1; i++){
            Word word = Word.createWord(words[i]);
            this.addChild(word);
            BitmapCharacter space = new BitmapCharacter(this.font, ' ');
            this.addChild(space);
        }
        Word word = Word.createWord(words[words.length - 1]);
        this.addChild(word);
    }
    
    
    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ){

        //draw characters
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
    
    /**
     * Event handling
     * @param event The event to handle
     */
    public boolean handleEvent(Event event){
        return true;
    }

}
