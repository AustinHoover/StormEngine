package electrosphere.server.physics.terrain.generation.heightmap;

import electrosphere.engine.Globals;
import electrosphere.util.noise.OpenSimplex2S;

/**
 * Generates hilly heightmaps
 */
public class HillsGen implements HeightmapGenerator {

    /**
     * Offset from baseline to place the noisemap at
     */
    static final float HEIGHT_OFFSET = 100;

    /**
     * Scales the input positions
     */
    static final float POSITION_SCALE = 0.01f;

    /**
     * Scales the output height
     */
    static final float VERTICAL_SCALE = 100.0f;

    /**
     * The different scales of noise to sample from
     */
    static final double[][] GRAD_NOISE = new double[][]{
        {0.01, 2.0},
        {0.02, 2.0},
        {0.05, 1.0},
        {0.1, 1.0},
        {0.3, 1.0},
    };

    /**
     * Distance from origin to sample for gradient calculation
     */
    public static float GRADIENT_DIST = 0.01f;

    /**
     * Param for controlling how pointer the initial layers are
     */
    public static float GRAD_INFLUENCE_DROPOFF = 0.35f;

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
        Globals.profiler.beginAggregateCpuSample("HillsGen.getHeight");
        double scaledX = x * POSITION_SCALE;
        double scaledY = y * POSITION_SCALE;
        float rVal = HillsGen.gradientHeight(SEED, scaledX, scaledY) * VERTICAL_SCALE + HEIGHT_OFFSET;
        Globals.profiler.endCpuSample();
        return rVal;
    }


    /**
     * Applies a gradient approach to heightfield generation
     * @param SEED The seed
     * @param x The x value
     * @param y The y value
     * @return The elevation at x,y
     */
    static float gradientHeight(long SEED, double x, double y){
        float rVal = 0;

        float gradXAccum = 0;
        float gradYAccum = 0;
        float warpX = (float)OpenSimplex2S.noise3_ImproveXY(SEED, x, y, 0);
        float warpY = (float)OpenSimplex2S.noise3_ImproveXY(SEED, x, y, 1);
        for(int n = 0; n < GRAD_NOISE.length; n++){
            //get noise samples
            float noiseOrigin = (float)(OpenSimplex2S.noise2_ImproveX(SEED, (x + warpX) * GRAD_NOISE[n][0], (y + warpY) * GRAD_NOISE[n][0]) * GRAD_NOISE[n][1]);
            float noiseX = (float)(OpenSimplex2S.noise2_ImproveX(SEED, (x + warpX) * GRAD_NOISE[n][0] + GRADIENT_DIST, (y + warpY) * GRAD_NOISE[n][0]) * GRAD_NOISE[n][1]);
            float noiseY = (float)(OpenSimplex2S.noise2_ImproveX(SEED, (x + warpX) * GRAD_NOISE[n][0], (y + warpY) * GRAD_NOISE[n][0] + GRADIENT_DIST) * GRAD_NOISE[n][1]);
            //calculate gradient accumulation
            float gradX = (noiseX - noiseOrigin) / GRADIENT_DIST;
            float gradY = (noiseY - noiseOrigin) / GRADIENT_DIST;
            gradXAccum = gradXAccum + gradX;
            gradYAccum = gradYAccum + gradY;
            //determine current noise's influence based on gradient
            float gradientMagnitude = (float)Math.sqrt(gradXAccum * gradXAccum + gradYAccum * gradYAccum);
            float influence = 1.0f / (1.0f + gradientMagnitude * GRAD_INFLUENCE_DROPOFF);

            //add to height
            float noiseValue = (float)(OpenSimplex2S.noise2_ImproveX(SEED, (x + warpX) * GRAD_NOISE[n][0], (y + warpY) * GRAD_NOISE[n][0]) * GRAD_NOISE[n][1]);
            rVal = rVal + (noiseValue * noiseValue) * influence;
        }
        return rVal;
    }


    @Override
    public String getTag() {
        return "hills";
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }
    
}
