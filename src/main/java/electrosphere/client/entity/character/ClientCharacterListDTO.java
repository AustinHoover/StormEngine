package electrosphere.client.entity.character;

import java.util.List;

/**
 * DTO for sending available characters to the client
 */
public class ClientCharacterListDTO {
    
    /**
     * The list of characters stored in the DTO
     */
    List<CharacterDescriptionDTO> characters;

    /**
     * Gets the list of characters
     * @return The list of characters
     */
    public List<CharacterDescriptionDTO> getCharacters() {
        return characters;
    }

    /**
     * Sets the list of characters
     * @param characters The list of characters
     */
    public void setCharacters(List<CharacterDescriptionDTO> characters) {
        this.characters = characters;
    }

}
