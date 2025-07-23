package electrosphere.client.ui.menu.mainmenu;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.Control;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.VirtualScrollable;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * A menu for rebinding controls
 */
public class MenuGeneratorsKeybind {

    //tracks whether the menu has been used to rebind a control or not
    static boolean modifiedControls = false;
    
    /**
     * The controls rebind window
     * @return the window element
     */
    public static Element createControlsRebindMenu(){
        FormElement rVal = new FormElement();
        rVal.setAlignItems(YogaAlignment.Center);

        //header buttons
        Div headerButtons = Div.createDiv();
        headerButtons.setFlexDirection(YogaFlexDirection.Row);
        Button backButton = Button.createButton("Back", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
        });
        headerButtons.addChild(backButton);

        Button saveButton = Button.createButton("Save", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
        });
        saveButton.setVisible(false);
        headerButtons.addChild(saveButton);

        Button cancelButton = Button.createButton("Cancel", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
        });
        cancelButton.setVisible(false);
        headerButtons.addChild(cancelButton);

        rVal.addChild(headerButtons);


        //
        //Generate keybind controls
        VirtualScrollable virtualScrollable = new VirtualScrollable(300, 700);
        virtualScrollable.setAlignItems(YogaAlignment.Start);
        //add a ton of children
        for(Control control : Globals.controlHandler.getMainControlsList()){
            if(control.isRebindable()){
                Div rebindItem = Div.createDiv();
                rebindItem.setFlexDirection(YogaFlexDirection.Row);

                Label controlNameLabel = Label.createLabel(control.getName());
                rebindItem.addChild(controlNameLabel);

                Button testButton = Button.createButton(control.getKeyValue() + "", () -> {
                    System.out.println("Start rebind");
                });
                rebindItem.addChild(testButton);

                virtualScrollable.addChild(rebindItem);
            }
        }
        
        rVal.addChild(virtualScrollable);

        return rVal;
    }

}
