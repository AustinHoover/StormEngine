package electrosphere.client.block.cells;

import java.util.HashMap;
import java.util.Map;

import electrosphere.renderer.texture.Texture;

/**
 * An atlas texture and accompanying map of all voxel textures
 */
public class BlockTextureAtlas {

    /**
     * The id is not in the atlas
     */
    public static final int MISSING = -1;

    /**
     * A map of voxel id -> coordinates in the atlas texture for its texture
     */
    Map<Integer,Integer> typeCoordMap = new HashMap<Integer,Integer>();

    /**
     * the actual texture
     */
    Texture specular;

    /**
     * the normal texture
     */
    Texture normal;

    /**
     * the width in pixels of a single texture in the atlas
     */
    public static final int ATLAS_ELEMENT_DIM = 256;
    
    /**
     * the width in pixels of the whole atlas texture
     */
    public static final int ATLAS_DIM = 8192;

    /**
     * number of textures per row in the atlas
     */
    public static final int ELEMENTS_PER_ROW = ATLAS_DIM / ATLAS_ELEMENT_DIM;

    /**
     * Puts an entry in the type-coord map to map a voxel type to a position
     * @param type the voxel type
     * @param coord the coordinate in the map
     */
    public void putTypeCoord(int type, int coord){
        typeCoordMap.put(type,coord);
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
     * Gets the index in the atlas of a provided voxel type (the voxel type is provided by its id)
     * @param voxelTypeId The id of the voxel type
     * @return the index in the atlas of the texture of the provided voxel type
     */
    public int getVoxelTypeOffset(int voxelTypeId){
        return typeCoordMap.containsKey(voxelTypeId) ? typeCoordMap.get(voxelTypeId) : MISSING;
    }

}
