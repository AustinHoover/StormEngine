package electrosphere.entity.state.server;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.server.macro.character.Character;

/**
 * Stores data that associated an entity to a character in the character database
 */
public class ServerCharacterData {
    
    /**
     * The character data
     */
    private Character charaData;

    /**
     * The associated entity
     */
    private Entity parent;

    /**
     * Constructor
     * @param parent
     * @param characterId
     */
    private ServerCharacterData(Entity parent, Character charaData){
        this.parent = parent;
        this.charaData = charaData;
    }

    /**
     * Attaches a ServerCharacterData to a given entity
     * @param entity The entity to add to
     * @param charaData The character data
     */
    public static void attachServerCharacterData(Entity entity, Character charaData){
        if(entity.containsKey(EntityDataStrings.TREE_SERVERCHARACTERDATA)){
            throw new Error("Server character data already attached!");
        }
        ServerCharacterData tree = new ServerCharacterData(entity, charaData);
        entity.putData(EntityDataStrings.TREE_SERVERCHARACTERDATA, tree);
        Globals.serverState.characterService.setEntity(charaData, entity);
    }

    /**
     * Checks if the entity has associated character data
     * @param entity The entity
     * @return true if the entity contains character data, false otherwise
     */
    public static boolean hasServerCharacterDataTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERCHARACTERDATA);
    }

    /**
     * Gets the character data on the entity
     * @param entity The entity
     * @return The ServerCharacterData
     */
    public static ServerCharacterData getServerCharacterData(Entity entity){
        return (ServerCharacterData)entity.getData(EntityDataStrings.TREE_SERVERCHARACTERDATA);
    }

    /**
     * Gets the associated character id for this entity
     * @return The id
     */
    public Character getCharacterData() {
        return charaData;
    }

    /**
     * Gets the parent entity of this data
     * @return The parent entity
     */
    public Entity getParent() {
        return parent;
    }

}
