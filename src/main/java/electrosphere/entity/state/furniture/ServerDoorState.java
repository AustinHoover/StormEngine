package electrosphere.entity.state.furniture;


import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.furniture.DoorData;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.state.furniture.ClientDoorState.DoorState;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * State for controlling door furniture behavior
 */
@SynchronizedBehaviorTree(name = "serverDoor", isServer = true, correspondingTree="clientDoor")
public class ServerDoorState implements BehaviorTree {

    /**
     * The current state of the door
     */
    @SyncedField
    DoorState state;

    /**
     * The parent entity
     */
    Entity parent;

    /**
     * The data for the door behavior
     */
    DoorData doorData;

    /**
     * The state transition util
     */
    StateTransitionUtil stateTransitionUtil;

    /**
     * Constructor
     * @param e
     * @param params
     */
    private ServerDoorState(Entity e, Object ... params){
        parent = e;
        this.state = DoorState.CLOSED;
        this.doorData = (DoorData)params[0];
        this.stateTransitionUtil = StateTransitionUtil.create(parent, true, new StateTransitionUtilItem[]{
            StateTransitionUtilItem.create(
                DoorState.OPEN,
                doorData.getOpen(),
                true
            ),
            StateTransitionUtilItem.create(
                DoorState.OPENING,
                doorData.getOpening(),
                () -> {
                    this.setState(DoorState.OPEN);
                    Realm parentRealm = Globals.serverState.realmManager.getEntityRealm(this.parent);
                    parentRealm.getCollisionEngine().destroyPhysics(this.parent);
                }
            ),
            StateTransitionUtilItem.create(
                DoorState.CLOSED,
                doorData.getClosed(),
                true
            ),
            StateTransitionUtilItem.create(
                DoorState.CLOSING,
                doorData.getClosing(),
                () -> {
                    this.setState(DoorState.CLOSED);
                    Realm parentRealm = Globals.serverState.realmManager.getEntityRealm(this.parent);
                    Vector3d pos = EntityUtils.getPosition(this.parent);
                    PhysicsEntityUtils.serverAttachCollidableTemplate(parentRealm, this.parent, PhysicsEntityUtils.getPhysicsTemplate(this.parent), pos);
                    ServerEntityUtils.repositionEntity(this.parent, pos);
                }
            ),
        });
    }

    @Override
    public void simulate(float deltaTime){
        this.stateTransitionUtil.simulate(this.state);
    }

    /**
     * Tries interacting with the door
     */
    public void interact(){
        switch(this.state){
            case OPEN: {
                this.setState(DoorState.CLOSING);
            } break;
            case CLOSED: {
                this.setState(DoorState.OPENING);
            } break;
            case OPENING:
            case CLOSING:
            //silently ignore
            break;
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
    public static ServerDoorState attachTree(Entity parent, Object ... params){
        ServerDoorState rVal = new ServerDoorState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERDOOR, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID);
    }

    /**
     * <p>
     * Gets the ServerDoorState of the entity
     * </p>
     * @param entity the entity
     * @return The ServerDoorState
     */
    public static ServerDoorState getServerDoorState(Entity entity){
        return (ServerDoorState)entity.getData(EntityDataStrings.TREE_SERVERDOOR);
    }

    /**
     * <p>
     * Checks if the entity has a ServerDoorState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerDoorState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERDOOR);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(DoorState state){
        this.state = state;
        int value = ClientDoorState.getDoorStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID, FieldIdEnums.TREE_SERVERDOOR_SYNCEDFIELD_STATE_ID, value));
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public DoorState getState(){
        return state;
    }

}
