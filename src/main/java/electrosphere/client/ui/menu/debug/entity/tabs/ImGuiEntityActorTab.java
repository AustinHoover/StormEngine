package electrosphere.client.ui.menu.debug.entity.tabs;

import java.util.Set;
import java.util.Random;

import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.foliage.ProceduralTreeBranchModel;
import electrosphere.data.entity.foliage.TreeModel;
import electrosphere.data.entity.graphics.ProceduralModel;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.tree.ProceduralTree;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.instance.InstancedActor;
import electrosphere.renderer.actor.mask.ActorAnimationMaskEntry;
import electrosphere.renderer.actor.mask.ActorMeshMask;
import electrosphere.renderer.anim.AnimChannel;
import electrosphere.renderer.anim.Animation;
import electrosphere.renderer.model.Bone;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import imgui.ImGui;

/**
 * The actor tab on the entity detail view
 */
public class ImGuiEntityActorTab {
    
    /**
     * Client scene entity view
     */
    public static void drawActorView(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Actor Details")){
            ImGui.indent();
            if(detailViewEntity != null && EntityUtils.getActor(detailViewEntity) != null){
                Actor actor = EntityUtils.getActor(detailViewEntity);
                CommonEntityType commonEntityData = CommonEntityUtils.getCommonData(detailViewEntity);

                //procedural model
                if(commonEntityData != null && commonEntityData.getGraphicsTemplate().getProceduralModel() != null && ImGui.collapsingHeader("Procedural Model")){
                    ImGui.text("Procedural Model path: " + commonEntityData.getGraphicsTemplate().getProceduralModel());
                    if(ImGui.button("Regenerate")){
                        if(commonEntityData.getGraphicsTemplate().getProceduralModel().getTreeModel() != null){
                            ProceduralTree.setProceduralActor(
                                detailViewEntity,
                                commonEntityData.getGraphicsTemplate().getProceduralModel().getTreeModel(),
                                new Random()
                            );
                        }
                    }
                } else {
                    ImGui.text("Model path: " + actor.getBaseModelPath());
                    Model loadedModel = Globals.assetManager.fetchModel(actor.getBaseModelPath());
                    ImGui.text("Model is loaded: " + (loadedModel != null));
                }
    
                //mesh mask
                if(ImGui.collapsingHeader("Mesh Mask")){
                    ActorMeshMask meshMask = actor.getMeshMask();
                    ImGui.text("To Draw Meshes:");
                    for(Mesh mesh : meshMask.getToDrawMeshes()){
                        ImGui.text(mesh.getMeshName());
                    }
                    ImGui.text("Blocked Meshes:");
                    for(String blocked : meshMask.getBlockedMeshes()){
                        ImGui.text(blocked);
                    }
                }

                //animation queue
                if(ImGui.collapsingHeader("Animation Queue")){
                    Set<ActorAnimationMaskEntry> animationQueue = actor.getAnimationData().getAnimationQueue();
                    for(ActorAnimationMaskEntry mask : animationQueue){
                        ImGui.text(mask.getAnimationName() + " -  " + mask.getPriority());
                        ImGui.text(mask.getDuration() + " " + mask.getTime());
                    }
                }

                //bone values
                if(ImGui.collapsingHeader("Bone Values")){
                    for(Bone bone : actor.getAnimationData().getBoneValues()){
                        ImGui.text(bone.boneID);
                        ImGui.text("Position: " + actor.getAnimationData().getBonePosition(bone.boneID));
                        ImGui.text("Rotation: " + actor.getAnimationData().getBoneRotation(bone.boneID));
                        ImGui.text(bone.getFinalTransform() + "");
                    }
                }

                //Draws all the bones in the world
                if(ImGui.button("Draw Bones")){
                    Globals.renderingEngine.getDebugContentPipeline().getDebugBonesPipeline().setEntity(detailViewEntity);
                }
    
                //Browsable list of all animations with their data
                if(ImGui.collapsingHeader("Animation Channel Data")){
                    Model model = Globals.assetManager.fetchModel(actor.getBaseModelPath());
                    ImGui.indent();
                    for(Animation animation : model.getAnimations()){
                        if(ImGui.collapsingHeader(animation.name)){
                            if(ImGui.button("Play")){
                                actor.getAnimationData().playAnimation(animation.name, AnimationPriorities.getValue(AnimationPriorities.MODIFIER_MAX));
                            }
                            for(AnimChannel channel : animation.channels){
                                ImGui.pushID(channel.getNodeID());
                                if(ImGui.button("Fully describe")){
                                    channel.fullDescribeChannel();
                                }
                                ImGui.text("=" + channel.getNodeID() + "=");
                                ImGui.text("" + channel.getCurrentPosition());
                                ImGui.text("" + channel.getCurrentRotation());
                                ImGui.text("" + channel.getCurrentScale());
                                ImGui.popID();
                            }
                        }
                    }
                    ImGui.unindent();
                }

                //print static draw status
                if(ImGui.collapsingHeader("Static Draw Status")){
                    ImGui.textWrapped(actor.getStatisDrawStatus());
                }

                //print data macros
                if(ImGui.collapsingHeader("Print Data")){
                    //print bone values
                    if(ImGui.button("Print current bone values")){
                        for(Bone bone : actor.getAnimationData().getBoneValues()){
                            LoggerInterface.loggerRenderer.DEBUG(bone.boneID);
                            LoggerInterface.loggerRenderer.DEBUG("" + bone.getFinalTransform());
                        }
                    }
        
                    //print animation keys
                    if(ImGui.button("Print animation keys")){
                        Model model = Globals.assetManager.fetchModel(actor.getBaseModelPath());
                        model.describeAllAnimations();
                    }
                }
            }
            ImGui.unindent();
        }
    }

    /**
     * Client scene entity view
     */
    public static void drawInstancedActorView(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Instanced Actor Details")){
            ImGui.indent();
            float[] limbScalarFalloffFactor = new float[1];
            float[] minimumLimbScalar = new float[1];
            float[] maximumLimbDispersion = new float[1];
            float[] minimumLimbDispersion = new float[1];
            int[] minimumNumberForks = new int[1];
            int[] maximumNumberForks = new int[1];
            int[] maximumTrunkSegments = new int[1];
            int[] maximumBranchSegments = new int[1];
            float[] maxBranchSegmentFalloffFactor = new float[1];
            int[] minimumSegmentToSpawnLeaves = new int[1];
            float[] minBranchHeightToStartSpawningLeaves = new float[1];
            float[] maxBranchHeightToStartSpawningLeaves = new float[1];
            float[] leafIncrement = new float[1];
            int[] minLeavesToSpawnPerPoint = new int[1];
            int[] maxLeavesToSpawnPerPoint = new int[1];
            float[] leafDistanceFromCenter = new float[1];
            float[] peelVariance = new float[1];
            float[] peelMinimum = new float[1];
            float[] swaySigmoidFactor = new float[1];
            int[] minimumSwayTime = new int[1];
            int[] swayTimeVariance = new int[1];
            float[] yawVariance = new float[1];
            float[] yawMinimum = new float[1];
            float[] minimumScalarToGenerateSwayTree = new float[1];
            float[] maximumScalarToGenerateSwayTree = new float[1];
            if(detailViewEntity != null && InstancedActor.getInstancedActor(detailViewEntity) != null){
                // InstancedActor actor = InstancedActor.getInstancedActor(detailViewEntity);
                CommonEntityType commonEntityData = CommonEntityUtils.getCommonData(detailViewEntity);

                //procedural model
                if(commonEntityData.getGraphicsTemplate().getProceduralModel() != null && ImGui.collapsingHeader("Procedural Model")){
                    ProceduralModel proceduralModel = commonEntityData.getGraphicsTemplate().getProceduralModel();
                    if(ImGui.button("Regenerate")){
                        ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                    }
                    if(proceduralModel.getTreeModel() != null && ImGui.collapsingHeader("Tree Model")){
                        TreeModel treeModel = proceduralModel.getTreeModel();


                        if(ImGui.collapsingHeader("Trunk")){
                            maximumTrunkSegments[0] = treeModel.getTrunkModel().getMaximumTrunkSegments();
                            if(ImGui.sliderInt("maximumTrunkSegments", maximumTrunkSegments, 1, 8)){
                                treeModel.getTrunkModel().setMaximumTrunkSegments(maximumTrunkSegments[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }
                        }


                        if(ImGui.collapsingHeader("Branches")){
                            ProceduralTreeBranchModel branchModel = treeModel.getBranchModel();
                            limbScalarFalloffFactor[0] = branchModel.getLimbScalarFalloffFactor();
                            if(ImGui.sliderFloat("limbScalarFalloffFactor", limbScalarFalloffFactor, 0.01f, 1f)){
                                branchModel.setLimbScalarFalloffFactor(limbScalarFalloffFactor[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }
                            
                            minimumLimbScalar[0] = branchModel.getMinimumLimbScalar();
                            if(ImGui.sliderFloat("minimumLimbScalar", minimumLimbScalar, 0.01f, 1f)){
                                branchModel.setLimbScalarFalloffFactor(minimumLimbScalar[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            maximumLimbDispersion[0] = branchModel.getMaximumLimbDispersion();
                            if(ImGui.sliderFloat("maximumLimbDispersion", maximumLimbDispersion, 0.01f, 1f)){
                                branchModel.setMaximumLimbDispersion(maximumLimbDispersion[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            minimumLimbDispersion[0] = branchModel.getMinimumLimbDispersion();
                            if(ImGui.sliderFloat("minimumLimbDispersion", minimumLimbDispersion, 0.01f, 1f)){
                                branchModel.setMinimumLimbDispersion(minimumLimbDispersion[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            minimumNumberForks[0] = branchModel.getMinimumNumberForks();
                            if(ImGui.sliderInt("minimumNumberForks", minimumNumberForks, 1, 8)){
                                branchModel.setMinimumNumberForks(minimumNumberForks[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            maximumNumberForks[0] = branchModel.getMaximumNumberForks();
                            if(ImGui.sliderInt("maximumNumberForks", maximumNumberForks, 1, 8)){
                                branchModel.setMaximumNumberForks(maximumNumberForks[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            maximumBranchSegments[0] = branchModel.getMaximumBranchSegments();
                            if(ImGui.sliderInt("maximumBranchSegments", maximumBranchSegments, 1, 8)){
                                branchModel.setMaximumBranchSegments(maximumBranchSegments[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            maxBranchSegmentFalloffFactor[0] = branchModel.getMaxBranchSegmentFalloffFactor();
                            if(ImGui.sliderFloat("maxBranchSegmentFalloffFactor", maxBranchSegmentFalloffFactor, 0.01f, 1f)){
                                branchModel.setMaxBranchSegmentFalloffFactor(maxBranchSegmentFalloffFactor[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }
                        }

                        if(ImGui.collapsingHeader("Branch Sway")){
                            peelVariance[0] = (float)treeModel.getPeelVariance();
                            if(ImGui.sliderFloat("peelVariance", peelVariance, 0.01f, 3f)){
                                treeModel.setPeelVariance(peelVariance[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            peelMinimum[0] = (float)treeModel.getPeelMinimum();
                            if(ImGui.sliderFloat("peelMinimum", peelMinimum, 0.01f, 3f)){
                                treeModel.setPeelMinimum(peelMinimum[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            swaySigmoidFactor[0] = (float)treeModel.getSwaySigmoidFactor();
                            if(ImGui.sliderFloat("swaySigmoidFactor", swaySigmoidFactor, 0.01f, 3f)){
                                treeModel.setSwaySigmoidFactor(swaySigmoidFactor[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            minimumSwayTime[0] = treeModel.getMinimumSwayTime();
                            if(ImGui.sliderInt("minimumSwayTime", minimumSwayTime, 1, 1000)){
                                treeModel.setMinimumSwayTime(minimumSwayTime[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            swayTimeVariance[0] = treeModel.getSwayTimeVariance();
                            if(ImGui.sliderInt("swayTimeVariance", swayTimeVariance, 1, 1000)){
                                treeModel.setSwayTimeVariance(swayTimeVariance[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            yawVariance[0] = (float)treeModel.getYawVariance();
                            if(ImGui.sliderFloat("yawVariance", yawVariance, 0.01f, 3f)){
                                treeModel.setYawVariance(yawVariance[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            yawMinimum[0] = (float)treeModel.getYawMinimum();
                            if(ImGui.sliderFloat("yawMinimum", yawMinimum, 0.01f, 3f)){
                                treeModel.setYawMinimum(yawMinimum[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            minimumScalarToGenerateSwayTree[0] = (float)treeModel.getMinimumScalarToGenerateSwayTree();
                            if(ImGui.sliderFloat("minimumScalarToGenerateSwayTree", minimumScalarToGenerateSwayTree, 0.01f, 3f)){
                                treeModel.setMinimumScalarToGenerateSwayTree(minimumScalarToGenerateSwayTree[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            maximumScalarToGenerateSwayTree[0] = (float)treeModel.getMaximumScalarToGenerateSwayTree();
                            if(ImGui.sliderFloat("maximumScalarToGenerateSwayTree", maximumScalarToGenerateSwayTree, 0.01f, 3f)){
                                treeModel.setMaximumScalarToGenerateSwayTree(maximumScalarToGenerateSwayTree[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }
                        }



                        if(ImGui.collapsingHeader("Leaves")){
                            ProceduralTreeBranchModel branchModel = treeModel.getBranchModel();
                            
                            minimumSegmentToSpawnLeaves[0] = branchModel.getMinimumSegmentToSpawnLeaves();
                            if(ImGui.sliderInt("minimumSegmentToSpawnLeaves", minimumSegmentToSpawnLeaves, 1, 8)){
                                branchModel.setMinimumSegmentToSpawnLeaves(minimumSegmentToSpawnLeaves[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }


                            minBranchHeightToStartSpawningLeaves[0] = treeModel.getMinBranchHeightToStartSpawningLeaves();
                            if(ImGui.sliderFloat("minBranchHeightToStartSpawningLeaves", minBranchHeightToStartSpawningLeaves, 0.01f, 5f)){
                                treeModel.setMinBranchHeightToStartSpawningLeaves(minBranchHeightToStartSpawningLeaves[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }


                            maxBranchHeightToStartSpawningLeaves[0] = treeModel.getMaxBranchHeightToStartSpawningLeaves();
                            if(ImGui.sliderFloat("maxBranchHeightToStartSpawningLeaves", maxBranchHeightToStartSpawningLeaves, 0.01f, 5f)){
                                treeModel.setMaxBranchHeightToStartSpawningLeaves(maxBranchHeightToStartSpawningLeaves[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }


                            leafIncrement[0] = treeModel.getLeafIncrement();
                            if(ImGui.sliderFloat("leafIncrement", leafIncrement, 0.01f, 1f)){
                                treeModel.setLeafIncrement(leafIncrement[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            minLeavesToSpawnPerPoint[0] = treeModel.getMinLeavesToSpawnPerPoint();
                            if(ImGui.sliderInt("minLeavesToSpawnPerPoint", minLeavesToSpawnPerPoint, 1, 8)){
                                treeModel.setMinLeavesToSpawnPerPoint(minLeavesToSpawnPerPoint[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }

                            maxLeavesToSpawnPerPoint[0] = treeModel.getMaxLeavesToSpawnPerPoint();
                            if(ImGui.sliderInt("maxLeavesToSpawnPerPoint", maxLeavesToSpawnPerPoint, 1, 8)){
                                treeModel.setMaxLeavesToSpawnPerPoint(maxLeavesToSpawnPerPoint[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }
                            
                            leafDistanceFromCenter[0] = treeModel.getLeafDistanceFromCenter();
                            if(ImGui.sliderFloat("leafDistanceFromCenter", leafDistanceFromCenter, 0.01f, 3f)){
                                treeModel.setLeafDistanceFromCenter(leafDistanceFromCenter[0]);
                                ImGuiEntityActorTab.regenerateModel(detailViewEntity,proceduralModel);
                            }
                        }
                    }
                }
            }
            ImGui.unindent();
        }
    }
    
    /**
     * Regenerates the procedural model for the entity
     * @param detailViewEntity The entity
     * @param proceduralModel The procedural model
     */
    private static void regenerateModel(Entity detailViewEntity, ProceduralModel proceduralModel){
        if(proceduralModel.getTreeModel() != null){
            for(Entity child : AttachUtils.getChildrenList(detailViewEntity)){
                ClientEntityUtils.destroyEntity(child);
            }
            AttachUtils.getChildrenList(detailViewEntity).clear();
            ProceduralTree.setProceduralActor(
                detailViewEntity,
                proceduralModel.getTreeModel(),
                new Random()
            );
        }
    }

}
