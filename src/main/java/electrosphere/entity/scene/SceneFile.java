package electrosphere.entity.scene;

import java.util.LinkedList;
import java.util.List;

/**
 * Model class for scene files
 */
public class SceneFile {
    
    /**
     * The entities in the scene
     */
    List<EntityDescriptor> entities;

    /**
     * The paths relative to the assets folder of each script to be loaded when the scene is loaded
     */
    List<String> scriptPaths;

    /**
     * The initial script to run when the scene is loaded into the engine
     */
    String initScriptPath;

    /**
     * The realm this scene is created within
     */
    RealmDescriptor realmDescriptor;

    /**
     * Controls whether the save utils will store a copy of the scene file in the save or not
     */
    boolean createSaveInstance;

    /**
     * Loads all cells on initialization
     */
    boolean loadAllCells;

    /**
     * The seed for the random number generator
     */
    long seed = 0;


    /**
     * Private constructor
     */
    private SceneFile(){
        
    }

    /**
     * Creates a scene file
     * @return The scene file
     */
    public static SceneFile createSceneFile(){
        SceneFile rVal = new SceneFile();
        rVal.entities = new LinkedList<EntityDescriptor>();
        rVal.scriptPaths = new LinkedList<String>();
        rVal.initScriptPath = null;
        rVal.realmDescriptor = new RealmDescriptor();
        rVal.createSaveInstance = false;
        rVal.loadAllCells = false;
        return rVal;
    }

    /**
     * Gets the paths of all scripts in this scene
     * @return The list of all paths
     */
    public List<String> getScriptPaths(){
        return scriptPaths;
    }

    /**
     * Gets the entity descriptors for all entities created on init of this scene
     * @return The list of all entity descriptors
     */
    public List<EntityDescriptor> getEntities(){
        return entities;
    }

    /**
     * Gets the path to the initial script run when this scene is initialized
     * @return The path to the initial script
     */
    public String getInitScriptPath(){
        return initScriptPath;
    }

    /**
     * Gets the realm descriptor
     * @return The realm descriptor
     */
    public RealmDescriptor getRealmDescriptor(){
        return realmDescriptor;
    }

    /**
     * Gets whether the save utils will store a copy of the scene file in the save or not
     * @return true if should create instance of scene file in save, false otherwise
     */
    public boolean getCreateSaveInstance(){
        return createSaveInstance;
    }

    /**
     * Sets whether the save utils will store a copy of the scene file in the save or not
     * @param createSaveInstancetrue if should create instance of scene file in save, false otherwise
     */
    public void setCreateSaveInstance(boolean createSaveInstance){
        this.createSaveInstance = createSaveInstance;
    }

    /**
     * Gets whether the scene should load all cells on initialization or not
     * @return true if should load all cells on init, false otherwise
     */
    public boolean loadAllCells(){
        return loadAllCells;
    }

    /**
     * Gets the seed of the scene file
     * @return The seed
     */
    public long getSeed(){
        return seed;
    }

}
