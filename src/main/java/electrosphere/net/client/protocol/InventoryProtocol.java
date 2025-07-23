package electrosphere.net.client.protocol;

import electrosphere.engine.Globals;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * The client protocol for handling inventory messages
 */
public class InventoryProtocol implements ClientProtocolTemplate<InventoryMessage> {

    @Override
    public InventoryMessage handleAsyncMessage(InventoryMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(InventoryMessage message) {
        switch(message.getMessageSubtype()){
            case ADDITEMTOINVENTORY:
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] ADD ITEM TO INVENTORY " + message.getentityId());
                if(Globals.clientState.playerEntity != null){
                    ClientInventoryState inventoryState;
                    if((inventoryState = ClientInventoryState.getClientInventoryState(Globals.clientState.playerEntity))!=null){
                        inventoryState.addNetworkMessage(message);
                    }
                }
                break;
            case SERVERCOMMANDEQUIPITEM: {
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] EQUIP ITEM " + message.getentityId());
                if(Globals.clientState.playerEntity != null){
                    ClientInventoryState inventoryState;
                    if((inventoryState = ClientInventoryState.getClientInventoryState(Globals.clientState.playerEntity))!=null){
                        inventoryState.addNetworkMessage(message);
                    }
                }
            } break;
            case REMOVEITEMFROMINVENTORY:
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] REMOVE ITEM FROM INVENTORY " + message.getentityId());
                if(Globals.clientState.playerEntity != null){
                    ClientInventoryState inventoryState;
                    if((inventoryState = ClientInventoryState.getClientInventoryState(Globals.clientState.playerEntity))!=null){
                        inventoryState.addNetworkMessage(message);
                    }
                }
                break;
            case SERVERCOMMANDMOVEITEMCONTAINER:
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] MOVE ITEM INVENTORY " + message.getentityId());
                if(Globals.clientState.playerEntity != null){
                    ClientInventoryState inventoryState;
                    if((inventoryState = ClientInventoryState.getClientInventoryState(Globals.clientState.playerEntity))!=null){
                        inventoryState.addNetworkMessage(message);
                    }
                }
                break;
            case SERVERCOMMANDUNEQUIPITEM: {
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] UNEQUIP ITEM " + message.getentityId());
                if(Globals.clientState.playerEntity != null){
                    ClientInventoryState inventoryState;
                    if((inventoryState = ClientInventoryState.getClientInventoryState(Globals.clientState.playerEntity))!=null){
                        inventoryState.addNetworkMessage(message);
                    }
                }
            } break;
            case SERVERUPDATEITEMCHARGES: {
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] SET CHARGES OF " + message.getentityId());
                if(Globals.clientState.playerEntity != null){
                    ClientInventoryState inventoryState;
                    if((inventoryState = ClientInventoryState.getClientInventoryState(Globals.clientState.playerEntity))!=null){
                        inventoryState.addNetworkMessage(message);
                    }
                }
            } break;
            case SERVERCOMMANDSTOREITEM: {
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] STORE " + message.getitemEntId());
                if(Globals.clientState.playerEntity != null){
                    ClientInventoryState inventoryState;
                    if((inventoryState = ClientInventoryState.getClientInventoryState(Globals.clientState.playerEntity))!=null){
                        inventoryState.addNetworkMessage(message);
                    }
                }
            } break;
            case CLIENTREQUESTUNWATCHINVENTORY:
            case CLIENTREQUESTWATCHINVENTORY:
            case CLIENTREQUESTSTOREITEM:
            case CLIENTREQUESTCRAFT:
            case CLIENTUPDATETOOLBAR:
            case CLIENTREQUESTADDNATURAL:
            case CLIENTREQUESTADDTOOLBAR:
            case CLIENTREQUESTPERFORMITEMACTION:
            case CLIENTREQUESTUNEQUIPITEM:
            case CLIENTREQUESTEQUIPITEM:
                //silently ignore
                break;
        }
    }
}
