package electrosphere.entity.state.inventory;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3d;

import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.equip.ServerEquipState;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.entity.state.item.ServerChargeState;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;
import electrosphere.net.server.protocol.InventoryProtocol;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.server.utils.ServerScriptUtils;

/**
 * Principally used to handle network messages related to inventory and thread synchronization
 */
public class ServerInventoryState implements BehaviorTree {

    /**
     * The queue of messages to handle
     */
    List<InventoryMessage> networkMessageQueue = new LinkedList<InventoryMessage>();

    /**
     * All entities watching this inventory state
     */
    List<Entity> watchers = new LinkedList<Entity>();

    /**
     * All entities that the parent is watching
     */
    List<Entity> toWatch = new LinkedList<Entity>();

    /**
     * The parent of the state
     */
    Entity parent;

    /**
     * Constructor
     */
    private ServerInventoryState(){ }

    /**
     * Creates an inventory state
     * @param parent The parent entity
     * @return The inventory state
     */
    public static ServerInventoryState serverCreateInventoryState(Entity parent){
        ServerInventoryState rVal = new ServerInventoryState();
        rVal.parent = parent;
        rVal.watchers.add(parent);
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        return rVal;
    }

    @Override
    public void simulate(float deltaTime) {
        for(InventoryMessage message : networkMessageQueue){
            networkMessageQueue.remove(message);
            switch(message.getMessageSubtype()){
                case ADDITEMTOINVENTORY: {
                    Entity itemEnt = EntityLookupUtils.getEntityById(message.getitemEntId());
                    ServerInventoryState.attemptStoreItemAnyInventory(parent,itemEnt);
                } break;
                case REMOVEITEMFROMINVENTORY: {
                    ServerInventoryState.serverAttemptEjectItemTransform(parent, EntityLookupUtils.getEntityById(message.getentityId()));
                } break;
                case CLIENTREQUESTEQUIPITEM: {
                    //item to equip
                    Entity itemEnt = EntityLookupUtils.getEntityById(message.getentityId());
                    ServerInventoryState.attemptStoreItemTransform(parent,itemEnt,InventoryProtocol.INVENTORY_TYPE_EQUIP,message.getequipPointId());
                }
                break;
                case CLIENTREQUESTUNEQUIPITEM: {
                    Entity itemEnt = EntityLookupUtils.getEntityById(message.getentityId());
                    ServerInventoryState.serverAttemptEjectItemTransform(parent, itemEnt);
                } break;
                case CLIENTREQUESTADDNATURAL: {
                    Entity itemEnt = EntityLookupUtils.getEntityById(message.getitemEntId());
                    ServerInventoryState.attemptStoreItemTransform(parent,itemEnt,InventoryProtocol.INVENTORY_TYPE_NATURAL,"");
                } break;
                case CLIENTREQUESTADDTOOLBAR: {
                    Entity itemEnt = EntityLookupUtils.getEntityById(message.getitemEntId());
                    String toolbarSlot = message.gettoolbarId() + "";
                    ServerInventoryState.attemptStoreItemTransform(parent,itemEnt,InventoryProtocol.INVENTORY_TYPE_TOOLBAR,toolbarSlot);
                } break;
                case CLIENTUPDATETOOLBAR: {
                    ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(parent);
                    serverToolbarState.attemptChangeSelection(message.gettoolbarId());
                } break;
                case CLIENTREQUESTSTOREITEM: {
                    Entity itemEnt = EntityLookupUtils.getEntityById(message.getitemEntId());
                    Entity currentParent = ItemUtils.getContainingParent(itemEnt);
                    Entity container = EntityLookupUtils.getEntityById(message.gettargetEntId());
                    //error checking
                    if(itemEnt == null){
                        throw new Error("Failed to locate server entity " + message.getitemEntId());
                    }
                    if(container == null){
                        throw new Error("Failed to locate server entity " + message.gettargetEntId());
                    }
                    //picking up from in the world
                    if(currentParent == null){
                        if(container != this.parent){
                            LoggerInterface.loggerEngine.WARNING("A client is trying to pick up an item for another entity!");
                        } else {
                            ServerInventoryState.attemptStoreItemTransform(parent, itemEnt, message.getcontainerType(), message.getequipPointId());
                        }
                    }
                    //transfering from one container to another
                    if(currentParent != null){
                        ServerInventoryState.attemptStoreItemTransform(container, itemEnt, message.getcontainerType(), message.getequipPointId());
                    }
                } break;
                case CLIENTREQUESTWATCHINVENTORY: {
                    Entity targetEnt = EntityLookupUtils.getEntityById(message.gettargetEntId());
                    if(targetEnt != null){
                        this.watch(targetEnt);
                    }
                } break;
                case CLIENTREQUESTUNWATCHINVENTORY: {
                    Entity targetEnt = EntityLookupUtils.getEntityById(message.gettargetEntId());
                    if(targetEnt != null){
                        this.unwatch(targetEnt);
                    }
                } break;
                case CLIENTREQUESTCRAFT:
                case CLIENTREQUESTPERFORMITEMACTION:
                case SERVERCOMMANDSTOREITEM:
                case SERVERCOMMANDUNEQUIPITEM:
                case SERVERCOMMANDEQUIPITEM:
                case SERVERCOMMANDMOVEITEMCONTAINER:
                case SERVERUPDATEITEMCHARGES:
                break;
            }
        }
    }

