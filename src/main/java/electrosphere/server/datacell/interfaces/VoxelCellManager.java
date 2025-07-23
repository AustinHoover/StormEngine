package electrosphere.server.datacell.interfaces;

import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Extension of a DataCellManager that provides voxel terrain access and editing functions
 */
public interface VoxelCellManager {

    /**
     * Gets the weight of a single voxel at a position
     * @param worldPosition The position in world coordinates of the chunk to grab data from
     * @param voxelPosition The position in voxel coordinates (local/relative to the chunk) to get voxel values from
     * @return The weight of the described voxel
     */
    public float getVoxelWeightAtLocalPosition(Vector3i worldPosition, Vector3i voxelPosition);

    /**
     * Gets the type of a single voxel at a position
     * @param worldPosition The position in world coordinates of the chunk to grab data from
     * @param voxelPosition The position in voxel coordinates (local/relative to the chunk) to get voxel values from
     * @return The type of the described voxel
     */
    public int getVoxelTypeAtLocalPosition(Vector3i worldPosition, Vector3i voxelPosition);

    /**
     * Gets the chunk data at a given world position
     * @param worldPosition The position in world coordinates
     * @return The ServerTerrainChunk of data at that position, or null if it is out of bounds or otherwise doesn't exist
     */
    public ServerTerrainChunk getChunkAtPosition(Vector3i worldPosition);

    /**
     * Gets the chunk data at a given world position
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @return The ServerTerrainChunk of data at that position, or null if it is out of bounds or otherwise doesn't exist
     */
    public ServerTerrainChunk getChunkAtPosition(int worldX, int worldY, int worldZ);

    /**
     * Edits a single voxel
     * @param worldPosition The world position of the chunk to edit
     * @param voxelPosition The voxel position of the voxel to edit
     * @param weight The weight to set the voxel to
     * @param type The type to set the voxel to
     */
    public void editChunk(Vector3i worldPosition, Vector3i voxelPosition, float weight, int type);

    /**
     * Gets the block data at a given world position
     * @param worldPosition The position in world coordinates
     * @return The BlockChunkData of data at that position, or null if it is out of bounds or otherwise doesn't exist
     */
    public BlockChunkData getBlocksAtPosition(Vector3i worldPosition);

    /**
     * Checks if the manager has already-generated blocks at a given position
     * @param worldPosition The position
     * @return true if there are blocks at the position, false otherwise
     */
    public boolean hasBlocksAtPosition(Vector3i worldPosition);

    /**
     * Edits a single block voxel
     * @param worldPosition The world position of the block to edit
     * @param voxelPosition The local block grid position of the block to edit
     * @param type The type of block to set the position to
     * @param metadata The metadata associated with the block type
     */
    public void editBlock(Vector3i worldPosition, Vector3i voxelPosition, short type, short metadata);


    /**
     * Gets the fluid chunk at a given world position
     * @param worldPosition The world position
     * @return the fluid chunk
     */
    public ServerFluidChunk getFluidChunkAtPosition(Vector3i worldPosition);
    
}
