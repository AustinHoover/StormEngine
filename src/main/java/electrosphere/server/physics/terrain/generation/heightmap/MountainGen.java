package electrosphere.server.physics.terrain.generation.heightmap;

import electrosphere.util.noise.OpenSimplex2S;
import io.github.studiorailgun.NoiseUtils;

public class MountainGen implements HeightmapGenerator {

    /**
     * Offset from baseline to place the noisemap at
     */
    static final float HEIGHT_OFFSET = 10;

    /**
     * The falloff factor
     */
    static final double FALLOFF_FACTOR = 10.0f;

    /**
     * Vertical scale of the noise
     */
    static final float VERTICAL_SCALE = 512.0f;

    /**
     * Horizontal scale of the noise
     */
    static final float HORIZONTAL_SCALE = 512.0f;

    /**
     * The power applied to the noise
     */
    static final float POWER_SCALE = 2;

    static final float RELAXATION_FACTOR = 0.13f;

    /**
     * The scale to apply to the coordinates
     */
    static final float GEN_SCALE = 1.0f / HORIZONTAL_SCALE;

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
     * The seed of the generator
     */
    long seed = 0;

    /**
     * Gets the height at a given position for this generation approach
     * @param SEED The seed
     * @param x The x position
     * @param y The y position
     * @return The height
     */
    public float getHeight(long SEED, double x, double y){
        return this.getHeight1(SEED, x, y);
    }

    /**
     * Gets the height at a given position for this generation approach
     * @param SEED The seed
     * @param x The x position
     * @param y The y position
     * @return The height
     */
    public float getHeight1(long SEED, double x, double y){
        float rVal = 0.0f;
        double smoothVoronoiSample = NoiseUtils.smoothVoronoi(x * GEN_SCALE, y * GEN_SCALE, (double)SEED, FALLOFF_FACTOR);
        double inverted = 1.0 - smoothVoronoiSample;
        double noisy = inverted +
        0.02 * OpenSimplex2S.noise2(SEED, x * GEN_SCALE * 3, y * GEN_SCALE * 3) +
        0.005 * OpenSimplex2S.noise2(SEED, x * GEN_SCALE * 5, y * GEN_SCALE * 5)
        ;
        double minClamped = Math.max(noisy,0.0f);
        double powered = Math.pow(minClamped,POWER_SCALE);
        rVal = (float)powered * VERTICAL_SCALE;
        return rVal;
    }

    public float getHeight2(long SEED, double x, double y){
        double invertedMinDist = (
            1.0 * (NoiseUtils.voronoiRelaxed(x * GEN_SCALE, y * GEN_SCALE, RELAXATION_FACTOR)) +
            0.5 * (NoiseUtils.voronoiRelaxed(x * 1.2 * GEN_SCALE, y * 1.2 * GEN_SCALE, RELAXATION_FACTOR)) +
            0.3 * (NoiseUtils.voronoiRelaxed(x * 2 * GEN_SCALE, y * 2 * GEN_SCALE, RELAXATION_FACTOR))
        );

        return (float)(Math.max(invertedMinDist - 0.6,0) / 1.6 * VERTICAL_SCALE);
    }


    @Override
    public String getTag() {
        return "mountains";
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }
}
