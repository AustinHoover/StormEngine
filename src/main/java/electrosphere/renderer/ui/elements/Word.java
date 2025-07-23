package electrosphere.renderer.ui.elements;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.font.Font;

/**
 * A collection of characters into a single element for wrapping purposes
 */
public class Word extends StandardDrawableContainerElement {
    
    /**
     * The default percentage width of the word element
     */
    public static final float DEFAULT_MIN_WIDTH_PERCENT = 100.0f;

    /**
     * The default percentage max height of the word element
     */
    public static final float DEFAULT_MAX_HEIGHT_PERCENT = 100.0f;
    
    /**
     * The text contained in this box
     */
    String text;

    /**
     * The font for the word
     */
    Font font;

    /**
     * Creates a word element
     * @param content The characters in the word
     * @return The word element
     */
    public static Word createWord(String content){
        return new Word(content);
    }
    
    /**
     * Constructor
     * @param text The characters in the word
     */
    private Word(String text) {
        super();
        this.text = text;
        this.font = Globals.fontManager.getFont("default");
        setFlexDirection(YogaFlexDirection.Row);
        setAlignItems(YogaAlignment.Start);
        setJustifyContent(YogaJustification.Start);
        setMinHeight((int)(font.getFontHeight() * Label.DEFAULT_FONT_SIZE));
        setMaxHeightPercent(DEFAULT_MAX_HEIGHT_PERCENT);
        setMaxWidthPercent(DEFAULT_MIN_WIDTH_PERCENT);
        setWrap(YogaWrap.NO_WRAP);
        setOverflow(YogaOverflow.Hidden);
        generateLetters();
    }

    /**
     * Gets the text contained in the word
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text contained in the word
     * @param text The text
     */
    public void setText(String text) {
        this.text = text;
        generateLetters();
        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,this);
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
        for(int i = 0; i < text.length(); i++){
            char toDraw = text.charAt(i);

            //error checking input data
            if(toDraw == ' ' || toDraw == '\n'){
                LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Tried to create a word with a '" + toDraw + "'! This is unintended!"));
            }

            BitmapCharacter newLetter = new BitmapCharacter(this.font, toDraw);
            this.addChild(newLetter);
        }
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
