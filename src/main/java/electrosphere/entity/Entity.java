package electrosphere.entity;

import java.util.HashMap;
import java.util.Set;

import org.graalvm.polyglot.HostAccess.Export;


/**
 * An entity
 */
public class Entity {
    
    
    
    /**
     * The iterator used to assign entities unique ids
     */
    static int entity_id_iterator = 0;
    
    /**
     * The id of this entity
     */
    int id;
    
    /**
     * The data associated with this entity
     */
    HashMap<String,Object> data;
    
    /**
     * Gets the id of this entity
     * @return The id
     */
    @Export
    public int getId() {
        return id;
    }
    
    /**
     * Sets the id of this entity
     * @param id The id
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Puts some data into this entity
     * @param key The key for the data
     * @param o The data
     */
    public void putData(String key, Object o){
        data.put(key,o);
    }

    /**
     * Checks if an entity contains a key
     * @param key The key
     * @return true if the entity contains the key, false otherwise
     */
    public boolean containsKey(String key){
        return data.containsKey(key);
    }
    
    /**
     * Gets some data on the entity
     * @param key The key for the data
     * @return The data if it exists, null otherwise
     */
    public Object getData(String key){
        return data.get(key);
    }
    
    /**
     * Constructs an entity
     */
    protected Entity(){
        data = new HashMap<String,Object>();
        id = entity_id_iterator;
        entity_id_iterator++;
    }
    
    /**
     * Removes data from an entity based on the key of the data
     * @param key The key
     */
    public void removeData(String key){
        data.remove(key);
    }

    /**
     * Gets the set of all keys on the entity
     * @return The set of all keys
     */
    public Set<String> getKeys(){
        return this.data.keySet();
    }
}
