package electrosphere.net.server.protocol;

import electrosphere.net.parser.net.message.CombatMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.template.ServerProtocolTemplate;

/**
 * Server protocol for dealing with combat messages
 */
public class CombatProtocol implements ServerProtocolTemplate<CombatMessage> {

    @Override
    public CombatMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, CombatMessage message) {
        switch(message.getMessageSubtype()){
            case SERVERREPORTHITBOXCOLLISION: {
                //silently ignore
            } break;
        }
        return null;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, CombatMessage message) {
        switch(message.getMessageSubtype()){
            case SERVERREPORTHITBOXCOLLISION: {
                //silently ignore
            } break;
        }
    }
    
}
