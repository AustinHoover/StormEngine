package electrosphere.net.client.protocol;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.ServerMessage;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * The client protocol for handling server messages
 */
public class ServerProtocol implements ClientProtocolTemplate<ServerMessage> {

    @Override
    public ServerMessage handleAsyncMessage(ServerMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(ServerMessage message) {
        switch(message.getMessageSubtype()){
            case PING:
                Globals.clientState.clientConnection.queueOutgoingMessage(ServerMessage.constructPongMessage());
                break;
            case PONG:
                //let the networking loop know we received a pong message
                Globals.clientState.clientConnection.markReceivedPongMessage();
                break;
            case DISCONNECT: {
                LoggerInterface.loggerNetworking.WARNING("Server sent signal to disconnect!");
            } break;
        }
    }

}
