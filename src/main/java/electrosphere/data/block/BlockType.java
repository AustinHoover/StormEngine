package electrosphere.data.block;

/**
 * Data about a particular type of block
 */
public class BlockType {

    /**
     * the id of this block type
     */
    int id;

    /**
     * the name of the type
     */
    String name;

    /**
     * the texture for the block type
     */
    String texture;

    /**
     * Tracks whether this block type is transparent or not
     */
    Boolean transparent;

    /**
     * Gets the id of the block type
     * @return The id
     */
    public int getId(){
        return id;
    }

    /**
     * Gets the name of the block type
     * @return The name
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the texture of this block types
     * @return the texture
     */
    public String getTexture(){
        return texture;
    }

    /**
     * Checks whether this block type is transparent or not
     * @return true if it is transparent, false otherwise
     */
    public Boolean isTransparent(){
        return transparent;
    }
}
