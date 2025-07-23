package electrosphere.entity.state.item;


import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;


/**
 * Item charge state
 */
@SynchronizedBehaviorTree(name = "clientChargeState", isServer = false, correspondingTree="serverChargeState")
public class ClientChargeState implements BehaviorTree {
    
    /**
     * The charges on the item
     */
    @SyncedField
    int charges;

    /**
     * The maximum allowed charges for this item
     */
    int maxCharges;

    /**
     * The parent of this state
     */
    Entity parent;

    /**
     * Constructor
     * @param parent The parent of this state
     * @param params The params
     */
    private ClientChargeState(Entity parent, Object ... params){
        this.parent = parent;
        this.maxCharges = (Integer)params[0];
        this.charges = 1;
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
    public static ClientChargeState attachTree(Entity parent, Object ... params){
        ClientChargeState rVal = new ClientChargeState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTCHARGESTATE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTCHARGESTATE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTCHARGESTATE_ID);
    }

    /**
     * <p>
     * Gets the ClientChargeState of the entity
     * </p>
     * @param entity the entity
     * @return The ClientChargeState
     */
    public static ClientChargeState getClientChargeState(Entity entity){
        return (ClientChargeState)entity.getData(EntityDataStrings.TREE_CLIENTCHARGESTATE);
    }

    /**
     * <p>
     * Checks if the entity has a ClientChargeState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientChargeState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTCHARGESTATE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets charges and handles the synchronization logic for it.
     * </p>
     * @param charges The value to set charges to.
     */
    public void setCharges(int charges){
        this.charges = charges;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets charges.
     * </p>
     */
    public int getCharges(){
        return charges;
    }

    @Override
    public void simulate(float deltaTime) {
    }

}
