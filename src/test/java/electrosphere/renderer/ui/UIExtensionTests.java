package electrosphere.renderer.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

import static electrosphere.test.testutils.Assertions.*;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.extensions.StateCleanupCheckerExtension;
import electrosphere.test.testutils.EngineInit;
import electrosphere.test.testutils.TestEngineUtils;
import electrosphere.test.testutils.TestRenderingUtils;

/**
 * Tests to verify the ui test template (we're testing our own testing framework woooooo)
 */
@ExtendWith(StateCleanupCheckerExtension.class)
public class UIExtensionTests {
    
    @IntegrationTest
    public void test_StartupShutdown_NoThrow(){
        assertDoesNotThrow(() -> {
            RenderingEngine.WINDOW_DECORATED = false;
            RenderingEngine.WINDOW_FULLSCREEN = true;
            EngineState.EngineFlags.RUN_AUDIO = false;
            EngineState.EngineFlags.RUN_SCRIPTS = false;
            Globals.WINDOW_WIDTH = 1920;
            Globals.WINDOW_HEIGHT = 1080;
            EngineInit.initGraphicalEngine();
            TestEngineUtils.flush();

            Main.shutdown();
        });
    }

    @Disabled
    @IntegrationTest
    public void test_Screencapture_Match(){
        RenderingEngine.WINDOW_DECORATED = false;
        RenderingEngine.WINDOW_FULLSCREEN = true;
        EngineState.EngineFlags.RUN_AUDIO = false;
        EngineState.EngineFlags.RUN_SCRIPTS = false;
        Globals.WINDOW_WIDTH = 1920;
        Globals.WINDOW_HEIGHT = 1080;
        EngineInit.initGraphicalEngine();
        TestEngineUtils.flush();

        TestEngineUtils.simulateFrames(3);

        String canonicalName = this.getClass().getCanonicalName();
        //check the render
        assertEqualsRender("./test/java/renderer/ui/test_Screencapture_Match.png", () -> {

            //on failure, save the failed render
            String failureSavePath = "./.testcache/" + canonicalName + "-test_Screencapture_Match.png";
            File saveFile = new File(failureSavePath);
            System.err.println("[[ATTACHMENT|" + saveFile.getAbsolutePath() + "]]");
            TestRenderingUtils.saveTestRender(failureSavePath);
        });
        Main.shutdown();
    }

    @IntegrationTest
    public void test_Screencapture_Blank_Match(){
        RenderingEngine.WINDOW_DECORATED = false;
        RenderingEngine.WINDOW_FULLSCREEN = true;
        EngineState.EngineFlags.RUN_AUDIO = false;
        EngineState.EngineFlags.RUN_SCRIPTS = false;
        Globals.WINDOW_WIDTH = 1920;
        Globals.WINDOW_HEIGHT = 1080;
        EngineInit.initGraphicalEngine();
        TestEngineUtils.flush();

        TestEngineUtils.simulateFrames(3);

        WindowUtils.replaceMainMenuContents(Div.createDiv());
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(2);

        String canonicalName = this.getClass().getCanonicalName();
        //check the render
        assertEqualsRender("./test/java/renderer/ui/test_Screencapture_Blank.png", () -> {

            //on failure, save the failed render
            String failureSavePath = "./.testcache/" + canonicalName + "-test_Screencapture_Blank.png";
            File saveFile = new File(failureSavePath);
            System.err.println("[[ATTACHMENT|" + saveFile.getAbsolutePath() + "]]");
            TestRenderingUtils.saveTestRender(failureSavePath);
        });

        Main.shutdown();
    }

}
