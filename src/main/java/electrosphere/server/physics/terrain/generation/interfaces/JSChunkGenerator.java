package electrosphere.server.physics.terrain.generation.interfaces;

import electrosphere.data.biome.BiomeData;

/**
 * A script-defined chunk generator
 */
public interface JSChunkGenerator {
    
    /**
     * Gets the tag for this generator
     * @returns The tag
     */
    public String getTag();

    /**
     * Retrieves the elevation for the world at a given x,z coordinate
     * @param worldX The world x coordinate
     * @param worldZ The world z coordinate
     * @param chunkX The x coordinate of the chunk within the specified world coordinate
     * @param chunkZ The z coordinate of the chunk within the specified world coordinate
     * @returns The elevation at that specific position
     */
    public float getElevation(int worldX, int worldZ, int chunkX, int chunkZ);

    /**
     * The function to get a voxel for a given position
     */
    public GeneratedVoxel getVoxel(
        int worldX, int worldY, int worldZ,
        int chunkX, int chunkY, int chunkZ,
        int stride,
        double surfaceHeight,
        BiomeData surfaceBiome
    );

}
