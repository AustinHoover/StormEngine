package electrosphere.client.entity.character;

/**
 * Tracks the characers available to the client
 */
public class ClientCharacterManager {
    
    /**
     * The list of characters available
     */
    private ClientCharacterListDTO characterList;

    /**
     * Tracks whether we're waiting on the character list or not
     */
    private boolean waitingOnList = true;

    /**
     * Gets the character list
     * @return The character list
     */
    public ClientCharacterListDTO getCharacterList() {
        return characterList;
    }

    /**
     * Sets the character list
     * @param characterList The character list
     */
    public void setCharacterList(ClientCharacterListDTO characterList) {
        this.characterList = characterList;
    }

    /**
     * Checks whether we're waiting on the character list or not
     * @return true if we're waiting, false otherwise
     */
    public boolean isWaitingOnList() {
        return waitingOnList;
    }

    /**
     * Sets whether we're waiting on the character list or not
     * @param waitingOnList true if we're waiting, false otherwise
     */
    public void setWaitingOnList(boolean waitingOnList) {
        this.waitingOnList = waitingOnList;
    }


    
    

}
