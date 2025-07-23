package electrosphere.data.crafting;

import electrosphere.data.Config;

/**
 * Validates recipes
 */
public class RecipeValidator {
    
    /**
     * Validates all recipes in a config
     * @param config The config
     */
    public static void validate(Config config){
        for(RecipeData recipeData : config.getRecipeMap().getTypes()){
            //validate that all reagents are items in the config
            for(RecipeIngredientData reagent : recipeData.getIngredients()){
                if(config.getItemMap().getType(reagent.getItemType()) == null){
                    throw new Error("Item does not exist: " + reagent.getItemType());
                }
            }
            //validate that all products are items in the config
            for(RecipeIngredientData reagent : recipeData.getProducts()){
                if(config.getItemMap().getType(reagent.getItemType()) == null){
                    throw new Error("Item does not exist: " + reagent.getItemType());
                }
            }
        }
    }

}
