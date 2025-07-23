package electrosphere.net.server.protocol;

import electrosphere.net.parser.net.message.PlayerMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.template.ServerProtocolTemplate;

/**
 * Player protocol handling
 */
public class PlayerProtocol implements ServerProtocolTemplate<PlayerMessage> {
    

    @Override
    public PlayerMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, PlayerMessage message) {
        switch(message.getMessageSubtype()){
            case SETINITIALDISCRETEPOSITION:
            case SET_ID:
            //silently ignore
            break;
        }
        return null;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, PlayerMessage message) {
        switch(message.getMessageSubtype()){
            case SETINITIALDISCRETEPOSITION:
            case SET_ID:
            //silently ignore
            break;
        }
    }

}
