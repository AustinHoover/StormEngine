package electrosphere.entity.state.equip;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.item.EquipWhitelist;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.mask.ActorMeshMask;

@SynchronizedBehaviorTree(name = "clientEquipState", isServer = false, correspondingTree="serverEquipState")
/**
 * Client view of items equipped to a given entity
 */
public class ClientEquipState implements BehaviorTree {
    
    //the parent entity of the btree
    Entity parent;

    //the list of available equip points
    List<EquipPoint> equipPoints = new LinkedList<EquipPoint>();

    //the map of point to the equipped entity
    Map<String,Entity> equipMap = new HashMap<String,Entity>();
    
    /**
     * Creates the tree
     * @param parent the entity this is attached to
     * @param equipPoints the list of available points
     */
    private ClientEquipState(Entity parent, Object ... params){
        @SuppressWarnings("unchecked")
        List<EquipPoint> equipPoints = (List<EquipPoint>)params[0];
        this.parent = parent;
        for(EquipPoint point : equipPoints){
            this.equipPoints.add(point);
        }
    }

    /**
     * Gets the list of equipped points
     * @return the list
     */
    public List<String> getEquippedPoints(){
        return new LinkedList<String>(equipMap.keySet());
    }


    /**
     * Gets the list of all equip points
     * @return The list of all equip points
     */
    public List<EquipPoint> getAllEquipPoints(){
        return equipPoints;
    }

    /**
     * Attempts to equip the item
     * @param toEquip the item to equip
     * @param point the point to equip to
     */
    public void commandAttemptEquip(Entity toEquip, EquipPoint point){
        boolean hasEquipped = hasEquippedAtPoint(point.getEquipPointId());
        boolean targetIsItem = ItemUtils.isItem(toEquip);
        String equipItemClass = ItemUtils.getEquipClass(toEquip);
        List<String> pointEquipClassList = point.getEquipClassWhitelist();
        boolean itemIsInPointWhitelist = pointEquipClassList.contains(equipItemClass);
        if(!hasEquipped && targetIsItem && itemIsInPointWhitelist){
            //send packet to server requesting to equip
            String pointName = point.getEquipPointId();
            int serverSideID = Globals.clientState.clientSceneWrapper.mapClientToServerId(toEquip.getId());
            NetworkMessage requestPickupMessage = InventoryMessage.constructclientRequestEquipItemMessage(pointName, serverSideID);
            Globals.clientState.clientConnection.queueOutgoingMessage(requestPickupMessage);
        }
    }
    
