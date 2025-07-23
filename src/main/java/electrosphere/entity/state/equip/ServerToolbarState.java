package electrosphere.entity.state.equip;


import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.block.ServerBlockTree;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;

import java.util.List;

import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.creature.block.BlockSystem;
import electrosphere.data.entity.creature.block.BlockVariant;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.creature.equip.ToolbarData;
import electrosphere.data.entity.item.EquipWhitelist;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.server.player.Player;
import electrosphere.net.server.protocol.InventoryProtocol;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;
import electrosphere.server.utils.ServerScriptUtils;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * The server's toolbar state
 */
@SynchronizedBehaviorTree(name = "serverToolbarState", isServer = true, correspondingTree="clientToolbarState")
public class ServerToolbarState implements BehaviorTree {
    
    /**
     * The selected toolbar slot
     */
    @SyncedField
    int selectedSlot;

    /**
     * The parent
     */
    Entity parent;

    /**
     * The toolbar data
     */
    ToolbarData toolbarData;

    /**
     * The real world item
     */
    Entity realWorldItem = null;

    /**
     * Attempts to add item to toolbar
     * @param inInventoryEntity The item to equip
     * @param point The point to equip to
     */
    public void attemptEquip(Entity inInventoryEntity, int slotId){
        if(inInventoryEntity == null){
            throw new Error("Entity is null!");
        }
        if(!ItemUtils.isItem(inInventoryEntity)){
            throw new Error("Entity is not an item!");
        }
        RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(parent);
        ServerEquipState serverEquipState = ServerEquipState.getEquipState(parent);
        if(!toolbarInventory.hasItemInSlot(slotId + "")){
            //remove from equip state
            if(InventoryUtils.hasEquipInventory(parent)){
                RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(parent);
                serverEquipState.serverAttemptUnequip(equipInventory.getItemSlot(inInventoryEntity));
                equipInventory.tryRemoveItem(inInventoryEntity);
            }

            //remove from natural inventory
            if(InventoryUtils.hasNaturalInventory(parent)){
                UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(parent);
                naturalInventory.removeItem(inInventoryEntity);
            }

            //add to toolbar
            toolbarInventory.tryRemoveItem(inInventoryEntity);
            this.unequip(inInventoryEntity);
            toolbarInventory.addItem(slotId + "", inInventoryEntity);
            if(slotId == selectedSlot){
                this.visuallyEquipCurrentSlot();
            }
            //if they're a player, let the player know that the item has moved container
            if(CreatureUtils.hasControllerPlayerId(parent)){
                //get player
                int playerId = CreatureUtils.getControllerPlayerId(parent);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(playerId);
                //tell the player they don't have the item anymore
                NetworkMessage inventoryMessage = InventoryMessage.constructserverCommandMoveItemContainerMessage(
                    inInventoryEntity.getId(), 
                    InventoryProtocol.INVENTORY_TYPE_TOOLBAR,
                    slotId + ""
                );
                controllerPlayer.addMessage(inventoryMessage);
            }
        }
    }

    /**
     * Visually updates what is equipped at the current slot
     */
    public void visuallyEquipCurrentSlot(){
        RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(parent);
        RelationalInventoryState equipInventoryState = InventoryUtils.getEquipInventory(parent);
        Vector3d parentPos = EntityUtils.getPosition(parent);
        Entity inInventoryEntity = toolbarInventory.getItemSlot(selectedSlot + "");
        if(inInventoryEntity != null){
            boolean targetHasWhitelist = ItemUtils.hasEquipList(inInventoryEntity);
            String equipItemClass = ItemUtils.getEquipClass(inInventoryEntity);
            //hydrate inventory item
            String itemType = ItemUtils.getType(inInventoryEntity);
            Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
            realWorldItem = ItemUtils.serverSpawnBasicItem(realm,new Vector3d(parentPos),itemType);
            //bind in world with in inventory
            ItemUtils.setRealWorldEntity(inInventoryEntity, realWorldItem);

            EquipPoint targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getPrimarySlot());
            if(equipItemClass != null && targetPoint.getEquipClassWhitelist() != null && !targetPoint.getEquipClassWhitelist().contains(equipItemClass)){
                targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getCombinedSlot());
            }

