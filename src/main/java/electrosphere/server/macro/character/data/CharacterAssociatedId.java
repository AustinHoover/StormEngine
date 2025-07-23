package electrosphere.server.macro.character.data;

/**
 * An id of some macro data that is associated with this character
 */
public class CharacterAssociatedId extends CharacterData {

    /**
     * The id
     */
    int id;

    /**
     * Constructor
     */
    public CharacterAssociatedId(String key, int id){
        super(key);
        this.id = id;
    }

    /**
     * Gets the id
     * @return The id
     */
    public int getId(){
        return id;
    }
    
}
