package electrosphere.entity.state.movement.editor;


import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.data.entity.creature.movement.EditorMovementSystem;
import electrosphere.engine.Globals;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.movement.fall.ClientFallTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.state.movement.sprint.ClientSprintTree;
import electrosphere.entity.state.movement.walk.ClientWalkTree;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.renderer.anim.Animation;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.util.math.SpatialMathUtils;

import java.util.concurrent.CopyOnWriteArrayList;

import org.joml.Quaterniond;
import org.joml.Vector3d;

@SynchronizedBehaviorTree(name = "clientEditorMovementTree", isServer = false, correspondingTree="serverEditorMovementTree")
/*
Behavior tree for movement in an entity
*/
public class ClientEditorMovementTree implements BehaviorTree {
    
    /**
     * The state of the editor movement tree
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
    public static enum EditorMovementRelativeFacing {
        FORWARD,
        LEFT,
        RIGHT,
        BACKWARD,
        FORWARD_LEFT,
        FORWARD_RIGHT,
        BACKWARD_LEFT,
        BACKWARD_RIGHT,
        UP,
        DOWN,
    }
    
    static final double STATE_DIFFERENCE_HARD_UPDATE_THRESHOLD = 1.0;
    static final double STATE_DIFFERENCE_SOFT_UPDATE_THRESHOLD = 0.1;
    static final double SOFT_UPDATE_MULTIPLIER = 0.3;
    static final double STATE_DIFFERENCE_CREEP_MULTIPLIER = 0.001; //while the movement tree is idle, slowly creep the position of the entity towards the true server position by this amount
    static final double STATE_DIFFERENCE_CREEP_CUTOFF = 0.01; //the cutoff for creep when we say it's "close enough"

    public static final float EDITOR_MAX_VELOCITY = 1.0f;
    public static final float EDITOR_ACCEL = 1.0f;
    
    String animationStartUp = Animation.ANIMATION_MOVEMENT_STARTUP;
    String animationMain = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSlowDown = Animation.ANIMATION_MOVEMENT_MOVE;
    String animationSprintStart = Animation.ANIMATION_SPRINT_STARTUP;
    String animationSprint = Animation.ANIMATION_SPRINT;
    String animationSprintWindDown = Animation.ANIMATION_SPRINT_WINDDOWN;
    
    MovementTreeState state;
    @SyncedField
    EditorMovementRelativeFacing facing = EditorMovementRelativeFacing.FORWARD;
    
    ClientSprintTree sprintTree;
    ClientJumpTree jumpTree;
    ClientFallTree fallTree;

    EditorMovementSystem editorMovementData;
    
    Entity parent;

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
    private ClientEditorMovementTree(Entity e, Object ... params){
        //Collidable collidable, EditorMovementSystem editorMovementData
        if(params.length < 1){
            throw new IllegalArgumentException("Tried to create a client editor movement tree without providing both mandatory parameters");
        }
        state = MovementTreeState.IDLE;
        parent = e;
        this.editorMovementData = (EditorMovementSystem)params[0];
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
    public void start(EditorMovementRelativeFacing facing){
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
                        ClientEditorMovementTree.getMovementRelativeFacingEnumAsShort(facing),
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
                    ClientEditorMovementTree.getMovementRelativeFacingEnumAsShort(facing),
                    2 //magic number corresponding to state slowdown
                )
            );
        }
    }
    
    @Override
    public void simulate(float deltaTime){
        Vector3d position = EntityUtils.getPosition(parent);
        Vector3d facingVector = CreatureUtils.getFacingVector(parent);
        float maxNaturalVelocity = EDITOR_MAX_VELOCITY;

        Entity serverEntity = EntityLookupUtils.getEntityById(Globals.clientState.clientSceneWrapper.mapClientToServerId(parent.getId()));

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
        
        
        //state machine
        switch(state){
            case STARTUP: {
                //update rotation
                rotation.set(movementQuaternion);

                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                //check if can transition state
                if(velocity >= maxNaturalVelocity){
                    velocity = maxNaturalVelocity;
                    state = MovementTreeState.MOVE;
                }
                CreatureUtils.setVelocity(parent, velocity);
                //actually update
                rotation.set(movementQuaternion);
                position.set(new Vector3d(position).add(new Vector3d(movementVector).mul(velocity)));
                if(serverEntity != null){
                    if(position.x >= 0 && position.y >= 0 && position.z >= 0 && position.x < Globals.clientState.clientWorldData.getWorldBoundMax().x && position.y < Globals.clientState.clientWorldData.getWorldBoundMax().y && position.z < Globals.clientState.clientWorldData.getWorldBoundMax().z){
                        ServerEntityUtils.repositionEntity(serverEntity, new Vector3d(position));
                    }
                }
                
                GravityUtils.clientAttemptActivateGravity(parent);
            } break;
            case MOVE: {
                //update rotation
                rotation.set(movementQuaternion);


                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                rotation.set(movementQuaternion);
                position.set(new Vector3d(position).add(new Vector3d(movementVector).mul(velocity)));
                if(serverEntity != null){
                    if(position.x >= 0 && position.y >= 0 && position.z >= 0 && position.x < Globals.clientState.clientWorldData.getWorldBoundMax().x && position.y < Globals.clientState.clientWorldData.getWorldBoundMax().y && position.z < Globals.clientState.clientWorldData.getWorldBoundMax().z){
                        ServerEntityUtils.repositionEntity(serverEntity, new Vector3d(position));
                    }
                }
                
                GravityUtils.clientAttemptActivateGravity(parent);
            } break;
            case SLOWDOWN: {
                //update rotation
                rotation.set(movementQuaternion);

                //velocity stuff
                this.updateVelocity();
                float velocity = this.getModifiedVelocity();
                //check if can transition state
                if(velocity <= 0){
                    velocity = 0;
                    state = MovementTreeState.IDLE;
                    CreatureUtils.setVelocity(parent, velocity);
                }
                rotation.set(movementQuaternion);
                position.set(new Vector3d(position).add(new Vector3d(movementVector).mul(velocity)));
                if(serverEntity != null){
                    if(position.x >= 0 && position.y >= 0 && position.z >= 0 && position.x < Globals.clientState.clientWorldData.getWorldBoundMax().x && position.y < Globals.clientState.clientWorldData.getWorldBoundMax().y && position.z < Globals.clientState.clientWorldData.getWorldBoundMax().z){
                        ServerEntityUtils.repositionEntity(serverEntity, new Vector3d(position));
                    }
                }
                
                GravityUtils.clientAttemptActivateGravity(parent);
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
        float acceleration = EDITOR_ACCEL;
        float maxNaturalVelocity = EDITOR_MAX_VELOCITY;
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
     * <p> Automatically generated </p>
     * <p>
     * Gets facing.
     * </p>
     */
    public EditorMovementRelativeFacing getFacing(){
        return facing;
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
    }
    

