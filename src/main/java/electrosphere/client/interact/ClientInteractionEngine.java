package electrosphere.client.interact;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.ui.menu.ingame.InteractionTargetMenu;
import electrosphere.collision.CollisionBodyCreation;
import electrosphere.collision.CollisionEngine;
import electrosphere.collision.PhysicsUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.common.CommonEntityUtils;

/**
 * Manages the interaction state 
 */
public class ClientInteractionEngine {

    /**
     * The lock used for thread-safe-ing the engine
     */
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * The list of entities that are currently registered for interaction
     */
    private static List<Entity> interactables = new LinkedList<Entity>();
    
    /**
     * Attaches a collidable template to a given entity
     * @param rVal The entity
     * @param physicsTemplate The collidable template
     */
    public static void attachCollidableTemplate(Entity rVal, CollidableTemplate physicsTemplate){
        DBody rigidBody = null;
        Collidable collidable;
        long categoryBit = Collidable.TYPE_OBJECT_BIT;
        CollisionEngine.lockOde();
        lock.lock();
        interactables.add(rVal);
        switch(physicsTemplate.getType()){
            case CollidableTemplate.COLLIDABLE_TYPE_CYLINDER: {

                //
                //create dbody
                rigidBody = CollisionBodyCreation.createCylinderBody(
                    Globals.clientState.clientSceneWrapper.getInteractionEngine(),
                    physicsTemplate.getDimension1(),
                    physicsTemplate.getDimension2(),
                    categoryBit
                );

                //
                //set offset from center of entity position
                CollisionBodyCreation.setOffsetPosition(
                    Globals.clientState.clientSceneWrapper.getInteractionEngine(),
                    rigidBody,
                    new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                );

                //
                //create collidable and link to structures
                collidable = new Collidable(rVal, Collidable.TYPE_OBJECT, true);

                //
                //store values
                Matrix4d offsetTransform = new Matrix4d().translationRotate(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW() //rotate
                );
                rVal.putData(EntityDataStrings.INTERACTION_OFFSET_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.INTERACTION_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.INTERACTION_COLLIDABLE, collidable);
                rVal.putData(EntityDataStrings.INTERACTION_BODY, rigidBody);

                Globals.clientState.clientSceneWrapper.getInteractionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(rVal));
            } break;
            case CollidableTemplate.COLLIDABLE_TYPE_CUBE: {
                //
                //create dbody
                rigidBody = CollisionBodyCreation.createCubeBody(
                    Globals.clientState.clientSceneWrapper.getInteractionEngine(),
                    new Vector3d(physicsTemplate.getDimension1(),physicsTemplate.getDimension2(),physicsTemplate.getDimension3()),
                    categoryBit
                );

                //
                //set offset from center of entity position
                CollisionBodyCreation.setOffsetPosition(
                    Globals.clientState.clientSceneWrapper.getInteractionEngine(),
                    rigidBody,
                    new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                );

                //
                //create collidable and link to structures
                collidable = new Collidable(rVal, Collidable.TYPE_OBJECT, true);

                //
                //store values
                Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                    1, 1, 1 //scale
                );
                rVal.putData(EntityDataStrings.INTERACTION_OFFSET_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.INTERACTION_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.INTERACTION_COLLIDABLE, collidable);
                rVal.putData(EntityDataStrings.INTERACTION_BODY, rigidBody);

                Globals.clientState.clientSceneWrapper.getInteractionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(rVal));
            } break;
            case CollidableTemplate.COLLIDABLE_TYPE_CAPSULE: {
                //
                //create dbody
                rigidBody = CollisionBodyCreation.createCapsuleBody(
                    Globals.clientState.clientSceneWrapper.getInteractionEngine(),
                    physicsTemplate.getDimension1(),
                    physicsTemplate.getDimension2(),
                    categoryBit
                );

                //
                //set offset from center of entity position
                CollisionBodyCreation.setOffsetPosition(
                    Globals.clientState.clientSceneWrapper.getInteractionEngine(),
                    rigidBody,
                    new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                );

                //
                //create collidable and link to structures
                collidable = new Collidable(rVal, Collidable.TYPE_OBJECT, true);

                //
                //store values
                Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                    1, 1, 1 //scale
                );
                rVal.putData(EntityDataStrings.INTERACTION_OFFSET_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.INTERACTION_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.INTERACTION_COLLIDABLE, collidable);
                rVal.putData(EntityDataStrings.INTERACTION_BODY, rigidBody);

                Globals.clientState.clientSceneWrapper.getInteractionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(rVal));
            } break;
            default: {
                throw new Error("Unsupported shape type! " + physicsTemplate.getType());
            }
        }
        lock.unlock();
        CollisionEngine.unlockOde();
    }

    /**
     * Destroys the interaction collidable attached to this entity
     * @param rVal The entity
     */
    public static void destroyCollidableTemplate(Entity rVal){
        lock.lock();
        CollisionEngine interactionEngine = Globals.clientState.clientSceneWrapper.getInteractionEngine();
        DBody body = null;
        Collidable collidable = null;
        if(rVal.containsKey(EntityDataStrings.INTERACTION_BODY)){
            body = (DBody)rVal.getData(EntityDataStrings.INTERACTION_BODY);
        }
        if(rVal.containsKey(EntityDataStrings.INTERACTION_COLLIDABLE)){
            collidable = (Collidable)rVal.getData(EntityDataStrings.INTERACTION_COLLIDABLE);
        }
        if(body != null){
            PhysicsUtils.destroyBody(interactionEngine, body);
            if(collidable != null){
                interactionEngine.deregisterCollisionObject(body, collidable);
            }
        }
        interactables.remove(rVal);
        lock.unlock();
    }

    /**
     * Updates the positions of all hitboxes
     */
    private static void updateInteractableTransforms(){
        lock.lock();
        CollisionEngine interactionEngine = Globals.clientState.clientSceneWrapper.getInteractionEngine();
        for(Entity entity : interactables){
            if(entity != null){
                Vector3d entityPosition = EntityUtils.getPosition(entity);
                Quaterniond entityRotation = EntityUtils.getRotation(entity);
                DBody body = (DBody)entity.getData(EntityDataStrings.INTERACTION_BODY);
                PhysicsUtils.setRigidBodyTransform(interactionEngine, new Vector3d(entityPosition), new Quaterniond(entityRotation), body);
            }
        }
        lock.unlock();
    }

    /**
     * Ray cast to pick the current interaction target
     * @return The current interaction target if it exists, null otherwise
     */
    public static Entity rayCast(Vector3d centerPos, Vector3d eyePos){
        lock.lock();
        CollisionEngine interactionEngine = Globals.clientState.clientSceneWrapper.getInteractionEngine();
        //update position of all interactables
        ClientInteractionEngine.updateInteractableTransforms();
        //ray cast
        Entity target = interactionEngine.rayCast(centerPos, eyePos, CollisionEngine.DEFAULT_INTERACT_DISTANCE, Collidable.MASK_NO_TERRAIN);
        lock.unlock();
        return target;
    }

    /**
     * Updates the interaction target label
     */
    public static void updateInteractionTargetLabel(){
        if(Globals.clientState.playerEntity != null && Globals.clientState.playerCamera != null){
            //clear block cursor
            Globals.cursorState.hintClearBlockCursor();

            boolean set = false;
            Entity camera = Globals.clientState.playerCamera;
            Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(camera));
            Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(camera));
            Entity target = ClientInteractionEngine.rayCast(centerPos, new Vector3d(eyePos).mul(-1));
            if(target != null){
                String text = CommonEntityUtils.getEntitySubtype(target);
                InteractionTargetMenu.setInteractionTargetString(text);
                set = true;
            }
            // if(!set){
            //     target = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCast(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
            //     if(target != null){
            //         EntityType type = CommonEntityUtils.getEntityType(target);
            //         if(type == null){
            //             throw new Error("Entity does not have a type defined!");
            //         }
            //         switch(type){
            //             case CREATURE: {
            //                 InteractionTargetMenu.setInteractionTargetString(CommonEntityUtils.getEntitySubtype(target));
            //                 set = true;
            //             } break;
            //             case ITEM: {
            //                 InteractionTargetMenu.setInteractionTargetString(CommonEntityUtils.getEntitySubtype(target));
            //                 set = true;
            //             } break;
            //             case FOLIAGE: {
            //                 InteractionTargetMenu.setInteractionTargetString(CommonEntityUtils.getEntitySubtype(target));
            //                 set = true;
            //             } break;
            //             default: {
            //                 //silently ignore
            //             } break;
            //         }
            //     }
            // }
            // if(!set){
            //     Vector3d collisionPosition = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(centerPos, new Vector3d(eyePos).mul(-1), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
            //     if(
            //         collisionPosition != null &&
            //         collisionPosition.distance(centerPos) < CollisionEngine.DEFAULT_INTERACT_DISTANCE &&
            //         collisionPosition.x >= 0 && collisionPosition.y >= 0 && collisionPosition.z >= 0
            //     ){
            //         //grab block at point
            //         BlockChunkData blockChunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(Globals.clientState.clientWorldData.convertRealToWorldSpace(collisionPosition), 0);
            //         if(blockChunkData != null){
            //             Vector3i blockPos = ClientWorldData.convertRealToLocalBlockSpace(new Vector3d(collisionPosition).add(new Vector3d(eyePos).mul(-BlockChunkData.BLOCK_SIZE_MULTIPLIER / 2.0f)));
            //             if(!blockChunkData.isEmpty(blockPos.x, blockPos.y, blockPos.z)){
            //                 short type = blockChunkData.getType(blockPos.x, blockPos.y, blockPos.z);
            //                 String text = Globals.gameConfigCurrent.getBlockData().getTypeFromId(type).getName();
            //                 InteractionTargetMenu.setInteractionTargetString(text);
            //                 Globals.cursorState.hintShowBlockCursor();
            //                 Globals.cursorState.hintClampToExistingBlock();
            //                 set = true;
            //             }
            //         }
            //         //if we didn't find a block type, try terrain
            //         if(!set){
            //             ChunkData chunkData = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(Globals.clientState.clientWorldData.convertRealToWorldSpace(collisionPosition), 0);
            //             if(chunkData != null){
            //                 int voxelType = chunkData.getType(ClientWorldData.convertRealToVoxelSpace(new Vector3d(collisionPosition).add(new Vector3d(ServerTerrainChunk.VOXEL_SIZE / 2.0f))));
            //                 if(voxelType != ServerTerrainChunk.VOXEL_TYPE_AIR){
            //                     String text = Globals.gameConfigCurrent.getVoxelData().getTypeFromId(voxelType).getName();
            //                     InteractionTargetMenu.setInteractionTargetString(text);
            //                     set = true;
            //                 }
            //             }
            //         }
            //     }
            // }
            if(!set){
                InteractionTargetMenu.setInteractionTargetString("");
            }
        }
    }

    /**
     * Checks whether the entity has an interaction body or not
     * @param entity The entity
     * @return true if it has an interaction body, false otherwise
     */
    public static boolean hasInteractionBody(Entity entity){
        return entity.containsKey(EntityDataStrings.INTERACTION_BODY);
    }

    /**
     * Gets the interaction body on the entity
     * @param entity The entity
     * @return The body if it exists, null otherwise
     */
    public static DBody getInteractionBody(Entity entity){
        return (DBody)entity.getData(EntityDataStrings.INTERACTION_BODY);
    }

    /**
     * Gets the template for the interaction body
     * @param entity The entity
     * @return The tempalte if it exists, null otherwise
     */
    public static CollidableTemplate getInteractionTemplate(Entity entity){
        return (CollidableTemplate)entity.getData(EntityDataStrings.INTERACTION_TEMPLATE);
    }

    /**
     * Gets the number of interactibles on the client
     * @return The number of interactibles
     */
    public static int getInteractiblesCount(){
        lock.lock();
        int rVal = interactables.size();
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the number of interactibles on the client
     * @return The number of interactibles
     */
    public static int getCollidablesCount(){
        lock.lock();
        CollisionEngine interactionEngine = Globals.clientState.clientSceneWrapper.getInteractionEngine();
        int rVal = interactionEngine.getCollidables().size();
        lock.unlock();
        return rVal;
    }

}
