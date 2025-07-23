package electrosphere.entity.state.movement.fall;

import electrosphere.audio.movement.MovementAudioService.InteractionType;
import electrosphere.client.terrain.sampling.ClientVoxelSampler;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.creature.movement.FallMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.collidable.Impulse;
import electrosphere.entity.state.gravity.ClientGravityTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.renderer.actor.Actor;

/**
 * Behavior tree for playing animations when an entity is falling/landing
 */
public class ClientFallTree implements BehaviorTree {

    /**
     * The state of the fall tree
     */
    static enum FallState {
        ACTIVE,
        INACTIVE,
    }

    /**
     * the raw data from disk
     */
    FallMovementSystem fallMovementSystem;

    /**
     * current state
     */
    FallState state = FallState.INACTIVE;

    /**
     * the entity this is attached to
     */
    Entity parent;

    /**
     * the related jump tree
     */
    ClientJumpTree jumpTree;

    /**
     * The state transition util
     */
    StateTransitionUtil stateTransitionUtil;

    /**
     * The number of frames this has been active
     */
    int frameCurrent = 0;

    public ClientFallTree(Entity parent, FallMovementSystem fallMovementSystem){
        this.parent = parent;
        this.fallMovementSystem = fallMovementSystem;
        stateTransitionUtil = StateTransitionUtil.create(
            parent,
            false,
            new StateTransitionUtilItem[]{
                StateTransitionUtilItem.create(
                    FallState.ACTIVE,
                    fallMovementSystem.getFallState(),
                    null
                )
            }
        );
    }

    @Override
    public void simulate(float deltaTime) {
        switch(state){
            case ACTIVE: {
                if(ClientGravityTree.getClientGravityTree(parent) != null && !ClientGravityTree.getClientGravityTree(parent).isActive()){
                    this.land();
                    break;
                }
                if(frameCurrent > 0){
                    stateTransitionUtil.simulate(FallState.ACTIVE);
                }
                frameCurrent++;
            } break;
            case INACTIVE: {
                if(this.shouldStart()){
                    this.start();
                }
                frameCurrent++;
            } break;
        }
    }

    /**
     * Starts the falling tree
     */
    public void start(){
        state = FallState.ACTIVE;
    }

    /**
     * Returns the status of the fall tree
     * @return true if falling, false otherwise
     */
    public boolean isFalling(){
        return state == FallState.ACTIVE;
    }

    /**
     * Checks if the fall tree should activate
     * @return true if should activate, false otherwise
     */
    protected boolean shouldStart(){
        boolean isPlayingJump = false;
        Actor entityActor = EntityUtils.getActor(parent);
        if(entityActor != null && ClientJumpTree.getClientJumpTree(parent) != null){
            isPlayingJump = entityActor.getAnimationData().isPlayingAnimation(ClientJumpTree.getClientJumpTree(parent).getJumpData().getAnimationJump());
        }
        boolean rVal = 
        frameCurrent > ServerFallTree.MIN_FRAMES_BEFORE_ACTIVATION_SCAN &&
        ClientGravityTree.getClientGravityTree(parent).isActive() &&
        !isPlayingJump &&
        !this.hadGroundCollision()
        ;
        return rVal;
    }

    /**
     * Checks if the entity had a collision with the ground this frame
     * @return true if it had a collision with the ground, false otherwise
     */
    public boolean hadGroundCollision(){
        boolean rVal = false;
        if(PhysicsEntityUtils.getCollidable(parent) != null){
            Collidable collidable = PhysicsEntityUtils.getCollidable(parent);
            Impulse[] impulses = collidable.getImpulses();
            for(int i = 0; i < collidable.getImpulseCount(); i++){
                Impulse impulse = impulses[i];
                if(impulse.getType().equals(Collidable.TYPE_STATIC)){
                    rVal = true;
                    break;
                }
            }
        }
        return rVal;
    }

    /**
     * Triggers the falling tree to land
     */
    public void land(){
        if(state != FallState.INACTIVE){
            state = FallState.INACTIVE;
            this.stateTransitionUtil.interrupt(FallState.ACTIVE);
            if(frameCurrent > ServerFallTree.MIN_FRAMES_BEFORE_LANDING_ANIM){
                Actor entityActor = EntityUtils.getActor(parent);
                if(entityActor != null){
                    if(
                        !entityActor.getAnimationData().isPlayingAnimation() || !entityActor.getAnimationData().isPlayingAnimation(fallMovementSystem.getLandState().getAnimation().getNameThirdPerson())
                    ){
                        entityActor.getAnimationData().playAnimation(fallMovementSystem.getLandState().getAnimation().getNameThirdPerson(),AnimationPriorities.getValue(AnimationPriorities.MOVEMENT_MODIFIER));
                        entityActor.getAnimationData().incrementAnimationTime(0.0001);
                    }
                    FirstPersonTree.conditionallyPlayAnimation(parent, fallMovementSystem.getLandState().getAnimation());
                }
                if(parent == Globals.clientState.playerEntity && !Globals.controlHandler.cameraIsThirdPerson()){
                    //first person
                    Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.LAND, EntityUtils.getPosition(parent));
                    //play first person audio
                } else {
                    //play third person audio
                    Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.LAND, EntityUtils.getPosition(parent));
                }
            }
            frameCurrent = 0;
        }
    }

    /**
     * Gets the falling tree state of an entity
     * @param parent the entity
     * @return the state
     */
    public static ClientFallTree getFallTree(Entity parent){
        return (ClientFallTree)parent.getData(EntityDataStrings.FALL_TREE);
    }

    /**
     * Sets the related jump tree
     * @param jumpTree the jump tree that is related to this fall tree (on the same entity)
     */
    public void setJumpTree(ClientJumpTree jumpTree){
        this.jumpTree = jumpTree;
    }
    
}
