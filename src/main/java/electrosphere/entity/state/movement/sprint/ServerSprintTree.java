package electrosphere.entity.state.movement.sprint;


import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.data.entity.creature.SprintSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementTreeState;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.entity.state.movement.sprint.ClientSprintTree.SprintTreeState;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * Server sprint tree
 */
@SynchronizedBehaviorTree(
    name = "serverSprintTree",
    isServer = true,
    correspondingTree = "clientSprintTree"
)
public class ServerSprintTree implements BehaviorTree {
    
    @SyncedField
    SprintTreeState state = SprintTreeState.NOT_SPRINTING;
    
    /**
     * The data for the sprint system
     */
    SprintSystem sprintData;

    /**
     * Gets the ground movement tree associated with this sprint tree
     */
    ServerGroundMovementTree groundMovementTree;
    
    /**
     * Gets the parent entity
     */
    Entity parent;
    
    /**
     * The current stamina for the tree
     */
    int staminaCurrent = 0;
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public SprintTreeState getState(){
        return state;
    }
    
    /**
     * Starts the sprint component
     */
    public void start(){
        if(staminaCurrent > 0){
            // System.out.println("Starting sprinting");
            this.setState(SprintTreeState.SPRINTING);
        }
    }
    
    /**
     * Interrupts the sprint component
     */
    public void interrupt(){
        this.setState(SprintTreeState.NOT_SPRINTING);
    }
    
    @Override
    public void simulate(float deltaTime){
        switch(state){
            case SPRINTING:
                if(groundMovementTree != null && groundMovementTree.getState() != MovementTreeState.IDLE){
                    staminaCurrent--;
                    if(staminaCurrent < 1){
                        state = SprintTreeState.NOT_SPRINTING;
                    }
                }
                break;
            case NOT_SPRINTING:
                staminaCurrent++;
                if(staminaCurrent > sprintData.getStaminaMax()){
                    staminaCurrent = sprintData.getStaminaMax();
                }
                break;
        }
    }

    /**
     * Gets the sprint system
     * @return The sprint system
     */
    public SprintSystem getSprintSystem(){
        return sprintData;
    }
    
    /**
     * Sets the ground movement tree associated with this sprint tree
     * @param groundMovementTree The ground movement tree
     */
    public void setServerGroundMovementTree(ServerGroundMovementTree groundMovementTree){
        this.groundMovementTree = groundMovementTree;
    }

    /**
     * Checks if the entity is sprinting
     * @return true if it is sprinting, false otherwise
     */
    public boolean isSprinting(){
        return this.state == SprintTreeState.SPRINTING;
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
    private ServerSprintTree(Entity parent, Object ... params){
        if(params.length < 1 || (params[0] instanceof SprintSystem) == false){
            throw new IllegalArgumentException("Trying to create client walk tree with invalid arguments!");
        }
        this.parent = parent;
        this.sprintData = (SprintSystem)params[0];
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
    public static ServerSprintTree attachTree(Entity parent, Object ... params){
        ServerSprintTree rVal = new ServerSprintTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERSPRINTTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID);
    }

    /**
     * <p>
     * Gets the ServerSprintTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerSprintTree
     */
    public static ServerSprintTree getServerSprintTree(Entity entity){
        return (ServerSprintTree)entity.getData(EntityDataStrings.TREE_SERVERSPRINTTREE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(SprintTreeState state){
        this.state = state;
        int value = ClientSprintTree.getSprintTreeStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID, FieldIdEnums.TREE_SERVERSPRINTTREE_SYNCEDFIELD_STATE_ID, value));
        }
    }

    /**
     * <p>
     * Checks if the entity has a ServerSprintTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerSprintTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERSPRINTTREE);
    }

}
