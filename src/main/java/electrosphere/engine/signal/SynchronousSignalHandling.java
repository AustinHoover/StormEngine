package electrosphere.engine.signal;

import electrosphere.engine.Globals;

/**
 * Synchronously handles signals
 */
public class SynchronousSignalHandling {
    
    /**
     * Runs the main thread signal handlers
     */
    public static void runMainThreadSignalHandlers(){
        Globals.engineState.scriptEngine.handleAllSignals();
        Globals.engineState.mainThreadSignalService.handleAllSignals();
    }

}
