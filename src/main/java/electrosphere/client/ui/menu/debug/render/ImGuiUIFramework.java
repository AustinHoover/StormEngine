package electrosphere.client.ui.menu.debug.render;

import electrosphere.client.ui.menu.dialog.DialogMenuGenerator;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.debug.DebugRendering;
import electrosphere.renderer.ui.elements.BitmapCharacter;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * UI Framework debug menus
 */
public class ImGuiUIFramework {
    
    //window for viewing information about the ui framework
    public static ImGuiWindow uiFrameworkWindow;

    /**
     * Client scene entity view
     */
    public static void createUIFrameworkDebugWindow(){
        uiFrameworkWindow = new ImGuiWindow("UI Framework");
        uiFrameworkWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //ui framework text
                ImGui.text("UI Framework");
                
                if(ImGui.button("Show UI Outlines")){
                    DebugRendering.RENDER_DEBUG_UI_TREE = !DebugRendering.RENDER_DEBUG_UI_TREE;
                }

                if(ImGui.button("Print tree")){
                    printUITrees();
                }

                if(ImGui.button("Test load dialog")){
                    DialogMenuGenerator.createDialogWindow("Data/menu/npcintro.html");
                }

                if(ImGui.button("Reload dynamic menus")){
                    DialogMenuGenerator.refresh();
                }

            }
        });
        uiFrameworkWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(uiFrameworkWindow);
    }

    /**
     * Prints the UI trees
     */
    private static void printUITrees(){
        int i = 0;
        for(Element window : Globals.elementService.getWindowList()){
            LoggerInterface.loggerUI.WARNING("Window " + i + " " + Globals.elementService.getWindowId(window));
            ImGuiUIFramework.printUITree(window, 1);
            LoggerInterface.loggerUI.WARNING("----\n\n");
            i++;
        }
    }

    /**
     * Prints the ui tree for a given element
     * @param rootEl The element
     * @param indent The current indentation
     */
    private static void printUITree(Element rootEl, int indent){
        String indentStr = "";
        for(int i = 0; i < indent; i++){
            indentStr = indentStr + "\t";
        }
        if(rootEl instanceof BitmapCharacter){

        } else {
            LoggerInterface.loggerUI.WARNING(indentStr + "--" + rootEl + "--");
            LoggerInterface.loggerUI.WARNING(indentStr + rootEl.getAbsoluteX() + " " + rootEl.getAbsoluteY() + " " + rootEl.getWidth() + " " + rootEl.getHeight());
            LoggerInterface.loggerUI.WARNING("\n");
        }
        if(rootEl instanceof ContainerElement){
            ContainerElement containerView = (ContainerElement)rootEl;
            for(Element child : containerView.getChildren()){
                printUITree(child, indent + 1);
            }
        }
    }

}
