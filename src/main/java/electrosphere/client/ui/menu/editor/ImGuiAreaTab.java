package electrosphere.client.ui.menu.editor;

import electrosphere.client.block.ClientBlockSelection;
import imgui.ImGui;

/**
 * Class for drawing the area tab of the editor window
 */
public class ImGuiAreaTab {
    
    /**
     * Draws the area tab
     */
    protected static void draw(){
        if(ImGui.button("Select all voxels")){
            ClientBlockSelection.selectAllBlocks();
        }
        if(ImGui.button("Export Selected Blocks")){
            ClientBlockSelection.exportSelection();
        }
        if(ImGui.button("Add Area Selection As Room")){
            ClientBlockSelection.exportSelection();
        }
    }

}
