package electrosphere.entity.state.attack;


import electrosphere.data.entity.common.treedata.TreeDataState;
import electrosphere.data.entity.creature.attack.AttackMove;
import electrosphere.data.entity.item.WeaponData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.collidable.Impulse;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxType;
import electrosphere.entity.state.movement.fall.ClientFallTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.state.rotator.RotatorTree;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.renderer.actor.Actor;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3d;

/**
 * Client basic attack tree
 */
@SynchronizedBehaviorTree(name = "clientAttackTree", isServer = false, correspondingTree="serverAttackTree")
public class ClientAttackTree implements BehaviorTree {
    
    /**
     * States available to the attack tree
     */
    @SynchronizableEnum
    public static enum AttackTreeState {
        WINDUP,
        HOLD,
        ATTACK,
        BLOCK_RECOIL,
        COOLDOWN,
        IDLE,
    }

    /**
     * The state of drifting forward during the attack
     */
    @SynchronizableEnum
    public static enum AttackTreeDriftState {
        DRIFT,
        NO_DRIFT,
    }
    
    //the current state of the tree
    @SyncedField(serverSendTransitionPacket = true)
    AttackTreeState state;

    //the current state of drifting caused by the tree
    @SyncedField
    AttackTreeDriftState driftState;
    
    //the parent entity of this attack tree
    Entity parent;
    
    //the last time this tree was updated by server
    long lastUpdateTime = 0;
    
    //the current frame of the current animation/move
    float frameCurrent;
    
    //the name of the current animation
    String animationName = "SwingWeapon";
    
    //the max frame
    int maxFrame = 60;

    List<AttackMove> currentMoveset = null;
    AttackMove currentMove = null;
    @SyncedField
    String currentMoveId = null;
    Entity currentWeapon = null;
    boolean currentMoveHasWindup;
    boolean currentMoveCanHold;
    boolean stillHold = true;
    boolean firesProjectile = false;
    String projectileToFire = null;
    String attackingPoint = null;

    /**
     * The list of entities that have collided with the current attack
     */
    List<Entity> collidedEntities = new LinkedList<Entity>();

    //The state transition util
    StateTransitionUtil stateTransitionUtil;
    
