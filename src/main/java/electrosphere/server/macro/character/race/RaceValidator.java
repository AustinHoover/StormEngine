package electrosphere.server.macro.character.race;

import electrosphere.data.Config;

/**
 * Validates race data
 */
public class RaceValidator {
    
    /**
     * Validates a config
     * @param config The config
     */
    public static void validate(Config config){
        for(Race race : config.getRaceMap().getRaces()){
            //check associated creature
            if(config.getCreatureTypeLoader().getType(race.getAssociatedCreature()) == null){
                throw new Error("Race " + race.raceId + " creature does not exist: " + race.getAssociatedCreature());
            }

            //check associated structures
            for(String structureId : race.getStructureIds()){
                if(config.getStructureData().getType(structureId) == null){
                    throw new Error("Race " + race.raceId + " structure id does not correspond to a structure: " + structureId);
                }
            }
        }
    }

}
