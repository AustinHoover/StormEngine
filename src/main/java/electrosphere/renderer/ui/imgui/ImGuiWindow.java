package electrosphere.renderer.ui.imgui;

import java.util.LinkedList;
import java.util.List;

import imgui.ImGui;
import imgui.type.ImBoolean;

/**
 * A window in ImGui. The window can contain any number of controls and information.
 */
public class ImGuiWindow {

    //the name of the window
    String windowName;

    //The elements housed within this window
    List<ImGuiElement> elements = new LinkedList<ImGuiElement>();

    //Optional callback for the window
    ImGuiWindowCallback callback = null;

    //window boolean
    ImBoolean open;
    
    /**
     * Creates the window
     */
    public ImGuiWindow(String windowName){
        open = new ImBoolean(true);
        this.windowName = windowName;
    }

    /**
     * Adds an imgui element to the window
     */
    public void addElement(ImGuiElement element){
        elements.add(element);
    }

    /**
     * Removes an element from this window
     * @param element The element
     */
    public void removeElement(ImGuiElement element){
        this.elements.remove(element);
    }

    /**
     * Sets the callback for the window
     * @param callback The callback
     */
    public void setCallback(ImGuiWindowCallback callback){
        this.callback = callback;
    }

    /**
     * Draws this window
     */
    public void draw(){
        if(open.getData()[0]){
            ImGui.begin(windowName,open);

            for(ImGuiElement element : elements){
                element.draw();
            }

            if(callback != null){
                callback.exec();
            }

            ImGui.end();
        }
    }

    /**
     * Sets the open status of the window
     * @param open if true will be open, if false will be closed
     */
    public void setOpen(boolean open){
        this.open.set(open);
    }

    /**
     * Gets whether the window is open or not
     * @return true if open, false otherwise
     */
    public boolean isOpen(){
        return this.open.get();
    }


    /**
     * An optional callback for the window that lets you directly call imgui functions
     */
    public static interface ImGuiWindowCallback {
        /**
         * The actual callback function
         */
        public void exec();
    }

}
