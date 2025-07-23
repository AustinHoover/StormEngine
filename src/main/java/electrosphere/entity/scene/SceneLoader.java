package electrosphere.entity.scene;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import electrosphere.server.entity.ServerContentManager;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.MacroDataLoader;
import electrosphere.server.physics.terrain.generation.DefaultChunkGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.FileUtils;

/**
 * Used to load scene files into the engine
 */
public class SceneLoader {

    /**
     * Loads a scene file on the server
     * @param saveName The name of the save
     * @param serverWorldData the server world data
     * @param isLevelEditor true if this is a level editor, false otherwise
     */
    public static void serverInstantiateSaveSceneFile(String saveName, ServerWorldData serverWorldData, boolean isLevelEditor){
        //load scene file
        SceneFile file = FileUtils.loadObjectFromSavePath(saveName, "/scene.json", SceneFile.class);

        //
        //Load macro data
        //
        MacroData macroData = null;
        if(
            file.realmDescriptor.getType().matches(RealmDescriptor.REALM_DESCRIPTOR_PROCEDURAL) ||
            file.realmDescriptor.getType().matches(RealmDescriptor.REALM_DESCRIPTOR_GENERATION_TESTING)
        ){
            macroData = MacroDataLoader.loadFromSave(saveName);
        }

        //instantiate scene data
        SceneLoader.serverInstantiateSceneFile(file,macroData,serverWorldData,isLevelEditor);
    }

    /**
     * Loads a scene file on the server
     * @param sceneName The name of the scene
     * @param serverWorldData the server world data
     * @param isLevelEditor true if this is a level editor, false otherwise
     */
    public static void serverInstantiateAssetSceneFile(String sceneName, ServerWorldData serverWorldData, boolean isLevelEditor){
        //load scene file
        String sanitizedPath = FileUtils.sanitizeFilePath("/Scenes/" + sceneName + "/scene.json");
        SceneFile file = FileUtils.loadObjectFromAssetPath(sanitizedPath, SceneFile.class);
        //instantiate scene data
        SceneLoader.serverInstantiateSceneFile(file,null,serverWorldData,isLevelEditor);
    }

