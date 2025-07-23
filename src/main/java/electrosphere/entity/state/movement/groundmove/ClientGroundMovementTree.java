package electrosphere.entity.state.movement.groundmove;


import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.audio.movement.MovementAudioService.InteractionType;
import electrosphere.client.terrain.sampling.ClientVoxelSampler;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.creature.movement.GroundMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.movement.fall.ClientFallTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree.JumpState;
import electrosphere.entity.state.movement.sprint.ClientSprintTree;
import electrosphere.entity.state.movement.walk.ClientWalkTree;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.renderer.anim.Animation;
import electrosphere.util.math.SpatialMathUtils;
import electrosphere.renderer.actor.Actor;

import java.util.concurrent.CopyOnWriteArrayList;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;

@SynchronizedBehaviorTree(name = "clientGroundMovementTree", isServer = false, correspondingTree="serverGroundMovementTree")
/*
Behavior tree for movement in an entity
*/
public class ClientGroundMovementTree implements BehaviorTree {
    
    /**
     * The state of the ground movement tree
     */
    public static enum MovementTreeState {
        STARTUP,
        MOVE,
        SLOWDOWN,
        IDLE,
    }

    /**
     * The relative facing of the character to its rotation
     * (ie is it strafing, moveing straight forward, backpedaling, etc)
     */
    @SynchronizableEnum
    public static enum MovementRelativeFacing {
        FORWARD,
        LEFT,
        RIGHT,
        BACKWARD,
        FORWARD_LEFT,
        FORWARD_RIGHT,
        BACKWARD_LEFT,
        BACKWARD_RIGHT,
    }
    
    static final double STATE_DIFFERENCE_HARD_UPDATE_THRESHOLD = 1.0;
    static final double STATE_DIFFERENCE_SOFT_UPDATE_THRESHOLD = 0.1;
    static final double SOFT_UPDATE_MULTIPLIER = 0.3;
    static final double STATE_DIFFERENCE_CREEP_MULTIPLIER = 0.001; //while the movement tree is idle, slowly creep the position of the entity towards the true server position by this amount
    static final double STATE_DIFFERENCE_CREEP_CUTOFF = 0.01; //the cutoff for creep when we say it's "close enough"
    
    String animationStartUp = Animation.ANIMATION_MOVEMENT_STARTUP;
    String animationMain = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSlowDown = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSprintStart = Animation.ANIMATION_SPRINT_STARTUP;
    String animationSprint = Animation.ANIMATION_SPRINT;
    String animationSprintWindDown = Animation.ANIMATION_SPRINT_WINDDOWN;
    
    MovementTreeState state;
    @SyncedField
    MovementRelativeFacing facing = MovementRelativeFacing.FORWARD;
    
    ClientSprintTree sprintTree;
    ClientJumpTree jumpTree;
    ClientFallTree fallTree;

    GroundMovementSystem groundMovementData;
    
    Entity parent;

    Collidable collidable;
    
    CopyOnWriteArrayList<EntityMessage> networkMessageQueue = new CopyOnWriteArrayList<EntityMessage>();
    
    //the last frame we got an update on true position from the server
    long lastUpdateTime = 0;
    //the last position reported by the server
    Vector3d lastServerPosition = null;

    //the vector controling the direction the entity will move in
    Vector3d movementVector = new Vector3d(1,0,0);

    //Tracks whether footstep audio has been played or not
    boolean playedFootstepFirst = false;
    boolean playedFootstepSecond = false;
    
    /**
     * Constructor
     * @param e The parent entity
     */
    private ClientGroundMovementTree(Entity e, Object ... params){
        //Collidable collidable, GroundMovementSystem groundMovementData
        if(params.length < 2){
            throw new IllegalArgumentException("Tried to create a client ground movement tree without providing both mandatory parameters");
        }
        state = MovementTreeState.IDLE;
        parent = e;
        this.collidable = (Collidable)params[0];
        this.groundMovementData = (GroundMovementSystem)params[1];
    }
    
    /**
     * Gets the state of the tree
     * @return The state
     */
    public MovementTreeState getState(){
        return state;
    }
    
