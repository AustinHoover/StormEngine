package electrosphere.client.ui.menu.ingame;

import electrosphere.client.ui.components.ToolbarInventoryPanel;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;

/**
 * Window that shows the toolbar preview
 */
public class ToolbarPreviewWindow {
    
    /**
     * Frame number to hide the toolbar preview on
     */
    static long frameToHide = 0;

    /**
     * The preview window
     */
    static Window previewWindow;

    //width of the panel
    static final int WINDOW_WIDTH = 550;
    static final int WINDOW_HEIGHT = 550;

    /**
     * Number of frames to reveal for
     */
    static final int REVEAL_FRAME_COUNT = 50;

    /**
     * Creates the level editor side panel window
     * @return
     */
    public static Window createToolbarPreviewWindow(){
        //setup window
        Window rVal = Window.createExpandableCenterAligned(Globals.renderingEngine.getOpenGLState(), false);
        rVal.setJustifyContent(YogaJustification.End);

        //attach scrollable after search input for organzation purposes
        rVal.addChild(ToolbarInventoryPanel.createToolbarInventoryPanel(Globals.clientState.playerEntity, false));

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,rVal);

        return rVal;
    }

    /**
     * Checks the visibility of the toolbar preview
     */
    public static void checkVisibility(){
        long currentFrame = Globals.engineState.timekeeper.getNumberOfSimFramesElapsed();
        if(currentFrame > frameToHide){
            if(previewWindow != null && previewWindow.getVisible()){
                previewWindow.setVisible(false);
            }
        }
    }

    /**
     * Reveals the toolbar preview window
     */
    public static void reveal(){
        previewWindow = ToolbarPreviewWindow.createToolbarPreviewWindow();
        WindowUtils.replaceWindow(WindowStrings.TOOLBAR_PREVIEW, previewWindow);
        frameToHide = Globals.engineState.timekeeper.getNumberOfSimFramesElapsed() + REVEAL_FRAME_COUNT;
    }

}
