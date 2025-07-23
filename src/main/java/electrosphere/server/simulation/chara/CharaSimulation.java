package electrosphere.server.simulation.chara;

import org.joml.Vector3d;

import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.macro.struct.StructureData;
import electrosphere.engine.Globals;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.CharacterUtils;
import electrosphere.server.macro.character.data.CharacterDataStrings;
import electrosphere.server.macro.character.goal.CharacterGoal;
import electrosphere.server.macro.character.goal.CharacterGoal.CharacterGoalType;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.macro.utils.StructurePlacementUtils;
import electrosphere.server.macro.utils.StructureRepairUtils;
import electrosphere.util.FileUtils;

/**
 * Methods for simulating characters
 */
public class CharaSimulation {

    /**
     * Maximum attempts to place a structure
     */
    static final int MAX_PLACE_ATTEMPTS = 10;

    /**
     * Sets the goal of the character
     * @param realm The realm
     * @param chara The character
     */
    public static void setGoal(Realm realm, Character chara){
        if(CharaSimulation.checkForShelter(realm, chara)){
            return;
        }
        if(CharaSimulation.checkTownGoals(realm, chara)){
            return;
        }
        //send a character on a walk
        if(chara.getId() == 3){
            Town hometown = CharacterUtils.getHometown(realm.getMacroData(), chara);
            if(hometown != null){
                MacroRegion target = hometown.getFarmPlots(realm.getMacroData()).get(0);
                CharacterGoal.setCharacterGoal(chara, new CharacterGoal(CharacterGoalType.MOVE_TO_MACRO_STRUCT, target));
            }
        }
    }
    
    /**
     * Checks if the character has shelter
     * @param realm The realm
     * @param chara The character
     */
    protected static boolean checkForShelter(Realm realm, Character chara){
        MacroData macroData = realm.getMacroData();
        /*
        If doesn’t have shelter, check if in town
        If in town,
            check if there’s an inn/church/friendly family
                if so, try to stay there
            if can’t find place to stay, fashion makeshift shelter
        If no town
            fashion makeshift shelter
        */
        if(CharacterUtils.getShelter(macroData,chara) != null){
            VirtualStructure shelter = CharacterUtils.getShelter(macroData,chara);
            if(shelter.isRepairable()){
                if(StructureRepairUtils.validateRepairable(realm, shelter)){
                    String repairMat = StructureRepairUtils.getNextRepairMat(realm, shelter);
                    if(CharaInventoryUtils.containsItem(chara, repairMat)){
                        CharacterGoal.setCharacterGoal(chara, new CharacterGoal(CharacterGoalType.BUILD_STRUCTURE, shelter));
                        return true;
                    } else {
                        CharacterGoal.setCharacterGoal(chara, new CharacterGoal(CharacterGoalType.ACQUIRE_ITEM, repairMat));
                        return true;
                    }
                } else {
                    shelter.setRepairable(false);
                }
            }
        } else {
            Vector3d position = chara.getPos();
            StructureData structureData = Globals.gameConfigCurrent.getStructureData().getTypes().iterator().next();

            //solve where to place
            Vector3d placementPos = StructurePlacementUtils.getPlacementPosition(macroData, structureData, position);

            //add to macro data
            VirtualStructure struct = VirtualStructure.createStructure(macroData, structureData, placementPos, VirtualStructure.ROT_FACE_NORTH);
            struct.setRepairable(true);
            struct.setFab(BlockFab.read(FileUtils.getAssetFile(struct.getFabPath())));
            CharacterUtils.addShelter(chara, struct);

            //target the struct
            CharacterGoal.setCharacterGoal(chara, new CharacterGoal(CharacterGoalType.BUILD_STRUCTURE, struct));
            return true;
        }
        return false;
    }
    
    protected static void checkTownMembership(Character chara){
        //TODO: eventually exclude people who shouldn't belong to a town (traders, bandits, etc)
//        for(Character chara : Globals.macroData.getAliveCharacters()){
        boolean hasHometown = chara.containsKey(CharacterDataStrings.HOMETOWN);
        boolean hasShelter = chara.containsKey(CharacterDataStrings.SHELTER);
        //if has structure & no hometown
        if(!hasHometown && hasShelter){
            // Structure shelter = CharacterUtils.getShelter(chara);
            //if there's at least one other structure nearby
            // Vector2i shelterDiscretePos = new Vector2i(shelter.getWorldX(),shelter.getWorldY());
            // List<Structure> nearbyPopulatedStructures = new LinkedList<Structure>();
            // for(Structure currentStruct : Globals.macroData.getStructures()){
            //     if(currentStruct.getWorldX() == shelterDiscretePos.x && currentStruct.getWorldY() == shelterDiscretePos.y && currentStruct != shelter){
            //         //if has a resident
            //         if(shelter.getDataKeys().contains(StructureDataStrings.RESIDENTS) && VirtualStructureUtils.getResidents(shelter).size() > 0){
            //             boolean noTown = true;
            //             for(Town town : Globals.macroData.getTowns()){
            //                 if(town.getStructures().contains(currentStruct)){
            //                     noTown = false;
            //                 }
            //             }
            //             if(noTown){
            //                 nearbyPopulatedStructures.add(currentStruct);
            //             }
            //         }
            //     }
            // }
            // if(nearbyPopulatedStructures.size() > 0){
            //     int numStructures = 0;
            //     int numResidents = 0;
            //     //form town
            //     Town newTown = Town.createTown(shelterDiscretePos.x, shelterDiscretePos.y);
            //     for(Structure structure : nearbyPopulatedStructures){
            //         numStructures++;
            //         newTown.addStructure(structure);
            //         for(Character resident : VirtualStructureUtils.getResidents(structure)){
            //             numResidents++;
            //             newTown.addResident(resident);
            //             CharacterUtils.addHometown(resident, newTown);
            //         }
            //     }
            //     newTown.addStructure(shelter);
            //     newTown.addResident(chara);
            //     CharacterUtils.addHometown(chara, newTown);
            //     System.out.println("Formed town with " + numStructures + " structures and " + numResidents + " residents");
            // }
        }
//        }
    }

    /**
     * Checks if the town has a job that the character can reserve
     * @param realm The realm
     * @param chara The character
     */
    protected static boolean checkTownGoals(Realm realm, Character chara){
        MacroData macroData = realm.getMacroData();
        if(CharacterUtils.getHometown(macroData, chara) == null){
            return false;
        }
        Town hometown = CharacterUtils.getHometown(macroData, chara);
        if(hometown.getJobs().size() > 0){
            return true;
        }
        return false;
    }


    /**
     * Performs whatever the current goal of the character is
     * @param realm The realm
     * @param chara The character
     */
    public static void performGoal(Realm realm, Character chara){
        //todo -- acquire item logic
    }
    
}
