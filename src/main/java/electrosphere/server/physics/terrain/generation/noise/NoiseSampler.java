package electrosphere.server.physics.terrain.generation.noise;

/**
 * A noise module that can sample values
 */
public interface NoiseSampler {

    /**
     * Gets the name of this noise module
     * @return The name of the module
     */
    public String getName();
    
    /**
     * Samples the noise at a given position with a given seed
     * @param SEED The seed
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The value of the noise at that position for the given seed
     */
    public double getValue(double SEED, double x, double y, double z);

}
