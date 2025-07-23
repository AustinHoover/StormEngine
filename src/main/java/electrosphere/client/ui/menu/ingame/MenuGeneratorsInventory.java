package electrosphere.client.ui.menu.ingame;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.DraggableElement.DragEventCallback;
import electrosphere.renderer.ui.events.DragEvent;

public class MenuGeneratorsInventory {


    public static Element worldItemDropCaptureWindow(){
        Window rVal = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, false);
        Div div = Div.createDiv();
        div.setOnDragRelease(new DragEventCallback() {public boolean execute(DragEvent event){
            LoggerInterface.loggerUI.INFO("World item drop capture window received drag release");
            if(Globals.clientState.draggedItem != null){
                    //drop item
                    ClientInventoryState.clientAttemptEjectItem(Globals.clientState.playerEntity,Globals.clientState.draggedItem);
                    //play sound effect
                    if(Globals.audioEngine != null){
                        Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(Globals.clientState.draggedItem));
                        if(itemData.getItemAudio() != null && itemData.getItemAudio().getUIReleaseAudio() != null){
                            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(itemData.getItemAudio().getUIReleaseAudio(), VirtualAudioSourceType.UI, false);
                        } else {
                            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_ITEM_RELEASE, VirtualAudioSourceType.UI, false);
                        }
                    }
                    //clear ui
                    WindowUtils.cleanItemDraggingWindow();
                    //re-render inventory
                    WindowUtils.replaceWindow(WindowStrings.WINDOW_CHARACTER, InventoryMainWindow.createInventoryWindow(Globals.clientState.playerEntity));
                    //null globals
                    Globals.clientState.dragSourceInventory = null;
                    Globals.clientState.draggedItem = null;
                    return false;
            }
            return true;
        }});
        div.setPositionX(0);
        div.setPositionY(0);
        div.setWidth(Globals.WINDOW_WIDTH);
        div.setHeight(Globals.WINDOW_HEIGHT);
        div.setFlexGrow(1);
        rVal.addChild(div);
        return rVal;
    }

}