            if(targetPoint == null){
                EquipPoint discoveredPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getPrimarySlot());
                String message = 
                "Failed to visually equip item slot!\n" + 
                "Primary Slot: " + toolbarData.getPrimarySlot() + "\n" +
                "Discovered point: " + discoveredPoint.getBone() + "\n" +
                "Item Class Whitelist: " + discoveredPoint.getEquipClassWhitelist() + "\n" +
                "Equipped item class: " + equipItemClass
                ;
                throw new Error(message);
            }
            
            //
            //Visual transforms
            if(targetHasWhitelist){
                //depends on the type of creature
                String parentCreatureId = CreatureUtils.getType(parent);
                List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(realWorldItem);
                for(EquipWhitelist whitelistItem : whitelist){
                    if(whitelistItem.getCreatureId().equals(parentCreatureId)){
                        //put in map
                        String modelName = whitelistItem.getModel();
                        Globals.assetManager.addModelPathToQueue(modelName);
                        //attach to parent bone
                        AttachUtils.serverAttachEntityToEntityAtBone(
                            parent,
                            realWorldItem,
                            targetPoint.getBone(),
                            AttachUtils.getEquipPointVectorOffset(targetPoint.getOffsetVectorThirdPerson()),
                            AttachUtils.getEquipPointRotationOffset(targetPoint.getOffsetRotationThirdPerson())
                        );
                        //make uncollidable
                        if(PhysicsEntityUtils.containsDBody(realWorldItem) && realWorldItem.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                            Realm inWorldRealm = Globals.serverState.realmManager.getEntityRealm(realWorldItem);
                            inWorldRealm.getCollisionEngine().destroyPhysics(realWorldItem);
                        }
                        //make untargetable
                        ServerEntityTagUtils.removeTagFromEntity(realWorldItem, EntityTags.TARGETABLE);
                        break;
                    }
                }
            } else {
                //does not depend on the type of creature
                AttachUtils.serverAttachEntityToEntityAtBone(
                    parent,
                    realWorldItem,
                    targetPoint.getBone(),
                    AttachUtils.getEquipPointVectorOffset(targetPoint.getOffsetVectorThirdPerson()),
                    AttachUtils.getEquipPointRotationOffset(targetPoint.getOffsetRotationThirdPerson())
                );
                if(PhysicsEntityUtils.containsDBody(realWorldItem) && realWorldItem.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                    Realm inWorldRealm = Globals.serverState.realmManager.getEntityRealm(realWorldItem);
                    inWorldRealm.getCollisionEngine().destroyPhysics(realWorldItem);
                }
                ServerEntityTagUtils.removeTagFromEntity(realWorldItem, EntityTags.TARGETABLE);
                GravityUtils.serverAttemptDeactivateGravity(realWorldItem);
            }

            //
            //update block state based on what we have equipped
            this.updateBlockVariant();
            
            //actually switch containers
            boolean parentHasNaturalInventory = InventoryUtils.hasNaturalInventory(parent);
            boolean parentHasEquipInventory = InventoryUtils.hasEquipInventory(parent);
            //make sure the switch is possible
            if(parentHasNaturalInventory){
                UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(parent);
                naturalInventory.removeItem(inInventoryEntity);
            }
            if(parentHasEquipInventory){
                //actually switch containers
                RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(parent);
                equipInventory.tryRemoveItem(inInventoryEntity);
            }
            //get the chunk the equipper is in, and broadcast to that chunk that they equipped the item
            //get datacell
            ServerDataCell dataCell = DataCellSearchUtils.getEntityDataCell(parent);
            //broadcast attach entity
            int equipperId = parent.getId();
            int inWorldItemId = realWorldItem.getId();
            NetworkMessage attachMessage = InventoryMessage.constructserverCommandEquipItemMessage(
                equipperId,
                InventoryProtocol.INVENTORY_TYPE_TOOLBAR,
                this.selectedSlot + "",
                inWorldItemId,
                itemType
            );
            //actually send the packet
            dataCell.broadcastNetworkMessage(attachMessage);

            //Fire signal to script engine to equip
            ServerScriptUtils.fireSignalOnEntity(parent, "equipItem");
        }
    }

    /**
     * Updates the server block variant based on what item is equipped
     */
    private void updateBlockVariant(){
        ServerBlockTree blockTree = ServerBlockTree.getServerBlockTree(parent);
        RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(parent);
        RelationalInventoryState equipInventoryState = InventoryUtils.getEquipInventory(parent);
        Entity selectedItemEntity = toolbarInventory.getItemSlot(this.selectedSlot + "");
        if(blockTree != null && selectedItemEntity != null){

            String equipItemClass = ItemUtils.getEquipClass(selectedItemEntity);
            EquipPoint targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getPrimarySlot());
            if(!targetPoint.getEquipClassWhitelist().contains(equipItemClass)){
                targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getCombinedSlot());
            }

            BlockSystem blockData = blockTree.getBlockSystem();
            if(selectedItemEntity != null && Globals.gameConfigCurrent.getItemMap().getItem(selectedItemEntity) != null){
                BlockVariant blockVariant = blockData.getVariantForPointWithItem(targetPoint.getEquipPointId(),ItemUtils.getEquipClass(selectedItemEntity));

                //TODO: refactor to allow sending more than one variant at a time
                //ie if you have two items equipped and you want to block with both
                if(blockVariant != null){
                    blockTree.setCurrentBlockVariant(blockVariant.getVariantId());
                } else {
                    blockTree.setCurrentBlockVariant("");
                }
            }
        }
    }

    /**
     * The item to unequip
     * @param item The item
     */
    public void unequip(Entity item){
        if(item == this.realWorldItem){
            this.removeVisuals();
        }
        if(ItemUtils.getRealWorldEntity(item) != null && ItemUtils.getRealWorldEntity(item) == this.realWorldItem){
            this.removeVisuals();
        }
    }

    /**
     * Updates the toolbar state based on the inventory (ie, should call this after editing the inventory directly)
     */
    public void update(){
        RelationalInventoryState inventory = InventoryUtils.getToolbarInventory(this.parent);
        Entity inventoryEquipped = inventory.getItemSlot("" + this.selectedSlot);
        if(inventoryEquipped == null && this.realWorldItem != null){
            this.unequip(this.realWorldItem);
        }
    }

    /**
     * Removes visuals for the currently equipped item if they exist
     */
    public void removeVisuals(){
        //destroy in world item
        boolean targetHasWhitelist = ItemUtils.hasEquipList(this.realWorldItem);

        //
        //Visual transforms
        if(targetHasWhitelist){
            if(this.realWorldItem != null){
                ServerEntityUtils.destroyEntity(this.realWorldItem);
            }
            this.realWorldItem = null;
        } else {
            if(this.realWorldItem != null){
                ServerEntityUtils.destroyEntity(this.realWorldItem);
            }
            this.realWorldItem = null;
        }

        //
        //update block state based on what we have equipped
        this.updateBlockVariant();
        //tell all clients to unequip the world item
        //get datacell
        ServerDataCell dataCell = DataCellSearchUtils.getEntityDataCell(parent);
        //broadcast attach entity
        NetworkMessage unequipMessage = InventoryMessage.constructserverCommandUnequipItemMessage(parent.getId(), InventoryProtocol.INVENTORY_TYPE_TOOLBAR, "");
        //actually send the packet
        dataCell.broadcastNetworkMessage(unequipMessage);
    }

    /**
     * Gets the real world item of the toolbar
     * @return The real world item if it exists, null otherwise
     */
    public Entity getRealWorldItem(){
        return realWorldItem;
    }


    /**
     * Gets the in-inventory item
     * @return The item entity if it exists, null otherwise
     */
    public Entity getInInventoryItem(){
        RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(parent);
        return toolbarInventory.getItemSlot(selectedSlot + "");
    }

    /**
     * Attempts to change the selection to a new value
     * @param value The value
     */
    public void attemptChangeSelection(int value){
        while(value < 0){
            value = value + ClientToolbarState.MAX_TOOLBAR_SIZE;
        }
        value = value % ClientToolbarState.MAX_TOOLBAR_SIZE;
        this.removeVisuals();
        this.setSelectedSlot(value);
        this.visuallyEquipCurrentSlot();
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ServerToolbarState attachTree(Entity parent, Object ... params){
        ServerToolbarState rVal = new ServerToolbarState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERTOOLBARSTATE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID);
        return rVal;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Detatches this tree from the entity.
     * </p>
     * @param entity The entity to detach to
     * @param tree The behavior tree to detach
     */
    public static void detachTree(Entity entity, BehaviorTree tree){
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID);
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p> Private constructor to enforce using the attach methods </p>
     * <p>
     * Constructor
     * </p>
     * @param parent The parent entity of this tree
     * @param params Optional parameters that can be provided when attaching the tree. All custom data required for creating this tree should be passed in this varargs.
     */
    private ServerToolbarState(Entity parent, Object ... params){
        this.parent = parent;
        this.toolbarData = (ToolbarData)params[0];
    }

    /**
     * <p>
     * Gets the ServerToolbarState of the entity
     * </p>
     * @param entity the entity
     * @return The ServerToolbarState
     */
    public static ServerToolbarState getServerToolbarState(Entity entity){
        return (ServerToolbarState)entity.getData(EntityDataStrings.TREE_SERVERTOOLBARSTATE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets selectedSlot and handles the synchronization logic for it.
     * </p>
     * @param selectedSlot The value to set selectedSlot to.
     */
    public void setSelectedSlot(int selectedSlot){
        this.selectedSlot = selectedSlot;
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientIntStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID, FieldIdEnums.TREE_SERVERTOOLBARSTATE_SYNCEDFIELD_SELECTEDSLOT_ID, selectedSlot));
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets selectedSlot.
     * </p>
     */
    public int getSelectedSlot(){
        return selectedSlot;
    }

    @Override
    public void simulate(float deltaTime) {
    }

    /**
     * <p>
     * Checks if the entity has a ServerToolbarState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerToolbarState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERTOOLBARSTATE);
    }

}
