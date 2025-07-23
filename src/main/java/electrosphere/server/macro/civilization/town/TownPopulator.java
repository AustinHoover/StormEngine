package electrosphere.server.macro.civilization.town;

import java.util.List;
import java.util.Random;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.CharacterUtils;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.service.CharacterService;

/**
 * Creates the population of a town
 */
public class TownPopulator {
    
    /**
     * Populates a town with characters
     * @param macroData The macro data
     * @param town The town
     */
    public static void populateTown(Realm realm, MacroData macroData, Town town){
        List<VirtualStructure> structs = town.getStructures(macroData);
        Random rand = new Random(town.getId());
        for(VirtualStructure struct : structs){
            Vector3d placePos = new Vector3d(struct.getPos()).add(1,1,1);
            ObjectTemplate template = ObjectTemplate.create(EntityType.CREATURE, "human");
            Character chara = Globals.serverState.characterService.createCharacter(template, CharacterService.NO_PLAYER, placePos);
            Race.setRace(chara, Globals.gameConfigCurrent.getRaceMap().getRace("human"));
            CharacterUtils.addShelter(chara, struct);
            town.addResident(chara);
        }

        //assign jobs to created characters
        // int farmPlots = town.getFarmPlots(macroData).size(); ~250
        // int popCount = town.getResidents(macroData).size(); ~15
    }

}
