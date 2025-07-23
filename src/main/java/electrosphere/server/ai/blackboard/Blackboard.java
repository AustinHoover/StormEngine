package electrosphere.server.ai.blackboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Blackboard for storing data with the tree
 */
public class Blackboard {
    
    /**
     * The values in the blackboard
     */
    private Map<String,Object> values = new HashMap<String,Object>();

    /**
     * Puts a value into the blackboard
     * @param key The key
     * @param value The value
     */
    public void put(String key, Object value){
        values.put(key,value);
    }

    
    /**
     * Gets a value from the blackboard
     * @param key The key of the value
     * @return The value if it exists, null otherwise
     */
    public Object get(String key){
        if(this.has(key)){
            return values.get(key);
        }
        return null;
    }

    /**
     * Checks if the blackboard contains a value at a given key
     * @param key The key
     * @return true if the blackboard has a value for that key, false otherwise
     */
    public boolean has(String key){
        return values.containsKey(key);
    }

    /**
     * Deletes a value from the blackboard
     * @param key The key to delete the value from
     */
    public void delete(String key){
        if(this.has(key)){
            values.remove(key);
        }
    }

}
