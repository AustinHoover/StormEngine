package electrosphere.engine;

import java.util.concurrent.TimeUnit;

import org.graalvm.polyglot.HostAccess.Export;
import org.lwjgl.glfw.GLFW;
import org.ode4j.ode.OdeHelper;

import electrosphere.audio.AudioEngine;
import electrosphere.client.ui.menu.debug.ImGuiWindowMacros;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.cli.CLIParser;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.engine.signal.SynchronousSignalHandling;
import electrosphere.engine.time.Timekeeper;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.RenderingEngine;
import electrosphere.server.MainServerFunctions;


/**
 * The main class
 */
public class Main {
    
    
    

    
    /**
     * Pauses simulation
     */
    public static final int FRAMESTEP_PAUSE = 0;

    /**
     * Simulates a single frame
     */
    public static final int FRAMESTEP_SINGLE = 1;

    /**
     * Toggles automatic simulation
     */
    public static final int FRAMESTEP_AUTO = 2;

    /**
     * Number of frames to wait before triggering gc again
     */
    public static final int GC_FRAME_FREQUENCY = 15;
        
    
    
    /**
     * Tracks whether the application is running or not
     */
    public static boolean running = true;

    /**
     * Tracks if ode has been initialized or not
     */
    static boolean initOde = false;
    
    /**
     * target amount of time per frame
     */
    public static float targetFrameRate = 60.0f;
    /**
     * Target period per frame
     */
    static float targetFramePeriod = 1.0f/targetFrameRate;

    /**
     * Framestep tracking variable
     */
    static int framestep = 2;

    /**
     * Sets whether to enable the profiler or not
     */
    private static boolean enableProfiler = false;


    /**
     * The initial method of the application
     * @param args CLI args
     */
    public static void main(String args[]){
        
        //
        //
        //     I N I T I A L I Z A T I O N
        //
        //

        Main.startUp(args);
        
        Main.mainLoop();

    }

    /**
     * Starts up the engine
     */
    public static void startUp(){
        Main.startUp(new String[]{
            "Renderer"
        });
    }

