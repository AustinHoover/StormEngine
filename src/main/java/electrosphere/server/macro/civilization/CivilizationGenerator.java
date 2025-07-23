package electrosphere.server.macro.civilization;

import org.joml.Vector3d;

import electrosphere.data.Config;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Generates civilizations
 */
public class CivilizationGenerator {

    /**
     * Initial radius of the town
     */
    static final double INITIAL_TOWN_RADIUS = 100;
    
    /**
     * Generates the civilizations for the macro data
     * @param serverWorldData The server world data
     * @param macroData The macro data
     * @param config The config
     */
    public static void generate(ServerWorldData serverWorldData, MacroData macroData, Config config){
        //TODO: spread out and don't just put at global spawn point
        Vector3d spawnPoint = new Vector3d(serverWorldData.getWorldSizeDiscrete() * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET / 2);
        spawnPoint.y = serverWorldData.getServerTerrainManager().getElevation(spawnPoint);
        for(Race race : config.getRaceMap().getRaces()){
            Civilization newCiv = Civilization.createCivilization(macroData, race);
            newCiv.addRace(race);
            Town.createTown(macroData, spawnPoint, INITIAL_TOWN_RADIUS, newCiv.getId());
        }
    }

}
