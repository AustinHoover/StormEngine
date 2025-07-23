package electrosphere.server.macro.character.race;

import java.util.List;

import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.data.CharacterData;
import electrosphere.server.macro.character.data.CharacterDataStrings;

/**
 * The race of a creature
 */
public class Race extends CharacterData {
    
    /**
     * The name of the race
     */
    String raceId;

    /**
     * Display name of the race
     */
    String displayName;

    /**
     * The associated creature for the race
     */
    String associatedCreatureId;

    /**
     * The list of structures that this race uses
     */
    List<String> structureIds;

    /**
     * Constructor
     */
    private Race(){
        super(CharacterDataStrings.RACE);
    }

    /**
     * Gets the name of the race
     * @return The name of the race
     */
    public String getId() {
        return raceId;
    }

    /**
     * Gets the associated creature data for the race
     * @return The associated creature data
     */
    public String getAssociatedCreature() {
        return associatedCreatureId;
    }
    
    /**
     * The display name for the race
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * The list of structure IDs that this race uses
     * @return The list of structure IDs
     */
    public List<String> getStructureIds() {
        return structureIds;
    }

    /**
     * Creates a race
     * @param name The name of the race
     * @param creatureName The name of the creature associated with the race
     * @return The race
     */
    public static Race create(String name, String creatureName){
        Race race = new Race();
        race.raceId = name;
        race.associatedCreatureId = creatureName;
        return race;
    }

    /**
     * Sets race data for the character
     * @param character The character
     * @param race The race data for the character
     */
    public static void setRace(Character character, Race race){
        character.putData(CharacterDataStrings.RACE, race);
    }
    
    /**
     * Gets race data for the character
     * @param character The character
     * @return The race data
     */
    public static Race getRace(Character character){
        return (Race)character.getData(CharacterDataStrings.RACE);
    }

    /**
     * Checks if a character has race data
     * @param character The character
     * @return true if it has race data, false otherwise
     */
    public static boolean hasRace(Character character){
        return character.containsKey(CharacterDataStrings.RACE);
    }
    
}
