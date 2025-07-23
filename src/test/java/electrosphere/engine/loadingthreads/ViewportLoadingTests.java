package electrosphere.engine.loadingthreads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.test.testutils.EngineInit;

/**
 * Tests loading viewport
 */
public class ViewportLoadingTests {

    @AfterEach
    public void clearTest(){
        Main.shutdown();
    }
    
    @IntegrationTest
    public void testViewportLoading_ThrowsError_False(){
        assertDoesNotThrow(() -> {
            //init engine
            EngineInit.initGraphicalEngine();
            
            //load scene
            EngineInit.setupConnectedTestViewport();

            //shutdown engine
            Main.shutdown();
        });
    }

    @IntegrationTest
    public void testViewportLoading_MainMenuVisible_False(){
        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();

        //should see viewport here
        for(Element window : Globals.elementService.getWindowList()){
            assertEquals(false,WindowUtils.windowIsVisible(window));
        }
    }

    @IntegrationTest
    public void testViewportLoadingTwice_MainMenuVisible_False(){
        //init engine once

        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();

        //should see viewport here

        //shutdown engine
        Main.shutdown();

        //init engine second time

        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();

        //should still see viewport here
        assertEquals(false,WindowUtils.windowIsVisible(WindowStrings.WINDOW_MENU_MAIN));
    }

    @IntegrationTest
    public void testViewportLoadingTwice_LoadingMenuVisible_False(){
        //init engine once

        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();

        //should see viewport here

        //shutdown engine
        Main.shutdown();

        //init engine second time

        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();

        //should still see viewport here
        assertEquals(false,WindowUtils.windowIsVisible(WindowStrings.WINDOW_LOADING));
    }

    @IntegrationTest
    public void testViewportLoadingTwice_ScreenFramebufferFlags_True(){
        //init engine once

        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();

        //should see viewport here

        //shutdown engine
        Main.shutdown();

        //init engine second time

        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();

        //should still see viewport here
        assertEquals(true,Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER);
        assertEquals(true,Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT);
    }

}
