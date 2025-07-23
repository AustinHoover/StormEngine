package electrosphere.server.macro.character.race;

import java.util.List;

/**
 * Map of name of race to data about said race
 */
public class RaceMap {
    
    /**
     * The list of races
     */
    List<Race> raceMap;
    
    /**
     * Gets the list of races
     * @return The list of races
     */
    public List<Race> getRaces() {
        return raceMap;
    }

    /**
     * Gets race data from its name
     * @param raceName The name of the race
     * @return The data for the race
     */
    public Race getRace(String raceName){
        for(Race race : raceMap){
            if(race.getId().equals(raceName)){
                return race;
            }
        }
        throw new Error("Failed to find race " + raceName);
    }
    
}
