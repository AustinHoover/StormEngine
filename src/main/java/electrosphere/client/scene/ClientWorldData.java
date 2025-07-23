package electrosphere.client.scene;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Client's data on the world
 */
public class ClientWorldData {

    
    
    /*
    
                       world max
    +---------------------+
    |                     |
    |                     |
    |                     |
    |                     |
    |                     |
    +---------------------+
 world min
    
    
    basically we're saying what the maximum and minimum x and z something can occupy are
    
    FOR THE TIME BEING DOES NOT ACCOUNT FOR Y
    */
    Vector3f worldMinPoint;
    Vector3f worldMaxPoint;
    
    
    int worldDiscreteSize;
    

    public ClientWorldData(
        Vector3f worldMinPoint,
        Vector3f worldMaxPoint,
        int worldDiscreteSize
    ) {
        this.worldMinPoint = worldMinPoint;
        this.worldMaxPoint = worldMaxPoint;
        this.worldDiscreteSize = worldDiscreteSize;
    }
    
    
    
    
    public Vector3f getWorldBoundMin(){
        return worldMinPoint;
    }
    
    public Vector3f getWorldBoundMax(){
        return worldMaxPoint;
    }

    public int getWorldDiscreteSize() {
        return worldDiscreteSize;
    }
    
    
    public static int convertRealToChunkSpace(double real){
        return (int)Math.floor(real / ServerTerrainChunk.CHUNK_DIMENSION);
    }

    public static Vector3i convertRealToChunkSpace(Vector3d real){
        return new Vector3i(
            ClientWorldData.convertRealToChunkSpace(real.x),
            ClientWorldData.convertRealToChunkSpace(real.y),
            ClientWorldData.convertRealToChunkSpace(real.z)
        );
    }

    public static float convertChunkToRealSpace(int chunk){
        return chunk * ServerTerrainChunk.CHUNK_DIMENSION;
    }

    public int convertRealToWorld(double real){
        return convertRealToChunkSpace(real);
    }

    public double convertWorldToReal(int world){
        return convertChunkToRealSpace(world);
    }

    /**
     * Checks if a world position is in bounds or not
     * @param worldPos The world position
     * @return true if is in bounds, false otherwise
     */
    public boolean worldPosInBounds(Vector3i worldPos){
        return worldPos.x >= convertRealToWorld(worldMinPoint.x) &&
        worldPos.x < convertRealToWorld(worldMaxPoint.x) &&
        worldPos.y >= convertRealToWorld(worldMinPoint.y) &&
        worldPos.y < convertRealToWorld(worldMaxPoint.y) &&
        worldPos.z >= convertRealToWorld(worldMinPoint.z) &&
        worldPos.z < convertRealToWorld(worldMaxPoint.z)
        ;
    }

    /**
     * Converts a real space position to its world space equivalent
     * @param position The real space position
     * @return The world space position (ie the chunk containing the real space position)
     */
    public Vector3i convertRealToWorldSpace(Vector3d position){
        return new Vector3i(
            convertRealToChunkSpace(position.x),
            convertRealToChunkSpace(position.y),
            convertRealToChunkSpace(position.z)
        );
    }

    /**
     * Converts a real space position to its absolute voxel space equivalent
     * @param position The real space position
     * @return The absolute voxel space position ie the voxel-aligned position not clamped to the current chunk
     */
    public Vector3i convertRealToAbsoluteVoxelSpace(Vector3d position){
        return new Vector3i(
            (int)Math.floor(position.x),
            (int)Math.floor(position.y),
            (int)Math.floor(position.z)
        );
    }

    /**
     * Converts a absolute voxel position to its relative voxel space equivalent
     * @param position The real space position
     * @return The relative voxel space position ie the voxel-aligned position not clamped to the current chunk
     */
    public Vector3i convertAbsoluteVoxelToRelativeVoxelSpace(Vector3i position){
        return new Vector3i(
            position.x % ServerTerrainChunk.CHUNK_DIMENSION,
            position.y % ServerTerrainChunk.CHUNK_DIMENSION,
            position.z % ServerTerrainChunk.CHUNK_DIMENSION
        );
    }

    /**
     * Converts a absolute voxel position to its world space equivalent
     * @param position The real space position
     * @return The world space position ie the voxel-aligned position not clamped to the current chunk
     */
    public Vector3i convertAbsoluteVoxelToWorldSpace(Vector3i position){
        return new Vector3i(
            position.x / ServerTerrainChunk.CHUNK_DIMENSION,
            position.y / ServerTerrainChunk.CHUNK_DIMENSION,
            position.z / ServerTerrainChunk.CHUNK_DIMENSION
        );
    }


