package electrosphere.client.ui.menu;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * Utilities for working with yoga
 */
public class YogaUtils {
    
    /**
     * Refreshes a component
     * @param containingWindow The window containing the component
     * @param render The function that renders the component's content
     */
    public static void refreshComponent(Element containingWindow, Runnable render){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,render);
        if(containingWindow != null){
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, containingWindow);
        }
    }

}
