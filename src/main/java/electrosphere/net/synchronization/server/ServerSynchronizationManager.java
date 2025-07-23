package electrosphere.net.synchronization.server;


import electrosphere.entity.state.stance.ServerStanceComponent;
import electrosphere.entity.state.movement.sprint.ServerSprintTree;
import electrosphere.logger.LoggerInterface;

import java.util.LinkedList;
import java.util.List;

import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.entity.state.movement.walk.ServerWalkTree;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.EntityLookupUtils;

/**
 * Server service to handle synchronization packets from client (principally, requesting to start btrees)
 */
public class ServerSynchronizationManager {
    
    /**
     * Signal to start the specified tree
     */
    public static final int SERVER_SYNC_START = 0;

    /**
     * Signal to interrupt the specified tree
     */
    public static final int SERVER_SYNC_INTERRUPT = 1;


    /**
     * The list of messages to loop through
     */
    private List<SynchronizationMessage> messages = new LinkedList<SynchronizationMessage>();

    /**
     * Pushes a message into the queue to be processed
     * @param message The message
     */
    public void pushMessage(SynchronizationMessage message){
        this.messages.add(message);
    }

    /**
     * Processes all messages in the queue and then clears the queue
     */
    public void processMessages(){
        List<SynchronizationMessage> messagesToClear = new LinkedList<SynchronizationMessage>();
        for(SynchronizationMessage message : messages){
            //attempt to handle the message
            messagesToClear.add(message);
            switch(message.getMessageSubtype()){
                case CLIENTREQUESTBTREEACTION: {
                    Entity entity = EntityLookupUtils.getEntityById(message.getentityId());
                    if(entity != null){
                        this.updateEntityState(entity,message.getbTreeId(),message);
                    } else {
                        LoggerInterface.loggerNetworking.WARNING("Receiving packet from client to perform action for nonexistant entity! " + message.getentityId());
                    }
                } break;
                case UPDATECLIENTSTATE:
                case UPDATECLIENTDOUBLESTATE:
                case UPDATECLIENTFLOATSTATE:
                case UPDATECLIENTINTSTATE:
                case UPDATECLIENTLONGSTATE:
                case UPDATECLIENTSTRINGSTATE:
                case ATTACHTREE:
                case DETATCHTREE:
                case LOADSCENE:
                case SERVERNOTIFYBTREETRANSITION:
                    //silently ignore
                    break;
            }
        }
        for(SynchronizationMessage message : messagesToClear){
            messages.remove(message);
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Performs actions requested by the client
     * </p>
     * @param entity The entity
     * @param bTreeId The id of the behavior tree
     * @param message The raw synchronization message holding the update data
     */
    private void updateEntityState(Entity entity, int bTreeId, SynchronizationMessage message){
        switch(bTreeId){
            case BehaviorTreeIdEnums.BTREE_CLIENTSTANCECOMPONENT_ID: {
                ServerStanceComponent tree = ServerStanceComponent.getServerStanceComponent(entity);
                switch(message.getbTreeValue()){
                    case ServerSynchronizationManager.SERVER_SYNC_START: {
                       tree.start();
                    } break;
                    case ServerSynchronizationManager.SERVER_SYNC_INTERRUPT: {
                       tree.interrupt();
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_CLIENTJUMPTREE_ID: {
                ServerJumpTree tree = ServerJumpTree.getServerJumpTree(entity);
                switch(message.getbTreeValue()){
                    case ServerSynchronizationManager.SERVER_SYNC_START: {
                       tree.start();
                    } break;
                    case ServerSynchronizationManager.SERVER_SYNC_INTERRUPT: {
                       tree.interrupt();
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_CLIENTSPRINTTREE_ID: {
                ServerSprintTree tree = ServerSprintTree.getServerSprintTree(entity);
                switch(message.getbTreeValue()){
                    case ServerSynchronizationManager.SERVER_SYNC_START: {
                       tree.start();
                    } break;
                    case ServerSynchronizationManager.SERVER_SYNC_INTERRUPT: {
                       tree.interrupt();
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_CLIENTWALKTREE_ID: {
                ServerWalkTree tree = ServerWalkTree.getServerWalkTree(entity);
                switch(message.getbTreeValue()){
                    case ServerSynchronizationManager.SERVER_SYNC_START: {
                       tree.start();
                    } break;
                    case ServerSynchronizationManager.SERVER_SYNC_INTERRUPT: {
                       tree.interrupt();
                    } break;
                }
            } break;
    
        }
    }

}
