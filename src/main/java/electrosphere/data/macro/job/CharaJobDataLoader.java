package electrosphere.data.macro.job;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import electrosphere.util.FileUtils;

/**
 * The job type loader
 */
public class CharaJobDataLoader {

     /**
     * The map of job name -> job data
     */
    Map<String,CharaJob> idTypeMap = new HashMap<String,CharaJob>();

    /**
     * Map of java file object -> job data file at that path
     */
    Map<File,CharaJobDataFile> fileMap = new HashMap<File,CharaJobDataFile>();

    /**
     * Adds job data to the loader
     * @param name The id of the job
     * @param type The job data
     */
    public void putType(String name, CharaJob type){
        idTypeMap.put(name,type);
    }

    /**
     * Gets job data from the id of the type
     * @param id The id of the type
     * @return The job data if it exists, null otherwise
     */
    public CharaJob getType(String id){
        return idTypeMap.get(id);
    }

    /**
     * Gets the collection of all job data
     * @return the collection of all job data
     */
    public Collection<CharaJob> getTypes(){
        return idTypeMap.values();
    }

    /**
     * Gets the set of all job data id's stored in the loader
     * @return the set of all job data ids
     */
    public Set<String> getTypeIds(){
        return idTypeMap.keySet();
    }

    /**
     * Reads a child job defintion file
     * @param loader The loader that is loading all the files
     * @param filename The filename
     * @return The list of job in the file
     */
    static List<CharaJob> recursiveReadJobLoader(CharaJobDataLoader loader, String filename){
        List<CharaJob> typeList = new LinkedList<CharaJob>();
        CharaJobDataFile loaderFile = FileUtils.loadObjectFromAssetPath(filename, CharaJobDataFile.class);
        loader.fileMap.put(FileUtils.getAssetFile(filename),loaderFile);
        //push the types from this file
        for(CharaJob type : loaderFile.getData()){
            typeList.add(type);
        }
        //push types from any other files
        if(loaderFile.getFiles() != null){
            for(String filepath : loaderFile.getFiles()){
                List<CharaJob> parsedTypeList = CharaJobDataLoader.recursiveReadJobLoader(loader, filepath);
                for(CharaJob type : parsedTypeList){
                    typeList.add(type);
                }
            }
        }
        return typeList;
    }

    /**
     * Loads all job definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The job defintion interface
     */
    public static CharaJobDataLoader loadJobFiles(String initialPath) {
        CharaJobDataLoader rVal = new CharaJobDataLoader();
        List<CharaJob> typeList = CharaJobDataLoader.recursiveReadJobLoader(rVal, initialPath);
        for(CharaJob type : typeList){
            rVal.putType(type.getName(), type);
        }
        return rVal;
    }

    /**
     * Saves the updated state of the loader
     */
    public void save(){
        for(Entry<File,CharaJobDataFile> pair : this.fileMap.entrySet()){
            FileUtils.serializeObjectToFilePath(pair.getKey(), pair.getValue());
        }
    }

}
