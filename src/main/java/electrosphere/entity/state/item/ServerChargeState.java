package electrosphere.entity.state.item;


import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.server.player.Player;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;


/**
 * Item charge state
 */
@SynchronizedBehaviorTree(name = "serverChargeState", isServer = true, correspondingTree="clientChargeState")
public class ServerChargeState implements BehaviorTree {

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
    private ServerChargeState(Entity parent, Object ... params){
        this.parent = parent;
        this.maxCharges = (Integer)params[0];
        this.charges = 1;
    }

    @Override
    public void simulate(float deltaTime) {
    }

    /**
     * Attempts to remove a charge from whatever item the parent entity currently has equipped
     * @param parent The parent
     */
    public static void attemptRemoveCharges(Entity parent, int charges){
        if(ServerToolbarState.hasServerToolbarState(parent)){
            ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(parent);
            Entity inventoryItem = serverToolbarState.getInInventoryItem();
            if(inventoryItem != null){
                ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(inventoryItem);
                if(serverChargeState != null){
                    serverChargeState.attemptAddCharges(-charges);
                }
            }
        }
    }

    /**
     * Attempts to remove a charge from whatever item the parent entity currently has equipped
     * @param parent The parent
     */
    public void attemptAddCharges(int charges){
        this.setCharges(this.getCharges() + charges);
        Entity containingParent = ItemUtils.getContainingParent(this.parent);
        if(this.charges <= 0){
            ServerInventoryState.serverDestroyInventoryItem(this.parent);
        } else if(containingParent != null) {
            if(CreatureUtils.hasControllerPlayerId(containingParent)){
                //get the player
                int controllerPlayerID = CreatureUtils.getControllerPlayerId(containingParent);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(controllerPlayerID);
                //send message
                controllerPlayer.addMessage(SynchronizationMessage.constructUpdateClientIntStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID, FieldIdEnums.TREE_SERVERCHARGESTATE_SYNCEDFIELD_CHARGES_ID, this.getCharges()));
            }
        }
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
    public static ServerChargeState attachTree(Entity parent, Object ... params){
        ServerChargeState rVal = new ServerChargeState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERCHARGESTATE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID);
    }

    /**
     * <p>
     * Gets the ServerChargeState of the entity
     * </p>
     * @param entity the entity
     * @return The ServerChargeState
     */
    public static ServerChargeState getServerChargeState(Entity entity){
        return (ServerChargeState)entity.getData(EntityDataStrings.TREE_SERVERCHARGESTATE);
    }

    /**
     * <p>
     * Checks if the entity has a ServerChargeState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerChargeState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERCHARGESTATE);
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
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientIntStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID, FieldIdEnums.TREE_SERVERCHARGESTATE_SYNCEDFIELD_CHARGES_ID, charges));
        }
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

}
