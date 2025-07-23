package electrosphere.net.server.protocol;

import electrosphere.data.crafting.RecipeData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.template.ServerProtocolTemplate;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.player.CraftingActions;
import electrosphere.server.player.PlayerActions;

/**
 * Server protocol for dealing with inventory messages
 */
public class InventoryProtocol implements ServerProtocolTemplate<InventoryMessage> {


    /**
     * the entity's equip inventory
     */
    public static final int INVENTORY_TYPE_EQUIP = 0;

    /**
     * the natural inventory of the entity
     */
    public static final int INVENTORY_TYPE_NATURAL = 1;

    /**
     * the toolbar
     */
    public static final int INVENTORY_TYPE_TOOLBAR = 2;

    @Override
    public InventoryMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, InventoryMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, InventoryMessage message) {
        Entity target;
        switch(message.getMessageSubtype()){
            case ADDITEMTOINVENTORY:
                LoggerInterface.loggerNetworking.DEBUG("[SERVER] ADD ITEM TO INVENTORY " + message.getentityId());
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasNaturalInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
                break;
            case REMOVEITEMFROMINVENTORY:
                LoggerInterface.loggerNetworking.DEBUG("[SERVER] REMOVE ITEM FROM INVENTORY " + message.getentityId());
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasNaturalInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
                break;
            case CLIENTREQUESTEQUIPITEM:
                LoggerInterface.loggerNetworking.DEBUG("[SERVER] REQUEST EQUIP ITEM " + message.getentityId());
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasNaturalInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
                break;
            case CLIENTREQUESTUNEQUIPITEM:
                LoggerInterface.loggerNetworking.DEBUG("[SERVER] REQUEST UNEQUIP ITEM " + message.getentityId());
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasNaturalInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
                break;
            case CLIENTREQUESTPERFORMITEMACTION: {
                PlayerActions.attemptPlayerAction(connectionHandler, message);
            } break;
            case CLIENTREQUESTADDTOOLBAR: {
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasToolbarInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
            } break;
            case CLIENTREQUESTADDNATURAL: {
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasNaturalInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
            } break;
            case CLIENTUPDATETOOLBAR: {
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasToolbarInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
            } break;
            case CLIENTREQUESTCRAFT: {
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                Entity workshopEntity = EntityLookupUtils.getEntityById(message.getstationId());
                RecipeData recipe = Globals.gameConfigCurrent.getRecipeMap().getType(message.getrecipeId());
                if(target != null && recipe != null){
                    CraftingActions.attemptCraft(target,workshopEntity,recipe);
                    // System.out.println(message.getentityId() + " " + message.getstationId() + " " + message.getrecipeId());
                    // InventoryUtils.serverGetInventoryState(target).addNetworkMessage(message);
                }
            } break;
            case CLIENTREQUESTUNWATCHINVENTORY:
            case CLIENTREQUESTWATCHINVENTORY:
            case CLIENTREQUESTSTOREITEM: {
                target = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(target != null && InventoryUtils.hasNaturalInventory(target)){
                    ServerInventoryState.getServerInventoryState(target).addNetworkMessage(message);
                }
            } break;
            case SERVERCOMMANDSTOREITEM:
            case SERVERCOMMANDUNEQUIPITEM:
            case SERVERCOMMANDMOVEITEMCONTAINER:
            case SERVERCOMMANDEQUIPITEM:
            case SERVERUPDATEITEMCHARGES:
                //silently ignore
                break;
        }
    }

}
