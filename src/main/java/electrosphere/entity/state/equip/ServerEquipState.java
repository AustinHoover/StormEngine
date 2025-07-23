package electrosphere.entity.state.equip;


import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.creature.block.BlockSystem;
import electrosphere.data.entity.creature.block.BlockVariant;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.item.EquipWhitelist;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.block.ServerBlockTree;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;
import electrosphere.net.server.protocol.InventoryProtocol;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;
import electrosphere.server.entity.poseactor.PoseActor;
import electrosphere.server.utils.ServerScriptUtils;

@SynchronizedBehaviorTree(name = "serverEquipState", isServer = true, correspondingTree="clientEquipState")
/**
 * Server view of items equipped onto an entity
 */
public class ServerEquipState implements BehaviorTree {
    
    //the parent entity of this equip state
    Entity parent;

    //the list of available equip points
    List<EquipPoint> equipPoints = new LinkedList<EquipPoint>();
    //the map of equip point id -> entity equipped at said point
    Map<String,Entity> equipMap = new HashMap<String,Entity>();
    
    public ServerEquipState(Entity parent, Object ... params){
        @SuppressWarnings("unchecked")
        List<EquipPoint> equipPoints = (List<EquipPoint>)params[0];
        this.parent = parent;
        for(EquipPoint point : equipPoints){
            this.equipPoints.add(point);
        }
    }

    public List<String> equippedPoints(){
        return new LinkedList<String>(equipMap.keySet());
    }

    public void commandAttemptEquip(Entity toEquip, EquipPoint point){
        boolean hasEquipped = hasEquippedAtPoint(point.getEquipPointId());
        boolean targetIsItem = ItemUtils.isItem(toEquip);
        String equipItemClass = ItemUtils.getEquipClass(toEquip);
        List<String> pointEquipClassList = point.getEquipClassWhitelist();
        boolean itemIsInPointWhitelist = pointEquipClassList.contains(equipItemClass);
        if(!hasEquipped && targetIsItem && itemIsInPointWhitelist){
            serverAttemptEquip(toEquip, point);
        }
    }

