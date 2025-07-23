package electrosphere.client.ui.menu.ingame;

import electrosphere.client.ui.components.SpawnSelectionPanel;
import electrosphere.client.ui.components.VoxelSelectionPanel;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;
import electrosphere.renderer.ui.events.NavigationEvent;

/**
 * Menu generators for terrain editing controls
 */
public class MenuGeneratorsTerrainEditing {
    
    static Window entitySelectionWindow;

    //width of the side panel
    static final int WINDOW_WIDTH = 550;
    static final int WINDOW_HEIGHT = 550;

    /**
     * Creates the level editor side panel window
     * @return
     */
    public static Window createVoxelTypeSelectionPanel(){
        //setup window
        Window terrainEditingSidePanelWindow = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, true);
        terrainEditingSidePanelWindow.setParentAlignContent(YogaAlignment.Center);
        terrainEditingSidePanelWindow.setParentJustifyContent(YogaJustification.Center);
        terrainEditingSidePanelWindow.setParentAlignItem(YogaAlignment.Center);
        terrainEditingSidePanelWindow.setAlignContent(YogaAlignment.Center);
        terrainEditingSidePanelWindow.setAlignItems(YogaAlignment.Center);
        terrainEditingSidePanelWindow.setJustifyContent(YogaJustification.Center);
        terrainEditingSidePanelWindow.setFlexDirection(YogaFlexDirection.Column);

        //nav logic
        terrainEditingSidePanelWindow.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.closeWindow(WindowStrings.VOXEL_TYPE_SELECTION);
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            MenuGeneratorsLevelEditor.voxelWindowOpen = false;
            return false;
        }});

        //attach scrollable after search input for organzation purposes
        terrainEditingSidePanelWindow.addChild(VoxelSelectionPanel.createVoxelTypeSelectionPanel((VoxelType type) -> {
            Globals.clientState.clientSelectedVoxelType = type;
        }));

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,terrainEditingSidePanelWindow);

        return terrainEditingSidePanelWindow;
    }

    /**
     * Creates the entity type selection window
     * @return
     */
    public static Window createEntityTypeSelectionPanel(){
        //setup window
        entitySelectionWindow = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, true);
        entitySelectionWindow.setParentAlignContent(YogaAlignment.Center);
        entitySelectionWindow.setParentJustifyContent(YogaJustification.Center);
        entitySelectionWindow.setParentAlignItem(YogaAlignment.Center);
        entitySelectionWindow.setAlignContent(YogaAlignment.Center);
        entitySelectionWindow.setAlignItems(YogaAlignment.Center);
        entitySelectionWindow.setJustifyContent(YogaJustification.Center);
        entitySelectionWindow.setFlexDirection(YogaFlexDirection.Column);

        //nav logic
        entitySelectionWindow.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.closeWindow(WindowStrings.SPAWN_TYPE_SELECTION);
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            MenuGeneratorsLevelEditor.voxelWindowOpen = false;
            return false;
        }});

        //attach scrollable after search input for organzation purposes
        entitySelectionWindow.addChild(SpawnSelectionPanel.createEntityTypeSelectionPanel((CommonEntityType type) -> {
            Globals.clientState.selectedSpawntype = type;
        }));        

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,entitySelectionWindow);

        return entitySelectionWindow;
    }

}