    /**
     * Loads a scene file on the server
     * @param path The path in the assets directory to a scene file
     * @param isSave if true, will try to load scene from save file instead of asset file
     */
    private static void serverInstantiateSceneFile(SceneFile file, MacroData macroData, ServerWorldData serverWorldData, boolean isLevelEditor){

        //
        //Content manager
        //
        ServerContentManager serverContentManager = null;
        if(
            file.realmDescriptor.getType().matches(RealmDescriptor.REALM_DESCRIPTOR_PROCEDURAL) ||
            file.realmDescriptor.getType().matches(RealmDescriptor.REALM_DESCRIPTOR_GENERATION_TESTING)
        ){
            serverContentManager = ServerContentManager.createServerContentManager(true,macroData);
        } else {
            serverContentManager = ServerContentManager.createServerContentManager(false,macroData);
        }

        //
        //Init the realm
        //
        Realm realm = null;
        switch(file.realmDescriptor.getType()){
            case RealmDescriptor.REALM_DESCRIPTOR_GRIDDED: {
                realm = Globals.serverState.realmManager.createGriddedRealm(serverWorldData,serverContentManager);
                if(file.loadAllCells()){
                    ((GriddedDataCellManager)realm.getDataCellManager()).loadAllCells();
                }
                if(file.getRealmDescriptor() != null && file.getRealmDescriptor().getType().equals(RealmDescriptor.REALM_DESCRIPTOR_GRIDDED)){
                    serverWorldData.getServerTerrainManager().overrideChunkGenerator(new DefaultChunkGenerator());
                    if(serverWorldData.getServerTerrainManager().getChunkGenerator() instanceof DefaultChunkGenerator){
                        DefaultChunkGenerator chunkGenerator = (DefaultChunkGenerator)serverWorldData.getServerTerrainManager().getChunkGenerator();
                        chunkGenerator.setBaseVoxelId(file.getRealmDescriptor().getBaseVoxel());
                    } else {
                        throw new Error("Failed to load " + serverWorldData.getServerTerrainManager().getChunkGenerator());
                    }
                }
            } break;
            case RealmDescriptor.REALM_DESCRIPTOR_PROCEDURAL: {
                realm = Globals.serverState.realmManager.createGriddedRealm(serverWorldData,serverContentManager);
                //generate spawns
                Vector3d spawnPoint = new Vector3d(serverWorldData.getWorldSizeDiscrete() * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET / 2);
                spawnPoint.y = serverWorldData.getServerTerrainManager().getElevation(
                    serverWorldData.getWorldSizeDiscrete() / 2,
                    serverWorldData.getWorldSizeDiscrete() / 2,
                    ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET / 2,
                    ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET / 2
                );
                realm.registerSpawnPoint(spawnPoint);
            } break;
            case RealmDescriptor.REALM_DESCRIPTOR_GENERATION_TESTING: {
                ServerWorldData newWorldData = ServerWorldData.createGenerationTestWorldData();
                realm = Globals.serverState.realmManager.createGriddedRealm(newWorldData, serverContentManager);
            } break;
            default: {
                throw new Error("Unhandled case! " + file.realmDescriptor.getType());
            }
        }
        //spawn initial entities
        for(EntityDescriptor descriptor : file.getEntities()){
            //spawn entity somehow
            switch(descriptor.getType()){

                case EntityDescriptor.TYPE_CREATURE: {
                    Vector3d position = new Vector3d(descriptor.posX,descriptor.posY,descriptor.posZ);
                    Entity newEntity = CreatureUtils.serverSpawnBasicCreature(realm, position, descriptor.subtype, null);
                    EntityUtils.getRotation(newEntity).set((float)descriptor.rotX, (float)descriptor.rotY, (float)descriptor.rotZ, (float)descriptor.rotW);
                } break;

                case EntityDescriptor.TYPE_ITEM: {
                    Vector3d position = new Vector3d(descriptor.posX,descriptor.posY,descriptor.posZ);
                    Entity newEntity = ItemUtils.serverSpawnBasicItem(realm, position, descriptor.subtype);
                    EntityUtils.getRotation(newEntity).set((float)descriptor.rotX, (float)descriptor.rotY, (float)descriptor.rotZ, (float)descriptor.rotW);
                } break;

                case EntityDescriptor.TYPE_OBJECT: {
                    Vector3d position = new Vector3d(descriptor.posX,descriptor.posY,descriptor.posZ);
                    Entity newEntity = CommonEntityUtils.serverSpawnBasicObject(realm, position, descriptor.subtype);
                    EntityUtils.getPosition(newEntity).set(descriptor.posX,descriptor.posY,descriptor.posZ);
                    EntityUtils.getRotation(newEntity).set((float)descriptor.rotX, (float)descriptor.rotY, (float)descriptor.rotZ, (float)descriptor.rotW);
                } break;
                default:
                throw new UnsupportedOperationException();
            }
        }
        //hook up macro data if relevant
        if(macroData != null){
            realm.getServerWorldData().getServerTerrainManager().setMacroData(macroData);
            realm.getServerWorldData().getServerBlockManager().setMacroData(macroData);
        }
        //load scripts
        if(!isLevelEditor && file.getInitScriptPath() != null){
            Realm finalRealm = realm;
            Globals.engineState.scriptEngine.getScriptContext().executeSynchronously(() -> {
                int sceneInstanceId = Globals.engineState.scriptEngine.getScriptContext().initScene(file.getInitScriptPath());
                finalRealm.setSceneInstanceId(sceneInstanceId);
            });
        }
        //TODO: integrate scripts for client side of scenes
        // for(String scriptPath : file.getScriptPaths()){
        //     Globals.scriptEngine.loadScript(scriptPath);
        // }
        // Globals.scriptEngine.runScript(file.getInitScriptPath());

        //TODO: instruct client to load the scene
    }

}
