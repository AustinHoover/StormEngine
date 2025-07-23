package electrosphere.server.physics.terrain.generation.noise.operators;

import electrosphere.server.physics.terrain.generation.noise.NoiseOctaveSampler;
import electrosphere.util.noise.OpenSimplex2S;

/**
 * Samples open simplex noise
 */
public class NoiseOperatorOpenSimplex implements NoiseOctaveSampler {

    /**
     * The name of this module
     */
    public static final String NAME = "OpenSimplex";

    /**
     * The frequency to sample at
     */
    double frequency = 0.02;

    /**
     * The lacunarity (the amount to change the frequency each octave)
     */
    double lacunarity = 0.02;

    /**
     * The number of octaves to sample
     */
    int octaveCount = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(double SEED, double x, double y, double z) {
        double rVal = 0;
        double sampleFreq = frequency;
        for(int i = 0; i < octaveCount; i++){
            rVal = rVal + OpenSimplex2S.noise3_ImproveXZ((long)SEED, x * sampleFreq, y * sampleFreq, z * sampleFreq);
            sampleFreq = sampleFreq + lacunarity;
        }
        return rVal;
    }

    @Override
    public void setLacunarity(double lacunarity) {
        this.lacunarity = lacunarity;
    }

    @Override
    public double getLacunarity() {
        return this.lacunarity;
    }

    @Override
    public void setOctaveCount(int octaveCount) {
        this.octaveCount = octaveCount;
    }

    @Override
    public int getOctaveCount() {
        return this.octaveCount;
    }

    @Override
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public double getFrequency() {
        return this.frequency;
    }
    
}
