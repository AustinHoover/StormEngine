package electrosphere.client.ui.menu.debug.entity.tabs;

import electrosphere.client.terrain.foliage.FoliageModel;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.foliage.GrassData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.renderer.actor.instance.TextureInstancedActor;
import imgui.ImGui;

/**
 * Tab for foliage-related data
 */
public class ImGuiEntityFoliageTab {

    /**
     * Grass Color (tip)
     */
    static float[] grassColorTip = new float[3];

    /**
     * Grass Color (base)
     */
    static float[] grassColorBase = new float[3];

    /**
     * Grass Max Tip Curve
     */
    static float[] grassMaxTipCurve = new float[1];

    /**
     * Grass Min Height Scale
     */
    static float[] grassMinHeightScale = new float[1];

    /**
     * Grass Max Height Scale
     */
    static float[] grassMaxHeightScale = new float[1];

    /**
     * Client scene entity view
     */
    public static void drawFoliageView(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Foliage Data")){
            FoliageType foliageData = (FoliageType)CommonEntityUtils.getCommonData(detailViewEntity);
            ImGui.indent();
            if(detailViewEntity != null && foliageData != null){

                if(foliageData.getGrassData() != null && ImGui.collapsingHeader("Grass Data")){
                    GrassData grassData = foliageData.getGrassData();

                    if(ImGui.sliderFloat3("Color (Tip)", grassColorTip, 0, 1)){
                        grassData.getTipColor().set(grassColorTip[0],grassColorTip[1],grassColorTip[2]);
                        for(Entity foliageEntity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_FOLIAGE_PASS)){
                            if(TextureInstancedActor.getTextureInstancedActor(foliageEntity) != null){
                                TextureInstancedActor actor = TextureInstancedActor.getTextureInstancedActor(foliageEntity);
                                actor.setUniformOnMesh(FoliageModel.MESH_NAME, FoliageModel.UNIFORM_TIP_COLOR, grassData.getTipColor());
                            }
                        }
                    }

                    if(ImGui.sliderFloat3("Color (Base)", grassColorBase, 0, 1)){
                        grassData.getBaseColor().set(grassColorBase[0],grassColorBase[1],grassColorBase[2]);
                        for(Entity foliageEntity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_FOLIAGE_PASS)){
                            if(TextureInstancedActor.getTextureInstancedActor(foliageEntity) != null){
                                TextureInstancedActor actor = TextureInstancedActor.getTextureInstancedActor(foliageEntity);
                                actor.setUniformOnMesh(FoliageModel.MESH_NAME, FoliageModel.UNIFORM_BASE_COLOR, grassData.getBaseColor());
                            }
                        }
                    }

                    if(ImGui.sliderFloat("Max Tip Curve", grassMaxTipCurve, 0, 1)){
                        grassData.setMaxTipCurve(grassMaxTipCurve[0]);
                    }

                    if(ImGui.sliderFloat("Min Height Scale", grassMinHeightScale, 0.1f, 10)){
                        grassData.setMinHeight(grassMinHeightScale[0]);
                    }

                    if(ImGui.sliderFloat("Max Height Scale", grassMaxHeightScale, 0.1f, 10)){
                        grassData.setMaxHeight(grassMaxHeightScale[0]);
                    }

                    if(ImGui.button("Regenerate All Grass")){
                        Globals.clientState.foliageCellManager.evictAll();
                    }
                }
            }
            ImGui.unindent();
        }
    }
    
}
