package electrosphere.client.script;

import electrosphere.engine.Globals;
import electrosphere.script.ScriptEngine;

/**
 * Utilities for dealing with the scripting engine from the client's perspective
 */
public class ClientScriptUtils {
    
    /**
     * Fires a signal
     * @param signalName The name of the signal
     * @param args The arguments provided alongside the signal
     */
    public static void fireSignal(String signalName, Object ... args){
        Globals.engineState.scriptEngine.getScriptContext().executeSynchronously(() -> {
            if(Globals.engineState.scriptEngine != null && Globals.engineState.scriptEngine.isInitialized()){
                Globals.engineState.scriptEngine.getScriptContext().fireSignal(signalName, ScriptEngine.GLOBAL_SCENE, args);
            }
        });
    }

    /**
     * Evaluates a string
     * @param evalCode The string to evaluate
     */
    public static void eval(String evalCode){
        Globals.engineState.scriptEngine.getScriptContext().executeSynchronously(() -> {
            if(Globals.engineState.scriptEngine != null && Globals.engineState.scriptEngine.isInitialized()){
                Globals.engineState.scriptEngine.getScriptContext().eval(evalCode);
            }
        });
    }
    
}
