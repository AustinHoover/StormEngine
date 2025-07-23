package electrosphere.client.terrain.editing;

import org.joml.Vector3i;

import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.script.ScriptClientVoxelUtils;
import electrosphere.engine.Globals;

/**
 * Utilities for editing blocks
 */
public class BlockEditing {
    
    /**
     * Edit blocks
     * @param type The type of block
     * @param metadata The metadata of the block
     */
    public static void editBlock(short type, short metadata){
        Vector3i cornerVoxel = ClientWorldData.convertRealToLocalBlockSpace(Globals.cursorState.getBlockCursorPos());
        int blockSize = Globals.cursorState.getBlockSize();
        Vector3i chunkPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(Globals.cursorState.getBlockCursorPos());
        ScriptClientVoxelUtils.clientRequestEditBlock(chunkPos, cornerVoxel, type, metadata, blockSize);
    }

    /**
     * Destroy blocks
     */
    public static void destroyBlock(){
        Vector3i cornerVoxel = ClientWorldData.convertRealToLocalBlockSpace(Globals.cursorState.getBlockCursorPos());
        int blockSize = Globals.cursorState.getBlockSize();
        Vector3i chunkPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(Globals.cursorState.getBlockCursorPos());
        ScriptClientVoxelUtils.clientRequestEditBlock(chunkPos, cornerVoxel, (short)0, (short)0, blockSize);
    }

}