    /**
     * Converts a relative voxel position to its absolute voxel equivalent
     * @param voxelPos The relative voxel position
     * @param worldPos The position of the chunk
     * @return The absolute voxel position ie the voxel-aligned position not clamped to the current chunk
     */
    public Vector3i convertRelativeVoxelToAbsoluteVoxelSpace(Vector3i voxelPos, Vector3i worldPos){
        return new Vector3i(
            worldPos.x * ServerTerrainChunk.CHUNK_DIMENSION + voxelPos.x,
            worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION + voxelPos.y,
            worldPos.z * ServerTerrainChunk.CHUNK_DIMENSION + voxelPos.z
        );
    }

    /**
     * Converts a relative voxel position to its absolute voxel equivalent
     * @param voxelPos The relative voxel position
     * @param worldPos The position of the chunk
     * @return The absolute voxel position ie the voxel-aligned position not clamped to the current chunk
     */
    public int convertRelativeVoxelToAbsoluteVoxelSpace(int voxelPos, int worldPos){
        return worldPos * ServerTerrainChunk.CHUNK_DIMENSION + voxelPos;
    }

    /**
     * Converts a world space vector to a real space vector
     * @param position The world space vector
     * @return The real space vector
     */
    public Vector3d convertWorldToRealSpace(Vector3i position){
        return new Vector3d(
            this.convertWorldToReal(position.x),
            this.convertWorldToReal(position.y),
            this.convertWorldToReal(position.z)
        );
    }

    public static Vector3i convertRealToVoxelSpace(Vector3d position){
        return new Vector3i(
            (int)Math.floor(position.x - ClientWorldData.convertChunkToRealSpace(ClientWorldData.convertRealToChunkSpace(position.x))),
            (int)Math.floor(position.y - ClientWorldData.convertChunkToRealSpace(ClientWorldData.convertRealToChunkSpace(position.y))),
            (int)Math.floor(position.z - ClientWorldData.convertChunkToRealSpace(ClientWorldData.convertRealToChunkSpace(position.z)))
        );
    }

    /**
     * Converts a real position to a block position
     * @param real The real position
     * @return The closest block position
     */
    public static int convertRealToLocalBlockSpace(double real){
        return (int)Math.floor(real * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE % BlockChunkData.CHUNK_DATA_WIDTH);
    }

    /**
     * Converts the position to a block-space position
     * @param position The real-space position
     * @return The nearest block-space position
     */
    public static Vector3i convertRealToLocalBlockSpace(Vector3d position){
        return new Vector3i(
            ClientWorldData.convertRealToLocalBlockSpace(position.x),
            ClientWorldData.convertRealToLocalBlockSpace(position.y),
            ClientWorldData.convertRealToLocalBlockSpace(position.z)
        );
    }

    /**
     * Converts a block position to a real position
     * @param chunkPos The position of the chunk
     * @param blockPos The position of the block within the chunk
     * @return The real position corresponding to the block's position
     */
    public double convertBlockToRealSpace(int chunkPos, int blockPos){
        return ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET * chunkPos + BlockChunkData.BLOCK_SIZE_MULTIPLIER * blockPos;
    }

    /**
     * Converts a block position to a real position
     * @param chunkPos The chunk's position
     * @param blockPos The block's position
     * @return The real position
     */
    public Vector3d convertBlockToRealSpace(Vector3i chunkPos, Vector3i blockPos){
        return new Vector3d(
            convertBlockToRealSpace(chunkPos.x, blockPos.x),
            convertBlockToRealSpace(chunkPos.y, blockPos.y),
            convertBlockToRealSpace(chunkPos.z, blockPos.z)
        );
    }

    /**
     * Checks that a chunk position is in bounds
     * @param chunkPos The chunk pos
     * @return true if it is in bounds, false otherwise
     */
    public boolean chunkInBounds(Vector3i chunkPos){
        return chunkPos.x >= 0 && chunkPos.y >= 0 && chunkPos.z >= 0 &&
        chunkPos.x < this.worldDiscreteSize && chunkPos.y < this.worldDiscreteSize && chunkPos.z < this.worldDiscreteSize;
    }

    /**
     * Clamps a real space position to the closest block space position
     * @param realPos The real space position
     * @return The real space position that is clamped to the closest block space position
     */
    public static Vector3d clampRealToBlock(Vector3d realPos){
        return new Vector3d(
            realPos.x - realPos.x % BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            realPos.y - realPos.y % BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            realPos.z - realPos.z % BlockChunkData.BLOCK_SIZE_MULTIPLIER
        );
    }

}
