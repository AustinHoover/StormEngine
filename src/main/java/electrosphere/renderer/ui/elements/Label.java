package electrosphere.renderer.ui.elements;

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
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.font.Font;

/**
 * A label
 */
public class Label extends StandardContainerElement implements DrawableElement {
    
    /**
     * The default font size
     */
    public static final float DEFAULT_FONT_SIZE = 1.0f;

    /**
     * The text of the label
     */
    private String text = "";
    
    /**
     * The font size of the label
     */
    private float fontSize = DEFAULT_FONT_SIZE;

    /**
     * The font to use with the label
     */
    private Font font;

    /**
     * Creates a label element
     * @param text the text for the label
     * @return the label element
     */
    public static Label createLabel(String text){
        Label rVal = new Label(DEFAULT_FONT_SIZE);
        rVal.setText(text);
        return rVal;
    }

    /**
     * Creates a label element
     * @param text the text for the label
     * @param fontSize The size of the font
     * @return the label element
     */
    public static Label createLabel(String text, float fontSize){
        Label rVal = new Label(fontSize);
        rVal.setText(text);
        return rVal;
    }

    /**
     * Simplified constructor
     * @param fontSize the size of the font (default is 1.0f)
     */
    private Label(float fontSize){
        super();
        this.font = Globals.fontManager.getFont("default");
        this.setHeight((int)(font.getFontHeight() * fontSize));
        this.fontSize = fontSize;
        this.setFlexDirection(YogaFlexDirection.Row);
    }
    
    /**
     * Generates the letter elements of the label
     */
    private void generateLetters(){
        //free children
        for(Element child : childList){
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, child);
        }
        this.clearChildren();
        int accumulatingWidth = 0;
        for(int i = 0; i < text.length(); i++){
            char toDraw = text.charAt(i);
            Vector3f bitMapDimension = this.font.getDimensionOfCharacterDiscrete(toDraw);
            BitmapCharacter newLetter = new BitmapCharacter(this.font,(int)(bitMapDimension.x * fontSize), this.getHeight(), fontSize, toDraw);
            accumulatingWidth += bitMapDimension.x * fontSize;
            childList.add(newLetter);
            Yoga.YGNodeInsertChild(yogaNode, newLetter.getYogaNode(), childList.size() - 1);
        }
        Yoga.YGNodeStyleSetWidth(yogaNode, accumulatingWidth);
    }

    /**
     * Sets the size of the font for this label
     * @param fontSize The size of the font
     */
    public void setFontSize(float fontSize){
        this.fontSize = fontSize;
        this.setHeight((int)(font.getFontHeight() * fontSize));
        this.generateLetters();
    }
    
    /**
     * Sets the text of the label
     * @param text The text
     */
    public void setText(String text){
        this.text = text;
        this.generateLetters();
    }

    /**
     * Sets the color of the label
     * @param color The color
     */
    public void setColor(Vector4f color){
        for(Element character : childList){
            ((BitmapCharacter)character).setColor(color);
        }
    }
    
    /**
     * Gets the text of the label
     * @return The text
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
