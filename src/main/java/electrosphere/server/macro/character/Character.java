package electrosphere.server.macro.character;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3d;

import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.server.macro.character.data.CharacterData;
import electrosphere.server.macro.spatial.MacroObject;

/**
 * A character
 */
public class Character implements MacroObject {

    /**
     * The id of the character object
     */
    int id;

    /**
     * The associated player's id
     */
    int playerId;
    
    /**
     * Data stored on the character
     */
    Map<String,CharacterData> data = new HashMap<String,CharacterData>();

    /**
     * The creature template
     */
    ObjectTemplate creatureTemplate;

    /**
     * The position of the character object
     */
    Vector3d pos = new Vector3d();

    
    /**
     * Gets the id of the character
     * @return The id of the character
     */
    public int getId() {
        return id;
    }
    
    /**
     * Sets the id of the character
     * @param id The id of the character
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Gets the associated player's id
     * @return The id of the associated player
     */
    public int getPlayerId(){
        return playerId;
    }
    
    /**
     * Sets the id of the associated player
     * @param id The id of the associated player
     */
    public void setPlayerId(int id){
        this.playerId = id;
    }
    
    /**
     * Puts data on the character
     * @param key The key for the data
     * @param item The data itself
     */
    public void putData(String key, CharacterData item){
        data.put(key,item);
    }

    /**
     * Checks if the character has a type of data
     * @param key The key to check
     * @return true if the character has data at that key, false otherwise
     */
    public boolean containsKey(String key){
        return data.containsKey(key);
    }
    
    /**
     * Gets the data at a given key on the character
     * @param key The key
     * @return The data if it exists, null otherwise
     */
    public CharacterData getData(String key){
        return data.get(key);
    }

    /**
     * Constructor
     * @param template
     */
    public Character(ObjectTemplate template){
        this.creatureTemplate = template;
    }
    
    /**
     * Gets the creature template for the character
     * @return The template
     */
    public ObjectTemplate getCreatureTemplate() {
        return creatureTemplate;
    }

    /**
     * Sets the creature template for the character
     * @param creatureTemplate The template
     */
    public void setCreatureTemplate(ObjectTemplate creatureTemplate) {
        this.creatureTemplate = creatureTemplate;
    }

    @Override
    public Vector3d getPos() {
        return this.pos;
    }

    @Override
    public void setPos(Vector3d pos){
        this.pos.set(pos);
    }
    
}
