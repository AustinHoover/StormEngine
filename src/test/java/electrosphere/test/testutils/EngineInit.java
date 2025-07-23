package electrosphere.test.testutils;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;

import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.engine.profiler.Profiler;
import electrosphere.net.NetUtils;

/**
 * Engine initialization utils for testing
 */
public class EngineInit {

    /**
     * The maximum number of frames to wait before failing the startup routine
     */
    public static final int MAX_FRAMES_TO_WAIT = 100;

    /**
     * Max time in milliseconds to wait while flushing
     */
    public static final int MAX_TIME_TO_WAIT = 15000;

    /**
     * Initializes the engine
     */
    public static void initHeadlessEngine(){
        EngineState.EngineFlags.RUN_CLIENT = true;
        EngineState.EngineFlags.RUN_SERVER = true;
        EngineState.EngineFlags.RUN_AUDIO = false;
        EngineState.EngineFlags.HEADLESS = true;
        Profiler.PROFILE = false;
        NetUtils.setPort(0);
        Main.startUp();
    }

    /**
     * Initializes the engine
     */
    public static void initGraphicalEngine(){
        EngineState.EngineFlags.RUN_CLIENT = true;
        EngineState.EngineFlags.RUN_SERVER = true;
        EngineState.EngineFlags.RUN_AUDIO = false;
        EngineState.EngineFlags.RUN_SCRIPTS = false;
        EngineState.EngineFlags.HEADLESS = false;
        Profiler.PROFILE = false;
        NetUtils.setPort(0);
        Main.startUp();
    }
    
    /**
     * Setups up a locally-connected client and server that have loaded a test scene
     */
    public static void setupConnectedTestScene(){
        //
        //load the scene
        LoadingThread loadingThread = new LoadingThread(LoadingThreadType.LEVEL,"testscene1");
        Globals.engineState.threadManager.start(loadingThread);

        //
        //wait for client to be fully init'd
        int frames = 0;
        while(Globals.engineState.threadManager.isLoading()){
            TestEngineUtils.simulateFrames(1);
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frames++;
            if(frames > MAX_FRAMES_TO_WAIT){
                String errorMessage = "Failed to setup connected test scene!\n" +
                "Still running threads are:\n"
                ;
                for(LoadingThread thread : Globals.engineState.threadManager.getLoadingThreads()){
                    errorMessage = errorMessage + thread.getType() + "\n";
                }
                Assertions.fail("Failed to startup");
            }
        }
    }

    /**
     * Setups up a locally-connected client and server that have loaded the viewport
     */
    public static void setupConnectedTestViewport(){
        //
        //load the scene
        LoadingThread loadingThread = new LoadingThread(LoadingThreadType.LOAD_VIEWPORT);
        Globals.engineState.threadManager.start(loadingThread);

        TestEngineUtils.flush();
    }

}
