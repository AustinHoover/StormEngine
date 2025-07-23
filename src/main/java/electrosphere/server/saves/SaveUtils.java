package electrosphere.server.saves;

import java.util.List;

import electrosphere.engine.Globals;
import electrosphere.entity.scene.RealmDescriptor;
import electrosphere.entity.scene.SceneFile;
import electrosphere.entity.scene.SceneLoader;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.db.DatabaseController;
import electrosphere.server.db.DatabaseUtils;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.MacroDataLoader;
import electrosphere.server.physics.fluid.generation.DefaultFluidGenerator;
import electrosphere.server.physics.fluid.manager.ServerFluidManager;
import electrosphere.server.physics.terrain.generation.DefaultChunkGenerator;
import electrosphere.server.physics.terrain.generation.ProceduralChunkGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;
import electrosphere.util.FileUtils;

/**
 * Utilities for dealing with saves (init, loading, storing, etc)
 */
public class SaveUtils {
    
    public static String deriveSaveDirectoryPath(String saveName){
        String path = "./saves/" + saveName;
        if(path.charAt(path.length() - 1) != '/'){
            path = path + "/";
        }
        return path;
    }
    
    /**
     * Initializes a save directory
     * @param saveName
     * @return true if initialized save, false if couldn't initialize
     */
    public static boolean initSave(String saveName){
        String dirPath = deriveSaveDirectoryPath(saveName);
        //check if exists
        if(FileUtils.checkFileExists(dirPath)){
            return false;
        }
        // create dir
        if(!FileUtils.createDirectory(dirPath)){
            //we for some unknown reason, couldn't make the save dir
            return false;
        }
        //init db file
        if(!DatabaseUtils.initCentralDBFile(dirPath)){
            return false;
        }
        return true;
    }


    //all directories to create within the save each time it is initialized
    private static final String[] directoryStructure = new String[]{
        "/content"
    };

    /**
     * Initializes a save directory, overwrites if one is already there
     * @param saveName Name of the save
     * @param sceneFile The scene descriptor file
     * @return true if initialized save, false if couldn't initialize
     */
    public static boolean createOrOverwriteSave(String saveName, SceneFile sceneFile){
        String dirPath = deriveSaveDirectoryPath(saveName);
        //check if exists
        if(FileUtils.checkFileExists(dirPath)){
            FileUtils.recursivelyDelete(dirPath);
        }
        // create dir
        if(!FileUtils.createDirectory(dirPath)){
            //we for some unknown reason, couldn't make the save dir
            return false;
        }
        //create full directory structure
        for(String subDir : directoryStructure){
            String fullPath = dirPath + subDir;
            if(!FileUtils.createDirectory(fullPath)){
                //we for some unknown reason, couldn't make the save dir
                return false;
            }
        }
        //create main save files
        SaveUtils.createSave(saveName, sceneFile);
        return true;
    }

    /**
     * Creates a save
     * @param saveName The name of the save
     * @param sceneFile The scene descriptor file
     * @return Returns true if the save was created successfully, returns false if the save was not created successfully
     */
    public static boolean createSave(String saveName, SceneFile sceneFile){
        String dirPath = deriveSaveDirectoryPath(saveName);

        //create save file
        Save save = new Save(saveName);
        Globals.serverState.currentSave = save; //chunk map saving requires global save to be set
        FileUtils.serializeObjectToSavePath(saveName, "/save.json", save);

        //write scene file
        if(sceneFile.getCreateSaveInstance()){
            FileUtils.serializeObjectToSavePath(saveName, "/scene.json", sceneFile);
        }

        //create server structures
        if(sceneFile.getRealmDescriptor().getType() == RealmDescriptor.REALM_DESCRIPTOR_PROCEDURAL){
            //generate terrain and save to disk
            //
            //Server world data
            ServerWorldData serverWorldData = ServerWorldData.createGriddedRealmWorldData(ServerWorldData.PROCEDURAL_WORLD_SIZE);
            FileUtils.serializeObjectToSavePath(saveName, "./world.json", serverWorldData);
            //terrain manager
            ServerTerrainManager serverTerrainManager = new ServerTerrainManager(serverWorldData, sceneFile.getSeed(), new ProceduralChunkGenerator(serverWorldData, false));
            serverTerrainManager.generate(sceneFile.getRealmDescriptor());
            serverTerrainManager.save(saveName);
            //fluid manager
            ServerFluidManager serverFluidManager = new ServerFluidManager(serverWorldData, serverTerrainManager, 0, new DefaultFluidGenerator());
            serverFluidManager.save(saveName);

            //attach terrain to world data prior to pushing into macro data
            serverWorldData.setManagers(serverTerrainManager, serverFluidManager, null);

            //macro data
            MacroData macroData = MacroData.generateWorld(sceneFile.getSeed(), serverWorldData);
            macroData.save(saveName);
        } else {
            //just save to disk
            //
            //Server world data
            ServerWorldData serverWorldData = ServerWorldData.createGriddedRealmWorldData(sceneFile.getRealmDescriptor().getGriddedRealmSize());
            FileUtils.serializeObjectToSavePath(saveName, "./world.json", serverWorldData);
            //terrain manager
            ServerTerrainManager serverTerrainManager = new ServerTerrainManager(serverWorldData, 0, new DefaultChunkGenerator());
            serverTerrainManager.save(saveName);
            //fluid manager
            ServerFluidManager serverFluidManager = new ServerFluidManager(serverWorldData, serverTerrainManager, 0, new DefaultFluidGenerator());
            serverFluidManager.save(saveName);
        }

        //init db file
        if(!DatabaseUtils.initCentralDBFile(dirPath)){
            return false;
        }


        return true;
    }

