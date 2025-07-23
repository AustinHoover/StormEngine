package electrosphere.entity.state.movement.walk;


import electrosphere.net.synchronization.server.ServerSynchronizationManager;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.data.entity.creature.movement.WalkMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

@SynchronizedBehaviorTree(
    name = "clientWalkTree",
    isServer = false,
    correspondingTree = "serverWalkTree",
    genStartInt = true
)
/*
Behavior tree for walking
*/
public class ClientWalkTree implements BehaviorTree {

    /**
     * States for the walk tree
     */
    @SynchronizableEnum
    public static enum WalkState {
        /**
         * Is not walking
         */
        INACTIVE,
        /**
         * Is walking
         */
        ACTIVE,
    }

    /**
     * The state of the tree
     */
    @SyncedField
    WalkState state = WalkState.INACTIVE;

    /**
     * The parent entity
     */
    Entity parent;

    /**
     * The data for the walking system
     */
    WalkMovementSystem walkMovementSystem;


    @Override
    public void simulate(float deltaTime){
    }

    /**
     * Checks if the tree is walking
     * @return True if is walking, false otherwise
     */
    public boolean isWalking(){
        return this.state == WalkState.ACTIVE;
    }

    /**
     * Gets the movement modifier to apply while walking
     * @return The modifier
     */
    public float getModifier(){
        return this.walkMovementSystem.getModifier();
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
    public static ClientWalkTree attachTree(Entity parent, Object ... params){
        ClientWalkTree rVal = new ClientWalkTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTWALKTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTWALKTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTWALKTREE_ID);
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p> Private constructor to enforce using the attach methods </p>
     * <p>
     * Constructor
     * </p>
     * @param parent The parent entity of this tree
     * @param params Optional parameters that can be provided when attaching the tree. All custom data required for creating this tree should be passed in this varargs.
     */
    public ClientWalkTree(Entity parent, Object ... params){
        if(params.length < 1 || (params[0] instanceof WalkMovementSystem) == false){
            throw new IllegalArgumentException("Trying to create client walk tree with invalid arguments!");
        }
        this.parent = parent;
        this.walkMovementSystem = (WalkMovementSystem)params[0];
    }

    /**
     * <p>
     * Gets the ClientWalkTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientWalkTree
     */
    public static ClientWalkTree getClientWalkTree(Entity entity){
        return (ClientWalkTree)entity.getData(EntityDataStrings.TREE_CLIENTWALKTREE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Requests that the server start this btree
     * </p>
     */
    public void start(){
        Globals.clientState.clientConnection.queueOutgoingMessage(
            SynchronizationMessage.constructClientRequestBTreeActionMessage(
                Globals.clientState.clientSceneWrapper.mapClientToServerId(parent.getId()),
                BehaviorTreeIdEnums.BTREE_CLIENTWALKTREE_ID,
                ServerSynchronizationManager.SERVER_SYNC_START
            )
        );
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Requests that the server start this btree
     * </p>
     */
    public void interrupt(){
        Globals.clientState.clientConnection.queueOutgoingMessage(
            SynchronizationMessage.constructClientRequestBTreeActionMessage(
                Globals.clientState.clientSceneWrapper.mapClientToServerId(parent.getId()),
                BehaviorTreeIdEnums.BTREE_CLIENTWALKTREE_ID,
                ServerSynchronizationManager.SERVER_SYNC_INTERRUPT
            )
        );
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(WalkState state){
        this.state = state;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts a short to the equivalent enum value
     * </p>
     * @param shortVal The short value
     * @return The enum value
     */
    public static WalkState getWalkStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return WalkState.INACTIVE;
            case 1:
                return WalkState.ACTIVE;
            default:
                return WalkState.INACTIVE;
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getWalkStateEnumAsShort(WalkState enumVal){
        switch(enumVal){
            case INACTIVE:
                return 0;
            case ACTIVE:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public WalkState getState(){
        return state;
    }

    /**
     * <p>
     * Checks if the entity has a ClientWalkTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientWalkTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTWALKTREE);
    }

}
