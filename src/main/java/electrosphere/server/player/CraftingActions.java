package electrosphere.server.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.data.crafting.RecipeData;
import electrosphere.data.crafting.RecipeIngredientData;
import electrosphere.data.entity.item.Item;
import electrosphere.data.entity.item.ItemDataMap;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.state.item.ServerChargeState;

/**
 * Actions around crafting
 */
public class CraftingActions {
    
    /**
     * Attempts to craft an item
     * @param crafter The entity performing the crafting
     * @param station The (optional) station for crafting
     * @param recipe The recipe to craft
     */
    public static void attemptCraft(Entity crafter, Entity station, RecipeData recipe){
        if(ServerInventoryState.getServerInventoryState(crafter) == null){
            return;
        }
        UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(crafter);
        RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(crafter);

        //get data obj
        ItemDataMap itemMap = Globals.gameConfigCurrent.getItemMap();

        //get reagents
        List<Entity> reagentList = new LinkedList<Entity>();
        boolean hasReagents = true;
        Map<String,Integer> ingredientTargetMap = new HashMap<String,Integer>();
        Map<String,Integer> ingredientAccretionMap = new HashMap<String,Integer>();
        //find the reagents we're going to use to craft
        for(RecipeIngredientData ingredient : recipe.getIngredients()){
            ingredientTargetMap.put(ingredient.getItemType(),ingredient.getCount());
            ingredientAccretionMap.put(ingredient.getItemType(),0);
            int found = 0;
            if(naturalInventory != null){
                for(Entity itemEnt : naturalInventory.getItems()){
                    if(itemMap.getItem(itemEnt).getId().matches(ingredient.getItemType())){
                        if(ServerChargeState.hasServerChargeState(itemEnt)){
                            ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(itemEnt);
                            found = found + serverChargeState.getCharges();
                        } else {
                            found++;
                        }
                        reagentList.add(itemEnt);
                    }
                    if(found >= ingredient.getCount()){
                        break;
                    }
                }
            }
            if(toolbarInventory != null){
                for(Entity itemEnt : toolbarInventory.getItems()){
                    if(itemEnt == null){
                        continue;
                    }
                    if(itemMap.getItem(itemEnt).getId().matches(ingredient.getItemType())){
                        if(ServerChargeState.hasServerChargeState(itemEnt)){
                            ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(itemEnt);
                            found = found + serverChargeState.getCharges();
                        } else {
                            found++;
                        }
                        reagentList.add(itemEnt);
                    }
                    if(found >= ingredient.getCount()){
                        break;
                    }
                }
            }
            if(found < ingredient.getCount()){
                hasReagents = false;
                break;
            }
        }
        if(hasReagents){
            for(Entity reagentEnt : reagentList){

                //determine how many we need to still remove
                Item itemData = itemMap.getItem(reagentEnt);
                int targetToRemove = ingredientTargetMap.get(itemData.getId());
                int alreadyFound = ingredientAccretionMap.get(itemData.getId());
                if(alreadyFound > targetToRemove){
                    throw new Error("Removed too many ingredients! " + targetToRemove + " " + alreadyFound);
                } else if(alreadyFound == targetToRemove){
                    continue;
                }


                if(ServerChargeState.hasServerChargeState(reagentEnt)){
                    ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(reagentEnt);
                    int available = serverChargeState.getCharges();

                    if(available > targetToRemove - alreadyFound){
                        int chargesToRemove = targetToRemove - alreadyFound;
                        serverChargeState.attemptAddCharges(-chargesToRemove);
                        //just remove charges
                        ingredientAccretionMap.put(itemData.getId(),alreadyFound + chargesToRemove);
                    } else {
                        //remove item entirely (consuming all charges)
                        if(naturalInventory != null){
                            naturalInventory.removeItem(reagentEnt);
                        }
                        if(toolbarInventory != null){
                            toolbarInventory.tryRemoveItem(reagentEnt);
                        }
                        ServerInventoryState.serverDestroyInventoryItem(reagentEnt);
                        ingredientAccretionMap.put(itemData.getId(),alreadyFound + available);
                    }
                } else {
                    if(naturalInventory != null){
                        naturalInventory.removeItem(reagentEnt);
                    }
                    if(toolbarInventory != null){
                        toolbarInventory.tryRemoveItem(reagentEnt);
                    }
                    ServerInventoryState.serverDestroyInventoryItem(reagentEnt);
                    ingredientAccretionMap.put(itemData.getId(),alreadyFound + 1);
                }
            }
            for(RecipeIngredientData product : recipe.getProducts()){
                Item productType = itemMap.getItem(product.getItemType());
                if(productType == null){
                    throw new Error("Could not locate product definition! " + productType + " " + product.getItemType());
                }
                ServerInventoryState.serverCreateInventoryItem(crafter, product.getItemType(), product.getCount());
            }
        }
    }

    /**
     * Checks if the item can be crafted
     * @param crafter The entity performing the crafting
     * @param workstation The workstation to perform the crafting at
     * @param recipe The recipe to craft
     * @return true if it can perform the recipe, false otherwise
     */
    public static boolean canCraft(Entity crafter, Entity workstation, RecipeData recipe){
        if(ServerInventoryState.getServerInventoryState(crafter) == null){
            return false;
        }
        UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(crafter);
        RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(crafter);

        //get data obj
        ItemDataMap itemMap = Globals.gameConfigCurrent.getItemMap();

        //get reagents
        List<Entity> reagentList = new LinkedList<Entity>();
        boolean hasReagents = true;
        Map<String,Integer> ingredientTargetMap = new HashMap<String,Integer>();
        Map<String,Integer> ingredientAccretionMap = new HashMap<String,Integer>();
        //find the reagents we're going to use to craft
        for(RecipeIngredientData ingredient : recipe.getIngredients()){
            ingredientTargetMap.put(ingredient.getItemType(),ingredient.getCount());
            ingredientAccretionMap.put(ingredient.getItemType(),0);
            int found = 0;
            if(naturalInventory != null){
                for(Entity itemEnt : naturalInventory.getItems()){
                    if(itemMap.getItem(itemEnt).getId().matches(ingredient.getItemType())){
                        if(ServerChargeState.hasServerChargeState(itemEnt)){
                            ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(itemEnt);
                            found = found + serverChargeState.getCharges();
                        } else {
                            found++;
                        }
                        reagentList.add(itemEnt);
                    }
                    if(found >= ingredient.getCount()){
                        break;
                    }
                }
            }
            if(toolbarInventory != null){
                for(Entity itemEnt : toolbarInventory.getItems()){
                    if(itemEnt == null){
                        continue;
                    }
                    if(itemMap.getItem(itemEnt).getId().matches(ingredient.getItemType())){
                        if(ServerChargeState.hasServerChargeState(itemEnt)){
                            ServerChargeState serverChargeState = ServerChargeState.getServerChargeState(itemEnt);
                            found = found + serverChargeState.getCharges();
                        } else {
                            found++;
                        }
                        reagentList.add(itemEnt);
                    }
                    if(found >= ingredient.getCount()){
                        break;
                    }
                }
            }
            if(found < ingredient.getCount()){
                hasReagents = false;
                break;
            }
        }

        return hasReagents;
    }

}
