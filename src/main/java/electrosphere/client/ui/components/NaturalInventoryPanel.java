package electrosphere.client.ui.components;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.ingame.InventoryMainWindow;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ClientEquipState;
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
import electrosphere.renderer.ui.elementtypes.ClickableElement.ClickEventCallback;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaWrap;
import electrosphere.renderer.ui.elementtypes.DraggableElement.DragEventCallback;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.DragEvent;

/**
 * An inventory panel showing a natural inventory
 */
public class NaturalInventoryPanel {

    /**
     * Creates the natural inventory panel
     * @param entity The entity who has the inventory
     * @return The panel element
     */
    public static Element createNaturalInventoryPanel(Entity entity){
        UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(entity);


        Div div = Div.createDiv();
        div.setJustifyContent(YogaJustification.Center);
        div.setMarginBottom(25);
        div.setMarginLeft(25);
        div.setMarginRight(25);
        div.setMarginTop(25);

        div.setOnDragRelease(new DragEventCallback() {public boolean execute(DragEvent event){
            LoggerInterface.loggerUI.INFO("Natural inventory received drag release event");
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
        Label menuTitle = Label.createLabel("INVENTORY");
        menuTitle.setMarginBottom(10);
        div.addChild(menuTitle);

        {
            //contains all the item panels
            Div panelContainer = Div.createDiv();
            panelContainer.setFlexDirection(YogaFlexDirection.Row);
            panelContainer.setWrap(YogaWrap.WRAP);

            for(int i = 0; i < inventory.getCapacity(); i++){
                String texturePath = "Textures/ui/uiFrame1.png";
                boolean hasItem = false;
                Entity currentItem = null;
                if(i < inventory.getItems().size() && inventory.getItems().get(i) != Globals.clientState.draggedItem){
                    currentItem = inventory.getItems().get(i);
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
                if(hasItem == true && inventory.getItems().get(i) != Globals.clientState.draggedItem){
                    panel = ItemIconPanel.createPanel(currentItem, i, inventory);
                } else {
                    panel = ItemIconPanel.createEmptyItemPanel(() -> {
                        if(Globals.clientState.draggedItem != null){
                            NetworkMessage requestPickupMessage = InventoryMessage.constructclientRequestStoreItemMessage(
                                Globals.clientState.clientSceneWrapper.mapClientToServerId(entity.getId()),
                                InventoryProtocol.INVENTORY_TYPE_NATURAL,
                                0 + "",
                                Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.draggedItem.getId())
                            );
                            Globals.clientState.clientConnection.queueOutgoingMessage(requestPickupMessage);
                        }
                    });
                }
                panelContainer.addChild(panel);
            }
            div.addChild(panelContainer);
        }
        return div;
    }

}
