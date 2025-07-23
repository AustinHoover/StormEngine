package electrosphere.client.ui.menu.debug.entity.tabs;

import java.util.LinkedList;
import java.util.List;

import org.ode4j.ode.DGeom;
import org.ode4j.ode.DSphere;

import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.entity.Entity;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState;
import imgui.ImGui;

/**
 * Tab for viewing and editing data about hitboxes
 */
public class ImGuiEntityHitboxTab {
    
    /**
     * Minimum offset value
     */
    static final float MIN_OFFSET = -10;

    /**
     * Maximum offset value
     */
    static final float MAX_OFFSET = 10;

    /**
     * Weights used for the slider
     */
    static float[] weights = new float[3];

    /**
     * Scale storage
     */
    static float[] scale = new float[1];

    /**
     * Hitbox data view
     */
    public static void drawHitboxTab(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Hitbox Data")){
            ImGui.indent();
            if(detailViewEntity != null && HitboxCollectionState.hasHitboxState(detailViewEntity)){
                HitboxCollectionState hitboxCollectionState = HitboxCollectionState.getHitboxState(detailViewEntity);
                
                int i = 0;
                for(HitboxState state : hitboxCollectionState.getHitboxes()){
                    HitboxData data = state.getHitboxData();
                    String name = "[" + i + "] ";
                    if(state.getBoneName() != null){
                        name = name + " " + state.getBoneName();
                    }
                    if(ImGui.collapsingHeader(name)){
                        if(ImGui.sliderFloat3("Offset", weights, MIN_OFFSET, MAX_OFFSET)){
                            List<Double> values = new LinkedList<Double>();
                            values.add((double)weights[0]);
                            values.add((double)weights[1]);
                            values.add((double)weights[2]);
                            data.setOffset(values);
                        }
                        if(ImGui.sliderFloat("Scale", scale, MIN_OFFSET, MAX_OFFSET)){
                            data.setRadius(scale[0]);
                            DGeom geom = hitboxCollectionState.getGeom(state);
                            if(geom instanceof DSphere){
                                DSphere sphere = (DSphere)geom;
                                sphere.setRadius(scale[0]);
                            }
                        }
                    }
                    i++;
                }

            }
            ImGui.unindent();
        }
    }

}
