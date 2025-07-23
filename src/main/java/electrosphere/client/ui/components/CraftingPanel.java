package electrosphere.client.ui.components;

import java.util.function.Consumer;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.data.crafting.RecipeData;
import electrosphere.data.crafting.RecipeIngredientData;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.VirtualScrollable;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.events.FocusEvent;

/**
 * The crafting panel
 */
public class CraftingPanel {

    /**
     * The title of the panel
     */
    public static final String PANEL_TITLE = "Crafting";


    /**
     * The width of the recipe scrollable
     */
    public static final int SCROLLABLE_WIDTH = 500;

    /**
     * The height of the recipe scrollable
     */
    public static final int SCROLLABLE_HEIGHT = 800;

    /**
     * The width of the details section
     */
    public static final int DETAILS_WIDTH = 500;

    /**
     * The height of the spacer on the details panel
     */
    public static final int DETAILS_SPACER_HEIGHT = 10;

    /**
     * The selected recipe
     */
    private static RecipeData selectedRecipe = null;

    
    /**
     * Creates the crafting panel component
     * @param craftingTag The tag to filter recipes by
     * @param onCraft Called when an item is crafted
     * @return The component
     */
    public static Element createCraftingPanelComponent(String craftingTag, Consumer<RecipeData> onCraft){

        //top level element
        Div rVal = Div.createCol();


        //title label
        Label titleLabel = Label.createLabel(PANEL_TITLE);
        titleLabel.setMarginBottom(DETAILS_SPACER_HEIGHT);



        //details about the current recipe selected
        Div recipeDetailsSection = Div.createDiv();
        recipeDetailsSection.setWidth(DETAILS_WIDTH);
        recipeDetailsSection.setHeight(SCROLLABLE_HEIGHT);



        //select the first recipe
        selectedRecipe = Globals.gameConfigCurrent.getRecipeMap().getTypes().iterator().next();
        CraftingPanel.setDetails(rVal, recipeDetailsSection, selectedRecipe);



        //the scrollable containing the list of recipes to select
        VirtualScrollable recipeScrollable = new VirtualScrollable(SCROLLABLE_WIDTH, SCROLLABLE_HEIGHT);
        Globals.gameConfigCurrent.getRecipeMap().getTypes()
        .stream()
        .filter(recipe -> recipe.getCraftingTag().matches(craftingTag))
        .forEach((RecipeData recipe) -> {
            Button recipeButton = Button.createButton(recipe.getDisplayName(), () -> {
                CraftingPanel.setDetails(rVal, recipeDetailsSection, recipe);
                selectedRecipe = recipe;
            });
            recipeButton.setOnFocus((FocusEvent event) -> {
                CraftingPanel.setDetails(rVal, recipeDetailsSection, recipe);
                selectedRecipe = recipe;
            });
            recipeButton.setMarginTop(DETAILS_SPACER_HEIGHT);
            recipeButton.setMarginLeft(DETAILS_SPACER_HEIGHT);
            recipeScrollable.addChild(recipeButton);
        });



        //the button to actually craft
        Button craftButton = Button.createButton("Craft", () -> {
            onCraft.accept(selectedRecipe);
        });
        Div buttonRow = Div.createRow(
            craftButton
        );



        //actually layout the panel
        rVal.addChild(titleLabel);
        rVal.addChild(Div.createRow(
            recipeScrollable,
            recipeDetailsSection
        ));
        rVal.addChild(buttonRow);
        rVal.setAlignItems(YogaAlignment.Center);

        return rVal;
    }

    /**
     * Sets the details panel when a recipe is selected
     * @param detailsPanel The details panel
     * @param currentRecipe The recipe that was selected
     */
    private static void setDetails(Div topLevelEl, Div detailsPanel, RecipeData currentRecipe){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,() -> {
            //clear the panel
            detailsPanel.clearChildren();

            //ingredients
            detailsPanel.addChild(Label.createLabel("Ingredients"));
            currentRecipe.getIngredients().forEach((RecipeIngredientData ingredient) -> {
                Label label = Label.createLabel(ingredient.getItemType());
                label.setMarginTop(5);
                label.setMarginLeft(15);
                detailsPanel.addChild(label);
            });

            //spacer
            Div spacerDiv = Div.createDiv();
            spacerDiv.setMarginBottom(DETAILS_SPACER_HEIGHT);
            detailsPanel.addChild(spacerDiv);

            //products
            detailsPanel.addChild(Label.createLabel("Products"));
            currentRecipe.getProducts().forEach((RecipeIngredientData ingredient) -> {
                Label label = Label.createLabel(ingredient.getItemType());
                label.setMarginTop(5);
                label.setMarginLeft(15);
                detailsPanel.addChild(label);
            });

            //apply yoga
            if(Globals.elementService.getWindow(WindowStrings.CRAFTING) != null){
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, Globals.elementService.getWindow(WindowStrings.CRAFTING));
            } else if(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN) != null){
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN));
            }
        });
    }

}
