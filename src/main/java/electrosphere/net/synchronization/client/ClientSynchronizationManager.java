package electrosphere.net.synchronization.client;


import electrosphere.entity.state.lod.ClientLODComponent;
import electrosphere.entity.state.growth.ClientGrowthComponent;
import electrosphere.entity.state.furniture.ClientDoorState;
import electrosphere.entity.state.item.ClientChargeState;
import electrosphere.entity.state.movement.editor.ClientEditorMovementTree;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.stance.ClientStanceComponent;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.state.movement.sprint.ClientSprintTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.state.movement.walk.ClientWalkTree;
import electrosphere.entity.state.life.ClientLifeTree;
import electrosphere.entity.state.block.ClientBlockTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree;
import electrosphere.logger.LoggerInterface;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.gravity.ClientGravityTree;
import electrosphere.entity.state.idle.ClientIdleTree;
import electrosphere.server.datacell.utils.EntityLookupUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.parser.net.message.SynchronizationMessage.SynchronizationMessageType;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;

/**
 * Takes in raw behavior tree packets from server and pushes their values into respective behavior trees in entities
 */
public class ClientSynchronizationManager {

    /**
     * The count at which to warn about a message bouncing
     */
    static final int MESSAGE_BOUNCE_WARNING_COUNT = 100;

    /**
     * Number of frames to keep an entity deletion key around
     */
    static final int DELETED_KEY_STORAGE_FRAMES = 1000;

    /**
     * The list of messages to loop through
     */
    private List<SynchronizationMessage> messages = new LinkedList<SynchronizationMessage>();

    /**
     * Map that tracks the number of times a network message bounces
     */
    private Map<SynchronizationMessage,Integer> messageBounceCount = new HashMap<SynchronizationMessage,Integer>();

    /**
     * The list of Ids that the server has said to destroy
     */
    private Map<Integer,Integer> deletedEntityIds = new HashMap<Integer,Integer>();

    /**
     * Lock for thread-safeing the manager
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * Pushes a message into the queue to be processed
     * @param message The message
     */
    public void pushMessage(SynchronizationMessage message){
        lock.lock();
        this.messages.add(message);
        lock.unlock();
    }

