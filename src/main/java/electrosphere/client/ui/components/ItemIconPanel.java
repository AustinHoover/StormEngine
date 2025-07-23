package electrosphere.client.ui.components;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.ingame.InventoryMainWindow;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.state.item.ClientChargeState;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Tooltip;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaPositionType;
import electrosphere.renderer.ui.elementtypes.DraggableElement.DragEventCallback;
import electrosphere.renderer.ui.elementtypes.HoverableElement.HoverEventCallback;
import electrosphere.renderer.ui.events.DragEvent;
import electrosphere.renderer.ui.events.HoverEvent;

/**
 * Creates item icon panels
 */
public class ItemIconPanel {
    
    /**
     * The tooltip for the currently hovered item
     */
    static Tooltip itemTooltip;

    /**
     * The starting drag container
     */
    static ContainerElement dragStartContainer = null;

    /**
     * Creates an item panel
     * @param currentItem The item entity
     * @return The item panel
     */
    public static Div createPanel(Entity currentItem, int itemId, Object inventory){
        Div rVal = Div.createDiv();

        String texturePath = "Textures/ui/uiFrame1.png";
        if(currentItem != null){
            //get texture path from item
            texturePath = ItemUtils.getItemIcon(currentItem);
        }
        Entity finalEnt = currentItem;
        if(!Globals.assetManager.hasLoadedTexture(texturePath)){
            Globals.assetManager.addTexturePathtoQueue(texturePath);
        }
        int panelWidth = 50;
        int panelHeight = 50;
        ImagePanel panel = ImagePanel.createImagePanel(texturePath);
        panel.setMinWidth(panelWidth);
        panel.setMinHeight(panelHeight);
        panel.setMarginRight(5);
        panel.setMarginBottom(5);
        panel.setMarginLeft(5);
        panel.setMarginTop(5);
        panel.setAlignSelf(YogaAlignment.Start);
        panel.setAbsolutePosition(false);
        panel.setOnDragStart(new DragEventCallback() {public boolean execute(DragEvent event){
            ItemIconPanel.dragStartContainer = panel.getParent();
            // System.out.println("Drag start");
            Globals.clientState.dragSourceInventory = inventory;
            if(inventory instanceof RelationalInventoryState){
                Globals.clientState.draggedItem = ((RelationalInventoryState)inventory).getItemSlot("" + itemId);
            } else {
                Globals.clientState.draggedItem = ((UnrelationalInventoryState)inventory).getItems().get(itemId);
            }
            ContainerElement container = (ContainerElement)panel.getParent();
            container.removeChild(panel);
            WindowUtils.pushItemIconToItemWindow(panel);
            //set new flex values now that its in this item dragging window
            panel.setPositionType(YogaPositionType.Absolute);
            panel.setPositionX(panel.getAbsoluteX());
            panel.setPositionY(panel.getAbsoluteY());
            WindowUtils.replaceWindow(WindowStrings.WINDOW_CHARACTER, InventoryMainWindow.createInventoryWindow(Globals.clientState.playerEntity));
            //play sound effect
            if(Globals.audioEngine != null){
                Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(finalEnt));
                if(itemData.getItemAudio() != null && itemData.getItemAudio().getUIGrabAudio() != null){
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(itemData.getItemAudio().getUIGrabAudio(), VirtualAudioSourceType.UI, false);
                } else {
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_ITEM_GRAB, VirtualAudioSourceType.UI, false);
                }
            }
            return false;
        }});
        panel.setOnDrag(new DragEventCallback() {public boolean execute(DragEvent event){
            // System.out.println("Drag");
            panel.setPositionX(event.getCurrentX() - panelWidth / 2);
            panel.setPositionY(event.getCurrentY() - panelHeight / 2);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, Globals.elementService.getWindow(WindowStrings.WINDOW_ITEM_DRAG_CONTAINER));
            return false;
        }});
        panel.setOnDragRelease(new DragEventCallback(){public boolean execute(DragEvent event){
            if(panel.getParent() != ItemIconPanel.dragStartContainer){
                if(panel.getParent() != null){
                    ContainerElement container = (ContainerElement)panel.getParent();
                    container.removeChild(panel);
                }
                panel.setPositionType(YogaPositionType.Relative);
                Globals.elementService.fireEvent(event, event.getCurrentX(), event.getCurrentY());
            }
            return false;
        }});
        panel.setOnHoverCallback(new HoverEventCallback() {public boolean execute(HoverEvent event){
            if(event.isHovered() && Globals.clientState.draggedItem == null){
                if(itemTooltip != null){
                    Tooltip.destroy(itemTooltip);
                }
                Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(currentItem);
                Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,()->{
                    itemTooltip = Tooltip.create(Div.createCol(Label.createLabel(itemData.getId())));
                    itemTooltip.setPositionX(panel.getAbsoluteX() + panelWidth);
                    itemTooltip.setPositionY(panel.getAbsoluteY());
                });
            } else {
                if(itemTooltip != null){
                    Tooltip.destroy(itemTooltip);
                }
            }
            return false;
        }});

        rVal.addChild(panel);

        if(ClientChargeState.hasClientChargeState(currentItem)){
            ImagePanel labelBackground = ImagePanel.createImagePanel(AssetDataStrings.TEXTURE_BLACK);
            labelBackground.setPositionType(YogaPositionType.Absolute);
            labelBackground.setWidth(20);
            labelBackground.setHeight(20);
            labelBackground.setPositionY(5);
            rVal.addChild(labelBackground);


            ClientChargeState clientChargeState = ClientChargeState.getClientChargeState(currentItem);
            Label chargeLabel = Label.createLabel("" + clientChargeState.getCharges());
            chargeLabel.setPositionX(5);
            chargeLabel.setPositionY(5);
            chargeLabel.setPositionType(YogaPositionType.Absolute);
            rVal.addChild(chargeLabel);
        }

        return rVal;
    }

    /**
     * Creates a panel that can receive items
     * @param onReceiveItem The logic to run when the item is received
     * @return The panel
     */
    public static Div createEmptyItemPanel(Runnable onReceiveItem){
        Div rVal = Div.createDiv();

        String texturePath = "Textures/ui/uiFrame1.png";
        if(!Globals.assetManager.hasLoadedTexture(texturePath)){
            Globals.assetManager.addTexturePathtoQueue(texturePath);
        }
        int panelWidth = 50;
        int panelHeight = 50;
        ImagePanel panel = ImagePanel.createImagePanel(texturePath);
        panel.setMinWidth(panelWidth);
        panel.setMinHeight(panelHeight);
        panel.setMarginRight(5);
        panel.setMarginBottom(5);
        panel.setMarginLeft(5);
        panel.setMarginTop(5);
        panel.setAlignSelf(YogaAlignment.Start);
        panel.setAbsolutePosition(false);
        panel.setOnDragRelease(new DragEventCallback(){public boolean execute(DragEvent event){
            onReceiveItem.run();
            Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(Globals.clientState.draggedItem));
            if(Globals.audioEngine != null){
                if(itemData != null && itemData.getItemAudio() != null && itemData.getItemAudio().getUIReleaseAudio() != null){
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(itemData.getItemAudio().getUIReleaseAudio(), VirtualAudioSourceType.UI, false);
                } else {
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_ITEM_RELEASE, VirtualAudioSourceType.UI, false);
                }
            }
            //update ui
            Globals.clientState.dragSourceInventory = null;
            Globals.clientState.draggedItem = null;
            //clear item container ui
            WindowUtils.cleanItemDraggingWindow();
            //rerender inventories
            WindowUtils.replaceWindow(WindowStrings.WINDOW_CHARACTER, InventoryMainWindow.createInventoryWindow(Globals.clientState.playerEntity));
            return false;
        }});
        
        rVal.addChild(panel);
        return rVal;
    }

    /**
     * Clears the tooltip
     */
    public static void clearTooltip(){
        if(itemTooltip != null){
            Tooltip.destroy(itemTooltip);
        }
    }

}
