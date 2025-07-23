package electrosphere.net.client.protocol;

import electrosphere.engine.Globals;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * The client protocol for handling synchronization messages
 */
public class SynchronizationProtocol implements ClientProtocolTemplate<SynchronizationMessage> {

    @Override
    public SynchronizationMessage handleAsyncMessage(SynchronizationMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(SynchronizationMessage message) {
        switch(message.getMessageSubtype()){
            case CLIENTREQUESTBTREEACTION:
            case UPDATECLIENTSTATE:
            case UPDATECLIENTSTRINGSTATE:
            case UPDATECLIENTDOUBLESTATE:
            case UPDATECLIENTFLOATSTATE:
            case UPDATECLIENTINTSTATE:
            case UPDATECLIENTLONGSTATE:
            case ATTACHTREE:
            case DETATCHTREE:
            case SERVERNOTIFYBTREETRANSITION:
                Globals.clientState.clientSynchronizationManager.pushMessage(message);
            break;
            case LOADSCENE:
                throw new UnsupportedOperationException("Received synchronization message on the client of unsupported type: " + message.getMessageSubtype());
        }
    }

}
