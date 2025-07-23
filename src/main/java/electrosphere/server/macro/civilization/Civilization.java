package electrosphere.server.macro.civilization;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import electrosphere.engine.Globals;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.civilization.town.Town;

/**
 * A civilization
 */
public class Civilization {
    
    /**
     * The id of the civilization
     */
    private int id;

    /**
     * The name of the civilization
     */
    private String name;

    /**
     * The towns that are a a part of this civilization
     */
    private List<Integer> towns = new LinkedList<Integer>();

    /**
     * The citizens of the civilization
     */
    private List<Integer> citizens = new LinkedList<Integer>();

    /**
     * The races that are a part of this civilization
     */
    private List<String> races = new LinkedList<String>();

    /**
     * Private constructor
     */
    private Civilization(){ }

    /**
     * Creates a civilization
     * @param macroData The macro data
     * @return The civilization
     */
    public static Civilization createCivilization(MacroData macroData, Race race){
        Civilization rVal = new Civilization();
        rVal.races.add(race.getId());
        macroData.addCivilization(rVal);
        return rVal;
    }

    /**
     * Gets the id of the civilization
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the civilization
     * @param id The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the civilization
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the civilization
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of towns that are a part of this civilization
     * @return The list of towns
     */
    public List<Town> getTowns(MacroData macroData){
        return towns.stream().map((Integer id) -> macroData.getTown(id)).filter((Town town) -> town != null).collect(Collectors.toList());
    }

    /**
     * Adds a town to the civilization
     * @param town The town
     */
    public void addTown(Town town){
        this.towns.add(town.getId());
    }

    /**
     * Gets the list of citizens of this civilization
     * @return The list of citizens
     */
    public List<Character> getCitizens(MacroData macroData){
        return citizens.stream().map((Integer id) -> Globals.serverState.characterService.getCharacter(id)).filter((Character chara) -> chara != null).collect(Collectors.toList());
    }

    /**
     * Adds a citizens of the civilization
     * @param chara The character
     */
    public void addCitizen(Character chara){
        this.citizens.add(chara.getId());
    }

    /**
     * Adds a race to the civilization
     * @param race The race
     */
    public void addRace(Race race){
        this.races.add(race.getId());
    }

    /**
     * Gets the list of race ids of this civilization
     * @return The list of race ids
     */
    public List<String> getRaceIds(){
        return this.races;
    }

}
