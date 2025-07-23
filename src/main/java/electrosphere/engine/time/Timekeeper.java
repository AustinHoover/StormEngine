package electrosphere.engine.time;

import org.lwjgl.glfw.GLFW;

import electrosphere.engine.EngineState;

/**
 * Service that keeps track of time for main thread activities.
 */
public class Timekeeper {

    /**
     * the time a single simulation frame should simulate for (this is fixed)
     */
    private double simFrameTime = 0.0;

    /**
     * the time that the system started (0 if using glfw reference, current system time if using java reference)
     */
    private double engineStartTime = 0.0;

    /**
     * the system time at the last call to update()
     */
    private double currentTime = 0.0;

    /**
     * accumulates time between current frame and next frame
     */
    private double frameAccumulator = 0.0;

    /**
     * the number of frames that have elapsed
     */
    private long numberOfSimFramesElapsed = 0;

    /**
     * the number of times the render pipeline has rendered a frame
     */
    public long numberOfRenderedFrames = 0;

    /**
     * the raw (not simulation) frametime of the most recent frame
     */
    private double mostRecentRawFrametime = 0;

    /**
     * The maximum amount of time that can overflow (ie cause more than one sim frame/render frame) before the sim frames are tossed out
     */
    private static double overflowMax = 20;

    /**
     * the maximum number of simulation frames that can happen in a row before the main loop immediately skips more
     */
    public static final int SIM_FRAME_HARDCAP = 3;

    /**
     * step interval time size (for physics)
     */
    public static final float ENGINE_STEP_SIZE = 0.01f;




    /**
     * Gets the time since the engine started from the system
     * @return The time (in seconds)
     */
    public double getTime(){
        if(EngineState.EngineFlags.HEADLESS){
            return System.currentTimeMillis() - engineStartTime;
        } else {
            return GLFW.glfwGetTime();
        }
    }
    
    /**
     * Inits the timekeeper
     * @param simFrameTime The amount of time that a frame should take
     */
    public void init(double simFrameTime){
        this.simFrameTime = simFrameTime;
        if(EngineState.EngineFlags.HEADLESS){
            engineStartTime = System.currentTimeMillis();
        }
        currentTime = getTime();
    }

    /**
     * Updates the timekeeper to track last frametime, etc
     */
    public void update(){
        //calculate frametime, set current time
        double newTime = getTime();
        double frameTime = newTime - currentTime;
        mostRecentRawFrametime = frameTime;
        if(frameTime > overflowMax){
            frameTime = overflowMax;
        }
        currentTime = newTime;
        //add to accumulator
        frameAccumulator += frameTime;
    }

    /**
     * Tries to pull a single frametime out of the accumulator
     * If the accumulator holds at least a frametime's worth of time, the function returns true
     * If the accumulator has not accumulated a simframetime's worth of time, the function returns false
     * @return True if the accumulator has at least a simulation frame's amount of time inside it
     */
    public boolean pullFromAccumulator(){
        boolean rVal = false;
        if(frameAccumulator >= simFrameTime){
            rVal = true;
            frameAccumulator -= simFrameTime;
            numberOfSimFramesElapsed++;
        }
        return rVal;
    }

    /**
     * Gets the amount of time a single simulation frame should take
     * @return The time
     */
    public double getSimFrameTime(){
        return this.simFrameTime;
    }

    /**
     * Gets the number of simulation frames that have elapsed
     * @return the number of frames
     */
    public long getNumberOfSimFramesElapsed(){
        return numberOfSimFramesElapsed;
    }

    /**
     * The number of rendered frames that have elapsed
     * @return the frame number
     */
    public long getNumberOfRenderFramesElapsed(){
        return numberOfRenderedFrames;
    }

    /**
     * Gets the most recent raw frametime
     * @return the frametime
     */
    public double getMostRecentRawFrametime(){
        return mostRecentRawFrametime;
    }

    /**
     * Gets the current time of the renderer since the engine started
     * @return The current time
     */
    public double getCurrentRendererTime(){
        return currentTime;
    }

    /**
     * The number of frames we're simulating this cycle
     * @return The number of frames we're simulating this cycle
     */
    public long getDeltaFrames(){
        //this should always return 1. We're always simulating 1 frame per run of the loop in main
        return 1;
    }

}
