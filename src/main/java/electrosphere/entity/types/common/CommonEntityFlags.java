package electrosphere.entity.types.common;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;

/**
 * Handling for common entity flags
 */
public class CommonEntityFlags {

    /**
     * Checks if the entity should be serialized
     * @param entity The entity
     * @return true if should be serialized, false otherwise
     */
    public static boolean shouldBeSerialized(Entity entity){
        return entity.containsKey(EntityDataStrings.SHOULD_SERIALIZE);
    }

    /**
     * Checks if the entity should be synchronized between server and client
     * @param entity The entity
     * @return true if should be synchronized, false otherwise
     */
    public static boolean shouldBeSynchronized(Entity entity){
        return !entity.containsKey(EntityDataStrings.SHOULD_SYNCHRONIZE) || (boolean)entity.getData(EntityDataStrings.SHOULD_SYNCHRONIZE);
    }

    /**
     * Sets the synchronization status of the entity
     * @param entity The entity
     * @param shouldSynchronize true if it should be synchronized, false otherwise
     */
    public static void setSynchronization(Entity entity, boolean shouldSynchronize){
        entity.putData(EntityDataStrings.SHOULD_SYNCHRONIZE, shouldSynchronize);
    }

    /**
     * Gets whether the entity is interactable or not
     * @param entity The entity
     * @return true if it is interactable, false otherwise
     */
    public static boolean isInteractable(Entity entity){
        return entity.containsKey(EntityDataStrings.INTERACTABLE) && (boolean)entity.getData(EntityDataStrings.INTERACTABLE);
    }

    /**
     * Sets whether this entity is interactable or not
     * @param entity The entity
     * @param isInteractable true if it is interactable, false otherwise
     */
    public static void setIsInteractable(Entity entity, boolean isInteractable){
        entity.putData(EntityDataStrings.INTERACTABLE, isInteractable);
    }

}
