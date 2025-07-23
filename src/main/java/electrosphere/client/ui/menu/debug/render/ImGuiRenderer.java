package electrosphere.client.ui.menu.debug.render;

import electrosphere.engine.Globals;
import electrosphere.renderer.pipelines.PostProcessingPipeline;
import electrosphere.renderer.pipelines.ShadowMapPipeline;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

public class ImGuiRenderer {
    
    //window for viewing information about loggers
    public static ImGuiWindow rendererWindow;

    //stores far plane
    private static float[] farPlaneArr = new float[]{1};

    /**
     * loggers view
     */
    public static void createRendererWindow(){
        rendererWindow = new ImGuiWindow("Renderer");
        rendererWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                if(ImGui.collapsingHeader("Pipelines")){
                    ImGui.indent();
                    if(ImGui.collapsingHeader("Shadow Map Pipeline")){
                        ShadowMapPipeline shadowMapPipeline = Globals.renderingEngine.getShadowMapPipeline();
                        if(ImGui.sliderFloat("Far Plane Distance", farPlaneArr, 1, 100)){
                            shadowMapPipeline.setFarPlane(farPlaneArr[0]);
                        }
                    }
                    if(ImGui.collapsingHeader("Main Content Pipeline")){
                        ImGui.textWrapped(Globals.renderingEngine.getMainContentPipeline().getTrackingInfo());
                    }
                    if(ImGui.collapsingHeader("Post Processing Pipeline")){
                        PostProcessingPipeline postProcessingPipeline = Globals.renderingEngine.getPostProcessingPipeline();
                        if(ImGui.button("Toggle Blur")){
                            postProcessingPipeline.setApplyBlur(!postProcessingPipeline.isApplyingBlur());
                        }
                    }
                    ImGui.unindent();
                }
                if(ImGui.collapsingHeader("Debug Toggles")){
                    ImGui.indent();
                    if(ImGui.button("Draw Client Hitboxes")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawCollisionSpheresClient(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawCollisionSpheresClient());
                    }
                    if(ImGui.button("Draw Server Hitboxes")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawCollisionSpheresServer(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawCollisionSpheresServer());
                    }
                    if(ImGui.button("Draw Physics Objects (client)")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawPhysicsObjectsClient(!Globals.gameConfigCurrent.getSettings().graphicsDebugDrawPhysicsObjectsClient());
                    }
                    if(ImGui.button("Draw Physics Objects (server)")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawPhysicsObjectsServer(!Globals.gameConfigCurrent.getSettings().graphicsDebugDrawPhysicsObjectsServer());
                    }
                    if(ImGui.button("Draw Grid Alignment Data")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawGridAlignment(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawGridAlignment());
                    }
                    if(ImGui.button("Draw Interaction Collidable Data")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawInteractionCollidables(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawInteractionCollidables());
                    }
                    if(ImGui.button("Draw Macro Data Colliders")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawMacroColliders(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawMacroColliders());
                    }
                    if(ImGui.button("Draw Client Cell Colliders")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawClientCellColliders(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawClientCellColliders());
                    }
                    if(ImGui.button("Draw Server Cell Colliders")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawServerCellColliders(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawServerCellColliders());
                    }
                    if(ImGui.button("Draw Server Facing Vectors")){
                        Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawServerFacingVectors(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawServerFacingVectors());
                    }
                    ImGui.unindent();
                }
                if(ImGui.collapsingHeader("OpenGL Details")){
                    ImGui.text("GL_MAX_TEXTURE_IMAGE_UNITS: " + Globals.renderingEngine.getOpenGLContext().getMaxTextureImageUnits());
                    ImGui.text("GL_MAX_TEXTURE_SIZE: " + Globals.renderingEngine.getOpenGLContext().getMaxTextureSize());
                }
            }
        });
        rendererWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(rendererWindow);
    }

}
