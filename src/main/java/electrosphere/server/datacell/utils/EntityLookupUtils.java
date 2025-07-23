package electrosphere.server.datacell.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;

/**
 * Utilities for looking up entities
 * Broke the id<->entity logic out from realm manager as it didn't make sense there
 */
public class EntityLookupUtils {

    /**
     * map of all entities by their ID
     */
    static Map<Integer,Entity> idToEntityMap = new HashMap<Integer, Entity>();

    /**
     * Registers the entity on the server side so that it can be looked up by id
     * @param entity The entity to register
     */
    public static void registerServerEntity(Entity entity){
        idToEntityMap.put(entity.getId(), entity);
    }

    /**
     * Gets the entity with a given id
     * @param id The id
     * @return The entity if it exists, null otherwise
     */
    public static Entity getEntityById(int id){
        return idToEntityMap.get(id);
    }

    /**
     * Removes an entity from registration with the server
     * @param entity The entity to remove
     */
    public static void removeEntity(Entity entity){
        idToEntityMap.remove(entity.getId());
    }

    /**
     * Returns whether this entity was created by the server or the client
     * @param entity The entity to test
     * @return True if the entity was created by the server, false otherwise
     */
    public static boolean isServerEntity(Entity entity){
        return idToEntityMap.containsKey(entity.getId());
    }

    /**
     * !!!DANGER!!! USE IN DEBUG/LEVEL EDITOR ONLY
     * Gets all entities tracked by the entity lookup utils
     * @return The collection of all server entities
     */
    public static Collection<Entity> getAllEntities(){
        return idToEntityMap.values();
    }

    /**
     * Gets the server equivalent of a client entity
     * @param clientEntity The client entity
     * @return The equivalent entity on the server if it exists, null otherwise
     */
    public static Entity getServerEquivalent(Entity clientEntity){
        if(EntityLookupUtils.isServerEntity(clientEntity)){
            throw new Error("Trying to get server equivalent of a server entity!");
        }
        int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(clientEntity.getId());
        Entity serverEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
        return serverEntity;
    }
    
}
