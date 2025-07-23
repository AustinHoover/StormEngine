package electrosphere.client.ui.menu.debug.server;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import imgui.ImGui;

/**
 * Debug menu for gridded managers
 */
public class ImGuiGriddedManager {
    
    //window for viewing information about the ai state
    public static ImGuiWindow griddedManagerWindow;

    /**
     * Client scene entity view
     */
    public static void createGriddedManagerWindow(){
        griddedManagerWindow = new ImGuiWindow("Gridded Manager");
        griddedManagerWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                GriddedDataCellManager manager = null;
                if(Globals.serverState.realmManager != null && Globals.serverState.realmManager.first() != null){
                    Realm realm = Globals.serverState.realmManager.first();
                    if(realm.getDataCellManager() instanceof GriddedDataCellManager){
                        manager = (GriddedDataCellManager)realm.getDataCellManager();
                    }
                }

                if(manager != null){
                    if(manager.getLoadedCells() != null){
                        ImGui.text("Loaded Cells: " + manager.getLoadedCells().size());
                    }
                    if(manager.getCellPlayerlessFrameMap() != null){
                        ImGui.text("Playerless tracking map size: " + manager.getCellPlayerlessFrameMap().keySet().size());
                    }
                    ImGui.text("Cells cleaned last frame: " + manager.getNumCleaned());
                    if(ImGui.button("Player Data Cell Info")){
                        Entity playerEntity = Globals.serverState.playerManager.getFirstPlayer().getPlayerEntity();
                        Realm realm = Globals.serverState.realmManager.getEntityRealm(playerEntity);
                        GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)realm.getDataCellManager();
                        griddedDataCellManager.printCellInfo(griddedDataCellManager.getDataCellAtPoint(EntityUtils.getPosition(playerEntity)));
                    }
                }
                
            }
        });
        griddedManagerWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(griddedManagerWindow);
    }

}
