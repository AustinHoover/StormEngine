package electrosphere.client.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.joml.Vector3i;

import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.terrain.cells.ClientDrawCellManager;
import electrosphere.client.terrain.cells.DrawCell;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.renderer.meshgen.BlockMeshgen.BlockMeshData;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;
import electrosphere.util.math.HashUtils;


/**
 * Handles networking for block-related messages on client
 */
public class ClientBlockManager {

    /**
     * queues messages from server
     */
    List<TerrainMessage> messageQueue = new LinkedList<TerrainMessage>();

    /**
     * Locks the block manager (eg if adding message from network)
     */
    static Semaphore lock = new Semaphore(1);

    /**
     * Maximum concurrent block requests
     */
    public static final int MAX_CONCURRENT_REQUESTS = 500;

    /**
     * Number of frames to wait before flagging a request as failed
     */
    public static final int FAILED_REQUEST_THRESHOLD = 500;
    
    /**
     * The interpolation ratio of block
     */
    public static final int INTERPOLATION_RATIO = ServerTerrainManager.SERVER_TERRAIN_MANAGER_INTERPOLATION_RATIO;
    
    /**
     * caches chunks from server
     */
    static final int CACHE_SIZE = 2500 + (int)(ClientDrawCellManager.FULL_RES_DIST * 10) + (int)(ClientDrawCellManager.HALF_RES_DIST * 10);

    /**
     * Size of the cache in bytes
     */
    static final int CACHE_SIZE_IN_MB = (CACHE_SIZE * ServerTerrainChunk.CHUNK_DIMENSION * ServerTerrainChunk.CHUNK_DIMENSION * ServerTerrainChunk.CHUNK_DIMENSION * 2 * 4) / 1024 / 1024;

    /**
     * Caches block data
     */
    BlockChunkCache blockCache;
    
    /**
     * The world data for the client
     */
    ClientWorldData clientWorldData;

    //The queue of block chunk data to be buffered to gpu
    // static List<TerrainChunkGenQueueItem> terrainChunkGenerationQueue = new LinkedList<TerrainChunkGenQueueItem>();

    /**
     * Tracks what outgoing requests are currently active
     */
    Map<Long,Integer> requestedMap = new HashMap<Long,Integer>();

    /**
     * Used to clear the request map
     */
    List<Long> toClearFailedRequests = new LinkedList<Long>();
    
    /**
     * Constructor
     */
    public ClientBlockManager(){
        blockCache = new BlockChunkCache(null);
    }
    
    
    /**
     * Handles messages that have been received from the server
     */
    public void handleMessages(){
        Globals.profiler.beginCpuSample("ClientBlockManager.handleMessages");
        lock.acquireUninterruptibly();
        List<TerrainMessage> bouncedMessages = new LinkedList<TerrainMessage>();
        for(TerrainMessage message : messageQueue){
            switch(message.getMessageSubtype()){
                case SENDREDUCEDBLOCKDATA: {
                    //construct return obj
                    BlockChunkData data = new BlockChunkData();
                    data.setWorldX(message.getworldX());
                    data.setWorldY(message.getworldY());
                    data.setWorldZ(message.getworldZ());
                    data.setHomogenousValue((short)message.gethomogenousValue());

                    //read main data
                    if(data.getHomogenousValue() == BlockChunkData.NOT_HOMOGENOUS){
                        List<Object> extraData = message.getExtraData();
                        if(extraData == null || extraData.size() != 2){
                            throw new Error("Failed to attach extra data!");
                        }
                        data.setType((short[])extraData.get(0));
                        data.setMetadata((short[])extraData.get(1));
                        extraData.clear();
                    }
                    blockCache.add(message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution(), data);
                    //remove from request map
                    this.requestedMap.remove(this.getRequestKey(message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution()));
                } break;
                default:
                    LoggerInterface.loggerEngine.WARNING("ClientBlockManager: unhandled network message of type" + message.getMessageSubtype());
                    break;
            }
        }
        messageQueue.clear();
        for(TerrainMessage message : bouncedMessages){
            messageQueue.add(message);
        }
        //evaluate if any terrain chunks have failed to request
        for(Long key : this.requestedMap.keySet()){
            int duration = this.requestedMap.get(key);
            if(duration > FAILED_REQUEST_THRESHOLD){
                toClearFailedRequests.add(key);
            } else {
                this.requestedMap.put(key,duration + 1);
            }
        }
        if(this.toClearFailedRequests.size() > 0){
            for(Long key : toClearFailedRequests){
                this.requestedMap.remove(key);
            }
            this.toClearFailedRequests.clear();
        }
        lock.release();
        Globals.profiler.endCpuSample();
    }

    /**
     * Evicts all cached blocks
     */
    public void evictAll(){
        this.blockCache.clear();
    }
    
    /**
     * Attaches a terrain message to the queue of messages that this manager needs to process
     * @param message The message
     */
    public void attachTerrainMessage(TerrainMessage message){
        lock.acquireUninterruptibly();
        messageQueue.add(message);
        lock.release();
    }
    
