package electrosphere.client.ui.menu.ingame;

import java.io.File;

import electrosphere.client.ui.components.FabSelectionPanel;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;
import electrosphere.renderer.ui.events.NavigationEvent;

/**
 * Menus to deal with fab files
 */
public class FabMenus {
    
    /**
     * The fab selection window
     */
    static Window fabSelectionWindow;

    //width of the panel
    static final int WINDOW_WIDTH = 550;
    static final int WINDOW_HEIGHT = 550;

    /**
     * Creates the level editor side panel window
     * @return
     */
    public static Window createFabSelectionPanel(){
        //setup window
        Window fabSelectionPanelWindow = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, true);
        fabSelectionPanelWindow.setParentAlignContent(YogaAlignment.Center);
        fabSelectionPanelWindow.setParentJustifyContent(YogaJustification.Center);
        fabSelectionPanelWindow.setParentAlignItem(YogaAlignment.Center);
        fabSelectionPanelWindow.setAlignContent(YogaAlignment.Center);
        fabSelectionPanelWindow.setAlignItems(YogaAlignment.Center);
        fabSelectionPanelWindow.setJustifyContent(YogaJustification.Center);
        fabSelectionPanelWindow.setFlexDirection(YogaFlexDirection.Column);

        //nav logic
        fabSelectionPanelWindow.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.closeWindow(WindowStrings.FAB_SELECTION);
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            MenuGeneratorsLevelEditor.voxelWindowOpen = false;
            return false;
        }});

        //attach scrollable after search input for organzation purposes
        fabSelectionPanelWindow.addChild(FabSelectionPanel.createFabSelectionPanel((File selectedFile) -> {
            BlockFab fab = BlockFab.read(selectedFile);
            Globals.cursorState.setSelectedFab(fab);
            Globals.cursorState.setSelectedFabPath("./assets/Data/fab/" + selectedFile.getName());
        }));

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,fabSelectionPanelWindow);

        return fabSelectionPanelWindow;
    }

}