    /**
     * Add a network message to be handled on the server
     * @param networkMessage The message
     */
    public void addNetworkMessage(InventoryMessage networkMessage) {
        networkMessageQueue.add(networkMessage);
    }

    /**
     * Tries to watch another entity's inventories
     * @param target The entity that contains the inventories
     */
    public void watch(Entity target){
        if(target == this.parent){
            throw new Error("Trying to watch self!");
        }
        if(!this.toWatch.contains(target)){
            this.toWatch.add(target);
            if(ServerInventoryState.getServerInventoryState(target) != null){
                ServerInventoryState targetState = ServerInventoryState.getServerInventoryState(target);
                if(!targetState.watchers.contains(this.parent)){
                    targetState.watchers.add(this.parent);
                    targetState.sendInventoryState(this.parent);
                }
            }
        }
    }

    /**
     * Tries to watch another entity's inventories
     * @param target The entity that contains the inventories
     */
    public void unwatch(Entity target){
        if(target == this.parent){
            throw new Error("Trying to unwatch self!");
        }
        if(this.toWatch.contains(target)){
            this.toWatch.remove(target);
        }
        if(ServerInventoryState.getServerInventoryState(target) != null){
            ServerInventoryState targetState = ServerInventoryState.getServerInventoryState(target);
            if(!targetState.watchers.contains(this.parent)){
                targetState.watchers.remove(this.parent);
            }
        }
    }

    /**
     * Sets the state of this inventory to a given entity
     */
    public void sendInventoryState(Entity playerEntity){
        //don't send if the target doesn't also have a player
        if(!CreatureUtils.hasControllerPlayerId(playerEntity)){
            return;
        }
        int playerId = CreatureUtils.getControllerPlayerId(playerEntity);
        Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(playerId);
        if(InventoryUtils.hasNaturalInventory(this.parent)){
            UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(this.parent);
            for(Entity itemEnt : naturalInventory.getItems()){
                controllerPlayer.addMessage(InventoryMessage.constructaddItemToInventoryMessage(this.parent.getId(), itemEnt.getId(), ItemUtils.getType(itemEnt)));
            }
        }
    }

    /**
     * Destroys this entity's inventory state
     */
    public void destroy(){
        for(Entity target : this.toWatch){
            if(ServerInventoryState.getServerInventoryState(target) != null){
                ServerInventoryState targetState = ServerInventoryState.getServerInventoryState(target);
                targetState.watchers.remove(this.parent);
                targetState.toWatch.remove(this.parent);
            }
        }
        for(Entity target : this.watchers){
            if(ServerInventoryState.getServerInventoryState(target) != null){
                ServerInventoryState targetState = ServerInventoryState.getServerInventoryState(target);
                targetState.watchers.remove(this.parent);
                targetState.toWatch.remove(this.parent);
            }
        }
    }

