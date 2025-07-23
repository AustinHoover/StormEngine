package electrosphere.data.entity.item.source;

import java.util.List;

import electrosphere.data.crafting.RecipeData;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.foliage.FoliageType;

/**
 * Data that stores how an item can be sources
 */
public class ItemSourcingData {

    /**
     * Types of sources for items
     */
    public static enum SourcingType {
        /**
         * Craft a recipe
         */
        RECIPE,
        /**
         * Harvest from an item
         */
        HARVEST,
        /**
         * Fell a tree
         */
        TREE,
        /**
         * Pick up the item off the floor
         */
        PICKUP,
    }

    /**
     * The goal item type
     */
    String goalItem;
    
    /**
     * The list of recipes that create this item
     */
    List<RecipeData> recipes;

    /**
     * The list of harvesting targets that can drop this item
     */
    List<CommonEntityType> harvestTargets;

    /**
     * The list of trees that can drop this item
     */
    List<FoliageType> trees;

    /**
     * Constructor
     * @param goalItem The item that this object stores sources of (ie if this is rocks, all the source lists should contain ways to get rocks)
     * @param recipes The recipes to source from
     * @param harvestTargets The objects to harvest from
     * @param trees The trees to drop from
     */
    public ItemSourcingData(String goalItem, List<RecipeData> recipes, List<CommonEntityType> harvestTargets, List<FoliageType> trees){
        this.goalItem = goalItem;
        this.recipes = recipes;
        this.harvestTargets = harvestTargets;
        this.trees = trees;
    }

    /**
     * Gets the list of recipes that can produce this item
     * @return The list of recipes
     */
    public List<RecipeData> getRecipes() {
        return recipes;
    }

    /**
     * Gets the list of harvest targets that can drop this item
     * @return The list of harvest targets
     */
    public List<CommonEntityType> getHarvestTargets() {
        return harvestTargets;
    }

    /**
     * Gets the list of trees that can drop this item
     * @return The list of trees
     */
    public List<FoliageType> getTrees() {
        return trees;
    }

    /**
     * Gets the item that this object stores sources of (ie if this is rocks, all the source lists should contain ways to get rocks)
     * @return The item type id
     */
    public String getGoalItem(){
        return goalItem;
    }

    

}
