package electrosphere.server.macro.civilization.town;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.entity.ServerContentManager;
import electrosphere.server.macro.MacroData;
import electrosphere.test.annotations.UnitTest;

/**
 * Test creating towns
 */
public class TownLayoutTests {
    
    @UnitTest
    public void test_generateWorld_1(){
        Globals.initGlobals();
        ServerWorldData worldData = ServerWorldData.createGenerationTestWorldData();
        MacroData macroData = MacroData.generateWorld(0, worldData);
        Globals.serverState.realmManager.createGriddedRealm(worldData, ServerContentManager.createServerContentManager(false, macroData));
        Town town = Town.createTown(macroData, new Vector3d(1000,0,1000), 256, 0);
        TownLayout.layoutTown(Globals.serverState.realmManager.first(), macroData, town);
        
        assertEquals(true, macroData.getStructures().size() > 50);
        assertEquals(true, town.getStructures(macroData).size() > 50);

        Globals.unloadScene();
        Globals.resetGlobals();
    }

}
