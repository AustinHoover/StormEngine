package electrosphere.entity.state.movement.fall;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.creature.movement.FallMovementSystem;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.state.gravity.ServerGravityTree;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.server.entity.poseactor.PoseActor;

public class ServerFallTree implements BehaviorTree {

    static enum FallState {
        ACTIVE,
        INACTIVE,
    }

    //the raw data from disk
    FallMovementSystem fallMovementSystem;

    FallState state = FallState.INACTIVE;

    Entity parent;

    ServerJumpTree jumpTree;

    //The state transition util
    StateTransitionUtil stateTransitionUtil;

    /**
     * The number of frames this has been active
     */
    int frameCurrent = 0;

    /**
     * The minimum frames to wait before scanning if it should activate due to gravity
     */
    public static final int MIN_FRAMES_BEFORE_ACTIVATION_SCAN = 45;

    /**
     * The minimum frames to wait before playing landing animation on fall
     */
    public static final int MIN_FRAMES_BEFORE_LANDING_ANIM = 10;

    public ServerFallTree(Entity parent, FallMovementSystem fallMovementSystem){
        this.parent = parent;
        this.fallMovementSystem = fallMovementSystem;
        stateTransitionUtil = StateTransitionUtil.create(
            parent,
            true,
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
                if(ServerGravityTree.getServerGravityTree(parent) != null && !ServerGravityTree.getServerGravityTree(parent).isActive()){
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
                    this.frameCurrent = 0;
                }
                frameCurrent++;
            } break;
        }
    }

    /**
     * Starts the fall tree
     */
    public void start(){
        state = FallState.ACTIVE;
    }

    /**
     * Checks if the fall tree is active
     * @return true if is active, false otherwise
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
        PoseActor entityActor = EntityUtils.getPoseActor(parent);
        if(entityActor != null && ServerJumpTree.getServerJumpTree(parent) != null){
            isPlayingJump = entityActor.isPlayingAnimation(ServerJumpTree.getServerJumpTree(parent).getJumpData().getAnimationJump());
        }
        boolean rVal = 
        frameCurrent > MIN_FRAMES_BEFORE_ACTIVATION_SCAN &&
        ServerGravityTree.getServerGravityTree(parent).isActive() &&
        !isPlayingJump
        ;
        return rVal;
    }

    /**
     * Triggers the falling tree to land
     */
    public void land(){
        if(state != FallState.INACTIVE){
            state = FallState.INACTIVE;
            this.stateTransitionUtil.interrupt(FallState.ACTIVE);
            if(frameCurrent > MIN_FRAMES_BEFORE_LANDING_ANIM){
                PoseActor poseActor = EntityUtils.getPoseActor(parent);
                TreeDataAnimation animationToPlay = this.fallMovementSystem.getLandState().getAnimation();
                if(
                    !poseActor.isPlayingAnimation() || !poseActor.isPlayingAnimation(animationToPlay)
                ){
                    poseActor.playAnimation(animationToPlay);
                    poseActor.incrementAnimationTime(0.0001);
                }
            }
            frameCurrent = 0;
        }
    }

    public static ServerFallTree getFallTree(Entity parent){
        return (ServerFallTree)parent.getData(EntityDataStrings.FALL_TREE);
    }


    public void setServerJumpTree(ServerJumpTree jumpTree){
        this.jumpTree = jumpTree;
    }

    /**
     * Gets the current frame of the attack tree
     * @return The frame
     */
    public int getFrameCurrent(){
        return frameCurrent;
    }
    
}
