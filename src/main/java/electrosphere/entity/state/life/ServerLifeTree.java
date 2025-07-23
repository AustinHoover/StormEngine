package electrosphere.entity.state.life;


import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.btree.StateTransitionUtil;
import electrosphere.entity.btree.StateTransitionUtil.StateTransitionUtilItem;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.net.parser.net.message.CombatMessage;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.DataCellSearchUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3d;

import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.data.entity.common.life.HealthSystem;
import electrosphere.data.entity.common.life.loot.LootPool;
import electrosphere.data.entity.common.life.loot.LootTicket;
import electrosphere.engine.Globals;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState;
import electrosphere.entity.state.life.ClientLifeTree.LifeStateEnum;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;

@SynchronizedBehaviorTree(name = "serverLifeTree", isServer = true, correspondingTree="clientLifeTree")
/**
 * Server life state tree
 */
public class ServerLifeTree implements BehaviorTree {
    
    //the current state of the tree
    @SyncedField
    LifeStateEnum state = LifeStateEnum.ALIVE;

    //the parent entity of this life tree
    Entity parent;

    //data used to construct the tree
    HealthSystem healthSystem;

    //state transition util
    StateTransitionUtil stateTransitionUtil;

    //is the entity invincible
    boolean isInvincible = false;
    //the current life value
    int lifeCurrent = 1;
    //the maximum life value
    int lifeMax = 1;
    //the maximum iframes
    int iFrameMaxCount = 1;
    //the current iframe count
    int iFrameCurrent = 0;

    //accumulates collisions and determines if the parent takes damage or blocks them
    List<CollisionEvent> collisionAccumulator = new LinkedList<CollisionEvent>();

    @Override
    public void simulate(float deltaTime) {
        this.handleAccumulatedCollisions();
        switch(state){
            case ALIVE: {
                if(iFrameCurrent > 0){
                    iFrameCurrent--;
                    if(iFrameCurrent == 0){
                        isInvincible = false;
                    }
                } else if(iFrameMaxCount == 0){
                    isInvincible = false;
                }
            } break;
            case DYING: {
                this.stateTransitionUtil.simulate(LifeStateEnum.DYING);
            } break;
            case DEAD: {
                if(ServerLifeTree.hasLootPool(parent)){
                    this.rollLootPool();
                }
                //delete the entity
                ServerEntityUtils.destroyEntity(parent);
            } break;
        }
    }

    /**
     * Revives the entity
     */
    public void revive(){
        this.setState(LifeStateEnum.ALIVE);
        isInvincible = false;
        lifeCurrent = lifeMax;
    }

    /**
     * Damages the entity
     * @param damage The amount of damage to inflict
     */
    public void damage(int damage){
        if(!isInvincible){
            lifeCurrent = lifeCurrent - damage;
            isInvincible = true;
            if(lifeCurrent < 0){
                lifeCurrent = 0;
                this.setState(LifeStateEnum.DYING);
            } else {
                iFrameCurrent = iFrameMaxCount;
            }
        }
    }

    /**
     * Kills the entity
     */
    public void kill(){
        if(this.getState() == LifeStateEnum.ALIVE){
            lifeCurrent = 0;
            this.setState(LifeStateEnum.DYING);
        }
    }

    /**
     * Roll the loot pool
     */
    protected void rollLootPool(){
        if(!ServerLifeTree.hasLootPool(parent)){
            return;
        }
        LootPool lootPool = ServerLifeTree.getLootPool(parent);
        if(lootPool == null || lootPool.getTickets() == null){
            return;
        }
        Random random = new Random();
        Vector3d position = new Vector3d(EntityUtils.getPosition(parent));
        Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
        for(LootTicket ticket : lootPool.getTickets()){
            if(random.nextDouble() <= ticket.getRarity()){
                int numToGen = random.nextInt(ticket.getMinQuantity(), ticket.getMaxQuantity()+1);
                for(int i = 0; i < numToGen; i++){
                    ItemUtils.serverSpawnBasicItem(realm, position, ticket.getItemId());
                }
            }
        }
    }

    /**
     * Checks if the entity is alive
     * @return true if alive, false otherwise
     */
    public boolean isAlive(){
        return this.state == LifeStateEnum.ALIVE;
    }

