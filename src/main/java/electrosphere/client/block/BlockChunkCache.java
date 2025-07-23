package electrosphere.client.block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.mem.BlockChunkPool;
import electrosphere.server.physics.block.diskmap.ServerBlockChunkDiskMap;
import electrosphere.util.math.HashUtils;

/**
 * Caches chunk data on the server
 */
public class BlockChunkCache {

    /**
     * Number of chunks to cache
     */
    static final int CACHE_SIZE = 500;

    /**
     * The size of the cache
     */
    private int cacheSize = CACHE_SIZE;

    /**
     * The map of full res chunk key -> chunk data
     */
    private Map<Long,BlockChunkData> cacheMapFullRes = new HashMap<Long,BlockChunkData>();

    /**
     * The map of half res chunk key -> chunk data
     */
    private Map<Long,BlockChunkData> cacheMapHalfRes = new HashMap<Long,BlockChunkData>();

    /**
     * The map of quarter res chunk key -> chunk data
     */
    private Map<Long,BlockChunkData> cacheMapQuarterRes = new HashMap<Long,BlockChunkData>();

    /**
     * The map of eighth res chunk key -> chunk data
     */
    private Map<Long,BlockChunkData> cacheMapEighthRes = new HashMap<Long,BlockChunkData>();

    /**
     * The map of sixteenth res chunk key -> chunk data
     */
    private Map<Long,BlockChunkData> cacheMapSixteenthRes = new HashMap<Long,BlockChunkData>();

    /**
     * Tracks how recently a chunk has been queries for (used for evicting old chunks from cache)
     */
    private List<Long> queryRecencyQueue = new LinkedList<Long>();

    /**
     * Tracks what chunks are already queued to be asynchronously loaded. Used so we don't have two threads generating/fetching the same chunk
     */
    private Map<Long, Boolean> queuedChunkMap = new HashMap<Long,Boolean>();

    /**
     * The lock for thread safety
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * The disk map
     */
    private ServerBlockChunkDiskMap chunkDiskMap;

    /**
     * Constructor
     * @param chunkDiskMap If supplied, will be used to save chunks as they are ejected
     */
    public BlockChunkCache(ServerBlockChunkDiskMap chunkDiskMap){
        this.chunkDiskMap = chunkDiskMap;
    }

    /**
     * Gets the collection of server block chunks that are cached
     * @return The collection of chunks
     */
    public Collection<BlockChunkData> getContents(){
        lock.lock();
        Collection<BlockChunkData> rVal = Collections.unmodifiableCollection(cacheMapFullRes.values());
        lock.unlock();
        return rVal;
    }

    /**
     * Evicts all chunks in the cache
     */
    public void clear(){
        lock.lock();
        cacheMapFullRes.clear();
        cacheMapHalfRes.clear();
        cacheMapQuarterRes.clear();
        cacheMapEighthRes.clear();
        cacheMapSixteenthRes.clear();
        lock.unlock();
    }

    /**
     * Gets the chunk at a given world position
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride of the data
     * @return The chunk
     */
    public BlockChunkData get(int worldX, int worldY, int worldZ, int stride){
        BlockChunkData rVal = null;
        Long key = this.getKey(worldX, worldY, worldZ);
        lock.lock();
        queryRecencyQueue.remove(key);
        queryRecencyQueue.add(0, key);
        Map<Long,BlockChunkData> cache = this.getCache(stride);
        rVal = cache.get(key);
        lock.unlock();
        return rVal;
    }

    /**
     * Adds a chunk to the cache
     * @param worldX The world x coordinate of the chunk
     * @param worldY The world y coordinate of the chunk
     * @param worldZ The world z coordinate of the chunk
     * @param stride The stride of the data
     * @param chunk The chunk itself
     */
    public void add(int worldX, int worldY, int worldZ, int stride, BlockChunkData chunk){
        Long key = this.getKey(worldX, worldY, worldZ);
        lock.lock();
        queryRecencyQueue.add(0, key);
        Map<Long,BlockChunkData> cache = this.getCache(stride);
        cache.put(key, chunk);
        while(queryRecencyQueue.size() > cacheSize){
            Long oldKey = queryRecencyQueue.remove(queryRecencyQueue.size() - 1);
            BlockChunkData fullRes = cacheMapFullRes.remove(oldKey);
            if(fullRes != null && this.chunkDiskMap != null){
                this.chunkDiskMap.saveToDisk(fullRes);
                BlockChunkPool.release(fullRes.getType());
                BlockChunkPool.release(fullRes.getMetadata());
            }
            BlockChunkData halfRes = cacheMapHalfRes.remove(oldKey);
            BlockChunkData quarterRes = cacheMapQuarterRes.remove(oldKey);
            BlockChunkData eighthRes = cacheMapEighthRes.remove(oldKey);
            BlockChunkData sixteenthRes = cacheMapSixteenthRes.remove(oldKey);
            if(halfRes != null){
                BlockChunkPool.release(halfRes.getType());
                BlockChunkPool.release(halfRes.getMetadata());
            }
            if(quarterRes != null){
                BlockChunkPool.release(quarterRes.getType());
                BlockChunkPool.release(quarterRes.getMetadata());
            }
            if(eighthRes != null){
                BlockChunkPool.release(eighthRes.getType());
                BlockChunkPool.release(eighthRes.getMetadata());
            }
            if(sixteenthRes != null){
                BlockChunkPool.release(sixteenthRes.getType());
                BlockChunkPool.release(sixteenthRes.getMetadata());
            }
        }
        lock.unlock();
    }

    /**
     * Checks if the cache contains the chunk at a given world position
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride of the data
     * @return true if the cache contains this chunk, false otherwise
     */
    public boolean containsChunk(int worldX, int worldY, int worldZ, int stride){
        Long key = this.getKey(worldX,worldY,worldZ);
        lock.lock();
        Map<Long,BlockChunkData> cache = this.getCache(stride);
        boolean rVal = cache.containsKey(key);
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the key for a given world position
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return The key
     */
    public long getKey(int worldX, int worldY, int worldZ){
        return HashUtils.hashIVec(worldX, worldY, worldZ);
    }

    /**
     * Checks if the chunk is already queued or not 
     * @param worldX The world x position of the chunk
     * @param worldY The world y position of the chunk
     * @param worldZ The world z position of the chunk
     * @return true if the chunk is already queued, false otherwise
     */
    public boolean chunkIsQueued(int worldX, int worldY, int worldZ){
        Long key = this.getKey(worldX,worldY,worldZ);
        lock.lock();
        boolean rVal = this.queuedChunkMap.containsKey(key);
        lock.unlock();
        return rVal;
    }

    /**
     * Flags a chunk as queued
     * @param worldX The world x position of the chunk
     * @param worldY The world y position of the chunk
     * @param worldZ The world z position of the chunk
     */
    public void queueChunk(int worldX, int worldY, int worldZ){
        Long key = this.getKey(worldX,worldY,worldZ);
        lock.lock();
        this.queuedChunkMap.put(key,true);
        lock.unlock();
    }

    /**
     * Unflags a chunk as queued
     * @param worldX The world x position of the chunk
     * @param worldY The world y position of the chunk
     * @param worldZ The world z position of the chunk
     * @param stride The stride of the chunk
     */
    public void unqueueChunk(int worldX, int worldY, int worldZ, int stride){
        Long key = this.getKey(worldX,worldY,worldZ);
        lock.lock();
        this.queuedChunkMap.remove(key);
        lock.unlock();
    }

    /**
     * Gets the cache
     * @param stride The stride of the data
     * @return The cache to use
     */
    public Map<Long,BlockChunkData> getCache(int stride){
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
