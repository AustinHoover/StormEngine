package electrosphere.data.entity.foliage;

import java.util.List;

/**
 * The map of all types of foliage in the game
 */
public class FoliageTypeMap {
    
    /**
     * List of all the foliage types in this file
     */
    List<FoliageType> foliageList;

    /**
     * The list of sub-files under this file
     */
    List<String> files;


    /**
     * Gets the list of all foliage types
     * @return The list of all foliage types
     */
    public List<FoliageType> getFoliageList() {
        return foliageList;
    }
    
    /**
     * Gets a foliage type by its name
     * @param name The name of the foliage type
     * @return The type object
     */
    public FoliageType getFoliage(String name){
        for(FoliageType foliage : foliageList){
            if(foliage.getId().matches(name)){
                return foliage;
            }
        }
        return null;
    }

    /**
     * Gets the list of files under this file
     * @return The list of files
     */
    public List<String> getFiles(){
        return files;
    }
    
}
