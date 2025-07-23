package electrosphere.data.crafting;

/**
 * An ingredient in a recipe
 */
public class RecipeIngredientData {
    
    /**
     * The type of item for the recipe
     */
    String itemType;

    /**
     * The count of the item required for the recipe
     */
    int count;

    /**
     * Creates a recipe ingredient definition
     * @param type The type of item
     * @param count The count of that item
     */
    public RecipeIngredientData(String type, int count){
        this.itemType = type;
        this.count = count;
    }

    /**
     * Gets the type of item for the recipe
     * @return The type of item for the recipe
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * Sets the type of item for the recipe
     * @param itemType The type of item for the recipe
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    /**
     * Gets the count of the item required for the recipe
     * @return The count of the item required for the recipe
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count of the item required for the recipe
     * @param count The count of the item required for the recipe
     */
    public void setCount(int count) {
        this.count = count;
    }

    

}
