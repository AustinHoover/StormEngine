package electrosphere.client.ui.menu.debug.perf;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import electrosphere.client.terrain.data.TerrainChunkDataPool;
import electrosphere.engine.Globals;
import electrosphere.mem.BlockChunkPool;
import electrosphere.renderer.ui.imgui.ImGuiLinePlot;
import electrosphere.renderer.ui.imgui.ImGuiLinePlot.ImGuiLinePlotDataset;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * Debug menu for memory profiling
 */
public class ImGuiMemory {

    /**
     * Size of a kilobyte
     */
    static final long KB = 1024;

    /**
     * Size of a megabyte
     */
    static final long MB = 1024 * 1024;

    /**
     * Size of a gigabyte
     */
    static final long GB = 1024 * 1024 * 1024;

    /**
     * Number of points on the graph
     */
    static final int GRAPH_POINT_COUNT = 50;

    //window for viewing information about the memory usage
    public static ImGuiWindow memoryWindow;

    /**
     * Memory window
     */
    public static void createMemoryDebugWindow(){

        //memory usage graph
        ImGuiLinePlot memoryGraph = new ImGuiLinePlot("Memory Usage",400,400);
        ImGuiLinePlotDataset memoryGraphDataset = new ImGuiLinePlotDataset("Memory Usage (mb)", GRAPH_POINT_COUNT);
        memoryGraphDataset.zeroOut();
        memoryGraph.addDataset(memoryGraphDataset);

        memoryWindow = new ImGuiWindow("Memory Usage");
        memoryWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {

                //get garbage collector name
                List<GarbageCollectorMXBean> gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
                if(gcMxBeans != null && gcMxBeans.size() > 0){
                    GarbageCollectorMXBean gcMxBean = gcMxBeans.get(0);
                    ImGui.text(gcMxBean.getName() + " - " + gcMxBean.getObjectName());
                }

                //get memory usage
                long totalMemory = Runtime.getRuntime().totalMemory();
                long freeMemory = Runtime.getRuntime().freeMemory();
                long memoryUsage = totalMemory - freeMemory;
                ImGui.text("Total Memory: " + formatMemory(totalMemory));
                ImGui.text("Free Memory: " + formatMemory(freeMemory));
                ImGui.text("Memory Usage: " + formatMemory(memoryUsage));
                if(ImGui.button("Manual free")){
                    System.gc();
                }

                if(ImGui.collapsingHeader("Object Pools")){
                    ImGui.text("Block Chunk Pool: " + BlockChunkPool.getPoolSize());
                    ImGui.text("Terrain Chunk Pool: " + TerrainChunkDataPool.getPoolSize());
                }

                //memory usage graph
                memoryGraphDataset.addPoint(memoryUsage);
                memoryGraph.draw();
                
            }
        });
        memoryWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(memoryWindow);
    }

    /**
     * Formats a memory value
     * @param memoryRaw The memory value
     * @return The formatted string
     */
    private static String formatMemory(long memoryRaw){
        if(memoryRaw < KB){
            return "" + memoryRaw;
        } else if(memoryRaw < MB){
            return (memoryRaw / KB) + "kb";
        } else {
            return (memoryRaw / MB) + "mb";
        }
    }
    
}
