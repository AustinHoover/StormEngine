package electrosphere.entity.state.block;


import java.util.List;

import electrosphere.data.entity.creature.block.BlockSystem;
import electrosphere.data.entity.creature.block.BlockVariant;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;

import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.block.ClientBlockTree.BlockState;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;

@SynchronizedBehaviorTree(name = "serverBlockTree", isServer = true, correspondingTree="clientBlockTree")
/**
 * Server block tree
 */
public class ServerBlockTree implements BehaviorTree {

    @SyncedField(serverSendTransitionPacket = true)
    BlockState state = BlockState.NOT_BLOCKING; //the current state of the tree

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
    private ServerBlockTree(Entity parent, Object ... params){
        this.parent = parent;
        this.blockSystem = (BlockSystem)params[0];
        this.stateTransitionUtil = StateTransitionUtil.create(parent, true, new StateTransitionUtilItem[]{
            StateTransitionUtilItem.create(
                BlockState.WIND_UP,
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant != null){
                        return variant.getWindUpAnimation();
                    }
                    return null;
                },
                null,
                () -> {this.setState(BlockState.BLOCKING);}
            ),
            StateTransitionUtilItem.create(
                BlockState.BLOCKING,
                () -> {return this.blockSystem.getBlockVariant(this.currentBlockVariant).getMainAnimation();},
                null,
                null
            ),
            StateTransitionUtilItem.create(
                BlockState.COOLDOWN,
                () -> {
                    BlockVariant variant = this.blockSystem.getBlockVariant(this.currentBlockVariant);
                    if(variant != null){
                        return variant.getCooldownAnimation();
                    }
                    return null;
                },
                null,
                () -> {this.setState(BlockState.NOT_BLOCKING);}
            ),
        });
    }

    /**
     * Starts the block tree
     */
    public void start(){
        if(this.currentBlockVariant != null && this.blockSystem.getBlockVariant(this.currentBlockVariant) != null){
            this.stateTransitionUtil.reset();
            setState(BlockState.WIND_UP);
        }
    }

    /**
     * Stops the block tree
     */
    public void stop(){
        this.stateTransitionUtil.reset();
        setState(BlockState.COOLDOWN);
        //activate hitboxes
        if(AttachUtils.hasChildren(parent)){
            List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
            for(Entity currentAttached : attachedEntities){
                if(HitboxCollectionState.hasHitboxState(currentAttached)){
                    HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                    currentState.setActive(false);
                    currentState.setBlockOverride(false);
                }
            }
        }
    }

    @Override
    public void simulate(float deltaTime) {
        switch(state){
            case WIND_UP: {
                this.stateTransitionUtil.simulate(BlockState.WIND_UP);
            } break;
            case BLOCKING: {
                //activate hitboxes
                List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
                for(Entity currentAttached : attachedEntities){
                    if(HitboxCollectionState.hasHitboxState(currentAttached)){
                        HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                        currentState.setActive(true);
                        currentState.setBlockOverride(true);
                    }
                }
                this.stateTransitionUtil.simulate(BlockState.BLOCKING);
            } break;
            case COOLDOWN: {
                //activate hitboxes
                if(AttachUtils.hasChildren(parent)){
                    List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
                    for(Entity currentAttached : attachedEntities){
                        if(HitboxCollectionState.hasHitboxState(currentAttached)){
                            HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                            currentState.setActive(false);
                            currentState.setBlockOverride(false);
                        }
                    }
                }
                this.stateTransitionUtil.simulate(BlockState.COOLDOWN);
            } break;
            case NOT_BLOCKING: {

            } break;
        }
    }

    /**
     * Gets whether the tree is blocking or not
     * @return true if blocking, false otherwise
     */
    public boolean isBlocking(){
        return this.state == BlockState.BLOCKING;
    }

    /**
     * Checks if this tree is in any active state or not (ie is it playing an animation, blocking, etc)
     * @return true if no animation is happening, false otherwise
     */
    public boolean isIdle(){
        return this.state == BlockState.NOT_BLOCKING;
    }

    /**
     * Gets the block system data for this tree
     * @return the data if it exists, otherwise null
     */
    public BlockSystem getBlockSystem(){
        return this.blockSystem;
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
        int value = ClientBlockTree.getBlockStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructServerNotifyBTreeTransitionMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID, FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_STATE_ID, value));
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
    public static ServerBlockTree attachTree(Entity parent, Object ... params){
        ServerBlockTree rVal = new ServerBlockTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERBLOCKTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID);
    }
    /**
     * <p>
     * Gets the ServerBlockTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerBlockTree
     */
    public static ServerBlockTree getServerBlockTree(Entity entity){
        return (ServerBlockTree)entity.getData(EntityDataStrings.TREE_SERVERBLOCKTREE);
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
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStringStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID, FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_CURRENTBLOCKVARIANT_ID, currentBlockVariant));
        }
    }
    /**
     * <p>
     * Checks if the entity has a ServerBlockTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerBlockTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERBLOCKTREE);
    }

}
