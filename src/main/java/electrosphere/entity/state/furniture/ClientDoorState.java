package electrosphere.entity.state.furniture;


import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.furniture.DoorData;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * State for controlling door furniture behavior
 */
@SynchronizedBehaviorTree(name = "clientDoor", isServer = false, correspondingTree="serverDoor")
public class ClientDoorState implements BehaviorTree {

    /**
     * Current state of the door
     */
    @SynchronizableEnum
    public static enum DoorState {
        /**
         * The door is already opened
         */
        OPEN,

        /**
         * The door is actively being opened
         */
        OPENING,

        /**
         * The door is already closed
         */
        CLOSED,

        /**
         * The door is actively closing
         */
        CLOSING,
    }

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
    private ClientDoorState(Entity e, Object ... params){
        parent = e;
        this.state = DoorState.CLOSED;
        this.doorData = (DoorData)params[0];
        this.stateTransitionUtil = StateTransitionUtil.create(parent, false, new StateTransitionUtilItem[]{
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
                    Globals.clientState.clientSceneWrapper.getCollisionEngine().destroyPhysics(this.parent);
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
                    Vector3d pos = EntityUtils.getPosition(this.parent);
                    Quaterniond rot = EntityUtils.getRotation(this.parent);
                    PhysicsEntityUtils.clientAttachCollidableTemplate(this.parent, PhysicsEntityUtils.getPhysicsTemplate(this.parent));
                    CollisionObjUtils.clientPositionCharacter(this.parent, pos, rot);
                }
            ),
        });
    }

    @Override
    public void simulate(float deltaTime){
        this.stateTransitionUtil.simulate(this.state);
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
    public static ClientDoorState attachTree(Entity parent, Object ... params){
        ClientDoorState rVal = new ClientDoorState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTDOOR, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTDOOR_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTDOOR_ID);
    }

    /**
     * <p>
     * Gets the ClientDoorState of the entity
     * </p>
     * @param entity the entity
     * @return The ClientDoorState
     */
    public static ClientDoorState getClientDoorState(Entity entity){
        return (ClientDoorState)entity.getData(EntityDataStrings.TREE_CLIENTDOOR);
    }

    /**
     * <p>
     * Checks if the entity has a ClientDoorState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientDoorState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTDOOR);
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

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts a short to the equivalent enum value
     * </p>
     * @param shortVal The short value
     * @return The enum value
     */
    public static DoorState getDoorStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return DoorState.OPEN;
            case 1:
                return DoorState.OPENING;
            case 2:
                return DoorState.CLOSED;
            case 3:
                return DoorState.CLOSING;
            default:
                return DoorState.OPEN;
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
    public static short getDoorStateEnumAsShort(DoorState enumVal){
        switch(enumVal){
            case OPEN:
                return 0;
            case OPENING:
                return 1;
            case CLOSED:
                return 2;
            case CLOSING:
                return 3;
            default:
                return 0;
        }
    }

}
