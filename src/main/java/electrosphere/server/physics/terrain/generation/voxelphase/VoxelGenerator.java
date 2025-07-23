package electrosphere.server.physics.terrain.generation.voxelphase;

import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeSurfaceGenerationParams;
import electrosphere.server.physics.terrain.generation.interfaces.GeneratedVoxel;
import electrosphere.server.physics.terrain.generation.interfaces.GenerationContext;


/**
 * Used for generating voxels
 */
public interface VoxelGenerator {


    /**
     * Gets the tag of the generator
     * @return The tag
     */
    public String getTag();

    /**
     * Sets the seed of the generator
     * @param seed The seed
     */
    public void setSeed(long seed);


    /**
     * Gets the value for a chunk
     * @param voxel The voxel to fill
     * @param worldX The world x pos
     * @param worldY The world y pos
     * @param worldZ The world z pos
     * @param chunkX The chunk x pos
     * @param chunkY The chunk y pos
     * @param chunkZ The chunk z pos
     * @param stride The stride of the data
     * @param surfaceHeight The height of the surface at x,z
     * @param surfaceGradient The rate of change in the surface at this point
     * @param surfaceSelectionNoise The noise value to select surface variants
     * @param surfaceBiome The surface biome of the chunk
     * @param surfaceGenPArams Extra parameters for generating surface voxel values
     * @param generationContext The generation context
     */
    public void getVoxel(
        GeneratedVoxel voxel,
        int worldX, int worldY, int worldZ,
        int chunkX, int chunkY, int chunkZ,
        double realX, double realY, double realZ,
        int stride,
        double surfaceHeight, double surfaceGradient, double surfaceSelectionNoise,
        BiomeData surfaceBiome, BiomeSurfaceGenerationParams surfaceGenParams,
        GenerationContext generationContext
    );
    
}
