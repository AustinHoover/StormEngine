package electrosphere.data.crafting;

import java.util.List;

/**
 * A file containing crafting recipe data
 */
public class RecipeDataFile {

    /**
     * The recipe data in this file
     */
    List<RecipeData> recipes;

    /**
     * All child files of this one
     */
    List<String> files;

    /**
     * Gets the recipe data in this file
     * @return The recipe data in this file
     */
    public List<RecipeData> getRecipes() {
        return recipes;
    }

    /**
     * Sets the recipe data in this file
     * @param recipes The recipe data in this file
     */
    public void setRecipes(List<RecipeData> recipes) {
        this.recipes = recipes;
    }

    /**
     * Gets all child files of this one
     * @return All child files of this one
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * Sets all child files of this one
     * @param files All child files of this one
     */
    public void setFiles(List<String> files) {
        this.files = files;
    }

    
    
}
