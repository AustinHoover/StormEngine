package electrosphere.engine;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import electrosphere.engine.os.OSData;
import electrosphere.engine.os.fs.FileWatcherService;
import electrosphere.engine.service.ServiceManager;
import electrosphere.engine.signal.SignalSystem;
import electrosphere.engine.signal.sync.MainThreadSignalService;
import electrosphere.engine.threads.ThreadManager;
import electrosphere.engine.time.Timekeeper;
import electrosphere.logger.LoggerInterface;
import electrosphere.script.ScriptEngine;

/**
 * State of the engine
 */
public class EngineState {

    /**
     * Java Process ID for this application
     */
    public final RuntimeMXBean jvmData = ManagementFactory.getRuntimeMXBean();

    /**
     * The OS data
     */
    public final OSData osData = new OSData();

    /**
     * The time keeping service
     */
    public Timekeeper timekeeper;
    
    /**
     * The thread manager
     */
    public final ThreadManager threadManager;

    /**
     * The service manager
     */
    public final ServiceManager serviceManager;

    /**
     * The signal system
     */
    public SignalSystem signalSystem;

    /**
     * Service for sending signals to the main thread
     */
    public final MainThreadSignalService mainThreadSignalService;

    /**
     * The scripting engine
     */
    public final ScriptEngine scriptEngine;

    /**
     * The file watcher service
     */
    public final FileWatcherService fileWatcherService;

    /**
     * Engine-wide flags
     */
    public EngineFlags flags = new EngineFlags();

    /**
     * Constructor
     */
    public EngineState(){
        //init loggers
        LoggerInterface.initLoggers();
        LoggerInterface.loggerStartup.INFO("Initialize global variables");

        this.timekeeper = new Timekeeper();
        this.serviceManager = ServiceManager.create();
        this.threadManager = new ThreadManager();
        this.threadManager.init();
        this.signalSystem = (SignalSystem)this.serviceManager.registerService(new SignalSystem());
        this.mainThreadSignalService = (MainThreadSignalService)this.serviceManager.registerService(new MainThreadSignalService());
        this.scriptEngine = (ScriptEngine)this.serviceManager.registerService(new ScriptEngine());
        this.fileWatcherService = (FileWatcherService)this.serviceManager.registerService(new FileWatcherService());
    }

    /**
     * Destroys the engine state
     */
    public void destroy(){
        this.threadManager.close();
        this.serviceManager.destroy();
    }

    /**
     * Engine-wide flags
     */
    public static class EngineFlags {

        /**
         * Run engine in demo mode
         */
        public static boolean RUN_DEMO = false;

        /**
         * Run client
         */
        public static boolean RUN_CLIENT = true;

        /**
         * Run server
         */
        public static boolean RUN_SERVER = true;

        /**
         * glfw session will be created with hidden window
         */
        public static boolean RUN_HIDDEN = false;

        /**
         * Run the audio engine
         */
        public static boolean RUN_AUDIO = true;

        /**
         * Run the script engine
         */
        public static boolean RUN_SCRIPTS = true;

        /**
         * toggles whether physics is run or not
         */
        public static boolean RUN_PHYSICS = true;

        /**
         * toggles whether fluid physics is run or not
         */
        public static boolean RUN_FLUIDS = false;

        /**
         * Garbage Collection
         * 
         * set to true to trigger full GC every frame
         * a full GC includes collecting old generations as well -- likely very laggy!!
         */
        public static boolean EXPLICIT_GC = false;

        /**
         * Engine timing
         */
        public static boolean EXPLICIT_SLEEP = true;

        /**
         * Triggers the engine to shut down
         */
        public static boolean ENGINE_SHUTDOWN_FLAG = false;
        
        /**
         * main debug flag
         * current enables imgui debug menu or not
         */
        public static boolean ENGINE_DEBUG = true;

        /**
         * Controls whether the engine is headless or not
         */
        public static boolean HEADLESS = false;

        /**
         * Controls whether we error check opengl calls or not
         */
        public static boolean ERROR_CHECK_OPENGL = true;

    }

}
