package electrosphere.data.voxel;

import java.util.List;

/**
 * Data about a particular type of voxel
 */
public class VoxelType {

    /**
     * the id of this voxel type
     */
    int id;

    /**
     * the name of the type
     */
    String name;

    /**
     * any ambient foliage that can be placed on this voxel type
     */
    List<String> ambientFoliage;

    /**
     * the texture for the voxel type
     */
    String texture;

    /**
     * The corresponding item
     */
    String correspondingItem;

    /**
     * Gets the id of the voxel type
     * @return The id
     */
    public int getId(){
        return id;
    }

    /**
     * Gets the name of the voxel type
     * @return The name
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the names of all ambient foliage that can be placed on this voxel type
     * @return The set of names
     */
    public List<String> getAmbientFoliage(){
        return ambientFoliage;
    }

    /**
     * Gets the texture of this voxel types
     * @return the texture
     */
    public String getTexture(){
        return texture;
    }

    /**
     * Gets the corresponding item
     * @return The corresponding item
     */
    public String getCorrespondingItem(){
        return correspondingItem;
    }
}
