package electrosphere.client.ui.menu.mainmenu;

import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * Generates menu items for the demo version of the engine
 */
public class MenuGeneratorsDemo {
    
    /**
     * Creates the title menu for the demo
     * @return The content element to embed in a title window
     */
    public static Element createTitleMenu(){
        FormElement rVal = new FormElement();
        //top-bottom
        rVal.setJustifyContent(YogaJustification.Center);
        //left-right
        rVal.setAlignItems(YogaAlignment.Center);
        rVal.setAlignContent(YogaAlignment.Start);

        //label (title)
        Label titleLabel = Label.createLabel("ORPG");
        rVal.addChild(titleLabel);

        return rVal;
    }

}
