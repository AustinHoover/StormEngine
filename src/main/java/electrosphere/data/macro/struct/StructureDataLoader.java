package electrosphere.data.macro.struct;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import electrosphere.data.block.fab.BlockFab;
import electrosphere.util.FileUtils;

/**
 * The structure type loader
 */
public class StructureDataLoader {

     /**
     * The map of structure id -> structure data
     */
    Map<String,StructureData> idTypeMap = new HashMap<String,StructureData>();

    /**
     * Map of java file object -> structure data file at that path
     */
    Map<File,StructureDataFile> fileMap = new HashMap<File,StructureDataFile>();

    /**
     * Adds structure data to the loader
     * @param name The id of the structure
     * @param type The structure data
     */
    public void putType(String name, StructureData type){
        idTypeMap.put(name,type);
    }

    /**
     * Gets structure data from the id of the type
     * @param id The id of the type
     * @return The structure data if it exists, null otherwise
     */
    public StructureData getType(String id){
        return idTypeMap.get(id);
    }

    /**
     * Gets the collection of all structure data
     * @return the collection of all structure data
     */
    public Collection<StructureData> getTypes(){
        return idTypeMap.values();
    }

    /**
     * Gets the set of all structure data id's stored in the loader
     * @return the set of all structure data ids
     */
    public Set<String> getTypeIds(){
        return idTypeMap.keySet();
    }

    /**
     * Reads a child structure defintion file
     * @param loader The loader that is loading all the files
     * @param filename The filename
     * @return The list of structure in the file
     */
    static List<StructureData> recursiveReadStructureLoader(StructureDataLoader loader, String filename){
        List<StructureData> typeList = new LinkedList<StructureData>();
        StructureDataFile loaderFile = FileUtils.loadObjectFromAssetPath(filename, StructureDataFile.class);
        loader.fileMap.put(FileUtils.getAssetFile(filename),loaderFile);
        //push the types from this file
        for(StructureData type : loaderFile.getData()){
            typeList.add(type);
        }
        //push types from any other files
        if(loaderFile.getFiles() != null){
            for(String filepath : loaderFile.getFiles()){
                List<StructureData> parsedTypeList = StructureDataLoader.recursiveReadStructureLoader(loader, filepath);
                for(StructureData type : parsedTypeList){
                    typeList.add(type);
                }
            }
        }
        return typeList;
    }

    /**
     * Loads all structure definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The structure defintion interface
     */
    public static StructureDataLoader loadStructureFiles(String initialPath) {
        StructureDataLoader rVal = new StructureDataLoader();
        List<StructureData> typeList = StructureDataLoader.recursiveReadStructureLoader(rVal, initialPath);
        for(StructureData type : typeList){
            rVal.putType(type.getId(), type);
            type.setFab(BlockFab.read(FileUtils.getAssetFile(type.getFabPath())));
        }
        return rVal;
    }

    /**
     * Saves the updated state of the loader
     */
    public void save(){
        for(Entry<File,StructureDataFile> pair : this.fileMap.entrySet()){
            FileUtils.serializeObjectToFilePath(pair.getKey(), pair.getValue());
        }
    }

}
