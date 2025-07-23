package electrosphere.data.crafting;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import electrosphere.util.FileUtils;

/**
 * A structure for efficiently looking up recipes
 */
public class RecipeDataMap {

    /**
     * Incrementer for assigning IDs to recipes
     */
    static int idIncrementer = 0;
    
    /**
     * The map of recipe id -> recipe data
     */
    Map<Integer,RecipeData> idRecipeMap = new HashMap<Integer,RecipeData>();

    /**
     * Gets recipe data from the id of the recipe
     * @param id The id of the recipe
     * @return The recipe data if it exists, null otherwise
     */
    public RecipeData getType(int id){
        return idRecipeMap.get(id);
    }

    /**
     * Gets the collection of all recipe data
     * @return the collection of all recipe data
     */
    public Collection<RecipeData> getTypes(){
        return idRecipeMap.values();
    }

    /**
     * Gets the set of all recipe data id's stored in the loader
     * @return the set of all recipe data ids
     */
    public Set<Integer> getTypeIds(){
        return idRecipeMap.keySet();
    }

    /**
     * Reads a child recipe defintion file
     * @param filename The filename
     * @return The list of recipes in the file
     */
    static List<RecipeData> recursiveReadRecipeLoader(String filename){
        List<RecipeData> typeList = new LinkedList<RecipeData>();
        RecipeDataFile loaderFile = FileUtils.loadObjectFromAssetPath(filename, RecipeDataFile.class);
        //push the types from this file
        for(RecipeData type : loaderFile.getRecipes()){
            typeList.add(type);
        }
        //push types from any other files
        for(String filepath : loaderFile.getFiles()){
            List<RecipeData> parsedTypeList = RecipeDataMap.recursiveReadRecipeLoader(filepath);
            for(RecipeData type : parsedTypeList){
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
     * Loads all recipe definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The recipe defintion interface
     */
    public static RecipeDataMap loadRecipeFiles(String initialPath) {
        RecipeDataMap rVal = new RecipeDataMap();
        List<RecipeData> typeList = RecipeDataMap.recursiveReadRecipeLoader(initialPath);
        for(RecipeData type : typeList){
            rVal.registerRecipe(type);
        }
        return rVal;
    }

    /**
     * Registers a recipe
     * @param data The recipe
     */
    public void registerRecipe(RecipeData data){
        data.setId(idIncrementer);
        idIncrementer++;
        idRecipeMap.put(data.getId(),data);
    }

}
