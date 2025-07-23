package electrosphere.entity;

import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.interact.ClientInteractionEngine;
import electrosphere.engine.Globals;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.renderer.actor.ActorUtils;
import electrosphere.renderer.actor.instance.TextureInstancedActor;

/**
 * Client only entity utility functions
 */
public class ClientEntityUtils {
    


    /**
     * Called when the creature is first spawned to serialize to all people in its initial chunk
     * @param entity
     * @param position
     */
    public static void initiallyPositionEntity(Entity entity, Vector3d position, Quaterniond rotation){
        //reposition entity
        CollisionObjUtils.clientPositionCharacter(entity, position, rotation);
    }

    /**
     * Called when the creature is first spawned to serialize to all people in its initial chunk
     * @param entity
     * @param position
     */
    public static void repositionEntity(Entity entity, Vector3d position, Quaterniond rotation){
        //reposition entity
        CollisionObjUtils.clientPositionCharacter(entity, position, rotation);
        EntityUtils.setPosition(entity, position);
    }

    /**
     * Destroys an entity on the client
     * @param entity the entity to destroy
     */
    public static void destroyEntity(Entity entity){

        if(entity != null){
            //
            //destroy the child entities, too
            if(AttachUtils.hasChildren(entity)){
                List<Entity> children = AttachUtils.getChildrenList(entity);
                for(Entity child : children){
                    ClientEntityUtils.destroyEntity(child);
                }
            }

            //delete unique model if present
            if(entity.containsKey(EntityDataStrings.HAS_UNIQUE_MODEL)){
                ActorUtils.queueActorForDeletion(entity);
            }

            //check for client-specific stuff
            Globals.renderingEngine.getLightManager().destroyPointLight(entity);

            //deregister all behavior trees
            EntityUtils.cleanUpEntity(entity);

            //instanced actor
            if(TextureInstancedActor.getTextureInstancedActor(entity) != null){
                TextureInstancedActor actor = TextureInstancedActor.getTextureInstancedActor(entity);
                actor.free();
            }

            //is an item in an inventory
            if(ItemUtils.getContainingParent(entity) != null){
                ClientInventoryState.clientRemoveItemFromInventories(entity);
            }


            if(Globals.clientState.clientSceneWrapper != null){
                Globals.clientState.clientSceneWrapper.getScene().deregisterEntity(entity);
                Globals.clientState.clientSceneWrapper.deregisterTranslationMapping(entity);
                if(Globals.clientState.clientSceneWrapper.getCollisionEngine() != null){
                    Globals.clientState.clientSceneWrapper.getCollisionEngine().destroyPhysics(entity);
                }
            }
            if(Globals.clientState.clientScene != null){
                Globals.clientState.clientScene.deregisterEntity(entity);
            }
            HitboxCollectionState.destroyHitboxState(entity,false);
            ClientInteractionEngine.destroyCollidableTemplate(entity);
            if(entity == Globals.clientState.playerEntity && Globals.clientState.firstPersonEntity != null){
                ClientEntityUtils.destroyEntity(Globals.clientState.firstPersonEntity);
            }
        }
    }
    
    /**
     * Sets the scale of the entity
     * @param entity The entity
     * @param scale The scale
     */
    public static void setScale(Entity entity, Vector3d scale){
        EntityUtils.getScale(entity).set(scale);
    }

}
