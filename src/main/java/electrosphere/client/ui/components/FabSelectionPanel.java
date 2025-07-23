package electrosphere.client.ui.components;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector4f;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.YogaUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.TextInput;
import electrosphere.renderer.ui.elements.VirtualScrollable;
import electrosphere.renderer.ui.elementtypes.ClickableElement.ClickEventCallback;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.HoverableElement.HoverEventCallback;
import electrosphere.renderer.ui.elementtypes.KeyEventElement.KeyboardEventCallback;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.HoverEvent;
import electrosphere.renderer.ui.events.KeyboardEvent;

/**
 * Panel for selecting fab files
 */
public class FabSelectionPanel {
    
    //text input
    static final int TEXT_INPUT_HEIGHT = 50;
    static final int TEXT_INPUT_WIDTH = 200;

    //single fab button
    static final int FAB_BUTTON_WIDTH = 90;
    static final int FAB_BUTTON_HEIGHT = 90;
    static final int FAB_BUTTON_TEXTURE_DIM = 70;
    static final int MARGIN_EACH_SIDE = 5;

    //fab selection
    static final int FAB_SCROLLABLE_WIDTH = FAB_BUTTON_WIDTH * 5;
    static final int FAB_SCROLLABLE_HEIGHT = FAB_BUTTON_HEIGHT * 5;

    /**
     * The color of the select fab type
     */
    static final Vector4f ELEMENT_COLOR_SELECTED = new Vector4f(1,0,0,1);
    

    /**
     * Creates the fab selection panel component
     * @return The top level element of the panel component
     */
    public static Div createFabSelectionPanel(Consumer<File> onSelectType){
        //setup window
        Div rVal = Div.createDiv();
        rVal.setAlignContent(YogaAlignment.Center);
        rVal.setAlignItems(YogaAlignment.Center);
        rVal.setJustifyContent(YogaJustification.Center);
        rVal.setFlexDirection(YogaFlexDirection.Column);

        //scrollable that contains all the fab types
        VirtualScrollable scrollable = new VirtualScrollable(FAB_SCROLLABLE_WIDTH, FAB_SCROLLABLE_HEIGHT);
        scrollable.setFlexDirection(YogaFlexDirection.Column);
        scrollable.setAlignItems(YogaAlignment.Start);

        //search input
        TextInput searchInput = TextInput.createTextInput();
        searchInput.setWidth(TEXT_INPUT_WIDTH);
        searchInput.setMinWidth(TEXT_INPUT_WIDTH);
        searchInput.setMinHeight(20);
        searchInput.setOnPress(new KeyboardEventCallback() {public boolean execute(KeyboardEvent event){
            boolean rVal = searchInput.defaultKeyHandling(event);
            FabSelectionPanel.fillInFabSelectors(scrollable, searchInput.getText(), onSelectType);
            return rVal;
        }});
        rVal.addChild(searchInput);


        //attach scrollable after search input for organzation purposes
        rVal.addChild(scrollable);

        //final step
        FabSelectionPanel.fillInFabSelectors(scrollable, searchInput.getText(), onSelectType);
        
        return rVal;
    }

    /**
     * Fills in the fab files to display based on the contents of the search string
     * @param scrollable the scrollable to drop selection buttons in to
     * @param searchString the string to search based on
     */
    static void fillInFabSelectors(VirtualScrollable scrollable, String searchString, Consumer<File> onSelectType){
        Element containingWindow = null;
        if(Globals.elementService.getWindow(WindowStrings.FAB_SELECTION) != null){
            containingWindow = Globals.elementService.getWindow(WindowStrings.FAB_SELECTION);
        } else if(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN) != null){
            containingWindow = Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN);
        }
        YogaUtils.refreshComponent(containingWindow, () -> {
            scrollable.clearChildren();
            Div currentRow = null;
            int incrementer = 0;
            //generate fab file buttons
            List<File> fabFiles = Arrays.asList(new File("./assets/Data/fab").listFiles());
            for(File fabFile : fabFiles){
                if(incrementer % 4 == 0){
                    currentRow = Div.createRow();
                    currentRow.setJustifyContent(YogaJustification.Evenly);
                    scrollable.addChild(currentRow);
                }
                Div containerDiv = Div.createDiv();
                containerDiv.setMinWidthPercent(25.0f);
                currentRow.addChild(containerDiv);

                Button newButton = new Button();
                newButton.setAlignItems(YogaAlignment.Center);
                //dimensions
                newButton.setMinWidth(FAB_BUTTON_WIDTH);
                newButton.setMinHeight(FAB_BUTTON_HEIGHT);
                //margin
                newButton.setMarginBottom(MARGIN_EACH_SIDE);
                newButton.setMarginLeft(MARGIN_EACH_SIDE);
                newButton.setMarginRight(MARGIN_EACH_SIDE);
                newButton.setMarginTop(MARGIN_EACH_SIDE);
                //label
                Label fabLabel = Label.createLabel(fabFile.getName());
                fabLabel.setMaxWidth(FAB_BUTTON_WIDTH);
                //icon/model
                ImagePanel texturePanel = ImagePanel.createImagePanel(AssetDataStrings.TEXTURE_DEFAULT);
                texturePanel.setWidth(FAB_BUTTON_TEXTURE_DIM);
                texturePanel.setHeight(FAB_BUTTON_TEXTURE_DIM);
                texturePanel.setMarginBottom(MARGIN_EACH_SIDE);
                texturePanel.setMarginLeft(MARGIN_EACH_SIDE);
                texturePanel.setMarginRight(MARGIN_EACH_SIDE);
                texturePanel.setMarginTop(MARGIN_EACH_SIDE);
                newButton.addChild(texturePanel);
                texturePanel.setAlignSelf(YogaAlignment.Center);
                //causes the texture panel to also behave as if the button was hovered
                texturePanel.setOnHoverCallback(new HoverEventCallback() {public boolean execute(HoverEvent event) {
                    return newButton.handleEvent(event);
                }});
                //button handling
                newButton.addChild(fabLabel);
                newButton.setOnClick(new ClickEventCallback() {public boolean execute(ClickEvent event){
                    //accept the selected file
                    onSelectType.accept(fabFile);
                    FabSelectionPanel.fillInFabSelectors(scrollable, searchString, onSelectType);
                    return false;
                }});
                containerDiv.addChild(newButton);
                incrementer++;
            }
            for(int i = incrementer; i % 4 != 0; i++){
                Div spacerDiv = Div.createDiv();
                spacerDiv.setMinWidthPercent(25.0f);
                currentRow.addChild(spacerDiv);
            }
        });
    }

}
