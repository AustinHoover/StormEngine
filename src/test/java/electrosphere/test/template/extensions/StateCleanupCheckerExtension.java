package electrosphere.test.template.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.RenderingEngine;

/**
 * Checks to make sure state has been properly reset. Must be the FIRST declared extension to work properly
 */
public class StateCleanupCheckerExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Object[] objectsToCheck = new Object[]{
            Globals.renderingEngine,
            Globals.audioEngine,
            LoggerInterface.loggerEngine,
            RenderingEngine.screenFramebuffer,
            Globals.engineState,
            Globals.clientState,
            Globals.serverState,
        };
        for(Object object : objectsToCheck){
            if(object != null){
                throw new Exception("Failed to cleanup state after test! " + object.toString());
            }
        }
    }
    
}