    /**
     * Attempts to equip an item
     * @param inInventoryEntity The item to equip
     * @param point The point to equip to
     */
    public void serverAttemptEquip(Entity inInventoryEntity, EquipPoint point){
        boolean hasEquipped = hasEquippedAtPoint(point.getEquipPointId());
        boolean targetIsItem = ItemUtils.isItem(inInventoryEntity);
        boolean targetHasWhitelist = ItemUtils.hasEquipList(inInventoryEntity);
        String equipItemClass = ItemUtils.getEquipClass(inInventoryEntity);
        List<String> pointEquipClassList = point.getEquipClassWhitelist();
        boolean itemIsInPointWhitelist = pointEquipClassList.contains(equipItemClass);
        if(inInventoryEntity == null){
            throw new Error("In inventory entity is null!");
        }
        if(ItemUtils.getContainingParent(inInventoryEntity) == null){
            throw new Error("Containing parent is null!");
        }
        if(!hasEquipped && targetIsItem && itemIsInPointWhitelist){
            //hydrate inventory item
            String itemType = ItemUtils.getType(inInventoryEntity);
            Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
            Entity inWorldItem = ItemUtils.serverSpawnBasicItem(realm,new Vector3d(0,0,0),itemType);
            //bind in world with in inventory
            ItemUtils.setRealWorldEntity(inInventoryEntity, inWorldItem);
            
            //
            //Visual transforms
            if(targetHasWhitelist){
                //depends on the type of creature
                String parentCreatureId = CreatureUtils.getType(parent);
                List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(inWorldItem);
                for(EquipWhitelist whitelistItem : whitelist){
                    if(whitelistItem.getCreatureId().equals(parentCreatureId)){
                        //put in map
                        equipMap.put(point.getEquipPointId(),inWorldItem);
                        String modelName = whitelistItem.getModel();
                        Globals.assetManager.addModelPathToQueue(modelName);
                        //attach to parent bone
                        AttachUtils.serverAttachEntityToEntityAtBone(
                            parent,
                            inWorldItem,
                            point.getBone(),
                            AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()),
                            AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson())
                        );
                        //make uncollidable
                        if(PhysicsEntityUtils.containsDBody(inWorldItem) && inWorldItem.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                            Realm inWorldRealm = Globals.serverState.realmManager.getEntityRealm(inWorldItem);
                            inWorldRealm.getCollisionEngine().destroyPhysics(inWorldItem);
                        }
                        //make untargetable
                        ServerEntityTagUtils.removeTagFromEntity(inWorldItem, EntityTags.TARGETABLE);
                        break;
                    }
                }
            } else {
                //does not depend on the type of creature
                equipMap.put(point.getEquipPointId(),inWorldItem);
                AttachUtils.serverAttachEntityToEntityAtBone(
                    parent,
                    inWorldItem,
                    point.getBone(),
                    AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()),
                    AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson())
                );
                if(PhysicsEntityUtils.containsDBody(inWorldItem) && inWorldItem.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                    Realm inWorldRealm = Globals.serverState.realmManager.getEntityRealm(inWorldItem);
                    inWorldRealm.getCollisionEngine().destroyPhysics(inWorldItem);
                }
                ServerEntityTagUtils.removeTagFromEntity(inWorldItem, EntityTags.TARGETABLE);
                GravityUtils.serverAttemptDeactivateGravity(inWorldItem);
            }

            //
            //update block state based on what we have equipped
            this.updateBlockVariant();
            
            //we need to send two packets
            //1) Remove item from original inventory
            //2) Add item with ID to "equipped" inventory
            //let clients know of the updates
            //get the parent (typically creature) that contains the in-inventory item
            Entity containingEntity = ItemUtils.getContainingParent(inInventoryEntity);
            //actually switch containers
            if(InventoryUtils.hasNaturalInventory(parent)){
                UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(parent);
                naturalInventory.removeItem(inInventoryEntity);
            }
            if(InventoryUtils.hasToolbarInventory(parent)){
                RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(parent);
                if(ServerToolbarState.getServerToolbarState(parent) != null){
                    ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(parent);
                    serverToolbarState.unequip(inWorldItem);
                }
                toolbarInventory.tryRemoveItem(inInventoryEntity);
            }
            if(InventoryUtils.hasEquipInventory(parent)){
                RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(parent);
                equipInventory.tryRemoveItem(inInventoryEntity);
                equipInventory.addItem(point.getEquipPointId(), inInventoryEntity);
            }
            //if they're a player, let the player know that the item has moved container
            if(CreatureUtils.hasControllerPlayerId(containingEntity)){
                //get player
                int playerId = CreatureUtils.getControllerPlayerId(containingEntity);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(playerId);
                //tell the player they don't have the item anymore
                NetworkMessage inventoryMessage = InventoryMessage.constructserverCommandMoveItemContainerMessage(
                    inInventoryEntity.getId(), 
                    InventoryProtocol.INVENTORY_TYPE_EQUIP,
                    point.getEquipPointId()
                    );
                controllerPlayer.addMessage(inventoryMessage);
            }
            //get the chunk the equipper is in, and broadcast to that chunk that they equipped the item
            //get datacell
            ServerDataCell dataCell = DataCellSearchUtils.getEntityDataCell(parent);
            //broadcast attach entity
            int equipperId = parent.getId();
            String equipPointId = point.getEquipPointId();
            int inWorldItemId = inWorldItem.getId();
            NetworkMessage attachMessage = InventoryMessage.constructserverCommandEquipItemMessage(
                equipperId,
                InventoryProtocol.INVENTORY_TYPE_EQUIP,
                equipPointId,
                inWorldItemId,
                itemType
            );
            //actually send the packet
            dataCell.broadcastNetworkMessage(attachMessage);

            //Fire signal to script engine to equip
            ServerScriptUtils.fireSignalOnEntity(parent, "equipItem");
        }
    }
    
    /**
     * Gets an equip point by its name
     * @param name The name of the equip point
     * @return The equip point if it exists, null otherwise
     */
    public EquipPoint getEquipPoint(String name){
        for(EquipPoint point : equipPoints){
            if(point.getEquipPointId().equals(name)){
                return point;
            }
        }
        return null;
    }

    /**
     * Gets the item equipped at a point
     * @param point The point id
     * @return The item if it exists, null otherwise
     */
    public Entity getEquippedItemAtPoint(String point){
        return equipMap.get(point);
    }

    /**
     * Returns whether the entity has an equip state
     * @param entity The entity to check
     * @return True if the entity contains an equip state, false otherwise
     */
    public static boolean hasEquipState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVEREQUIPSTATE);
    }

    /**
     * Gets the equip state on the entity
     * @param entity The entity to retrieve equip state from
     * @return The equip state on the entity
     */
    public static ServerEquipState getEquipState(Entity entity){
        return (ServerEquipState)entity.getData(EntityDataStrings.TREE_SERVEREQUIPSTATE);
    }

    /**
     * Sets the equip state on the entity
     * @param entity The entity to attach the equip state to
     * @param equipState The equip state to attach
     */
    public static void setEquipState(Entity entity, ServerEquipState equipState){
        entity.putData(EntityDataStrings.TREE_SERVEREQUIPSTATE, equipState);
    }
    
    // public void drop(Entity entity){
        // if(hasEquipPrimary()){
        //     AttachUtils.detatchEntityFromEntityAtBone(parent,equipPrimary);
        //     if(equipPrimary.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLISION_BODY) && equipPrimary.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLIDABLE)){
        //         CollisionObject rigidBody = (CollisionObject)equipPrimary.getData(EntityDataStrings.PHYSICS_COLLISION_BODY);
        //         Globals.collisionEngine.registerPhysicsObject(rigidBody);
        //     }
        //     Globals.entityManager.setTargetable(equipPrimary, true);
        //     equipPrimary = null;
        // }
    // }

    /**
     * Commands the equip state to unequip an item at a given equip point
     * @param pointId The equip point
     */
    public void commandAttemptUnequip(String pointId){
        boolean hasEquipped = hasEquippedAtPoint(pointId);
        if(hasEquipped){
            //perform the attempt
            serverAttemptUnequip(pointId);
        }
    }

    /**
     * Attempts the unequip on the server
     * @param pointId The slot to attempt to unequip
     */
    public void serverAttemptUnequip(String pointId){
        boolean hasNaturalInventory = InventoryUtils.hasNaturalInventory(parent);
        boolean hasEquipInventory = InventoryUtils.hasEquipInventory(parent);
        boolean hasEquipped = hasEquippedAtPoint(pointId);
        if(hasEquipped && hasEquipInventory && hasNaturalInventory){
            UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(parent);
            RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(parent);
            //switch the inventory it's under
            Entity ejectedItem = equipInventory.getItemSlot(pointId);
            equipInventory.removeItemSlot(pointId);
            naturalInventory.addItem(ejectedItem);
            //destroy in world item
            serverTransformUnequipPoint(pointId);
            //tell all clients to unequip the world item
            //get datacell
            ServerDataCell dataCell = DataCellSearchUtils.getEntityDataCell(parent);
            //broadcast attach entity
            NetworkMessage unequipMessage = InventoryMessage.constructserverCommandUnequipItemMessage(parent.getId(), InventoryProtocol.INVENTORY_TYPE_EQUIP, pointId);
            //actually send the packet
            dataCell.broadcastNetworkMessage(unequipMessage);
            //if the parent is a player entity, tell the player about the updated inventory stuff
            if(CreatureUtils.hasControllerPlayerId(parent)){
                //get player
                int playerId = CreatureUtils.getControllerPlayerId(parent);
                Player controllerPlayer = Globals.serverState.playerManager.getPlayerFromId(playerId);
                //tell the player they don't have the item anymore
                NetworkMessage inventoryMessage = InventoryMessage.constructserverCommandMoveItemContainerMessage(
                    ejectedItem.getId(), 
                    InventoryProtocol.INVENTORY_TYPE_NATURAL,
                    pointId
                    );
                controllerPlayer.addMessage(inventoryMessage);
            }
        }
    }

    /**
     * Performs the transform to unequip an item from an equip point
     * @param pointId The equip point id
     */
    public void serverTransformUnequipPoint(String pointId){
        Entity equipped = equipMap.remove(pointId);
        if(equipped != null){
            boolean targetHasWhitelist = ItemUtils.hasEquipList(equipped);
            EquipPoint point = this.getEquipPoint(pointId);

            //
            //Visual transforms
            if(targetHasWhitelist){
                //have to do fancy mesh removal nonsense
                //basically the reverse of below
                // List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(equipped);
                // for(EquipWhitelist whitelistItem : whitelist){
                //     if(whitelistItem.getCreatureId().equals(parentCreatureId)){
                //         //put in map
                //         equipMap.put(point.getEquipPointId(),toEquip);
                //         String modelName = whitelistItem.getModel();
                //         Globals.assetManager.addModelPathToQueue(modelName);
                //         Actor parentActor = EntityUtils.getActor(parent);
                //         //queue meshes from display model to parent actor
                //         ActorMeshMask meshMask = parentActor.getMeshMask();
                //         for(String toBlock : whitelistItem.getMeshMaskList()){
                //             meshMask.blockMesh(modelName, toBlock);
                //         }
                //         for(String toDraw : whitelistItem.getMeshList()){
                //             meshMask.queueMesh(modelName, toDraw);
                //         }
                //         //attach to parent bone
                //         AttachUtils.attachEntityToEntityAtBone(parent, toEquip, point.getBone());
                //         //make uncollidable
                //         if(toEquip.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLISION_BODY) && toEquip.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLIDABLE)){
                //             CollisionObject rigidBody = (CollisionObject)toEquip.getData(EntityDataStrings.PHYSICS_COLLISION_BODY);
                //             Globals.collisionEngine.deregisterPhysicsObject(rigidBody);
                //         }
                //         //hide toEquip actor
                //         EntityUtils.setDraw(toEquip, false);
                //         //make untargetable
                //         Globals.entityManager.setTargetable(toEquip, false);
                //         break;
                //     }
                // }
            } else {
                ServerEntityUtils.destroyEntity(equipped);
            }

            //interrupt animation
            if(point != null){
                PoseActor thirdPersonActor = EntityUtils.getPoseActor(parent);
                if(point.getEquippedAnimation() != null){
                    TreeDataAnimation animation = point.getEquippedAnimation();
                    //play third person
                    if(thirdPersonActor.isPlayingAnimation() && thirdPersonActor.isPlayingAnimation(animation)){
                        if(animation != null){
                            thirdPersonActor.interruptAnimation(animation,true);
                        }
                        thirdPersonActor.incrementAnimationTime(0.0001);
                    }
                }
            }

            //
            //update block state based on what we have equipped
            this.updateBlockVariant();
        }
    }

    /**
     * Gets whether an item is equipped at a point or not
     * @param point The point to check
     * @return true if an item is equipped at the point, false otherwise
     */
    public boolean hasEquippedAtPoint(String point){
        return equipMap.containsKey(point);
    }

    /**
     * Updates the server block variant based on what item is equipped
     */
    private void updateBlockVariant(){
        ServerBlockTree blockTree = ServerBlockTree.getServerBlockTree(parent);
        if(blockTree != null){

            List<EquipPoint> pointsThatCanBlock = new LinkedList<EquipPoint>();
            for(EquipPoint point : equipPoints){
                if(point.getCanBlock()){
                    pointsThatCanBlock.add(point);
                }
            }

            BlockSystem blockData = blockTree.getBlockSystem();
            for(EquipPoint point : pointsThatCanBlock){
                Entity item = getEquippedItemAtPoint(point.getEquipPointId());
                if(item != null && Globals.gameConfigCurrent.getItemMap().getItem(item) != null && Globals.gameConfigCurrent.getItemMap().getItem(item).getBlockSystem() != null){
                    BlockVariant blockVariant = blockData.getVariantForPointWithItem(point.getEquipPointId(),ItemUtils.getEquipClass(item));

                    //TODO: refactor to allow sending more than one variant at a time
                    //ie if you have two items equipped and you want to block with both
                    //think equipping a sword and a shield at once
                    if(blockVariant != null){
                        blockTree.setCurrentBlockVariant(blockVariant.getVariantId());
                    } else {
                        LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Equipped item to equip point that does not have assigned block variant!!"));
                    }
                }
            }
        }
    }

    @Override
    public void simulate(float deltaTime) {
        PoseActor thirdPersonActor = EntityUtils.getPoseActor(parent);
        //play animations for equip points that have items equipped
        for(EquipPoint point : this.equipPoints){
            if(this.hasEquippedAtPoint(point.getEquipPointId()) && point.getEquippedAnimation() != null){
                TreeDataAnimation animation = point.getEquippedAnimation();
                //play third person
                if(!thirdPersonActor.isPlayingAnimation() || !thirdPersonActor.isPlayingAnimation(animation)){
                    if(animation != null){
                        thirdPersonActor.playAnimation(animation);
                    }
                    thirdPersonActor.incrementAnimationTime(0.0001);
                }
            }
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
    public static ServerEquipState attachTree(Entity parent, Object ... params){
        ServerEquipState rVal = new ServerEquipState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVEREQUIPSTATE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVEREQUIPSTATE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVEREQUIPSTATE_ID);
    }
    /**
     * <p>
     * Gets the ServerEquipState of the entity
     * </p>
     * @param entity the entity
     * @return The ServerEquipState
     */
    public static ServerEquipState getServerEquipState(Entity entity){
        return (ServerEquipState)entity.getData(EntityDataStrings.TREE_SERVEREQUIPSTATE);
    }

    /**
     * <p>
     * Checks if the entity has a ServerEquipState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerEquipState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVEREQUIPSTATE);
    }

}
