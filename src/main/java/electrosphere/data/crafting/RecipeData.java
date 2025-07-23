package electrosphere.data.crafting;

import java.util.LinkedList;
import java.util.List;

import electrosphere.data.entity.item.Item;

/**
 * Data on a crafting recipe
 */
public class RecipeData {

    /**
     * The id of the recipe
     */
    int id;

    /**
     * The display name for the recipe
     */
    String displayName;

    /**
     * The tag to determine when to display this recipe in the crafting menu
     */
    String craftingTag;
    
    /**
     * The ingredients required for the recipe
     */
    List<RecipeIngredientData> ingredients;

    /**
     * The products produced by the recipe
     */
    List<RecipeIngredientData> products;

    /**
     * Creates a spawn item recipe from an existing recipe and the spawn item definition
     * @param existingRecipe The existing recipe
     * @param spawnItem The spawn item definition
     * @param count The number of the spawn item to create
     * @return The spawn item recipe
     */
    public static RecipeData createSpawnItemRecipe(RecipeData existingRecipe, Item spawnItem, int count){
        RecipeData rVal = new RecipeData();
        rVal.displayName = spawnItem.getDisplayName();
        rVal.craftingTag = existingRecipe.craftingTag;
        rVal.ingredients = existingRecipe.ingredients;
        rVal.products = new LinkedList<RecipeIngredientData>();
        if(existingRecipe.products != null){
            rVal.products.addAll(existingRecipe.products);
        }
        rVal.products.add(new RecipeIngredientData(spawnItem.getId(),count));
        return rVal;
    }

    /**
     * Gets the ingredients required for the recipe
     * @return The ingredients required for the recipe
     */
    public List<RecipeIngredientData> getIngredients() {
        return ingredients;
    }

    /**
     * Sets the ingredients required for the recipe
     * @param ingredients The ingredients required for the recipe
     */
    public void setIngredients(List<RecipeIngredientData> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Gets the products produced by the recipe
     * @return The products produced by the recipe
     */
    public List<RecipeIngredientData> getProducts() {
        return products;
    }

    /**
     * Sets the products produced by the recipe
     * @param products The products produced by the recipe
     */
    public void setProducts(List<RecipeIngredientData> products) {
        this.products = products;
    }

    /**
     * Gets the id of the recipe
     * @return The id of the recipe
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the recipe
     * @param id The id of the recipe
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the display name
     * @return The display name
     */
    public String getDisplayName(){
        return displayName;
    }

    /**
     * Gets the crafting tag
     * @return the crafting tag
     */
    public String getCraftingTag() {
        return craftingTag;
    }

    /**
     * Sets the crafting tag
     * @param craftingTag the crafting tag
     */
    public void setCraftingTag(String craftingTag) {
        this.craftingTag = craftingTag;
    }

    

    

    

}