    /**
     * Gets the list of all entities watching this inventory state
     * @return the list of all entities watching this inventory state
     */
    public List<Entity> getWatchers(){
        return this.watchers;
    }

    /**
     * Perform the entity transforms to actually store an item in an inventory, if server this has the side effect of also sending packets on success
     * @param creature The creature to store the item in
     * @param item The item to store
     * @return The in-inventory item
     */
    public static Entity attemptStoreItemAnyInventory(Entity creature, Entity item){
        if(item == null){
            throw new Error("Null item provided! " + item);
        }
        if(!ItemUtils.isItem(item)){
            throw new Error("Item is not an item!");
        }
        if(!InventoryUtils.hasNaturalInventory(creature)){
            throw new Error("Creature does not have a natural inventory");
        }
        if(ItemUtils.itemIsInInventory(item)){
            throw new Error("Item is already in an inventory!");
        }
        Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(item);
        //get inventory
        //for the moment we're just gonna get natural inventory
        //later we'll need to search through all creature inventories to find the item
        UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(creature);

        //check if it should be added to an existing stack
        Entity foundExisting = null;
        if(itemData.getMaxStack() != null){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(creature);
            if(toolbarInventory != null){
                for(Entity toolbarItem : toolbarInventory.getItems()){
                    if(toolbarItem == null){
                        continue;
                    }
                    Item toolbarData = Globals.gameConfigCurrent.getItemMap().getItem(toolbarItem);
                    if(!toolbarData.getId().equals(itemData.getId())){
                        continue;
                    }
                    ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(toolbarItem);
                    if(serverChargeState.getCharges() >= itemData.getMaxStack()){
                        continue;
                    }
                    foundExisting = toolbarItem;
                    break;
                }
            }
            if(foundExisting == null){
                for(Entity naturalItem : inventory.getItems()){
                    if(naturalItem == null){
                        continue;
                    }
                    Item toolbarData = Globals.gameConfigCurrent.getItemMap().getItem(naturalItem);
                    if(!toolbarData.getId().equals(itemData.getId())){
                        continue;
                    }
                    ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(naturalItem);
                    if(serverChargeState.getCharges() >= itemData.getMaxStack()){
                        continue;
                    }
                    foundExisting = naturalItem;
                    break;
                }
            }
        }

        //increase charges
        if(foundExisting != null){
            ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(foundExisting);
            serverChargeState.setCharges(serverChargeState.getCharges() + 1);
            //if we are the server, immediately send required packets
            ServerDataCell dataCell = DataCellSearchUtils.getEntityDataCell(item);
            dataCell.broadcastNetworkMessage(EntityMessage.constructDestroyMessage(item.getId()));

            //tell entities watching this inventory that their item has another charge
            ServerInventoryState serverInventoryState = ServerInventoryState.getServerInventoryState(creature);
            for(Entity watcher : serverInventoryState.getWatchers()){
                if(CreatureUtils.hasControllerPlayerId(watcher)){
                    //get the player
                    int controllerPlayerID = CreatureUtils.getControllerPlayerId(watcher);
                    Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(controllerPlayerID);
                    //send message
                    controllerPlayer.addMessage(InventoryMessage.constructserverUpdateItemChargesMessage(foundExisting.getId(), serverChargeState.getCharges()));
                }
            }

            //alert script engine
            ServerScriptUtils.fireSignalOnEntity(creature, "itemPickup", item.getId(), foundExisting.getId());
            //destroy the item that was left over
            ServerEntityUtils.destroyEntity(item);
            return foundExisting;
        }

        //destroy in-world entity and create in-inventory item
        //we're doing this so that we're not constantly sending networking messages for invisible entities attached to the player
        Entity inventoryItem = ItemUtils.serverRecreateContainerItem(item, creature);
        //store item in inventory
        inventory.addItem(inventoryItem);
        //if we are the server, immediately send required packets
        ServerDataCell dataCell = DataCellSearchUtils.getEntityDataCell(item);
        //broadcast destroy entityq
        dataCell.broadcastNetworkMessage(EntityMessage.constructDestroyMessage(item.getId()));

        //tell entities watching this inventory that they have an item in their inventory
        ServerInventoryState serverInventoryState = ServerInventoryState.getServerInventoryState(creature);
        for(Entity watcher : serverInventoryState.getWatchers()){
            if(CreatureUtils.hasControllerPlayerId(watcher)){
                //get the player
                int controllerPlayerID = CreatureUtils.getControllerPlayerId(watcher);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(controllerPlayerID);
                //send message
                controllerPlayer.addMessage(InventoryMessage.constructaddItemToInventoryMessage(creature.getId(), inventoryItem.getId(), ItemUtils.getType(inventoryItem)));
            }
        }

        //alert script engine
        ServerScriptUtils.fireSignalOnEntity(creature, "itemPickup", item.getId(), inventoryItem.getId());
        //destroy the item that was left over
        ServerEntityUtils.destroyEntity(item);
        return inventoryItem;
    }

