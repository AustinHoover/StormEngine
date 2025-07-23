package electrosphere.logger;

import org.graalvm.polyglot.HostAccess.Export;

/**
 * A channel for logging messages
 */
public class Logger {
    
    /**
     * The different logging levels
     */
    public enum LogLevel {
        LOOP_DEBUG, //this should be used for debugging messages that are executed very rapidly/every frame
        DEBUG,
        INFO,
        WARNING,
        ERROR,
    }
    
    /**
     * the level of this log
     */
    private LogLevel level;

    /**
     * The name of the logger
     */
    private String name;
    
    /**
     * Creates a logger channel
     * @param name The name of the logger
     * @param level The level of message to report on this channel
     */
    public Logger(String name, LogLevel level){
        this.name = name;
        this.level = level;
    }

    /**
     * Sets the logging level of this logger
     * @param level The logging level
     */
    public void setLevel(LogLevel level){
        this.level = level;
        new Exception("Changing log level to " + level + "!").printStackTrace();
    }
    
    /**
     * Logs a loop debug message.
     * This should be used for debugging messages that are executed very rapidly/every frame
     * @param message The message to report
     */
    public void DEBUG_LOOP(String message){
        if(level == LogLevel.LOOP_DEBUG){
            System.out.println(message);
        }
    }


    /**
     * Logs a debug message.
     * This should be used for debugging messages that are executed on a given condition that won't necessarily be every loop (ie all network messages)
     * @param message The message to report
     */
    @Export
    public void DEBUG(String message){
        if(level == LogLevel.LOOP_DEBUG || level == LogLevel.DEBUG){
            System.out.println(message);
        }
    }

    /**
     * Logs a debug message.
     * This should be used for debugging messages that are executed on a given condition that won't necessarily be every loop (ie all network messages)
     * @param message The message to report
     * @param e The exception to also log
     */
    public void DEBUG(String message, Exception e){
        if(level == LogLevel.LOOP_DEBUG || level == LogLevel.DEBUG){
            System.out.println(message);
            e.printStackTrace();
        }
    }
    

    /**
     * Logs an info message.
     * This should be used for messages that would have interest to someone running a server (ie specific network messages, account creation, etc)
     * @param message The message to report
     */
    @Export
    public void INFO(String message){
        if(level == LogLevel.LOOP_DEBUG || level == LogLevel.DEBUG || level == LogLevel.INFO){
            System.out.println(message);
        }
    }
    

    /**
     * Logs a warning message.
     * This should be used for reporting events that happen in the engine that are concerning but don't mean the engine has failed to execute (ie a texture failed to load)
     * @param message The message to report
     */
    @Export
    public void WARNING(String message){
        if(level == LogLevel.LOOP_DEBUG || level == LogLevel.DEBUG || level == LogLevel.INFO || level == LogLevel.WARNING){
            System.out.println(message);
        }
    }
    

    /**
     * Logs an error message.
     * This should be used every time we throw any kind of error in the engine
     * @param message The message to report
     */
    public void ERROR(String message, Throwable e){
        if(level == LogLevel.LOOP_DEBUG || level == LogLevel.DEBUG || level == LogLevel.INFO || level == LogLevel.WARNING || level == LogLevel.ERROR){
            System.err.println(message);
            this.ERROR(e);
        }
    }

    /**
     * Logs an error message.
     * This should be used every time we throw any kind of error in the engine
     * @param e The exception to report
     */
    public void ERROR(Throwable e){
        if(level == LogLevel.LOOP_DEBUG || level == LogLevel.DEBUG || level == LogLevel.INFO || level == LogLevel.WARNING || level == LogLevel.ERROR){
            e.printStackTrace();
        }
    }

    /**
     * Prints a message at the specified logging level
     * @param level The logging level
     * @param message The message
     */
    public void PRINT(LogLevel level, String message){
        switch(level){
            case ERROR:{
                this.ERROR(new Exception(message));
            } break;
            case WARNING:{
                this.WARNING(message);
            } break;
            case INFO:{
                this.INFO(message);
            } break;
            case DEBUG:{
                this.DEBUG(message);
            } break;
            case LOOP_DEBUG:{
                this.DEBUG_LOOP(message);
            } break;
        }
    }

    /**
     * Gets the level of the logger
     * @return The level
     */
    public LogLevel getLevel(){
        return this.level;
    }

    /**
     * Gets the name of the logger
     * @return the name
     */
    public String getName(){
        return this.name;
    }

}
