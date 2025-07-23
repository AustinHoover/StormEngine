package electrosphere.client.ui.menu.ingame;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.components.CraftingPanel;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.data.crafting.RecipeData;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;
import electrosphere.renderer.ui.events.NavigationEvent;

/**
 * Creates a crafting window
 */
public class CraftingWindow {

    /**
     * The data for crafting with your hands
     */
    public static final String HAND_CRAFTING_DATA = "HAND";

    /**
     * Creates a character customizer panel
     * @return The panel component
     */
    public static Window createCraftingWindow(String data){
        Window rVal = Window.createExpandableCenterAligned(Globals.renderingEngine.getOpenGLState());
        rVal.setParentAlignItem(YogaAlignment.Center);
        rVal.setParentJustifyContent(YogaJustification.Center);

        rVal.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.CRAFTING), false);
            Globals.elementService.unregisterWindow(WindowStrings.CRAFTING);
            if(Globals.cameraHandler.getTrackPlayerEntity()){
                Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            } else {
                Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_FREE_CAMERA);
            }
            if(Globals.audioEngine != null){
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_INVENTORY_CLOSE, VirtualAudioSourceType.UI, false);
            }
            Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(false);
            return false;
        }});


        //
        //contents
        //
        rVal.addChild(CraftingPanel.createCraftingPanelComponent(
            data,
            (RecipeData recipe) -> {
                if(Globals.clientState.interactionTarget != null){
                    Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestCraftMessage(
                        Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId()),
                        Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.interactionTarget.getId()),
                        recipe.getId()
                    ));
                } else {
                    Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestCraftMessage(
                        Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId()),
                        Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId()),
                        recipe.getId()
                    ));
                }
        }));

        //
        //Final setup
        //

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, rVal);

        

        return rVal;
    }

}
