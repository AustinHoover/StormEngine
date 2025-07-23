package electrosphere.entity.state.movement.editor;


import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.data.entity.creature.movement.EditorMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing;
import electrosphere.entity.state.movement.editor.ClientEditorMovementTree.MovementTreeState;
import electrosphere.entity.state.movement.fall.ServerFallTree;
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
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.utils.ServerScriptUtils;
import electrosphere.util.math.SpatialMathUtils;

import java.util.LinkedList;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;

@SynchronizedBehaviorTree(name = "serverEditorMovementTree", isServer = true, correspondingTree="clientEditorMovementTree")
/*
Behavior tree for movement in an entity
*/
public class ServerEditorMovementTree implements BehaviorTree {

    
    String animationStartUp = Animation.ANIMATION_MOVEMENT_STARTUP;
    String animationMain = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSlowDown = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSprintStart = Animation.ANIMATION_SPRINT_STARTUP;
    String animationSprint = Animation.ANIMATION_SPRINT;
    String animationSprintWindDown = Animation.ANIMATION_SPRINT_WINDDOWN;
    
    MovementTreeState state;
    @SyncedField
    EditorMovementRelativeFacing facing;

    //The data for the movement system
    EditorMovementSystem editorMovementSystem;
    
    ServerSprintTree sprintTree;
    ServerJumpTree jumpTree;
    ServerFallTree fallTree;
    
    Entity parent;
    
    List<EntityMessage> networkMessageQueue = new LinkedList<EntityMessage>();
    
    long lastUpdateTime = 0;

    //the vector organizing the direction the entity will move in
    Vector3d movementVector = new Vector3d(1,0,0);
    
    
    private ServerEditorMovementTree(Entity e, Object ... params){
        //EditorMovementSystem system
        state = MovementTreeState.IDLE;
        facing = EditorMovementRelativeFacing.FORWARD;
        parent = e;
        this.editorMovementSystem = (EditorMovementSystem)params[0];
    }
    
    public MovementTreeState getState(){
        return state;
    }
    
