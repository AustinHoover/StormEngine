package electrosphere.server.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.entity.ServerContentManager;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.civilization.town.TownLayout;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.path.MacroPathCache;
import electrosphere.test.annotations.IntegrationTest;

/**
 * Testing the macro pathing service
 */
public class MacroPathingServiceTests {
    
    @IntegrationTest
    public void test_findPath_1(){
        Globals.initGlobals();
        ServerWorldData worldData = ServerWorldData.createGenerationTestWorldData();
        MacroData macroData = MacroData.generateWorld(0, worldData);
        MacroPathCache pathCache = macroData.getPathCache();
        Globals.serverState.realmManager.createGriddedRealm(worldData, ServerContentManager.createServerContentManager(false, macroData));
        Town town = Town.createTown(macroData, new Vector3d(1000,0,1000), 256, 0);
        TownLayout.layoutTown(Globals.serverState.realmManager.first(), macroData, town);
        List<MacroRegion> farmPlots = town.getFarmPlots(macroData);
        
        List<Vector3d> path = Globals.serverState.macroPathingService.findPath(macroData, pathCache.getPathingNode(farmPlots.get(0).getPos()), pathCache.getPathingNode(farmPlots.get(1).getPos()));
        assertTrue(path.size() > 0);

        Globals.unloadScene();
        Globals.resetGlobals();
    }

}
