package electrosphere.server.player;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.interact.ItemActions;
import electrosphere.data.entity.common.interact.InteractionData;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.block.BlockVariant;
import electrosphere.data.entity.item.Item;
import electrosphere.data.entity.item.ItemUsage;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.block.ServerBlockTree;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.furniture.ServerDoorState;
import electrosphere.entity.state.item.ServerChargeState;
import electrosphere.entity.state.life.ServerLifeTree;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.physics.block.editing.ServerBlockEditing;
import electrosphere.server.physics.terrain.editing.TerrainEditing;
import electrosphere.server.utils.ServerScriptUtils;

/**
 * Class for handling 
 */
public class PlayerActions {

    /**
     * Attempts to perform an action a player requested
     * @param connectionHandler The player's connection handler
     * @param message The network message that encapsulates the requested action
     */
    public static void attemptPlayerAction(ServerConnectionHandler connectionHandler, InventoryMessage message){
        Entity playerEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());

        if(message.getitemActionCode() == ItemActions.ITEM_ACTION_CODE_SECONDARY){
            int itemActionCodeState = message.getitemActionCodeState();
            ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(playerEntity);
            if(serverToolbarState != null && serverToolbarState.getRealWorldItem() != null){
                Entity realWorldItemEnt = serverToolbarState.getRealWorldItem();
                Entity inventoryItemEnt = serverToolbarState.getInInventoryItem();
                Item item = Globals.gameConfigCurrent.getItemMap().getItem(realWorldItemEnt);
                CreatureData creatureData = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(CreatureUtils.getType(playerEntity));
                ServerBlockTree serverBlockTree = ServerBlockTree.getServerBlockTree(playerEntity);

                //check block status
                boolean shouldBlock = false;
                if(creatureData.getBlockSystem() != null && creatureData.getBlockSystem().getAllVariants() != null && serverBlockTree != null){
                    for(BlockVariant variant : creatureData.getBlockSystem().getAllVariants()){
                        if(variant.getVariantId().equals(serverBlockTree.getCurrentBlockVariant())){
                            shouldBlock = true;
                            break;
                        }
                    }
                }

                //actually perform actions
                if(shouldBlock){
                    PlayerActions.block(playerEntity, message);
                } else if(item.getSecondaryUsage() != null){
                    if(
                        item.getSecondaryUsage().getOnlyOnMouseDown() == null ||
                        (itemActionCodeState == ItemActions.ITEM_ACTION_CODE_STATE_ON && item.getSecondaryUsage().getOnlyOnMouseDown())
                    ){
                        PlayerActions.secondaryUsage(playerEntity, inventoryItemEnt, item, message);
                    }
                }
            }
        }
    }

    /**
     * Attempts to block
     */
    private static void block(Entity playerEntity, InventoryMessage message){
        ServerBlockTree serverBlockTree = ServerBlockTree.getServerBlockTree(playerEntity);
        if(serverBlockTree != null){
            if(message.getitemActionCodeState() == ItemActions.ITEM_ACTION_CODE_STATE_ON){
                serverBlockTree.start();
            } else {
                serverBlockTree.stop();
            }
        }
    }

    /**
     * Performs various secondary usages
     * @param playerEntity The player's entity
     * @param itemEnt The item entity
     * @param item The item data
     * @param message The message
     */
    private static void secondaryUsage(Entity playerEntity, Entity itemEnt, Item item, InventoryMessage message){
        ItemUsage secondaryUsage = item.getSecondaryUsage();
        Realm playerRealm = Globals.serverState.realmManager.getEntityRealm(playerEntity);

        //entity spawning
        if(secondaryUsage.getSpawnEntityId() != null){
            Vector3d spawnPos = new Vector3d(message.getviewTargetX(),message.getviewTargetY(),message.getviewTargetZ());
            CommonEntityUtils.serverSpawnBasicObject(playerRealm, spawnPos, secondaryUsage.getSpawnEntityId());
            ServerChargeState.getServerChargeState(itemEnt).attemptAddCharges(-1);
        }

        //block editing
        if(secondaryUsage.getBlockId() != null){
            //clamp the placement pos to the block grid..
            Vector3d placementPos = new Vector3d(message.getviewTargetX(),message.getviewTargetY(),message.getviewTargetZ());
            Vector3i worldPos = ServerWorldData.convertRealToWorldSpace(placementPos);
            Vector3i blockPos = ServerWorldData.convertRealToLocalBlockSpace(placementPos);


            //actually edit
            ServerBlockEditing.editBlockChunk(playerRealm, worldPos, blockPos, (short)(int)secondaryUsage.getBlockId(), (short)0);
            LoggerInterface.loggerEngine.DEBUG("Place block type " + secondaryUsage.getBlockId() + " at " + placementPos + " -> " + worldPos + " " + blockPos);
        }

        //voxel editing
        if(secondaryUsage.getVoxelId() != null){
            Vector3d placementPos = new Vector3d(message.getviewTargetX(),message.getviewTargetY(),message.getviewTargetZ());
            //actually edit
            TerrainEditing.editTerrain(playerRealm, placementPos, TerrainEditing.DEFAULT_MAGNITUDE, secondaryUsage.getVoxelId(), TerrainEditing.DEFAULT_WEIGHT);
            ServerChargeState.getServerChargeState(itemEnt).attemptAddCharges(-1);
            LoggerInterface.loggerEngine.DEBUG("Place voxel type " + secondaryUsage.getVoxelId() + " at " + placementPos);
        }
    }


    /**
     * Attempts to perform an action a player requested
     * @param connectionHandler The player's connection handler
     * @param target The target if the interaction
     * @param signal The type of interaction
     */
    public static void attemptInteraction(ServerConnectionHandler connectionHandler, Entity target, String signal){
        Entity playerEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
        switch(signal){
            case InteractionData.ON_INTERACT_HARVEST: {
                PlayerActions.harvest(playerEntity, target);
            } break;
            case InteractionData.ON_INTERACT_DOOR: {
                if(ServerDoorState.hasServerDoorState(target)){
                    ServerDoorState serverDoorState = ServerDoorState.getServerDoorState(target);
                    serverDoorState.interact();
                }
                ServerScriptUtils.fireSignalOnEntity(playerEntity, "entityInteractHarvest", target);
            } break;
            default: {
                throw new Error("Unsupported signal received! " + signal);
            }
        }
    }

    /**
     * Tries to harvest the target as the creature
     * @param creature The creature
     * @param target The target to harvest
     */
    public static void harvest(Entity creature, Entity target){
        if(ServerLifeTree.hasServerLifeTree(target)){
            ServerLifeTree serverLifeTree = ServerLifeTree.getServerLifeTree(target);
            serverLifeTree.kill();
        }
        ServerScriptUtils.fireSignalOnEntity(creature, "entityInteractHarvest", target);
    }
    
}
