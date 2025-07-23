package electrosphere.engine.loadingthreads;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.AfterEach;

import electrosphere.engine.Main;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.testutils.EngineInit;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests (re)loading the main menu
 */
public class MainMenuLoadingTests {

    @AfterEach
    public void clearTest(){
        Main.shutdown();
    }

    @IntegrationTest
    public void testBackout_ThrowsError_False(){
        //init engine
        EngineInit.initGraphicalEngine();
        
        //load viewport
        EngineInit.setupConnectedTestViewport();

        //make sure backout doesn't crash
        assertDoesNotThrow(()->{
            LoadingThread.execSync(LoadingThreadType.RETURN_TITLE_MENU);

            //guarantees the engine can continue to execute after the thread resolves)
            TestEngineUtils.simulateFrames(1);
        });
    }
    
}
