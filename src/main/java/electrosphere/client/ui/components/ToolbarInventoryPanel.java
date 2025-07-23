package electrosphere.client.ui.components;

import org.joml.Vector4f;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.ingame.InventoryMainWindow;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.protocol.InventoryProtocol;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Panel;
import electrosphere.renderer.ui.elementtypes.ClickableElement.ClickEventCallback;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.DraggableElement.DragEventCallback;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.DragEvent;

/**
 * Toolbar inventory panel
 */
public class ToolbarInventoryPanel {
    
    /**
     * Creates the toolbar inventory panel
     * @param entity The entity who has the inventory
     * @return The panel element
     */
    public static Element createToolbarInventoryPanel(Entity entity, boolean showTitle){
        RelationalInventoryState inventory = InventoryUtils.getToolbarInventory(entity);

        int selectedIndex = 0;
        if(ClientToolbarState.hasClientToolbarState(entity)){
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(entity);
            selectedIndex = clientToolbarState.getSelectedSlot();
        }


        Div div = Div.createDiv();
        div.setJustifyContent(YogaJustification.Center);
        div.setMarginBottom(25);
        div.setMarginLeft(25);
        div.setMarginRight(25);
        div.setMarginTop(25);

        div.setOnDragRelease(new DragEventCallback() {public boolean execute(DragEvent event){
            LoggerInterface.loggerUI.INFO("Toolbar inventory received drag release event");
            if(Globals.clientState.draggedItem != null){
                if(Globals.clientState.dragSourceInventory != inventory){
                    if(Globals.clientState.dragSourceInventory instanceof RelationalInventoryState){
                        if(ClientEquipState.hasEquipState(entity) && InventoryUtils.hasEquipInventory(entity)){
                            RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(entity);
                            ClientEquipState equipState = ClientEquipState.getEquipState(entity);
                            equipState.commandAttemptUnequip(equipInventory.getItemSlot(Globals.clientState.draggedItem));
                        }
                    } if(Globals.clientState.dragSourceInventory instanceof UnrelationalInventoryState){
                        //transfer item
                        // sourceInventory.removeItem(Globals.draggedItem);
                        // inventory.addItem(Globals.draggedItem);
                        // //null out global state
                        // Globals.dragSourceInventory = null;
                        // Globals.draggedItem = null;
                        Entity item = Globals.clientState.draggedItem;
                        if(ClientEquipState.hasEquipState(entity) && InventoryUtils.hasEquipInventory(entity)){
                            RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(entity);
                            ClientEquipState equipState = ClientEquipState.getEquipState(entity);
                            equipState.commandAttemptUnequip(equipInventory.getItemSlot(item));
                        }
                        //clear item container ui
                        WindowUtils.cleanItemDraggingWindow();
                        //re-render inventory
                        WindowUtils.replaceWindow(WindowStrings.WINDOW_CHARACTER, InventoryMainWindow.createInventoryWindow(Globals.clientState.playerEntity));
                    }
                }
            }
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
            return true;
        }});

        div.setOnClick(new ClickEventCallback() {public boolean execute(ClickEvent event){
            WindowUtils.focusWindow(WindowStrings.WINDOW_MENU_INVENTORY);
            return false;
        }});
        

        //label 1 (inventory)
        if(showTitle){
            Label menuTitle = Label.createLabel("TOOLBAR");
            menuTitle.setMarginBottom(10);
            div.addChild(menuTitle);
        }

        {
            //contains all the item panels
            Div panelContainer = Div.createDiv();
            panelContainer.setFlexDirection(YogaFlexDirection.Row);

            for(int i = 0; i < inventory.getSlots().size(); i++){
                String texturePath = "Textures/ui/uiFrame1.png";
                boolean hasItem = false;
                Entity currentItem = null;
                if(inventory.getItemSlot("" + i) != Globals.clientState.draggedItem && inventory.getItemSlot("" + i) != null){
                    currentItem = inventory.getItemSlot("" + i);
                    //get texture path from item
                    texturePath = ItemUtils.getItemIcon(currentItem);
                    //flag that this isn't an empty slot
                    hasItem = true;
                }
                if(!Globals.assetManager.hasLoadedTexture(texturePath)){
                    Globals.assetManager.addTexturePathtoQueue(texturePath);
                }

                //create the actual item panel
                Div panel = null;
                if(hasItem == true && inventory.getItemSlot("" + i) != Globals.clientState.draggedItem){
                    panel = ItemIconPanel.createPanel(currentItem, i, inventory);
                } else {
                    int slotId = i;
                    panel = ItemIconPanel.createEmptyItemPanel(() -> {
                        NetworkMessage requestPickupMessage = InventoryMessage.constructclientRequestStoreItemMessage(
                            Globals.clientState.clientSceneWrapper.mapClientToServerId(entity.getId()),
                            InventoryProtocol.INVENTORY_TYPE_TOOLBAR,
                            slotId + "",
                            Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.draggedItem.getId())
                        );
                        Globals.clientState.clientConnection.queueOutgoingMessage(requestPickupMessage);
                    });
                }

                ContainerElement container = null;
                if(i == selectedIndex){
                    container = Panel.createPanel(panel);
                    ((Panel)container).setColor(new Vector4f(1.0f));
                } else {
                    container = Div.createCol(panel);
                    container.setMarginBottom(10);
                    container.setMarginLeft(10);
                    container.setMarginRight(10);
                    container.setMarginTop(10);
                }
                panelContainer.addChild(container);
            }
            div.addChild(panelContainer);
        }
        return div;
    }

}
