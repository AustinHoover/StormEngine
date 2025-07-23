package electrosphere.server.macro.character.data;

/**
 * A type of data for a character
 */
public abstract class CharacterData {
    
    /**
     * The type of data
     */
    String dataType;

    /**
     * Constructor
     * @param dataType The type of data
     */
    public CharacterData(String dataType){
        this.dataType = dataType;
    }

    /**
     * Gets the type of the data
     * @return The type of data
     */
    public String getDataType(){
        return this.dataType;
    }

}
