package electrosphere.renderer.ui.imgui.filediag;

import java.io.File;
import java.util.function.Consumer;

import imgui.extension.imguifiledialog.ImGuiFileDialog;

/**
 * Manages file dialogs created with imgui
 */
public class ImGuiFileDialogManager {

    /**
     * Key for the file dialog
     */
    public static final String key = "fileDiagKey";

    /**
     * Minimum width of the file modal
     */
    public static final int MIN_WIDTH = 500;

    /**
     * Minimum height of the file modal
     */
    public static final int MIN_HEIGHT = 500;

    /**
     * Maximum width of the file modal
     */
    public static final int MAX_WIDTH = 1000;

    /**
     * Maximum height of the file modal
     */
    public static final int MAX_HEIGHT = 1000;

    /**
     * Filter for any file ending
     */
    public static final String ANY_FILE_ENDING = ".*";

    /**
     * The consumer of the selected path when a selection is made in the dialog
     */
    private static Consumer<File> onAccept = null;
    
    /**
     * Opens the file dialog
     * @param onAccept The consumer for the path when a file is selected
     */
    public static void open(String title, String fileDefaultName, String fileEndings, Consumer<File> onAccept){
        ImGuiFileDialogManager.onAccept = onAccept;
        ImGuiFileDialog.openModal(key, title, fileEndings, fileDefaultName, 0, 0, 0);
    }

    /**
     * Opens the file dialog
     * @param onAccept The consumer for the path when a file is selected
     */
    public static void openDirSelect(String title, String fileDefaultName, Consumer<File> onAccept){
        ImGuiFileDialogManager.onAccept = onAccept;
        ImGuiFileDialog.openModal(key, title, null, fileDefaultName, 0, 0, 0);
    }

    /**
     * Handles rendering file dialog
     */
    public static void handleFileDialogs(){
        if(ImGuiFileDialog.display(key, 0, MIN_WIDTH, MIN_HEIGHT, MAX_WIDTH, MAX_HEIGHT)){
            if(ImGuiFileDialog.isOk()){
                File file = new File(ImGuiFileDialog.getCurrentPath() + "/" + ImGuiFileDialog.getCurrentFileName());
                onAccept.accept(file);
            }
            ImGuiFileDialog.close();
        }
    }

}
