package electrosphere.renderer.texture;

import java.util.HashMap;
import java.util.Map;

/**
 * An atlas of texture path -> coordinates on provided texture
 */
public class TextureAtlas {
    

    /**
     * A map of texture path -> coordinates in the atlas texture for its texture
     */
    private Map<String,Integer> pathCoordMap = new HashMap<String,Integer>();


    /**
     * the actual texture
     */
    private Texture specular;

    /**
     * the normal texture
     */
    private Texture normal;

    /**
     * the width in pixels of a single texture in the atlas
     */
    public static final int ATLAS_ELEMENT_DIM = 64;

    /**
     * the width in pixels of the whole atlas texture
     */
    public static final int ATLAS_DIM = 1024;

    /**
     * number of textures per row in the atlas
     */
    public static final int ELEMENTS_PER_ROW = ATLAS_DIM / ATLAS_ELEMENT_DIM;

    /**
     * Puts an entry in the path-coord map to map a texture path to a position
     * @param path the texture's path
     * @param coord the coordinate in the map
     */
    public void putPathCoord(String path, int coord){
        pathCoordMap.put(path,coord);
    }

    /**
     * Sets the specular
     * @param specular the specular
     */
    public void setSpecular(Texture specular){
        this.specular = specular;
    }

    /**
     * Sets the normal
     * @param normal the normal
     */
    public void setNormal(Texture normal){
        this.normal = normal;
    }

    /**
     * Gets the atlas specular
     * @return the atlas specular
     */
    public Texture getSpecular(){
        return specular;
    }

    /**
     * Gets the atlas normal
     * @return the atlas normal
     */
    public Texture getNormal(){
        return normal;
    }

    /**
     * Gets the index in the atlas of a provided texture path
     * @param texturePath The path to the texture you want the index of
     * @return the index in the atlas of the texture at the provided texture path
     */
    public int getTextureIndex(String texturePath){
        return pathCoordMap.containsKey(texturePath) ? pathCoordMap.get(texturePath) : -1;
    }

    /**
     * Gets the NDC dimensions of an image in the atlas
     * @return The NDC dimension
     */
    public float getNDCDimension(){
        return ATLAS_ELEMENT_DIM / (float)ATLAS_DIM;
    }

    /**
     * Gets the NDC x coordinate of an index in the atlas
     * @param i The index
     * @return The NDC x coordinate
     */
    public float getNDCCoordX(int i){
        return (i % ELEMENTS_PER_ROW) * this.getNDCDimension();
    }

    /**
     * Gets the NDC y coordinate of an index in the atlas
     * @param i The index
     * @return The NDC y coordinate
     */
    public float getNDCCoordY(int i){
        return (i / ELEMENTS_PER_ROW) * this.getNDCDimension();
    }

}
