package electrosphere.client.terrain.sampling;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Samples voxels
 */
public class ClientVoxelSampler {

    /**
     * Returned if a voxel is sampled from an invalid position
     */
    public static final int INVALID_POSITION = -1;
    
    /**
     * Gets the voxel type beneath an entity
     * @param entity The entity
     * @return The voxel type, INVALID_POSITION if the position queried is invalid
     */
    public static int getVoxelTypeBeneathEntity(Entity entity){
        return ClientVoxelSampler.getVoxelType(new Vector3d(EntityUtils.getPosition(entity)).add(new Vector3d(ServerTerrainChunk.VOXEL_SIZE / 2.0f)));
    }

    /**
     * Gets the voxel type at a given real-space position
     * @param realPos The real-space position
     * @return The voxel type id, INVALID_POSITION if the position queried is invalid
     */
    public static int getVoxelType(Vector3d realPos){
        int voxelId = 0;
        Vector3i chunkSpacePos = Globals.clientState.clientWorldData.convertRealToWorldSpace(realPos);
        Vector3i voxelSpacePos = ClientWorldData.convertRealToVoxelSpace(realPos);
        if(Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(chunkSpacePos, ChunkData.NO_STRIDE)){
            ChunkData chunkData = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(chunkSpacePos, ChunkData.NO_STRIDE);
            voxelId = chunkData.getType(voxelSpacePos);
        } else {
            return INVALID_POSITION;
        }
        return voxelId;
    }

}
