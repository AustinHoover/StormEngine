package electrosphere.net.server.protocol;

import electrosphere.net.parser.net.message.ServerMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.template.ServerProtocolTemplate;

/**
 * The server's protocol for handling a message
 */
public class ServerProtocol implements ServerProtocolTemplate<ServerMessage> {
    
    /**
     * the connection handler associated with this protocol object
     */
    ServerConnectionHandler connectionHandler;
    

    @Override
    public ServerMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, ServerMessage message) {
        switch(message.getMessageSubtype()){
            case PING: {
                connectionHandler.addMessagetoOutgoingQueue(ServerMessage.constructPongMessage());
            } break;
            case PONG: {
                connectionHandler.markReceivedPongMessage();
            } break;
            case DISCONNECT:
                //silently ignore
                break;
        }
        return null;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, ServerMessage message) {
        switch(message.getMessageSubtype()){
            case DISCONNECT:
            case PING:
            case PONG:
                //silently ignore
                break;
        }
    }

}
