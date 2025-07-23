package electrosphere.data.entity.item.source;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import electrosphere.data.crafting.RecipeData;
import electrosphere.data.crafting.RecipeIngredientData;
import electrosphere.data.entity.common.CommonEntityTokens;
import electrosphere.data.entity.item.ItemIdStrings;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.types.item.ItemUtils;

/**
 * A tree of dependencies to acquire a final item
 */
public class ItemSourcingTree {
    
    /**
     * The root item to search for
     */
    String rootItemId;

    /**
     * The map of item id -> specific 
     */
    Map<String,ItemSourcingData> itemSourceMap = new HashMap<String,ItemSourcingData>();

    /**
     * Creates an item sourcing tree
     * @param itemType The type of item to source
     * @return The tree of dependencies to get the item
     */
    public static ItemSourcingTree create(String itemType){
        ItemSourcingTree rVal = new ItemSourcingTree();
        rVal.rootItemId = itemType;
        rVal.evaluate();
        return rVal;
    }

    /**
     * Evaluates what items are still depended upon
     */
    public void evaluate(){
        //all items that haven't had sources solved for yet
        List<String> unsolvedItems = new LinkedList<String>();
        unsolvedItems.add(this.rootItemId);

        while(unsolvedItems.size() > 0){
            String currentId = unsolvedItems.remove(0);
            ItemSourcingData sourcingData = Globals.gameConfigCurrent.getItemSourcingMap().getSourcingData(currentId);
            if(sourcingData.recipes.size() > 0){
                for(RecipeData recipeData : sourcingData.recipes){
                    for(RecipeIngredientData reagent : recipeData.getIngredients()){
                        if(!unsolvedItems.contains(reagent.getItemType()) && !itemSourceMap.containsKey(reagent.getItemType())){
                            unsolvedItems.add(reagent.getItemType());
                        }
                    }
                }
            }
            if(sourcingData.trees.size() > 0){
                unsolvedItems.add(ItemIdStrings.ITEM_STONE_AXE);
            }
            this.itemSourceMap.put(currentId,sourcingData);
        }
    }

    /**
     * Gets the sourcing data for the current dependency
     * @param entity The entity to check
     * @return The sourcing data for the current dependency
     */
    public ItemSourcingData getCurrentDependency(Entity entity){
        List<Entity> items = InventoryUtils.getAllInventoryItems(entity);
        List<String> itemIds = items.stream().map((Entity item) -> {return ItemUtils.getType(item);}).collect(Collectors.toList());
        if(itemIds.contains(this.rootItemId)){
            return this.itemSourceMap.get(this.rootItemId);
        }
        String currentRootId = this.rootItemId;
        return this.getCurrentDependencyRecursive(entity,itemIds,currentRootId);
    }

    /**
     * Gets the sourcing data for the current dependency
     * @param entity The entity to check
     * @param inventoryIds The contents of the entity's inventory
     * @param currentRoot The current root item to search for
     * @return The sourcing data for the current dependency
     */
    private ItemSourcingData getCurrentDependencyRecursive(Entity entity, List<String> inventoryIds, String currentRoot){
        ItemSourcingData sourcingData = this.itemSourceMap.get(currentRoot);
        if(sourcingData == null){
            throw new Error("Failed to find sourcing data for " + currentRoot);
        }
        if(sourcingData.harvestTargets.size() > 0){
            return sourcingData;
        }
        if(sourcingData.trees.size() > 0){
            //if we don't have an axe in inventory, consider it a dependency
            if(!ServerInventoryState.serverHasTool(entity, CommonEntityTokens.TOKEN_AXE)){
                ItemSourcingData axeSource = this.getCurrentDependencyRecursive(entity, inventoryIds, ItemIdStrings.ITEM_STONE_AXE);
                return axeSource;
            }
            //we have an axe, return this sourcing data
            return sourcingData;
        }
        if(sourcingData.recipes.size() > 0){
            //the ingredient to source if we don't have all ingredients
            ItemSourcingData ingredientToSource = null;

            //whether we have all ingredients already or not
            boolean foundAllIngredients = false;

            //whether the recipe is source-able at all
            boolean recipeIsSourceable = true;

            //the recipe
            RecipeData craftableRecipe = null;
            for(RecipeData recipeData : sourcingData.recipes){
                //check if we have all the ingredients to craft this item
                foundAllIngredients = true;
                recipeIsSourceable = true;
                ingredientToSource = null;
                for(RecipeIngredientData ingredient : recipeData.getIngredients()){
                    ItemSourcingData sourcingForCurrentReagent = this.getCurrentDependencyRecursive(entity, inventoryIds, ingredient.getItemType());
                    if(sourcingForCurrentReagent == null){
                        recipeIsSourceable = false;
                        break;
                    }
                    if(!inventoryIds.contains(ingredient.getItemType())){
                        //ingredient is not in inventory
                        foundAllIngredients = false;
                        ingredientToSource = sourcingForCurrentReagent;
                    }
                }
                if(!recipeIsSourceable){
                    continue;
                }
                if(foundAllIngredients){
                    craftableRecipe = recipeData;
                    break;
                } else {
                    return ingredientToSource;
                }
            }
            if(craftableRecipe != null){
                return sourcingData;
            }
        }
        //unable to source this item
        return null;
    }

    /**
     * Gets the item id of the root item
     * @return The id
     */
    public String getRootItem(){
        return this.rootItemId;
    }

}
