package electrosphere.data.entity.item.source;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.data.Config;
import electrosphere.data.crafting.RecipeData;
import electrosphere.data.crafting.RecipeDataMap;
import electrosphere.data.crafting.RecipeIngredientData;
import electrosphere.data.entity.common.CommonEntityTokens;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.common.life.loot.LootPool;
import electrosphere.data.entity.common.life.loot.LootTicket;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.foliage.FoliageTypeLoader;
import electrosphere.data.entity.item.Item;
import electrosphere.data.entity.item.ItemDataMap;

/**
 * Map of items to the methods to source them
 */
public class ItemSourcingMap {

    /**
     * Map of item id -> sourcing data
     */
    private Map<String,ItemSourcingData> itemSourcingDataMap = new HashMap<String,ItemSourcingData>();
    
    /**
     * Parses a sourcing map from a given config
     * @param config The config
     * @return The sourcing map
     */
    public static ItemSourcingMap parse(Config config){
        ItemSourcingMap sourcingMap = new ItemSourcingMap();

        RecipeDataMap recipeMap = config.getRecipeMap();
        ItemDataMap itemMap = config.getItemMap();
        FoliageTypeLoader foliageMap = config.getFoliageMap();

        //structures that store sources
        List<RecipeData> recipes;
        List<CommonEntityType> harvestTargets;
        List<FoliageType> trees;

        //iterate through each item and find where they can be sources
        for(Item item : itemMap.getTypes()){
            //construct new lists for each item type
            recipes = new LinkedList<RecipeData>();
            harvestTargets = new LinkedList<CommonEntityType>();
            trees = new LinkedList<FoliageType>();
            //check if any recipes can create this item
            for(RecipeData recipe : recipeMap.getTypes()){
                for(RecipeIngredientData product : recipe.getProducts()){
                    if(product.getItemType().equals(item.getId())){
                        recipes.add(recipe);
                        break;
                    }
                }
            }

            //check if any common objects can from this item as loot
            for(CommonEntityType foliageEnt : foliageMap.getTypes()){
                if(foliageEnt.getTokens() == null){
                    continue;
                }
                if(!foliageEnt.getTokens().contains(CommonEntityTokens.TOKEN_HARVESTABLE)){
                    continue;
                }
                if(foliageEnt.getHealthSystem() != null){
                    if(foliageEnt.getHealthSystem().getLootPool() != null){
                        LootPool lootPool = foliageEnt.getHealthSystem().getLootPool();
                        for(LootTicket ticket : lootPool.getTickets()){
                            if(ticket.getItemId().equals(item.getId())){
                                harvestTargets.add(foliageEnt);
                                break;
                            }
                        }
                    }
                }
            }

            //check if any trees can drop the item
            for(FoliageType foliage : foliageMap.getTypes()){
                if(foliage.getTokens() != null && foliage.getTokens().contains(CommonEntityTokens.TOKEN_TREE)){
                    if(foliage.getHealthSystem() != null){
                        if(foliage.getHealthSystem().getLootPool() != null){
                            LootPool lootPool = foliage.getHealthSystem().getLootPool();
                            for(LootTicket ticket : lootPool.getTickets()){
                                if(ticket.getItemId().equals(item.getId())){
                                    trees.add(foliage);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            //store the sourcing data
            ItemSourcingData sourcingData = new ItemSourcingData(item.getId(), recipes, harvestTargets, trees);
            sourcingMap.itemSourcingDataMap.put(item.getId(),sourcingData);
        }


        return sourcingMap;
    }

    /**
     * Gets the sourcing data for a given item type
     * @param itemId The item type
     * @return The sourcing data for that type of item
     */
    public ItemSourcingData getSourcingData(String itemId){
        return itemSourcingDataMap.get(itemId);
    }

    /**
     * Gets the sourcing data for a given item type
     * @param item The item data
     * @return The sourcing data for that type of item
     */
    public ItemSourcingData getSourcingData(Item item){
        return itemSourcingDataMap.get(item.getId());
    }

}
