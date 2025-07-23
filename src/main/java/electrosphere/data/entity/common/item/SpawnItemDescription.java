package electrosphere.data.entity.common.item;

import electrosphere.data.crafting.RecipeData;
import electrosphere.data.entity.graphics.GraphicsTemplate;

/**
 * Describes an automatically generated item definition for an item that can spawn this object
 */
public class SpawnItemDescription {

    /**
     * The icon for the item
     */
    String itemIcon;

    /**
     * The graphics template for the generated item
     */
    GraphicsTemplate graphicsTemplate;

    /**
     * The recipe to create the spawn item
     */
    RecipeData recipe;

    /**
     * The maximum stack of this item
     */
    Integer maxStack;

    /**
     * Gets the item icon for this spawn item
     * @return The item icon
     */
    public String getItemIcon() {
        return itemIcon;
    }

    /**
     * Gets the graphics template for this spawn item
     * @return The graphics template
     */
    public GraphicsTemplate getGraphicsTemplate() {
        return graphicsTemplate;
    }

    /**
     * Gets the recipe data
     * @return The recipe data
     */
    public RecipeData getRecipeData(){
        return recipe;
    }

    /**
     * Gets the maximum allowed stack of this item
     * @return The maximum allowed stack
     */
    public Integer getMaxStack(){
        return maxStack;
    }
    

}
