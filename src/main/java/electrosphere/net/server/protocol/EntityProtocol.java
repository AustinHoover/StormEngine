package electrosphere.net.server.protocol;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.server.ServerPlayerViewDirTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.template.ServerProtocolTemplate;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.player.PlayerActions;

/**
 * The server protocol for handling entity messages
 */
public class EntityProtocol implements ServerProtocolTemplate<EntityMessage> {

    @Override
    public EntityMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, EntityMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, EntityMessage message) {
        //error check
        if(Globals.clientState.clientScene != null && Globals.clientState.clientScene.getEntityFromId(message.getentityID()) != null){
            LoggerInterface.loggerNetworking.WARNING("Server received packet for entity that is in client scene wrapper!");
        }

        //parse message
        Entity targetEntity;
        switch(message.getMessageSubtype()){
            case MOVEUPDATE:
                targetEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(targetEntity != null){
                    CreatureUtils.serverAttachEntityMessageToMovementTree(targetEntity,message);
                    connectionHandler.getNetworkParser().release(message);
                }
                break;
            case ATTACKUPDATE:
                targetEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(targetEntity != null){
                    ServerAttackTree.getServerAttackTree(targetEntity).addNetworkMessage(message);
                }
                break;
            case STARTATTACK: {
                targetEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(targetEntity != null){
                    ServerAttackTree.getServerAttackTree(targetEntity).addNetworkMessage(message);
                }
            } break;
            case UPDATEENTITYVIEWDIR: {
                targetEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                if(targetEntity != null && ServerPlayerViewDirTree.hasTree(targetEntity)){
                    ServerPlayerViewDirTree.getTree(targetEntity).setPlayerViewDir(message.getpropertyType(), message.getyaw(),message.getpitch(),message.gettime());
                    connectionHandler.getNetworkParser().release(message);
                }
            } break;
            case INTERACT: {
                targetEntity = EntityLookupUtils.getEntityById(message.getentityID());
                PlayerActions.attemptInteraction(connectionHandler, targetEntity, message.getinteractionSignal());
            } break;
            //ignore stack
            case KILL:
            case DESTROY:
            case CREATE:
            case ATTACHENTITYTOENTITY:
            case SETPROPERTY:
            case SYNCPHYSICS:
                //silently ignore
                break;
        }
    }
    
}
