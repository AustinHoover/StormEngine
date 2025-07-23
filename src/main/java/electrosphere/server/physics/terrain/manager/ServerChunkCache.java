package electrosphere.server.physics.terrain.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import electrosphere.client.terrain.cells.ClientDrawCellManager;
import electrosphere.server.physics.terrain.diskmap.ChunkDiskMap;
import electrosphere.util.math.HashUtils;

/**
 * Caches chunk data on the server
 */
public class ServerChunkCache {

    /**
     * Number of chunks to cache
     */
    static final int CACHE_SIZE = 1500;

    /**
     * Stride for a full res chunk
     */
    public static final int STRIDE_FULL_RES = ClientDrawCellManager.FULL_RES_LOD;

    /**
     * Stride for a half res chunk
     */
    public static final int STRIDE_HALF_RES = ClientDrawCellManager.HALF_RES_LOD;

    /**
     * Stride for a quarter res chunk
     */
    public static final int STRIDE_QUARTER_RES = ClientDrawCellManager.QUARTER_RES_LOD;

    /**
     * Stride for a eighth res chunk
     */
    public static final int STRIDE_EIGHTH_RES = ClientDrawCellManager.EIGHTH_RES_LOD;

    /**
     * Stride for a sixteenth res chunk
     */
    public static final int STRIDE_SIXTEENTH_RES = ClientDrawCellManager.SIXTEENTH_RES_LOD;

    /**
     * The size of the cache
     */
    int cacheSize = CACHE_SIZE;

    /**
     * The map of full res chunk key -> chunk data
     */
    Map<Long,ServerTerrainChunk> cacheMapFullRes = new HashMap<Long,ServerTerrainChunk>();

    /**
     * The map of half res chunk key -> chunk data
     */
    Map<Long,ServerTerrainChunk> cacheMapHalfRes = new HashMap<Long,ServerTerrainChunk>();

    /**
     * The map of quarter res chunk key -> chunk data
     */
    Map<Long,ServerTerrainChunk> cacheMapQuarterRes = new HashMap<Long,ServerTerrainChunk>();

    /**
     * The map of eighth res chunk key -> chunk data
     */
    Map<Long,ServerTerrainChunk> cacheMapEighthRes = new HashMap<Long,ServerTerrainChunk>();

    /**
     * The map of sixteenth res chunk key -> chunk data
     */
    Map<Long,ServerTerrainChunk> cacheMapSixteenthRes = new HashMap<Long,ServerTerrainChunk>();

    /**
     * Tracks how recently a chunk has been queries for (used for evicting old chunks from cache)
     */
    List<Long> queryRecencyQueue = new LinkedList<Long>();

    /**
     * Tracks what chunks are already queued to be asynchronously loaded. Used so we don't have two threads generating/fetching the same chunk
     */
    Map<Long, Boolean> queuedChunkMap = new HashMap<Long,Boolean>();

    /**
     * The lock for thread safety
     */
    Semaphore lock = new Semaphore(1);

    /**
     * The disk map to use for file io
     */
    ChunkDiskMap chunkDiskMap;

    /**
     * Constructor
     * @param diskMap The disk map to use for file io
     */
    public ServerChunkCache(ChunkDiskMap chunkDiskMap){
        this.chunkDiskMap = chunkDiskMap;
    }

    /**
     * Gets the collection of server terrain chunks that are cached
     * @return The collection of chunks
     */
    public Collection<ServerTerrainChunk> getFullRes(){
        lock.acquireUninterruptibly();
        Collection<ServerTerrainChunk> rVal = Collections.unmodifiableCollection(cacheMapFullRes.values());
        lock.release();
        return rVal;
    }

    /**
     * Evicts all chunks in the cache
     */
    public void clear(){
        lock.acquireUninterruptibly();
        cacheMapFullRes.clear();
        cacheMapHalfRes.clear();
        cacheMapQuarterRes.clear();
        cacheMapEighthRes.clear();
        cacheMapSixteenthRes.clear();
        lock.release();
    }

    /**
     * Gets the chunk at a given world position
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride of the data
     * @return The chunk
     */
    public ServerTerrainChunk get(int worldX, int worldY, int worldZ, int stride){
        ServerTerrainChunk rVal = null;
        Long key = this.getKey(worldX, worldY, worldZ);
        lock.acquireUninterruptibly();
        if(queryRecencyQueue.remove(key)){
            queryRecencyQueue.add(0, key);
            Map<Long,ServerTerrainChunk> cache = this.getCache(stride);
            rVal = cache.get(key);
        }
        lock.release();
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
    public void add(int worldX, int worldY, int worldZ, int stride, ServerTerrainChunk chunk){
        Long key = this.getKey(worldX, worldY, worldZ);
        lock.acquireUninterruptibly();
        queryRecencyQueue.add(0, key);
        Map<Long,ServerTerrainChunk> cache = this.getCache(stride);
        cache.put(key, chunk);
        while(queryRecencyQueue.size() > cacheSize){
            Long oldKey = queryRecencyQueue.remove(queryRecencyQueue.size() - 1);
            ServerTerrainChunk fullRes = cacheMapFullRes.remove(oldKey);
            if(fullRes != null){
                this.chunkDiskMap.saveToDisk(fullRes);
            }
            cacheMapHalfRes.remove(oldKey);
            cacheMapQuarterRes.remove(oldKey);
            cacheMapEighthRes.remove(oldKey);
            cacheMapSixteenthRes.remove(oldKey);
        }
        lock.release();
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
        lock.acquireUninterruptibly();
        Map<Long,ServerTerrainChunk> cache = this.getCache(stride);
        boolean rVal = cache.containsKey(key);
        lock.release();
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
        lock.acquireUninterruptibly();
        boolean rVal = this.queuedChunkMap.containsKey(key);
        lock.release();
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
        lock.acquireUninterruptibly();
        this.queuedChunkMap.put(key,true);
        lock.release();
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
        lock.acquireUninterruptibly();
        this.queuedChunkMap.remove(key);
        lock.release();
    }

    /**
     * Gets the cache
     * @param stride The stride of the data
     * @return The cache to use
     */
    public Map<Long,ServerTerrainChunk> getCache(int stride){
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
