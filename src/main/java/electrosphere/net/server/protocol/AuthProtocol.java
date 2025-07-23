package electrosphere.net.server.protocol;

import electrosphere.auth.AuthenticationManager;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.AuthMessage;
import electrosphere.net.parser.net.message.PlayerMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.server.player.Player;
import electrosphere.net.template.ServerProtocolTemplate;

/**
 * The server protocol for authorization packets
 */
public class AuthProtocol implements ServerProtocolTemplate<AuthMessage> {

    @Override
    public AuthMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, AuthMessage message) {
        switch(message.getMessageSubtype()){
            case AUTHDETAILS: {
                //auth check
                int loginId = Globals.authenticationManager.authenticate(message.getuser(), message.getpass());
                if(loginId != AuthenticationManager.INVALID_LOGIN){
                    //TODO: actually set connection/protocol to authenticated
                    connectionHandler.addMessagetoOutgoingQueue(AuthMessage.constructAuthSuccessMessage());
                    Player newPlayer = new Player(connectionHandler, loginId);
                    Globals.serverState.playerManager.registerPlayer(newPlayer);
                    //there is a race condition here where if a local non-server client connects first then it breaks
                    if(connectionHandler.getIPAddress().contains("127.0.0.1") && EngineState.EngineFlags.RUN_CLIENT == true && Globals.clientState.clientPlayer == null){
                        Globals.clientState.clientPlayer = newPlayer;
                    }
                    connectionHandler.addMessagetoOutgoingQueue(PlayerMessage.constructSet_IDMessage(connectionHandler.getPlayerId()));
                } else {
                    connectionHandler.addMessagetoOutgoingQueue(AuthMessage.constructAuthFailureMessage());
                }
                LoggerInterface.loggerNetworking.INFO("[SERVER] Received auth details");
            } break;
            case AUTHREQUEST:
            case AUTHSUCCESS:
            case AUTHFAILURE: {
                //silently drop
            } break;
        }
        return null;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, AuthMessage message) {
        switch(message.getMessageSubtype()){
            case AUTHDETAILS:
            case AUTHREQUEST:
            case AUTHSUCCESS:
            case AUTHFAILURE:
                //silently drop
                break;
        }
    }

}
