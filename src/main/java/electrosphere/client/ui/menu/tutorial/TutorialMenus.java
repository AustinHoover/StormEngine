package electrosphere.client.ui.menu.tutorial;

import org.graalvm.polyglot.HostAccess.Export;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.data.tutorial.TutorialHint;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.TextBox;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;

/**
 * Generates in game tutorial windows
 */
public class TutorialMenus {

    /**
     * The width of a tutorial popup
     */
    static final int TUTORIAL_POPUP_WIDTH = 500;
    /**
     * The height of a tutorial popup
     */
    static final int TUTORIAL_POPUP_HEIGHT = 500;

    @Export
    /**
     * Shows a tutorial hint
     * @param hint the hint id
     * @param onClose A callback fired when the hint popup is closed. Either a 
     */
    public static void showTutorialHint(String hintId, boolean captureControls, Runnable onClose){
        //get the hint definition
        TutorialHint hintDefinition = Globals.gameConfigCurrent.getHintData().getHintById(hintId);

        //Get the window
        Window windowEl;
        if(Globals.elementService.containsWindow(WindowStrings.TUTORIAL_POPUP)){
            windowEl = (Window)Globals.elementService.getWindow(WindowStrings.TUTORIAL_POPUP);
        } else {
            //create the window
            windowEl = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, TUTORIAL_POPUP_WIDTH, TUTORIAL_POPUP_HEIGHT, true);
            //parent container
            windowEl.setParentAlignContent(YogaAlignment.Center);
            windowEl.setParentAlignItem(YogaAlignment.Center);
            windowEl.setParentJustifyContent(YogaJustification.Center);
            //child elements arrangement
            windowEl.setFlexDirection(YogaFlexDirection.Column);
            windowEl.setAlignItems(YogaAlignment.Center);
            windowEl.setJustifyContent(YogaJustification.Between);
            Globals.elementService.registerWindow(WindowStrings.TUTORIAL_POPUP, windowEl);
        }

        //clear previous content
        windowEl.clearChildren();

        //optionally switch to ui controls
        if(captureControls){
            Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
        }

        //create tutorial elements
        windowEl.addChild(Label.createLabel(hintDefinition.getTitleString()));
        windowEl.addChild(TextBox.createTextBox(hintDefinition.getDescriptionString(), false));
        windowEl.addChild(Button.createButton("Close", () -> {
            WindowUtils.recursiveSetVisible(windowEl, false);
            if(onClose != null){
                onClose.run();
            }
            //optionally switch to ui controls
            if(captureControls){
                Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            }
        }));

        //show the window
        WindowUtils.recursiveSetVisible(windowEl, true);

    }
    
}
