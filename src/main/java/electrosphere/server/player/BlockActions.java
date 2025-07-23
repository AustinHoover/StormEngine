package electrosphere.server.player;

import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.data.block.BlockType;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.item.ServerChargeState;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.server.player.Player;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.physics.block.editing.ServerBlockEditing;

/**
 * Actions involving blocks
 */
public class BlockActions {
    
    /**
     * Tries to edit a block
     * @param creature The creature that is performing the edit
     * @param chunkPos The chunk position
     * @param blockPos The block position
     * @param blockType The type of block to edit to
     * @param editSize The size of the edit
     */
    public static void editBlockArea(Entity creature, Vector3i chunkPos, Vector3i blockPos, short blockType, int editSize){
        Realm playerRealm = Globals.serverState.realmManager.getEntityRealm(creature);
        if(ServerToolbarState.hasServerToolbarState(creature)){
            //check that we have the block equipped
            ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(creature);
            Entity equippedItem = serverToolbarState.getRealWorldItem();
            String equippedItemType = CommonEntityUtils.getEntitySubtype(equippedItem);
            BlockType blockTypeData = Globals.gameConfigCurrent.getBlockData().getTypeFromId((int)blockType);
            String goalBlockEntityId = Item.getBlockTypeId(blockTypeData);
            if(CreatureUtils.hasControllerPlayerId(creature)){
                Player player = Globals.serverState.playerManager.getPlayerFromId(CreatureUtils.getControllerPlayerId(creature));
                Globals.serverState.structureScanningService.queue(player, ServerWorldData.convertLocalBlockToRealSpace(chunkPos, blockPos));
            }

            if(equippedItemType.equals(goalBlockEntityId)){
                //place the block
                ServerBlockEditing.editBlockArea(playerRealm, chunkPos, blockPos, blockType, (short)0, editSize);
                if(!Globals.gameConfigCurrent.getCreatureTypeLoader().getType(creature).getId().equals(LoadingUtils.EDITOR_RACE_NAME)){
                    ServerChargeState.attemptRemoveCharges(creature, 1);
                }
            } else if(blockType == BlockChunkData.BLOCK_TYPE_EMPTY){
                ServerBlockEditing.editBlockArea(playerRealm, chunkPos, blockPos, BlockChunkData.BLOCK_TYPE_EMPTY, (short)0, editSize);
            }
        }
    }

    /**
     * Places a fab
     * @param creature The creature placing the fab
     * @param chunkPos The chunk position to place at
     * @param blockPos The block position to place at
     * @param blockRotation The rotation to apply to the fab
     * @param fabPath The path to the fab itself
     */
    public static void placeFab(Entity creature, Vector3i chunkPos, Vector3i blockPos, int blockRotation, String fabPath){
        Realm playerRealm = Globals.serverState.realmManager.getEntityRealm(creature);
        ServerBlockEditing.placeBlockFab(playerRealm, chunkPos, blockPos, blockRotation, fabPath);
        if(!Globals.gameConfigCurrent.getCreatureTypeLoader().getType(creature).getId().equals(LoadingUtils.EDITOR_RACE_NAME)){
            ServerChargeState.attemptRemoveCharges(creature, 1);
        }
        if(CreatureUtils.hasControllerPlayerId(creature)){
            Player player = Globals.serverState.playerManager.getPlayerFromId(CreatureUtils.getControllerPlayerId(creature));
            Globals.serverState.structureScanningService.queue(player, ServerWorldData.convertLocalBlockToRealSpace(chunkPos, blockPos));
        }
    }

}
