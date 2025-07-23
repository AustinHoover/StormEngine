package electrosphere.client.ui.menu.debug.entity.tabs;

import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.item.ItemUtils;
import imgui.ImGui;

/**
 * Tab for inventory data
 */
public class ImGuiEntityInventoryTab {
    
    /**
     * Inventory view
     */
    public static void drawInventoryTab(boolean show, Entity detailViewEntity){
        if(detailViewEntity == null){
            return;
        }
        if(show && ImGui.collapsingHeader("Inventory Data")){
            ImGui.indent();
            if(InventoryUtils.hasNaturalInventory(detailViewEntity)){
                if(ImGui.collapsingHeader("Natural Inventory")){
                    UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(detailViewEntity);
                    for(Entity itemEnt : naturalInventory.getItems()){
                        ImGui.text(itemEnt.getId() + " - " + ItemUtils.getType(itemEnt));
                    }
                }
            }
            ImGui.unindent();
        }
    }

    /**
     * Client scene entity view
     */
    public static void drawToolbarTab(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Toolbar Data")){
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(detailViewEntity);
            ImGui.indent();
            if(detailViewEntity != null && clientToolbarState != null){
                ImGui.text("Selected slot: " + clientToolbarState.getSelectedSlot());
                ImGui.text("Selected item: " + clientToolbarState.getCurrentPrimaryItem());
            }
            ImGui.unindent();
        }
    }

}
