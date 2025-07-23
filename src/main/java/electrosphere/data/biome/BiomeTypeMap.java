package electrosphere.data.biome;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import electrosphere.util.FileUtils;

/**
 * Structure for efficiently accessing biome data
 */
public class BiomeTypeMap {
    
    /**
     * The map of biome id -> biome data
     */
    Map<String,BiomeData> idBiomeMap = new HashMap<String,BiomeData>();

    /**
     * The map of index -> biome data
     */
    Map<Integer,BiomeData> indexBiomeMap = new HashMap<Integer,BiomeData>();

    /**
     * The map of biome data -> index
     */
    Map<BiomeData,Integer> biomeIndexMap = new HashMap<BiomeData,Integer>();

    /**
     * The list of surface biomes
     */
    List<BiomeData> surfaceBiomes = new LinkedList<BiomeData>();

    /**
     * The list of sky biomes
     */
    List<BiomeData> skyBiomes = new LinkedList<BiomeData>();

    /**
     * The list of subterranean biomes
     */
    List<BiomeData> subterraneanBiomes = new LinkedList<BiomeData>();
    

    /**
     * Adds biome data to the loader
     * @param name The id of the biome
     * @param type The biome data
     */
    public void putBiome(String id, BiomeData biome){
        idBiomeMap.put(id,biome);
        if(biome.isSurface()){
            this.surfaceBiomes.add(biome);
            biome.getSurfaceGenerationParams().precomputeSurfaceDistribution();
        }
        if(biome.isAerial()){
            this.skyBiomes.add(biome);
        }
        if(biome.isSubterranean()){
            this.subterraneanBiomes.add(biome);
        }
        int index = indexBiomeMap.size();
        indexBiomeMap.put(index,biome);
        biomeIndexMap.put(biome,index);
    }

    /**
     * Gets biome data from the id of the biome
     * @param id The id of the biome
     * @return The biome data if it exists, null otherwise
     */
    public BiomeData getType(String id){
        return idBiomeMap.get(id);
    }

    /**
     * Gets the collection of all biome data
     * @return the collection of all biome data
     */
    public Collection<BiomeData> getTypes(){
        return idBiomeMap.values();
    }

    /**
     * Gets the set of all biome data id's stored in the loader
     * @return the set of all biome data ids
     */
    public Set<String> getTypeIds(){
        return idBiomeMap.keySet();
    }

    /**
     * Reads a child biome defintion file
     * @param filename The filename
     * @return The list of biomes in the file
     */
    static List<BiomeData> recursiveReadBiomeLoader(String filename){
        List<BiomeData> typeList = new LinkedList<BiomeData>();
        BiomeDataFile loaderFile = FileUtils.loadObjectFromAssetPath(filename, BiomeDataFile.class);
        //push the types from this file
        for(BiomeData type : loaderFile.getBiomes()){
            typeList.add(type);
        }
        //push types from any other files
        for(String filepath : loaderFile.getFiles()){
            List<BiomeData> parsedTypeList = recursiveReadBiomeLoader(filepath);
            for(BiomeData type : parsedTypeList){
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
     * Loads all biome definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The biome defintion interface
     */
    public static BiomeTypeMap loadBiomeFile(String initialPath) {
        BiomeTypeMap rVal = new BiomeTypeMap();
        List<BiomeData> typeList = recursiveReadBiomeLoader(initialPath);
        for(BiomeData biome : typeList){
            rVal.putBiome(biome.getId(), biome);
        }
        return rVal;
    }

    /**
     * Gets the list of surface biomes
     * @return The list of surface biomes
     */
    public List<BiomeData> getSurfaceBiomes(){
        return this.surfaceBiomes;
    }

    /**
     * Gets the list of sky biomes
     * @return The list of sky biomes
     */
    public List<BiomeData> getSkyBiomes(){
        return this.skyBiomes;
    }

    /**
     * Gets the list of subterranean biomes
     * @return The list of subterranean biomes
     */
    public List<BiomeData> getSubterraneanBiomes(){
        return this.subterraneanBiomes;
    }

    /**
     * Gets the biome by its index
     * @param index The index
     * @return The biome if the index exists, null otherwise
     */
    public BiomeData getBiomeByIndex(int index){
        return indexBiomeMap.get(index);
    }

    /**
     * Gets the index of a given biome
     * @param biome The biome
     * @return The index of the biome
     */
    public Integer getIndexOfBiome(BiomeData biome){
        return biomeIndexMap.get(biome);
    }

}
