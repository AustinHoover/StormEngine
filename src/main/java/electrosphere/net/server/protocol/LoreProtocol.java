package electrosphere.net.server.protocol;

import java.util.List;

import electrosphere.engine.Globals;
import electrosphere.net.parser.net.message.LoreMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.template.ServerProtocolTemplate;
import electrosphere.util.Utilities;

/**
 * The server protocol for handling lore messages
 */
public class LoreProtocol implements ServerProtocolTemplate<LoreMessage> {

    @Override
    public LoreMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, LoreMessage message) {
        switch(message.getMessageSubtype()){
            case REQUESTRACES: {
                List<String> playableRaces = Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces();
                String returnData = Utilities.stringify(playableRaces);
                connectionHandler.addMessagetoOutgoingQueue(LoreMessage.constructResponseRacesMessage(returnData));
                return null;
            }
            case TEMPORALUPDATE:
            case RESPONSERACES:
                return message;
        }
        return null;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, LoreMessage message) {
        switch(message.getMessageSubtype()){
            case REQUESTRACES:
            case RESPONSERACES:
            case TEMPORALUPDATE:
                //silently ignore
                break;
        }
    }
    
}