    /**
     * Tries to add an item to a given inventory on a given container
     * @param container The container to store the item in
     * @param item The item to store
     * @param containerType The type of container to store into
     * @param slotId (Optional) The slot within the container to store into
     * @return The in-inventory item
     */
    public static void attemptStoreItemTransform(Entity container, Entity itemEnt, int containerType, String slotId){
        if(itemEnt == null){
            throw new Error("Null item provided! " + itemEnt);
        }
        if(!ItemUtils.isItem(itemEnt)){
            throw new Error("Item is not an item!");
        }
        if(containerType != InventoryProtocol.INVENTORY_TYPE_EQUIP && containerType != InventoryProtocol.INVENTORY_TYPE_NATURAL && containerType != InventoryProtocol.INVENTORY_TYPE_TOOLBAR){
            throw new Error("Invalid container type! " + containerType);
        }

        //check if should remove from existing container
        if(ItemUtils.getContainingParent(itemEnt) != null && ItemUtils.getContainingParent(itemEnt) != container){
            Entity existingContainer = ItemUtils.getContainingParent(itemEnt);
            ServerInventoryState.serverRemoveItemFromInventories(existingContainer,itemEnt);
        }

        //set containing parent
        ItemUtils.setContainingParent(itemEnt, container);

        //apply transforms
        if(containerType == InventoryProtocol.INVENTORY_TYPE_EQUIP){
            ServerEquipState equipState = ServerEquipState.getEquipState(container);
            EquipPoint point = equipState.getEquipPoint(slotId);
            equipState.commandAttemptEquip(itemEnt, point);
        } else if(containerType == InventoryProtocol.INVENTORY_TYPE_NATURAL){
            ServerInventoryState.serverAddToNatural(container, itemEnt);
        } else if(containerType == InventoryProtocol.INVENTORY_TYPE_TOOLBAR){
            ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(container);
            serverToolbarState.attemptEquip(itemEnt, Integer.parseInt(slotId));
        }
    }

