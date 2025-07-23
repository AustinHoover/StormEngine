package electrosphere.client.ui.menu.debug.entity.tabs;

import electrosphere.client.ui.menu.debug.server.ImGuiServerDataCell;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.datacell.ServerDataCell;
import imgui.ImGui;

/**
 * Display entity data related to server
 */
public class ImGuiEntityServerTab {
    
    /**
     * Physics view
     */
    public static void drawServerView(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Server Details")){
            ImGui.indent();
            ServerDataCell serverDataCell = Globals.serverState.entityDataCellMapper.getEntityDataCell(detailViewEntity);
            if(serverDataCell == null){
                ImGui.text("Entity's data cell is null!");
            } else {
                if(ImGui.button("View Data Cell")){
                    ImGuiServerDataCell.viewCell(serverDataCell);
                }
            }
            ImGui.unindent();
        }
    }

}
