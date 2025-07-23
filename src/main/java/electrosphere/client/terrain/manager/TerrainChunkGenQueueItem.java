package electrosphere.client.terrain.manager;

import electrosphere.client.terrain.cells.DrawCell;
import electrosphere.client.terrain.cells.VoxelTextureAtlas;
import electrosphere.client.terrain.data.TerrainChunkData;
import electrosphere.entity.Entity;

/**
 * Represents an item in a queue of terrain chunks to have models generated in the main thread
 */
public class TerrainChunkGenQueueItem {
    
    //the data of the chunk (verts, normals, etc)
    TerrainChunkData data;
    //the hash promised to store the model under in asset manager
    String promisedHash;
    //the texture atlas
    VoxelTextureAtlas atlas;

    /**
     * The draw cell to notify once this model is fully available to render
     */
    DrawCell notifyTarget;

    /**
     * The optional entity to delete on generation of this target
     */
    Entity toDelete;

    /**
     * Creates a queue item
     * @param data
     * @param promisedHash
     * @param atlas
     * @param notifyTarget The draw cell to notify once this model is fully available to render
     */
    public TerrainChunkGenQueueItem(
        TerrainChunkData data,
        String promisedHash,
        VoxelTextureAtlas atlas,
        DrawCell notifyTarget,
        Entity toDelete
    ){
        this.data = data;
        this.promisedHash = promisedHash;
        this.atlas = atlas;
        this.notifyTarget = notifyTarget;
        this.toDelete = toDelete;
    }

    /**
     * Gets the mesh data for the chunk
     * @return the mesh data
     */
    public TerrainChunkData getData(){
        return data;
    }

    /**
     * Gets the hash promised for the chunk
     * @return the hash
     */
    public String getPromisedHash(){
        return this.promisedHash;
    }

    /**
     * Gets the texture atlas assigned to the chunk
     * @return the atlas
     */
    public VoxelTextureAtlas getAtlas(){
        return atlas;
    }

}
