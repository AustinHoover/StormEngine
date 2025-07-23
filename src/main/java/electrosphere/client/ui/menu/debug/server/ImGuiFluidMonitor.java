package electrosphere.client.ui.menu.debug.server;

import electrosphere.client.fluid.cells.FluidCellManager;
import electrosphere.client.fluid.manager.ClientFluidManager;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.fluid.manager.ServerFluidManager;
import imgui.ImGui;

/**
 * Monitors the fluid system on server/client
 */
public class ImGuiFluidMonitor {

    /**
     * Window for viewing chunk status on server and client
     */
    public static ImGuiWindow fluidWindow;

    /**
     * Client scene entity view
     */
    public static void createFluidDebugWindow(){
        fluidWindow = new ImGuiWindow("Fluids");
        fluidWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                if(ImGui.collapsingHeader("Server Data")){
                    ServerFluidManager fluidManager = Globals.serverState.playerManager.getPlayerRealm(Globals.clientState.clientPlayer).getServerWorldData().getServerFluidManager();
                    //server engine details
                    ImGui.text("Fluids Debug");
                    ImGui.text("State: " + (fluidManager.getSimulate() ? "on" : "off"));
                    if(ImGui.button("Toggle Simulation")){
                        fluidManager.setSimulate(!fluidManager.getSimulate());
                    }
                    ImGui.text("Broadcast Size (This Frame): " + fluidManager.getBroadcastSize());
                    ImGui.text("Active Chunk Count (This Frame): " + fluidManager.getActiveChunkCount());
                    ImGui.text("Normalization Ratio: " + ServerFluidChunk.getNormalizationRatio());
                    ImGui.text("Mass: " + ServerFluidChunk.getMassCount());


                    if(fluidManager.getChunk(0, 0, 1) != null){
                        ImGuiFluidMonitor.printChunkDebugData();
                    }

                }
                if(ImGui.collapsingHeader("Client Data")){

                    FluidCellManager fluidCellManager = Globals.clientState.fluidCellManager;
                    ImGui.text("FluidCellManager Data");
                    ImGui.text("Undrawable size: " + fluidCellManager.getUndrawableSize());
                    ImGui.text("Unrequested size: " + fluidCellManager.getUnrequestedSize());

                    ClientFluidManager clientFluidManager = Globals.clientState.clientFluidManager;
                    ImGui.text("ClientFluidManager Data");
                    ImGui.text("Message Count (This Frame): " + clientFluidManager.getMessageCount());
                }
            }
        });
        fluidWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(fluidWindow);
    }

    /**
     * Prints debug data about the chunk at 0,0,1
     */
    private static void printChunkDebugData(){
        ServerFluidManager fluidManager = Globals.serverState.playerManager.getPlayerRealm(Globals.clientState.clientPlayer).getServerWorldData().getServerFluidManager();
        ServerFluidChunk chunk = fluidManager.getChunk(0, 0, 1);
        ImGui.text("Pressure: " + chunk.getTotalPressure());
        ImGui.text("Velocity magnitude: " + chunk.getTotalVelocityMag());
        if(ImGui.button("Capture lowest layers")){
            //print density

            System.out.println("Density");
            for(int y = 0; y < 3; y++){
                System.out.println("Layer " + y);
                for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        System.out.print(chunk.getWeight(x, y, z) + ", ");
                    }
                    System.out.println();
                }
            }
            System.out.println("\n\n\n");

            System.out.println("X Velocity");
            for(int y = 0; y < 3; y++){
                System.out.println("Layer " + y);
                for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        System.out.print(chunk.getVelocityX(x, y, z) + ", ");
                    }
                    System.out.println();
                }
            }
            System.out.println("\n\n\n");

            System.out.println("Y Velocity");
            for(int y = 0; y < 3; y++){
                System.out.println("Layer " + y);
                for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        System.out.print(chunk.getVelocityY(x, y, z) + ", ");
                    }
                    System.out.println();
                }
            }
            System.out.println("\n\n\n");

            System.out.println("Z Velocity");
            for(int y = 0; y < 3; y++){
                System.out.println("Layer " + y);
                for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        System.out.print(chunk.getVelocityZ(x, y, z) + ", ");
                    }
                    System.out.println();
                }
            }
            System.out.println("\n\n\n");


            System.out.println("Divergence");
            for(int y = 0; y < 3; y++){
                System.out.println("Layer " + y);
                for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        System.out.print(chunk.getDivergence(x, y, z) + ", ");
                    }
                    System.out.println();
                }
            }
            System.out.println("\n\n\n");


            System.out.println("Pressure");
            for(int y = 0; y < 3; y++){
                System.out.println("Layer " + y);
                for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        System.out.print(chunk.getPressure(x, y, z) + ", ");
                    }
                    System.out.println();
                }
            }
            System.out.println("\n\n\n");
        }
    }

}
