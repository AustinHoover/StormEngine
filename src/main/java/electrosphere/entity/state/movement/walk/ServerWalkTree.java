package electrosphere.entity.state.movement.walk;


import electrosphere.data.entity.creature.movement.WalkMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.movement.walk.ClientWalkTree.WalkState;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

@SynchronizedBehaviorTree(
    name = "serverWalkTree",
    isServer = true,
    correspondingTree = "clientWalkTree"
)
/*
Behavior tree for walking
*/
public class ServerWalkTree implements BehaviorTree {

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
    public void simulate(float deltaTime) {
    }

    /**
     * Gets the movement modifier to apply while walking
     * @return The modifier
     */
    public float getModifier(){
        return this.walkMovementSystem.getModifier();
    }

    /**
     * Starts the tree
     */
    public void start(){
        this.setState(WalkState.ACTIVE);
    }

    /**
     * Stops the tree
     */
    public void stop(){
        this.setState(WalkState.INACTIVE);
    }

    /**
     * Interrupts the tree
     */
    public void interrupt(){
        this.setState(WalkState.INACTIVE);
    }

    /**
     * Checks if the tree is walking
     * @return True if is walking, false otherwise
     */
    public boolean isWalking(){
        return this.state == WalkState.ACTIVE;
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
     * <p> (initially) Automatically generated </p>
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ServerWalkTree attachTree(Entity parent, Object ... params){
        ServerWalkTree rVal = new ServerWalkTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERWALKTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID);
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
    public ServerWalkTree(Entity parent, Object ... params){
        if(params.length < 1 || (params[0] instanceof WalkMovementSystem) == false){
            throw new IllegalArgumentException("Trying to create client walk tree with invalid arguments!");
        }
        this.parent = parent;
        this.walkMovementSystem = (WalkMovementSystem)params[0];
    }

    /**
     * <p>
     * Gets the ServerWalkTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerWalkTree
     */
    public static ServerWalkTree getServerWalkTree(Entity entity){
        return (ServerWalkTree)entity.getData(EntityDataStrings.TREE_SERVERWALKTREE);
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
        int value = ClientWalkTree.getWalkStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID, FieldIdEnums.TREE_SERVERWALKTREE_SYNCEDFIELD_STATE_ID, value));
        }
    }

    /**
     * <p>
     * Checks if the entity has a ServerWalkTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerWalkTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERWALKTREE);
    }

}