    /**
     * Requests to the server that the entity start moving
     * @param facing The facing relative to the view direction that the entity should move in (ie strafe right vs walk straight forward)
     */
    public void start(MovementRelativeFacing facing){
        if(canStartMoving()){
            setFacing(facing);
            state = MovementTreeState.STARTUP;
            //if we aren't the server, alert the server we intend to walk forward
            Vector3d position = EntityUtils.getPosition(parent);
            Quaterniond rotation = EntityUtils.getRotation(parent);
            float velocity = CreatureUtils.getVelocity(parent);
            if(this.parent == Globals.clientState.playerEntity){
                Globals.clientState.clientConnection.queueOutgoingMessage(
                    EntityMessage.constructmoveUpdateMessage(
                        Globals.clientState.clientSceneWrapper.mapClientToServerId(parent.getId()),
                        Globals.engineState.timekeeper.getNumberOfSimFramesElapsed(),
                        position.x,
                        position.y,
                        position.z,
                        rotation.x,
                        rotation.y,
                        rotation.z,
                        rotation.w,
                        velocity,
                        ClientGroundMovementTree.getMovementRelativeFacingEnumAsShort(facing),
                        0 //magic number corresponding to state startup
                    )
                );
            }
        }
    }
    
    /**
     * Requests to the server that the movetree stop
     */
    public void slowdown(){
        state = MovementTreeState.SLOWDOWN;
        //if we aren't the server, alert the server we intend to slow down
        Vector3d position = EntityUtils.getPosition(parent);
        Quaterniond rotation = EntityUtils.getRotation(parent);
        float velocity = CreatureUtils.getVelocity(parent);
        if(this.parent == Globals.clientState.playerEntity){
            Globals.clientState.clientConnection.queueOutgoingMessage(
                EntityMessage.constructmoveUpdateMessage(
                    Globals.clientState.clientSceneWrapper.mapClientToServerId(parent.getId()),
                    Globals.engineState.timekeeper.getNumberOfSimFramesElapsed(),
                    position.x,
                    position.y,
                    position.z,
                    rotation.x,
                    rotation.y,
                    rotation.z,
                    rotation.w,
                    velocity,
                    ClientGroundMovementTree.getMovementRelativeFacingEnumAsShort(facing),
                    2 //magic number corresponding to state slowdown
                )
            );
        }
    }
    
    @Override
    public void simulate(float deltaTime){
        Actor entityActor = EntityUtils.getActor(parent);
        Vector3d position = EntityUtils.getPosition(parent);
        Vector3d facingVector = CreatureUtils.getFacingVector(parent);
        float maxNaturalVelocity = ServerGroundMovementTree.getMaximumVelocity(parent, this.groundMovementData, facing);
        DBody body = PhysicsEntityUtils.getDBody(parent);
        DVector3C linearVelocity = null;

        //body can be null if the behavior tree wasn't detatched for some reason
        if(body != null){
            linearVelocity = body.getLinearVel();
        }

        //
        //rotation update
        if(this.state != MovementTreeState.IDLE){
            this.movementVector.set(facingVector);
            switch(facing){
                case FORWARD:
                    movementVector.normalize();
                break;
                case LEFT:
                    movementVector.rotateY((float)(90 * Math.PI / 180)).normalize();
                break;
                case RIGHT:
                    movementVector.rotateY((float)(-90 * Math.PI / 180)).normalize();
                break;
                case BACKWARD:
                    movementVector.x = -movementVector.x;
                    movementVector.z = -movementVector.z;
                    movementVector.normalize();
                break;
                case FORWARD_LEFT:
                    movementVector.rotateY((float)(45 * Math.PI / 180)).normalize();
                break;
                case FORWARD_RIGHT:
                    movementVector.rotateY((float)(-45 * Math.PI / 180)).normalize();
                break;
                case BACKWARD_LEFT:
                    movementVector.rotateY((float)(135 * Math.PI / 180)).normalize();
                break;
                case BACKWARD_RIGHT:
                    movementVector.rotateY((float)(-135 * Math.PI / 180)).normalize();
                break;
            }
        }
        Quaterniond movementQuaternion = new Quaterniond().rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(facingVector.x,0,facingVector.z)).normalize();
        Quaterniond rotation = EntityUtils.getRotation(parent);
        
