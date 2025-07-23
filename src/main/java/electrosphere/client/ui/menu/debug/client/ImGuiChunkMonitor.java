package electrosphere.client.ui.menu.debug.client;

import org.joml.Vector3i;

import electrosphere.engine.Globals;
import electrosphere.entity.EntityUtils;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

public class ImGuiChunkMonitor {

    /**
     * Num datapoints
     */
    public static final int PRESSURE_GRAPH_POINT_COUNT = 100;

    /**
     * Window for viewing chunk status on server and client
     */
    public static ImGuiWindow chunkMonitorWindow;

    /**
     * Client scene entity view
     */
    public static void createChunkMonitorWindow(){
        chunkMonitorWindow = new ImGuiWindow("Chunk Monitor");

        chunkMonitorWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //ui framework text
                ImGui.text("Chunk Monitor");

                if(Globals.clientState.clientDrawCellManager != null){
                    Globals.clientState.clientDrawCellManager.updateStatus();
                    ImGui.text("Full res chunks: " + Globals.clientState.clientDrawCellManager.getMaxResCount());
                    ImGui.text("Terrain node count: " + Globals.clientState.clientDrawCellManager.getNodeCount());
                }
                if(Globals.clientState.clientBlockCellManager != null){
                    ImGui.text("Block node count: " + Globals.clientState.clientBlockCellManager.getNodeCount());
                }
                if(Globals.clientState.foliageCellManager != null){
                    ImGui.text("Foliage node count: " + Globals.clientState.foliageCellManager.getNodeCount());
                }
                if(ImGui.button("Break at chunk")){
                    if(Globals.clientState.foliageCellManager != null){
                        Vector3i absVoxelPos = Globals.clientState.clientWorldData.convertRealToAbsoluteVoxelSpace(EntityUtils.getPosition(Globals.clientState.playerEntity));
                        Globals.clientState.foliageCellManager.addBreakPoint(absVoxelPos);
                    }
                }
            }
        });
        chunkMonitorWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(chunkMonitorWindow);
    }
}
