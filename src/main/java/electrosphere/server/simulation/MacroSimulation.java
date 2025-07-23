package electrosphere.server.simulation;

import java.util.List;

import electrosphere.engine.Globals;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.civilization.town.TownSimulator;
import electrosphere.server.service.CharacterService;
import electrosphere.server.simulation.chara.CharaSimulation;
import electrosphere.server.simulation.temporal.TemporalSimulator;

/**
 * Performs the macro-level (ie virtual, non-physics based) simulation
 */
public class MacroSimulation {
    
    /**
     * Tracks whether the macro simulation is ready or not
     */
    private boolean isReady = false;
    
    /**
     * Iterates the macro simulation
     */
    public static void simulate(Realm realm){
        Globals.profiler.beginCpuSample("MacroSimulation.simulate");

        //
        //simulate characters
        Globals.profiler.beginCpuSample("MacroSimulation.simulate - characters");
        List<Character> characters = Globals.serverState.characterService.getLoadedCharacters();
        if(characters != null && characters.size() > 0){
            for(Character character : characters){
                if(character.getPlayerId() != CharacterService.NO_PLAYER){
                    continue;
                }
                //update the goal of the character
                CharaSimulation.setGoal(realm, character);
                //if the character doesn't have an entity, simulate it at the macro level
                if(Globals.serverState.characterService.getEntity(character) == null){
                    CharaSimulation.performGoal(realm, character);
                }
            }
        }
        Globals.profiler.endCpuSample();

        //
        //simulate towns
        Globals.profiler.beginCpuSample("MacroSimulation.simulate - towns");
        List<Town> towns = realm.getMacroData().getTowns();
        for(Town town : towns){
            TownSimulator.simualte(town);
        }
        Globals.profiler.endCpuSample();

        //
        //temporal update
        TemporalSimulator.simulate(realm);

        Globals.profiler.endCpuSample();
    }
    
    /**
     * Sets whether the macro simulation is ready or not
     * @param status true for ready, false otherwise
     */
    public void setReady(boolean status){
        isReady = status;
    }
    
    /**
     * Gets whether the macro simulation is ready or not
     * @return true for ready, false otherwise
     */
    public boolean isReady(){
        return isReady;
    }

}
