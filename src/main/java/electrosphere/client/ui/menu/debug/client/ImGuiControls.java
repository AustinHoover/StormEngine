package electrosphere.client.ui.menu.debug.client;

import electrosphere.engine.Globals;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * Controls debug menus
 */
public class ImGuiControls {
    
    //window for viewing information about the controls state
    public static ImGuiWindow controlsWindow;

    /**
     * Client scene entity view
     */
    public static void createControlsDebugWindow(){
        controlsWindow = new ImGuiWindow("Controls");
        controlsWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //ui framework text
                ImGui.text("Controls");

                //control handler stuff
                ImGui.text("ControlHandler state: " + Globals.controlHandler.getHandlerState());
                
            }
        });
        controlsWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(controlsWindow);
    }

}
