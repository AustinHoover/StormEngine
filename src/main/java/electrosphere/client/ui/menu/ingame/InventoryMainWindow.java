package electrosphere.client.ui.menu.ingame;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.components.EquipmentInventoryPanel;
import electrosphere.client.ui.components.ItemIconPanel;
import electrosphere.client.ui.components.NaturalInventoryPanel;
import electrosphere.client.ui.components.ToolbarInventoryPanel;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;
import electrosphere.renderer.ui.events.NavigationEvent;

/**
 * The main inventory display window
 */
public class InventoryMainWindow {

    /**
     * Maximum allowed height
     */
    public static final int MAX_HEIGHT = 750;

    /**
     * Margins around the window
     */
    public static final int MARGIN = 100;

    /**
     * Views an inventory of an entity
     * @param entity The entity
     */
    public static void viewInventory(Entity entity){
        if(entity == Globals.clientState.playerEntity){
            if(Globals.elementService.getWindow(WindowStrings.WINDOW_CHARACTER) == null){
                //create window
                Window mainMenuWindow = InventoryMainWindow.createInventoryWindow(entity);
                //register
                Globals.elementService.registerWindow(WindowStrings.WINDOW_CHARACTER, mainMenuWindow);
                //make visible
                WindowUtils.recursiveSetVisible(WindowStrings.WINDOW_CHARACTER, true);
                //controls
                Globals.controlHandler.hintUpdateControlState(ControlsState.INVENTORY);
                //play sound effect
                if(Globals.audioEngine != null){
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_INVENTORY_OPEN, VirtualAudioSourceType.UI, false);
                }
                Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(true);
                //
                Globals.clientState.openInventoriesCount++;
            } else if(Globals.elementService.getWindow(WindowStrings.WINDOW_CHARACTER) != null){
                Globals.elementService.closeWindow(WindowStrings.WINDOW_CHARACTER);
                WindowUtils.clearTooltips();
                Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(false);
                if(Globals.audioEngine != null){
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_INVENTORY_CLOSE, VirtualAudioSourceType.UI, false);
                }
            }
        } else {
            if(Globals.elementService.getWindow(WindowStrings.WINDOW_INVENTORY_TARGET) == null){
                Globals.clientState.targetContainer = entity;
                //create window
                Window mainMenuWindow = InventoryMainWindow.createInventoryWindow(entity);
                //register
                Globals.elementService.registerWindow(WindowStrings.WINDOW_INVENTORY_TARGET, mainMenuWindow);
                //make visible
                WindowUtils.recursiveSetVisible(WindowStrings.WINDOW_INVENTORY_TARGET, true);
                //controls
                Globals.controlHandler.hintUpdateControlState(ControlsState.INVENTORY);
                //play sound effect
                if(Globals.audioEngine != null){
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_INVENTORY_OPEN, VirtualAudioSourceType.UI, false);
                }
                Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(true);
                //
                Globals.clientState.openInventoriesCount++;
            } else if(Globals.elementService.getWindow(WindowStrings.WINDOW_INVENTORY_TARGET) != null){
                Globals.elementService.closeWindow(WindowStrings.WINDOW_INVENTORY_TARGET);
                WindowUtils.clearTooltips();
                Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(false);
                if(Globals.audioEngine != null){
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_INVENTORY_CLOSE, VirtualAudioSourceType.UI, false);
                }
            }
        }
        InventoryMainWindow.updateInventoryWindowPositions();
    }

    /**
     * Creates a window that views an entity's inventory
     * @param entity The entity with an inventory
     * @return The Window element for the window
     */
    public static Window createInventoryWindow(Entity entity){
        if(entity == null){
            throw new Error("Trying to recreate inventory window with null entity!");
        }
        Window rVal = Window.createExpandableCenterAligned(Globals.renderingEngine.getOpenGLState());
        rVal.setMarginBottom(MARGIN);
        rVal.setMarginLeft(MARGIN);
        rVal.setMarginRight(MARGIN);
        rVal.setMarginTop(MARGIN);
        rVal.setWidth(MAX_HEIGHT);
        rVal.setHeight(MAX_HEIGHT);
        rVal.setMinWidth(MAX_HEIGHT);
        rVal.setMinHeight(MAX_HEIGHT);

        String windowString;
        if(entity == Globals.clientState.playerEntity){
            windowString = WindowStrings.WINDOW_CHARACTER;
        } else {
            windowString = WindowStrings.WINDOW_INVENTORY_TARGET;
        }
        InventoryMainWindow.updateSpecificInventoryPlacement(rVal,windowString);

        rVal.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(windowString), false);
            Globals.elementService.unregisterWindow(windowString);
            if(windowString.equals(WindowStrings.WINDOW_INVENTORY_TARGET)){
                Globals.clientState.targetContainer = null;
            }
            if(Globals.cameraHandler.getTrackPlayerEntity()){
                Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            } else {
                Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_FREE_CAMERA);
            }
            if(Globals.audioEngine != null){
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_INVENTORY_CLOSE, VirtualAudioSourceType.UI, false);
            }
            ItemIconPanel.clearTooltip();
            Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(false);
            return false;
        }});


        //
        //contents
        //

        if(InventoryUtils.hasEquipInventory(entity)){
            rVal.addChild(EquipmentInventoryPanel.createEquipmentInventoryPanel(entity));
        }

        if(InventoryUtils.hasNaturalInventory(entity)){
            rVal.addChild(NaturalInventoryPanel.createNaturalInventoryPanel(entity));
        }

        if(InventoryUtils.hasToolbarInventory(entity)){
            rVal.addChild(ToolbarInventoryPanel.createToolbarInventoryPanel(entity, true));
        }

        //
        //Final setup
        //

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, rVal);

        

        return rVal;
    }

    /**
     * Updates the inventory window positions
     */
    private static void updateInventoryWindowPositions(){
        Window characterWindow = ((Window)Globals.elementService.getWindow(WindowStrings.WINDOW_CHARACTER));
        Window targetWindow = ((Window)Globals.elementService.getWindow(WindowStrings.WINDOW_INVENTORY_TARGET));
        if(
            characterWindow != null &&
            targetWindow != null
        ){
            characterWindow.setParentAlignItem(YogaAlignment.Start);
            targetWindow.setParentAlignItem(YogaAlignment.End);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, characterWindow);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, targetWindow);
        } else {
            if(characterWindow != null){
                characterWindow.setParentAlignItem(YogaAlignment.Center);
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, characterWindow);
            }
            if(targetWindow != null){
                targetWindow.setParentAlignItem(YogaAlignment.Center);
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, targetWindow);
            }
        }
    }

    /**
     * Updates a specific inventory's placement
     * @param window The window element
     * @param windowString The window string for this element
     */
    private static void updateSpecificInventoryPlacement(Window window, String windowString){
        Window characterWindow = ((Window)Globals.elementService.getWindow(WindowStrings.WINDOW_CHARACTER));
        Window targetWindow = ((Window)Globals.elementService.getWindow(WindowStrings.WINDOW_INVENTORY_TARGET));
        if(windowString.equals(WindowStrings.WINDOW_CHARACTER)){
            characterWindow = window;
        }
        if(windowString.equals(WindowStrings.WINDOW_INVENTORY_TARGET)){
            targetWindow = window;
        }
        if(
            characterWindow != null &&
            targetWindow != null
        ){
            characterWindow.setParentAlignItem(YogaAlignment.Start);
            targetWindow.setParentAlignItem(YogaAlignment.End);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, characterWindow);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, targetWindow);
        } else {
            if(characterWindow != null){
                characterWindow.setParentAlignItem(YogaAlignment.Center);
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, characterWindow);
            }
            if(targetWindow != null){
                targetWindow.setParentAlignItem(YogaAlignment.Center);
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, targetWindow);
            }
        }
    }

}