        //parse attached network messages
        for(EntityMessage message : networkMessageQueue){
            networkMessageQueue.remove(message);
            long updateTime = message.gettime();
//            System.out.println("MOVE to " + message.getX() + " " + message.getY() + " " + message.getZ());
            switch(message.getMessageSubtype()){
                case MOVEUPDATE:
                    if(updateTime >= lastUpdateTime){
                        lastUpdateTime = updateTime;
                        switch(message.gettreeState()){
                            case 0:
                                state = MovementTreeState.STARTUP;
                            //    System.out.println("Set state STARTUP");
                                GravityUtils.clientAttemptActivateGravity(parent);
                                break;
                            case 1:
                                state = MovementTreeState.MOVE;
                            //    System.out.println("Set state MOVE");
                                GravityUtils.clientAttemptActivateGravity(parent);
                                break;
                            case 2:
                                state = MovementTreeState.SLOWDOWN;
                            //    System.out.println("Set state SLOWDOWN");
                                GravityUtils.clientAttemptActivateGravity(parent);
                                break;
                            case 3:
                                state = MovementTreeState.IDLE;
                            //    System.out.println("Set state IDLE");
                                break;
                        }
                        //this should only fire on the client, we don't want the server snap updating due to client position reporting
                        lastServerPosition = new Vector3d(message.getpositionX(),message.getpositionY(),message.getpositionZ());
                        if(position.distance(lastServerPosition) > STATE_DIFFERENCE_HARD_UPDATE_THRESHOLD){
                            EntityUtils.setPosition(parent, lastServerPosition);
                        } else if(position.distance(lastServerPosition) > STATE_DIFFERENCE_SOFT_UPDATE_THRESHOLD){
                            EntityUtils.getPosition(parent).lerp(lastServerPosition,SOFT_UPDATE_MULTIPLIER);
                        }
                        //we want to always update the server facing vector with where the client says they're facing
                        EntityUtils.getRotation(parent).set(message.getrotationX(),message.getrotationY(),message.getrotationZ(),message.getrotationW());
                        CollisionObjUtils.clientPositionCharacter(parent, position, rotation);
                        // CreatureUtils.setFacingVector(parent, new Vector3d(message.getrotationX(),message.getrotationY(),message.getrotationZ()));
                        break;
                    }
                    break;
                default:
                break;
            }
            Globals.clientState.clientConnection.release(message);
        }

        // System.out.println(movementVector + " " + velocity * Main.deltaTime);
        
