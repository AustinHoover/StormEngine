package electrosphere.net.synchronization.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.entity.Entity;

/**
 * A service that tracks entities which have behavior trees that need to be looked up in list
 * Principally used by the synchronization logic to iterate over all networked behavior trees on either server or client
 */
public class EntityValueTrackingService {

    //The main map that contains all the entities that are tracked and their respective behavior tree ids
    Map<Entity,List<Integer>> treeMap = new HashMap<Entity,List<Integer>>();
    

    /**
     * Gets the list of tree ids attached to a given entity
     * @param entity The entity to check
     * @return If the entity is tracked and has attached trees, will return the list of attached trees. If the entity is not tracked or has no attached trees, will return null.
     */
    public List<Integer> getEntityTrees(Entity entity){
        return treeMap.get(entity);
    }

    /**
     * Attaches a behavior tree id to an entity
     * @param entity The entity ot attach to
     * @param treeId The numeric id of the behavior tree type
     */
    public void attachTreeToEntity(Entity entity, int treeId){
        if(treeMap.containsKey(entity)){
            if(!treeMap.get(entity).contains(treeId)){
                treeMap.get(entity).add(treeId);
            }
        } else {
            List<Integer> treeList = new LinkedList<Integer>();
            treeList.add(treeId);
            treeMap.put(entity,treeList);
        }
    }

    /**
     * Removes a behavior tree id from an entity
     * @param entity The entity
     * @param treeId The numeric id of the behavior tree type
     */
    public void detatchTreeFromEntity(Entity entity, int treeId){
        if(treeMap.containsKey(entity)){
            treeMap.get(entity).remove((Object)treeId);
        }
    }

    /**
     * Deregisters the entity from the tracking service
     * @param entity The entity
     */
    public void deregisterEntity(Entity entity){
        treeMap.remove(entity);
    }

}
