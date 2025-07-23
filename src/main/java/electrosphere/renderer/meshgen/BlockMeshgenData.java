package electrosphere.renderer.meshgen;

import org.joml.Vector3i;

/**
 * Interface for data that can be pased to the block meshgen function
 */
public interface BlockMeshgenData {
    
    /**
     * Checks if the data is empty at a position
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return true if it is empty, false otherwise
     */
    public boolean isEmpty(int x, int y, int z);

    /**
     * Gets the type at a given position
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return The type
     */
    public short getType(int x, int y, int z);

    /**
     * Gets the dimensions of this data
     * @return The dimensions
     */
    public Vector3i getDimensions();

}