    /**
     * Handles the collisions that have been accumulated in this tree
     */
    private void handleAccumulatedCollisions(){
        int numCollisions = 0;
        if(collisionAccumulator.size() > 0){
            //get the blocked entities
            List<Entity> blockedEntities = new LinkedList<Entity>();
            for(CollisionEvent event : collisionAccumulator){
                if(event.isBlock && !blockedEntities.contains(event.source)){
                    //don't allow multiple hits per collision
                    blockedEntities.add(event.source);

                    //tracking
                    numCollisions++;

                    //tell clients an impact just happened
                    DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                        CombatMessage.constructserverReportHitboxCollisionMessage(
                            event.source.getId(), 
                            parent.getId(),
                            Globals.engineState.timekeeper.getNumberOfSimFramesElapsed(),
                            event.sourceHitboxData.getHitboxData().getType(),
                            HitboxData.HITBOX_TYPE_BLOCK_CONNECTED,
                            event.position.x,
                            event.position.y,
                            event.position.z
                        )
                    );
                }
            }
            for(CollisionEvent event : collisionAccumulator){
                if(event.isDamage && !blockedEntities.contains(event.source)){
                    //don't allow multiple hits per collision
                    blockedEntities.add(event.source);

                    //tracking
                    numCollisions++;

                    //tell clients an impact just happened
                    DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                        CombatMessage.constructserverReportHitboxCollisionMessage(
                            event.source.getId(), 
                            parent.getId(),
                            Globals.engineState.timekeeper.getNumberOfSimFramesElapsed(),
                            event.sourceHitboxData.getHitboxData().getType(),
                            event.parentHitboxData.getHitboxData().getType(),
                            event.position.x,
                            event.position.y,
                            event.position.z
                        )
                    );

                    //do damage calculation
                    if(this.isAlive()){
                        int damage = 0;
                        if(ItemUtils.isItem(event.source)){
                            damage = ItemUtils.getWeaponDataRaw(event.source).getDamage();
                        } else if(CreatureUtils.isCreature(event.source)){
                            damage = event.sourceHitboxData.getHitboxData().getDamage();
                        }
                        this.damage(damage);
                    }
                }
            }
            collisionAccumulator.clear();
        }
        if(numCollisions > 0){
            LoggerInterface.loggerEngine.DEBUG("Server life tree handled: " + numCollisions + " unique collisions");
        }
    }

    /**
     * Adds a collision event to the life tree
     * @param collisionSource The collision source
     * @param position The position of the collision event
     * @param sourceHitboxData The hitbox data for the source of the collision
     * @param parentHitboxData The hitbox data for the parent of the tree
     * @param isDamage True if this is a damage event
     * @param isBlock True if this is a block event
     */
    public void addCollisionEvent(Entity collisionSource, HitboxState sourceHitboxData, HitboxState parentHitboxData, Vector3d position, boolean isDamage, boolean isBlock){
        CollisionEvent collisionEvent = new CollisionEvent(collisionSource, sourceHitboxData, parentHitboxData, isDamage, isBlock, position);
        this.collisionAccumulator.add(collisionEvent);
    }

    /**
     * Sets the loot pool of the entity
     * @param entity The entity
     * @param lootPool The loot pool
     */
    public static void setLootPool(Entity entity, LootPool lootPool){
        entity.putData(EntityDataStrings.LOOT_POOL, lootPool);
    }

    /**
     * Gets the loot pool on an entity
     * @param entity The entity
     * @return The loot pool if it exists, null otherwise
     */
    public static LootPool getLootPool(Entity entity){
        return (LootPool)entity.getData(EntityDataStrings.LOOT_POOL);
    }

    /**
     * Checks if an entity has a loot pool
     * @param entity The entity
     * @return true if it has a loot pool, false otherwise
     */
    public static boolean hasLootPool(Entity entity){
        return entity.containsKey(EntityDataStrings.LOOT_POOL);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public LifeStateEnum getState(){
        return state;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(LifeStateEnum state){
        this.state = state;
        int value = ClientLifeTree.getLifeStateEnumEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID, FieldIdEnums.TREE_SERVERLIFETREE_SYNCEDFIELD_STATE_ID, value));
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
    public static ServerLifeTree attachTree(Entity parent, Object ... params){
        ServerLifeTree rVal = new ServerLifeTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERLIFETREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID);
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p> Private constructor to enforce using the attach methods </p>
     * <p>
     * Constructor
     * </p>
     * @param parent The parent entity of this tree
     */
    public ServerLifeTree(Entity parent, Object ... params){
        this.parent = parent;
        this.healthSystem = (HealthSystem)params[0];
        this.lifeMax = this.healthSystem.getMaxHealth();
        this.lifeCurrent = this.lifeMax;
        this.iFrameMaxCount = this.healthSystem.getOnDamageIFrames();
        this.iFrameCurrent = 0;
        this.stateTransitionUtil = StateTransitionUtil.create(parent, true, new StateTransitionUtilItem[]{
            StateTransitionUtilItem.create(
                LifeStateEnum.DYING,
                this.healthSystem.getDyingState(),
                () -> {
                    this.setState(LifeStateEnum.DEAD);
                }
            )
        });
        if(this.healthSystem.getLootPool() != null){
            ServerLifeTree.setLootPool(parent, this.healthSystem.getLootPool());
        }
    }

    /**
     * <p>
     * Gets the ServerLifeTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerLifeTree
     */
    public static ServerLifeTree getServerLifeTree(Entity entity){
        return (ServerLifeTree)entity.getData(EntityDataStrings.TREE_SERVERLIFETREE);
    }

    /**
     * A single collision event
     */
    public static class CollisionEvent {

        /**
         * The source of the collision event
         */
        Entity source;

        /**
         * The hitbox data for the source of the collision
         */
        HitboxState sourceHitboxData;

        /**
         * The hitbox data for the parent of the tree
         */
        HitboxState parentHitboxData;

        /**
         * True if this is a damage event
         */
        boolean isDamage;

        /**
         * True if this is a block event
         */
        boolean isBlock;

        /**
         * The position of the collision
         */
        Vector3d position;

        /**
         * Constructor
         * @param source The source of the collision
         * @param sourceHitboxData The hitbox data for the source of the collision
         * @param parentHitboxData The hitbox data for the parent of the tree
         * @param isDamage True if this is a damage event
         * @param isBlock True if this is a block event
         * @param position The position of the collision
         */
        public CollisionEvent(Entity source, HitboxState sourceHitboxData, HitboxState parentHitboxData, boolean isDamage, boolean isBlock, Vector3d position){
            this.source = source;
            this.sourceHitboxData = sourceHitboxData;
            this.parentHitboxData = parentHitboxData;
            this.isDamage = isDamage;
            this.isBlock = isBlock;
            this.position = position;
        }

    }

    /**
     * <p>
     * Checks if the entity has a ServerLifeTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerLifeTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERLIFETREE);
    }

}
