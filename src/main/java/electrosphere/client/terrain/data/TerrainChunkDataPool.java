package electrosphere.client.terrain.data;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.client.terrain.cache.ChunkData;

/**
 * Pools allocated terrain chunk data objects
 */
public class TerrainChunkDataPool {

    /**
     * Structure to store not-in-use objects
     */
    static List<TerrainChunkData> meshPool = new LinkedList<TerrainChunkData>();

    /**
     * Structure to store not-in-use objects
     */
    static List<ChunkData> dataPool = new LinkedList<ChunkData>();

    /**
     * Lock for thread-safeing operations
     */
    static ReentrantLock lock = new ReentrantLock();

    /**
     * Gets a terrain chunk object from the pool. Allocates if no free one is available.
     * @return A terrain chunk object of the requested type
     */
    public static TerrainChunkData getMesh(){
        TerrainChunkData rVal = null;
        lock.lock();
        if(meshPool.size() > 0){
            rVal = meshPool.remove(0);
        } else {
            rVal = new TerrainChunkData();
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a terrain chunk data object back into the pool
     * @param data The object to release
     */
    public static void release(TerrainChunkData data){
        lock.lock();
        TerrainChunkDataPool.meshPool.add(data);
        lock.unlock();
    }

    /**
     * Gets a terrain chunk object from the pool. Allocates if no free one is available.
     * @return A terrain chunk object of the requested type
     */
    public static ChunkData getData(){
        ChunkData rVal = null;
        lock.lock();
        if(dataPool.size() > 0){
            rVal = dataPool.remove(0);
        } else {
            rVal = new ChunkData();
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a terrain chunk data object back into the pool
     * @param data The object to release
     */
    public static void release(ChunkData data){
        lock.lock();
        TerrainChunkDataPool.dataPool.add(data);
        lock.unlock();
    }

    /**
     * Gets the size of the chunk pool
     * @return The size of the chunk pool
     */
    public static int getPoolSize(){
        return meshPool.size();
    }
    
}