    /**
     * Starts the server movement tree
     * @param facing The facing dir to start with
     */
    public void start(EditorMovementRelativeFacing facing){
        if(canStartMoving()){
            setFacing(facing);
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
                    ClientEditorMovementTree.getMovementRelativeFacingEnumAsShort(facing),
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
                ClientEditorMovementTree.getMovementRelativeFacingEnumAsShort(facing),
                2 //magic number corresponding to state slowdown
            )
        );
    }
    
    @Override
    public void simulate(float deltaTime){
        float maxNaturalVelocity = 0;
        if(CreatureUtils.hasVelocity(parent)){
            maxNaturalVelocity = ClientEditorMovementTree.EDITOR_MAX_VELOCITY;
        }
        Vector3d position = EntityUtils.getPosition(parent);
        Vector3d facingVector = CreatureUtils.getFacingVector(parent);
        if(ServerPlayerViewDirTree.hasTree(parent)){
            ServerPlayerViewDirTree serverViewTree =ServerPlayerViewDirTree.getTree(parent);
            facingVector = CameraEntityUtils.getFacingVec(serverViewTree.getYaw(), serverViewTree.getPitch());
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
                case UP: {
                    movementVector = SpatialMathUtils.getUpVector();
                } break;
                case DOWN: {
                    movementVector = SpatialMathUtils.getUpVector().mul(-1);
                } break;
            }
        }
        Quaterniond movementQuaternion = new Quaterniond().rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(facingVector.x,0,facingVector.z)).normalize();
        Quaterniond rotation = EntityUtils.getRotation(parent);
        if(facingVector.length() == 0){
            throw new IllegalStateException("Facing vector length is 0. This will break ODE4J");
        }
        
        //parse attached network messages
        for(EntityMessage message : networkMessageQueue){
            networkMessageQueue.remove(message);
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
                                start(ClientEditorMovementTree.getMovementRelativeFacingShortAsEnum((short)message.getpropertyValueInt()));
                            } break;
                            case 2: {
                                // System.out.println("Receive move packet from client treestate " + message.gettreeState());
                                slowdown();
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

        // System.out.println(movementVector + " " + velocity * Main.deltaTime);
        
        //state machine
        switch(state){
            case STARTUP: {
                CreatureUtils.setFacingVector(parent, facingVector);
                rotation.set(movementQuaternion);
                //run startup code
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                //check if can transition state
                if(velocity >= maxNaturalVelocity){
                    velocity = maxNaturalVelocity;
                    state = MovementTreeState.MOVE;
                    CreatureUtils.setVelocity(parent, velocity);
                }
                position.set(new Vector3d(position).add(new Vector3d(movementVector).mul(velocity)));
//                position.set(newPosition);
                
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
                        ClientEditorMovementTree.getMovementRelativeFacingEnumAsShort(facing),
                        0
                    )
                );
            } break;
            case MOVE: {
                CreatureUtils.setFacingVector(parent, facingVector);
                rotation.set(movementQuaternion);
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                position.set(new Vector3d(position).add(new Vector3d(movementVector).mul(velocity)));
                
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
                        ClientEditorMovementTree.getMovementRelativeFacingEnumAsShort(facing),
                        1
                    )
                );

                //tell script engine we moved
                ServerScriptUtils.fireSignalOnEntity(parent, "entityGroundMove", position);
            } break;
            case SLOWDOWN: {
                CreatureUtils.setFacingVector(parent, facingVector);
                rotation.set(movementQuaternion);
                //velocity stuff
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                position.set(new Vector3d(position).add(new Vector3d(movementVector).mul(velocity)));
                //check if can transition state
                if(velocity <= 0){
                    velocity = 0;
                    state = MovementTreeState.IDLE;
                }
                // PhysicsEntityUtils.getDBody(parent).addForce(
                //     movementVector.x * velocity * Globals.timekeeper.getSimFrameTime(),
                //     linearVelocity.get1(),
                //     movementVector.z * velocity * Globals.timekeeper.getSimFrameTime()
                // );
//                position.set(newPosition);
                
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
                        ClientEditorMovementTree.getMovementRelativeFacingEnumAsShort(facing),
                        2
                    )
                );
            } break;
            case IDLE: {
            } break;
        }
    }

    /**
     * Updates the velocity and acceleration
     */
    private void updateVelocity(){
        float velocity = CreatureUtils.getVelocity(parent);
        float acceleration = ClientEditorMovementTree.EDITOR_ACCEL;
        float maxNaturalVelocity = ClientEditorMovementTree.EDITOR_MAX_VELOCITY;
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

    public void addNetworkMessage(EntityMessage networkMessage) {
        networkMessageQueue.add(networkMessage);
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
     * @param editorMovementSystem
     * @return
     */
    public static float getMaximumVelocity(Entity entity, EditorMovementSystem editorMovementSystem, EditorMovementRelativeFacing facing){
        float maxVelocity = ClientEditorMovementTree.EDITOR_MAX_VELOCITY;
        return maxVelocity;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets facing.
     * </p>
     */
    public EditorMovementRelativeFacing getFacing(){
        return facing;
    }



    /**
     * <p>
     * Gets the ServerEditorMovementTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerEditorMovementTree
     */
    public static ServerEditorMovementTree getServerEditorMovementTree(Entity entity){
        return (ServerEditorMovementTree)entity.getData(EntityDataStrings.TREE_SERVEREDITORMOVEMENTTREE);
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
    public static ServerEditorMovementTree attachTree(Entity parent, Object ... params){
        ServerEditorMovementTree rVal = new ServerEditorMovementTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVEREDITORMOVEMENTTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets facing and handles the synchronization logic for it.
     * </p>
     * @param facing The value to set facing to.
     */
    public void setFacing(EditorMovementRelativeFacing facing){
        this.facing = facing;
        int value = ClientEditorMovementTree.getEditorMovementRelativeFacingEnumAsShort(facing);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID, FieldIdEnums.TREE_SERVEREDITORMOVEMENTTREE_SYNCEDFIELD_FACING_ID, value));
        }
    }

    /**
     * <p>
     * Checks if the entity has a ServerEditorMovementTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerEditorMovementTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVEREDITORMOVEMENTTREE);
    }

}
