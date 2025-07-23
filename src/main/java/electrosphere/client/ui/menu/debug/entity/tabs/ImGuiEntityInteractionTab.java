package electrosphere.client.ui.menu.debug.entity.tabs;

import org.ode4j.ode.DBody;

import electrosphere.client.interact.ClientInteractionEngine;
import electrosphere.client.ui.components.imgui.CollidableEditBlock;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.entity.Entity;
import imgui.ImGui;

/**
 * Tab for interaction engine data
 */
public class ImGuiEntityInteractionTab {
    
    /**
     * Interaction view
     */
    public static void drawInteractionTab(boolean show, Entity detailViewEntity){
        if(detailViewEntity == null){
            return;
        }
        if(show && ImGui.collapsingHeader("Interaction Data")){
            ImGui.indent();
            if(ClientInteractionEngine.getInteractionBody(detailViewEntity) != null){
                DBody body = ClientInteractionEngine.getInteractionBody(detailViewEntity);
                CollidableTemplate template = ClientInteractionEngine.getInteractionTemplate(detailViewEntity);
                CollidableEditBlock.drawCollidableEdit(body, template);
            }
            ImGui.unindent();
        }
    }

}
