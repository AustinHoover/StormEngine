package electrosphere.data.entity.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A loader from a common entity type
 */
public class CommonEntityMap {

    /**
     * The map of entity id -> entity data
     */
    Map<String,CommonEntityType> idTypeMap = new HashMap<String,CommonEntityType>();

    /**
     * Adds entity data to the loader
     * @param name The id of the entity
     * @param type The entity data
     */
    public void putType(String name, CommonEntityType type){
        idTypeMap.put(name,type);
    }

    /**
     * Gets entity data from the id of the type
     * @param id The id of the type
     * @return The entity data if it exists, null otherwise
     */
    public CommonEntityType getType(String id){
        return idTypeMap.get(id);
    }

    /**
     * Gets the collection of all entity data
     * @return the collection of all entity data
     */
    public Collection<CommonEntityType> getTypes(){
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
