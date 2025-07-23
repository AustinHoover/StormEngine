package electrosphere.client.ui.menu.debug.entity.tabs;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import imgui.ImGui;

/**
 * An entire tab of debug actions
 */
public class ImGuiEntityDebugActions {
    
    /**
     * Debug actions view
     */
    public static void drawDebugActions(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Debug Actions")){
            ImGui.indent();
            if(detailViewEntity != null){

                if(ImGui.button("Teleport to player")){
                    if(Globals.clientState.clientSceneWrapper.containsServerId(detailViewEntity.getId())){
                        ServerEntityUtils.repositionEntity(detailViewEntity, EntityUtils.getPosition(Globals.clientState.playerEntity));
                    }
                }

            }
            ImGui.unindent();
        }
    }

}
