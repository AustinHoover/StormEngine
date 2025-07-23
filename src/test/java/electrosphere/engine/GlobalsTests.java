package electrosphere.engine;

import static org.junit.jupiter.api.Assertions.*;

import electrosphere.test.annotations.IntegrationTest;

/**
 * Tests for globals
 */
public class GlobalsTests {
    
    @IntegrationTest
    public void resetGlobals_Variables_null(){
        Globals.initGlobals();
        Globals.resetGlobals();
        assertNull(Globals.assetManager);
        assertNull(Globals.elementService);
    }

    @IntegrationTest
    public void resetGlobalsWithoutInit_ThrowsError_false(){
        assertDoesNotThrow(() -> {
            Globals.resetGlobals();
        });
    }

    @IntegrationTest
    public void unloadScene_Variables_notNull(){
        Globals.initGlobals();
        Globals.unloadScene();
        assertNotNull(Globals.assetManager);
        assertNotNull(Globals.serverState.realmManager);
    }

}
