package electrosphere.renderer.ui.elements;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.font.Font;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * A single character
 */
public class BitmapCharacter extends StandardElement implements DrawableElement {
    
    String text;
    
    Vector4f color = new Vector4f(1.0f);

    Font font;

    float fontSize = 1.0f;
    
    
    /**
     * Constructor
     * @param font
     * @param posX
     * @param posY
     * @param width
     * @param height
     * @param toDraw
     */
    public BitmapCharacter(Font font, int width, int height, float fontSize, char toDraw){
        super();
        setWidth(width);
        setHeight(height);
        this.text = "" + toDraw;
        this.font = font;
        this.fontSize = fontSize;
    }

    /**
     * Creates a bitmap character that will be positioned by Yoga
     * @param font The font of the character
     * @param toDraw The glyph to draw
     */
    public BitmapCharacter(Font font, char toDraw){
        super();
        this.text = "" + toDraw;
        this.font = font;
        Vector3f discreteDims = this.font.getDimensionOfCharacterDiscrete(toDraw);
        setMinWidth((int)discreteDims.x);
        setMinHeight((int)Math.max(discreteDims.y,this.font.getFontHeight()));
    }
    
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public void setColor(Vector4f color) {
        this.color = color;
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
        float ndcX =      (float)this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX)/framebuffer.getWidth();
        float ndcY =      (float)this.absoluteToFramebuffer(getAbsoluteYPlacement(),framebufferPosY)/framebuffer.getHeight();
        float ndcWidth =  (float)getWidth()/framebuffer.getWidth();
        float ndcHeight = (float)getHeightPlacement()/framebuffer.getHeight();
        char toDraw = text.charAt(0);
        Vector3f characterPosition = new Vector3f(ndcX,ndcY,0);
        Vector3f characterDimensions = new Vector3f(ndcWidth,ndcHeight,0);
        Vector3f bitMapPosition = this.font.getPositionOfCharacter(toDraw);
        Vector3f bitMapDimension = this.font.getDimensionOfCharacter(toDraw);
        //load model and try overwriting with font material
        Model charModel = Globals.assetManager.fetchModel(AssetDataStrings.BITMAP_CHARACTER_MODEL);
        Material mat = this.font.getMaterial();
        charModel.tryOverwriteMaterial(mat);
        if(charModel != null && toDraw != ' '){
            charModel.pushUniformToMesh(AssetDataStrings.ASSET_STRING_BITMAP_FONT_MESH_NAME, "mPosition", characterPosition);
            charModel.pushUniformToMesh(AssetDataStrings.ASSET_STRING_BITMAP_FONT_MESH_NAME, "mDimension", characterDimensions);
            charModel.pushUniformToMesh(AssetDataStrings.ASSET_STRING_BITMAP_FONT_MESH_NAME, "tPosition", bitMapPosition);
            charModel.pushUniformToMesh(AssetDataStrings.ASSET_STRING_BITMAP_FONT_MESH_NAME, "tDimension", bitMapDimension);
            charModel.pushUniformToMesh(AssetDataStrings.ASSET_STRING_BITMAP_FONT_MESH_NAME, "color", color);
            charModel.drawUI();
        }
    }

    /**
     * Gets the absolute y to use for placement
     * @return The absolute y to use for placement
     */
    public int getAbsoluteYPlacement(){
        return super.getAbsoluteY() + (int)Math.ceil(this.font.getOffsetY(text.charAt(0)) * this.fontSize);
    }

    
    /**
     * Gets the height to use for placement
     * @return The height to use for placement
     */
    public int getHeightPlacement(){
        return (int)(Math.ceil(super.getHeight() * this.font.getQuadScalingY(text.charAt(0))));
    }

    public boolean handleEvent(Event event){
        return true;
    }
    
}
