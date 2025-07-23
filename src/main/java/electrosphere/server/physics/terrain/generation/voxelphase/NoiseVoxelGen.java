package electrosphere.server.physics.terrain.generation.voxelphase;

import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeFloorElement;
import electrosphere.data.biome.BiomeSurfaceGenerationParams;
import electrosphere.data.voxel.sampler.SamplerFile;
import electrosphere.server.physics.terrain.generation.interfaces.GeneratedVoxel;
import electrosphere.server.physics.terrain.generation.interfaces.GenerationContext;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;

/**
 * Generates voxels based on a noise config
 */
public class NoiseVoxelGen implements VoxelGenerator {

    /**
     * The width of the surface in number of voxels
     */
    public static final int SURFACE_VOXEL_WIDTH = 2;

    /**
     * The voxel type for the floor of the world
     */
    public static final int FLOOR_VOXEL_TYPE = 6;

    /**
     * The tag for the noise function that generates 
     */
    String tag;

    /**
     * The sampler to pull from when allocating voxels
     */
    NoiseSampler sampler;

    /**
     * The seed of the generator
     */
    long seed;

    /**
     * Constructor
     * @param samplerDefinitionFile The file to model this generator off of
     */
    public NoiseVoxelGen(SamplerFile samplerDefinitionFile){
        this.sampler = samplerDefinitionFile.getSampler();
        this.tag = samplerDefinitionFile.getName();
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }

    @Override
    public void getVoxel(
        GeneratedVoxel voxel,
        int worldX, int worldY, int worldZ,
        int chunkX, int chunkY, int chunkZ,
        double realX, double realY, double realZ,
        int stride, double surfaceHeight, double surfaceGradient, double surfaceSelectionNoise,
        BiomeData surfaceBiome, BiomeSurfaceGenerationParams surfaceParams,
        GenerationContext generationContext
    ) {

        //floor handling
        if(realY < 1){
            voxel.weight = 1.0f;
            voxel.type = FLOOR_VOXEL_TYPE;
            return;
        }

        double strideMultiplier = Math.pow(2,stride);
        double heightDiff = realY - surfaceHeight;
        double sample = 1.0;//this.sampler.getValue(0, realX, realY, realZ);
        if(sample <= 0){
            voxel.weight = (float)(sample * 2);
            voxel.type = 0;
            return;
        }
        sample = Math.min(sample,1.0);
        if(heightDiff < -strideMultiplier * SURFACE_VOXEL_WIDTH){
            //below surface, ie generate stone here
            double finalSurface = sample;
            voxel.weight = (float)finalSurface;
            voxel.type = 6;
        } else if(heightDiff > 0) {
            //above surface, ie generate air here
            voxel.weight = -1.0f;
            voxel.type = 0;
        } else if(heightDiff < -strideMultiplier){
            BiomeFloorElement floorEl = surfaceParams.getFloorVariant((float)surfaceSelectionNoise);
            //generate full-size surface-type voxel, ie generate grass here
            double finalHeight = sample;
            voxel.weight = (float)finalHeight;
            voxel.type = floorEl.getVoxelId();
        } else {
            BiomeFloorElement floorEl = surfaceParams.getFloorVariant((float)surfaceSelectionNoise);
            //surface, ie generate grass here
            double surfacePercent = -heightDiff / strideMultiplier;
            if(surfacePercent > 1.0 || surfacePercent < 0){
                throw new Error("surfacePercent " + surfacePercent + " " + realY + " " + surfaceHeight + " " + heightDiff + " " + strideMultiplier);
            }
            double finalHeight = sample * surfacePercent * 2 - 1;
            voxel.weight = (float)(finalHeight * sample);
            voxel.type = floorEl.getVoxelId();
        }
    }

    /**
     * Calculates the weight of a voxel on the surface based on the surface height, the position of the voxel, and the stride multiplier
     * @param surfaceHeight The surface height
     * @param realPosY The position of the voxel
     * @param strideMultiplier The stride multiplier
     * @return The weight of the voxel
     */
    protected static double getSurfaceWeight(double surfaceHeight, double realPosY, double strideMultiplier){
        return ((realPosY - surfaceHeight) / strideMultiplier);
    }
    
}
