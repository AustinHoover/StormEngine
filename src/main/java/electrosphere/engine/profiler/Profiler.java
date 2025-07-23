package electrosphere.engine.profiler;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.remotery.Remotery;

/**
 * A profiler for monitoring engine performance
 */
public class Profiler {

    /**
     * Pointer value for uninitialized profiler
     */
    private static final int UNINITIALIZED = -1;

    /**
     * Controls whether to profile or not
     * !!WARNING!!: when this is turned on, testing can behave weirdly!! IE GET STUCK!
     */
    public static boolean PROFILE = false;

    /**
     * Pointer to the global instance
     */
    private long pointer = UNINITIALIZED;
    
    /**
     * Creates the profiler
     */
    public Profiler(){
    }

    /**
     * Begins a CPU sample
     * @param sampleName The name of the sample
     */
    public void beginCpuSample(String sampleName){
        if(PROFILE){
            Remotery.rmt_BeginCPUSample(sampleName, Remotery.RMTSF_None, null);
        }
    }

    /**
     * Begins an aggregate CPU sample
     * @param sampleName The name of the sample
     */
    public void beginAggregateCpuSample(String sampleName){
        if(PROFILE){
            Remotery.rmt_BeginCPUSample(sampleName, Remotery.RMTSF_Aggregate, null);
        }
    }

    /**
     * Begins an recursive CPU sample
     * @param sampleName The name of the sample
     */
    public void beginRecursiveCpuSample(String sampleName){
        if(PROFILE){
            Remotery.rmt_BeginCPUSample(sampleName, Remotery.RMTSF_Recursive, null);
        }
    }

    /**
     * Begins a Root CPU sample (will assert if another sample is not ended before this one)
     * @param sampleName The name of the root sample
     */
    public void beginRootCpuSample(String sampleName){
        if(PROFILE){
            Remotery.rmt_BeginCPUSample(sampleName, Remotery.RMTSF_Root, null);
        }
    }

    /**
     * Ends a CPU sample
     * @param sampleName The name of the sample
     */
    public void endCpuSample(){
        if(PROFILE){
            Remotery.rmt_EndCPUSample();
        }
    }

    /**
     * Starts the remotery instance
     */
    public void start(){
        Profiler.PROFILE = true;
        try(MemoryStack stack = MemoryStack.stackPush()){
            PointerBuffer allocBuffer = stack.mallocPointer(1);
            Remotery.rmt_CreateGlobalInstance(allocBuffer);
            pointer = allocBuffer.get();
        }
    }

    /**
     * Destroys the profiler
     */
    public void destroy(){
        if(pointer != UNINITIALIZED){
            Remotery.rmt_DestroyGlobalInstance(pointer);
        }
    }

}
