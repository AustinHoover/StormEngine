package electrosphere.server.macro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.test.annotations.UnitTest;

/**
 * Tests for macro data
 */
public class MacroDataTests {
    
    @UnitTest
    public void test_generateWorld_1(){
        Globals.initGlobals();
        ServerWorldData worldData = ServerWorldData.createGenerationTestWorldData();
        MacroData macroData = MacroData.generateWorld(0, worldData);
        
        assertEquals(true, macroData.getCivilizations().size() > 0);

        Globals.resetGlobals();
    }

}
