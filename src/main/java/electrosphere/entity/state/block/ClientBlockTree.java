package electrosphere.entity.state.block;


import electrosphere.data.entity.creature.block.BlockSystem;
import electrosphere.data.entity.creature.block.BlockVariant;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;

@SynchronizedBehaviorTree(name = "clientBlockTree", isServer = false, correspondingTree="serverBlockTree")
/**
 * Client block tree
 */
public class ClientBlockTree implements BehaviorTree {

    @SynchronizableEnum
    /**
     * The state of the block tree
     */
    public enum BlockState {
        WIND_UP,
        BLOCKING,
        COOLDOWN,
        NOT_BLOCKING,
    }

    @SyncedField(serverSendTransitionPacket = true)
    BlockState state = BlockState.NOT_BLOCKING; //the current state

    //the parent entity to this tree
    Entity parent;

    @SyncedField
    String currentBlockVariant = null; //The current block variant (depends on equipped items)

    //The data for block animations
    BlockSystem blockSystem;

    //The state transition util
    StateTransitionUtil stateTransitionUtil;

    /**
     * Constructor
     */
    private ClientBlockTree(Entity parent, Object ... params){
        this.parent = parent;
        this.blockSystem = (BlockSystem)params[0];
        this.stateTransitionUtil = StateTransitionUtil.create(parent, false, new StateTransitionUtilItem[]{
            StateTransitionUtilItem.create(
                BlockState.WIND_UP,
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant == null){
                        return null;
                    } else {
                        return variant.getWindUpAnimation();
                    }
                },
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant == null){
                        return null;
                    } else {
                        return variant.getWindUpAudio();
                    }
                },
                null
            ),
            StateTransitionUtilItem.create(
                BlockState.BLOCKING,
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant == null){
                        return null;
                    } else {
                        return variant.getMainAnimation();
                    }
                },
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant == null){
                        return null;
                    } else {
                        return variant.getMainAudio();
                    }
                },
                null
            ),
            StateTransitionUtilItem.create(
                BlockState.COOLDOWN,
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant == null){
                        return null;
                    } else {
                        return variant.getCooldownAnimation();
                    }
                },
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant == null){
                        return null;
                    } else {
                        return variant.getCooldownAudio();
                    }
                },
                null
            ),
        });
    }

    /**
     * <p> (Initially) Automatically Generated </p>
     * <p>
     * Performs a state transition on a client state variable.
     * Will be triggered when a server performs a state change.
     * </p>
     * @param newState The new value of the state
     */
    public void transitionState(BlockState newState){
        this.setState(newState);
        switch(newState){
            case WIND_UP: {
            } break;
            case BLOCKING: {
                this.stateTransitionUtil.interrupt(BlockState.WIND_UP);
            } break;
            case COOLDOWN: {
                this.stateTransitionUtil.interrupt(BlockState.BLOCKING);
            } break;
            case NOT_BLOCKING: {
                this.stateTransitionUtil.interrupt(BlockState.COOLDOWN);
            } break;
        }
    }

    @Override
    public void simulate(float deltaTime) {
        switch(state){
            case WIND_UP: {
                this.stateTransitionUtil.simulate(BlockState.WIND_UP);
            } break;
            case BLOCKING: {
                this.stateTransitionUtil.simulate(BlockState.BLOCKING);
            } break;
            case COOLDOWN: {
                this.stateTransitionUtil.simulate(BlockState.COOLDOWN);
            } break;
            case NOT_BLOCKING: {

            } break;
        }
    }

    /**
     * returns if the client block tree is active or not
     * @return true if active, false otherwise
     */
    public boolean isActive(){
        return this.state != BlockState.NOT_BLOCKING;
    }

    
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public BlockState getState(){
        return state;
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(BlockState state){
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
    public static ClientBlockTree attachTree(Entity parent, Object ... params){
        ClientBlockTree rVal = new ClientBlockTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTBLOCKTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTBLOCKTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTBLOCKTREE_ID);
    }
    /**
     * <p>
     * Gets the ClientBlockTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientBlockTree
     */
    public static ClientBlockTree getClientBlockTree(Entity entity){
        return (ClientBlockTree)entity.getData(EntityDataStrings.TREE_CLIENTBLOCKTREE);
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getBlockStateEnumAsShort(BlockState enumVal){
        switch(enumVal){
            case WIND_UP:
                return 0;
            case BLOCKING:
                return 1;
            case COOLDOWN:
                return 2;
            case NOT_BLOCKING:
                return 3;
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
    public static BlockState getBlockStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return BlockState.WIND_UP;
            case 1:
                return BlockState.BLOCKING;
            case 2:
                return BlockState.COOLDOWN;
            case 3:
                return BlockState.NOT_BLOCKING;
            default:
                return BlockState.WIND_UP;
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets currentBlockVariant.
     * </p>
     */
    public String getCurrentBlockVariant(){
        return currentBlockVariant;
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets currentBlockVariant and handles the synchronization logic for it.
     * </p>
     * @param currentBlockVariant The value to set currentBlockVariant to.
     */
    public void setCurrentBlockVariant(String currentBlockVariant){
        this.currentBlockVariant = currentBlockVariant;
    }

    /**
     * <p>
     * Checks if the entity has a ClientBlockTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientBlockTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTBLOCKTREE);
    }

}
