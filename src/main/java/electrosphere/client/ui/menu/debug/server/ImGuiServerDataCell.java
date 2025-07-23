package electrosphere.client.ui.menu.debug.server;

import electrosphere.engine.Globals;
import electrosphere.entity.EntityTags;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.datacell.ServerDataCell;
import imgui.ImGui;

/**
 * Window for viewing data about a server data cell
 */
public class ImGuiServerDataCell {
    
    /**
     * The current cell to view information about
     */
    private static ServerDataCell currentCell;

    /**
     * window for viewing information about the server data cell
     */
    protected static ImGuiWindow serverDataCellWindow;

    /**
     * Client scene entity view
     */
    protected static void createServerDataCellWindow(){
        serverDataCellWindow = new ImGuiWindow("Server Data Cell Details");
        serverDataCellWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                if(currentCell == null){
                    ImGui.text("Current cell is null!");
                } else {
                    ImGui.text("Total entities: " + currentCell.getScene().getEntityList().size());
                    ImGui.text("Creatures: " + currentCell.getScene().getEntitiesWithTag(EntityTags.CREATURE));
                    ImGui.text("Ready: " + currentCell.isReady());
                }
            }
        });
        serverDataCellWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(serverDataCellWindow);
    }

    /**
     * Opens the window to view a specific server data cell
     * @param serverDataCell The cell
     */
    public static void viewCell(ServerDataCell serverDataCell){
        if(serverDataCellWindow == null){
            ImGuiServerDataCell.createServerDataCellWindow();
        }
        ImGuiServerDataCell.currentCell = serverDataCell;
        serverDataCellWindow.setOpen(true);
    }

}
