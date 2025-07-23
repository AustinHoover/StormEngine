package electrosphere.test.template.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import electrosphere.engine.Globals;
import electrosphere.renderer.RenderingEngine;

/**
 * Spins up an tears down generic rendering environment
 */
public class RenderingExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Globals.initGlobals();
        Globals.renderingEngine = new RenderingEngine();
        Globals.renderingEngine.createOpenglContext();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Globals.renderingEngine.destroy();
        Globals.resetGlobals();
    }

}