    /**
     * Checks if the block cache contains chunk data at a given world position
     * @param worldX the x position
     * @param worldY the y position
     * @param worldZ the z position
     * @param stride The stride of the data
     * @return true if the data exists, false otherwise
     */
    public boolean containsChunkDataAtWorldPoint(int worldX, int worldY, int worldZ, int stride){
        return blockCache.containsChunk(worldX, worldY, worldZ, stride);
    }

    /**
     * Checks if the block cache contains chunk data at a given world position
     * @param worldPos The vector containing the world-space position
     * @param stride The stride of the data
     * @return true if the data exists, false otherwise
     */
    public boolean containsChunkDataAtWorldPoint(Vector3i worldPos, int stride){
        return this.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, stride);
    }

    /**
     * Requests a chunk from the server
     * @param worldX the world x coordinate of the chunk
     * @param worldY the world y coordinate of the chunk
     * @param worldZ the world z coordinate of the chunk
     * @param stride The stride of the data
     * @return true if the request was successfully sent, false otherwise
     */
    public boolean requestChunk(int worldX, int worldY, int worldZ, int stride){
        boolean rVal = false;
        lock.acquireUninterruptibly();
        if(this.requestedMap.size() < MAX_CONCURRENT_REQUESTS && !this.requestedMap.containsKey(this.getRequestKey(worldX, worldY, worldZ, stride))){
            Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestReducedBlockDataMessage(
                worldX,
                worldY,
                worldZ,
                stride
            ));
            this.requestedMap.put(this.getRequestKey(worldX, worldY, worldZ, stride), 0);
            rVal = true;
        }
        lock.release();
        return rVal;
    }
    
    /**
     * Gets the chunk data at a given world position
     * @param worldX The x component of the world coordinate
     * @param worldY The y component of the world coordinate
     * @param worldZ The z component of the world coordinate
     * @param stride The stride of the data
     * @return The chunk data if it exists, otherwise null
     */
    public BlockChunkData getChunkDataAtWorldPoint(int worldX, int worldY, int worldZ, int stride){
        return blockCache.get(worldX, worldY, worldZ, stride);
    }

    /**
     * Gets the chunk data at a given world position
     * @param worldPos The world position as a joml vector
     * @param stride The stride of the data
     * @return The chunk data if it exists, otherwise null
     */
    public BlockChunkData getChunkDataAtWorldPoint(Vector3i worldPos, int stride){
        return this.getChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, stride);
    }
    
    

    /**
     * Queues a block chunk to be pushed to GPU based on chunk data
     * @param data The chunk data (triangles, normals, etc)
     * @return The model path that is promised to eventually reflect the block model when it makes it to gpu
     */
    public static String queueBlockGridGeneration(BlockMeshData data, DrawCell notifyTarget, Entity toDelete){
        throw new UnsupportedOperationException("Unimplemented");
        // String promisedHash = "";
        // UUID newUUID = UUID.randomUUID();
        // promisedHash = newUUID.toString();
        // TerrainChunkGenQueueItem queueItem = new TerrainChunkGenQueueItem(data, promisedHash, atlas, notifyTarget, toDelete);
        // lock.acquireUninterruptibly();
        // terrainChunkGenerationQueue.add(queueItem);
        // lock.release();
        // return promisedHash;
    }

    /**
     * Pushes all block data in queue to the gpu and registers the resulting models
     */
    public static void generateBlockChunkGeometry(){
        throw new UnsupportedOperationException("Unimplemented");
        // Globals.profiler.beginCpuSample("ClientBlockManager.generateTerrainChunkGeometry");
        // lock.acquireUninterruptibly();
        // for(TerrainChunkGenQueueItem queueItem : terrainChunkGenerationQueue){
        //     Model terrainModel = TransvoxelModelGeneration.generateTerrainModel(queueItem.getData(), queueItem.getAtlas());
        //     Globals.assetManager.registerModelToSpecificString(terrainModel, queueItem.getPromisedHash());
        //     if(queueItem.notifyTarget != null){
        //         queueItem.notifyTarget.alertToGeneration();
        //     }
        //     if(queueItem.toDelete != null){
        //         ClientEntityUtils.destroyEntity(queueItem.toDelete);
        //     }
        // }
        // terrainChunkGenerationQueue.clear();
        // lock.release();
        // Globals.profiler.endCpuSample();
    }

    /**
     * Gets all chunks in the terrain cache
     * @return The collection of all chunk data objects
     */
    public Collection<BlockChunkData> getAllChunks(){
        return blockCache.getContents();
    }

    /**
     * Gets the key for a given request
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride of the data
     * @return The key
     */
    private Long getRequestKey(int worldX, int worldY, int worldZ, int stride){
        return (long)HashUtils.hashIVec(worldY, worldZ, worldZ);
    }
}
