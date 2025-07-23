package electrosphere.renderer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import electrosphere.engine.Globals;
import electrosphere.test.template.extensions.StateCleanupCheckerExtension;

/**
 * Tests for the core rendering engine
 */
@ExtendWith(StateCleanupCheckerExtension.class)
public class RenderingEngineTests {

    @Test
    public void testRenderingEngineResetsAllState(){
        assertDoesNotThrow(() -> {
            for(int i = 0; i < 5; i++){
                Globals.initGlobals();
                Globals.renderingEngine = new RenderingEngine();
                Globals.renderingEngine.createOpenglContext();
                Globals.renderingEngine.destroy();
                Globals.resetGlobals();
            }
        });
    }

}