    /**
     * Performs the actual logic to term meshes on/off when equpping an item
     * @param toEquip The entity to equip
     * @param point The equipment point to equip to
     */
    public void attemptEquip(Entity toEquip, EquipPoint point){
        boolean hasEquipped = hasEquippedAtPoint(point.getEquipPointId());
        boolean targetIsItem = ItemUtils.isItem(toEquip);
        boolean targetIsAttached = AttachUtils.isAttached(toEquip);
        boolean targetHasWhitelist = ItemUtils.hasEquipList(toEquip);
        String equipItemClass = ItemUtils.getEquipClass(toEquip);
        List<String> pointEquipClassList = point.getEquipClassWhitelist();
        boolean itemIsInPointWhitelist = pointEquipClassList.contains(equipItemClass);
        if(!hasEquipped && targetIsItem && !targetIsAttached && itemIsInPointWhitelist){

            //
            //visual transforms
            if(targetHasWhitelist){
                //depends on the type of creature, must be replacing a mesh
                String parentCreatureId = CreatureUtils.getType(parent);
                List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(toEquip);
                for(EquipWhitelist whitelistItem : whitelist){
                    if(whitelistItem.getCreatureId().equals(parentCreatureId)){
                        //put in map
                        equipMap.put(point.getEquipPointId(),toEquip);
                        String modelName = whitelistItem.getModel();
                        Globals.assetManager.addModelPathToQueue(modelName);
                        Actor parentActor = EntityUtils.getActor(parent);
                        //queue meshes from display model to parent actor
                        ActorMeshMask meshMask = parentActor.getMeshMask();
                        for(String toBlock : whitelistItem.getMeshMaskList()){
                            meshMask.blockMesh(modelName, toBlock);
                        }
                        for(String toDraw : whitelistItem.getMeshList()){
                            meshMask.queueMesh(modelName, toDraw);
                        }
                        //attach to parent bone
                        if(parent != Globals.clientState.firstPersonEntity || Globals.controlHandler.cameraIsThirdPerson()){
                            AttachUtils.clientAttachEntityToEntityAtBone(
                                parent,
                                toEquip,
                                point.getBone(),
                                AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()),
                                AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson())
                            );
                        } else {
                            AttachUtils.clientAttachEntityToEntityAtBone(
                                Globals.clientState.firstPersonEntity,
                                toEquip,
                                point.getFirstPersonBone(),
                                AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorFirstPerson()),
                                AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationFirstPerson())
                            );
                        }
                        //make uncollidable
                        if(PhysicsEntityUtils.containsDBody(toEquip) && toEquip.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                            Globals.clientState.clientSceneWrapper.getCollisionEngine().destroyPhysics(toEquip);
                        }
                        //make untargetable
                        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(toEquip, EntityTags.TARGETABLE);
                        break;
                    }
                }
            } else {
                //does not depend on the type of creature, must be attaching to a bone
                //make sure it's visible
                if(EntityUtils.getActor(toEquip) == null){
                    Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(toEquip));
                    EntityCreationUtils.makeEntityDrawable(toEquip, itemData.getGraphicsTemplate().getModel().getPath());
                    if(itemData.getIdleAnim() != null){
                        toEquip.putData(EntityDataStrings.ANIM_IDLE,itemData.getIdleAnim());
                    }
                }
                //actually equip
                equipMap.put(point.getEquipPointId(),toEquip);
                if(parent != Globals.clientState.firstPersonEntity ||  Globals.controlHandler.cameraIsThirdPerson()){
                    AttachUtils.clientAttachEntityToEntityAtBone(
                        parent,
                        toEquip,
                        point.getBone(),
                        AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()),
                        AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson())
                    );
                } else {
                    AttachUtils.clientAttachEntityToEntityAtBone(
                        Globals.clientState.firstPersonEntity,
                        toEquip,
                        point.getFirstPersonBone(),
                        AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorFirstPerson()),
                        AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationFirstPerson())
                    );
                }
                if(PhysicsEntityUtils.containsDBody(toEquip) && toEquip.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                    Globals.clientState.clientSceneWrapper.getCollisionEngine().destroyPhysics(toEquip);
                }
                Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(toEquip, EntityTags.TARGETABLE);
                GravityUtils.clientAttemptDeactivateGravity(toEquip);
            }
        }

        // if(!hasEquipPrimary() && ItemUtils.isItem(toEquip) && !AttachUtils.isAttached(toEquip)){
        //     if(ItemUtils.hasEquipList(toEquip)){
        //         String parentCreatureId = CreatureUtils.getType(parent);
        //         List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(toEquip);
        //         for(EquipWhitelist whitelistItem : whitelist){
        //             if(whitelistItem.getCreatureId().equals(parentCreatureId)){
        //                 equipPrimary = toEquip;
        //                 String modelName = whitelistItem.getModel();
        //                 Globals.assetManager.addModelPathToQueue(modelName);
        //                 Actor parentActor = EntityUtils.getActor(parent);
        //                 //queue meshes from display model to parent actor
        //                 ActorMeshMask meshMask = parentActor.getMeshMask();
        //                 for(String toBlock : whitelistItem.getMeshMaskList()){
        //                     meshMask.blockMesh(modelName, toBlock);
        //                 }
        //                 for(String toDraw : whitelistItem.getMeshList()){
        //                     meshMask.queueMesh(modelName, toDraw);
        //                 }
        //                 //attach to parent bone
        //                 AttachUtils.attachEntityToEntityAtBone(parent, toEquip, equipPrimaryBoneName);
        //                 //make uncollidable
        //                 if(toEquip.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLISION_BODY) && toEquip.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLIDABLE)){
        //                     CollisionObject rigidBody = (CollisionObject)toEquip.getData(EntityDataStrings.PHYSICS_COLLISION_BODY);
        //                     Globals.collisionEngine.deregisterPhysicsObject(rigidBody);
        //                 }
        //                 //hide toEquip actor
        //                 EntityUtils.setDraw(toEquip, false);
        //                 //make untargetable
        //                 Globals.entityManager.setTargetable(equipPrimary, false);
        //                 break;
        //             }
        //         }
        //     } else {
        //         equipPrimary = toEquip;
        //         AttachUtils.attachEntityToEntityAtBone(parent, toEquip, equipPrimaryBoneName);
        //         if(toEquip.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLISION_BODY) && toEquip.getDataKeys().contains(EntityDataStrings.PHYSICS_COLLIDABLE)){
        //             CollisionObject rigidBody = (CollisionObject)toEquip.getData(EntityDataStrings.PHYSICS_COLLISION_BODY);
        //             Globals.collisionEngine.deregisterPhysicsObject(rigidBody);
        //         }
        //         Globals.entityManager.setTargetable(equipPrimary, false);
        //     }
        // }
    }

    /**
     * Gets the equip point by its name
     * @param name the name of the equip point
     * @return the equip point object if it exists, otherwise null
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
     * @param point the point's name
     * @return the item entity
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
        return entity.containsKey(EntityDataStrings.TREE_CLIENTEQUIPSTATE);
    }

    /**
     * Gets the equip state on the entity
     * @param entity The entity to retrieve equip state from
     * @return The equip state on the entity
     */
    public static ClientEquipState getEquipState(Entity entity){
        return (ClientEquipState)entity.getData(EntityDataStrings.TREE_CLIENTEQUIPSTATE);
    }

    /**
     * Sets the equip state on the entity
     * @param entity The entity to attach the equip state to
     * @param equipState The equip state to attach
     */
    public static void setEquipState(Entity entity, ClientEquipState equipState){
        entity.putData(EntityDataStrings.TREE_CLIENTEQUIPSTATE, equipState);
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
     * Attempts to unequip the item at a given point
     * @param pointId the id of the point
     */
    public void commandAttemptUnequip(String pointId){
        boolean hasEquipped = hasEquippedAtPoint(pointId);
        if(hasEquipped){
            //send packet to server requesting to equip
            NetworkMessage requestUnequipMessage = InventoryMessage.constructclientRequestUnequipItemMessage(pointId);
            Globals.clientState.clientConnection.queueOutgoingMessage(requestUnequipMessage);
        }
    }

    /**
     * Performs the actual logic to turn meshes on/off when unequipping an item
     * @param pointId The equipment point to unequip
     */
    public void clientTransformUnequipPoint(String pointId){
        Entity equipped = equipMap.remove(pointId);
        if(equipped != null){
            boolean targetHasWhitelist = ItemUtils.hasEquipList(equipped);
            EquipPoint point = this.getEquipPoint(pointId);

            //
            //visual transforms
            if(targetHasWhitelist){
                //depends on the type of creature, must be replacing meshes
                String parentCreatureId = CreatureUtils.getType(parent);
                List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(equipped);
                for(EquipWhitelist whitelistItem : whitelist){
                    if(whitelistItem.getCreatureId().equals(parentCreatureId)){
                        //put in map
                        Actor parentActor = EntityUtils.getActor(parent);
                        //queue meshes from display model to parent actor
                        ActorMeshMask meshMask = parentActor.getMeshMask();
                        for(String toUnblock : whitelistItem.getMeshMaskList()){
                            meshMask.unblockMesh(toUnblock);
                        }
                        for(String toDraw : whitelistItem.getMeshList()){
                            meshMask.removeAdditionalMesh(toDraw);
                        }
                        break;
                    }
                }
            } else {
                //does not depend on the type of creature
                AttachUtils.clientDetatchEntityFromEntityAtBone(parent, equipped);
                ClientEntityUtils.destroyEntity(equipped);
            }

            //interrupt animation
            if(point != null){
                Actor thirdPersonActor = EntityUtils.getActor(parent);
                if(point.getEquippedAnimation() != null){
                    TreeDataAnimation animation = point.getEquippedAnimation();
                    //play third person
                    if(thirdPersonActor.getAnimationData().isPlayingAnimation() && thirdPersonActor.getAnimationData().isPlayingAnimation(animation)){
                        if(animation != null){
                            thirdPersonActor.getAnimationData().interruptAnimation(animation,true);
                        }
                        thirdPersonActor.getAnimationData().incrementAnimationTime(0.0001);
                    }
                    
                    //play first person
                    FirstPersonTree.conditionallyInterruptAnimation(parent, animation);
                }
            }
        }
    }

    /**
     * Performs the actual logic to turn meshes on/off when unequipping an item
     * @param pointId The equipment point to unequip
     */
    public void clientTransformUnequip(Entity itemEnt){
        for(Entry<String,Entity> pair : this.equipMap.entrySet()){
            if(pair.getValue().equals(itemEnt)){
                this.clientTransformUnequipPoint(pair.getKey());
            }
        }
    }

    /**
     * Checks if a point has an item equipped
     * @param point the equip point
     * @return true if there is an item equipped, false otherwise
     */
    public boolean hasEquippedAtPoint(String point){
        return equipMap.containsKey(point);
    }


    /**
     * Checks if the player has any attached entities, and if so, makes sure they're attached to the right model
     * This should be used when we change the camera of the player (IE from first to third person or vice versa)
     */
    public void evaluatePlayerAttachments(){
        if(this.parent != Globals.clientState.playerEntity){
            LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Re-evaluating client attachments on non-player entity! This should only be called for the player's entity!"));
        }
        if(Globals.controlHandler.cameraIsThirdPerson()){
            for(String occupiedPoint : this.getEquippedPoints()){
                EquipPoint point = this.getEquipPoint(occupiedPoint);
                Entity toEquip = this.equipMap.get(point.getEquipPointId());
                if(AttachUtils.getParent(toEquip) != Globals.clientState.playerEntity){
                    AttachUtils.clientDetatchEntityFromEntityAtBone(Globals.clientState.firstPersonEntity, toEquip);
                    AttachUtils.clientAttachEntityToEntityAtBone(
                        Globals.clientState.playerEntity,
                        toEquip,
                        point.getBone(),
                        AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()),
                        AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson())
                    );
                } else {
                    AttachUtils.clientUpdateEntityTransforms(toEquip, Globals.clientState.playerEntity);
                }
            }
        } else {
            for(String occupiedPoint : this.getEquippedPoints()){
                EquipPoint point = this.getEquipPoint(occupiedPoint);
                Entity toEquip = this.equipMap.get(point.getEquipPointId());
                if(AttachUtils.getParent(toEquip) != Globals.clientState.firstPersonEntity){
                    AttachUtils.clientDetatchEntityFromEntityAtBone(Globals.clientState.playerEntity, toEquip);
                    AttachUtils.clientAttachEntityToEntityAtBone(
                        Globals.clientState.firstPersonEntity,
                        toEquip,
                        point.getFirstPersonBone(),
                        AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorFirstPerson()),
                        AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationFirstPerson())
                    );
                } else {
                    AttachUtils.clientUpdateEntityTransforms(toEquip, Globals.clientState.firstPersonEntity);
                }
            }
        }
    }


    @Override
    public void simulate(float deltaTime) {
        Actor thirdPersonActor = EntityUtils.getActor(parent);
        //play animations for equip points that have items equipped
        for(EquipPoint point : this.equipPoints){
            if(this.hasEquippedAtPoint(point.getEquipPointId()) && point.getEquippedAnimation() != null){
                TreeDataAnimation animation = point.getEquippedAnimation();
                //play third person
                if(!thirdPersonActor.getAnimationData().isPlayingAnimation() || !thirdPersonActor.getAnimationData().isPlayingAnimation(animation)){
                    if(animation != null){
                        thirdPersonActor.getAnimationData().playAnimation(animation,true);
                    }
                    thirdPersonActor.getAnimationData().incrementAnimationTime(0.0001);
                }
                
                //play first person
                FirstPersonTree.conditionallyPlayAnimation(parent, animation);
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
    public static ClientEquipState attachTree(Entity parent, Object ... params){
        ClientEquipState rVal = new ClientEquipState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTEQUIPSTATE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTEQUIPSTATE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTEQUIPSTATE_ID);
    }
    /**
     * <p>
     * Gets the ClientEquipState of the entity
     * </p>
     * @param entity the entity
     * @return The ClientEquipState
     */
    public static ClientEquipState getClientEquipState(Entity entity){
        return (ClientEquipState)entity.getData(EntityDataStrings.TREE_CLIENTEQUIPSTATE);
    }

    /**
     * <p>
     * Checks if the entity has a ClientEquipState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientEquipState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTEQUIPSTATE);
    }

}
