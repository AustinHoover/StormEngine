package electrosphere.server.db;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.util.FileUtils;
import java.io.IOException;

/**
 * Utilities for working with the database
 */
public class DatabaseUtils {

    
    /**
     * Initializes the central db file
     * @param path The path to initialize at
     * @return true if it succeeded, false otherwise
     */
    public static boolean initCentralDBFile(String path){
        String sanitizedPath = FileUtils.sanitizeFilePath(path);
        if(!FileUtils.checkFileExists(sanitizedPath)){
            return false;
        }
        String dbFilePath = sanitizedPath + "/central" + DatabaseController.FILE_EXT;
        if(!Globals.serverState.dbController.isConnected()){
            Globals.serverState.dbController.connect(dbFilePath);
        }
        DatabaseUtils.runScript(Globals.serverState.dbController,"createTables.sql");
        //both of these are used for arena mode as well as main game
        Globals.serverState.dbController.disconnect();
        return true;
    }

    /**
     * Runs a script
     * @param controller The controller
     * @param scriptPath The script's path
     * @return true if it succeeds, false otherwise
     */
    public static boolean runScript(DatabaseController controller, String scriptPath){
        String rawScript = "";
        try {
            rawScript = FileUtils.getSQLScriptFileAsString(scriptPath);
        } catch (IOException ex) {
            LoggerInterface.loggerEngine.ERROR("Failure reading create db script", ex);
            return false;
        }
        String[] scriptLines = rawScript.split("\n");
        String accumulatorString = "";
        for(String line : scriptLines){
            if(line.length() > 1 && !line.startsWith("--")){
                if(line.contains(";")){
                    accumulatorString = accumulatorString + line;
                    LoggerInterface.loggerDB.INFO("EXECUTE: " + accumulatorString);
                    controller.executePreparedStatement(accumulatorString);
                    accumulatorString = "";
                } else {
                    accumulatorString = accumulatorString + line;
                }
            }
        }
        return true;
    }


}
