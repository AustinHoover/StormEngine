package electrosphere.client.fluid.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Acts as a cache in front of fluid model to streamline receiving chunks
 */
public class ClientFluidCache {
    
    //cache capacity
    int cacheSize;
    //the map of chunk key -> chunk data
    Map<String,FluidChunkData> cacheMap = new HashMap<String,FluidChunkData>();
    //the list of keys in the cache
    LinkedList<String> cacheList = new LinkedList<String>();

    /**
     * Lock for the fluid cache to threadsafe
     */
    ReentrantLock lock = new ReentrantLock();
    
    /**
     * Constructor
     * @param cacheSize The capacity of the cache
     */
    public ClientFluidCache(int cacheSize){
        this.cacheSize = cacheSize;
    }
    
    /**
     * Adds a chunk data to the fluid cache
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @param chunkData The chunk data to add at the specified positions
     */
    public void addChunkDataToCache(int worldX, int worldY, int worldZ, FluidChunkData chunkData){
        lock.lock();
        String key = this.getKey(worldX,worldY,worldZ);
        if(cacheMap.containsKey(key)){
            FluidChunkData currentChunk = cacheMap.remove(key);
            cacheList.remove(key);
            currentChunk.freeBuffers();
        }
        cacheMap.put(key,chunkData);
        cacheList.add(key);
        while(cacheList.size() > cacheSize){
            String currentKey = cacheList.pop();
            FluidChunkData currentChunk = cacheMap.remove(currentKey);
            currentChunk.freeBuffers();
        }
        lock.unlock();
    }
    
    
    /**
     * Generates a key for the cache based on the position provided
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @return The cache key
     */
    public String getKey(int worldX, int worldY, int worldZ){
        return worldX + "_" + worldY + "_" + worldZ;
    }
    
    /**
     * Checks whether the cache contains chunk data at a given world point
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @return True if the cache contains chunk data at the specified point, false otherwise
     */
    public boolean containsChunkDataAtWorldPoint(int worldX, int worldY, int worldZ){
        lock.lock();
        boolean rVal = cacheMap.containsKey(getKey(worldX,worldY,worldZ));
        lock.unlock();
        return rVal;
    }
    
    
    
    
    
    /**
     * Gets chunk data at the given world point
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @return The chunk data if it exists, null otherwise
     */
    public FluidChunkData getSubChunkDataAtPoint(int worldX, int worldY, int worldZ){
        lock.lock();
        FluidChunkData rVal = cacheMap.get(getKey(worldX,worldY,worldZ));
        lock.unlock();
        return rVal;
    }
    
}
