package electrosphere.logger;

import java.util.Arrays;
import java.util.List;

import electrosphere.logger.Logger.LogLevel;

/**
 * The list of logging channels available
 */
public class LoggerInterface {
    
    /**
     * The level to initialize loggers with
     */
    static LogLevel initLevel = LogLevel.WARNING;
    
    public static Logger loggerNetworking;
    public static Logger loggerFileIO;
    public static Logger loggerGameLogic;
    public static Logger loggerRenderer;
    public static Logger loggerEngine;
    public static Logger loggerStartup;
    public static Logger loggerAuth;
    public static Logger loggerDB;
    public static Logger loggerAudio;
    public static Logger loggerUI;
    public static Logger loggerScripts;
    public static Logger loggerAI;
    
    /**
     * Initializes all logic objects
     */
    public static void initLoggers(){
        loggerStartup = new Logger("Startup", initLevel);
        loggerNetworking = new Logger("Networking", initLevel);
        loggerFileIO = new Logger("File IO", initLevel);
        loggerGameLogic = new Logger("Game Logic", initLevel);
        loggerRenderer = new Logger("Renderer", initLevel);
        loggerEngine = new Logger("Engine", initLevel);
        loggerAuth = new Logger("Auth", initLevel);
        loggerDB = new Logger("DB", initLevel);
        loggerAudio = new Logger("Audio", initLevel);
        loggerUI = new Logger("UI", initLevel);
        loggerScripts = new Logger("Scripts", initLevel);
        loggerAI = new Logger("AI", initLevel);
        loggerStartup.INFO("Initialized loggers");
    }

    /**
     * Destroys all loggers
     */
    public static void destroyLoggers(){
        loggerNetworking = null;
        loggerFileIO = null;
        loggerGameLogic = null;
        loggerRenderer = null;
        loggerEngine = null;
        loggerStartup = null;
        loggerAuth = null;
        loggerDB = null;
        loggerAudio = null;
        loggerUI = null;
        loggerScripts = null;
        loggerAI = null;
    }

    /**
     * Gets the list of all loggers
     * @return The list of all loggers
     */
    public static List<Logger> getLoggers(){
        Logger[] loggerList = new Logger[]{
            loggerAI,
            loggerAudio,
            loggerAuth,
            loggerNetworking,
            loggerFileIO,
            loggerGameLogic,
            loggerRenderer,
            loggerEngine,
            loggerStartup,
            loggerDB,
            loggerUI,
            loggerScripts,
        };
        return Arrays.asList(loggerList);
    }

    /**
     * Sets the log level for all loggers
     * @param level The level
     */
    public static void setLogLevel(LogLevel level){
        for(Logger logger : getLoggers()){
            logger.setLevel(level);
        }
    }

    /**
     * Sets the level to initialize loggers with
     * @param level The level
     */
    public static void setInitLogLevel(LogLevel level){
        LoggerInterface.initLevel = level;
    }

}
