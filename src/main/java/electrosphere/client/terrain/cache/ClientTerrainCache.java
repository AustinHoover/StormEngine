package electrosphere.client.terrain.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.joml.Vector3i;

import electrosphere.util.math.HashUtils;



/**
 * Acts as a cache in front of terrain model to streamline receiving chunks
 */
public class ClientTerrainCache {
    
    /**
     * Cache capacity
     */
    int cacheSize;

    /**
     * The map of full res chunk key -> chunk data
     */
    Map<Long,ChunkData> cacheMapFullRes = new HashMap<Long,ChunkData>();

    /**
     * The map of half res chunk key -> chunk data
     */
    Map<Long,ChunkData> cacheMapHalfRes = new HashMap<Long,ChunkData>();

    /**
     * The map of quarter res chunk key -> chunk data
     */
    Map<Long,ChunkData> cacheMapQuarterRes = new HashMap<Long,ChunkData>();

    /**
     * The map of eighth res chunk key -> chunk data
     */
    Map<Long,ChunkData> cacheMapEighthRes = new HashMap<Long,ChunkData>();

    /**
     * The map of sixteenth res chunk key -> chunk data
     */
    Map<Long,ChunkData> cacheMapSixteenthRes = new HashMap<Long,ChunkData>();

    /**
     * The list of keys in the cache
     */
    List<Long> cacheList = new LinkedList<Long>();

    /**
     * A map of chunk to its world position
     */
    Map<ChunkData,Vector3i> chunkPositionMap = new HashMap<ChunkData,Vector3i>();

    /**
     * The lock on the terrain cache
     */
    Semaphore lock = new Semaphore(1);
    
    /**
     * Constructor
     * @param cacheSize The capacity of the cache
     */  
    public ClientTerrainCache(int cacheSize){
        this.cacheSize = cacheSize;
    }
    
    /**
     * Adds a chunk data to the terrain cache
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @param chunkData The chunk data to add at the specified positions
     */
    public void addChunkDataToCache(int worldX, int worldY, int worldZ, ChunkData chunkData){
        lock.acquireUninterruptibly();
        Map<Long,ChunkData> cache = this.getCache(chunkData.getStride());
        cache.put(getKey(worldX,worldY,worldZ),chunkData);
        chunkPositionMap.put(chunkData,new Vector3i(worldX,worldY,worldZ));
        cacheList.add(this.getKey(worldX,worldY,worldZ));
        while(cacheList.size() > cacheSize){
            Long currentKey = cacheList.remove(0);
            ChunkData chunk = null;
            ChunkData removed = null;
            removed = cacheMapFullRes.remove(currentKey);
            if(removed != null){
                chunk = removed;
            }
            removed = cacheMapHalfRes.remove(currentKey);
            if(removed != null){
                chunk = removed;
            }
            removed = cacheMapQuarterRes.remove(currentKey);
            if(removed != null){
                chunk = removed;
            }
            removed = cacheMapEighthRes.remove(currentKey);
            if(removed != null){
                chunk = removed;
            }
            removed = cacheMapSixteenthRes.remove(currentKey);
            if(removed != null){
                chunk = removed;
            }
            chunkPositionMap.remove(chunk);
        }
        if(cacheMapFullRes.size() > cacheSize){
            throw new Error("Client cache surpassed designated size! " + cacheMapFullRes.size());
        }
        if(cacheMapHalfRes.size() > cacheSize){
            throw new Error("Client cache surpassed designated size! " + cacheMapHalfRes.size());
        }
        if(cacheMapQuarterRes.size() > cacheSize){
            throw new Error("Client cache surpassed designated size! " + cacheMapQuarterRes.size());
        }
        if(cacheMapEighthRes.size() > cacheSize){
            throw new Error("Client cache surpassed designated size! " + cacheMapEighthRes.size());
        }
        if(cacheMapSixteenthRes.size() > cacheSize){
            throw new Error("Client cache surpassed designated size! " + cacheMapSixteenthRes.size());
        }
        lock.release();
    }

    /**
     * Evicts all chunks from the cache
     */
    public void evictAll(){
        lock.acquireUninterruptibly();
        this.cacheList.clear();
        this.cacheMapFullRes.clear();
        this.cacheMapHalfRes.clear();
        this.cacheMapQuarterRes.clear();
        this.cacheMapEighthRes.clear();
        this.cacheMapSixteenthRes.clear();
        this.chunkPositionMap.clear();
        lock.release();
    }
    
    
    /**
     * Generates a key for the cache based on the position provided
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @return The cache key
     */
    public long getKey(int worldX, int worldY, int worldZ){
        return HashUtils.hashIVec(worldX, worldY, worldZ);
    }
    
    /**
     * Checks whether the cache contains chunk data at a given world point
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @param stride The stride of the data
     * @return True if the cache contains chunk data at the specified point, false otherwise
     */
    public boolean containsChunkDataAtWorldPoint(int worldX, int worldY, int worldZ, int stride){
        lock.acquireUninterruptibly();
        boolean rVal = this.getCache(stride).containsKey(getKey(worldX,worldY,worldZ));
        lock.release();
        return rVal;
    }
    
    
    
    
    
    /**
     * Gets chunk data at the given world point
     * @param worldX The x world position
     * @param worldY The y world position
     * @param worldZ The z world position
     * @param stride The stride of the data
     * @return The chunk data if it exists, null otherwise
     */
    public ChunkData getSubChunkDataAtPoint(int worldX, int worldY, int worldZ, int stride){
        lock.acquireUninterruptibly();
        ChunkData rVal = this.getCache(stride).get(getKey(worldX,worldY,worldZ));
        lock.release();
        return rVal;
    }

    /**
     * Gets the list of all chunks in the cache
     * @return The list of all chunks in the cache
     */
    public Collection<ChunkData> getAllChunks(){
        return this.cacheMapFullRes.values();
    }

    /**
     * Gets the world position of a chunk
     * @param chunk The chunk
     * @return The world position of the chunk
     */
    public Vector3i getChunkPosition(ChunkData chunk){
        return chunkPositionMap.get(chunk);
    }
    
    /**
     * Gets the cache
     * @param stride The stride of the data
     * @return The cache to use
     */
    public Map<Long,ChunkData> getCache(int stride){
        switch(stride){
            case 0: {
                return cacheMapFullRes;
            }
            case 1: {
                return cacheMapHalfRes;
            }
            case 2: {
                return cacheMapQuarterRes;
            }
            case 3: {
                return cacheMapEighthRes;
            }
            case 4: {
                return cacheMapSixteenthRes;
            }
            default: {
                throw new Error("Invalid stride probided! " + stride);
            }
        }
    }

}
