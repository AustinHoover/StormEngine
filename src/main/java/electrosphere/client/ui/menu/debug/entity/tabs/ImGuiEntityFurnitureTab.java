package electrosphere.client.ui.menu.debug.entity.tabs;

import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.grident.GridAlignedData;
import electrosphere.entity.Entity;
import electrosphere.entity.types.common.CommonEntityUtils;
import imgui.ImGui;

/**
 * Furniture data
 */
public class ImGuiEntityFurnitureTab {

    /**
     * Minimum dimension for grid alignment
     */
    static final int GRID_ALIGN_MIN = 1;

    /**
     * Maximum dimension for grid alignment
     */
    static final int GRID_ALIGN_MAX = 10;

    /**
     * Holds width value
     */
    static int[] widthHolder = new int[1];

    /**
     * Holds height value
     */
    static int[] heightHolder = new int[1];

    /**
     * Holds length value
     */
    static int[] lengthHolder = new int[1];
    
    /**
     * Furniture data view
     */
    public static void drawFurnitureTab(boolean show, Entity detailViewEntity){
        if(detailViewEntity == null){
            return;
        }
        if(CommonEntityUtils.getCommonData(detailViewEntity) == null){
            return;
        }
        CommonEntityType type = CommonEntityUtils.getCommonData(detailViewEntity);
        if(show && ImGui.collapsingHeader("Furniture Data")){
            ImGui.indent();
            if(type.getGridAlignedData() != null){
                GridAlignedData gridAlignedData = type.getGridAlignedData();
                if(ImGui.sliderInt("Width",widthHolder,GRID_ALIGN_MIN,GRID_ALIGN_MAX)){
                    gridAlignedData.setWidth(widthHolder[0]);
                }
                if(ImGui.sliderInt("Height",heightHolder,GRID_ALIGN_MIN,GRID_ALIGN_MAX)){
                    gridAlignedData.setHeight(heightHolder[0]);
                }
                if(ImGui.sliderInt("Length",lengthHolder,GRID_ALIGN_MIN,GRID_ALIGN_MAX)){
                    gridAlignedData.setLength(lengthHolder[0]);
                }
            }
            ImGui.unindent();
        }
    }

}
