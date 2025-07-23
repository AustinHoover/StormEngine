package electrosphere.data.entity.common;

import java.util.List;

/**
 * A recursive file structure for loading types of entities
 */
public class CommonEntityLoader {
    
    /**
     * The entities in this file
     */
    List<CommonEntityType> objects;

    /**
     * All child files of this one
     */
    List<String> files;

    /**
     * Gets the entities stored in this file
     * @return The list of entities
     */
    public List<CommonEntityType> getEntities(){
        return objects;
    }

    /**
     * Gets all child files of this one
     * @return The list of all child files
     */
    public List<String> getFiles(){
        return files;
    }

}
