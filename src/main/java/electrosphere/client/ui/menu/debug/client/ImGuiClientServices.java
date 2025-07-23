package electrosphere.client.ui.menu.debug.client;

import org.joml.Vector3i;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.interact.ClientInteractionEngine;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.client.terrain.cells.DrawCell;
import electrosphere.client.terrain.foliage.FoliageCell;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * Debug ui for draw cells
 */
public class ImGuiClientServices {
    
    /**
     * Window for viewing chunk status on server and client
     */
    public static ImGuiWindow clientServicesWindow;

    /**
     * Client scene entity view
     */
    public static void createClientServicesWindow(){
        clientServicesWindow = new ImGuiWindow("Draw Cells");
        clientServicesWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {

                if(ImGui.collapsingHeader("Draw Cells")){
                    if(ImGui.button("Debug DrawCell at camera position")){
                        Vector3i cameraWorldPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                        DrawCell cell = Globals.clientState.clientDrawCellManager.getDrawCell(cameraWorldPos.x, cameraWorldPos.y, cameraWorldPos.z);
                        LoggerInterface.loggerEngine.WARNING("" + cell);

                        LoggerInterface.loggerEngine.WARNING("Chunk topology:");
                        ChunkData data = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(cameraWorldPos, 1);
                        if(data != null){
                            for(int x = 0; x < ChunkData.CHUNK_DATA_SIZE; x++){
                                String line = "";
                                for(int z = 0; z < ChunkData.CHUNK_DATA_SIZE; z++){
                                    int height = 0;
                                    for(int y = 0; y < ChunkData.CHUNK_DATA_SIZE; y++){
                                        if(data.getType(x, y, z) != 0){
                                            height = y;
                                        }
                                    }
                                    line = line + String.format("%2d ",height);
                                }
                                LoggerInterface.loggerEngine.WARNING(line);
                            }
                            LoggerInterface.loggerEngine.WARNING("\n");
                        } else {
                            LoggerInterface.loggerEngine.WARNING("Chunk not in cache! " + cameraWorldPos);
                        }
                    }
                    if(ImGui.button("Print debug info for FoliageCell at camera position")){
                        Vector3i cameraWorldPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                        FoliageCell cell = Globals.clientState.foliageCellManager.getFoliageCell(cameraWorldPos.x, cameraWorldPos.y, cameraWorldPos.z);
                        LoggerInterface.loggerEngine.WARNING("" + cell);
                    }
                    if(ImGui.button("Debug FoliageCell evaluation at camera position")){
                        Vector3i cameraWorldPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                        FoliageCell cell = Globals.clientState.foliageCellManager.getFoliageCell(cameraWorldPos.x, cameraWorldPos.y, cameraWorldPos.z);
                        cell.setTripDebug(true);
                    }
                    if(ImGui.button("Request terrain at camera position")){
                        Vector3i cameraWorldPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                        Globals.clientState.clientTerrainManager.requestChunk(cameraWorldPos.x, cameraWorldPos.y, cameraWorldPos.z, 0);
                    }
                }


                if(ImGui.collapsingHeader("Interaction Engine")){
                    ImGui.text("Interactible count: " + ClientInteractionEngine.getInteractiblesCount());
                    ImGui.text("Collidables count: " + ClientInteractionEngine.getCollidablesCount());
                }
            }
        });
        clientServicesWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(clientServicesWindow);
    }

}
