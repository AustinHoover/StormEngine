package electrosphere.client.ui.menu.ingame;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Panel;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;

/**
 * Menu for displaying a tooltip that shows the current interaction target
 */
public class InteractionTargetMenu {

    /**
     * Width of window
     */
    static final int WINDOW_WIDTH = 100;
    
    /**
     * Height of window
     */
    static final int WINDOW_HEIGHT = 100;
    
    /**
     * Creates the main in game menu that shows up when you (typically) hit the escape key
     * @return The window for the menu
     */
    public static Window createInteractionTargetTooltipWindow(){
        Window rVal = Window.createExpandableCenterAligned(Globals.renderingEngine.getOpenGLState(), false);
        rVal.setAlignItems(YogaAlignment.Center);

        Label label = Label.createLabel("");
        rVal.addChild(Panel.createPanel(label));

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,rVal);

        return rVal;
    }

    /**
     * Sets the text for the interaction target tooltip
     * @param text The text, pass an empty string to hide the tooltip
     */
    public static void setInteractionTargetString(String text){
        Window interactionTooltipWindow = (Window)Globals.elementService.getWindow(WindowStrings.TARGET_TOOLTIP);
        if(interactionTooltipWindow != null){
            Panel container = (Panel)interactionTooltipWindow.getChildren().get(0);
            Label label = (Label)container.getChildren().get(0);
            if(text.length() == 0){
                if(label.getText().length() != 0){
                    label.setText("");
                }
                interactionTooltipWindow.setVisible(false);
            } else {
                interactionTooltipWindow.setVisible(true);
                if(!label.getText().contains(text)){
                    label.setText(text);
                    Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, interactionTooltipWindow);
                }
            }
        }
    }

}
