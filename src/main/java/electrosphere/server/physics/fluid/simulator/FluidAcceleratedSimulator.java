package electrosphere.server.physics.fluid.simulator;

import java.io.File;
import java.util.List;

import electrosphere.logger.LoggerInterface;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;

/**
 * A c-accelerated fluid simulator
 */
public class FluidAcceleratedSimulator implements ServerFluidSimulator {

    /**
     * The library path property's name
     */
    static final String LIBRARY_PATH_PROP_NAME = "java.library.path";

    /**
     * Directory to serach for the library in
     */
    static final String LIB_DIR = "./shared-folder";

    /**
     * Timestep to simulate by
     */
    public static final float SIMULATE_TIMESTEP = 0.1f;

    /**
     * The gravity constant
     */
    public static final float GRAVITY_CONST = -100f;

    /**
     * Load fluid sim library
     */
    static {
        String libraryPath = System.getProperty(LIBRARY_PATH_PROP_NAME);
        if(!libraryPath.contains(LIB_DIR)){
            LoggerInterface.loggerEngine.ERROR(new Error("Failed to load fluid library! The path does not contain the library folder! " + libraryPath));
        }
        String osName = System.getProperty("os.name").toLowerCase();
        String libPath = LIB_DIR;
        if(osName.contains("win")){
            libPath = libPath + "/libStormEngine.dll";
        } else {
            libPath = libPath + "/libStormEngine.so";
        }
        String absolutePath = new File(libPath).toPath().toAbsolutePath().toString();
        System.load(absolutePath);
    }

    /**
     * Starts up the simulator
     */
    public FluidAcceleratedSimulator(){
        FluidAcceleratedSimulator.init(FluidAcceleratedSimulator.GRAVITY_CONST);
    }

    /**
     * Initializes the data for the fluid sim library
     */
    private static native void init(float gravity);

    /**
     * Main native simulation function
     * @param chunks The list of chunks to simulate with
     * @param timestep The timestep to simulate
     */
    private static native void simulate(List<ServerFluidChunk> chunks, float timestep);

    /**
     * Frees all native memory
     */
    private static native void free();




    @Override
    public void simulate(List<ServerFluidChunk> fluidChunks, List<ServerFluidChunk> broadcastQueue){
        FluidAcceleratedSimulator.simulate(fluidChunks, SIMULATE_TIMESTEP);
        for(ServerFluidChunk fluidChunk : fluidChunks){
            if(fluidChunk.getUpdated()){
                broadcastQueue.add(fluidChunk);
            }
        }
    }


    /**
     * Cleans up the simulator's native state
     */
    public static void cleanup(){
        FluidAcceleratedSimulator.free();
    }


    /**
     * Sums the density for a chunk
     * @param fluidChunk The chunk
     * @return The total density of the chunk
     */
    private static double sumAllDensity(ServerFluidChunk fluidChunk){
        double rVal = 0;
        for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
            for(int y = 0; y < ServerFluidChunk.BUFFER_DIM; y++){
                for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                    rVal = rVal + fluidChunk.getWeight(x, y, z);
                }
            }
        }
        return rVal;
    }
    
}
