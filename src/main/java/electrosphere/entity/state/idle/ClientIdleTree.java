package electrosphere.entity.state.idle;


import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementTreeState;
import electrosphere.data.entity.creature.IdleData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.renderer.actor.Actor;



@SynchronizedBehaviorTree(name = "idle", isServer = false, correspondingTree="serverIdle")
/**
 * Tree for playing an idle animation when an entity isn't doing anything
 */
public class ClientIdleTree implements BehaviorTree {

    @SynchronizableEnum
    public static enum IdleTreeState {
        IDLE,
        NOT_IDLE,
    }

    @SyncedField
    private IdleTreeState state;

    Entity parent;
    IdleData idleData;

    /**
     * Creates an idle tree
     * @param e the entity to attach the tree to
     */
    private ClientIdleTree(Entity e, Object ... params){
        state = IdleTreeState.IDLE;
        parent = e;
        if(params.length > 0 && params[0] instanceof IdleData){
            idleData = (IdleData)params[0];
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public IdleTreeState getState(){
        return state;
    }


    public void interrupt(){
        state = IdleTreeState.NOT_IDLE;
    }

    public void stop(){
        state = IdleTreeState.NOT_IDLE;
    }

    @Override
    public void simulate(float deltaTime){
        Actor entityActor = EntityUtils.getActor(parent);

        //state machine
        switch(state){
            case IDLE:
                if(entityActor != null){
                    if(
                        idleData != null &&
                        (!entityActor.getAnimationData().isPlayingAnimation() || !entityActor.getAnimationData().isPlayingAnimation(idleData.getAnimation())) &&
                        (
                            Globals.assetManager.fetchModel(entityActor.getBaseModelPath()) != null &&
                            Globals.assetManager.fetchModel(entityActor.getBaseModelPath()).getAnimation(idleData.getAnimation().getNameThirdPerson()) != null
                        )

                    ){
                        entityActor.getAnimationData().playAnimation(idleData.getAnimation(),true);
                        entityActor.getAnimationData().incrementAnimationTime(0.0001);
                    }
                    if(idleData != null){
                        FirstPersonTree.conditionallyPlayAnimation(parent, idleData.getAnimation());
                    }
                }
                break;
            case NOT_IDLE:
                break;
        }
    }

    boolean movementTreeIsIdle(){
        boolean rVal = false;
        boolean hasMovementTree = parent.containsKey(EntityDataStrings.CLIENT_MOVEMENT_BT);
        if(hasMovementTree){
            BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(parent);
            if(movementTree instanceof ClientGroundMovementTree){
                if(((ClientGroundMovementTree)movementTree).getState() == MovementTreeState.IDLE){
                    rVal = true;
                }
            }
        } else {
            rVal = true;
        }
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_IDLE_ID);
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
    public static ClientIdleTree attachTree(Entity parent, Object ... params){
        ClientIdleTree rVal = new ClientIdleTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_IDLE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_IDLE_ID);
        return rVal;
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getIdleTreeStateEnumAsShort(IdleTreeState enumVal){
        switch(enumVal){
            case IDLE:
                return 0;
            case NOT_IDLE:
                return 1;
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
    public static IdleTreeState getIdleTreeStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return IdleTreeState.IDLE;
            case 1:
                return IdleTreeState.NOT_IDLE;
            default:
                return IdleTreeState.IDLE;
        }
    }
    /**
     * <p>
     * Gets the IdleTree of the entity
     * </p>
     * @param entity the entity
     * @return The IdleTree
     */
    public static ClientIdleTree getIdleTree(Entity entity){
        return (ClientIdleTree)entity.getData(EntityDataStrings.TREE_IDLE);
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(IdleTreeState state){
        this.state = state;
    }
    /**
     * <p>
     * Gets the ClientIdleTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientIdleTree
     */
    public static ClientIdleTree getClientIdleTree(Entity entity){
        return (ClientIdleTree)entity.getData(EntityDataStrings.TREE_IDLE);
    }
    /**
     * <p>
     * Checks if the entity has a ClientIdleTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientIdleTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_IDLE);
    }

}