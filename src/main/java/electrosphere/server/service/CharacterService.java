package electrosphere.server.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.google.gson.Gson;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.db.DatabaseResult;
import electrosphere.server.db.DatabaseResultRow;
import electrosphere.server.macro.character.Character;
import electrosphere.util.SerializationUtils;

/**
 * Service for interacting with macro-level characters
 */
public class CharacterService extends SignalServiceImpl {

    /**
     * Playerid id for a playerless character
     */
    public static final int NO_PLAYER = -1;

    /**
     * Map that stores the characters currently loaded into memory
     */
    Map<Integer,Character> loadedCharacterMap = new HashMap<Integer,Character>();

    /**
     * Map of character -> entity
     */
    Map<Character,Entity> characterEntityMap = new HashMap<Character,Entity>();

    /**
     * Lock for thread-safe-ing the service
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor
     */
    public CharacterService(){
        super("CharacterService", new SignalType[]{});
    }

    /**
     * Creates a character in the database
     * @param template The creature template for the character
     * @param playerId The player's id
     * @param pos The position to place the character at
     */
    public Character createCharacter(ObjectTemplate template, int playerId, Vector3d pos){
        if(template == null){
            throw new Error("Template is null!");
        }
        lock.lock();
        Character toStore = new Character(template);
        toStore.setPos(pos);
        toStore.setPlayerId(playerId);
        DatabaseResult result = Globals.serverState.dbController.executePreparedQuery(
            "INSERT INTO charaData (playerId,dataVal) VALUES (?,?) RETURNING id;",
            playerId,
            new Gson().toJson(toStore)
        );
        if(!result.hasResult()){
            throw new Error("Failed to insert character!");
        }
        for(DatabaseResultRow row : result){
            toStore.setId(row.getAsInteger("id"));
        }
        loadedCharacterMap.put(toStore.getId(),toStore);
        lock.unlock();
        return toStore;
    }

    /**
     * Gets the character
     * @param characterId The character's id
     * @return The character if it exists, null otherwise
     */
    public Character getCharacter(int characterId){
        lock.lock();
        if(loadedCharacterMap.containsKey(characterId)){
            Character rVal = loadedCharacterMap.get(characterId);
            lock.unlock();
            return rVal;
        }
        Character charData = null;
        DatabaseResult result = Globals.serverState.dbController.executePreparedQuery("SELECT id, playerId, dataVal FROM charaData WHERE id=?;", characterId);
        if(!result.hasResult()){
            LoggerInterface.loggerDB.WARNING("Failed to locate creature template for characterId=" + characterId);
            lock.unlock();
            return null;
        }
        for(DatabaseResultRow row : result){
            charData = SerializationUtils.deserialize(row.getAsString("dataVal"),Character.class);
            charData.setId(row.getAsInteger("id"));
            charData.setPlayerId(row.getAsInteger("playerId"));
        }
        loadedCharacterMap.put(charData.getId(),charData);
        lock.unlock();
        return charData;
    }

