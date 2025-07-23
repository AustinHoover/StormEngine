package electrosphere.entity.state.inventory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.item.ClientChargeState;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.protocol.InventoryProtocol;

/**
 * Principally used to handle network messages related to inventory and thread synchronization
 */
public class ClientInventoryState implements BehaviorTree {

    /**
     * The queue of messages to handle
     */
    CopyOnWriteArrayList<InventoryMessage> networkMessageQueue = new CopyOnWriteArrayList<InventoryMessage>();

    /**
     * Parent entity of this inventory state
     */
    Entity parent;

    /**
     * Constructor
     */
    private ClientInventoryState(){ }

    /**
     * Creates a client inventory state
     * @param parent The parent entity
     * @return The client's inventory state
     */
    public static ClientInventoryState clientCreateInventoryState(Entity parent){
        ClientInventoryState rVal = new ClientInventoryState();
        rVal.parent = parent;
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        return rVal;
    }

    @Override
    public void simulate(float deltaTime) {
        List<InventoryMessage> bouncedMessages = new LinkedList<InventoryMessage>();
        for(InventoryMessage message : networkMessageQueue){
            networkMessageQueue.remove(message);
            switch(message.getMessageSubtype()){
                case SERVERCOMMANDSTOREITEM: {
                    Entity clientSideContainer = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.gettargetEntId());
                    Entity clientSideItem = null;
                    if(Globals.clientState.clientSceneWrapper.containsServerId(message.getitemEntId())){
                        LoggerInterface.loggerNetworking.DEBUG("Client move item " + message.getitemEntId());
                        clientSideItem = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getitemEntId());
                    } else {
                        LoggerInterface.loggerNetworking.DEBUG("Client create item " + message.getitemEntId());
                        clientSideItem = ClientInventoryState.clientConstructInInventoryItem(clientSideContainer,message.getitemTemplate());
                        Globals.clientState.clientSceneWrapper.mapIdToId(clientSideItem.getId(), message.getitemEntId());
                    }
                    ClientInventoryState.moveItem(clientSideContainer, clientSideItem, message.getcontainerType(), message.getequipPointId());
                    //attempt re-render ui
                    WindowUtils.attemptRedrawInventoryWindows();
                } break;
                case ADDITEMTOINVENTORY: {
                    LoggerInterface.loggerNetworking.DEBUG("Client create item " + message.getitemEntId());
                    Entity clientSideContainer = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.gettargetEntId());
                    //the ID we get is of the in-inventory item
                    Entity inInventorySpawnedItem = ClientInventoryState.clientConstructInInventoryItem(clientSideContainer,message.getitemTemplate());
                    //map id
                    if(inInventorySpawnedItem != null){
                        Globals.clientState.clientSceneWrapper.mapIdToId(inInventorySpawnedItem.getId(), message.getitemEntId());
                    }
                    //attempt re-render ui
                    WindowUtils.attemptRedrawInventoryWindows();
                } break;
                case REMOVEITEMFROMINVENTORY: {
                    LoggerInterface.loggerNetworking.DEBUG("Client remove item from inventories " + message.getentityId());
                    Entity item = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityId());
                    if(item != null){
                        ClientInventoryState.clientRemoveItemFromInventories(item);
                        //attempt re-render ui
                        WindowUtils.attemptRedrawInventoryWindows();
                    }
                } break;
                case SERVERCOMMANDMOVEITEMCONTAINER: {
                    LoggerInterface.loggerNetworking.DEBUG("Client move item container " + message.getentityId());
                    //this is a command to switch an item from one inventory to another (ie equip->natural or vice-versa)
                    Entity itemEnt = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityId());
                    ClientInventoryState.moveItem(this.parent, itemEnt, message.getcontainerType(), message.getequipPointId());
                    //once we've switched the items around, redraw the inventory to reflect the updated contents
                    WindowUtils.attemptRedrawInventoryWindows();
                } break;
                case SERVERCOMMANDUNEQUIPITEM: {
                    LoggerInterface.loggerNetworking.DEBUG("Client unequip item " + message.getequipPointId());
                    switch(message.getcontainerType()){
                        case InventoryProtocol.INVENTORY_TYPE_NATURAL: {
                            throw new UnsupportedOperationException("unsupported!");
                        }
                        case InventoryProtocol.INVENTORY_TYPE_EQUIP: {
                            if(ClientEquipState.hasEquipState(parent)){
                                //unequip the item
                                ClientEquipState equipState = ClientEquipState.getEquipState(parent);
                                Entity entityInSlot = equipState.getEquippedItemAtPoint(message.getequipPointId());
                                equipState.clientTransformUnequipPoint(message.getequipPointId());
                                //destroy the in-world manifestation of said item
                                ClientEntityUtils.destroyEntity(entityInSlot);
                            }
                        } break;
                        case InventoryProtocol.INVENTORY_TYPE_TOOLBAR: {
                            if(ClientToolbarState.getClientToolbarState(parent) != null){
                                ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(parent);
                                clientToolbarState.unequip();
                            }
                        } break;
                    }
                } break;
                case SERVERCOMMANDEQUIPITEM: {
                    LoggerInterface.loggerNetworking.DEBUG("Client equip item " + message.getentityId());
                    //translate equipper id
                    Entity equipper = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getequipperId());
                    //spawn in world id
                    Entity inWorldEntity = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityId());
                    if(inWorldEntity != null){
                        //translate id
                        Globals.clientState.clientSceneWrapper.mapIdToId(inWorldEntity.getId(), message.getentityId());
                        switch(message.getcontainerType()){
                            case InventoryProtocol.INVENTORY_TYPE_NATURAL: {
                                throw new UnsupportedOperationException("unsupported!");
                            }
                            case InventoryProtocol.INVENTORY_TYPE_EQUIP: {
                                //grab equip state
                                ClientEquipState equipState = ClientEquipState.getEquipState(equipper);
                                //create entity from template in message
                                //get equippoint
                                String equipPointName = message.getequipPointId();
                                EquipPoint equipPoint = equipState.getEquipPoint(equipPointName);
                                //attach
                                equipState.attemptEquip(inWorldEntity, equipPoint);
                            } break;
                            case InventoryProtocol.INVENTORY_TYPE_TOOLBAR: {
                                //grab toolbar state
                                ClientToolbarState toolbarState = ClientToolbarState.getClientToolbarState(equipper);
                                //attach
                                toolbarState.attemptEquip(inWorldEntity);
                            } break;
                        }
                    } else {
                        bouncedMessages.add(message);
                    }
                } break;
                case SERVERUPDATEITEMCHARGES: {
                    LoggerInterface.loggerNetworking.DEBUG("Client set item charges " + message.getentityId());
                    Entity clientInventoryItem = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityId());
                    ClientChargeState clientChargeState = ClientChargeState.getClientChargeState(clientInventoryItem);
                    clientChargeState.setCharges(message.getcharges());
                } break;
                case CLIENTREQUESTSTOREITEM:
                case CLIENTREQUESTCRAFT:
                case CLIENTUPDATETOOLBAR:
                case CLIENTREQUESTADDNATURAL:
                case CLIENTREQUESTADDTOOLBAR:
                case CLIENTREQUESTEQUIPITEM:
                case CLIENTREQUESTUNEQUIPITEM:
                case CLIENTREQUESTPERFORMITEMACTION:
                case CLIENTREQUESTWATCHINVENTORY:
                case CLIENTREQUESTUNWATCHINVENTORY:
                break;
            }
        }
        this.networkMessageQueue.addAll(bouncedMessages);
    }

    /**
     * Moves an item into an inventory on a container
     * @param containerEnt The container
     * @param itemEnt The item
     * @param targetContainer The type of container (natural, toolbar, etc)
     * @param targetSlot (Optional) The slot within the container
     */
    private static void moveItem(Entity containerEnt, Entity itemEnt, int targetContainer, String targetSlot){
        //
        //remove from existing container if relevant
        if(ItemUtils.getContainingParent(itemEnt) != null){
            Entity existingContainerEnt = ItemUtils.getContainingParent(itemEnt);
            if(InventoryUtils.hasNaturalInventory(existingContainerEnt)){
                UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(existingContainerEnt);
                naturalInventory.removeItem(itemEnt);
            }
            if(InventoryUtils.hasToolbarInventory(existingContainerEnt)){
                RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(existingContainerEnt);
                Entity removed = toolbarInventory.tryRemoveItem(itemEnt);
                if(removed != null){
                    if(ClientToolbarState.hasClientToolbarState(containerEnt)){
                        ClientToolbarState toolbarState = ClientToolbarState.getClientToolbarState(containerEnt);
                        toolbarState.update();
                    }
                }
            }
            if(InventoryUtils.hasEquipInventory(existingContainerEnt)){
                RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(existingContainerEnt);
                if(ClientEquipState.hasClientEquipState(existingContainerEnt)){
                    ClientEquipState clientEquipState = ClientEquipState.getClientEquipState(existingContainerEnt);
                    clientEquipState.clientTransformUnequip(itemEnt);
                }
                equipInventory.tryRemoveItem(itemEnt);
            }
        }
        //
        //remove from inventories on existing container
        if(InventoryUtils.hasNaturalInventory(containerEnt)){
            UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(containerEnt);
            naturalInventory.removeItem(itemEnt);
        }
        if(InventoryUtils.hasEquipInventory(containerEnt)){
            RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(containerEnt);
            equipInventory.tryRemoveItem(itemEnt);
        }
        if(ClientEquipState.getClientEquipState(containerEnt) != null){
            ClientEquipState clientEquipState = ClientEquipState.getClientEquipState(containerEnt);
            clientEquipState.clientTransformUnequip(itemEnt);
        }
        if(InventoryUtils.hasToolbarInventory(containerEnt)){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(containerEnt);
            Entity removed = toolbarInventory.tryRemoveItem(itemEnt);
            if(removed != null){
                if(ClientToolbarState.hasClientToolbarState(containerEnt)){
                    ClientToolbarState toolbarState = ClientToolbarState.getClientToolbarState(containerEnt);
                    toolbarState.update();
                }
            }
        }
        //
        //store in new container
        switch(targetContainer){
            case InventoryProtocol.INVENTORY_TYPE_EQUIP: {
                String equipPointId = targetSlot;
                if(InventoryUtils.hasEquipInventory(containerEnt)){
                    RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(containerEnt);
                    equipInventory.addItem(equipPointId, itemEnt);
                }
                if(ClientEquipState.getClientEquipState(containerEnt) != null){
                    ClientEquipState clientEquipState = ClientEquipState.getClientEquipState(containerEnt);
                    clientEquipState.attemptEquip(itemEnt, clientEquipState.getEquipPoint(equipPointId));
                }
            } break;
            case InventoryProtocol.INVENTORY_TYPE_NATURAL: {
                if(InventoryUtils.hasNaturalInventory(containerEnt)){
                    UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(containerEnt);
                    naturalInventory.addItem(itemEnt);
                }
            } break;
            case InventoryProtocol.INVENTORY_TYPE_TOOLBAR: {
                if(InventoryUtils.hasToolbarInventory(containerEnt)){
                    RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(containerEnt);
                    toolbarInventory.tryRemoveItem(itemEnt);
                    toolbarInventory.addItem(targetSlot, itemEnt);
                }
                if(ClientToolbarState.hasClientToolbarState(containerEnt)){
                    ClientToolbarState toolbarState = ClientToolbarState.getClientToolbarState(containerEnt);
                    toolbarState.update();
                }
            }
        }
        //
        //set containing parent
        ItemUtils.setContainingParent(itemEnt, containerEnt);
    }

    /**
     * Adds a network message to the client inventory state
     * @param networkMessage The message
     */
    public void addNetworkMessage(InventoryMessage networkMessage) {
        networkMessageQueue.add(networkMessage);
    }

    /**
     * Asks the server to add the item to the player's natural inventory
     * @param item The item to store
     */
    public static void clientAddToNatural(Entity item){
        //tell the server we want to try the transform
        NetworkMessage requestPickupMessage = InventoryMessage.constructclientRequestAddNaturalMessage(
            Globals.clientState.clientSceneWrapper.mapClientToServerId(item.getId())
        );
        Globals.clientState.clientConnection.queueOutgoingMessage(requestPickupMessage);
    }

    /**
     * Attempts to store the in-world item entity in a creature inventory container
     * @param creature the creature which has a natural inventory
     * @param item the in-world item entity to store
     */
    public static void clientAttemptStoreItem(Entity creature, Entity item){
        //tell the server we want to try the transform
        NetworkMessage requestPickupMessage = InventoryMessage.constructaddItemToInventoryMessage(
            Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId()),
            Globals.clientState.clientSceneWrapper.mapClientToServerId(item.getId()),
            ItemUtils.getType(item)
        );
        Globals.clientState.clientConnection.queueOutgoingMessage(requestPickupMessage);
    }

    /**
     * Places an item of provided type in the parent container's natural inventory
     * @param parentContainer The entity (typically a creature) which will receive the item in their natural inventory
     * @param type The type of item to place in the inventory
     * @return The in-inventory item entity
     */
    public static Entity clientConstructInInventoryItem(Entity parentContainer, String type){
        if(!InventoryUtils.hasNaturalInventory(parentContainer)){
            return null;
        }
        //if we pass sanity checks, actually perform transform
        //get inventory
        //for the moment we're just gonna get natural inventory
        //later we'll need to search through all creature inventories to find the item
        UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(parentContainer);
        //create item
        //TODO: optimize by directly creating the container item instead of first spawning regular item
        LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Spawning temporary in-world item before placing into inventory");
        Entity spawnedItem = ItemUtils.clientSpawnBasicItem(type);
        //convert to in-inventory
        Entity inventoryItem = ItemUtils.clientRecreateContainerItem(spawnedItem, parentContainer);
        //destroy the item that was left over
        ClientEntityUtils.destroyEntity(spawnedItem);
        //store item in inventory
        inventory.addItem(inventoryItem);
        //set item containing parent
        ItemUtils.setContainingParent(inventoryItem, parentContainer);
        //return
        return inventoryItem;
    }

    /**
     * Attempts ejecting an item from a client's inventory
     * need creature so we can figure out where to drop the item
     * @param creature The creature
     * @param item The item
     */
    public static void clientAttemptEjectItem(Entity creature, Entity item){
        //if we're the client, tell the server we want to try the transform
        NetworkMessage requestPickupMessage = InventoryMessage.constructremoveItemFromInventoryMessage(Globals.clientState.clientSceneWrapper.mapClientToServerId(item.getId()));
        Globals.clientState.clientConnection.queueOutgoingMessage(requestPickupMessage);
        if(Globals.audioEngine != null){
            Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(item));
            if(itemData != null && itemData.getItemAudio() != null && itemData.getItemAudio().getUIReleaseAudio() != null){
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(itemData.getItemAudio().getUIReleaseAudio(), VirtualAudioSourceType.UI, false);
            } else {
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_ITEM_RELEASE, VirtualAudioSourceType.UI, false);
            }
        }
    }

    /**
     * [CLIENT ONLY] Called when the server says to remove an item from all inventories
     * Only does the remove, doesn't create the in-world item
     * @param item The item to remove
     */
    public static void clientRemoveItemFromInventories(Entity item){
        if(item == null){
            throw new Error("Item is null!");
        }
        if(!ItemUtils.isItem(item)){
            throw new Error("Item is not an item!");
        }
        if(!ItemUtils.itemIsInInventory(item)){
            throw new Error("Item is not in an inventory!");
        }
        if(ItemUtils.getContainingParent(item) == null){
            throw new Error("Trying to remove non-container item from inventories");
        }
        Entity container = ItemUtils.getContainingParent(item);
        //check if the item is in an inventory
        if(InventoryUtils.hasNaturalInventory(container)){
            //get inventory
            UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(container);
            //remove item from inventory
            inventory.removeItem(item);
        }
        if(InventoryUtils.hasEquipInventory(container)){
            //get inventory
            RelationalInventoryState inventory = InventoryUtils.getEquipInventory(container);
            //get real world item
            Entity realWorldItem = ItemUtils.getRealWorldEntity(item);
            if(realWorldItem != null){
                //drop item
                ClientEquipState equipState = ClientEquipState.getEquipState(container);
                equipState.clientTransformUnequipPoint(inventory.getItemSlot(item));
            }
            //remove item from inventory
            inventory.tryRemoveItem(item);
        }
        if(InventoryUtils.hasToolbarInventory(container)){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(container);
            toolbarInventory.tryRemoveItem(item);
            Globals.cursorState.hintClearBlockCursor();
            ClientToolbarState.getClientToolbarState(container).update();
        }
    }

    /**
     * Gets the current inventory state
     * @param target the entity to get inventory state from
     * @return The inventory state behavior tree or null
     */
    public static ClientInventoryState getClientInventoryState(Entity target){
        if(!target.containsKey(EntityDataStrings.CLIENT_INVENTORY_STATE)){
            return null;
        }
        return (ClientInventoryState)target.getData(EntityDataStrings.CLIENT_INVENTORY_STATE);
    }

    /**
     * Sets the current inventory state
     * @param target The entity to attach inventory state to
     * @param state The inventory state to attach
     */
    public static void setClientInventoryState(Entity target, ClientInventoryState state){
        target.putData(EntityDataStrings.CLIENT_INVENTORY_STATE, state);
    }
    
}