    private ClientAttackTree(Entity e, Object ... params){
        this.setState(AttackTreeState.IDLE);
        this.setDriftState(AttackTreeDriftState.NO_DRIFT);
        parent = e;
        this.stateTransitionUtil = StateTransitionUtil.create(parent, false, new StateTransitionUtilItem[]{
            StateTransitionUtilItem.create(
                AttackTreeState.WINDUP,
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getWindupState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getWindupState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAudioData();
                    }
                },
                false
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.HOLD,
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getHoldState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getHoldState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAudioData();
                    }
                },
                true
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.ATTACK,
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getAttackState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getAttackState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAudioData();
                    }
                },
                false
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.BLOCK_RECOIL,
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getBlockRecoilState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getBlockRecoilState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAudioData();
                    }
                },
                false
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.COOLDOWN,
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getCooldownState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                () -> {
                    TreeDataState state = null;
                    if(currentMove != null){
                        state = currentMove.getCooldownState();
                    }
                    if(state == null){
                        return null;
                    } else {
                        return state.getAudioData();
                    }
                },
                () -> {
                    this.setState(AttackTreeState.IDLE);
                }
            ),
        });
    }
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public AttackTreeState getState(){
        return state;
    }
    
    /**
     * Starts an attack
     */
    public void start(){
        currentMoveCanHold = false;
        currentMoveHasWindup = false;
        stillHold = true;
        firesProjectile = false;
        projectileToFire = null;
        currentWeapon = null;
        attackingPoint = null;
        //figure out attack type we should be doing
        String attackType = this.getAttackType();
        //if we can attack, setup doing so
        if(this.canAttack(attackType)){
            this.setAttackMoveTypeActive(attackType);
            currentMoveset = this.getMoveset(attackType);
            if(currentMoveset != null){
                Globals.clientState.clientConnection.queueOutgoingMessage(EntityMessage.constructstartAttackMessage());
            }
        }
    }

    public void release(){
        stillHold = false;
    }
    
    public void interrupt(){
        this.setState(AttackTreeState.IDLE);
    }
    
    public void slowdown(){
        this.setState(AttackTreeState.COOLDOWN);
    }
    
    @Override
    public void simulate(float deltaTime){
        frameCurrent = frameCurrent + (float)Globals.engineState.timekeeper.getDeltaFrames();
        Vector3d movementVector = CreatureUtils.getFacingVector(parent);

        //
        //synchronize move from server
        if(this.currentMoveset == null){
            this.currentMoveset = this.getMoveset(this.getAttackType());
        }
        if(
            this.currentMoveset != null &&
                (this.currentMove == null && this.currentMoveId != null)
                ||
                (this.currentMove != null && this.currentMove.getAttackMoveId() != this.currentMoveId)
        ){
            for(AttackMove move : currentMoveset){
                if(move.getAttackMoveId().equals(currentMoveId)){
                    currentMove = move;
                }
            }
        }

        //handle the drifting if we're supposed to currently
        switch(driftState){
            case DRIFT:
            if(currentMove != null){
                //calculate the vector of movement
                CollisionObjUtils.getCollidable(parent).addImpulse(new Impulse(new Vector3d(movementVector), new Vector3d(0,0,0), new Vector3d(0,0,0), currentMove.getDriftGoal() * Globals.engineState.timekeeper.getSimFrameTime(), "movement"));
                if(frameCurrent > currentMove.getDriftFrameEnd()){
                    this.setDriftState(AttackTreeDriftState.NO_DRIFT);
                }
            }
            break;
            case NO_DRIFT:
            if(currentMove != null){
                if(frameCurrent > currentMove.getDriftFrameStart() && frameCurrent < currentMove.getDriftFrameEnd()){
                    this.setDriftState(AttackTreeDriftState.DRIFT);
                }
            }
            break;
        }

        //state machine
        switch(state){
            case WINDUP: {
                if(parent.containsKey(EntityDataStrings.CLIENT_ROTATOR_TREE)){
                    RotatorTree.getClientRotatorTree(parent).setActive(true);
                }
                this.stateTransitionUtil.simulate(AttackTreeState.WINDUP);
            } break;
            case HOLD: {
                this.stateTransitionUtil.simulate(AttackTreeState.HOLD);
            } break;
            case ATTACK: {
                this.stateTransitionUtil.simulate(AttackTreeState.ATTACK);
                //activate hitboxes
                List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
                if(attachedEntities != null){
                    for(Entity currentAttached : attachedEntities){
                        if(HitboxCollectionState.hasHitboxState(currentAttached)){
                            HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                            currentState.setActive(true);
                        }
                    }
                }
                if(this.currentMove != null){
                    if(this.currentMove.getActiveBones() != null && HitboxCollectionState.hasHitboxState(this.parent)){
                        HitboxCollectionState hitboxCollectionState = HitboxCollectionState.getHitboxState(this.parent);
                        for(String boneName : this.currentMove.getActiveBones()){
                            List<HitboxState> hitboxes = hitboxCollectionState.getHitboxes(boneName);
                            for(HitboxState hitbox : hitboxes){
                                if(hitbox.getType() == HitboxType.HIT){
                                    hitbox.setActive(true);
                                }
                            }
                        }
                    }
                }
            } break;
            case BLOCK_RECOIL: {
                this.stateTransitionUtil.simulate(AttackTreeState.BLOCK_RECOIL);
                //deactivate hitboxes
                List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
                if(attachedEntities != null){
                    for(Entity currentAttached : attachedEntities){
                        if(HitboxCollectionState.hasHitboxState(currentAttached)){
                            HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                            currentState.setActive(false);
                        }
                    }
                }
                if(this.currentMove != null){
                    if(this.currentMove.getActiveBones() != null && HitboxCollectionState.hasHitboxState(this.parent)){
                        HitboxCollectionState hitboxCollectionState = HitboxCollectionState.getHitboxState(this.parent);
                        for(String boneName : this.currentMove.getActiveBones()){
                            List<HitboxState> hitboxes = hitboxCollectionState.getHitboxes(boneName);
                            for(HitboxState hitbox : hitboxes){
                                if(hitbox.getType() == HitboxType.HIT){
                                    hitbox.setActive(false);
                                }
                            }
                        }
                    }
                }
            } break;
            case COOLDOWN: {
                this.stateTransitionUtil.simulate(AttackTreeState.COOLDOWN);
                //deactive hitboxes
                List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
                if(attachedEntities != null){
                    for(Entity currentAttached : attachedEntities){
                        if(HitboxCollectionState.hasHitboxState(currentAttached)){
                            HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                            currentState.setActive(false);
                        }
                    }
                }
                if(this.currentMove != null){
                    if(this.currentMove.getActiveBones() != null && HitboxCollectionState.hasHitboxState(this.parent)){
                        HitboxCollectionState hitboxCollectionState = HitboxCollectionState.getHitboxState(this.parent);
                        for(String boneName : this.currentMove.getActiveBones()){
                            List<HitboxState> hitboxes = hitboxCollectionState.getHitboxes(boneName);
                            for(HitboxState hitbox : hitboxes){
                                if(hitbox.getType() == HitboxType.HIT){
                                    hitbox.setActive(false);
                                }
                            }
                        }
                    }
                }
                if(currentMove != null && frameCurrent > currentMove.getWindupFrames() + currentMove.getAttackFrames() + currentMove.getCooldownFrames()){
                    frameCurrent = 0;
                    if(parent.containsKey(EntityDataStrings.CLIENT_ROTATOR_TREE)){
                        RotatorTree.getClientRotatorTree(parent).setActive(false);
                    }
                }
            } break;
            case IDLE: {
                currentMove = null;
                currentMoveset = null;
            } break;
        }
    }

    /**
     * Gets the current attack type
     * @return The current attack type
     */
    protected String getAttackType(){
        String rVal = null;
        if(ClientToolbarState.getClientToolbarState(parent) != null){
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(parent);
            Entity item = clientToolbarState.getCurrentPrimaryItem();
            if(item != null && ItemUtils.isWeapon(item)){
                currentWeapon = item;
                switch(ItemUtils.getWeaponClass(item)){
                    case "sword1h":
                    rVal = EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND;
                    break;
                    case "sword2h":
                    rVal = EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_TWO_HAND;
                    break;
                    case "bow2h":
                    rVal = EntityDataStrings.ATTACK_MOVE_TYPE_BOW_TWO_HAND;
                    break;
                }
            }
        }
        if(rVal == null && this.getMoveset(EntityDataStrings.ATTACK_MOVE_UNARMED) != null){
            return EntityDataStrings.ATTACK_MOVE_UNARMED;
        }
        return rVal;
    }
    
    /**
     * Checks if the tree can perform the specified attack
     * @param attackType The attack type
     * @return true if can attack, false otherwise
     */
    protected boolean canAttack(String attackType){
        if(attackType == null){
            return false;
        }
        if(state != AttackTreeState.IDLE){
            //checks if we have a next move and if we're in the specified range of frames when we're allowed to chain into it
            if(
                currentMove == null ||
                currentMove.getNextMoveId() == null ||
                currentMove.getNextMoveId().equals("") ||
                frameCurrent < currentMove.getMoveChainWindowStart() ||
                frameCurrent > currentMove.getMoveChainWindowEnd()
            ){
                return false;
            }
        }
        if(state == AttackTreeState.IDLE){
            if(!ClientEquipState.hasEquipState(parent)){
                return false;
            }
        }
        if(ClientJumpTree.getClientJumpTree(parent) != null){
            ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(parent);
            if(clientJumpTree.isJumping()){
                return false;
            }
        }
        if(ClientFallTree.getFallTree(parent) != null){
            ClientFallTree fallTree = ClientFallTree.getFallTree(parent);
            if(fallTree.isFalling()){
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the object for next move in the current attack chain
     * @param moveset The moveset to search
     * @param nextMoveId The id of the next move
     * @return The object that corresponds to the id if it exists, otherwise false
     */
    AttackMove getNextMove(List<AttackMove> moveset, String nextMoveId){
        AttackMove rVal = null;
        for(AttackMove move : moveset){
            if(move.getAttackMoveId().equals(nextMoveId)){
                rVal = move;
                break;
            }
        }
        return rVal;
    }

    /**
     * Sets the current attack type of the entity
     * @param attackType the current attack type
     */
    public void setAttackMoveTypeActive(String attackType){
        parent.putData(EntityDataStrings.ATTACK_MOVE_TYPE_ACTIVE, attackType);
    }

    /**
     * Gets the current moveset
     * @param attackType the attack type
     * @return The moveset if it exists
     */
    @SuppressWarnings("unchecked")
    public List<AttackMove> getMoveset(String attackType){
        return (List<AttackMove>)parent.getData(attackType);
    }

    /**
     * Gets the movement penalty applied due to this tree
     * @return The movement penalty
     */
    public double getMovementPenalty(){
        if(currentWeapon != null && ItemUtils.getWeaponDataRaw(currentWeapon) != null){
            WeaponData weaponData = ItemUtils.getWeaponDataRaw(currentWeapon);
            if(this.state != AttackTreeState.IDLE && weaponData.getWeaponActionMovePenalty() != null){
                return weaponData.getWeaponActionMovePenalty();
            }
        }
        return 1.0;
    }

    /**
     * Freezes the animation for a frame or two when a collision occurs
     */
    public void freezeFrame(){
        Actor actor = EntityUtils.getActor(parent);
        if(this.currentMove != null && this.currentMove.getHitstun() != null){
            String animName = this.currentMove.getAttackState().getAnimation().getNameThirdPerson();
            actor.getAnimationData().setFreezeFrames(animName, this.currentMove.getHitstun());
            if(parent == Globals.clientState.playerEntity && !Globals.controlHandler.cameraIsThirdPerson()){
                Actor viewmodelActor = EntityUtils.getActor(Globals.clientState.firstPersonEntity);
                animName = this.currentMove.getAttackState().getAnimation().getNameFirstPerson();
                viewmodelActor.getAnimationData().setFreezeFrames(animName, this.currentMove.getHitstun());
            }
        }
    }

    /**
     * Checks if the target can be collided with
     * @param target The target
     * @return true if can be collided with, false otherwise (ie it has already collided)
     */
    public boolean canCollideEntity(Entity target){
        return !this.collidedEntities.contains(target);
    }

    /**
     * Sets that the current attack has collided with the provided entity
     * @param target The target entity
     */
    public void collideEntity(Entity target){
        this.collidedEntities.add(target);
    }
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(AttackTreeState state){
        this.state = state;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets driftState.
     * </p>
     */
    public AttackTreeDriftState getDriftState(){
        return driftState;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets driftState and handles the synchronization logic for it.
     * </p>
     * @param driftState The value to set driftState to.
     */
    public void setDriftState(AttackTreeDriftState driftState){
        this.driftState = driftState;
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
    public static ClientAttackTree attachTree(Entity parent, Object ... params){
        ClientAttackTree rVal = new ClientAttackTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTATTACKTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTATTACKTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTATTACKTREE_ID);
    }

    /**
     * <p>
     * Gets the ClientAttackTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientAttackTree
     */
    public static ClientAttackTree getClientAttackTree(Entity entity){
        return (ClientAttackTree)entity.getData(EntityDataStrings.TREE_CLIENTATTACKTREE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getAttackTreeStateEnumAsShort(AttackTreeState enumVal){
        switch(enumVal){
            case WINDUP:
                return 0;
            case HOLD:
                return 1;
            case ATTACK:
                return 2;
            case BLOCK_RECOIL:
                return 3;
            case COOLDOWN:
                return 4;
            case IDLE:
                return 5;
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
    public static AttackTreeState getAttackTreeStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return AttackTreeState.WINDUP;
            case 1:
                return AttackTreeState.HOLD;
            case 2:
                return AttackTreeState.ATTACK;
            case 3:
                return AttackTreeState.BLOCK_RECOIL;
            case 4:
                return AttackTreeState.COOLDOWN;
            case 5:
                return AttackTreeState.IDLE;
            default:
                return AttackTreeState.WINDUP;
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
    public static short getAttackTreeDriftStateEnumAsShort(AttackTreeDriftState enumVal){
        switch(enumVal){
            case DRIFT:
                return 0;
            case NO_DRIFT:
                return 1;
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
    public static AttackTreeDriftState getAttackTreeDriftStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return AttackTreeDriftState.DRIFT;
            case 1:
                return AttackTreeDriftState.NO_DRIFT;
            default:
                return AttackTreeDriftState.DRIFT;
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets currentMoveId.
     * </p>
     */
    public String getCurrentMoveId(){
        return currentMoveId;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets currentMoveId and handles the synchronization logic for it.
     * </p>
     * @param currentMoveId The value to set currentMoveId to.
     */
    public void setCurrentMoveId(String currentMoveId){
        this.currentMoveId = currentMoveId;
    }

    /**
     * <p> (Initially) Automatically Generated </p>
     * <p>
     * Performs a state transition on a client state variable.
     * Will be triggered when a server performs a state change.
     * </p>
     * @param newState The new value of the state
     */
    public void transitionState(AttackTreeState newState){
        this.stateTransitionUtil.reset();
        if(newState == AttackTreeState.BLOCK_RECOIL){
            this.stateTransitionUtil.interrupt(AttackTreeState.ATTACK);
        }
        if(newState == AttackTreeState.ATTACK){
            this.collidedEntities.clear();
        }
        this.setState(newState);
    }

    /**
     * <p>
     * Checks if the entity has a ClientAttackTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientAttackTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTATTACKTREE);
    }

}
