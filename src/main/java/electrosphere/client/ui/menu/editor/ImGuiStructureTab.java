package electrosphere.client.ui.menu.editor;

import java.io.File;

import org.joml.Vector3d;

import electrosphere.client.block.ClientBlockSelection;
import electrosphere.client.block.solver.RoomSolver;
import electrosphere.client.interact.select.AreaSelection;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.block.fab.BlockFabMetadata;
import electrosphere.data.block.fab.RoomMetadata;
import electrosphere.data.block.fab.StructureMetadata;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.imgui.filediag.ImGuiFileDialogManager;
import imgui.ImGui;

/**
 * Tab for editing structures
 */
public class ImGuiStructureTab {
    
    /**
     * Draws the contents of the structure tab
     */
    protected static void draw(){
        if(Globals.clientState.clientLevelEditorData.getCurrentFab() == null){
            ImGui.text("No structure currently being edited");
            if(ImGui.button("Discover structure")){
                ClientBlockSelection.selectAllBlocks();
                AreaSelection area = Globals.cursorState.getAreaSelection();
                if(area != null){
                    BlockFab blockFab = ClientBlockSelection.convertSelectionToFab();
                    Globals.clientState.clientLevelEditorData.setCurrentFab(blockFab);
                    Globals.clientState.clientLevelEditorData.setCurrentFabOrigin(area.getRectStart());
                }
            }
        } else {
            BlockFab currentFab = Globals.clientState.clientLevelEditorData.getCurrentFab();
            if(ImGui.button("Create Structure Data")){
                StructureMetadata structureData = StructureMetadata.create(Globals.cursorState.getAreaSelection());
                currentFab.getFabMetadata().setStructureData(structureData);
            }
            if(currentFab.getFabMetadata().getStructureData() != null && ImGui.button("Calculate Rooms")){
                RoomSolver.computeRoomsFromSelection(Globals.cursorState.getAreaSelection(),currentFab.getFabMetadata().getStructureData());
            }
            if(ImGui.button("Save")){
                String defaultName = "struct";
                ImGuiFileDialogManager.open("Save Fab", defaultName, ".fab", (File target) -> {
                    currentFab.write(target);
                });
            }
        }
    }

    /**
     * Draws the details tab
     */
    protected static void drawDetails(){
        if(Globals.clientState.clientLevelEditorData.getCurrentFab() == null){
            ImGui.text("Select a fab to show details here");
        } else {
            BlockFab currentFab = Globals.clientState.clientLevelEditorData.getCurrentFab();
            ImGui.text("Origin: " + Globals.clientState.clientLevelEditorData.getCurrentFabOrigin());
            ImGui.text("Dimensions: " + currentFab.getDimensions());
            BlockFabMetadata fabMetadata = currentFab.getFabMetadata();
            if(fabMetadata.getAreas() != null){
                if(ImGui.collapsingHeader("Areas in fab: " +  fabMetadata.getAreas().size())){
                    int i = 0;
                    for(AreaSelection area : fabMetadata.getAreas()){
                        Vector3d dims = new Vector3d(area.getRectEnd()).sub(area.getRectStart());
                        ImGui.text("Area " + i + " dimensions " + dims);
                        i++;
                    }
                }
            } else {
                ImGui.text("Areas undefined in metadata");
            }
            if(fabMetadata.getStructureData() != null){
                StructureMetadata structureMetadata = fabMetadata.getStructureData();
                if(ImGui.collapsingHeader("Rooms in structure: " +  structureMetadata.getRooms().size())){
                    int i = 0;
                    ImGui.indent();
                    for(RoomMetadata room : structureMetadata.getRooms()){
                        if(ImGui.collapsingHeader("Room " + i)){
                            ImGui.indent();
                            ImGui.text("Entry points: " + room.getEntryPoints().size());
                            ImGui.text("Furniture slots: " + room.getFurnitureSlots().size());
                            ImGui.unindent();
                        }
                        i++;
                    }
                    ImGui.unindent();
                }
            } else {
                ImGui.text("Structure Data undefined in metadata");
            }
        }
    }

}
