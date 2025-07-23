package electrosphere.server.simulation.chara;

import electrosphere.engine.Globals;
import electrosphere.server.macro.character.Character;

/**
 * Most basic utilities for working with characters in macro simulation
 */
public class CharaMacroUtils {
    
    /**
     * Checks if a character is being handled by micro simulation
     * @param chara The character
     * @return true if it is being handled by micro simulation, false otherwise
     */
    public static boolean isMicroSim(Character chara){
        return Globals.serverState.characterService.getEntity(chara) != null;
    }

}
