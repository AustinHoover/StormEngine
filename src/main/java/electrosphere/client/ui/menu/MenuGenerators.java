package electrosphere.client.ui.menu;

import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * Generator functions for creating menus
 */
public class MenuGenerators {

    /**
     * Creates the empty content to display when loading main menu
     * @return The empty content
     */
    public static Element createEmptyMainMenu(){
        Div rVal = Div.createDiv();
        return rVal;
    }

    
    
}
