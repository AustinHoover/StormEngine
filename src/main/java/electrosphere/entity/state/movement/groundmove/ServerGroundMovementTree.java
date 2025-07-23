package electrosphere.entity.state.movement.groundmove;


import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.server.entity.poseactor.PoseActor;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.creature.movement.GroundMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.engine.time.Timekeeper;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.movement.fall.ServerFallTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementRelativeFacing;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementTreeState;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.entity.state.movement.sprint.ServerSprintTree;
import electrosphere.entity.state.movement.walk.ServerWalkTree;
import electrosphere.entity.state.server.ServerPlayerViewDirTree;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.renderer.anim.Animation;
import electrosphere.server.ai.AI;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.utils.ServerScriptUtils;
import electrosphere.util.math.BasicMathUtils;
import electrosphere.util.math.SpatialMathUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;

@SynchronizedBehaviorTree(name = "serverGroundMovementTree", isServer = true, correspondingTree="clientGroundMovementTree")
/*
Behavior tree for movement in an entity
*/
public class ServerGroundMovementTree implements BehaviorTree {

    /**
     * Denotes an invalid elevation to lerp non-body collidables towards
     */
    public static final double INVALID_ELEVATION = -1;

    /**
     * Amount to lerp non-body collidable elevation by
     */
    public static final double ELEVATION_LERP_FACTOR = 0.01;

    /**
     * Lock for handling threading with network messages
     */
    static ReentrantLock lock = new ReentrantLock();

    
    String animationStartUp = Animation.ANIMATION_MOVEMENT_STARTUP;
    String animationMain = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSlowDown = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSprintStart = Animation.ANIMATION_SPRINT_STARTUP;
    String animationSprint = Animation.ANIMATION_SPRINT;
    String animationSprintWindDown = Animation.ANIMATION_SPRINT_WINDDOWN;
    
    MovementTreeState state;
    @SyncedField
    MovementRelativeFacing facing;

    //The data for the movement system
    GroundMovementSystem groundMovementSystem;
    
    ServerSprintTree sprintTree;
    ServerJumpTree jumpTree;
    ServerFallTree fallTree;
    
    Entity parent;
    
    Collidable collidable;
    
    List<EntityMessage> networkMessageQueue = new LinkedList<EntityMessage>();
    
    long lastUpdateTime = 0;

    //the vector organizing the direction the entity will move in
    Vector3d movementVector = new Vector3d(1,0,0);

    /**
     * Target elevation for specifically non-body collidables to lerp to organically
     */
    private double collidableElevationTarget = -1;
    
    
    private ServerGroundMovementTree(Entity e, Object ... params){
        //Collidable collidable, GroundMovementSystem system
        state = MovementTreeState.IDLE;
        facing = MovementRelativeFacing.FORWARD;
        parent = e;
        this.collidable = (Collidable)params[0];
        this.groundMovementSystem = (GroundMovementSystem)params[1];
    }
    
    public MovementTreeState getState(){
        return state;
    }
    
