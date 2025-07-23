package electrosphere.data.entity.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import electrosphere.data.block.BlockData;
import electrosphere.data.block.BlockType;
import electrosphere.data.crafting.RecipeData;
import electrosphere.data.crafting.RecipeDataMap;
import electrosphere.data.entity.common.CommonEntityMap;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.voxel.VoxelData;
import electrosphere.data.voxel.VoxelType;
import electrosphere.entity.Entity;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.util.FileUtils;

/**
 * A structure for efficiently looking up items
 */
public class ItemDataMap {

    /**
     * Default product count for spawn item crafting recipes
     */
    static final int SPAWN_ITEM_DEFAULT_PRODUCT_COUNT = 1;
    
    /**
     * The map of item id -> item data
     */
    Map<String,Item> idItemMap = new HashMap<String,Item>();

    /**
     * Adds item data to the loader
     * @param name The id of the item
     * @param type The item data
     */
    public void putType(String id, Item type){
        if(idItemMap.containsKey(id)){
            throw new Error("Registering item id that already exists! " + id + " " + type + " " + idItemMap.get(id));
        }
        idItemMap.put(id,type);
    }

    /**
     * Gets item data from the id of the item
     * @param id The id of the item
     * @return The item data if it exists, null otherwise
     */
    public Item getType(String id){
        return idItemMap.get(id);
    }

    /**
     * Gets item data from the id of the item
     * @param id The id of the item
     * @return The item data if it exists, null otherwise
     */
    public Item getItem(String id){
        return idItemMap.get(id);
    }

    /**
     * Gets item data from a given entity
     * @param entity The entity to get the item data for
     * @return The item data if it exists, null otherwise
     */
    public Item getItem(Entity entity){
        String itemId = ItemUtils.getType(entity);
        return idItemMap.get(itemId);
    }

    /**
     * Gets the collection of all item data
     * @return the collection of all item data
     */
    public Collection<Item> getTypes(){
        return idItemMap.values();
    }

    /**
     * Gets the set of all item data id's stored in the loader
     * @return the set of all item data ids
     */
    public Set<String> getTypeIds(){
        return idItemMap.keySet();
    }

    /**
     * Reads a child item defintion file
     * @param filename The filename
     * @return The list of items in the file
     */
    static List<Item> recursiveReadItemLoader(String filename){
        List<Item> typeList = new LinkedList<Item>();
        ItemDataFile loaderFile = FileUtils.loadObjectFromAssetPath(filename, ItemDataFile.class);
        //push the types from this file
        for(Item type : loaderFile.getItems()){
            typeList.add(type);
        }
        //push types from any other files
        for(String filepath : loaderFile.getFiles()){
            List<Item> parsedTypeList = recursiveReadItemLoader(filepath);
            for(Item type : parsedTypeList){
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
     * Loads all item definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The item defintion interface
     */
    public static ItemDataMap loadItemFiles(String initialPath) {
        ItemDataMap rVal = new ItemDataMap();
        List<Item> typeList = recursiveReadItemLoader(initialPath);
        for(Item type : typeList){
            rVal.putType(type.getId(), type);
        }
        return rVal;
    }

    /**
     * Loads all furnitures as items
     * @param itemDataMap The item data map
     * @param objectMap The object map that contains furniture
     */
    public static void loadSpawnItems(ItemDataMap itemDataMap, RecipeDataMap recipeMap, CommonEntityMap objectMap){
        for(CommonEntityType objectData : objectMap.getTypes()){
            if(objectData.getSpawnItem() != null){
                Item spawnItem = Item.createSpawnItem(objectData);
                //create spawn items
                itemDataMap.putType(spawnItem.getId(), spawnItem);
                //add recipe if present
                if(objectData.getSpawnItem().getRecipeData() != null){
                    RecipeData recipeData = RecipeData.createSpawnItemRecipe(objectData.getSpawnItem().getRecipeData(), spawnItem, ItemDataMap.SPAWN_ITEM_DEFAULT_PRODUCT_COUNT);
                    recipeMap.registerRecipe(recipeData);
                }
            }
        }
    }

    /**
     * Loads all block types as items
     * @param itemDataMap The item data map
     * @param blockData The data on all block types
     */
    public static void generateBlockItems(ItemDataMap itemDataMap, BlockData blockData){
        for(BlockType blockType : blockData.getTypes()){
            Item spawnItem = Item.createBlockItem(blockType);
            //create spawn items
            itemDataMap.putType(spawnItem.getId(), spawnItem);
        }
    }

    /**
     * Loads all voxel types as items
     * @param itemDataMap The item data map
     * @param voxelData The data on all voxel types
     */
    public static void generateVoxelItems(ItemDataMap itemDataMap, VoxelData voxelData){
        for(VoxelType voxelType : voxelData.getTypes()){
            Item spawnItem = Item.createVoxelItem(voxelType);
            //create spawn items
            itemDataMap.putType(spawnItem.getId(), spawnItem);
        }
    }

}
