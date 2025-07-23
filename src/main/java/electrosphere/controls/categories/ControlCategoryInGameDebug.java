package electrosphere.controls.categories;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import electrosphere.client.ui.menu.editor.ImGuiEditorWindows;
import electrosphere.controls.Control;
import electrosphere.controls.Control.ControlMethod;
import electrosphere.controls.Control.ControlType;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.MouseState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.CharacterMessage;

/**
 * Control callbacks for in-game debug controls
 */
public class ControlCategoryInGameDebug {

    public static final String DEBUG_FRAMESTEP = "framestep";
    public static final String DEBUG_SWAP_EDITOR_MODE = "swapEditorMode";
    public static final String DEBUG_SWITCH_FIRST_THIRD = "switchFirstThird";
    public static final String DEBUG_SWAP_GAME_EDITOR = "swapGameEditor";

    /**
     * Maps the controls
     * @param handler
     */
    public static void mapControls(ControlHandler handler){
        handler.addControl(DEBUG_FRAMESTEP, new Control(ControlType.KEY, GLFW.GLFW_KEY_P,false,"Framesetp","Steps the engine forward one frame"));
        handler.addControl(DEBUG_SWAP_EDITOR_MODE, new Control(ControlType.KEY, GLFW.GLFW_KEY_F4,false,"Swap Editor Entity","Swaps to/from the editor entity"));
        handler.addControl(DEBUG_SWITCH_FIRST_THIRD, new Control(ControlType.KEY, GLFW.GLFW_KEY_F5,false,"Switch First/Third Person","Swaps between first and third person"));
        handler.addControl(DEBUG_SWAP_GAME_EDITOR, new Control(ControlType.KEY, GLFW.GLFW_KEY_F6,false,"Toggle Editor Mode","Toggles between the editor mode and the actual game mode"));
    }
    
    /**
     * Populates the in-game debug controls list
     * @param controlMap
     */
    public static void setCallbacks(
        HashMap<String, Control> controlMap,
        List<Control> mainGameDebugControlList,
        List<Control> alwaysOnDebugControlList
    ){
        mainGameDebugControlList.add(controlMap.get(DEBUG_FRAMESTEP));
        controlMap.get(DEBUG_FRAMESTEP).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            Main.setFramestep(1);
        }});
        controlMap.get(DEBUG_FRAMESTEP).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            Main.setFramestep(1);
        }});
        controlMap.get(DEBUG_FRAMESTEP).setRepeatTimeout(0.5f * Main.targetFrameRate);
        // RenderingEngine.incrementOutputFramebuffer();


        //
        //Swap to/from editor entity
        //
        alwaysOnDebugControlList.add(controlMap.get(DEBUG_SWAP_EDITOR_MODE));
        controlMap.get(DEBUG_SWAP_EDITOR_MODE).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            LoggerInterface.loggerEngine.INFO("Swap to/from editor entity");
            Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructEditorSwapMessage());
        }});
        controlMap.get(DEBUG_SWAP_EDITOR_MODE).setRepeatTimeout(0.5f * Main.targetFrameRate);

        //
        //Swap first/third person
        //
        alwaysOnDebugControlList.add(controlMap.get(DEBUG_SWITCH_FIRST_THIRD));
        controlMap.get(DEBUG_SWITCH_FIRST_THIRD).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            LoggerInterface.loggerEngine.INFO("Swaps between first and third person");
            Globals.controlHandler.setIsThirdPerson(!Globals.controlHandler.cameraIsThirdPerson());
        }});
        controlMap.get(DEBUG_SWITCH_FIRST_THIRD).setRepeatTimeout(0.5f * Main.targetFrameRate);

        //
        //Swap between editor and game mode
        //
        alwaysOnDebugControlList.add(controlMap.get(DEBUG_SWAP_GAME_EDITOR));
        controlMap.get(DEBUG_SWAP_GAME_EDITOR).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            LoggerInterface.loggerEngine.INFO("Toggle Editor Mode");
            ImGuiEditorWindows.toggleEditorMenus();
        }});
        controlMap.get(DEBUG_SWAP_GAME_EDITOR).setRepeatTimeout(0.5f * Main.targetFrameRate);
    }

}