    /**
     * Overwrites a save's data
     * @param saveName The name of the save
     */
    public static void overwriteSave(String saveName){

        //write save file
        FileUtils.serializeObjectToSavePath(saveName, "/save.json", Globals.serverState.currentSave);

        //write server structures
        Globals.serverState.realmManager.save(saveName);

        //store character service
        Globals.serverState.characterService.saveAll();

        LoggerInterface.loggerEngine.WARNING("Finished saving " + saveName);
    }

    /**
     * Deletes a save
     * @param saveName The name of the save
     */
    public static void deleteSave(String saveName){
        String dirPath = deriveSaveDirectoryPath(saveName);
        //check if exists
        if(FileUtils.checkFileExists(dirPath)){
            FileUtils.recursivelyDelete(dirPath);
        }
    }
    
    /**
     * Loads a save into the server
     * @param saveName The name of the save
     * @return true always
     */
    public static boolean loadSave(String saveName, boolean isLevelEditor){
        String dirPath = deriveSaveDirectoryPath(saveName);

        //load save file
        Globals.serverState.currentSave = FileUtils.loadObjectFromSavePath(saveName, "/save.json", Save.class);


        //load world data
        ServerWorldData serverWorldData = null;
        if(FileUtils.checkSavePathExists(saveName, "/world.json")){
            //load from save itself
            LoggerInterface.loggerEngine.INFO("Load world data from save " + saveName);
            serverWorldData = ServerWorldData.loadWorldData(saveName, false);
        } else if(FileUtils.checkFileExists("/Scenes/" + saveName + "/world.json")){
            //load from defined scene
            LoggerInterface.loggerEngine.INFO("Load world data from scene " + saveName);
            serverWorldData = ServerWorldData.loadWorldData(saveName, true);
        } else {
            //The world data is neither defined in the save itself nor in the assets scene files
            throw new IllegalStateException("Trying to load a save that does not contain world data!");
        }

        //load scene file
        if(FileUtils.checkSavePathExists(saveName, "/scene.json")){
            //load from save itself
            LoggerInterface.loggerEngine.INFO("Load scene data from save " + saveName);
            SceneLoader.serverInstantiateSaveSceneFile(saveName, serverWorldData, isLevelEditor);
        } else if(FileUtils.getAssetFile("/Scenes/" + saveName + "/scene.json").exists()){
            //load from defined scene
            LoggerInterface.loggerEngine.INFO("Load scene data from scene " + saveName);
            SceneLoader.serverInstantiateAssetSceneFile(saveName, serverWorldData, isLevelEditor);
        } else {
            //The scene is neither defined in the save itself nor in the assets scene files
            throw new IllegalStateException("Trying to load a save that does not contain a scene!");
        }

        //load db
        String dbFilePath = FileUtils.sanitizeFilePath(dirPath) + "/central" + DatabaseController.FILE_EXT;
        Globals.serverState.dbController.connect(dbFilePath);

        //Load data stored in db
        MacroDataLoader.loadAllAliveCharacters();

        return true;
    }
    
    /**
     * Gets the list of all saves by name
     * @return The list of all saves
     */
    public static List<String> getSaves(){
        if(FileUtils.checkFileExists("./saves")){
            return FileUtils.listDirectory("./saves");
        } else {
            FileUtils.createDirectory("./saves");
            return FileUtils.listDirectory("./saves");
        }
    }
    
    /**
     * Checks if the save has a world file
     * @param saveName The name of the save
     * @return true if the world file exists, false otherwise
     */
    public static boolean saveHasWorldFile(String saveName){
        String dirPath = SaveUtils.deriveSaveDirectoryPath(saveName) + "/world.json";
        LoggerInterface.loggerEngine.DEBUG("Exists? " + dirPath);
        return FileUtils.checkFileExists(dirPath);
    }

    /**
     * Checks if the save is a procedural world
     * @param saveName The name of the save
     * @return true if the world is procedural, false otherwise
     */
    public static boolean isProcedural(String saveName){
        if(!FileUtils.checkFileExists(SaveUtils.deriveSaveDirectoryPath(saveName) + "scene.json")){
            return false;
        }
        SceneFile sceneFile = FileUtils.loadObjectFromSavePath(saveName, "scene.json", SceneFile.class);
        if(sceneFile == null){
            return false;
        }
        return sceneFile.getRealmDescriptor().getType().equals(RealmDescriptor.REALM_DESCRIPTOR_PROCEDURAL);
    }

    /**
     * Checks if the level name is a test scene
     * @param name The name of the level
     * @return true if it is a test scene, false otherwise
     */
    public static boolean isTestScene(String name){
        return name.equals("testscene1");
    }

}
