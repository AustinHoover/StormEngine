package electrosphere.client.ui.menu.debug.server;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import electrosphere.server.physics.terrain.generation.ProceduralChunkGenerator;
import electrosphere.server.physics.terrain.models.TerrainModel;
import imgui.ImGui;
import imgui.type.ImInt;

/**
 * Menu for altering parameters of the test terrain generator
 */
public class ImGuiTestGen {
    
    //window for viewing information about the ai state
    public static ImGuiWindow testGenWindow;

    /**
     * Client scene entity view
     */
    public static void createTestGenDebugWindow(){
        testGenWindow = new ImGuiWindow("Test Terrain Generation");
        int[] macroDataScaleInput = new int[1];
        int[] seedInput = new int[1];
        ImInt biome00 = new ImInt(0);
        ImInt biome10 = new ImInt(0);
        ImInt biome01 = new ImInt(0);
        ImInt biome11 = new ImInt(0);
        testGenWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //ui framework text
                ImGui.text("Test Terrain Generation");

                TerrainModel terrainModel = Globals.serverState.realmManager.first().getServerWorldData().getServerTerrainManager().getModel();

                //regenerate the test area
                if(ImGui.button("Regenerate")){
                    //recompile script engine
                    Globals.engineState.signalSystem.post(SignalType.SCRIPT_RECOMPILE, () -> {

                        //run once script recompilation has completed

                        //clear server
                        GriddedDataCellManager gridManager = (GriddedDataCellManager)Globals.serverState.realmManager.first().getDataCellManager();
                        gridManager.evictAll();

                        //clear client
                        Globals.clientState.clientDrawCellManager.evictAll();
                        Globals.clientState.clientTerrainManager.evictAll();
                    });
                }

                //set macro data scale in terrain model
                if(ImGui.sliderInt("Macro Data Scale", macroDataScaleInput, ProceduralChunkGenerator.GENERATOR_REALM_SIZE / terrainModel.getBiome().length, TerrainModel.DEFAULT_MACRO_DATA_SCALE)){
                    terrainModel.setMacroDataScale(macroDataScaleInput[0]);
                }

                //set macro data scale in terrain model
                if(ImGui.sliderInt("Seed", seedInput, 0, 100)){
                    terrainModel.setSeed(seedInput[0]);
                }

                //sets the (surface) biome[0][0] value
                if(ImGui.inputInt("biome[0][0]", biome00)){
                    terrainModel.getBiome()[0][0] = biome00.shortValue();
                }

                //sets the (surface) biome[1][0] value
                if(ImGui.inputInt("biome[1][0]", biome10)){
                    terrainModel.getBiome()[1][0] = biome10.shortValue();
                }

                //sets the (surface) biome[0][1] value
                if(ImGui.inputInt("biome[0][1]", biome01)){
                    terrainModel.getBiome()[0][1] = biome01.shortValue();
                }

                //sets the (surface) biome[1][1] value
                if(ImGui.inputInt("biome[1][1]", biome11)){
                    terrainModel.getBiome()[1][1] = biome11.shortValue();
                }

                //Toggles whether the client draws cell manager should update or not
                if(ImGui.button("Toggle ClientDrawCellManager updates " + (Globals.clientState.clientDrawCellManager.getShouldUpdate() ? "off" : "on"))){
                    Globals.clientState.clientDrawCellManager.setShouldUpdate(!Globals.clientState.clientDrawCellManager.getShouldUpdate());
                }
                
            }
        });
        testGenWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(testGenWindow);
    }

}