        //state machine
        switch(state){
            case STARTUP: {
                //update rotation
                rotation.set(movementQuaternion);
                //play animation
                String animationToPlay = determineCorrectAnimation(MovementTreeState.STARTUP);
                if(entityActor != null){
                    if(!entityActor.getAnimationData().isPlayingAnimation(animationToPlay)){
                        entityActor.getAnimationData().playAnimation(animationToPlay,AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                        entityActor.getAnimationData().incrementAnimationTime(0.0001);
                        //reset footstep tracking
                        this.playedFootstepFirst = false;
                        this.playedFootstepSecond = false;
                    }
                    FirstPersonTree.conditionallyPlayAnimation(parent, groundMovementData.getAnimationStartup().getNameFirstPerson(), AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                }
                //conditionally play footstep audio
                this.playFootstepAudio(0,entityActor.getAnimationData().getAnimationTime(animationToPlay),position);

                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                //check if can transition state
                if(velocity >= maxNaturalVelocity){
                    velocity = maxNaturalVelocity;
                    state = MovementTreeState.MOVE;
                }
                CreatureUtils.setVelocity(parent, velocity);
                //actually update
                if(body != null){
                    PhysicsEntityUtils.getDBody(parent).enable();
                    body.setLinearVel(
                        movementVector.x * velocity * Globals.engineState.timekeeper.getSimFrameTime(),
                        linearVelocity.get1(),
                        movementVector.z * velocity * Globals.engineState.timekeeper.getSimFrameTime()
                    );
                    body.setAngularVel(0, 0, 0);
                }
                rotation.set(movementQuaternion);
                
                GravityUtils.clientAttemptActivateGravity(parent);
            } break;
            case MOVE: {
                //update rotation
                rotation.set(movementQuaternion);
                //play animation
                String animationToPlay = determineCorrectAnimation(MovementTreeState.MOVE);
                if(entityActor != null){
                    if(!entityActor.getAnimationData().isPlayingAnimation(animationToPlay)){
                        entityActor.getAnimationData().playAnimation(animationToPlay,AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                        entityActor.getAnimationData().incrementAnimationTime(0.0001);
                        //reset footstep tracking
                        this.playedFootstepFirst = false;
                        this.playedFootstepSecond = false;
                    }
                    FirstPersonTree.conditionallyPlayAnimation(parent, groundMovementData.getAnimationLoop().getNameFirstPerson(), AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                }

                //conditionally play footstep audio
                this.playFootstepAudio(0,entityActor.getAnimationData().getAnimationTime(animationToPlay),position);

                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                if(body != null){
                    PhysicsEntityUtils.getDBody(parent).enable();
                    body.setLinearVel(
                        movementVector.x * velocity * Globals.engineState.timekeeper.getSimFrameTime(),
                        linearVelocity.get1(),
                        movementVector.z * velocity * Globals.engineState.timekeeper.getSimFrameTime()
                    );
                    body.setAngularVel(0, 0, 0);
                }
                rotation.set(movementQuaternion);
                
                GravityUtils.clientAttemptActivateGravity(parent);
            } break;
            case SLOWDOWN: {
                //update rotation
                rotation.set(movementQuaternion);
                //run slowdown code
                String animationToPlay = determineCorrectAnimation(MovementTreeState.SLOWDOWN);
                if(entityActor != null){
                    //play animations
                    if(!entityActor.getAnimationData().isPlayingAnimation(animationToPlay)){
                        entityActor.getAnimationData().playAnimation(animationToPlay,AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                        entityActor.getAnimationData().incrementAnimationTime(0.0001);
                        //reset footstep tracking
                        this.playedFootstepFirst = false;
                        this.playedFootstepSecond = false;
                    }
                    FirstPersonTree.conditionallyPlayAnimation(parent, groundMovementData.getAnimationWindDown().getNameFirstPerson(), AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                    if(entityActor.getAnimationData().isPlayingAnimation(determineCorrectAnimation(MovementTreeState.MOVE))){
                        entityActor.getAnimationData().stopAnimation(determineCorrectAnimation(MovementTreeState.MOVE));
                    }
                }
                //conditionally play footstep audio
                this.playFootstepAudio(0,entityActor.getAnimationData().getAnimationTime(animationToPlay),position);

                //velocity stuff
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                //check if can transition state
                if(velocity <= 0){
                    velocity = 0;
                    state = MovementTreeState.IDLE;
                    if(entityActor != null){
                        animationToPlay = determineCorrectAnimation(MovementTreeState.SLOWDOWN);
                        if(entityActor.getAnimationData().isPlayingAnimation() && entityActor.getAnimationData().isPlayingAnimation(animationToPlay)){
                            entityActor.getAnimationData().stopAnimation(animationToPlay);
                        }
                    }
                    CreatureUtils.setVelocity(parent, velocity);
                } else {
                    GravityUtils.clientAttemptActivateGravity(parent);
                }
                if(body != null){
                    PhysicsEntityUtils.getDBody(parent).enable();
                    body.setLinearVel(
                        movementVector.x * velocity * Globals.engineState.timekeeper.getSimFrameTime(),
                        linearVelocity.get1(),
                        movementVector.z * velocity * Globals.engineState.timekeeper.getSimFrameTime()
                    );
                    body.setAngularVel(0, 0, 0);
                }
                rotation.set(movementQuaternion);
                
            } break;
            case IDLE: {
                Vector3d playerPos = EntityUtils.getPosition(parent);
                if(lastServerPosition != null && lastServerPosition.distance(playerPos) > STATE_DIFFERENCE_CREEP_CUTOFF){
                    playerPos.lerp(lastServerPosition,STATE_DIFFERENCE_CREEP_MULTIPLIER);
                }
            } break;
        }
    }

    public void addNetworkMessage(EntityMessage networkMessage) {
        networkMessageQueue.add(networkMessage);
    }
    
    public boolean canStartMoving(){
        boolean rVal = true;
        return rVal;
    }

    public void setAnimationStartUp(String animationStartUp) {
        this.animationStartUp = animationStartUp;
    }

    public void setAnimationMain(String animationMain) {
        this.animationMain = animationMain;
    }

    public void setAnimationSlowDown(String animationSlowDown) {
        this.animationSlowDown = animationSlowDown;
    }
    
    public void setAnimationSprintStartUp(String animationSprintStartUp){
        this.animationSprintStart = animationSprintStartUp;
    }
    
    public void setAnimationSprint(String animationSprint){
        this.animationSprint = animationSprint;
    }
    
    public void setAnimationSprintWindDown(String animationSprintWindDown){
        this.animationSprintWindDown = animationSprintWindDown;
    }
    
    public void setSprintTree(ClientSprintTree sprintTree){
        this.sprintTree = sprintTree;
    }

    public void setClientJumpTree(ClientJumpTree jumpTree){
        this.jumpTree = jumpTree;
    }

    public void setClientFallTree(ClientFallTree fallTree){
        this.fallTree = fallTree;
    }

    /**
     * Updates the velocity and acceleration
     */
    private void updateVelocity(){
        float velocity = CreatureUtils.getVelocity(parent);
        float acceleration = CreatureUtils.getAcceleration(parent);
        float maxNaturalVelocity = ServerGroundMovementTree.getMaximumVelocity(parent, this.groundMovementData, facing);
        switch(this.state){
            case IDLE: {
            } break;
            case STARTUP: {
                //run startup code
                velocity = velocity + acceleration * (float)Globals.engineState.timekeeper.getSimFrameTime();
                CreatureUtils.setVelocity(parent, velocity);
            } break;
            case MOVE: {
                if(velocity != maxNaturalVelocity){
                    velocity = maxNaturalVelocity;
                    CreatureUtils.setVelocity(parent, velocity);
                }
            } break;
            case SLOWDOWN: {
                //velocity stuff
                velocity = velocity - acceleration * (float)Globals.engineState.timekeeper.getSimFrameTime();
                CreatureUtils.setVelocity(parent, velocity);
            } break;
        }
    }

    /**
     * Gets the velocity to move at
     * @return The velocity
     */
    private float getModifiedVelocity(){
        float velocity = CreatureUtils.getVelocity(parent);
        float sprintModifier = 1.0f;
        float walkModifier = 1.0f;
        float attackModifier = 1.0f;
        if(ClientWalkTree.getClientWalkTree(parent) != null && ClientWalkTree.getClientWalkTree(parent).isWalking()){
            walkModifier = ClientWalkTree.getClientWalkTree(parent).getModifier();
        }
        if(ClientSprintTree.getClientSprintTree(parent) != null && ClientSprintTree.getClientSprintTree(parent).isSprinting()){
            sprintModifier = ClientSprintTree.getClientSprintTree(parent).getSprintSystem().getModifier();
        }
        if(ClientAttackTree.getClientAttackTree(parent) != null){
            attackModifier = (float)ClientAttackTree.getClientAttackTree(parent).getMovementPenalty();
        }
        return velocity * sprintModifier * walkModifier * attackModifier;
    }

    /**
     * Conditionally plays audio for footsteps
     * @param voxelType The voxel type
     * @param animationTime The time of the current animation
     * @param position The position of the parent entity
     */
    private void playFootstepAudio(int voxelType, double animationTime, Vector3d position){
        Float firstOffset = this.groundMovementData.getFootstepFirstAudioOffset();
        Float secondOffset = this.groundMovementData.getFootstepSecondAudioOffset();
        if(
            this.shouldPlayFootsteps() &&
            !this.playedFootstepFirst &&
            firstOffset != null &&
            animationTime > firstOffset
        ){
            this.playedFootstepFirst = true;
            if(parent == Globals.clientState.playerEntity && !Globals.controlHandler.cameraIsThirdPerson()){
                Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.STEP_BARE_REG, position);
            } else {
                Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.STEP_BARE_REG, position);
            }
        }
        if(
            this.shouldPlayFootsteps() &&
            !this.playedFootstepSecond &&
            secondOffset != null &&
            animationTime > secondOffset
        ){
            this.playedFootstepSecond = true;
            if(parent == Globals.clientState.playerEntity && !Globals.controlHandler.cameraIsThirdPerson()){
                Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.STEP_BARE_REG, position);
            } else {
                Globals.audioEngine.movementAudioService.playAudioPositional(ClientVoxelSampler.getVoxelTypeBeneathEntity(parent), InteractionType.STEP_BARE_REG, position);
            }
        }
        if(firstOffset == null || secondOffset == null){
            LoggerInterface.loggerAudio.WARNING("Footstep offset undefined for creature! " + CreatureUtils.getType(parent));
        }
    }

    /**
     * Checks if this entity should play footstep sound effects
     * @return true if should play footsteps, false otherwise
     */
    private boolean shouldPlayFootsteps(){
        if(ClientJumpTree.getClientJumpTree(parent) != null){
            ClientJumpTree jumpTree = ClientJumpTree.getClientJumpTree(parent);
            if(jumpTree.getState() != JumpState.INACTIVE){
                return false;
            }
        }
        return true;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets facing.
     * </p>
     */
    public MovementRelativeFacing getFacing(){
        return facing;
    }

    public String determineCorrectAnimation(MovementTreeState state){
        String rVal = "";
        if(sprintTree != null){
            switch(sprintTree.getState()){
                case SPRINTING:
                switch(state){
                    case IDLE:
                    break;
                    case STARTUP:
                    rVal = animationSprintStart;
                    break;
                    case MOVE:
                    rVal = animationSprint;
                    break;
                    case SLOWDOWN:
                    rVal = animationSprintWindDown;
                    break;
                }
                break;
                case NOT_SPRINTING:
                switch(state){
                    case IDLE:
                    break;
                    case STARTUP:
                    switch(facing){
                        case FORWARD:
                        rVal = animationStartUp;
                        break;
                        case BACKWARD:
                        rVal = animationStartUp;
                        break;
                        case LEFT:
                        rVal = Animation.ANIMATION_WALK_LEFT;
                        break;
                        case RIGHT:
                        rVal = Animation.ANIMATION_WALK_RIGHT;
                        break;
                        case FORWARD_LEFT:
                        rVal = animationStartUp;
                        break;
                        case FORWARD_RIGHT:
                        rVal = animationStartUp;
                        break;
                        case BACKWARD_LEFT:
                        rVal = Animation.ANIMATION_WALK_LEFT;
                        break;
                        case BACKWARD_RIGHT:
                        rVal = Animation.ANIMATION_WALK_RIGHT;
                        break;
                    }
                    break;
                    case MOVE:
                    switch(facing){
                        case FORWARD:
                        rVal = animationMain;
                        break;
                        case BACKWARD:
                        rVal = animationMain;
                        break;
                        case LEFT:
                        rVal = Animation.ANIMATION_WALK_LEFT;
                        break;
                        case RIGHT:
                        rVal = Animation.ANIMATION_WALK_RIGHT;
                        break;
                        case FORWARD_LEFT:
                        rVal = animationMain;
                        break;
                        case FORWARD_RIGHT:
                        rVal = animationMain;
                        break;
                        case BACKWARD_LEFT:
                        rVal = Animation.ANIMATION_WALK_LEFT;
                        break;
                        case BACKWARD_RIGHT:
                        rVal = Animation.ANIMATION_WALK_RIGHT;
                        break;
                    }
                    break;
                    case SLOWDOWN:
                    switch(facing){
                        case FORWARD:
                        rVal = animationSlowDown;
                        break;
                        case BACKWARD:
                        rVal = animationSlowDown;
                        break;
                        case LEFT:
                        rVal = Animation.ANIMATION_WALK_LEFT;
                        break;
                        case RIGHT:
                        rVal = Animation.ANIMATION_WALK_RIGHT;
                        break;
                        case FORWARD_LEFT:
                        rVal = animationSlowDown;
                        break;
                        case FORWARD_RIGHT:
                        rVal = animationSlowDown;
                        break;
                        case BACKWARD_LEFT:
                        rVal = Animation.ANIMATION_WALK_LEFT;
                        break;
                        case BACKWARD_RIGHT:
                        rVal = Animation.ANIMATION_WALK_RIGHT;
                        break;
                    }
                    break;
                }
                break;
            }
        } else {
            switch(state){
                case IDLE:
                break;
                case STARTUP:
                switch(facing){
                    case FORWARD:
                    rVal = animationStartUp;
                    break;
                    case BACKWARD:
                    rVal = animationStartUp;
                    break;
                    case LEFT:
                    rVal = Animation.ANIMATION_WALK_LEFT;
                    break;
                    case RIGHT:
                    rVal = Animation.ANIMATION_WALK_RIGHT;
                    break;
                    case FORWARD_LEFT:
                    rVal = animationStartUp;
                    break;
                    case FORWARD_RIGHT:
                    rVal = animationStartUp;
                    break;
                    case BACKWARD_LEFT:
                    rVal = Animation.ANIMATION_WALK_LEFT;
                    break;
                    case BACKWARD_RIGHT:
                    rVal = Animation.ANIMATION_WALK_RIGHT;
                    break;
                }
                break;
                case MOVE:
                switch(facing){
                    case FORWARD:
                    rVal = animationMain;
                    break;
                    case BACKWARD:
                    rVal = animationMain;
                    break;
                    case LEFT:
                    rVal = Animation.ANIMATION_WALK_LEFT;
                    break;
                    case RIGHT:
                    rVal = Animation.ANIMATION_WALK_RIGHT;
                    break;
                    case FORWARD_LEFT:
                    rVal = animationMain;
                    break;
                    case FORWARD_RIGHT:
                    rVal = animationMain;
                    break;
                    case BACKWARD_LEFT:
                    rVal = Animation.ANIMATION_WALK_LEFT;
                    break;
                    case BACKWARD_RIGHT:
                    rVal = Animation.ANIMATION_WALK_RIGHT;
                    break;
                }
                break;
                case SLOWDOWN:
                switch(facing){
                    case FORWARD:
                    rVal = animationSlowDown;
                    break;
                    case BACKWARD:
                    rVal = animationSlowDown;
                    break;
                    case LEFT:
                    rVal = Animation.ANIMATION_WALK_LEFT;
                    break;
                    case RIGHT:
                    rVal = Animation.ANIMATION_WALK_RIGHT;
                    break;
                    case FORWARD_LEFT:
                    rVal = animationSlowDown;
                    break;
                    case FORWARD_RIGHT:
                    rVal = animationSlowDown;
                    break;
                    case BACKWARD_LEFT:
                    rVal = Animation.ANIMATION_WALK_LEFT;
                    break;
                    case BACKWARD_RIGHT:
                    rVal = Animation.ANIMATION_WALK_RIGHT;
                    break;
                }
                break;
            }
        }
        

        return rVal;
    }
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets facing and handles the synchronization logic for it.
     * </p>
     * @param facing The value to set facing to.
     */
    public void setFacing(MovementRelativeFacing facing){
        this.facing = facing;
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
    public static ClientGroundMovementTree attachTree(Entity parent, Object ... params){
        ClientGroundMovementTree rVal = new ClientGroundMovementTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTGROUNDMOVEMENTTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTGROUNDMOVEMENTTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTGROUNDMOVEMENTTREE_ID);
    }

    /**
     * <p>
     * Gets the GroundMovementTree of the entity
     * </p>
     * @param entity the entity
     * @return The GroundMovementTree
     */
    public static ClientGroundMovementTree getGroundMovementTree(Entity entity){
        return (ClientGroundMovementTree)entity.getData(EntityDataStrings.TREE_CLIENTGROUNDMOVEMENTTREE);
    }

    /**
     * <p>
     * Gets the ClientGroundMovementTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientGroundMovementTree
     */
    public static ClientGroundMovementTree getClientGroundMovementTree(Entity entity){
        return (ClientGroundMovementTree)entity.getData(EntityDataStrings.TREE_CLIENTGROUNDMOVEMENTTREE);
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getMovementRelativeFacingEnumAsShort(MovementRelativeFacing enumVal){
        switch(enumVal){
            case FORWARD:
                return 0;
            case LEFT:
                return 1;
            case RIGHT:
                return 2;
            case BACKWARD:
                return 3;
            case FORWARD_LEFT:
                return 4;
            case FORWARD_RIGHT:
                return 5;
            case BACKWARD_LEFT:
                return 6;
            case BACKWARD_RIGHT:
                return 7;
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
    public static MovementRelativeFacing getMovementRelativeFacingShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return MovementRelativeFacing.FORWARD;
            case 1:
                return MovementRelativeFacing.LEFT;
            case 2:
                return MovementRelativeFacing.RIGHT;
            case 3:
                return MovementRelativeFacing.BACKWARD;
            case 4:
                return MovementRelativeFacing.FORWARD_LEFT;
            case 5:
                return MovementRelativeFacing.FORWARD_RIGHT;
            case 6:
                return MovementRelativeFacing.BACKWARD_LEFT;
            case 7:
                return MovementRelativeFacing.BACKWARD_RIGHT;
            default:
                return MovementRelativeFacing.FORWARD;
        }
    }
    /**
     * <p>
     * Checks if the entity has a ClientGroundMovementTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientGroundMovementTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTGROUNDMOVEMENTTREE);
    }

}
