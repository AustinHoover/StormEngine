package electrosphere.client.ui.menu.debug;

import java.util.HashMap;
import java.util.Map;

import electrosphere.client.ui.menu.debug.audio.ImGuiAudio;
import electrosphere.client.ui.menu.debug.client.ImGuiChunkMonitor;
import electrosphere.client.ui.menu.debug.client.ImGuiClientServices;
import electrosphere.client.ui.menu.debug.client.ImGuiControls;
import electrosphere.client.ui.menu.debug.engine.ImGuiLogger;
import electrosphere.client.ui.menu.debug.entity.ImGuiEntityMacros;
import electrosphere.client.ui.menu.debug.perf.ImGuiMemory;
import electrosphere.client.ui.menu.debug.perf.ImGuiNetworkMonitor;
import electrosphere.client.ui.menu.debug.render.ImGuiRenderer;
import electrosphere.client.ui.menu.debug.render.ImGuiUIFramework;
import electrosphere.client.ui.menu.debug.scene.ImGuiSceneWindow;
import electrosphere.client.ui.menu.debug.server.ImGuiAI;
import electrosphere.client.ui.menu.debug.server.ImGuiFluidMonitor;
import electrosphere.client.ui.menu.debug.server.ImGuiGriddedManager;
import electrosphere.client.ui.menu.debug.server.ImGuiTestGen;
import electrosphere.client.ui.menu.editor.ImGuiEditorWindows;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.renderer.ui.imgui.ImGuiLinePlot;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiLinePlot.ImGuiLinePlotDataset;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.physics.terrain.generation.ProceduralChunkGenerator;
import imgui.ImGui;

/**
 * Various methods for creating specific imgui windows in engine
 */
public class ImGuiWindowMacros {
    

    //main debug menu
    private static ImGuiWindow mainDebugWindow;

    //tracks if the debug menu is open
    private static boolean debugIsOpen = false;


    //Framerate graph
    private static ImGuiWindow globalFrametimeWindow;
    private static ImGuiLinePlot globalFrametimePlot;
    private static Map<String,ImGuiLinePlotDataset> globalFrametimeDatasets;

    /**
     * Initializes imgui windows
     */
    public static void initImGuiWindows(){
        ImGuiWindowMacros.createMainDebugMenu();
        ImGuiWindowMacros.createFramerateGraph();
        ImGuiPlayerEntity.createPlayerEntityDebugWindow();
        ImGuiFluidMonitor.createFluidDebugWindow();
        ImGuiEntityMacros.createClientEntityWindows();
        ImGuiUIFramework.createUIFrameworkDebugWindow();
        ImGuiControls.createControlsDebugWindow();
        ImGuiAI.createAIDebugWindow();
        ImGuiAudio.createAudioDebugMenu();
        ImGuiLogger.createLoggersWindow();
        ImGuiRenderer.createRendererWindow();
        ImGuiTestGen.createTestGenDebugWindow();
        ImGuiChunkMonitor.createChunkMonitorWindow();
        ImGuiNetworkMonitor.createNetworkMonitorWindow();
        ImGuiGriddedManager.createGriddedManagerWindow();
        ImGuiMemory.createMemoryDebugWindow();
        ImGuiEditorWindows.initEditorWindows();
        ImGuiClientServices.createClientServicesWindow();
        ImGuiSceneWindow.createChunkMonitorWindow();
    }

    /**
     * Creates a framerate graph
     */
    private static void createFramerateGraph(){
        globalFrametimeWindow = new ImGuiWindow("Frametime Graph");
        globalFrametimePlot = new ImGuiLinePlot("Frametime plot");
        globalFrametimeDatasets = new HashMap<String,ImGuiLinePlotDataset>();
        ImGuiWindowMacros.initFramerateGraphSeries("totalframerate");
        ImGuiWindowMacros.initFramerateGraphSeries("simframes");
        ImGuiWindowMacros.initFramerateGraphSeries("render");
        ImGuiWindowMacros.initFramerateGraphSeries("assetLoad");
        ImGuiWindowMacros.initFramerateGraphSeries("clientNetwork");
        globalFrametimeWindow.addElement(globalFrametimePlot);
        globalFrametimeWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(globalFrametimeWindow);
    }

    /**
     * Gets the main debug window
     * @return the main debug window
     */
    public static ImGuiWindow getMainDebugWindow(){
        return mainDebugWindow;
    }

    /**
     * Inits a series for the framerate graph
     * @param seriesName The name of the series
     */
    private static void initFramerateGraphSeries(String seriesName){
        ImGuiLinePlotDataset dataSet = new ImGuiLinePlotDataset(seriesName, 50);
        globalFrametimeDatasets.put(seriesName,dataSet);
        for(int x = 0; x < 50; x++){
            dataSet.addPoint(x, 0);
        }
        globalFrametimePlot.addDataset(dataSet);
    }

