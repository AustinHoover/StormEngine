package electrosphere.server.physics.terrain.generation.heightmap;

import electrosphere.util.noise.OpenSimplex2S;

/**
 * Generates a mild plain that would look like a seafloor
 */
public class SeaFloorGen implements HeightmapGenerator {

    /**
     * The seed of the generator
     */
    long seed = 0;

    /**
     * The scale of the noise
     */
    float noiseScale = 0.01f;

    @Override
    public float getHeight(long SEED, double x, double y) {
        float noise = (float)OpenSimplex2S.noise2_ImproveX(SEED, x * noiseScale, y * noiseScale);
        return noise;
    }

    @Override
    public String getTag() {
        return "seafloor";
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }
    
}
