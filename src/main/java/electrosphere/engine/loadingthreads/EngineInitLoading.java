package electrosphere.engine.loadingthreads;

import electrosphere.engine.Globals;

/**
 * Loading routines to init different parts of the engine
 */
public class EngineInitLoading {
    
    /**
     * Loads the core assets of the scripting engine from disk and initializes the engine
     */
    protected static void loadScriptingEngine(Object[] params){
        Globals.engineState.scriptEngine.initScripts();
    }

}