    /**
     * Processes all messages in the queue and then clears the queue
     */
    public void processMessages(){
        lock.lock();
        List<SynchronizationMessage> messagesToClear = new LinkedList<SynchronizationMessage>();
        for(SynchronizationMessage message : messages){

            //track number of times this message has bounced
            if(messageBounceCount.containsKey(message)){
                messageBounceCount.put(message, messageBounceCount.get(message) + 1);
            } else {
                messageBounceCount.put(message, 0);
            }

            //remove sync messages if the entity was already deleted by the server
            if(this.deletedEntityIds.containsKey(message.getentityId())){
                messageBounceCount.remove(message);
            }

            //attempt to handle the message
            if(Globals.clientState.clientSceneWrapper.containsServerId(message.getentityId()) && Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityId()) != null){
                messagesToClear.add(message);
                messageBounceCount.remove(message);
                switch(message.getMessageSubtype()){
                    case UPDATECLIENTSTATE:
                    case UPDATECLIENTDOUBLESTATE:
                    case UPDATECLIENTFLOATSTATE:
                    case UPDATECLIENTINTSTATE:
                    case UPDATECLIENTLONGSTATE:
                    case UPDATECLIENTSTRINGSTATE:{
                        int bTreeId = message.getbTreeId();
                        int entityId = message.getentityId();
                        Entity targetEntity = Globals.clientState.clientSceneWrapper.getEntityFromServerId(entityId);
                        this.updateEntityState(targetEntity,bTreeId,message);
                    } break;
                    case SERVERNOTIFYBTREETRANSITION: {
                        int bTreeId = message.getbTreeId();
                        int entityId = message.getentityId();
                        Entity targetEntity = Globals.clientState.clientSceneWrapper.getEntityFromServerId(entityId);
                        this.transitionBTree(targetEntity, bTreeId, message);
                    } break;
                    case ATTACHTREE:{
                        // int bTreeId = message.getbTreeId();
                        // int bTreeValue = message.getbTreeValue();
                        // int entityId = message.getentityId();
                        throw new UnsupportedOperationException("Not implemented yet!");
                    }
                    case DETATCHTREE:{
                        // int bTreeId = message.getbTreeId();
                        // int entityId = message.getentityId();
                        throw new UnsupportedOperationException("Not implemented yet!");
                    }
                    case LOADSCENE: {
                        throw new UnsupportedOperationException("Not implemented yet!");
                    }
                    case CLIENTREQUESTBTREEACTION:
                        //silently ignore
                        break;
                }
            } else if(Globals.clientState.clientSceneWrapper.containsServerId(message.getentityId()) && Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityId()) == null){
                String errorMessage = 
                "Client received synchronization packet for entity that does not exists on client!\n" +
                "Specifically, the synchronization manager thinks this id is registered, but there is no entity at that key\n" +
                "Entity id in network message: " + message.getentityId()
                ;
                Globals.clientState.clientSceneWrapper.dumpTranslationLayerStatus();
                Globals.clientState.clientSceneWrapper.dumpIdData(message.getentityId());
                Globals.clientState.clientSceneWrapper.getScene().describeScene();
                throw new IllegalStateException(errorMessage);
            } else if(!Globals.clientState.clientSceneWrapper.containsServerId(message.getentityId()) && !Globals.clientState.clientSceneWrapper.hasBeenDeleted(message.getentityId())){
                //TODO: have client send query to server to resend this entity
                // String errorMessage = 
                // "Client received synchronization packet for entity that does not exists on client!\n" +
                // "This ID was never created on the client, yet the client is receiving a synchronization packet for it!\n" +
                // "Entity id in network message: " + message.getentityId()
                // ;
                // Globals.clientState.clientSceneWrapper.dumpTranslationLayerStatus();
                // Globals.clientState.clientSceneWrapper.dumpIdData(message.getentityId());
                // Globals.clientState.clientSceneWrapper.getScene().describeScene();
                // throw new IllegalStateException(errorMessage);
            }

            //warn if a message has bounced a certain number of times
            if(messageBounceCount.containsKey(message) && messageBounceCount.get(message) > MESSAGE_BOUNCE_WARNING_COUNT){

                //data from the message itself
                int serverId = message.getentityId();
                String clientContainsServerId = "" + Globals.clientState.clientSceneWrapper.containsServerId(serverId);
                String entityFromServerId = "" + Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityId());

                //actual server entity
                Entity actualServerEntity = EntityLookupUtils.getEntityById(serverId);
                String serverTypeId = "";
                if(actualServerEntity != null){
                    serverTypeId = CommonEntityUtils.getEntitySubtype(actualServerEntity);
                }
                
                //other data about the message
                SynchronizationMessageType type = message.getMessageSubtype();

