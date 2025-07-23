package electrosphere.entity.state.attack;


import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.data.entity.common.treedata.TreeDataState;
import electrosphere.data.entity.creature.attack.AttackMove;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.item.WeaponData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.attack.ClientAttackTree.AttackTreeDriftState;
import electrosphere.entity.state.attack.ClientAttackTree.AttackTreeState;
import electrosphere.entity.state.collidable.Impulse;
import electrosphere.entity.state.equip.ServerEquipState;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxType;
import electrosphere.entity.state.movement.fall.ServerFallTree;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.entity.state.rotator.ServerRotatorTree;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.entity.types.projectile.ProjectileUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.renderer.actor.Actor;
import electrosphere.server.datacell.Realm;

import java.util.LinkedList;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Server basic attack tree
 */
@SynchronizedBehaviorTree(name = "serverAttackTree", isServer = true, correspondingTree="clientAttackTree")
public class ServerAttackTree implements BehaviorTree {
    
    //the state of the attack tree
    @SyncedField(serverSendTransitionPacket = true)
    AttackTreeState state;

    //the state of drifting caused by the attack animation
    @SyncedField
    AttackTreeDriftState driftState;
    
    Entity parent;
    
    List<EntityMessage> networkMessageQueue = new LinkedList<EntityMessage>();
    
    long lastUpdateTime = 0;
    
    float frameCurrent;
    
    String animationName = "SwingWeapon";
    
    int maxFrame = 60;

    List<AttackMove> currentMoveset = null;
    @SyncedField
    String currentMoveId = null; //the id of the current move -- used to synchronize the move to client
    AttackMove currentMove = null; //the actual current move object
    Entity currentWeapon = null;
    boolean currentMoveHasWindup;
    boolean currentMoveCanHold;
    boolean stillHold = true;
    boolean firesProjectile = false;
    String projectileToFire = null;
    String attackingPoint = null;

    /**
     * The minimum number of fall frames required before it blocks attacking
     */
    int MIN_FALL_FRAMES_TO_BLOCK_ATTACK = 3;

    /**
     * The list of entities that have collided with the current attack
     */
    List<Entity> collidedEntities = new LinkedList<Entity>();

    //The state transition util
    StateTransitionUtil stateTransitionUtil;
    
