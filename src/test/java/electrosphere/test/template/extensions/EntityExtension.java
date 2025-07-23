package electrosphere.test.template.extensions;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import electrosphere.engine.Main;
import electrosphere.test.testutils.EngineInit;

/**
 * Spins up and tears down entity testing environment
 */
public class EntityExtension implements BeforeEachCallback, AfterEachCallback {
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        //init engine
        EngineInit.initGraphicalEngine();
        
        //load scene
        EngineInit.setupConnectedTestViewport();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Main.shutdown();
        //sleep to keep the ide from exploding when running tests manually
        TimeUnit.MILLISECONDS.sleep(1);
    }

}
