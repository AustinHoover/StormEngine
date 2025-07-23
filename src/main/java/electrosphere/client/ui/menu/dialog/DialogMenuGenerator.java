package electrosphere.client.ui.menu.dialog;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.parsing.HtmlParser;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.util.FileUtils;

/**
 * Generates dialog menus
 */
public class DialogMenuGenerator {

    /**
     * The currently displayed dialog's path
     */
    static String currentDialog = null;

    /**
     * Displays the appropriate dialog for opening a dialog window with a given entity
     * @param target The entity
     */
    public static void displayEntityDialog(Entity target){
        //TODO: logic to determine what menu to actually display
        DialogMenuGenerator.currentDialog = "Data/menu/npcintro.html";
        DialogMenuGenerator.refresh();
    }

    /**
     * Displays the appropriate dialog for opening a dialog window with a given entity
     * @param target The entity
     */
    public static void displayDialogPath(String path){
        //TODO: logic to determine what menu to actually display
        DialogMenuGenerator.currentDialog = path;
        DialogMenuGenerator.refresh();
    }
    
    /**
     * Displays a dialog menu
     * @param path The path to the html
     * @return The element that is the root for the window
     */
    public static Window createDialogWindow(String path){
        DialogMenuGenerator.currentDialog = path;
        //
        //Boilerplate for window itself
        //
        Window rVal = Window.createExpandableCenterAligned(Globals.renderingEngine.getOpenGLState());
        rVal.setParentAlignItem(YogaAlignment.Center);
        rVal.setParentJustifyContent(YogaJustification.Center);
        rVal.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.closeWindow(WindowStrings.NPC_DIALOG);
            Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            return false;
        }});

        //
        //main dialog ui logic
        //
        Div container = Div.createCol();
        try {
            String content = FileUtils.getAssetFileAsString(path);
            Document doc = Jsoup.parseBodyFragment(content);
            Node bodyNode = doc.getElementsByTag("body").first();
            container.addChild(HtmlParser.parseJSoup(bodyNode));
        } catch (IOException e) {
            LoggerInterface.loggerFileIO.ERROR(e);
        }
        rVal.addChild(container);


        //
        //Final setup
        //

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, rVal);

        return rVal;
    }

    /**
     * Refreshes the currently displayed dialog
     */
    public static void refresh(){
        WindowUtils.replaceWindow(WindowStrings.NPC_DIALOG, DialogMenuGenerator.createDialogWindow(DialogMenuGenerator.currentDialog));
        Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
    }

}
