package electrosphere.server.physics.terrain.generation.heightmap;

/**
 * Generates height values for terrain
 */
public interface HeightmapGenerator {
    
    /**
     * Gets the height of a given position, given a SEED value
     * @param SEED The seed for the terrain
     * @param x The x value
     * @param y The y value
     * @return The height
     */
    public float getHeight(long SEED, double x, double y);

    /**
     * Gets the tag associated with this generator
     * @return The tag
     */
    public String getTag();

    /**
     * Sets the seed of the generator
     * @param seed The seed
     */
    public void setSeed(long seed);

}
