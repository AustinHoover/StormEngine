package electrosphere.net.client.protocol;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.AuthMessage;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.net.parser.net.message.LoreMessage;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * The client protocol for handling auth messages
 */
public class AuthProtocol implements ClientProtocolTemplate<AuthMessage> {

    @Override
    public AuthMessage handleAsyncMessage(AuthMessage message) {
        switch(message.getMessageSubtype()){
            case AUTHREQUEST: {
                //Try login
                Globals.clientState.clientConnection.queueOutgoingMessage(AuthMessage.constructAuthDetailsMessage(Globals.clientState.clientUsername,Globals.clientState.clientPassword));
                LoggerInterface.loggerNetworking.INFO("[CLIENT] Received auth request");
            } break;
            case AUTHSUCCESS: {
                //clean password hash from memory
                Globals.clientState.clientPassword = "";
                //request playable races
                Globals.clientState.clientConnection.queueOutgoingMessage(LoreMessage.constructRequestRacesMessage());
                //request characters available to this player
                Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructRequestCharacterListMessage());
                //log that we succeeded
                LoggerInterface.loggerNetworking.INFO("[CLIENT] Successfully logged in");
            } break;
            case AUTHFAILURE: {
                //TODO: handle better
                LoggerInterface.loggerNetworking.ERROR("Auth failure",new Exception("Auth failure"));
            } break;
            //ignore stack
            case AUTHDETAILS: {
                //silently ignore
            } break;
        }
        return null;
    }

    @Override
    public void handleSyncMessage(AuthMessage message) {
        switch(message.getMessageSubtype()){
            case AUTHREQUEST:
            case AUTHSUCCESS:
            case AUTHFAILURE:
            case AUTHDETAILS:
                break;
        }
    }

}
