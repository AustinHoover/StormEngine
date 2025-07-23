package electrosphere.data.entity.creature;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.entity.Entity;
import electrosphere.entity.types.creature.CreatureUtils;

/**
 * The creature type loader
 */
public class CreatureTypeLoader {

     /**
     * The map of entity id -> entity data
     */
    Map<String,CreatureData> idTypeMap = new HashMap<String,CreatureData>();

    /**
     * The list of playable races
     */
    List<String> playableRaceNames = new LinkedList<String>();

    /**
     * The lock on playable races
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Adds a playable race to the loader
     * @param name The race name
     */
    public void putPlayableRace(String name){
        playableRaceNames.add(name);
    }

    /**
     * Gets the list of playable races
     * @return The list of playable race names
     */
    public List<String> getPlayableRaces(){
        List<String> races = null;
        lock.lock();
        races = playableRaceNames;
        lock.unlock();
        return races;
    }

    /**
     * Clears the playable race list
     */
    public void clearPlayableRaces(){
        playableRaceNames.clear();
    }

    /**
     * Loads the list of playable races
     * @param races The list of playable races
     */
    public void loadPlayableRaces(List<String> races){
        lock.lock();
        playableRaceNames = races;
        lock.unlock();
    }

    /**
     * Adds entity data to the loader
     * @param name The id of the entity
     * @param type The entity data
     */
    public void putType(String name, CreatureData type){
        idTypeMap.put(name,type);
    }

    /**
     * Gets entity data from the id of the type
     * @param id The id of the type
     * @return The entity data if it exists, null otherwise
     */
    public CreatureData getType(String id){
        return idTypeMap.get(id);
    }

    /**
     * Gets entity data from the entity
     * @param ent The entity
     * @return The entity data if it exists, null otherwise
     */
    public CreatureData getType(Entity ent){
        return idTypeMap.get(CreatureUtils.getType(ent));
    }

    /**
     * Gets the collection of all entity data
     * @return the collection of all entity data
     */
    public Collection<CreatureData> getTypes(){
        return idTypeMap.values();
    }

    /**
     * Gets the set of all entity data id's stored in the loader
     * @return the set of all entity data ids
     */
    public Set<String> getTypeIds(){
        return idTypeMap.keySet();
    }


}
