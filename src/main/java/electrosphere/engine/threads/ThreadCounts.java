package electrosphere.engine.threads;

/**
 * Thread counts for various tasks
 */
public class ThreadCounts {
    
    /**
     * Number of threads for foliage meshgen
     */
    public static final int FOLIAGE_MESHGEN_THREADS = 2;

    /**
     * Number of threads for block meshgen
     */
    public static final int BLOCK_MESHGEN_THREADS = 4;

    /**
     * Number of threads for terrain meshgen
     */
    public static final int TERRAIN_MESHGEN_THREADS = 4;

    /**
     * Number of threads for solving pathfinding
     */
    public static final int PATHFINDING_THREADS = 1;

    /**
     * Number of threads for solving macro pathfinding
     */
    public static final int MACRO_PATHING_THREADS = 1;

    /**
     * Number of threads for gridded datacell manager chunk loading/unloading
     */
    public static final int GRIDDED_DATACELL_LOADING_THREADS = 4;

    /**
     * Number of threads for generating physics for the gridded datacell manager
     */
    public static final int GRIDDED_DATACELL_PHYSICS_GEN_THREADS = 4;

    /**
     * Number of threads for generating block chunks on the server
     */
    public static final int SERVER_BLOCK_GENERATION_THREADS = 2;

    /**
     * Number of threads for generating terrain chunks on the server
     */
    public static final int SERVER_TERRAIN_GENERATION_THREADS = 2;

    /**
     * Default thread count for the thread manager
     */
    public static final int DEFAULT_SERVICE_THREADS = 1;

}
