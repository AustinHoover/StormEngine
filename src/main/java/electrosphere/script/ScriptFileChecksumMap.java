package electrosphere.script;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage for the script file -> checksum map
 */
public class ScriptFileChecksumMap {

    /**
     * Stores all loaded files' md5 checksums
     */
    Map<String,String> fileChecksumMap = new HashMap<String,String>();

    /**
     * Stores all loaded files' last modified time
     */
    Map<String,String> fileModifyTimeMap = new HashMap<String,String>();

    /**
     * Gets the file checksum map
     * @return The map
     */
    public Map<String, String> getFileChecksumMap() {
        return fileChecksumMap;
    }

    /**
     * Gets the file last modify map
     * @return The map
     */
    public Map<String, String> getFileLastModifyMap() {
        return fileModifyTimeMap;
    }
    
}