    /**
     * Adds a datapoint to the framerate graph
     * @param seriesName The name of the series to add a datapoint for
     * @param y the y coord
     */
    public static void addGlobalFramerateDatapoint(String seriesName, double y){
        if(globalFrametimeDatasets != null && globalFrametimeDatasets.containsKey(seriesName)){
            globalFrametimeDatasets.get(seriesName).addPoint(y);
        }
    }


    /**
     * Inits the main debug menu
     */
    private static void createMainDebugMenu(){
        mainDebugWindow = new ImGuiWindow("Debug");
        mainDebugWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //show global framerate line graph
                if(ImGui.button("Show Overall Frametime")){
                    globalFrametimeWindow.setOpen(true);
                }
                //show audio debug
                if(ImGui.button("Show Audio Debug Menu")){
                    ImGuiAudio.audioDebugMenu.setOpen(true);
                }
                //show audio debug
                if(ImGui.button("Show Player Entity Debug Menu")){
                    ImGuiPlayerEntity.playerEntityWindow.setOpen(true);
                }
                //show fluids debug
                if(ImGui.button("Show Fluids Debug Menu")){
                    ImGuiFluidMonitor.fluidWindow.setOpen(true);
                }
                //client entity debug
                if(ImGui.button("Client Entity Debug")){
                    ImGuiEntityMacros.clientEntityListWindow.setOpen(true);
                }
                if(ImGui.button("Client Scene")){
                    ImGuiSceneWindow.viewScene(Globals.clientState.clientScene, Globals.clientState.clientSceneWrapper.getCollisionEngine());
                }
                //controls state debug
                if(ImGui.button("Control State Debug")){
                    ImGuiControls.controlsWindow.setOpen(true);
                }
                //controls state debug
                if(ImGui.button("AI State Debug")){
                    ImGuiAI.aiWindow.setOpen(true);
                }
                //logger state control
                if(ImGui.button("Loggers")){
                    ImGuiLogger.loggersWindow.setOpen(true);
                }
                //logger state control
                if(ImGui.button("Renderers")){
                    ImGuiRenderer.rendererWindow.setOpen(true);
                }
                //logger state control
                if(ImGui.button("UI")){
                    ImGuiUIFramework.uiFrameworkWindow.setOpen(true);
                }
                //test gen window (only drawn if realm is a test generation realm)
                if(
                    Globals.serverState.realmManager != null &&
                    Globals.serverState.realmManager.first() != null &&
                    Globals.serverState.realmManager.first().getServerWorldData() != null &&
                    Globals.serverState.realmManager.first().getServerWorldData().getServerTerrainManager().getChunkGenerator() instanceof ProceduralChunkGenerator &&
                    ImGui.button("Test Terrain Gen")
                ){
                    ImGuiTestGen.testGenWindow.setOpen(true);
                }
                //chunk monitor
                if(ImGui.button("Chunk Monitor")){
                    ImGuiChunkMonitor.chunkMonitorWindow.setOpen(true);
                }
                //gridded data cell monitor
                if(ImGui.button("Gridded Data Cell Monitor")){
                    ImGuiGriddedManager.griddedManagerWindow.setOpen(true);
                }
                if(ImGui.button("Server Scene")){
                    ImGuiSceneWindow.viewScene(null, Globals.serverState.realmManager.first().getCollisionEngine());
                }
                //memory usage
                if(ImGui.button("Memory Usage")){
                    ImGuiMemory.memoryWindow.setOpen(!ImGuiMemory.memoryWindow.isOpen());
                }
                if(ImGui.button("Network Monitor")){
                    ImGuiNetworkMonitor.netMonitorWindow.setOpen(true);
                }
                if(ImGui.button("Client Services")){
                    ImGuiClientServices.clientServicesWindow.setOpen(true);
                }
                if(ImGui.button("Enable Profiler")){
                    Main.setEnableProfiler();
                }
                //close button
                if(ImGui.button("Close")){
                    mainDebugWindow.setOpen(false);
                }
            }
        });
        mainDebugWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(mainDebugWindow);
    }

    /**
     * Toggles the open state of the menu
     */
    public static void toggleMainDebugMenu(){
        mainDebugWindow.setOpen(!mainDebugWindow.isOpen());
        ImGuiWindowMacros.debugIsOpen = mainDebugWindow.isOpen();
        if(mainDebugWindow.isOpen()){
            Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
        } else {
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
        }
    }

    /**
     * Makes sure the main debug menu properly toggles controls if it is closed with the X button
     */
    public static void synchronizeMainDebugMenuVisibility(){
        if(ImGuiWindowMacros.debugIsOpen && !mainDebugWindow.isOpen()){
            ImGuiWindowMacros.debugIsOpen = false;
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
        }
    }

}