    /**
     * Perform the entity transforms to actually store an item in an inventory, if server this has the side effect of also sending packets on success
     * @param creature The creature to store the item in
     * @param item The item to store
     * @return The in-inventory item
     */
    public static Entity serverAddToNatural(Entity creature, Entity item){
        if(!ItemUtils.isItem(item)){
            return null;
        }
        if(!InventoryUtils.hasNaturalInventory(creature)){
            return null;
        }
        //get inventory
        //for the moment we're just gonna get natural inventory
        //later we'll need to search through all creature inventories to find the item
        UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(creature);
        //destroy in-world entity and create in-inventory item
        //we're doing this so that we're not constantly sending networking messages for invisible entities attached to the player
        Entity inventoryItem = item;
        if(!ItemUtils.itemIsInInventory(item)){
            inventoryItem = ItemUtils.serverRecreateContainerItem(item, creature);
            ServerEntityUtils.destroyEntity(item);
        }
        if(InventoryUtils.hasEquipInventory(creature) && InventoryUtils.getEquipInventory(creature).containsItem(item)){
            InventoryUtils.getEquipInventory(creature).tryRemoveItem(item);
        }
        if(InventoryUtils.hasToolbarInventory(creature) && InventoryUtils.getToolbarInventory(creature).containsItem(item)){
            if(ServerToolbarState.getServerToolbarState(creature) != null){
                ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(creature);
                serverToolbarState.unequip(item);
            }
            InventoryUtils.getToolbarInventory(creature).tryRemoveItem(item);
        }
        //store item in inventory
        inventory.addItem(inventoryItem);
        //set item containing parent
        ItemUtils.setContainingParent(inventoryItem, creature);

        //tell entities watching this inventory that they have an item in their inventory
        ServerInventoryState serverInventoryState = ServerInventoryState.getServerInventoryState(creature);
        for(Entity watcher : serverInventoryState.getWatchers()){
            if(CreatureUtils.hasControllerPlayerId(watcher)){
                //get the player
                int controllerPlayerID = CreatureUtils.getControllerPlayerId(watcher);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(controllerPlayerID);
                //send message
                controllerPlayer.addMessage(InventoryMessage.constructserverCommandStoreItemMessage(
                    creature.getId(),
                    inventoryItem.getId(),
                    ItemUtils.getType(inventoryItem),
                    InventoryProtocol.INVENTORY_TYPE_NATURAL,
                    ""
                ));
            }
        }

        //alert script engine
        ServerScriptUtils.fireSignalOnEntity(creature, "itemAddToNatural", item.getId(), inventoryItem.getId());
        return inventoryItem;
    }

    /**
     * Attempts the transform to eject an item from an inventory, if this is the server it has added side effect of sending packets on success
     * @param creature The creature to eject the item from
     * @param item The item to eject
     */
    public static void serverAttemptEjectItemTransform(Entity creature, Entity item){
        if(creature == null){
            throw new Error("Provided null creature!");
        }
        if(item == null){
            throw new Error("Provided null item!");
        }
        if(!ItemUtils.isItem(item)){
            return;
        }
        if(!ItemUtils.itemIsInInventory(item)){
            return;
        }
        if(InventoryUtils.hasNaturalInventory(creature)){
            //get inventory
            UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(creature);
            //remove item from inventory
            inventory.removeItem(item);
        }
        if(InventoryUtils.hasEquipInventory(creature)){
            //get inventory
            RelationalInventoryState inventory = InventoryUtils.getEquipInventory(creature);
            //get inventory slot
            String inventorySlot = inventory.getItemSlot(item);
            //remove item from inventory
            if(inventory.tryRemoveItem(item) != null){
                //get real world item
                Entity realWorldItem = ItemUtils.getRealWorldEntity(item);
                if(realWorldItem != null){
                    //Tell players to unequip the item
                    Realm realm = Globals.serverState.realmManager.getEntityRealm(realWorldItem);
                    if(realm != null){
                        //get closest chunk
                        ServerDataCell dataCell = Globals.serverState.entityDataCellMapper.getEntityDataCell(realWorldItem);
                        //broadcast destroy item
                        NetworkMessage unequipMessage = InventoryMessage.constructserverCommandUnequipItemMessage(creature.getId(), InventoryProtocol.INVENTORY_TYPE_EQUIP, inventorySlot);
                        dataCell.broadcastNetworkMessage(unequipMessage);
                    }
                    //drop item
                    ServerEquipState equipState = ServerEquipState.getEquipState(creature);
                    equipState.serverTransformUnequipPoint(inventorySlot);
                }
            }
        }
        ServerInventoryState.serverRemoveItemFromInventories(creature, item);
        //get parent realm
        Realm realm = Globals.serverState.realmManager.getEntityRealm(creature);
        //find "in front of creature"
        Vector3d dropSpot = new Vector3d(EntityUtils.getPosition(creature));
        if(CreatureUtils.getFacingVector(creature) != null){
            dropSpot.add(CreatureUtils.getFacingVector(creature));
        }
        //
        //tell player that the item is no longer in their inventory
        ServerInventoryState serverInventoryState = ServerInventoryState.getServerInventoryState(creature);
        for(Entity watcher : serverInventoryState.getWatchers()){
            if(CreatureUtils.hasControllerPlayerId(watcher)){
                //get player
                int playerId = CreatureUtils.getControllerPlayerId(watcher);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(playerId);
                //tell the player to destroy the item
                controllerPlayer.addMessage(EntityMessage.constructDestroyMessage(item.getId()));
            }
        }
        //
        //compose item into in-world entity
        Entity inWorldItem = ItemUtils.serverSpawnBasicItem(realm,dropSpot,ItemUtils.getType(item));
        //destroy the entity on server side
        ServerEntityUtils.destroyEntity(item);
        //activate gravity
        GravityUtils.serverAttemptActivateGravity(inWorldItem);
    }

