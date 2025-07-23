package electrosphere.net.client.protocol;

import org.joml.Vector3d;

import electrosphere.client.collision.ClientNetworkHitboxCollision;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.net.parser.net.message.CombatMessage;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * Client protocl for dealing with combat messages
 */
public class CombatProtocol implements ClientProtocolTemplate<CombatMessage> {

    @Override
    public CombatMessage handleAsyncMessage(CombatMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(CombatMessage message) {
        switch(message.getMessageSubtype()){
            case SERVERREPORTHITBOXCOLLISION: {
                Vector3d position = new Vector3d(message.getpositionX(),message.getpositionY(),message.getpositionZ());
                Entity senderEntity = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityID());
                Entity receiverEntity = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getreceiverEntityID());
                if(senderEntity != null && receiverEntity != null){
                    ClientNetworkHitboxCollision.handleHitboxCollision(senderEntity, receiverEntity, position, message.gethitboxType(), message.gethurtboxType());
                }
            } break;
        }
    }
    
}
