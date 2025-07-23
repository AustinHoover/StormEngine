package electrosphere.data.entity.foliage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FoliageTypeLoader {
    
    /**
     * The map of entity id -> entity data
     */
    Map<String,FoliageType> idTypeMap = new HashMap<String,FoliageType>();

    /**
     * Adds entity data to the loader
     * @param name The id of the entity
     * @param type The entity data
     */
    public void putType(String name, FoliageType type){
        idTypeMap.put(name,type);
    }

    /**
     * Gets entity data from the id of the type
     * @param id The id of the type
     * @return The entity data if it exists, null otherwise
     */
    public FoliageType getType(String id){
        return idTypeMap.get(id);
    }

    /**
     * Gets the collection of all entity data
     * @return the collection of all entity data
     */
    public Collection<FoliageType> getTypes(){
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
