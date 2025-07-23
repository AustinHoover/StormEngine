package electrosphere.client.ui.components;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.TextInput;
import electrosphere.renderer.ui.elements.VirtualScrollable;
import electrosphere.renderer.ui.elementtypes.ClickableElement.ClickEventCallback;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.KeyEventElement.KeyboardEventCallback;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.KeyboardEvent;

/**
 * A panel for selecting an entity to spawn
 */
public class SpawnSelectionPanel {

    //text input
    static final int TEXT_INPUT_HEIGHT = 50;
    static final int TEXT_INPUT_WIDTH = 200;

    //type selection
    static final int SELECTION_SCROLLABLE_WIDTH = 500;
    static final int SELECTION_SCROLLABLE_HEIGHT = 450;

    //single listing
    static final int MARGIN_EACH_SIDE = 5;

    //margin from top for search input
    static final int SEARCH_INPUT_MARGIN = 10;
    static final int SEARCH_INPUT_MIN_HEIGHT = 20;
    
    /**
     * Creates the spawning menu selection panel
     * @return
     */
    public static Div createEntityTypeSelectionPanel(Consumer<CommonEntityType> onSelectType){
        //setup window
        Div rVal = Div.createDiv();
        rVal.setAlignContent(YogaAlignment.Center);
        rVal.setAlignItems(YogaAlignment.Center);
        rVal.setJustifyContent(YogaJustification.Center);
        rVal.setFlexDirection(YogaFlexDirection.Column);

        //scrollable that contains all the voxel types
        VirtualScrollable scrollable = new VirtualScrollable(SELECTION_SCROLLABLE_WIDTH, SELECTION_SCROLLABLE_HEIGHT);
        scrollable.setFlexDirection(YogaFlexDirection.Column);
        scrollable.setAlignItems(YogaAlignment.Start);
        scrollable.setMarginBottom(MARGIN_EACH_SIDE);
        scrollable.setMarginLeft(MARGIN_EACH_SIDE);
        scrollable.setMarginRight(MARGIN_EACH_SIDE);
        scrollable.setMarginTop(MARGIN_EACH_SIDE);

        //search input
        TextInput searchInput = TextInput.createTextInput();
        searchInput.setWidth(TEXT_INPUT_WIDTH);
        searchInput.setMinWidth(TEXT_INPUT_WIDTH);
        searchInput.setMinHeight(SEARCH_INPUT_MIN_HEIGHT);
        searchInput.setMarginTop(SEARCH_INPUT_MARGIN);
        searchInput.setOnPress(new KeyboardEventCallback() {public boolean execute(KeyboardEvent event){
            boolean rVal = searchInput.defaultKeyHandling(event);
            SpawnSelectionPanel.fillInEntitySelectors(scrollable, searchInput.getText(), onSelectType);
            return rVal;
        }});
        rVal.addChild(searchInput);


        //attach scrollable after search input for organzation purposes
        rVal.addChild(scrollable);

        //final step
        SpawnSelectionPanel.fillInEntitySelectors(scrollable, searchInput.getText(), onSelectType);
        
        return rVal;
    }

    /**
     * Fills in the types to display based on the contents of the search string
     * @param scrollable the scrollable to drop selection buttons in to
     * @param searchString the string to search based on
     */
    static void fillInEntitySelectors(VirtualScrollable scrollable, String searchString, Consumer<CommonEntityType> onSelectType){
        scrollable.clearChildren();

        //get relevant types
        List<CommonEntityType> types = new LinkedList<CommonEntityType>();
        types.addAll(Globals.gameConfigCurrent.getCreatureTypeLoader().getTypes());
        types.addAll(Globals.gameConfigCurrent.getFoliageMap().getTypes());
        types.addAll(Globals.gameConfigCurrent.getItemMap().getTypes());
        types.addAll(Globals.gameConfigCurrent.getObjectTypeMap().getTypes());
        types = types.stream().filter((type) -> type.getId().toLowerCase().contains(searchString.toLowerCase())).toList();

        //generate ui elements
        for(CommonEntityType type : types){
            Div containerDiv = Div.createDiv();
            containerDiv.setMinWidthPercent(25.0f);
            scrollable.addChild(containerDiv);

            Button newButton = new Button();
            newButton.setAlignItems(YogaAlignment.Center);
            //margin
            newButton.setMarginBottom(MARGIN_EACH_SIDE);
            newButton.setMarginLeft(MARGIN_EACH_SIDE);
            newButton.setMarginRight(MARGIN_EACH_SIDE);
            newButton.setMarginTop(MARGIN_EACH_SIDE);
            //label
            Label voxelLabel = Label.createLabel(type.getId());
            //button handling
            newButton.addChild(voxelLabel);
            newButton.setOnClick(new ClickEventCallback() {public boolean execute(ClickEvent event){
                //set voxel type to this type
                onSelectType.accept(type);
                Globals.clientState.selectedSpawntype = type;
                return false;
            }});
            containerDiv.addChild(newButton);
        }
    }

}
