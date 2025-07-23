package electrosphere.renderer.ui.font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import electrosphere.renderer.model.Material;

/**
 * A font
 */
public class Font {
    
    //the font image
    Material fontMaterial;
    //the list of glyphs for the font
    public List<Glyph> glyphs;
    //dimensions of the font image
    public int imageWidth;
    public int imageHeight;

    //the map of character->position in the image
    HashMap<Character,Vector3f> positionMap = new HashMap<Character,Vector3f>();
    //the map of character->dimension of the character in pixels
    HashMap<Character,Vector3f> dimensionMap = new HashMap<Character,Vector3f>();
    
    Map<Character,Glyph> glyphMap = new HashMap<Character,Glyph>();

    /**
     * Creates the font object
     * @param fontMaterial
     * @param glyphs
     * @param imageWidth
     * @param imageHeight
     */
    protected Font(
        Material fontMaterial,
        List<Glyph> glyphs,
        int imageWidth,
        int imageHeight
    ){
        this.fontMaterial = fontMaterial;
        this.glyphs = glyphs;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
    }
    
    /**
     * Data about a single glyph in the font
     */
    public static class Glyph {

        public String symbol;
        public int startX;
        public int startY;
        public int width;
        public int height;
        public int offsetY = 0;
        public float quadScalingY = 1;
    }

    /**
     * Gets the position of a given character
     * @param character The character
     * @return the position
     */
    public Vector3f getPositionOfCharacter(char character){
        Vector3f position;
        if((position = positionMap.get(character))!=null){
            position = new Vector3f(position);
            position.x = position.x / imageWidth;
            position.y = position.y / imageHeight;
        } else {
            position = new Vector3f();
        }
        return position;
    }
    
    /**
     * Gets the dimensions of a given character
     * @param character the character
     * @return the dimensions
     */
    public Vector3f getDimensionOfCharacter(char character){
        Vector3f dimension;
        if((dimension = dimensionMap.get(character))!=null){
            dimension = new Vector3f(dimension);
            dimension.x = dimension.x / imageWidth;
            dimension.y = dimension.y / imageHeight;
        } else {
            dimension = new Vector3f(0.5f,0.5f,0f);
        }
        return dimension;
    }

    /**
     * Gets the dimensions of a given character in discrete pixels
     * @param character The character
     * @return the dimensions in pixels
     */
    public Vector3f getDimensionOfCharacterDiscrete(char character){
        Vector3f dimension;
        if((dimension = dimensionMap.get(character))!=null){
            dimension = new Vector3f(dimension);
        } else {
            dimension = new Vector3f(12,14,0f);
        }
        return dimension;
    }

    /**
     * Gets the offset y of the character
     * @param c The character
     * @return The offset y
     */
    public int getOffsetY(Character c){
        int rVal = 0;
        if(glyphMap.containsKey(c)){
            rVal = glyphMap.get(c).offsetY;
        }
        return rVal;
    }

    /**
     * Gets the quad y scaling of the character
     * @param c The character
     * @return The scaling
     */
    public float getQuadScalingY(Character c){
        float rVal = 1.0f;
        if(glyphMap.containsKey(c)){
            rVal = glyphMap.get(c).quadScalingY;
        }
        return rVal;
    }
    
    /**
     * Gets the height of the font
     * @return the height
     */
    public int getFontHeight(){
        int maxHeight = 0;
        for(Glyph glyph : this.glyphs){
            if(glyph.height > maxHeight){
                maxHeight = glyph.height;
            }
        }
        return maxHeight;
    }


    /**
     * Processes the glyphs into structures that will contain instantly lookup-able data
     */
    protected void process(){
        for(Glyph glyph : glyphs){
            char charVal = glyph.symbol.charAt(0);
            Vector3f position = new Vector3f(glyph.startX,glyph.startY,0);
            positionMap.put(charVal,position);
        }
        //fill dimension map
        dimensionMap.clear();
        for(Glyph glyph : glyphs){
            char charVal = glyph.symbol.charAt(0);
            Vector3f dimension = new Vector3f(glyph.width,glyph.height,0);
            dimensionMap.put(charVal,dimension);
        }
        //fill glyph map
        for(Glyph glyph: glyphs){
            this.glyphMap.put(glyph.symbol.charAt(0),glyph);
        }
    }

    /**
     * Gets the material for the font
     * @return The material
     */
    public Material getMaterial(){
        return fontMaterial;
    }
    
    
}