    /**
     * [SERVER ONLY] Called when the server says to remove an item from all inventories
     * Only does the remove, doesn't create the in-world item
     * @param creature The creature to remove the item from (likely to be player entity)
     * @param item The item to remove
     */
    public static void serverRemoveItemFromInventories(Entity creature, Entity item){
        if(creature == null){
            throw new Error("Creature is null!");
        }
        if(item == null){
            throw new Error("Item is null!");
        }
        if(!ItemUtils.isItem(item)){
            throw new Error("Item is not an item!");
        }
        if(!ItemUtils.itemIsInInventory(item)){
            throw new Error("Item is not in an inventory!");
        }
        //check if the item is in an inventory
        if(InventoryUtils.hasNaturalInventory(creature)){
            //get inventory
            UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(creature);
            //remove item from inventory
            inventory.removeItem(item);
        }
        if(InventoryUtils.hasEquipInventory(creature)){
            //get inventory
            RelationalInventoryState inventory = InventoryUtils.getEquipInventory(creature);
            //get real world item
            Entity realWorldItem = ItemUtils.getRealWorldEntity(item);
            if(realWorldItem != null){
                //drop item
                ServerEquipState equipState = ServerEquipState.getEquipState(creature);
                equipState.serverTransformUnequipPoint(inventory.getItemSlot(item));
            }
            //remove item from inventory
            inventory.tryRemoveItem(item);
        }
        if(InventoryUtils.hasToolbarInventory(creature)){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(creature);
            toolbarInventory.tryRemoveItem(item);
            Globals.cursorState.hintClearBlockCursor();
            ServerToolbarState.getServerToolbarState(creature).update();
        }
        //
        //tell player that the item is no longer in their inventory
        ServerInventoryState serverInventoryState = ServerInventoryState.getServerInventoryState(creature);
        for(Entity watcher : serverInventoryState.getWatchers()){
            if(CreatureUtils.hasControllerPlayerId(watcher)){
                //get player
                int playerId = CreatureUtils.getControllerPlayerId(watcher);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(playerId);
                //tell the player they don't have the item anymore
                controllerPlayer.addMessage(InventoryMessage.constructremoveItemFromInventoryMessage(item.getId()));
            }
        }
    }

    /**
     * Destroys an item that is in an inventory
     * @param item The item
     */
    public static void serverDestroyInventoryItem(Entity item){
        Entity creature = ItemUtils.getContainingParent(item);
        if(creature == null){
            throw new Error("Creature is null!");
        }
        if(item == null){
            throw new Error("Item is null!");
        }
        if(!CreatureUtils.isCreature(creature)){
            throw new Error("Creature is not a creature!");
        }
        if(!ItemUtils.isItem(item)){
            throw new Error("Item is not an item!");
        }
        if(!ItemUtils.itemIsInInventory(item)){
            throw new Error("Item is not in an inventory!");
        }
        //check if the item is in an inventory
        if(InventoryUtils.hasNaturalInventory(creature)){
            //get inventory
            UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(creature);
            //remove item from inventory
            inventory.removeItem(item);
        }
        if(InventoryUtils.hasEquipInventory(creature)){
            //get inventory
            RelationalInventoryState inventory = InventoryUtils.getEquipInventory(creature);
            //get real world item
            Entity realWorldItem = ItemUtils.getRealWorldEntity(item);
            if(realWorldItem != null){
                //drop item
                ServerEquipState equipState = ServerEquipState.getEquipState(creature);
                equipState.serverTransformUnequipPoint(inventory.getItemSlot(item));
            }
            //remove item from inventory
            inventory.tryRemoveItem(item);
        }
        if(InventoryUtils.hasToolbarInventory(creature)){
            RelationalInventoryState inventory = InventoryUtils.getToolbarInventory(creature);
            inventory.tryRemoveItem(item);
            if(ServerToolbarState.hasServerToolbarState(creature)){
                ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(creature);
                serverToolbarState.update();
            }
        }
        //
        //tell player that the item is no longer in their inventory
        ServerInventoryState serverInventoryState = ServerInventoryState.getServerInventoryState(creature);
        for(Entity watcher : serverInventoryState.getWatchers()){
            if(CreatureUtils.hasControllerPlayerId(watcher)){
                //get player
                int playerId = CreatureUtils.getControllerPlayerId(watcher);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(playerId);
                //tell the player they don't have the item anymore
                controllerPlayer.addMessage(InventoryMessage.constructremoveItemFromInventoryMessage(item.getId()));
            }
        }
        ServerEntityUtils.destroyEntity(item);
    }

