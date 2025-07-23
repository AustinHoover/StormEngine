package electrosphere.mem;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.client.block.BlockChunkData;

/**
 * A pool for temporary vectors
 */
public class BlockChunkPool {
    
    /**
     * Structure to store not-in-use objects
     */
    static List<short[]> shortPool = new LinkedList<short[]>();

    /**
     * Lock for thread-safeing operations
     */
    static ReentrantLock lock = new ReentrantLock();

    /**
     * Gets a short[] from the pool. Allocates if no free one is available.
     * @param type The type of the message
     * @return A short[]
     */
    public static short[] getShort(){
        short[] rVal = null;
        lock.lock();
        if(shortPool.size() > 0){
            rVal = shortPool.remove(0);
        } else {
            rVal = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a Vector3f back into the pool
     * @param data The object to release
     */
    public static void release(short[] data){
        lock.lock();
        if(data != null){
            BlockChunkPool.shortPool.add(data);
        }
        lock.unlock();
    }

    /**
     * Gets the size of the short pool
     * @return The size of the short pool
     */
    public static int getPoolSize(){
        return shortPool.size();
    }

}