    /**
     * <p>
     * Gets the ClientEditorMovementTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientEditorMovementTree
     */
    public static ClientEditorMovementTree getClientEditorMovementTree(Entity entity){
        return (ClientEditorMovementTree)entity.getData(EntityDataStrings.TREE_CLIENTEDITORMOVEMENTTREE);
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getMovementRelativeFacingEnumAsShort(EditorMovementRelativeFacing enumVal){
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
            case UP:
                return 8;
            case DOWN:
                return 9;
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
    public static EditorMovementRelativeFacing getMovementRelativeFacingShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return EditorMovementRelativeFacing.FORWARD;
            case 1:
                return EditorMovementRelativeFacing.LEFT;
            case 2:
                return EditorMovementRelativeFacing.RIGHT;
            case 3:
                return EditorMovementRelativeFacing.BACKWARD;
            case 4:
                return EditorMovementRelativeFacing.FORWARD_LEFT;
            case 5:
                return EditorMovementRelativeFacing.FORWARD_RIGHT;
            case 6:
                return EditorMovementRelativeFacing.BACKWARD_LEFT;
            case 7:
                return EditorMovementRelativeFacing.BACKWARD_RIGHT;
            case 8:
                return EditorMovementRelativeFacing.UP;
            case 9:
                return EditorMovementRelativeFacing.DOWN;
            default:
                return EditorMovementRelativeFacing.FORWARD;
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
    public static ClientEditorMovementTree attachTree(Entity parent, Object ... params){
        ClientEditorMovementTree rVal = new ClientEditorMovementTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTEDITORMOVEMENTTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTEDITORMOVEMENTTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTEDITORMOVEMENTTREE_ID);
    }

    /**
     * <p>
     * Checks if the entity has a ClientEditorMovementTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientEditorMovementTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTEDITORMOVEMENTTREE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts a short to the equivalent enum value
     * </p>
     * @param shortVal The short value
     * @return The enum value
     */
    public static EditorMovementRelativeFacing getEditorMovementRelativeFacingShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return EditorMovementRelativeFacing.FORWARD;
            case 1:
                return EditorMovementRelativeFacing.LEFT;
            case 2:
                return EditorMovementRelativeFacing.RIGHT;
            case 3:
                return EditorMovementRelativeFacing.BACKWARD;
            case 4:
                return EditorMovementRelativeFacing.FORWARD_LEFT;
            case 5:
                return EditorMovementRelativeFacing.FORWARD_RIGHT;
            case 6:
                return EditorMovementRelativeFacing.BACKWARD_LEFT;
            case 7:
                return EditorMovementRelativeFacing.BACKWARD_RIGHT;
            case 8:
                return EditorMovementRelativeFacing.UP;
            case 9:
                return EditorMovementRelativeFacing.DOWN;
            default:
                return EditorMovementRelativeFacing.FORWARD;
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getEditorMovementRelativeFacingEnumAsShort(EditorMovementRelativeFacing enumVal){
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
            case UP:
                return 8;
            case DOWN:
                return 9;
            default:
                return 0;
        }
    }

}
