package electrosphere.entity.state.idle;


import electrosphere.entity.state.attack.ClientAttackTree.AttackTreeState;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.idle.ClientIdleTree.IdleTreeState;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementTreeState;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.data.entity.creature.IdleData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.server.entity.poseactor.PoseActor;

import java.util.LinkedList;
import java.util.List;


@SynchronizedBehaviorTree(name = "serverIdle", isServer = true, correspondingTree = "idle")
/**
 * Tree for playing an idle animation when an entity isn't doing anything
 */
public class ServerIdleTree implements BehaviorTree {

    @SyncedField
    private IdleTreeState state;

    Entity parent;

    //The idle data
    IdleData idleData;

    List<EntityMessage> networkMessageQueue = new LinkedList<EntityMessage>();

    /**
     * Creates a server idle tree
     * @param e The entity to attach it to
     */
    public ServerIdleTree(Entity e, Object ... params){
        state = IdleTreeState.IDLE;
        parent = e;
        this.idleData = (IdleData)params[0];
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public IdleTreeState getState(){
        return state;
    }

    /**
     * Starts idling
     */
    public void start(){
        setState(IdleTreeState.IDLE);
    }

    /**
     * Interrupts the idle animation
     */
    public void interrupt(){
        setState(IdleTreeState.NOT_IDLE);
    }

    /**
     * Stops the idle animation
     */
    public void stop(){
        setState(IdleTreeState.NOT_IDLE);
    }

    /**
     * Simulates the idle tree
     */
    public void simulate(float deltaTime){
        PoseActor poseActor = EntityUtils.getPoseActor(parent);

        boolean movementTreeIsIdle = movementTreeIsIdle();

        boolean hasAttackTree = parent.containsKey(EntityDataStrings.TREE_SERVERATTACKTREE);
        ServerAttackTree attackTree = null;
        if(hasAttackTree){
            attackTree = CreatureUtils.serverGetAttackTree(parent);
        }

        boolean isIdle;

        //state machine
        switch(state){
            case IDLE:
                if(poseActor != null){
                    if(
                        (!poseActor.isPlayingAnimation() || !poseActor.isPlayingAnimation(idleData.getAnimation()))
                    ){
                        poseActor.playAnimation(idleData.getAnimation());
                        poseActor.incrementAnimationTime(0.0001);
                    }
                }
                isIdle = true;
                if(!movementTreeIsIdle){
                    isIdle = false;
                }
                if(hasAttackTree){
                    if(attackTree.getState() != AttackTreeState.IDLE){
                        isIdle = false;
                    }
                }
                if(!isIdle){
                    this.setState(IdleTreeState.NOT_IDLE);
                }
                break;
            case NOT_IDLE:
                isIdle = true;
                if(!movementTreeIsIdle){
                    isIdle = false;
                }
                if(hasAttackTree){
                    if(attackTree.getState() != AttackTreeState.IDLE){
                        isIdle = false;
                    }
                }
                if(isIdle){
                    this.setState(IdleTreeState.IDLE);
                }
                break;
        }
    }

    boolean movementTreeIsIdle(){
        boolean rVal = false;
        boolean hasMovementTree = parent.containsKey(EntityDataStrings.SERVER_MOVEMENT_BT);
        if(hasMovementTree){
            BehaviorTree movementTree = CreatureUtils.serverGetEntityMovementTree(parent);
            if(movementTree instanceof ServerGroundMovementTree){
                if(((ServerGroundMovementTree)movementTree).getState() == MovementTreeState.IDLE){
                    rVal = true;
                }
            }
        } else {
            rVal = true;
        }
        return rVal;
    }

    public void addNetworkMessage(EntityMessage networkMessage) {
        networkMessageQueue.add(networkMessage);
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ServerIdleTree attachTree(Entity parent, Object ... params){
        ServerIdleTree rVal = new ServerIdleTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERIDLE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID);
        return rVal;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Detatches this tree from the entity.
     * </p>
     * @param entity The entity to detach to
     * @param tree The behavior tree to detach
     */
    public static void detachTree(Entity entity, BehaviorTree tree){
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(IdleTreeState state){
        this.state = state;
        int value = ClientIdleTree.getIdleTreeStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID, FieldIdEnums.TREE_SERVERIDLE_SYNCEDFIELD_STATE_ID, value));
        }
    }
    
    /**
     * <p>
     * Gets the ServerIdleTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerIdleTree
     */
    public static ServerIdleTree getServerIdleTree(Entity entity){
        return (ServerIdleTree)entity.getData(EntityDataStrings.TREE_SERVERIDLE);
    }
    /**
     * <p>
     * Checks if the entity has a ServerIdleTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerIdleTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERIDLE);
    }

}