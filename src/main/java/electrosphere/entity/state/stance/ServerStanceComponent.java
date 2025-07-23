package electrosphere.entity.state.stance;


import electrosphere.entity.btree.BehaviorTree;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.state.stance.ClientStanceComponent.CombatStance;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * Tracks the weapon/combat stance of the entity
 */
@SynchronizedBehaviorTree(
    name = "serverStanceComponent",
    isServer = true,
    correspondingTree = "clientStanceComponent"
)
public class ServerStanceComponent implements BehaviorTree {
    
    @SyncedField
    CombatStance state = CombatStance.IDLE;

    /**
     * The parent entity of this component
     */
    Entity parent;

    /**
     * <p> (initially) Automatically generated </p>
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ServerStanceComponent attachTree(Entity parent, Object ... params){
        ServerStanceComponent rVal = new ServerStanceComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERSTANCECOMPONENT, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID);
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
    public ServerStanceComponent(Entity parent, Object ... params){
        this.parent = parent;
    }

    /**
     * <p>
     * Gets the ServerStanceComponent of the entity
     * </p>
     * @param entity the entity
     * @return The ServerStanceComponent
     */
    public static ServerStanceComponent getServerStanceComponent(Entity entity){
        return (ServerStanceComponent)entity.getData(EntityDataStrings.TREE_SERVERSTANCECOMPONENT);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(CombatStance state){
        this.state = state;
        int value = ClientStanceComponent.getCombatStanceEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID, FieldIdEnums.TREE_SERVERSTANCECOMPONENT_SYNCEDFIELD_STATE_ID, value));
        }
    }

    @Override
    public void simulate(float deltaTime) {
        throw new UnsupportedOperationException("Unimplemented method 'simulate'");
    }

    public void start(){

    }

    public void interrupt(){
        
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public CombatStance getState(){
        return state;
    }

    /**
     * <p>
     * Checks if the entity has a ServerStanceComponent component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerStanceComponent(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERSTANCECOMPONENT);
    }

}
