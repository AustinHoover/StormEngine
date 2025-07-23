package electrosphere.client.ui.components;

import java.util.List;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.ingame.InventoryMainWindow;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elementtypes.ClickableElement.ClickEventCallback;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.DraggableElement.DragEventCallback;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.DragEvent;

/**
 * Inventory panel showing an entity's equipment
 */
public class EquipmentInventoryPanel {

    

    /**
     * Creates the equipment inventory panel
     * @param inventory The inventory
     * @return The panel
     */
    public static Element createEquipmentInventoryPanel(Entity entity){
        RelationalInventoryState inventory = InventoryUtils.getEquipInventory(entity);
        
        Div div = Div.createDiv();
        div.setJustifyContent(YogaJustification.Center);
        div.setMarginBottom(25);
        div.setMarginLeft(25);
        div.setMarginRight(25);
        div.setMarginTop(25);

        div.setOnDragRelease(new DragEventCallback() {public boolean execute(DragEvent event){
            LoggerInterface.loggerUI.INFO("Character inventory received drag release event");
            if(Globals.clientState.draggedItem != null){
                //play sound effect
                if(Globals.audioEngine != null){
                    Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(Globals.clientState.draggedItem));
                    if(itemData.getItemAudio() != null && itemData.getItemAudio().getUIReleaseAudio() != null){
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(itemData.getItemAudio().getUIReleaseAudio(), VirtualAudioSourceType.UI, false);
                    } else {
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_ITEM_RELEASE, VirtualAudioSourceType.UI, false);
                    }
                }
                //null out global state
                Globals.clientState.dragSourceInventory = null;
                Globals.clientState.draggedItem = null;
                //clear item container ui
                WindowUtils.cleanItemDraggingWindow();
                //re-render inventory
                WindowUtils.replaceWindow(WindowStrings.WINDOW_CHARACTER, InventoryMainWindow.createInventoryWindow(Globals.clientState.playerEntity));
                return false;
            }
            return true;
        }});

        div.setOnClick(new ClickEventCallback() {public boolean execute(ClickEvent event){
            WindowUtils.focusWindow(WindowStrings.WINDOW_CHARACTER);
            return false;
        }});
        

        //label 1 (inventory)
        Label inventoryLabel = Label.createLabel("CHARACTER");
        inventoryLabel.setMarginBottom(10);
        div.addChild(inventoryLabel);


        {
            Div leftSlots = Div.createCol();
            leftSlots.setFlexGrow(1);
            Div rightSlots = Div.createCol();
            rightSlots.setFlexGrow(1);
            rightSlots.setAlignItems(YogaAlignment.End);

            List<String> slots = inventory.getSlots();
            EquipPoint equipPoint = null;

            for(int i = 0; i < slots.size(); i++){
                Div colContainer = i % 2 == 0 ? leftSlots : rightSlots;
                boolean endAlign = i % 2 == 1;
                String texturePath = "Textures/ui/uiFrame1.png";
                boolean hasItem = false;
                String slotId = slots.get(i);
                Entity currentItem = null;
                equipPoint = inventory.getEquipPointFromSlot(slotId);
                if(!equipPoint.isCombinedPoint() && !equipPoint.isToolbarSlot()){
                    if(inventory.getItemSlot(slotId) != null && inventory.getItemSlot(slotId) != Globals.clientState.draggedItem){
                        currentItem = inventory.getItemSlot(slotId);
                        //get texture path from item
                        texturePath = ItemUtils.getItemIcon(currentItem);
                        //flag that this isn't an empty slot
                        hasItem = true;
                    } else if(inventory.getCombinedPoint(slotId) != null && inventory.hasItemInSlot(inventory.getCombinedPoint(slotId).getEquipPointId()) && inventory.getItemSlot(inventory.getCombinedPoint(slotId).getEquipPointId()) != Globals.clientState.draggedItem){
                        currentItem = inventory.getItemSlot(inventory.getCombinedPoint(slotId).getEquipPointId());
                        //get texture path from item
                        texturePath = ItemUtils.getItemIcon(currentItem);
                        //flag that this isn't an empty slot
                        hasItem = true;
                        equipPoint = inventory.getCombinedPoint(slotId);
                        slotId = equipPoint.getEquipPointId();
                    }
                    Entity finalEnt = currentItem;
                    if(!Globals.assetManager.hasLoadedTexture(texturePath)){
                        Globals.assetManager.addTexturePathtoQueue(texturePath);
                    }
                    int panelWidth = 50;
                    int panelHeight = 50;
                    ImagePanel panel = ImagePanel.createImagePanel(texturePath);
                    panel.setWidth(50);
                    panel.setHeight(50);
                    panel.setMarginBottom(5);
                    panel.setMarginLeft(15);
                    panel.setMarginRight(15);
                    panel.setMarginTop(15);
                    if(endAlign){
                        panel.setAlignSelf(YogaAlignment.End);
                    }
                    if(hasItem == true){
                        //literally just here to get around finality of variable within callback
                        String finalSlotId = slotId;
                        panel.setOnDragStart(new DragEventCallback() {
                            public boolean execute(DragEvent event){
                                LoggerInterface.loggerUI.DEBUG("Drag start");
                                Globals.clientState.dragSourceInventory = inventory;
                                Globals.clientState.draggedItem = inventory.getItemSlot(finalSlotId);
                                ContainerElement container = (ContainerElement)panel.getParent();
                                container.removeChild(panel);
                                WindowUtils.pushItemIconToItemWindow(panel);
                                panel.setAbsolutePosition(true);
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
                            if(panel.getParent() != colContainer){
                                if(panel.getParent() != null){
                                    ContainerElement container = (ContainerElement)panel.getParent();
                                    container.removeChild(panel);
                                }
                                Globals.elementService.fireEvent(event, event.getCurrentX(), event.getCurrentY());
                                panel.setAbsolutePosition(false);
                            }
                            return false;
                        }});
                        
                    } else {
                        int itemId = i;
                        panel.setOnDragRelease(new DragEventCallback(){public boolean execute(DragEvent event){
                            if(Globals.clientState.dragSourceInventory instanceof RelationalInventoryState){
                                if(Globals.clientState.dragSourceInventory == InventoryUtils.getToolbarInventory(entity)){
                                    if(inventory.canEquipItemToSlot(Globals.clientState.draggedItem, slots.get(itemId))){
                                        //fire equip event to equip state
                                        ClientEquipState equipState = ClientEquipState.getEquipState(Globals.clientState.playerEntity);
                                        equipState.commandAttemptEquip(Globals.clientState.draggedItem,inventory.getEquipPointFromSlot(slots.get(itemId)));
                                        //play sound effect
                                        if(Globals.audioEngine != null){
                                            Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(finalEnt));
                                            if(itemData != null && itemData.getItemAudio() != null && itemData.getItemAudio().getUIReleaseAudio() != null){
                                                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(itemData.getItemAudio().getUIReleaseAudio(), VirtualAudioSourceType.UI, false);
                                            } else {
                                                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_ITEM_RELEASE, VirtualAudioSourceType.UI, false);
                                            }
                                        }
                                    } else if(inventory.canEquipItemToCombinedSlot(Globals.clientState.draggedItem, slots.get(itemId))){
                                        EquipPoint combinedPoint = inventory.getCombinedPoint(slots.get(itemId));
                                        //fire equip event to equip state
                                        ClientEquipState equipState = ClientEquipState.getEquipState(Globals.clientState.playerEntity);
                                        equipState.commandAttemptEquip(Globals.clientState.draggedItem,combinedPoint);
                                    }
                                } else if(ItemUtils.getContainingParent(Globals.clientState.draggedItem) != Globals.clientState.playerEntity){
                                    throw new UnsupportedOperationException("Unimplemented!");
                                }
                            } else if(Globals.clientState.dragSourceInventory instanceof UnrelationalInventoryState){
                                if(inventory.canEquipItemToSlot(Globals.clientState.draggedItem, slots.get(itemId))){
                                    //fire equip event to equip state
                                    ClientEquipState equipState = ClientEquipState.getEquipState(Globals.clientState.playerEntity);
                                    equipState.commandAttemptEquip(Globals.clientState.draggedItem,inventory.getEquipPointFromSlot(slots.get(itemId)));
                                    //play sound effect
                                    if(Globals.audioEngine != null){
                                        Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(finalEnt));
                                        if(itemData != null && itemData.getItemAudio() != null && itemData.getItemAudio().getUIReleaseAudio() != null){
                                            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(itemData.getItemAudio().getUIReleaseAudio(), VirtualAudioSourceType.UI, false);
                                        } else {
                                            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_SFX_ITEM_RELEASE, VirtualAudioSourceType.UI, false);
                                        }
                                    }
                                } else if(inventory.canEquipItemToCombinedSlot(Globals.clientState.draggedItem, slots.get(itemId))){
                                    EquipPoint combinedPoint = inventory.getCombinedPoint(slots.get(itemId));
                                    //fire equip event to equip state
                                    ClientEquipState equipState = ClientEquipState.getEquipState(Globals.clientState.playerEntity);
                                    equipState.commandAttemptEquip(Globals.clientState.draggedItem,combinedPoint);
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
                            //update ui
                            Globals.clientState.dragSourceInventory = null;
                            Globals.clientState.draggedItem = null;
                            //clear item container ui
                            WindowUtils.cleanItemDraggingWindow();
                            //rerender inventories
                            WindowUtils.replaceWindow(WindowStrings.WINDOW_CHARACTER, InventoryMainWindow.createInventoryWindow(Globals.clientState.playerEntity));
                            return false;
                        }});
                    }
                    colContainer.addChild(panel);

                    //create the slot text
                    Label slotLabel = Label.createLabel(slots.get(i));
                    slotLabel.setMarginBottom(5);
                    slotLabel.setMarginLeft(15);
                    slotLabel.setMarginRight(15);
                    slotLabel.setMarginTop(5);
                    colContainer.addChild(slotLabel);
                }
            }
            Div columnContainer = Div.createRow(leftSlots,rightSlots);
            columnContainer.setAlignItems(YogaAlignment.Between);
            columnContainer.setFlexGrow(1);
            div.addChild(columnContainer);
        }

        return div;
    }


}