    /**
     * Gets the characters that a player has
     * @param playerId The player's id
     * @return The list of characters that player has
     */
    public List<Character> getCharacters(int playerId){
        lock.lock();
        DatabaseResult result = Globals.serverState.dbController.executePreparedQuery("SELECT id, playerId, dataVal FROM charaData WHERE playerId=?;",playerId);
        List<Character> rVal = new LinkedList<Character>();
        if(result.hasResult()){
            //if we get a valid response from the database, check that it actually matches hashes
            for(DatabaseResultRow row : result){
                int id = row.getAsInteger("id");
                if(loadedCharacterMap.containsKey(id)){
                    rVal.add(loadedCharacterMap.get(id));
                } else {
                    Character description = SerializationUtils.deserialize(row.getAsString("dataVal"),Character.class);
                    description.setId(id);
                    description.setPlayerId(row.getAsInteger("playerId"));
                    loadedCharacterMap.put(description.getId(),description);
                    rVal.add(description);
                }
            }
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Saves a character from an entity
     * @param characterEntity The entity
     */
    public void saveCharacter(Entity characterEntity){
        lock.lock();
        if(!ServerCharacterData.hasServerCharacterDataTree(characterEntity)){
            throw new Error("Trying to save entity hat does not contain character data!");
        }
        ServerCharacterData characterData = ServerCharacterData.getServerCharacterData(characterEntity);
        Character charaData = characterData.getCharacterData();

        //serialize
        charaData.setCreatureTemplate(CommonEntityUtils.getObjectTemplate(characterEntity));
        charaData.setPos(EntityUtils.getPosition(characterEntity));
        String toStore = SerializationUtils.serialize(charaData);

        //store a serialization to associate with the character
        Globals.serverState.dbController.executePreparedStatement(
            "UPDATE charaData SET dataVal=? WHERE id=?;",
            toStore,
            charaData.getId()
        );
        lock.unlock();
    }

    /**
     * Gets all characters
     * @return The list of all characters
     */
    public List<Character> getAllCharacters(){
        lock.lock();
        DatabaseResult result = Globals.serverState.dbController.executePreparedQuery("SELECT id, playerId, dataVal FROM charaData");
        List<Character> rVal = new LinkedList<Character>();
        if(result.hasResult()){
            //if we get a valid response from the database, check that it actually matches hashes
            for(DatabaseResultRow row : result){
                int id = row.getAsInteger("id");
                if(loadedCharacterMap.containsKey(id)){
                    rVal.add(loadedCharacterMap.get(id));
                } else {
                    Character description = SerializationUtils.deserialize(row.getAsString("dataVal"),Character.class);
                    description.setId(id);
                    description.setPlayerId(row.getAsInteger("playerId"));
                    loadedCharacterMap.put(description.getId(),description);
                    rVal.add(description);
                }
            }
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the list of loaded characters
     * @return The list of loaded characters
     */
    public List<Character> getLoadedCharacters(){
        lock.lock();
        List<Character> rVal = new LinkedList<Character>(this.loadedCharacterMap.values());
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the characters at a given world position
     * @param worldPos The world position
     * @return The list of characters occupying that world position
     */
    public List<Character> getCharacters(Vector3i worldPos){
        List<Character> rVal = new LinkedList<Character>();
        List<Character> allCharacters = this.getLoadedCharacters();
        for(Character character : allCharacters){
            if(ServerWorldData.convertRealToChunkSpace(character.getPos()).equals(worldPos.x, worldPos.y, worldPos.z)){
                rVal.add(character);
            }
        }
        return rVal;
    }

    /**
     * Associates an entity to a character
     * @param character The character
     * @param entity The entity
     */
    public void setEntity(Character character, Entity entity){
        lock.lock();
        if(this.characterEntityMap.containsKey(character)){
            lock.unlock();
            throw new Error("Entity already set!");
        }
        this.characterEntityMap.put(character, entity);
        lock.unlock();
    }

    /**
     * Gets the entity associated with a character
     * @param character The character
     * @return The associated entity if it exists, null otherwise
     */
    public Entity getEntity(Character character){
        lock.lock();
        Entity rVal = this.characterEntityMap.get(character);
        lock.unlock();
        return rVal;
    }

    /**
     * Removes an entity association with a character
     * @param character The character
     */
    public void removeEntity(Character character){
        lock.lock();
        this.characterEntityMap.remove(character);
        lock.unlock();
    }

    /**
     * Saves the character service
     */
    public void saveAll(){
        lock.lock();
        for(Character chara : this.loadedCharacterMap.values()){
            ObjectTemplate template = chara.getCreatureTemplate();
            if(this.characterEntityMap.containsKey(chara)){
                Entity characterEntity = this.characterEntityMap.get(chara);
                template = CommonEntityUtils.getObjectTemplate(characterEntity);
                chara.setCreatureTemplate(template);
                chara.setPos(EntityUtils.getPosition(characterEntity));
            }

            //serialize
            String toStore = SerializationUtils.serialize(chara);

            //store a serialization to associate with the character
            Globals.serverState.dbController.executePreparedStatement(
                "UPDATE charaData SET dataVal=? WHERE id=?;",
                toStore,
                chara.getId()
            );
        }
        lock.unlock();
    }

    @Override
    public void unloadScene(){
        super.unloadScene();
        this.loadedCharacterMap.clear();
        this.characterEntityMap.clear();
    }

}
