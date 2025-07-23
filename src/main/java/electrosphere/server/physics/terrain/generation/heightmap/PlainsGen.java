package electrosphere.server.physics.terrain.generation.heightmap;

import electrosphere.util.noise.OpenSimplex2S;

/**
 * Generator for plains
 */
public class PlainsGen implements HeightmapGenerator {

    /**
     * Offset from baseline to place the noisemap at
     */
    static final float HEIGHT_OFFSET = 10;

    /**
     * The scale to apply to the coordinates
     */
    static final float GEN_SCALE = 0.2f;

    /**
     * The seed of the generator
     */
    long seed = 0;

    /**
     * The different scales of noise to sample from
     */
    static final double[][] NOISE_SCALES = new double[][]{
        {0.01, 3.0},
        {0.02, 2.0},
        {0.05, 0.8},
        {0.1, 0.3},
        {0.3, 0.2},
    };


    /**
     * Gets the height at a given position for this generation approach
     * @param SEED The seed
     * @param x The x position
     * @param y The y position
     * @return The height
     */
    public float getHeight(long SEED, double x, double y){
        return PlainsGen.sampleAllNoise(SEED, x, y) + HEIGHT_OFFSET;
    }


    /**
     * Samples all noise values directly
     * @param SEED The seed
     * @param x The x value
     * @param y The y value
     * @return The elevation at x,y
     */
    static float sampleAllNoise(long SEED, double x, double y){
        float rVal = 0;
        double scaledX = x * GEN_SCALE;
        double scaledY = y * GEN_SCALE;
        for(int n = 0; n < NOISE_SCALES.length; n++){
            rVal = rVal + (float)(OpenSimplex2S.noise2_ImproveX(SEED, scaledX * NOISE_SCALES[n][0], scaledY * NOISE_SCALES[n][0]) * NOISE_SCALES[n][1]);
        }
        return rVal;
    }


    @Override
    public String getTag() {
        return "plains";
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }
    
}
