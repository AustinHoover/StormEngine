package electrosphere.client.ui.menu.editor;

import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * Details window of the editor
 */
public class ImGuiEditorDetailsWindow {

    /**
     * The window object
     */
    private static ImGuiWindow detailsWindow;

    /**
     * Gets the details window
     * @return the details window
     */
    public static ImGuiWindow getDetailsWindow(){
        return detailsWindow;
    }

    /**
     * Inits the details menu
     */
    protected static void createDetailsMenu(){
        detailsWindow = new ImGuiWindow("Details");
        detailsWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                switch(ImGuiEditorWindows.getCurrentTab()){
                    case 0: {
                        //general tab
                        ImGui.text("General tab");
                    } break;
                    case 1: {
                        //assets tab
                        ImGui.text("Asset tab");
                    } break;
                    case 2: {
                        //hierarchy tab
                        ImGui.text("Hierarchy tab");
                    } break;
                    case 3: {
                        //area tab
                        ImGui.text("Area tab");
                    } break;
                    case 4: {
                        //structure tab
                        ImGuiStructureTab.drawDetails();
                    } break;
                }
            }
        });
        detailsWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(detailsWindow);
    }

    /**
     * Toggles the open state of the menu
     */
    protected static void toggleDetailsMenus(){
        detailsWindow.setOpen(!detailsWindow.isOpen());
        if(detailsWindow.isOpen()){
            Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
        } else {
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
        }
    }
    
}
