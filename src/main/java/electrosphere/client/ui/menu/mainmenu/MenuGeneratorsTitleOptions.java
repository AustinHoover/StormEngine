package electrosphere.client.ui.menu.mainmenu;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.NavigationEvent;

/**
 * Functions for generating the options menu ui
 */
public class MenuGeneratorsTitleOptions {
    
    /**
     * Generates the main options page
     * @return The element containing the main options page
     */
    public static Element createOptionsMainMenu(){
        FormElement rVal = new FormElement();

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
            return false;
        });
        
        //label (options)
        Label optionsLabel = Label.createLabel("Options");
        rVal.addChild(optionsLabel);

        //button (back)
        rVal.addChild(Button.createButton("Back", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
        }));

        //button to open rebind controls window
        Button rebindControlsButton = Button.createButton("Controls", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsKeybind.createControlsRebindMenu());
        });
        rVal.addChild(rebindControlsButton);

        return rVal;
    }

}
