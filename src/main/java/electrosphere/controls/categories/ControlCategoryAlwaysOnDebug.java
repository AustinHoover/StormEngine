package electrosphere.controls.categories;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.debug.ImGuiWindowMacros;
import electrosphere.controls.Control;
import electrosphere.controls.Control.ControlMethod;
import electrosphere.controls.Control.ControlType;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.MouseState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;

public class ControlCategoryAlwaysOnDebug {

    public static final String DEBUG_OPEN_DEBUG_MENU = "openDebugMenu";
    
    /**
     * Maps the controls
     * @param handler
     */
    public static void mapControls(ControlHandler handler){
        handler.addControl(DEBUG_OPEN_DEBUG_MENU, new Control(ControlType.KEY, GLFW.GLFW_KEY_F2,false,"Debug Menu","Opens the debug menu"));
    }
    
    /**
     * Populates the in-game debug controls list
     * @param controlMap
     */
    public static void setCallbacks(
        HashMap<String, Control> controlMap,
        List<Control> alwaysOnDebugControlList
    ){
        alwaysOnDebugControlList.add(controlMap.get(DEBUG_OPEN_DEBUG_MENU));
        controlMap.get(DEBUG_OPEN_DEBUG_MENU).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            LoggerInterface.loggerEngine.INFO("open debug menu");
            ImGuiWindowMacros.toggleMainDebugMenu();
            //play sound effect
            if(Globals.audioEngine != null){
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_TONE_CONFIRM_PRIMARY, VirtualAudioSourceType.UI, false);
            }
        }});
        controlMap.get(DEBUG_OPEN_DEBUG_MENU).setRepeatTimeout(0.5f * Main.targetFrameRate);
    }

}
