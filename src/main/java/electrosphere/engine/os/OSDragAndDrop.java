package electrosphere.engine.os;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import electrosphere.client.ui.menu.editor.ImGuiEditorWindows;
import electrosphere.engine.Globals;
import electrosphere.engine.os.fs.FileWatcher;
import electrosphere.logger.LoggerInterface;

/**
 * Handles drag-and-drop signals from the OS
 */
public class OSDragAndDrop {

    /**
     * Fires when a file path is dropped on the application
     * @param paths The paths of the files
     */
    public static void handleDragAndDrop(List<String> paths){
        if(ImGuiEditorWindows.getHierarchyWindow().isOpen()){
            OSDragAndDrop.handleDropAsset(paths);
        }
    }

    /**
     * Handles the case where asset path(s) have been dropped on the application
     * @param paths The paths of the assets
     */
    private static void handleDropAsset(List<String> paths){
        //try watching the file and register it to be an asset
        LoggerInterface.loggerFileIO.WARNING("File(s) added to application");
        for(String rawPath : paths){
            String normalized = rawPath.toLowerCase();
            Path absolutePath = new File(rawPath).toPath();
            Path relativePath = new File("./assets").getAbsoluteFile().toPath().relativize(absolutePath);

            //register based on file type
            if(normalized.contains(".glsl") || normalized.contains(".dae")){
                Globals.assetManager.addModelPathToQueue(relativePath.toString());
            } else if(normalized.contains(".wav") || normalized.contains(".ogg")){
                Globals.assetManager.addAudioPathToQueue(relativePath.toString());
            } else if(normalized.contains(".jpg") || normalized.contains(".jpeg") || normalized.contains(".png")){
                Globals.assetManager.addTexturePathtoQueue(relativePath.toString());
            } else {
                throw new Error("Unhandled file type! " + rawPath);
            }

            //watch the asset for file updates
            LoggerInterface.loggerEngine.WARNING("Tracking " + rawPath);
            Globals.engineState.fileWatcherService.trackFile(new FileWatcher(rawPath).setOnWrite(updatedPath -> {
                LoggerInterface.loggerFileIO.WARNING("File updated: " + updatedPath.toString());
                Globals.assetManager.updateAsset(updatedPath.toString());
            }).setOnDelete(updatedPath -> {
                LoggerInterface.loggerFileIO.WARNING("File deleted: " + updatedPath.toString());
            }).setOnCreate(updatedPath -> {
                LoggerInterface.loggerFileIO.WARNING("File created: " + updatedPath.toString());
            }));
        }
    }
    
}
