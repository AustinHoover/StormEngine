package electrosphere.server.physics.block.editing;

import java.io.File;

import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3i;
import org.joml.Vector4f;

import electrosphere.client.block.BlockChunkData;
import electrosphere.controls.cursor.CursorState;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.interfaces.VoxelCellManager;

/**
 * Provides utilities for editing block (particularly brushes, etc)
 */
public class ServerBlockEditing {

    /**
     * The minimum value before hard setting to 0
     */
    static final float MINIMUM_FULL_VALUE = 0.01f;
    
    /**
     * Performs a block chunk edit. Basically has a sphere around the provided position that it attempts to add value to
     * @param realm The realm to modify in
     * @param worldPos The world position
     * @param voxelPos The block position within the chunk at the world position
     * @param type The new type of block
     * @param metadata The new metadata for the block
     */
    public static void editBlockChunk(Realm realm, Vector3i worldPos, Vector3i voxelPos, short type, short metadata){
        if(realm != null && realm.getDataCellManager() instanceof VoxelCellManager){
            VoxelCellManager voxelCellManager = (VoxelCellManager) realm.getDataCellManager();

            BlockChunkData data;
            if(
                voxelPos.x < BlockChunkData.CHUNK_DATA_WIDTH &&
                voxelPos.y < BlockChunkData.CHUNK_DATA_WIDTH &&
                voxelPos.z < BlockChunkData.CHUNK_DATA_WIDTH &&
                voxelPos.x >= 0 &&
                voxelPos.y >= 0 &&
                voxelPos.z >= 0 &&
                (data = voxelCellManager.getBlocksAtPosition(worldPos)) != null
            ){
                data.setType(voxelPos.x, voxelPos.y, voxelPos.z, type);
                voxelCellManager.editBlock(worldPos, voxelPos, type, metadata);
            }
        }
    }

    /**
     * Performs a series of block edits on an area of blocks. Basically has a sphere around the provided position that it attempts to add value to
     * @param realm The realm to modify in
     * @param worldPos The world position
     * @param voxelPos The block position within the chunk at the world position
     * @param type The new type of block
     * @param metadata The new metadata for the block
     * @param size The size of the area to edit
     */
    public static void editBlockArea(Realm realm, Vector3i worldPos, Vector3i voxelPos, short type, short metadata, int size){
        if(size < CursorState.MIN_BLOCK_SIZE || size > CursorState.MAX_BLOCK_SIZE){
            throw new Error("Size out of bounds: " + size);
        }
        Vector3i pos = new Vector3i();
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                for(int z = 0; z < size; z++){
                    pos = new Vector3i(voxelPos).add(x,y,z);
                    ServerBlockEditing.editBlockChunk(realm, worldPos, pos, type, metadata);
                }
            }
        }
    }

    /**
     * Places a block fab
     * @param realm The realm
     * @param chunkPos The chunk position
     * @param voxelPos The voxel position
     * @param rotation The rotation of the fab
     * @param fabPath The fab
     */
    public static void placeBlockFab(Realm realm, Vector3i chunkPos, Vector3i voxelPos, int rotation, String fabPath){
        BlockFab fab = BlockFab.read(new File(fabPath));
        Vector3i dims = fab.getDimensions();
        Vector3i currChunkPos = new Vector3i();
        Vector3i currVoxelPos = new Vector3i();
        VoxelCellManager voxelCellManager = (VoxelCellManager) realm.getDataCellManager();
        Quaterniond rotationQuatd = CursorState.getBlockRotation(rotation);
        Quaternionf rotationQuatf = new Quaternionf((float)rotationQuatd.x,(float)rotationQuatd.y,(float)rotationQuatd.z,(float)rotationQuatd.w);
        Matrix4f rotMat = new Matrix4f().rotate(rotationQuatf);
        Vector4f rotationHolder = new Vector4f(1,1,1,1);
        rotMat.transform(rotationHolder);

        //if the cursor is not the default rotation, we want to actually place blocks one position LOWER than the cursor depending on the rotation
        //this is because we want the final vertex to end on the block (ie we must place at final vertex-1)
        Vector3i offsetFromRot = new Vector3i(
            Math.round(Math.min(0,rotationHolder.x)),
            Math.round(Math.min(0,rotationHolder.y)),
            Math.round(Math.min(0,rotationHolder.z))
        );
        for(int x = 0; x < dims.x; x++){
            for(int y = 0; y < dims.y; y++){
                for(int z = 0; z < dims.z; z++){

                    rotationHolder.set(x,y,z,1);
                    rotMat.transform(rotationHolder);
                    currVoxelPos.set(voxelPos).add(Math.round(rotationHolder.x),Math.round(rotationHolder.y),Math.round(rotationHolder.z)).add(offsetFromRot);


                    currChunkPos.set(chunkPos).add(
                        currVoxelPos.x / BlockChunkData.CHUNK_DATA_WIDTH,
                        currVoxelPos.y / BlockChunkData.CHUNK_DATA_WIDTH,
                        currVoxelPos.z / BlockChunkData.CHUNK_DATA_WIDTH
                    );
                    currVoxelPos.set(
                        currVoxelPos.x % BlockChunkData.CHUNK_DATA_WIDTH,
                        currVoxelPos.y % BlockChunkData.CHUNK_DATA_WIDTH,
                        currVoxelPos.z % BlockChunkData.CHUNK_DATA_WIDTH
                    );
                    voxelCellManager.editBlock(currChunkPos, currVoxelPos, fab.getType(x, y, z), (short)0);
                }
            }
        }
    }

}
