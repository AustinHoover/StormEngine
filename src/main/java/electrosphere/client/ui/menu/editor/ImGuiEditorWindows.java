package electrosphere.client.ui.menu.editor;

import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * Various methods for creating specific imgui windows in engine
 */
public class ImGuiEditorWindows {
    

    //scene hierarchy menu
    private static ImGuiWindow mainWindow;

    //tracks if the editor menu is open
    private static boolean editorIsOpen = false;

    /**
     * The current tab
     */
    private static int currentTab = 0;

    /**
     * Initializes imgui windows
     */
    public static void initEditorWindows(){
        ImGuiEditorWindows.createMainEditorMenu();
        ImGuiEditorDetailsWindow.createDetailsMenu();
    }

    /**
     * Gets the hierarchy window
     * @return the hierarchy window
     */
    public static ImGuiWindow getHierarchyWindow(){
        return mainWindow;
    }


    /**
     * Inits the hierarchy menus
     */
    private static void createMainEditorMenu(){
        mainWindow = new ImGuiWindow("Editor");
        mainWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                if(ImGui.beginTabBar("Tabs")){
                    if(ImGui.beginTabItem("General")){
                        currentTab = 0;
                        ImGui.text("hello :)");
                        ImGui.endTabItem();
                    }
                    if(ImGui.beginTabItem("Assets")){
                        currentTab = 1;
                        ImGui.text("asset selector here");
                        if(ImGui.button("testasset")){
                                
                        }
                        if(ImGui.beginDragDropSource()){
                            ImGui.setDragDropPayload("asdf");
                            ImGui.endDragDropSource();
                        }
                        ImGui.endTabItem();
                    }
                    if(ImGui.beginTabItem("Hierarchy")){
                        currentTab = 2;
                        ImGui.text("hierarchy controls here");
                        ImGui.endTabItem();
                    }
                    if(ImGui.beginTabItem("Areas")){
                        currentTab = 3;
                        ImGuiAreaTab.draw();
                        ImGui.endTabItem();
                    }
                    if(ImGui.beginTabItem("Structure")){
                        currentTab = 4;
                        ImGuiStructureTab.draw();
                        ImGui.endTabItem();
                    }

                    ImGui.endTabBar();
                }
            }
        });
        mainWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(mainWindow);
    }

    /**
     * Toggles the open state of the menu
     */
    public static void toggleEditorMenus(){
        mainWindow.setOpen(!mainWindow.isOpen());
        ImGuiEditorWindows.editorIsOpen = mainWindow.isOpen();
        if(mainWindow.isOpen()){
            Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
            Main.setFramestep(Main.FRAMESTEP_PAUSE);
        } else {
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            Main.setFramestep(Main.FRAMESTEP_AUTO);
        }

        //toggle all windows
        ImGuiEditorDetailsWindow.toggleDetailsMenus();
    }

    /**
     * Makes sure the main editor menu properly toggles controls if it is closed with the X button
     */
    public static void synchronizeMainEditorMenuVisibility(){
        if(ImGuiEditorWindows.editorIsOpen && !mainWindow.isOpen()){
            ImGuiEditorWindows.editorIsOpen = false;
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
        }
    }

    /**
     * Gets the current tab
     * @return The current tab
     */
    protected static int getCurrentTab(){
        return currentTab;
    }

}
