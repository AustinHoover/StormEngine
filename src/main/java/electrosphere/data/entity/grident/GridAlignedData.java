package electrosphere.data.entity.grident;

/**
 * Data for aligning this entity with the block grid
 */
public class GridAlignedData {
    
    /**
     * The width in blocks to occupy on the block grid
     */
    Integer width;

    /**
     * The length in blocks to occupy on the block grid
     */
    Integer length;

    /**
     * The height in blocks to occupy on the block grid
     */
    Integer height;

    /**
     * Gets the height in blocks to occupy on the block grid
     * @return The height in blocks to occupy on the block grid
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Gets the length in blocks to occupy on the block grid
     * @return The length in blocks to occupy on the block grid
     */
    public Integer getLength() {
        return length;
    }

    /**
     * Gets the width in blocks to occupy on the block grid
     * @return The width in blocks to occupy on the block grid
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Sets the width of the grid alignment data
     * @param width The width
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * Sets the length of the grid alignment data
     * @param length The length
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Sets the height of the grid alignment data
     * @param height The height
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    

}