                //construct message
                String warningMessage =
                "A synchronization message has bounced at least " + MESSAGE_BOUNCE_WARNING_COUNT + "times!\n" +
                "Type of message that was sent: " + type + "\n" +
                "Id of the entity on the server: " + serverId + "\n" +
                "Type of entity on server: " + serverTypeId + "\n" +
                "Client contains an entity that maps to that server id: " + clientContainsServerId + "\n" +
                "Entity on the client that was resolved from the server id: " + entityFromServerId + "\n" +
                "Message btree id: " + message.getbTreeId() + "\n" +
                "Message field id: " + message.getfieldId() + "\n" +
                "Deleted keys stores entity id: " + this.deletedEntityIds.get(message.getentityId()) + "\n" +
                ""
                ;
                LoggerInterface.loggerNetworking.WARNING(warningMessage);
            }
        }
        for(SynchronizationMessage message : messagesToClear){
            messages.remove(message);
            Globals.clientState.clientConnection.release(message);
        }
        Set<Integer> deletionKeys = this.deletedEntityIds.keySet();
        for(int key : deletionKeys){
            int framesStored = this.deletedEntityIds.get(key);
            if(framesStored > DELETED_KEY_STORAGE_FRAMES){
                this.deletedEntityIds.remove(key);
            }
        }
        lock.unlock();
    }

    /**
     * Adds an id that the server said to destroy
     * @param id The id that was destroyed
     */
    public void addDeletedId(int id){
        this.deletedEntityIds.put(id,1);
    }

    /**
     * Checks if an entity has been deleted by the server
     * @param entityId The entity's id
     * @return true if it has been deleted, false otherwise
     */
    public boolean isDeleted(int entityId){
        return this.deletedEntityIds.containsKey(entityId);
    }

    /**
     * Ejects a deleted key (ie if server tells us to create a deleted entity again)
     * @param entityId The entity id to un-delete
     */
    public void ejectDeletedKey(int entityId){
        if(this.deletedEntityIds.containsKey(entityId)){
            this.deletedEntityIds.remove(entityId);
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Updates the state of a given behavior tree on a given entity
     * </p>
     * @param entity The entity
     * @param bTreeId The id of the behavior tree
     * @param message The raw synchronization message holding the update data
     */
    private void updateEntityState(Entity entity, int bTreeId, SynchronizationMessage message){
        switch(bTreeId){
            case BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_STATE_ID:{
                        ClientAttackTree tree = ClientAttackTree.getClientAttackTree(entity);
                        if(tree != null){
                            tree.setState(ClientAttackTree.getAttackTreeStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                    case FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_DRIFTSTATE_ID:{
                        ClientAttackTree tree = ClientAttackTree.getClientAttackTree(entity);
                        if(tree != null){
                            tree.setDriftState(ClientAttackTree.getAttackTreeDriftStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                    case FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_CURRENTMOVEID_ID:{
                        ClientAttackTree tree = ClientAttackTree.getClientAttackTree(entity);
                        if(tree != null){
                            tree.setCurrentMoveId(message.getstringValue());
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_STATE_ID:{
                        ClientBlockTree tree = ClientBlockTree.getClientBlockTree(entity);
                        if(tree != null){
                            tree.setState(ClientBlockTree.getBlockStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                    case FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_CURRENTBLOCKVARIANT_ID:{
                        ClientBlockTree tree = ClientBlockTree.getClientBlockTree(entity);
                        if(tree != null){
                            tree.setCurrentBlockVariant(message.getstringValue());
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVEREQUIPSTATE_ID: {
                switch(message.getfieldId()){
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERTOOLBARSTATE_SYNCEDFIELD_SELECTEDSLOT_ID:{
                        ClientToolbarState tree = ClientToolbarState.getClientToolbarState(entity);
                        if(tree != null){
                            tree.setSelectedSlot(message.getintValue());
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERDOOR_SYNCEDFIELD_STATE_ID:{
                        ClientDoorState tree = ClientDoorState.getClientDoorState(entity);
                        if(tree != null){
                            tree.setState(ClientDoorState.getDoorStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERGRAVITY_SYNCEDFIELD_STATE_ID:{
                        ClientGravityTree tree = ClientGravityTree.getClientGravityTree(entity);
                        if(tree != null){
                            tree.setState(ClientGravityTree.getGravityTreeStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERGROWTH_SYNCEDFIELD_STATUS_ID:{
                        ClientGrowthComponent tree = ClientGrowthComponent.getClientGrowthComponent(entity);
                        if(tree != null){
                            tree.setStatus(message.getintValue());
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERIDLE_SYNCEDFIELD_STATE_ID:{
                        ClientIdleTree tree = ClientIdleTree.getClientIdleTree(entity);
                        if(tree != null){
                            tree.setState(ClientIdleTree.getIdleTreeStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERCHARGESTATE_SYNCEDFIELD_CHARGES_ID:{
                        ClientChargeState tree = ClientChargeState.getClientChargeState(entity);
                        if(tree != null){
                            tree.setCharges(message.getintValue());
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERLIFETREE_SYNCEDFIELD_STATE_ID:{
                        ClientLifeTree tree = ClientLifeTree.getClientLifeTree(entity);
                        if(tree != null){
                            tree.setState(ClientLifeTree.getLifeStateEnumShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERLODTREE_SYNCEDFIELD_LODLEVEL_ID:{
                        ClientLODComponent tree = ClientLODComponent.getClientLODComponent(entity);
                        if(tree != null){
                            tree.setLodLevel(message.getintValue());
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERSTANCECOMPONENT_SYNCEDFIELD_STATE_ID:{
                        ClientStanceComponent tree = ClientStanceComponent.getClientStanceComponent(entity);
                        if(tree != null){
                            tree.setState(ClientStanceComponent.getCombatStanceShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVEREDITORMOVEMENTTREE_SYNCEDFIELD_FACING_ID:{
                        ClientEditorMovementTree tree = ClientEditorMovementTree.getClientEditorMovementTree(entity);
                        if(tree != null){
                            tree.setFacing(ClientEditorMovementTree.getEditorMovementRelativeFacingShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERGROUNDMOVEMENTTREE_SYNCEDFIELD_FACING_ID:{
                        ClientGroundMovementTree tree = ClientGroundMovementTree.getClientGroundMovementTree(entity);
                        if(tree != null){
                            tree.setFacing(ClientGroundMovementTree.getMovementRelativeFacingShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_STATE_ID:{
                        ClientJumpTree tree = ClientJumpTree.getClientJumpTree(entity);
                        if(tree != null){
                            tree.setState(ClientJumpTree.getJumpStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                    case FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTFRAME_ID:{
                        ClientJumpTree tree = ClientJumpTree.getClientJumpTree(entity);
                        if(tree != null){
                            tree.setCurrentFrame(message.getintValue());
                        }
                    } break;
                    case FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTJUMPFORCE_ID:{
                        ClientJumpTree tree = ClientJumpTree.getClientJumpTree(entity);
                        if(tree != null){
                            tree.setCurrentJumpForce(message.getfloatValue());
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERSPRINTTREE_SYNCEDFIELD_STATE_ID:{
                        ClientSprintTree tree = ClientSprintTree.getClientSprintTree(entity);
                        if(tree != null){
                            tree.setState(ClientSprintTree.getSprintTreeStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERWALKTREE_SYNCEDFIELD_STATE_ID:{
                        ClientWalkTree tree = ClientWalkTree.getClientWalkTree(entity);
                        if(tree != null){
                            tree.setState(ClientWalkTree.getWalkStateShortAsEnum((short)message.getbTreeValue()));
                        }
                    } break;
                }
            } break;
    
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Transitions a behavior tree to a new state
     * </p>
     * @param entity The entity
     * @param bTreeId The id of the behavior tree
     * @param message The raw synchronization message holding the update data
     */
    private void transitionBTree(Entity entity, int bTreeId, SynchronizationMessage message){
        switch(bTreeId){
            case BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_STATE_ID:{
                        ClientAttackTree tree = ClientAttackTree.getClientAttackTree(entity);
                        tree.transitionState(ClientAttackTree.getAttackTreeStateShortAsEnum((short)message.getbTreeValue()));
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_STATE_ID:{
                        ClientBlockTree tree = ClientBlockTree.getClientBlockTree(entity);
                        tree.transitionState(ClientBlockTree.getBlockStateShortAsEnum((short)message.getbTreeValue()));
                    } break;
                }
            } break;
            case BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID: {
                switch(message.getfieldId()){
                    case FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_STATE_ID:{
                        ClientJumpTree tree = ClientJumpTree.getClientJumpTree(entity);
                        tree.transitionState(ClientJumpTree.getJumpStateShortAsEnum((short)message.getbTreeValue()));
                    } break;
                }
            } break;
    
        }
    }
    
}
