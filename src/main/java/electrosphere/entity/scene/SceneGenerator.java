package electrosphere.entity.scene;

import java.util.Objects;

import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import electrosphere.server.physics.terrain.generation.ProceduralChunkGenerator;

/**
 * Generates scene files where appropriate (ie, if playing the procedurally generated level)
 */
public class SceneGenerator {
    
    /**
     * Creates a scene file for the procedurally generated gamemode
     * @param gridSize The size of the terrain grid of the scene
     * @return The scene file
     */
    public static SceneFile createProceduralSceneFile(String saveName, String seed){
        //base file stuff
        SceneFile file = SceneFile.createSceneFile();
        //realm descriptor stuff
        file.realmDescriptor.type = RealmDescriptor.REALM_DESCRIPTOR_PROCEDURAL;
        file.realmDescriptor.griddedRealmSize = GriddedDataCellManager.MAX_GRID_SIZE;
        file.createSaveInstance = true; //won't have a predefined scene to load, so must create one in the save
        file.loadAllCells = false; // do not load all cells on init
        file.seed = Objects.hash(seed);
        return file;
    }

    /**
     * Creates a scene file for the generation testing realm
     * @param gridSize The size of the terrain grid of the scene
     * @return The scene file
     */
    public static SceneFile createGenerationTestingSceneFile(String saveName){
        //base file stuff
        SceneFile file = SceneFile.createSceneFile();
        //realm descriptor stuff
        file.realmDescriptor.type = RealmDescriptor.REALM_DESCRIPTOR_GENERATION_TESTING;
        file.realmDescriptor.griddedRealmSize = ProceduralChunkGenerator.GENERATOR_REALM_SIZE;
        file.createSaveInstance = true; //won't have a predefined scene to load, so must create one in the save
        file.loadAllCells = false; // do not load all cells on init
        
        return file;
    }

}