    /**
     * Creates an item in the creature's inventory
     * @param creature The creature
     * @param itemId The item's ID
     */
    public static void serverCreateInventoryItem(Entity creature, String itemId, int count){
        Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(itemId);
        if(itemData.getMaxStack() == null){
            for(int i = 0; i < count; i++){
                ItemUtils.serverCreateContainerItem(creature, itemData);
            }
        } else {
            //scan for items to add charges to first
            int added = 0;
            List<Entity> existingItems = InventoryUtils.getAllInventoryItems(creature);
            for(Entity existingItem : existingItems){
                Item existingData = Globals.gameConfigCurrent.getItemMap().getItem(existingItem);
                if(existingData.getId().equals(itemId)){
                    ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(existingItem);
                    if(serverChargeState.getCharges() < existingData.getMaxStack()){
                        int available = existingData.getMaxStack() - serverChargeState.getCharges();
                        //just need to add charges to this
                        if(available >= count - added){
                            serverChargeState.attemptAddCharges(count - added);
                            added = count;
                            break;
                        } else {
                            serverChargeState.attemptAddCharges(available);
                            added = added + available;
                        }
                    }
                }
            }
            //need to start creating items to add more charges
            Item targetData = Globals.gameConfigCurrent.getItemMap().getItem(itemId);
            if(added < count){
                int numFullItemsToAdd = (count - added) / targetData.getMaxStack();
                int remainder = (count - added) % targetData.getMaxStack();
                for(int i = 0; i < numFullItemsToAdd; i++){
                    Entity newInventoryItem = ItemUtils.serverCreateContainerItem(creature, itemData);
                    ServerChargeState.getServerChargeState(newInventoryItem).setCharges(targetData.getMaxStack());
                }
                if(remainder > 0){
                    Entity newInventoryItem = ItemUtils.serverCreateContainerItem(creature, itemData);
                    ServerChargeState.getServerChargeState(newInventoryItem).setCharges(remainder);
                }
            }
        }
    }

    /**
     * Checks if the enity has a given type of tool
     * @param entity The entity
     * @param toolType The type of tool
     * @return true if it has the type of tool, false otherwise
     */
    public static boolean serverHasTool(Entity entity, String toolType){
        List<Entity> items = InventoryUtils.getAllInventoryItems(entity);
        for(Entity itemEnt : items){
            Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(itemEnt);
            if(itemData.getTokens().contains(toolType)){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the current inventory state
     * @param target the entity to get inventory state from
     * @return The inventory state behavior tree or null
     */
    public static ServerInventoryState getServerInventoryState(Entity target){
        if(!target.containsKey(EntityDataStrings.SERVER_INVENTORY_STATE)){
            return null;
        }
        return (ServerInventoryState)target.getData(EntityDataStrings.SERVER_INVENTORY_STATE);
    }

    /**
     * Sets the current inventory state
     * @param target The entity to attach inventory state to
     * @param state The inventory state to attach
     */
    public static void setServerInventoryState(Entity target, ServerInventoryState state){
        target.putData(EntityDataStrings.SERVER_INVENTORY_STATE, state);
    }
    
}