    /**
     * Starts up the engine
     * @param args The command line arguments
     */
    public static void startUp(String args[]){
        //parse command line arguments
        CLIParser.parseCLIArgs(args);
        
        //init global variables
        Globals.initGlobals();

        //init scripting engine
        if(EngineState.EngineFlags.RUN_SCRIPTS){
            Globals.engineState.threadManager.start(new LoadingThread(LoadingThreadType.SCRIPT_ENGINE));
        }

        //controls
        if(EngineState.EngineFlags.RUN_CLIENT){
            Main.initControlHandler();
        }

        //init ODE
        if(!initOde){
            OdeHelper.initODE();
            initOde = true;
        }

        //create the drawing context
        if(EngineState.EngineFlags.RUN_CLIENT && !EngineState.EngineFlags.HEADLESS){
            //create opengl context
            Globals.renderingEngine = new RenderingEngine();
            Globals.renderingEngine.createOpenglContext();
            Globals.initDefaultGraphicalResources();
            ImGuiWindowMacros.initImGuiWindows();

            //inits the controls state of the control handler
            Globals.controlHandler.hintUpdateControlState(ControlsState.TITLE_MENU);

            //start initial asset loading
            Globals.engineState.threadManager.start(new LoadingThread(LoadingThreadType.INIT_ASSETS));
        }

        //Sets a hook that fires when the engine process stops
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(LoggerInterface.loggerEngine != null){
                LoggerInterface.loggerEngine.INFO("Shutdown hook!");
            }
        }));
        
        //create the audio context
        Globals.audioEngine = new AudioEngine();
        if(EngineState.EngineFlags.RUN_CLIENT && !EngineState.EngineFlags.HEADLESS && EngineState.EngineFlags.RUN_AUDIO){
            Globals.audioEngine.init();
            Globals.audioEngine.listAllDevices();
            Globals.initDefaultAudioResources();
            // Globals.audioEngine.setGain(0.1f);
        }

        //init timekeeper
        Globals.engineState.timekeeper.init(targetFramePeriod);
        
        //fire off a loading thread for the title menus/screen
        LoggerInterface.loggerStartup.INFO("Fire off loading thread");
        if(EngineState.EngineFlags.RUN_DEMO){
            LoadingThread serverThread = new LoadingThread(LoadingThreadType.DEMO_MENU);
            Globals.engineState.threadManager.start(serverThread);
        } else if(EngineState.EngineFlags.RUN_CLIENT){
            LoadingThread serverThread = new LoadingThread(LoadingThreadType.TITLE_MENU);
            Globals.engineState.threadManager.start(serverThread);
        } else {
            throw new IllegalStateException("Need to add handling for only running server again");
        }
        
        //recapture the screen for rendering
        if(EngineState.EngineFlags.RUN_CLIENT && !EngineState.EngineFlags.HEADLESS){
            LoggerInterface.loggerStartup.INFO("Recapture screen");
            Globals.controlHandler.setRecapture(true);
        }
    }

    /**
     * Runs the main loop indefinitely. Blocks the thread this is called in.
     */
    public static void mainLoop(){
        Main.mainLoop(0);
        Main.shutdown();
    }

    /**
     * Runs the main loop for a specified number of frames.
     * @param maxFrames The number of frames to run for. If 0, will run indefinitely.
     */
    public static void mainLoop(long maxFrames){

        //resets running flag to that we can repeatedly loop (ie in tests)
        running = true;

        double startTime, endTime;

        //main loop
        while (running) {
            //enable profiler control
            if(Main.enableProfiler){
                Globals.profiler.start();
                Main.enableProfiler = false;
            }
            try {

            Globals.profiler.beginRootCpuSample("frame");
            LoggerInterface.loggerEngine.DEBUG_LOOP("Begin Main Loop Frame");

            //
            //Update timekeeper, thread manager, and process all main thread signals
            //
            Globals.engineState.timekeeper.update();
            Globals.engineState.threadManager.update();

            
            
            ///
            ///    A S S E T     M A N A G E R     S T U F F
            ///
            if(EngineState.EngineFlags.RUN_CLIENT){
                startTime = Globals.engineState.timekeeper.getTime();
                Globals.profiler.beginCpuSample("Load Assets");
                LoggerInterface.loggerEngine.DEBUG_LOOP("Begin load assets");
                Globals.assetManager.loadAssetsInQueue();
                LoggerInterface.loggerEngine.DEBUG_LOOP("Begin delete assets");
                Globals.assetManager.handleDeleteQueue();
                Globals.profiler.endCpuSample();
                endTime = Globals.engineState.timekeeper.getTime();
                ImGuiWindowMacros.addGlobalFramerateDatapoint("assetLoad", endTime - startTime);
            }
            
            
            
            
            ///
            ///    C L I E N T    N E T W O R K I N G    S T U F F
            ///
            //Why is this its own function? Just to get the networking code out of main()
            if(Globals.clientState.clientConnection != null){
                startTime = Globals.engineState.timekeeper.getTime();
                Globals.profiler.beginCpuSample("Client networking");
                LoggerInterface.loggerEngine.DEBUG_LOOP("Begin parse client messages");
                Globals.clientState.clientConnection.parseMessagesSynchronous();
                Globals.profiler.endCpuSample();
                endTime = Globals.engineState.timekeeper.getTime();
                ImGuiWindowMacros.addGlobalFramerateDatapoint("clientNetwork", endTime - startTime);
            }

            
            
            ///
            ///    I N P U T     C O N T R O L S
            ///
            //Poll controls
            if(EngineState.EngineFlags.RUN_CLIENT){
                Globals.profiler.beginCpuSample("Poll Controls");
                LoggerInterface.loggerEngine.DEBUG_LOOP("Begin recapture screen");
                Globals.controlHandler.pollControls();
                RenderingEngine.recaptureIfNecessary();
                Globals.profiler.endCpuSample();
            }


            ///
            ///    S Y N C H R O N O U S      S I G N A L      H A N D L I N G
            ///
            SynchronousSignalHandling.runMainThreadSignalHandlers();

            ///
            ///    E N G I N E    S E R V I C E S
            ///
            if(Globals.engineState.fileWatcherService != null){
                Globals.engineState.fileWatcherService.poll();
            }
            if(EngineState.EngineFlags.RUN_SCRIPTS && Globals.engineState.scriptEngine != null){
                Globals.engineState.scriptEngine.scanScriptDir();
            }

            
            ///
            ///
            ///    M A I N     S I M U L A T I O N     I N N E R     L O O P
            ///

            int simFrameHardcapCounter = 0;
            startTime = Globals.engineState.timekeeper.getTime();
            while(Globals.engineState.timekeeper.pullFromAccumulator() && framestep > 0 && simFrameHardcapCounter < Timekeeper.SIM_FRAME_HARDCAP){

                //do not simulate extra frames if we're already behind schedule
                if(Globals.engineState.timekeeper.getMostRecentRawFrametime() > Globals.engineState.timekeeper.getSimFrameTime() && simFrameHardcapCounter > 0){
                    break;
                }

                //sim frame hard cap counter increment
                simFrameHardcapCounter++;
                //handle framestep
                if(framestep == 1){
                    framestep = 0;
                }



                ///
                ///    C L I E N T    S I M U L A T I O N    S T U F F
                ///
                LoggerInterface.loggerEngine.DEBUG_LOOP("Begin client simulation");
                if(Globals.clientState != null && Globals.clientState.clientSimulation != null){
                    Globals.profiler.beginCpuSample("Client simulation");
                    Globals.clientState.clientSimulation.simulate();
                    Globals.profiler.endCpuSample();
                }









                

                ///
                ///    S E R V E R     M A I N     R O U T I N E S
                ///
                Globals.profiler.beginCpuSample("Main Server Functions");
                MainServerFunctions.simulate();
                Globals.profiler.endCpuSample();
            }
            endTime = Globals.engineState.timekeeper.getTime();
            ImGuiWindowMacros.addGlobalFramerateDatapoint("simframes", endTime - startTime);



            //
            //     M A I N    A U D I O     F U N C T I O N
            //
            Globals.profiler.beginCpuSample("audio engine update");
            if(Globals.audioEngine != null && Globals.audioEngine.initialized()){
                Globals.audioEngine.update();
            }
            Globals.profiler.endCpuSample();
            



            ///
            ///    M A I N   R E N D E R   F U N C T I O N
            ///
            LoggerInterface.loggerEngine.DEBUG_LOOP("Begin rendering call");
            if(EngineState.EngineFlags.RUN_CLIENT && !EngineState.EngineFlags.HEADLESS){
                startTime = Globals.engineState.timekeeper.getTime();
                Globals.profiler.beginCpuSample("render");
                Globals.renderingEngine.drawScreen();
                Globals.profiler.endCpuSample();
                endTime = Globals.engineState.timekeeper.getTime();
                ImGuiWindowMacros.addGlobalFramerateDatapoint("render", endTime - startTime);
            }




            ///
            ///     G A R B A G E    C H E C K
            ///
            Globals.profiler.beginCpuSample("gc");
            if(EngineState.EngineFlags.EXPLICIT_GC && Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed() % Main.GC_FRAME_FREQUENCY == 0){
                System.gc();
            }
            Globals.profiler.endCpuSample();





            ///
            ///   S H U T D O W N    C H E C K
            ///
            if(EngineState.EngineFlags.HEADLESS){
                if(EngineState.EngineFlags.ENGINE_SHUTDOWN_FLAG){
                    running = false;
                }
            } else {
                if(EngineState.EngineFlags.ENGINE_SHUTDOWN_FLAG || (EngineState.EngineFlags.RUN_CLIENT && GLFW.glfwWindowShouldClose(Globals.renderingEngine.getWindowPtr()))){
                    running = false;
                }
            }
            





            ///
            ///     C L E A N U P    T I M E    V A R I A B L E S
            ///
            if(EngineState.EngineFlags.EXPLICIT_SLEEP && Globals.engineState.timekeeper.getMostRecentRawFrametime() < 0.01f){
                Globals.profiler.beginCpuSample("sleep");
                if(Globals.engineState.timekeeper.getMostRecentRawFrametime() < targetFramePeriod){
                    Main.sleep((int)(1000.0 * (targetFramePeriod - Globals.engineState.timekeeper.getMostRecentRawFrametime())));
                } else {
                    Main.sleep(1);
                }
                Globals.profiler.endCpuSample();
            }
            Globals.engineState.timekeeper.numberOfRenderedFrames++;
            if(maxFrames > 0 && Globals.engineState.timekeeper.numberOfRenderedFrames > maxFrames){
                running = false;
            }


            ///
            ///    F R A M E   T I M E    T R A C K I NG
            ///
            ImGuiWindowMacros.addGlobalFramerateDatapoint("totalframerate", Globals.engineState.timekeeper.getMostRecentRawFrametime());


            ///
            ///     E N D     M A I N     L O O P
            ///
            LoggerInterface.loggerEngine.DEBUG_LOOP("End Main Loop Frame");
            Globals.profiler.endCpuSample();

            } catch (NullPointerException ex){
                LoggerInterface.loggerEngine.ERROR(ex);
            }

        }

    }

    /**
     * Shuts down the engine
     */
    public static void shutdown(){
        if(LoggerInterface.loggerEngine != null){
            LoggerInterface.loggerEngine.INFO("ENGINE SHUTDOWN");
        }
        //
        //   S H U T D O W N
        //
        //Terminate the program.
        if(Globals.renderingEngine != null){
            Globals.renderingEngine.destroy();
        }
        //shut down audio engine
        if(Globals.audioEngine != null && Globals.audioEngine.initialized()){
            Globals.audioEngine.shutdown();
        }
        //if netmonitor is running, close
        if(Globals.netMonitor != null){
            Globals.netMonitor.close();
        }
        //shutdown profiler
        Globals.profiler.destroy();
        
        //shutdown ode
        if(initOde){
            OdeHelper.closeODE();
            initOde = false;
        }
        //
        //Destroy engine state
        if(Globals.engineState != null){
            Globals.engineState.destroy();
        }
        //reset globals for good measure (making sure no long-running threads can re-inject entities into scenes)
        Globals.resetGlobals();
    }

    static void sleep(int i) {
        try {
            TimeUnit.MILLISECONDS.sleep(i);
        } catch (InterruptedException ex) {
            System.out.println("Sleep somehow interrupted?!");
        }
    }
    
    
    
    
    public static void initControlHandler(){
        LoggerInterface.loggerStartup.INFO("Initialize control handler");
        Globals.controlHandler = ControlHandler.generateExampleControlsMap();
        Globals.controlHandler.setCallbacks();
    }
    
    /**
     * Sets the framestep state (2 to resume automatic, 1 to make single step)
     * @param framestep 2 - automatic framestep, 1 - single step, 0 - no step
     */
    @Export
    public static void setFramestep(int framestep){
        Main.framestep = framestep;
    }
    
    /**
     * Sets the engine to enable the profiler
     */
    public static void setEnableProfiler(){
        Main.enableProfiler = true;
    }
    
}
