package electrosphere.entity.state.life;


import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.Entity;
import electrosphere.data.entity.common.life.HealthSystem;
import electrosphere.engine.Globals;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;

@SynchronizedBehaviorTree(name = "clientLifeTree", isServer = false, correspondingTree="serverLifeTree")
/**
 * Client life state tree
 */
public class ClientLifeTree implements BehaviorTree {
    
    @SynchronizableEnum
    /**
     * States available to the life tree
     */
    public static enum LifeStateEnum {
        ALIVE,
        DYING,
        DEAD,
    }

    //the current state of the tree
    @SyncedField
    LifeStateEnum state = LifeStateEnum.ALIVE;

    //the parent entity of this life tree
    Entity parent;

    //data used to construct the tree
    HealthSystem healthSystem;

    //state transition util
    //state transition util
    StateTransitionUtil stateTransitionUtil;



    @Override
    public void simulate(float deltaTime) {
        
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public LifeStateEnum getState(){
        return state;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(LifeStateEnum state){
        this.state = state;
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
    public static ClientLifeTree attachTree(Entity parent, Object ... params){
        ClientLifeTree rVal = new ClientLifeTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTLIFETREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTLIFETREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTLIFETREE_ID);
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p> Private constructor to enforce using the attach methods </p>
     * <p>
     * Constructor
     * </p>
     * @param parent The parent entity of this tree
     */
    public ClientLifeTree(Entity parent, Object ... params){
        this.parent = parent;
        this.healthSystem = (HealthSystem)params[0];
        this.stateTransitionUtil = StateTransitionUtil.create(parent, false, new StateTransitionUtilItem[]{
            StateTransitionUtilItem.create(
                LifeStateEnum.DYING,
                this.healthSystem.getDyingState(),
                () -> {}
            )
        });
    }

    /**
     * <p>
     * Gets the ClientLifeTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientLifeTree
     */
    public static ClientLifeTree getClientLifeTree(Entity entity){
        return (ClientLifeTree)entity.getData(EntityDataStrings.TREE_CLIENTLIFETREE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getLifeStateEnumEnumAsShort(LifeStateEnum enumVal){
        switch(enumVal){
            case ALIVE:
                return 0;
            case DYING:
                return 1;
            case DEAD:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts a short to the equivalent enum value
     * </p>
     * @param shortVal The short value
     * @return The enum value
     */
    public static LifeStateEnum getLifeStateEnumShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return LifeStateEnum.ALIVE;
            case 1:
                return LifeStateEnum.DYING;
            case 2:
                return LifeStateEnum.DEAD;
            default:
                return LifeStateEnum.ALIVE;
        }
    }

    /**
     * <p>
     * Checks if the entity has a ClientLifeTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientLifeTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTLIFETREE);
    }

}
