package electrosphere.server.macro;

import org.joml.Vector3d;

import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.civilization.town.TownLayout;
import electrosphere.server.macro.civilization.town.TownPopulator;

/**
 * Updates macro data as a player comes into range of it
 */
public class MacroDataUpdater {

    /**
     * Generate all towns within 5 km of a player
     */
    private static final int TOWN_GENERATION_DIST = 5 * 1000;
    
    /**
     * Updates the macro data
     * @param realm The realm related to this data
     * @param macroData The data
     * @param playerPos The player's position
     */
    public static void update(Realm realm, MacroData macroData, Vector3d playerPos){
        //scan for all towns within update range
        if(macroData != null){
            for(Town town : macroData.getTowns()){
                //only generate data for towns that aren't already full resolution
                if(town.getResolution() == Town.TOWN_RES_MAX){
                    continue;
                }
                Vector3d townPos = town.getPos();
                if(townPos.distance(playerPos) < TOWN_GENERATION_DIST){
                    TownLayout.layoutTown(realm, macroData, town);
                    TownPopulator.populateTown(realm, macroData, town);
                    town.setResolution(Town.TOWN_RES_MAX);
                }
            }
        }
    }

}
