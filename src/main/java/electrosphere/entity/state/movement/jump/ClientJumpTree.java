package electrosphere.entity.state.movement.jump;


import electrosphere.net.parser.net.message.SynchronizationMessage;

import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;

import electrosphere.audio.movement.MovementAudioService.InteractionType;
import electrosphere.client.terrain.sampling.ClientVoxelSampler;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.creature.movement.JumpMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.server.ServerSynchronizationManager;
import electrosphere.renderer.actor.Actor;

@SynchronizedBehaviorTree(
    name = "clientJumpTree",
    isServer = false,
    correspondingTree = "serverJumpTree",
    genStartInt = true
)
/*
Behavior tree for jumping
*/
public class ClientJumpTree implements BehaviorTree {

    /**
     * States for the jump tree
     */
    @SynchronizableEnum
    public static enum JumpState {
        INACTIVE,
        ACTIVE,
        AWAITING_LAND,
    }

    @SyncedField(serverSendTransitionPacket = true)
    JumpState state = JumpState.INACTIVE;

    @SyncedField
    int currentFrame = 0;

    @SyncedField
    float currentJumpForce = 0;

    String animationJump = "Armature|Jump";

    JumpMovementSystem jumpData;

    Entity parent;

    int jumpFrames = 0;
    float jumpForce = 10.0f;

    static final float jumpFalloff = 0.99f;

    public ClientJumpTree(Entity parent, Object ... params){
        this.parent = parent;
        this.jumpData = (JumpMovementSystem)params[0];
        this.jumpFrames = this.jumpData.getJumpFrames();
        this.jumpForce = this.jumpData.getJumpForce();
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
                BehaviorTreeIdEnums.BTREE_CLIENTJUMPTREE_ID,
                ServerSynchronizationManager.SERVER_SYNC_START
            )
        );
    }

    @Override
    public void simulate(float deltaTime) {
        Actor entityActor = EntityUtils.getActor(parent);
        switch(state){
            case ACTIVE: {
                if(entityActor != null){
                    if(!entityActor.getAnimationData().isPlayingAnimation() || !entityActor.getAnimationData().isPlayingAnimation(jumpData.getAnimationJump().getNameThirdPerson())){
                        entityActor.getAnimationData().playAnimation(jumpData.getAnimationJump().getNameThirdPerson(),AnimationPriorities.getValue(AnimationPriorities.MOVEMENT_MODIFIER));
                        entityActor.getAnimationData().incrementAnimationTime(0.0001);
                    }
                    FirstPersonTree.conditionallyPlayAnimation(parent, jumpData.getAnimationJump());
                }
                //stop body falling if it is
                DBody body = PhysicsEntityUtils.getDBody(parent);
                if(body != null){
                    DVector3C linearVelocity = body.getLinearVel();
                    body.setLinearVel(linearVelocity.get0(), 0, linearVelocity.get2());
                    //push parent up
                    body.addForce(0, currentJumpForce, 0);
                    body.enable();
                }
                if(currentFrame >= jumpFrames){
                    GravityUtils.clientAttemptActivateGravity(parent);
                }
            } break;
            case INACTIVE:
            break;
            case AWAITING_LAND:
            break;
        }
    }

    /**
     * <p>
     * Gets the ClientJumpTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientJumpTree
     */
    public static ClientJumpTree getClientJumpTree(Entity entity){
        return (ClientJumpTree)entity.getData(EntityDataStrings.TREE_CLIENTJUMPTREE);
    }

    public void land(){
        if(state != JumpState.INACTIVE && currentFrame > 2){
            this.setState(JumpState.INACTIVE);
        }
    }

    public boolean isJumping(){
        return state == JumpState.ACTIVE;
    }

    String determineCorrectAnimation(){
        return animationJump;
    }

    public void setAnimationJump(String animationName){
        animationJump = animationName;
    }
    
    /**
     * Gets the jump data for the tree
     * @return The jump data
     */
    public JumpMovementSystem getJumpData(){
        return this.jumpData;
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
    public static ClientJumpTree attachTree(Entity parent, Object ... params){
        ClientJumpTree rVal = new ClientJumpTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTJUMPTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTJUMPTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTJUMPTREE_ID);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getJumpStateEnumAsShort(JumpState enumVal){
        switch(enumVal){
            case INACTIVE:
                return 0;
            case ACTIVE:
                return 1;
            case AWAITING_LAND:
                return 2;
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
    public static JumpState getJumpStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return JumpState.INACTIVE;
            case 1:
                return JumpState.ACTIVE;
            case 2:
                return JumpState.AWAITING_LAND;
            default:
                return JumpState.INACTIVE;
        }
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
                BehaviorTreeIdEnums.BTREE_CLIENTJUMPTREE_ID,
                ServerSynchronizationManager.SERVER_SYNC_INTERRUPT
            )
        );
    }

    /**
     * <p> (Initially) Automatically Generated </p>
     * <p>
     * Performs a state transition on a client state variable.
     * Will be triggered when a server performs a state change.
     * </p>
     * @param newState The new value of the state
     */
    public void transitionState(JumpState newState){
        this.setState(newState);
        switch(newState){
            case INACTIVE: {
            } break;
            case ACTIVE: {
                if(parent == Globals.clientState.playerEntity && !Globals.controlHandler.cameraIsThirdPerson()){
                    Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.JUMP, EntityUtils.getPosition(parent));
                } else {
                    Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.JUMP, EntityUtils.getPosition(parent));
                }
            } break;
            case AWAITING_LAND: {
            } break;
        }
    }

    /**
     * <p>
     * Checks if the entity has a ClientJumpTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientJumpTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTJUMPTREE);
    }

}
