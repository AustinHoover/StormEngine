package electrosphere.client.ui.menu.debug.scene;

import electrosphere.collision.CollisionEngine;
import electrosphere.engine.Globals;
import electrosphere.entity.scene.Scene;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * Views a scene
 */
public class ImGuiSceneWindow {

    /**
     * The target scene
     */
    public static Scene targetScene;

    /**
     * The associated collision engine
     */
    public static CollisionEngine targetCollisionEngine;
    
    /**
     * Window for scene data
     */
    public static ImGuiWindow sceneWindow;

    /**
     * Scene view
     */
    public static void createChunkMonitorWindow(){
        sceneWindow = new ImGuiWindow("Scene Data");

        sceneWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                if(targetCollisionEngine != null && ImGui.collapsingHeader("Collision Engine")){
                    ImGui.text(targetCollisionEngine.getStatus());
                }
            }
        });
        sceneWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(sceneWindow);
    }

    /**
     * Views a scene
     * @param scene The scene
     * @param engine The collision engine associated with the scene (optional)
     */
    public static void viewScene(Scene scene, CollisionEngine engine){
        ImGuiSceneWindow.targetScene = scene;
        ImGuiSceneWindow.targetCollisionEngine = engine;
        sceneWindow.setOpen(true);
    }

}
