package electrosphere.server.macro.utils;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.data.block.BlockType;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import electrosphere.server.macro.structure.VirtualStructure;

/**
 * Utilities for repairing a structure
 */
public class StructureRepairUtils {
    
    /**
     * Solves for the next position in the structure's fab that can be repaired
     * @param realm The realm the structure is within
     * @param struct The structure
     * @return The next position that can be repaired if it exists, null otherwise
     */
    public static Vector3i getRepairablePosition(Realm realm, VirtualStructure struct){
        //error checking
        if(!(realm.getDataCellManager() instanceof GriddedDataCellManager)){
            throw new Error("Realm is not a gridded realm!");
        }

        BlockFab fab = struct.getFab();
        Vector3d structStartPos = new Vector3d(struct.getAABB().minX,struct.getAABB().minY,struct.getAABB().minZ);
        GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)realm.getDataCellManager();
        for(int x = 0; x < fab.getDimensions().x; x++){
            for(int y = 0; y < fab.getDimensions().y; y++){
                for(int z = 0; z < fab.getDimensions().z; z++){
                    Vector3d offsetPos = new Vector3d(structStartPos).add(
                        x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                    );
                    Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(offsetPos);
                    Vector3i blockPos = ServerWorldData.convertRealToLocalBlockSpace(offsetPos);
                    BlockChunkData blockChunkData = griddedDataCellManager.getBlocksAtPosition(chunkPos);
                    short existingBlockType = blockChunkData.getType(blockPos.x, blockPos.y, blockPos.z);
                    short desiredType = fab.getType(x, y, z);
                    if(existingBlockType != desiredType){
                        return new Vector3i(x,y,z);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the item id of the material to use for the next repair to perform on a structure
     * @param realm The realm
     * @param struct The structure
     * @return The id of the item to use to repair
     */
    public static String getNextRepairMat(Realm realm, VirtualStructure struct){
        Vector3i repairPos = StructureRepairUtils.getRepairablePosition(realm, struct);

        if(repairPos == null){
            return null;
        }

        //get the id of item entity type for the block we need
        BlockFab fab = struct.getFab();
        short blockTypeId = fab.getType(repairPos.x, repairPos.y, repairPos.z);
        BlockType blockType = Globals.gameConfigCurrent.getBlockData().getTypeFromId(blockTypeId);
        String itemId = Item.getBlockTypeId(blockType);

        return itemId;
    }

    /**
     * Solves for whether the structure can be repaired or not
     * @param realm The realm the structure is within
     * @param struct The structure
     */
    public static void updateRepairableStatus(Realm realm, VirtualStructure struct){
        //error checking
        if(!(realm.getDataCellManager() instanceof GriddedDataCellManager)){
            throw new Error("Realm is not a gridded realm!");
        }

        //assume it's not repairable, then check if this is true
        struct.setRepairable(false);

        BlockFab fab = struct.getFab();
        Vector3d structStartPos = new Vector3d(struct.getAABB().minX,struct.getAABB().minY,struct.getAABB().minZ);
        GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)realm.getDataCellManager();
        for(int x = 0; x < fab.getDimensions().x; x++){
            for(int y = 0; y < fab.getDimensions().y; y++){
                for(int z = 0; z < fab.getDimensions().z; z++){
                    Vector3d offsetPos = new Vector3d(structStartPos).add(x,y,z);
                    Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(offsetPos);
                    Vector3i blockPos = ServerWorldData.convertRealToLocalBlockSpace(offsetPos);
                    BlockChunkData blockChunkData = griddedDataCellManager.getBlocksAtPosition(chunkPos);
                    if(blockChunkData.getType(blockPos.x, blockPos.y, blockPos.z) != fab.getType(x, y, z)){
                        struct.setRepairable(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Validates the repairability status of the structure
     * @param realm The realm the structure is within
     * @param struct The structure
     * @return true if the structure is actaully repairable, false otherwise
     */
    public static boolean validateRepairable(Realm realm, VirtualStructure struct){
        //error checking
        if(!(realm.getDataCellManager() instanceof GriddedDataCellManager)){
            throw new Error("Realm is not a gridded realm!");
        }

        BlockFab fab = struct.getFab();
        Vector3d structStartPos = new Vector3d(struct.getAABB().minX,struct.getAABB().minY,struct.getAABB().minZ);
        GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)realm.getDataCellManager();
        for(int x = 0; x < fab.getDimensions().x; x++){
            for(int y = 0; y < fab.getDimensions().y; y++){
                for(int z = 0; z < fab.getDimensions().z; z++){
                    Vector3d offsetPos = new Vector3d(structStartPos).add(
                        x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                    );
                    Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(offsetPos);
                    Vector3i blockPos = ServerWorldData.convertRealToLocalBlockSpace(offsetPos);

                    //check existing blocks
                    BlockChunkData blockChunkData = griddedDataCellManager.getBlocksAtPosition(chunkPos);
                    if(blockChunkData != null){
                        short existingBlockType = blockChunkData.getType(blockPos.x, blockPos.y, blockPos.z);
                        short desiredType = fab.getType(x, y, z);
                        if(existingBlockType != desiredType){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
