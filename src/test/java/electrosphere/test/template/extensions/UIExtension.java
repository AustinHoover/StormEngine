package electrosphere.test.template.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.renderer.RenderingEngine;
import electrosphere.test.testutils.EngineInit;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Spins up an tears down generic ui environment
 */
public class UIExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        RenderingEngine.WINDOW_DECORATED = false;
        RenderingEngine.WINDOW_FULLSCREEN = true;
        EngineState.EngineFlags.RUN_AUDIO = false;
        EngineState.EngineFlags.RUN_SCRIPTS = false;
        Globals.WINDOW_WIDTH = 1920;
        Globals.WINDOW_HEIGHT = 1080;
        EngineInit.initGraphicalEngine();
        TestEngineUtils.flush();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Main.shutdown();
    }
    
}
