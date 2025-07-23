package electrosphere.net.client.protocol;

import org.joml.Vector3i;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.PlayerMessage;
import electrosphere.net.server.player.Player;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * The client protocol for handling player messages
 */
public class PlayerProtocol implements ClientProtocolTemplate<PlayerMessage> {

    @Override
    public PlayerMessage handleAsyncMessage(PlayerMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(PlayerMessage message) {
        Globals.profiler.beginCpuSample("PlayerProtocol.handlePlayerMessage");
        switch(message.getMessageSubtype()){
            case SET_ID:
                Globals.clientState.clientPlayer = new Player(message.getplayerID(), Player.CLIENT_DB_ID);
                LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Player ID is " + Globals.clientState.clientPlayer.getId());
                break;
            case SETINITIALDISCRETEPOSITION: {
                Globals.clientState.clientPlayerData.setWorldPos(new Vector3i(message.getinitialDiscretePositionX(), message.getinitialDiscretePositionY(), message.getinitialDiscretePositionZ()));
            } break;
        }
        Globals.profiler.endCpuSample();
    }

}
