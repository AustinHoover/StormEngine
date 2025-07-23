package electrosphere.server.physics.terrain.generation.heightmap;

import electrosphere.data.voxel.sampler.SamplerFile;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;

/**
 * Generates a heightmap using a noise definition
 */
public class HeightmapNoiseGen implements HeightmapGenerator {

    /**
     * The tag for the noise function that generates 
     */
    String tag;

    /**
     * The seed of the generator
     */
    long seed = 0;

    /**
     * The sampler to pull from when allocating voxels
     */
    NoiseSampler sampler;

    /**
     * Constructor
     * @param sampler The sampler to pull from
     */
    public HeightmapNoiseGen(SamplerFile samplerDefinitionFile){
        this.sampler = samplerDefinitionFile.getSampler();
        this.tag = samplerDefinitionFile.getName();
    }

    @Override
    public float getHeight(long SEED, double x, double y) {
        return (float)sampler.getValue(0, x, y, 0);
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }
    
}
