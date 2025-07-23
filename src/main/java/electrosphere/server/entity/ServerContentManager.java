package electrosphere.server.entity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joml.Vector3i;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.entity.serialization.ContentSerialization;
import electrosphere.server.macro.MacroData;
import electrosphere.server.saves.SaveUtils;
import electrosphere.util.FileUtils;
import electrosphere.util.math.HashUtils;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.spatial.MacroLODObject;
import electrosphere.server.macro.spatial.MacroObject;

/**
 * Manages creating/saving/loading/destroying content (ie entities) on the server
 */
public class ServerContentManager {

    /**
     * Maximum amount of time to wait for macro data to generate
     */
    public static final int MAX_TIME_TO_WAIT = 10000;

    /**
     * controls whether the manager should generate content on loading a new scene
     */
    boolean generateContent = false;

    /**
     * The macro data for the content manager
     */
    MacroData macroData;

    /**
     * Constructor
     */
    private ServerContentManager(boolean generateContent, MacroData macroData){
        this.generateContent = generateContent;
        this.macroData = macroData;
    }

    /**
     * Creates a server content manager
     * @param generateContent if true, will generate content on loading a new scene, otherwise will not
     * @param macroData The macro data
     * @return The server content manager
     */
    public static ServerContentManager createServerContentManager(boolean generateContent, MacroData macroData){
        return new ServerContentManager(generateContent, macroData);
    }
    
    /**
     * Generates content for a given data cell
     * @param realm The realm
     * @param worldPos The world position
     * @param cell The cell
     * @param cellKey The key for this cell
     */
    public void generateContentForDataCell(Realm realm, Vector3i worldPos, ServerDataCell cell, Long cellKey){
        Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell");

        //
        //Block for macro data generation if relevant
        //
        List<MacroObject> objects = null;
        Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - collision check macro data");
        if(macroData == null){
            objects = new LinkedList<MacroObject>();
        } else {
            objects = macroData.getNearbyObjects(ServerWorldData.convertChunkToRealSpace(worldPos.x, worldPos.y, worldPos.z));
            //if any of this macro data isn't ready, return a null chunk
            long notFullResCount = objects.stream().filter((MacroObject macroObj) -> macroObj instanceof MacroLODObject).map((MacroObject oldView) -> (MacroLODObject)oldView).filter((MacroLODObject lodObj) -> !lodObj.isFullRes()).count();
            int waitCount = 0;
            while(notFullResCount > 0 && waitCount < MAX_TIME_TO_WAIT){
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                notFullResCount = objects.stream().filter((MacroObject macroObj) -> macroObj instanceof MacroLODObject).map((MacroObject oldView) -> (MacroLODObject)oldView).filter((MacroLODObject lodObj) -> !lodObj.isFullRes()).count();
                waitCount++;
            }
            if(notFullResCount > 0){
                throw new Error("Failed to generate content " + notFullResCount + " " + waitCount);
            }
        }
        Globals.profiler.endCpuSample();



        //
        //Actual generation/loading
        //
        String fullPath = "/content/" + cellKey + ".dat";
        Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - Actual generation/loading");
        if(generateContent){ //in other words, if not arena mode
            if(FileUtils.checkSavePathExists(Globals.serverState.currentSave.getName(), fullPath)){
                Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - Load from existing key");
                //if on disk (has already been generated)
                ContentSerialization contentRaw = FileUtils.loadObjectFromSavePath(Globals.serverState.currentSave.getName(), fullPath, ContentSerialization.class);
                contentRaw.hydrateRawContent(realm,cell);
                Globals.profiler.endCpuSample();
            } else {
                Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - Generate from scratch");
                //else create from scratch
                ServerContentGenerator.generateContent(realm, this.macroData, cell, worldPos, HashUtils.hashIVec(worldPos.x, worldPos.y, worldPos.z));
                Globals.profiler.endCpuSample();
            }
        } else {
            //just because content wasn't generated doesn't mean there isn't data saved under that key
            if(FileUtils.checkSavePathExists(Globals.serverState.currentSave.getName(), fullPath)){
                Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - Load from on-disk file without key");
                //if on disk (has already been generated)
                ContentSerialization contentRaw = FileUtils.loadObjectFromSavePath(Globals.serverState.currentSave.getName(), fullPath, ContentSerialization.class);
                contentRaw.hydrateRawContent(realm,cell);
                Globals.profiler.endCpuSample();
            }
        }
        Globals.profiler.endCpuSample();
        //checking for null because there are cases where we might not have macro data to instantiate from
        //ie, if we load an asset-defined (not save-defined) scene that does not have save data
        //ie, imagine a puzzle room or something like that
        Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - Macro data");
        if(macroData != null){
            Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - Get nearby characters");
            List<Character> nearbyCharacters = Globals.serverState.characterService.getCharacters(worldPos);
            Globals.profiler.endCpuSample();
            Globals.profiler.beginCpuSample("ServerContentManager.generateContentForDataCell - Spawn characters");
            for(Character character : nearbyCharacters){
                this.spawnMacroObject(realm, character);
            }
            Globals.profiler.endCpuSample();
        }
        Globals.profiler.endCpuSample();

        Globals.profiler.endCpuSample();
    }

    /**
     * Saves entity content to disk
     * @param locationKey the location key to save under
     * @param entities the collection of entities to save
     */
    public void saveContentToDisk(Long locationKey, Collection<Entity> entities){
        //serialize all non-character entities
        ContentSerialization serialization = ContentSerialization.constructContentSerialization(entities);
        String dirPath = SaveUtils.deriveSaveDirectoryPath(Globals.serverState.currentSave.getName());
        String fullPath = dirPath + "/content/" + locationKey + ".dat";
        FileUtils.serializeObjectToFilePath(fullPath, serialization);

        //store all character entities in database
        for(Entity entity : entities){
            if(ServerCharacterData.hasServerCharacterDataTree(entity)){
                Globals.serverState.characterService.saveCharacter(entity);
            }
        }
    }

    /**
     * Saves a collection of serialized entities to disk
     * @param locationKey The location key to save under
     * @param serialization The collection of entities to save
     */
    public void saveSerializationToDisk(Long locationKey, ContentSerialization serialization){
        String dirPath = SaveUtils.deriveSaveDirectoryPath(Globals.serverState.currentSave.getName());
        String fullPath = dirPath + "/content/" + locationKey + ".dat";
        FileUtils.serializeObjectToFilePath(fullPath, serialization);
    }

    /**
     * Spawns a macro object
     * @param realm The realm
     * @param object The object
     */
    public void spawnMacroObject(Realm realm, MacroObject object){
        if(object instanceof Character){
            if(Race.hasRace((Character)object)){
                Race race = Race.getRace((Character)object);
                String creatureName = race.getAssociatedCreature();
                if(creatureName == null){
                    throw new Error("Creature name not defined! " + ((Character)object).getId());
                }
                //place macro object
                Entity characterEntity = CreatureUtils.serverSpawnBasicCreature(realm, object.getPos(), creatureName, null);
                ServerCharacterData.attachServerCharacterData(characterEntity, (Character)object);
            } else {
                LoggerInterface.loggerEngine.WARNING("No race defined!");
            }
        }
    }

    /**
     * Gets the macro data
     * @return The macro data
     */
    public MacroData getMacroData(){
        return macroData;
    }


}
