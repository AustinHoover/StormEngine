package electrosphere.entity.state.stance;


import electrosphere.entity.btree.BehaviorTree;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.server.ServerSynchronizationManager;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.entity.Entity;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * Tracks the weapon/combat stance of the entity
 */
@SynchronizedBehaviorTree(
    name = "clientStanceComponent",
    isServer = false,
    correspondingTree = "serverStanceComponent",
    genStartInt = true
)
public class ClientStanceComponent implements BehaviorTree {
    
    @SynchronizableEnum
    public static enum CombatStance {
        /**
         * Not in attacking stance
         */
        IDLE,
        /**
         * No weapon equipped, in attacking stance
         */
        UNARMED,
        /**
         * Weapon equipped, in attacking stance
         */
        ARMED,
    }

    @SyncedField
    CombatStance state = CombatStance.IDLE;

    /**
     * The parent entity
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
    public static ClientStanceComponent attachTree(Entity parent, Object ... params){
        ClientStanceComponent rVal = new ClientStanceComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTSTANCECOMPONENT, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTSTANCECOMPONENT_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTSTANCECOMPONENT_ID);
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
    public ClientStanceComponent(Entity parent, Object ... params){
        this.parent = parent;
    }

    /**
     * <p>
     * Gets the ClientStanceComponent of the entity
     * </p>
     * @param entity the entity
     * @return The ClientStanceComponent
     */
    public static ClientStanceComponent getClientStanceComponent(Entity entity){
        return (ClientStanceComponent)entity.getData(EntityDataStrings.TREE_CLIENTSTANCECOMPONENT);
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
                BehaviorTreeIdEnums.BTREE_CLIENTSTANCECOMPONENT_ID,
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
                BehaviorTreeIdEnums.BTREE_CLIENTSTANCECOMPONENT_ID,
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
    public void setState(CombatStance state){
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
    public static CombatStance getCombatStanceShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return CombatStance.IDLE;
            case 1:
                return CombatStance.UNARMED;
            case 2:
                return CombatStance.ARMED;
            default:
                return CombatStance.IDLE;
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
    public static short getCombatStanceEnumAsShort(CombatStance enumVal){
        switch(enumVal){
            case IDLE:
                return 0;
            case UNARMED:
                return 1;
            case ARMED:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public void simulate(float deltaTime) {
        throw new UnsupportedOperationException("Unimplemented method 'simulate'");
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
     * Checks if the entity has a ClientStanceComponent component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientStanceComponent(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTSTANCECOMPONENT);
    }

}
