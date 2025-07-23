package electrosphere.server.physics.terrain.generation;

import java.util.List;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.entity.scene.RealmDescriptor;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.physics.terrain.generation.interfaces.ChunkGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * An arena terrain chunk generator
 */
public class DefaultChunkGenerator implements ChunkGenerator {

    /**
     * The id to generate the floor with
     */
    int baseVoxelId = RealmDescriptor.VOXEL_DIRT_ID;

    /**
     * Constructor
     */
    public DefaultChunkGenerator(){
    }

    @Override
    public ServerTerrainChunk generateChunk(List<MacroObject> macroData, int worldX, int worldY, int worldZ, int stride) {
        //Each chunk also needs custody of the next chunk's first values so that they can perfectly overlap.
        //Hence, width should actually be chunk dimension + 1
        float[][][] weights = new float[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
        int[][][] values = new int[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
        for(int inc = 0; inc < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; inc++){
            for(int weightX = 0; weightX < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; weightX++){
                for(int weightZ = 0; weightZ < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; weightZ++){
                    weights[weightX][inc][weightZ] = -1;
                    values[weightX][inc][weightZ] = 0;
                }
            }
        }
        if(worldY < 1){
            for(int weightX = 0; weightX < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; weightX++){
                for(int weightZ = 0; weightZ < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; weightZ++){
                    weights[weightX][0][weightZ] = 0.1f;
                    values[weightX][0][weightZ] = baseVoxelId;
                }
            }
        }
        ServerTerrainChunk rVal = new ServerTerrainChunk(worldX, worldY, worldZ, ChunkData.NOT_HOMOGENOUS, weights, values);
        return rVal;
    }

    @Override
    public double getElevation(int worldX, int worldZ, int chunkX, int chunkZ){
        return 0.1;
    }

    @Override
    public void setModel(TerrainModel model) {
        //Does nothing as the arena is not based on a terrain model
    }

    /**
     * Sets the base voxel id of the chunk generator
     * @param baseVoxelId The base voxel id
     */
    public void setBaseVoxelId(int baseVoxelId){
        this.baseVoxelId = baseVoxelId;
    }
    
}
