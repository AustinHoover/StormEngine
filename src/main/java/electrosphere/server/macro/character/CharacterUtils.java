package electrosphere.server.macro.character;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.data.CharacterAssociatedId;
import electrosphere.server.macro.character.data.CharacterDataStrings;
import electrosphere.server.macro.character.diety.Diety;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.service.CharacterService;

/**
 * Utility functions for dealing with characters
 */
public class CharacterUtils {

    /**
     * Default human for character stuff
     */
    public static final String DEFAULT_RACE = "human";
    
    /**
     * Adds diety data for the character
     * @param character The character
     * @param diety The diety data
     */
    public static void addDiety(Character character, Diety diety){
        character.putData(CharacterDataStrings.DIETY, diety);
    }
    
    /**
     * Gets diety data for the character
     * @param character The character
     * @return The diety data
     */
    public static Diety getDiety(Character character){
        return (Diety)character.getData(CharacterDataStrings.DIETY);
    }
    
    /**
     * Sets the shelter of a character
     * @param character The character
     * @param shelter The shelter
     */
    public static void addShelter(Character character, VirtualStructure shelter){
        character.putData(CharacterDataStrings.SHELTER, new CharacterAssociatedId(CharacterDataStrings.SHELTER, shelter.getId()));
    }
    
    /**
     * Gets the shelter of a character
     * @param macroData The macro data
     * @param character The character
     * @return The shelter if it exists, null otherwise
     */
    public static VirtualStructure getShelter(MacroData macroData, Character character){
        if(!character.containsKey(CharacterDataStrings.SHELTER)){
            return null;
        }
        int structId = ((CharacterAssociatedId)character.getData(CharacterDataStrings.SHELTER)).getId();
        return macroData.getStructure(structId);
    }
    
    /**
     * Adds a hometown to a character
     * @param character The character
     * @param town The town
     */
    public static void addHometown(Character character, Town town){
        character.putData(CharacterDataStrings.HOMETOWN, new CharacterAssociatedId(CharacterDataStrings.HOMETOWN,town.getId()));
    }
    
    /**
     * Gets the hometown of a character
     * @param macroData The macro data
     * @param character The character
     * @return The hometown if it exists, null otherwise
     */
    public static Town getHometown(MacroData macroData, Character character){
        if(!character.containsKey(CharacterDataStrings.HOMETOWN)){
            return null;
        }
        int townId = ((CharacterAssociatedId)character.getData(CharacterDataStrings.HOMETOWN)).getId();
        return macroData.getTown(townId);
    }
    
    /**
     * Spawns a character
     * @param realm The realm to spawn the character within
     * @param position The position to create the character at
     * @param race The race of the character to create
     * @return The character
     */
    public static Character spawnCharacter(Realm realm, Vector3d position, String race){
        Race raceData = Globals.gameConfigCurrent.getRaceMap().getRace(race);
        String creatureType = raceData.getAssociatedCreature();
        Character rVal = Globals.serverState.characterService.createCharacter(ObjectTemplate.createDefault(EntityType.CREATURE, creatureType), CharacterService.NO_PLAYER, position);
        Race.setRace(rVal, Race.create(race, creatureType));
        realm.getDataCellManager().evaluateMacroObject(rVal);
        return rVal;
    }

}
