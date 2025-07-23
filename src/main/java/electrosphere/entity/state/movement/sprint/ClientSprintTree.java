package electrosphere.entity.state.movement.sprint;


import electrosphere.data.entity.creature.SprintSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.server.ServerSynchronizationManager;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * Client sprint tree
 */
@SynchronizedBehaviorTree(
    name = "clientSprintTree",
    isServer = false,
    correspondingTree = "serverSprintTree",
    genStartInt = true
)
public class ClientSprintTree implements BehaviorTree {
    
    @SynchronizableEnum
    public static enum SprintTreeState {
        SPRINTING,
        NOT_SPRINTING,
    }
    

    @SyncedField
    SprintTreeState state = SprintTreeState.NOT_SPRINTING;

    /**
     * The data for the sprint system
     */
    SprintSystem sprintData;
    
    /**
     * The parent entity
     */
    Entity parent;
    
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
     * <p> Automatically generated </p>
     * <p>
     * Requests that the server start this btree
     * </p>
     */
    public void start(){
        Globals.clientState.clientConnection.queueOutgoingMessage(
            SynchronizationMessage.constructClientRequestBTreeActionMessage(
                Globals.clientState.clientSceneWrapper.mapClientToServerId(parent.getId()),
                BehaviorTreeIdEnums.BTREE_CLIENTSPRINTTREE_ID,
                ServerSynchronizationManager.SERVER_SYNC_START
            )
        );
    }
    
    @Override
    public void simulate(float deltaTime){
    }

    /**
     * Gets the sprint system
     * @return The sprint system
     */
    public SprintSystem getSprintSystem(){
        return sprintData;
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
    private ClientSprintTree(Entity parent, Object ... params){
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
    public static ClientSprintTree attachTree(Entity parent, Object ... params){
        ClientSprintTree rVal = new ClientSprintTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTSPRINTTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTSPRINTTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTSPRINTTREE_ID);
    }

    /**
     * <p>
     * Gets the ClientSprintTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientSprintTree
     */
    public static ClientSprintTree getClientSprintTree(Entity entity){
        return (ClientSprintTree)entity.getData(EntityDataStrings.TREE_CLIENTSPRINTTREE);
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
                BehaviorTreeIdEnums.BTREE_CLIENTSPRINTTREE_ID,
                ServerSynchronizationManager.SERVER_SYNC_INTERRUPT
            )
        );
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts a short to the equivalent enum value
     * </p>
     * @param shortVal The short value
     * @return The enum value
     */
    public static SprintTreeState getSprintTreeStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return SprintTreeState.SPRINTING;
            case 1:
                return SprintTreeState.NOT_SPRINTING;
            default:
                return SprintTreeState.SPRINTING;
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
    public static short getSprintTreeStateEnumAsShort(SprintTreeState enumVal){
        switch(enumVal){
            case SPRINTING:
                return 0;
            case NOT_SPRINTING:
                return 1;
            default:
                return 0;
        }
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
    }

    /**
     * <p>
     * Checks if the entity has a ClientSprintTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientSprintTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTSPRINTTREE);
    }

}
