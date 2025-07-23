package electrosphere.server.physics.terrain.generation.noise;

/**
 * A noce function that can be sampled in octaves
 */
public interface NoiseOctaveSampler extends NoiseSampler {
    
    /**
     * Sets the lacunarity of the sampler
     * @param lacunarity The lacunarity
     */
    public void setLacunarity(double lacunarity);

    /**
     * Gets the lacunarity of the sampler
     * @return The lacunarity
     */
    public double getLacunarity();

    /**
     * Sets the number of octaves to sample
     * @param octaveCount The number of octaves to sample
     */
    public void setOctaveCount(int octaveCount);

    /**
     * Gets the number of octaves to sample
     * @return The number of octaves to sample
     */
    public int getOctaveCount();

    /**
     * Sets the frequency to sample at
     * @param frequency The frequency
     */
    public void setFrequency(double frequency);

    /**
     * Gets the frequency of the function
     * @return The frequency
     */
    public double getFrequency();

}
