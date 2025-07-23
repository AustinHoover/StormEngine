package electrosphere.client.ui.components;

import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector4f;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.YogaUtils;
import electrosphere.data.voxel.VoxelData;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.Globals;
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
 * A panel that provides a voxel selection
 */
public class VoxelSelectionPanel {

    //text input
    static final int TEXT_INPUT_HEIGHT = 50;
    static final int TEXT_INPUT_WIDTH = 200;

    //single voxel button
    static final int VOXEL_BUTTON_WIDTH = 90;
    static final int VOXEL_BUTTON_HEIGHT = 90;
    static final int VOXEL_BUTTON_TEXTURE_DIM = 70;
    static final int MARGIN_EACH_SIDE = 5;

    //voxel selection
    static final int VOXEL_SCROLLABLE_WIDTH = VOXEL_BUTTON_WIDTH * 5;
    static final int VOXEL_SCROLLABLE_HEIGHT = VOXEL_BUTTON_HEIGHT * 5;

    /**
     * The color of the select voxel type
     */
    static final Vector4f ELEMENT_COLOR_SELECTED = new Vector4f(1,0,0,1);
    

    /**
     * Creates the level editor side panel top view
     * @return
     */
    public static Div createVoxelTypeSelectionPanel(Consumer<VoxelType> onSelectType){
        //setup window
        Div rVal = Div.createDiv();
        rVal.setAlignContent(YogaAlignment.Center);
        rVal.setAlignItems(YogaAlignment.Center);
        rVal.setJustifyContent(YogaJustification.Center);
        rVal.setFlexDirection(YogaFlexDirection.Column);

        //scrollable that contains all the voxel types
        VirtualScrollable scrollable = new VirtualScrollable(VOXEL_SCROLLABLE_WIDTH, VOXEL_SCROLLABLE_HEIGHT);
        scrollable.setFlexDirection(YogaFlexDirection.Column);
        scrollable.setAlignItems(YogaAlignment.Start);

        //search input
        TextInput searchInput = TextInput.createTextInput();
        searchInput.setWidth(TEXT_INPUT_WIDTH);
        searchInput.setMinWidth(TEXT_INPUT_WIDTH);
        searchInput.setMinHeight(20);
        searchInput.setOnPress(new KeyboardEventCallback() {public boolean execute(KeyboardEvent event){
            boolean rVal = searchInput.defaultKeyHandling(event);
            VoxelSelectionPanel.fillInVoxelSelectors(scrollable, searchInput.getText(), onSelectType);
            return rVal;
        }});
        rVal.addChild(searchInput);


        //attach scrollable after search input for organzation purposes
        rVal.addChild(scrollable);

        //final step
        VoxelSelectionPanel.fillInVoxelSelectors(scrollable, searchInput.getText(), onSelectType);
        
        return rVal;
    }

    /**
     * Fills in the voxels to display based on the contents of the search string
     * @param scrollable the scrollable to drop selection buttons in to
     * @param searchString the string to search based on
     */
    static void fillInVoxelSelectors(VirtualScrollable scrollable, String searchString, Consumer<VoxelType> onSelectType){
        Element containingWindow = null;
        if(Globals.elementService.getWindow(WindowStrings.VOXEL_TYPE_SELECTION) != null){
            containingWindow = Globals.elementService.getWindow(WindowStrings.VOXEL_TYPE_SELECTION);
        } else if(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN) != null){
            containingWindow = Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN);
        }
        YogaUtils.refreshComponent(containingWindow, () -> {
            scrollable.clearChildren();
            VoxelData voxelData = Globals.gameConfigCurrent.getVoxelData();
            List<VoxelType> matchingVoxels = voxelData.getTypes().stream().filter((type)->type.getName().toLowerCase().contains(searchString.toLowerCase())).toList();
            Div currentRow = null;
            int incrementer = 0;
            //generate voxel buttons
            for(VoxelType type : matchingVoxels){
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
                newButton.setMinWidth(VOXEL_BUTTON_WIDTH);
                newButton.setMinHeight(VOXEL_BUTTON_HEIGHT);
                //margin
                newButton.setMarginBottom(MARGIN_EACH_SIDE);
                newButton.setMarginLeft(MARGIN_EACH_SIDE);
                newButton.setMarginRight(MARGIN_EACH_SIDE);
                newButton.setMarginTop(MARGIN_EACH_SIDE);
                //set color if this is the selected voxel type
                if(type == Globals.clientState.clientSelectedVoxelType){
                    newButton.setColor(ELEMENT_COLOR_SELECTED);
                }
                //label
                Label voxelLabel = Label.createLabel(type.getName());
                //icon/model
                ImagePanel texturePanel = ImagePanel.createImagePanel(type.getTexture());
                if(type.getTexture() != null){
                    Globals.assetManager.addTexturePathtoQueue(type.getTexture());
                }
                texturePanel.setWidth(VOXEL_BUTTON_TEXTURE_DIM);
                texturePanel.setHeight(VOXEL_BUTTON_TEXTURE_DIM);
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
                newButton.addChild(voxelLabel);
                newButton.setOnClick(new ClickEventCallback() {public boolean execute(ClickEvent event){
                    //set voxel type to this type
                    onSelectType.accept(type);
                    Globals.clientState.clientSelectedVoxelType = type;
                    VoxelSelectionPanel.fillInVoxelSelectors(scrollable, searchString, onSelectType);
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