    /**
     * Starts the server movement tree
     * @param facing The facing dir to start with
     */
    public void start(MovementRelativeFacing facing){
        if(this.canStartMoving()){
            this.setFacing(facing);
            state = MovementTreeState.STARTUP;
            //if we aren't the server, alert the server we intend to walk forward
            Vector3d position = EntityUtils.getPosition(parent);
            Quaterniond rotation = EntityUtils.getRotation(parent);
            float velocity = CreatureUtils.getVelocity(parent);
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                EntityMessage.constructmoveUpdateMessage(
                    parent.getId(),
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
    
    /**
     * Interrupts the tree
     */
    public void interrupt(){
        state = MovementTreeState.IDLE;
        CreatureUtils.setVelocity(parent, 0);
    }
    
    /**
     * Triggers the move tree to slow down
     */
    public void slowdown(){
        state = MovementTreeState.SLOWDOWN;
        //if we aren't the server, alert the server we intend to slow down
        Vector3d position = EntityUtils.getPosition(parent);
        Quaterniond rotation = EntityUtils.getRotation(parent);
        float velocity = CreatureUtils.getVelocity(parent);
        DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
            EntityMessage.constructmoveUpdateMessage(
                parent.getId(),
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
    
    @Override
    public void simulate(float deltaTime){
        float maxNaturalVelocity = 0;
        if(CreatureUtils.hasVelocity(parent)){
            maxNaturalVelocity = ServerGroundMovementTree.getMaximumVelocity(parent, this.groundMovementSystem, facing);
        }
        PoseActor poseActor = EntityUtils.getPoseActor(parent);
        Vector3d position = EntityUtils.getPosition(parent);
        Vector3d facingVector = CreatureUtils.getFacingVector(parent);
        if(ServerPlayerViewDirTree.hasTree(parent)){
            if(AI.getAI(parent) == null || !AI.getAI(parent).isApplyToPlayer()){
                ServerPlayerViewDirTree serverViewTree =ServerPlayerViewDirTree.getTree(parent);
                facingVector = CameraEntityUtils.getFacingVec(serverViewTree.getYaw(), serverViewTree.getPitch());
            } 
        }
        DBody body = PhysicsEntityUtils.getDBody(parent);
        //TODO: eventually handle non-rigid-body entities
        DVector3C linearVelocity = null;
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
        if(facingVector.length() == 0){
            throw new IllegalStateException("Facing vector length is 0. This will break ODE4J");
        }
        
        //parse attached network messages
        lock.lock();
        for(EntityMessage message : networkMessageQueue){
            long updateTime = message.gettime();
//            System.out.println("MOVE to " + message.getX() + " " + message.getY() + " " + message.getZ());
            switch(message.getMessageSubtype()){
                case MOVEUPDATE: {
                    if(updateTime >= lastUpdateTime){
                        lastUpdateTime = updateTime;
                        switch(message.gettreeState()){
                            //0 is startup
                            case 0: {
                                // System.out.println("Receive move packet from client treestate " + message.gettreeState());
                                this.start(ClientGroundMovementTree.getMovementRelativeFacingShortAsEnum((short)message.getpropertyValueInt()));
                            } break;
                            case 2: {
                                // System.out.println("Receive move packet from client treestate " + message.gettreeState());
                                this.slowdown();
                            } break;
                            default: {

                            } break;
                        }
                        //we want to always update the server facing vector with where the client says they're facing
                        EntityUtils.getRotation(parent).set(message.getrotationX(),message.getrotationY(),message.getrotationZ(),message.getrotationW());
                        break;
                    }
                } break;
                default:
                break;
            }
        }
        networkMessageQueue.clear();
        lock.unlock();

        // System.out.println(movementVector + " " + velocity * Main.deltaTime);
        
        //state machine
        switch(this.state){
            case STARTUP: {
                CreatureUtils.setFacingVector(parent, facingVector);
                rotation.set(movementQuaternion);
                if(poseActor != null){
                    String animationToPlay = determineCorrectAnimation(MovementTreeState.STARTUP);
                    if(
                        !poseActor.isPlayingAnimation() || !poseActor.isPlayingAnimation(animationToPlay) &&
                        (jumpTree == null || !jumpTree.isJumping()) &&
                        (fallTree == null || !fallTree.isFalling())
                    ){
                        poseActor.playAnimation(animationToPlay,AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                        poseActor.incrementAnimationTime(0.0001);
                    }
                }
                //run startup code
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                //check if can transition state
                if(velocity >= maxNaturalVelocity){
                    velocity = maxNaturalVelocity;
                    state = MovementTreeState.MOVE;
                    CreatureUtils.setVelocity(parent, velocity);
                }
                if(body == null){
                    Vector3d velVec = new Vector3d(movementVector);
                    velVec.mul(velocity * Globals.engineState.timekeeper.getSimFrameTime() * Timekeeper.ENGINE_STEP_SIZE);
                    velVec.add(position);
                    if(this.collidableElevationTarget != ServerGroundMovementTree.INVALID_ELEVATION){
                        velVec.y = BasicMathUtils.lerp(position.y, this.collidableElevationTarget, ServerGroundMovementTree.ELEVATION_LERP_FACTOR);
                    }
                    ServerEntityUtils.repositionEntity(parent, velVec);
                } else {
                    body.enable();
                    body.setLinearVel(
                        movementVector.x * velocity * Globals.engineState.timekeeper.getSimFrameTime(),
                        linearVelocity.get1(),
                        movementVector.z * velocity * Globals.engineState.timekeeper.getSimFrameTime()
                    );
                    body.setAngularVel(0, 0, 0);
                }
                
                GravityUtils.serverAttemptActivateGravity(parent);
                
                DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                    EntityMessage.constructmoveUpdateMessage(
                        parent.getId(),
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
                        0
                    )
                );
            } break;
            case MOVE: {
                CreatureUtils.setFacingVector(parent, facingVector);
                rotation.set(movementQuaternion);
                //check if can restart animation
                //if yes, restart animation
                if(poseActor != null){
                    String animationToPlay = determineCorrectAnimation(MovementTreeState.MOVE);
                    if(
                        !poseActor.isPlayingAnimation() || !poseActor.isPlayingAnimation(animationToPlay) &&
                        (jumpTree == null || !jumpTree.isJumping()) &&
                        (fallTree == null || !fallTree.isFalling())
                    ){
                        poseActor.playAnimation(animationToPlay,AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                        poseActor.incrementAnimationTime(0.0001);
                    }
                }
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                if(body == null){
                    Vector3d velVec = new Vector3d(movementVector);
                    velVec.mul(velocity * Globals.engineState.timekeeper.getSimFrameTime() * Timekeeper.ENGINE_STEP_SIZE);
                    velVec.add(position);
                    if(this.collidableElevationTarget != ServerGroundMovementTree.INVALID_ELEVATION){
                        velVec.y = BasicMathUtils.lerp(position.y, this.collidableElevationTarget, ServerGroundMovementTree.ELEVATION_LERP_FACTOR);
                    }
                    ServerEntityUtils.repositionEntity(parent, velVec);
                } else {
                    body.enable();
                    body.setLinearVel(
                        movementVector.x * velocity * Globals.engineState.timekeeper.getSimFrameTime(),
                        linearVelocity.get1(),
                        movementVector.z * velocity * Globals.engineState.timekeeper.getSimFrameTime()
                    );
                    body.setAngularVel(0, 0, 0);
                }
                
                GravityUtils.serverAttemptActivateGravity(parent);
                
                DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                    EntityMessage.constructmoveUpdateMessage(
                        parent.getId(),
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
                        1
                    )
                );

                //tell script engine we moved
                ServerScriptUtils.fireSignalOnEntity(parent, "entityGroundMove", position);
            } break;
            case SLOWDOWN: {
                CreatureUtils.setFacingVector(parent, facingVector);
                rotation.set(movementQuaternion);
                //run slowdown code
                if(poseActor != null){
                    String animationToPlay = determineCorrectAnimation(MovementTreeState.SLOWDOWN);
                    if(
                        !poseActor.isPlayingAnimation() || !poseActor.isPlayingAnimation(animationToPlay) &&
                        (jumpTree == null || !jumpTree.isJumping()) &&
                        (fallTree == null || !fallTree.isFalling())
                        ){
                            poseActor.playAnimation(animationToPlay,AnimationPriorities.getValue(AnimationPriorities.CORE_MOVEMENT));
                            poseActor.incrementAnimationTime(0.0001);
                    }
                    if(poseActor.isPlayingAnimation(determineCorrectAnimation(MovementTreeState.MOVE))){
                        poseActor.stopAnimation(determineCorrectAnimation(MovementTreeState.MOVE));
                    }
                }
                //velocity stuff
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                //check if can transition state
                if(velocity <= 0){
                    velocity = 0;
                    state = MovementTreeState.IDLE;
                    if(poseActor != null){
                        String animationToPlay = determineCorrectAnimation(MovementTreeState.SLOWDOWN);
                        if(poseActor.isPlayingAnimation() && poseActor.isPlayingAnimation(animationToPlay)){
                            poseActor.stopAnimation(animationToPlay);
                        }
                    }
                } else {
                    GravityUtils.serverAttemptActivateGravity(parent);
                }
                if(body == null){
                    Vector3d velVec = new Vector3d(movementVector);
                    velVec.mul(velocity * Globals.engineState.timekeeper.getSimFrameTime() * Timekeeper.ENGINE_STEP_SIZE);
                    velVec.add(position);
                    if(this.collidableElevationTarget != ServerGroundMovementTree.INVALID_ELEVATION){
                        velVec.y = BasicMathUtils.lerp(position.y, this.collidableElevationTarget, ServerGroundMovementTree.ELEVATION_LERP_FACTOR);
                    }
                    ServerEntityUtils.repositionEntity(parent, velVec);
                } else {
                    body.enable();
                    body.setLinearVel(
                        movementVector.x * velocity * Globals.engineState.timekeeper.getSimFrameTime(),
                        linearVelocity.get1(),
                        movementVector.z * velocity * Globals.engineState.timekeeper.getSimFrameTime()
                    );
                    body.setAngularVel(0, 0, 0);
                }
                
                DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                    EntityMessage.constructmoveUpdateMessage(
                        parent.getId(),
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
                        2
                    )
                );
            } break;
            case IDLE: {
            } break;
            default: {
                throw new Error("Unsupported state!? " + this.state);
            }
        }
    }

    /**
     * Updates the velocity and acceleration
     */
    private void updateVelocity(){
        float velocity = CreatureUtils.getVelocity(parent);
        float acceleration = CreatureUtils.getAcceleration(parent);
        float maxNaturalVelocity = ServerGroundMovementTree.getMaximumVelocity(parent, this.groundMovementSystem, facing);
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
        if(ServerWalkTree.getServerWalkTree(parent) != null && ServerWalkTree.getServerWalkTree(parent).isWalking()){
            walkModifier = ServerWalkTree.getServerWalkTree(parent).getModifier();
        }
        if(ServerSprintTree.getServerSprintTree(parent) != null && ServerSprintTree.getServerSprintTree(parent).isSprinting()){
            sprintModifier = ServerSprintTree.getServerSprintTree(parent).getSprintSystem().getModifier();
        }
        if(ServerAttackTree.getServerAttackTree(parent) != null){
            attackModifier = (float)ServerAttackTree.getServerAttackTree(parent).getMovementPenalty();
        }
        return velocity * sprintModifier * walkModifier * attackModifier;
    }

    /**
     * Adds a network message for the tree to parse
     * @param networkMessage The message
     */
    public void addNetworkMessage(EntityMessage networkMessage) {
        lock.lock();
        networkMessageQueue.add(networkMessage);
        lock.unlock();
    }
    
    /**
     * Checks if the tree is moving
     * @return true if is moving, false otherwise
     */
    public boolean isMoving(){
        return this.state != MovementTreeState.IDLE;
    }

    /**
     * Checks if the tree CAN start moving
     * @return true if CAN start moving, false otherwise
     */
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
    
    public void setServerSprintTree(ServerSprintTree sprintTree){
        this.sprintTree = sprintTree;
    }

    public void setServerJumpTree(ServerJumpTree jumpTree){
        this.jumpTree = jumpTree;
    }

    public void setServerFallTree(ServerFallTree fallTree){
        this.fallTree = fallTree;
    }

    /**
     * Gets the maximum velocity of an entity
     * @param entity
     * @param groundMovementSystem
     * @return
     */
    public static float getMaximumVelocity(Entity entity, GroundMovementSystem groundMovementSystem, MovementRelativeFacing facing){
        float maxVelocity = groundMovementSystem.getMaxVelocity();
        switch(facing){
            case FORWARD: {
                return maxVelocity;
            }
            case BACKWARD_LEFT:
            case BACKWARD_RIGHT:
            case BACKWARD: {
                if(groundMovementSystem.getBackpedalMultiplier() != null){
                    return maxVelocity * groundMovementSystem.getBackpedalMultiplier();
                }
            } break;
            case LEFT:
            case RIGHT:
            case FORWARD_LEFT:
            case FORWARD_RIGHT: {
                if(groundMovementSystem.getStrafeMultiplier() != null){
                    return maxVelocity * groundMovementSystem.getStrafeMultiplier();
                }
            } break;
        }
        return maxVelocity;
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
        int value = ClientGroundMovementTree.getMovementRelativeFacingEnumAsShort(facing);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID, FieldIdEnums.TREE_SERVERGROUNDMOVEMENTTREE_SYNCEDFIELD_FACING_ID, value));
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
    public static ServerGroundMovementTree attachTree(Entity parent, Object ... params){
        ServerGroundMovementTree rVal = new ServerGroundMovementTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERGROUNDMOVEMENTTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID);
    }

    /**
     * <p>
     * Gets the ServerGroundMovementTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerGroundMovementTree
     */
    public static ServerGroundMovementTree getServerGroundMovementTree(Entity entity){
        return (ServerGroundMovementTree)entity.getData(EntityDataStrings.TREE_SERVERGROUNDMOVEMENTTREE);
    }
    
    /**
     * <p>
     * Checks if the entity has a ServerGroundMovementTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerGroundMovementTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERGROUNDMOVEMENTTREE);
    }

    /**
     * Sets the target elevation for non-body collidables to lerp to
     * @param collidableElevationTarget The target elevation
     */
    public void setCollidableElevationTarget(double collidableElevationTarget){
        this.collidableElevationTarget = collidableElevationTarget;
    }

}
