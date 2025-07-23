package electrosphere.net.server.protocol;

import electrosphere.engine.Globals;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.template.ServerProtocolTemplate;

/**
 * Server protocol for synchronization packet handling
 */
public class SynchronizationProtocol implements ServerProtocolTemplate<SynchronizationMessage> {

    @Override
    public SynchronizationMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, SynchronizationMessage message) {
        switch(message.getMessageSubtype()){
            case CLIENTREQUESTBTREEACTION:
                return message;
            case UPDATECLIENTSTATE:
            case UPDATECLIENTSTRINGSTATE:
            case UPDATECLIENTDOUBLESTATE:
            case UPDATECLIENTFLOATSTATE:
            case UPDATECLIENTINTSTATE:
            case UPDATECLIENTLONGSTATE:
            case ATTACHTREE:
            case DETATCHTREE:
            case LOADSCENE:
            case SERVERNOTIFYBTREETRANSITION:
                //silently ignore
            break;
        }
        return null;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, SynchronizationMessage message) {
        switch(message.getMessageSubtype()){
            case CLIENTREQUESTBTREEACTION:{
                Globals.serverState.serverSynchronizationManager.pushMessage(message);
            } break;
            case UPDATECLIENTSTATE:
            case UPDATECLIENTSTRINGSTATE:
            case UPDATECLIENTDOUBLESTATE:
            case UPDATECLIENTFLOATSTATE:
            case UPDATECLIENTINTSTATE:
            case UPDATECLIENTLONGSTATE:
            case ATTACHTREE:
            case DETATCHTREE:
            case LOADSCENE:
            case SERVERNOTIFYBTREETRANSITION:
                //silently ignore
            break;
        }
    }

}
