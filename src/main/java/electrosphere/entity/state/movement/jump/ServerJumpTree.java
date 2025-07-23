package electrosphere.entity.state.movement.jump;


import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.server.entity.poseactor.PoseActor;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.engine.Globals;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.creature.movement.JumpMovementSystem;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.entity.state.movement.jump.ClientJumpTree.JumpState;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;

@SynchronizedBehaviorTree(name = "serverJumpTree", isServer = true, correspondingTree="clientJumpTree")
/*
Behavior tree for movement in an entity
*/
public class ServerJumpTree implements BehaviorTree {

    @SyncedField(serverSendTransitionPacket = true)
    JumpState state = JumpState.INACTIVE;

    @SyncedField
    int currentFrame = 0;

    @SyncedField
    float currentJumpForce = 0;

    String animationJump = "Armature|Jump";

    Entity parent;

    int jumpFrames = 0;
    float jumpForce = 10.0f;
    JumpMovementSystem jumpData;

    static final float jumpFalloff = 0.99f;

    public ServerJumpTree(Entity parent, Object ... params){
        this.parent = parent;
        this.jumpData = (JumpMovementSystem)params[0];
        this.jumpFrames = this.jumpData.getJumpFrames();
        this.jumpForce = this.jumpData.getJumpForce();
    }

    public void start(){
        if(state == JumpState.INACTIVE){
            this.setState(JumpState.ACTIVE);
            this.setCurrentFrame(0);
            this.setCurrentJumpForce(jumpForce);
            GravityUtils.serverAttemptActivateGravity(parent);
        }
    }

    /**
     * Interrupts the tree
     */
    public void interrupt(){
    }

    @Override
    public void simulate(float deltaTime) {
        PoseActor poseActor = EntityUtils.getPoseActor(parent);
        switch(state){
            case ACTIVE:
                if(poseActor != null){
                    if(!poseActor.isPlayingAnimation() || !poseActor.isPlayingAnimation(jumpData.getAnimationJump().getNameThirdPerson())){
                        poseActor.playAnimation(jumpData.getAnimationJump().getNameThirdPerson(),AnimationPriorities.getValue(AnimationPriorities.MOVEMENT_MODIFIER));
                        poseActor.incrementAnimationTime(0.0001);
                    }
                }
                this.setCurrentFrame(this.getCurrentFrame()+1);
                this.setCurrentJumpForce(currentJumpForce * jumpFalloff);
                //stop body falling if it is
                DBody body = PhysicsEntityUtils.getDBody(parent);
                if(body != null){
                    DVector3C linearVelocity = body.getLinearVel();
                    body.setLinearVel(linearVelocity.get0(), 0, linearVelocity.get2());
                    //push parent up
                    body.addForce(0, currentJumpForce, 0);
                    body.enable();
                }
                //potentially disable
                if(currentFrame >= jumpFrames){
                    this.setState(JumpState.AWAITING_LAND);
                    GravityUtils.serverAttemptActivateGravity(parent);
                }
            break;
            case INACTIVE:
            break;
            case AWAITING_LAND:
            break;
        }
    }

    /**
     * <p>
     * Gets the ServerJumpTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerJumpTree
     */
    public static ServerJumpTree getServerJumpTree(Entity entity){
        return (ServerJumpTree)entity.getData(EntityDataStrings.TREE_SERVERJUMPTREE);
    }

    public void land(){
        if(state != JumpState.INACTIVE && currentFrame > 2){
            this.setState(JumpState.INACTIVE);
        }
    }

    public boolean isJumping(){
        return state == JumpState.ACTIVE;
    }

    /**
     * Gets the jump data for the tree
     * @return The jump data
     */
    public JumpMovementSystem getJumpData(){
        return this.jumpData;
    }

    public void setAnimationJump(String animationName){
        animationJump = animationName;
    }
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public JumpState getState(){
        return state;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(JumpState state){
        this.state = state;
        int value = ClientJumpTree.getJumpStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructServerNotifyBTreeTransitionMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID, FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_STATE_ID, value));
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
    public static ServerJumpTree attachTree(Entity parent, Object ... params){
        ServerJumpTree rVal = new ServerJumpTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERJUMPTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets currentFrame.
     * </p>
     */
    public int getCurrentFrame(){
        return currentFrame;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets currentFrame and handles the synchronization logic for it.
     * </p>
     * @param currentFrame The value to set currentFrame to.
     */
    public void setCurrentFrame(int currentFrame){
        this.currentFrame = currentFrame;
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientIntStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID, FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTFRAME_ID, currentFrame));
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets currentJumpForce.
     * </p>
     */
    public float getCurrentJumpForce(){
        return currentJumpForce;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets currentJumpForce and handles the synchronization logic for it.
     * </p>
     * @param currentJumpForce The value to set currentJumpForce to.
     */
    public void setCurrentJumpForce(float currentJumpForce){
        this.currentJumpForce = currentJumpForce;
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientFloatStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID, FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTJUMPFORCE_ID, currentJumpForce));
        }
    }

    /**
     * <p>
     * Checks if the entity has a ServerJumpTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerJumpTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERJUMPTREE);
    }

}