    /**
     * Private constructor
     * @param e The parent entity
     * @param params The data to construct the tree with
     */
    private ServerAttackTree(Entity e, Object ... params){
        state = AttackTreeState.IDLE;
        driftState = AttackTreeDriftState.NO_DRIFT;
        parent = e;
        this.stateTransitionUtil = StateTransitionUtil.create(parent, true, new StateTransitionUtilItem[]{
            StateTransitionUtilItem.create(
                AttackTreeState.WINDUP,
                () -> {
                    TreeDataState state = currentMove.getWindupState();
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                () -> {
                    TreeDataState state = currentMove.getWindupState();
                    if(state == null){
                        return null;
                    } else {
                        return state.getAudioData();
                    }
                },
                () -> {
                    if(currentMoveCanHold && stillHold){
                        this.setState(AttackTreeState.HOLD);
                    } else {
                        this.setState(AttackTreeState.ATTACK);
                    }
                    this.stateTransitionUtil.interrupt(AttackTreeState.WINDUP);
                }
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.HOLD,
                () -> {
                    if(currentMove != null && currentMove.getHoldState() == null){
                        return null;
                    } else {
                        return currentMove.getHoldState().getAnimation();
                    }
                },
                () -> {
                    TreeDataState state = currentMove.getHoldState();
                    if(state == null){
                        return null;
                    } else {
                        return state.getAudioData();
                    }
                },
                null
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.ATTACK,
                () -> {
                    TreeDataState state = currentMove.getAttackState();
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                null,
                () -> {
                    this.stateTransitionUtil.interrupt(AttackTreeState.ATTACK);
                    this.setState(AttackTreeState.COOLDOWN);
                }
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.BLOCK_RECOIL,
                () -> {
                    if(currentMove != null && currentMove.getBlockRecoilState() != null){
                        return currentMove.getBlockRecoilState().getAnimation();
                    } else {
                        return null;
                    }
                },
                null,
                () -> {
                    this.stateTransitionUtil.interrupt(AttackTreeState.BLOCK_RECOIL);
                    this.setState(AttackTreeState.COOLDOWN);
                }
            ),
            StateTransitionUtilItem.create(
                AttackTreeState.COOLDOWN,
                () -> {
                    TreeDataState state = currentMove.getCooldownState();
                    if(state == null){
                        return null;
                    } else {
                        return state.getAnimation();
                    }
                },
                null,
                () -> {
                    this.setState(AttackTreeState.IDLE);
                    this.stateTransitionUtil.interrupt(AttackTreeState.COOLDOWN);
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
                if(currentMove == null){
                    currentMove = currentMoveset.get(0);
                } else {
                    currentMove = this.getNextMove(currentMoveset,currentMove.getNextMoveId());
                }
                if(currentMove != null){
                    firesProjectile = currentMove.getFiresProjectile();
                    if(firesProjectile){
                        projectileToFire = ItemUtils.getWeaponDataRaw(currentWeapon).getProjectileModel();
                    }

                    //set initial stuff (this alerts the client as well)
                    this.setCurrentMoveId(currentMove.getAttackMoveId());

                    //start tree
                    if(currentMove.getWindupState() != null){
                        this.setState(AttackTreeState.WINDUP);
                    } else if(currentMove.getAttackState() != null){
                        this.setState(AttackTreeState.ATTACK);
                    } else {
                        LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Trying to start attacking tree, but current move does not have windup or attack states defined!"));
                    }
                    this.stateTransitionUtil.reset();
                    //intuit can hold from presence of windup anim
                    currentMoveCanHold = currentMove.getHoldState() != null;
                    //clear collided list
                    this.collidedEntities.clear();
                    frameCurrent = 0;
                } else {
                    this.setState(AttackTreeState.IDLE);
                }
            }
        }
    }

    /**
     * Releases the tree from holding its animation
     */
    public void release(){
        stillHold = false;
    }
    
    /**
     * Interrupts the tree
     */
    public void interrupt(){
        this.setState(AttackTreeState.IDLE);
        //activate hitboxes
        List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
        for(Entity currentAttached : attachedEntities){
            if(HitboxCollectionState.hasHitboxState(currentAttached)){
                HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                currentState.setActive(false);
            }
        }
    }
    
    /**
     * Causes the attack tree to instead enter recoil state
     */
    public void recoilFromBlock(){
        if(currentMove != null){
            this.setState(AttackTreeState.BLOCK_RECOIL);
            //deactivate hitboxes
            List<Entity> attachedEntities = AttachUtils.getChildrenList(parent);
            for(Entity currentAttached : attachedEntities){
                if(HitboxCollectionState.hasHitboxState(currentAttached)){
                    HitboxCollectionState currentState = HitboxCollectionState.getHitboxState(currentAttached);
                    currentState.setActive(false);
                }
            }
        }
    }
    
    @Override
    public void simulate(float deltaTime){
        frameCurrent = frameCurrent + (float)Globals.engineState.timekeeper.getDeltaFrames();
        Vector3d movementVector = CreatureUtils.getFacingVector(parent);
        
        //parse attached network messages
        for(EntityMessage message : networkMessageQueue){
            networkMessageQueue.remove(message);
            switch(message.getMessageSubtype()){
                case ATTACKUPDATE:
                    EntityUtils.getPosition(parent).set(message.getpositionX(),message.getpositionY(),message.getpositionZ());
                    break;
                case STARTATTACK: {
                    this.start();
                } break;
                default:
                //silently ignore
                break;
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

        // if(state != AttackTreeState.IDLE){
        //     System.out.println(frameCurrent);
        // }
        
        //state machine
        switch(state){
            case WINDUP: {
                if(parent.containsKey(EntityDataStrings.SERVER_ROTATOR_TREE)){
                    ServerRotatorTree.getServerRotatorTree(parent).setActive(true);
                }
                this.stateTransitionUtil.simulate(AttackTreeState.WINDUP);
            } break;
            case HOLD: {
                this.stateTransitionUtil.simulate(AttackTreeState.HOLD);
                if(!stillHold){
                    this.setState(AttackTreeState.ATTACK);
                    this.stateTransitionUtil.interrupt(AttackTreeState.HOLD);
                }
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
                if(firesProjectile && projectileToFire != null){
                    //spawn projectile
                    Vector3d spawnPosition = new Vector3d(0,0,0);
                    Quaterniond arrowRotation = new Quaterniond();
                    String targetBone = null;
                    ServerEquipState equipState = ServerEquipState.getEquipState(parent);
                    EquipPoint weaponPoint = null;
                    if((weaponPoint = equipState.getEquipPoint(attackingPoint)) != null){
                        targetBone = weaponPoint.getBone();
                    }
                    if(targetBone != null){
                        Actor parentActor = EntityUtils.getActor(parent);
                        //transform bone space
                        spawnPosition = new Vector3d(parentActor.getAnimationData().getBonePosition(targetBone));
                        spawnPosition = spawnPosition.mul(((Vector3f)EntityUtils.getScale(parent)));
                        Quaterniond rotation = EntityUtils.getRotation(parent);
                        spawnPosition = spawnPosition.rotate(new Quaterniond(rotation.x,rotation.y,rotation.z,rotation.w));
                        //transform worldspace
                        spawnPosition.add(new Vector3d(EntityUtils.getPosition(parent)));
                        //set
                        // EntityUtils.getPosition(currentEntity).set(position);
                        //set rotation
    //                    Quaternionf rotation = parentActor.getBoneRotation(targetBone);
    //                    EntityUtils.getRotation(currentEntity).set(rotation).normalize();
                        // Vector3d facingAngle = CreatureUtils.getFacingVector(parent);
                        arrowRotation = parentActor.getAnimationData().getBoneRotation(targetBone);
                        // EntityUtils.getRotation(currentEntity).rotationTo(MathUtils.ORIGIN_VECTORF, new Vector3f((float)facingAngle.x,(float)facingAngle.y,(float)facingAngle.z)).mul(parentActor.getBoneRotation(targetBone)).normalize();
                    }
                    Vector3f initialVector = new Vector3f((float)movementVector.x,(float)movementVector.y,(float)movementVector.z).normalize();
                    Realm parentRealm = Globals.serverState.realmManager.getEntityRealm(parent);
                    ProjectileUtils.serverSpawnBasicProjectile(parentRealm, projectileToFire, spawnPosition, arrowRotation, 750, initialVector, 0.03f);
                    projectileToFire = null;
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
                if(frameCurrent > currentMove.getWindupFrames() + currentMove.getAttackFrames() + currentMove.getCooldownFrames()){
                    this.setState(AttackTreeState.IDLE);
                    this.stateTransitionUtil.interrupt(AttackTreeState.COOLDOWN);
                    frameCurrent = 0;
                    if(parent.containsKey(EntityDataStrings.SERVER_ROTATOR_TREE)){
                        ServerRotatorTree.getServerRotatorTree(parent).setActive(false);
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
     * Adds a network message to the tree
     * @param networkMessage The network message
     */
    public void addNetworkMessage(EntityMessage networkMessage) {
        networkMessageQueue.add(networkMessage);
    }

    /**
     * Gets the current attack type
     * @return The current attack type
     */
    protected String getAttackType(){
        String rVal = null;
        if(ServerToolbarState.getServerToolbarState(parent) != null){
            ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(parent);
            Entity item = serverToolbarState.getRealWorldItem();
            if(item == null){
                return EntityDataStrings.ATTACK_MOVE_UNARMED;
            }
            if(ItemUtils.isWeapon(item)){
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
            if(!ServerEquipState.hasEquipState(parent)){
                return false;
            }
        }
        if(ServerJumpTree.getServerJumpTree(parent) != null){
            ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(parent);
            if(serverJumpTree.isJumping()){
                return false;
            }
        }
        if(ServerFallTree.getFallTree(parent) != null){
            ServerFallTree serverFallTree = ServerFallTree.getFallTree(parent);
            if(serverFallTree.isFalling() && serverFallTree.getFrameCurrent() > MIN_FALL_FRAMES_TO_BLOCK_ATTACK){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this tree can start an attack
     * @return true if can attack, false otherwise
     */
    public boolean canAttack(){
        String attackType = getAttackType();
        if(attackType == null){
            return false;
        }
        return this.canAttack(attackType);
    }

    /**
     * Gets the next attack move
     * @param moveset The moveset
     * @param nextMoveId The next move's id
     * @return The next move if it exists, null otherwise
     */
    private AttackMove getNextMove(List<AttackMove> moveset, String nextMoveId){
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
     * Checks whether the attack tree is active or not
     * @return true if active, false otherwise
     */
    public boolean isAttacking(){
        return this.state != AttackTreeState.IDLE;
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
     * Checks if the entity has a copy of this tree
     * @param target The entity
     * @return true if has a copy of this tree, false otherwise
     */
    public static boolean hasAttackTree(Entity target){
        return target.containsKey(EntityDataStrings.TREE_SERVERATTACKTREE);
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
        int value = ClientAttackTree.getAttackTreeStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructServerNotifyBTreeTransitionMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID, FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_STATE_ID, value));
        }
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
        int value = ClientAttackTree.getAttackTreeDriftStateEnumAsShort(driftState);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID, FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_DRIFTSTATE_ID, value));
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
    public static ServerAttackTree attachTree(Entity parent, Object ... params){
        ServerAttackTree rVal = new ServerAttackTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERATTACKTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID);
    }

    /**
     * <p>
     * Gets the ServerAttackTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerAttackTree
     */
    public static ServerAttackTree getServerAttackTree(Entity entity){
        return (ServerAttackTree)entity.getData(EntityDataStrings.TREE_SERVERATTACKTREE);
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
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStringStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID, FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_CURRENTMOVEID_ID, currentMoveId));
        }
    }

    /**
     * <p>
     * Checks if the entity has a ServerAttackTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerAttackTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERATTACKTREE);
    }

}
