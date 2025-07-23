package electrosphere.client.ui.menu.script;

import org.graalvm.polyglot.HostAccess.Export;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.dialog.DialogMenuGenerator;
import electrosphere.client.ui.menu.ingame.FabMenus;
import electrosphere.client.ui.menu.ingame.MenuGeneratorsTerrainEditing;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.Globals;

/**
 * Utilities provided to the scripting interface that allow creating/destroying menus
 */
public class ScriptMenuUtils {
    
    /**
     * Opens the voxel selection menu
     */
    @Export
    public static void openVoxel(){
        WindowUtils.replaceWindow(WindowStrings.VOXEL_TYPE_SELECTION,MenuGeneratorsTerrainEditing.createVoxelTypeSelectionPanel());
        Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
    }

    /**
     * Opens the menu to select what to spawn
     */
    @Export
    public static void openSpawnSelection(){
        WindowUtils.replaceWindow(WindowStrings.SPAWN_TYPE_SELECTION,MenuGeneratorsTerrainEditing.createEntityTypeSelectionPanel());
        Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
    }

    /**
     * Opens the menu to select what fab to use
     */
    @Export
    public static void openFabSelection(){
        WindowUtils.replaceWindow(WindowStrings.FAB_SELECTION,FabMenus.createFabSelectionPanel());
        Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
    }

    /**
     * Opens a specified dialog
     */
    @Export
    public static void openDialog(String path){
        String sanitized = path.replace("\"", "").replace("'", "");
        DialogMenuGenerator.displayDialogPath(sanitized);
    }

}
