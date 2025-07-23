package electrosphere.server.physics.terrain.generation;

import java.util.List;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.physics.terrain.generation.interfaces.ChunkGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * Chunk generator for overworld chunks
 */
public class OverworldChunkGenerator implements ChunkGenerator {

    //The model of terrain for the overworld
    TerrainModel model;

    //cache for the bicubic interpolated chunks
    //don't need to interpolate each time a new chunk is created
    //This should eventually be removed as terrain generation becomes more complicated than a heightmap
    // Map<String, float[][]> heightmapCache = new ConcurrentHashMap<String, float[][]>();

    /**
     * Constructor
     */
    public OverworldChunkGenerator(){
    }

    @Override
    public ServerTerrainChunk generateChunk(List<MacroObject> macroData, int worldX, int worldY, int worldZ, int stride) {
        ServerTerrainChunk returnedChunk;
        //Each chunk also needs custody of the next chunk's first values so that they can perfectly overlap.
        //Hence, width should actually be chunk dimension + 1
        float[][] heightmap = getHeightmap(worldX, worldZ);
        float[][][] weights = new float[ServerTerrainChunk.CHUNK_DIMENSION][ServerTerrainChunk.CHUNK_DIMENSION][ServerTerrainChunk.CHUNK_DIMENSION];
        int[][][] values = new int[ServerTerrainChunk.CHUNK_DIMENSION][ServerTerrainChunk.CHUNK_DIMENSION][ServerTerrainChunk.CHUNK_DIMENSION];
        for(int weightX = 0; weightX < ServerTerrainChunk.CHUNK_DIMENSION; weightX++){
            for(int weightY = 0; weightY < ServerTerrainChunk.CHUNK_DIMENSION; weightY++){
                for(int weightZ = 0; weightZ < ServerTerrainChunk.CHUNK_DIMENSION; weightZ++){
                    float height = heightmap[ServerTerrainChunk.CHUNK_DIMENSION * worldX + weightX][ServerTerrainChunk.CHUNK_DIMENSION * worldZ + weightZ];
                    if(weightY < height){
                        weights[weightX][weightY][weightZ] = 1;
                        values[weightX][weightY][weightZ] = 1;
                    } else if(height == 0 && weightY == 0 && worldY == 0) {
                        weights[weightX][weightY][weightZ] = 0.1f;
                        values[weightX][weightY][weightZ] = 1;
                    } else {
                        weights[weightX][weightY][weightZ] = -1;
                        values[weightX][weightY][weightZ] = 0;
                    }
                }
            }
        }
        returnedChunk = new ServerTerrainChunk(worldX, worldY, worldZ, ChunkData.NOT_HOMOGENOUS, weights, values);
        return returnedChunk;
    }

    @Override
    public double getElevation(int worldX, int worldZ, int chunkX, int chunkZ){
        float[][] heightmap = getHeightmap(worldX, worldZ);
        return heightmap[chunkX][chunkZ];
    }

    @Override
    /**
     * Sets the terrain model for the overworld algo
     */
    public void setModel(TerrainModel model){
        this.model = model;
    }

    /**
     * Gets a heightmap array. Either pulls it from cache if it exists or does the logic to generate it
     * @param worldX The x position in world coordinates of the chunk
     * @param worldZ THe z position in world coordinates of the chunk
     * @return The heightmap array
     */
    private float[][] getHeightmap(int worldX, int worldZ){
        // String key = worldX + "_" + worldZ;
        // if(heightmapCache.containsKey(key)){
        //     return heightmapCache.get(key);
        // } else {
        //     float[][] macroValues = model.getRad5MacroValuesAtPosition(worldX, worldZ);
        //     float[][] heightmap = TerrainInterpolator.getBicubicInterpolatedChunk(
        //             macroValues,
        //             model.getDynamicInterpolationRatio()
        //     );
        //     heightmapCache.put(key,heightmap);
        //     return heightmap;
        // }
        return null;
    }
    
}
