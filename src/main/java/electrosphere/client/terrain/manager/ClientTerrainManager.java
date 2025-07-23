package electrosphere.client.terrain.manager;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkCache;
import electrosphere.client.block.BlockChunkData;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.client.terrain.cache.ClientTerrainCache;
import electrosphere.client.terrain.cells.ClientDrawCellManager;
import electrosphere.client.terrain.cells.DrawCell;
import electrosphere.client.terrain.cells.VoxelTextureAtlas;
import electrosphere.client.terrain.data.TerrainChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.mem.BlockChunkPool;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.renderer.meshgen.TransvoxelModelGeneration;
import electrosphere.renderer.model.Model;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;
import electrosphere.util.math.HashUtils;

/**
 * Manages terrain storage and access on the client
 */
public class ClientTerrainManager {
    
    /**
     * queues messages from server
     */
    private List<TerrainMessage> messageQueue = new LinkedList<TerrainMessage>();

    /**
     * Locks the terrain manager (eg if adding message from network)
     */
    private static Semaphore lock = new Semaphore(1);

    /**
     * Maximum concurrent terrain requests
     */
    public static final int MAX_CONCURRENT_REQUESTS = 500;

    /**
     * Number of frames to wait before flagging a request as failed
     */
    public static final int FAILED_REQUEST_THRESHOLD = 500;
    
    /**
     * The interpolation ratio of terrain
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
     * used for caching the macro values
     */
    private ClientTerrainCache terrainCache;

    /**
     * Caches block data
     */
    private BlockChunkCache blockCache;

    /**
     * The queue of terrain chunk data to be buffered to gpu
     */
    private static List<TerrainChunkGenQueueItem> terrainChunkGenerationQueue = new LinkedList<TerrainChunkGenQueueItem>();

    /**
     * Tracks what outgoing requests are currently active
     */
    private Map<Long,Integer> requestedMap = new HashMap<Long,Integer>();

    /**
     * Tracks what outgoing block requests are currently active
     */
    private Map<Long,Integer> requestedBlockMap = new HashMap<Long,Integer>();

    /**
     * Used to clear the request map
     */
    private List<Long> toClearFailedRequests = new LinkedList<Long>();
    
    /**
     * Constructor
     */
    public ClientTerrainManager(){
        terrainCache = new ClientTerrainCache(CACHE_SIZE);
        blockCache = new BlockChunkCache(null);
    }
    
    
    /**
     * Handles messages that have been received from the server
     */
    public void handleMessages(){
        Globals.profiler.beginCpuSample("ClientTerrainManager.handleMessages");
        lock.acquireUninterruptibly();
        for(TerrainMessage message : messageQueue){
            switch(message.getMessageSubtype()){
                case SENDCHUNKDATA: {
                    int[][][] values = new int[ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE];
                    float[][][] weights = new float[ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE];
                    ByteBuffer buffer = ByteBuffer.wrap(message.getchunkData());
                    FloatBuffer floatBuffer = buffer.asFloatBuffer();
                    for(int x = 0; x < ChunkData.CHUNK_DATA_SIZE; x++){
                        for(int y = 0; y < ChunkData.CHUNK_DATA_SIZE; y++){
                            for(int z = 0; z < ChunkData.CHUNK_DATA_SIZE; z++){
                                weights[x][y][z] = floatBuffer.get();
                            }
                        }
                    }
                    IntBuffer intView = buffer.asIntBuffer();
                    intView.position(floatBuffer.position());
                    for(int x = 0; x < ChunkData.CHUNK_DATA_SIZE; x++){
                        for(int y = 0; y < ChunkData.CHUNK_DATA_SIZE; y++){
                            for(int z = 0; z < ChunkData.CHUNK_DATA_SIZE; z++){
                                values[x][y][z] = intView.get();
                            }
                        }
                    }
                    ChunkData data = new ChunkData(message.getworldX(), message.getworldY(), message.getworldZ(), ChunkData.NO_STRIDE, ChunkData.NOT_HOMOGENOUS);
                    data.setVoxelType(values);
                    data.setVoxelWeight(weights);
                    terrainCache.addChunkDataToCache(
                        message.getworldX(), message.getworldY(), message.getworldZ(), 
                        data
                    );
                } break;
                case SENDREDUCEDCHUNKDATA: {
                    ChunkData data = (ChunkData)message.getExtraData().get(0);
                    message.getExtraData().clear();
                    if(message.getchunkResolution() == ChunkData.NO_STRIDE && terrainCache.containsChunkDataAtWorldPoint(message.getworldX(), message.getworldY(), message.getworldZ(), ChunkData.NO_STRIDE)){
                        //this is a full-res chunk, and we already had this chunk in cache
                        //need to flag foliage cell to update these positions given that we have changed terrain values
                        Globals.clientState.foliageCellManager.markUpdateable(message.getworldX(), message.getworldY(), message.getworldZ());
                    }
                    terrainCache.addChunkDataToCache(
                        message.getworldX(), message.getworldY(), message.getworldZ(), 
                        data
                    );
                    //remove from request map
                    this.requestedMap.remove(this.getRequestKey(message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution()));
                } break;
                case SENDREDUCEDBLOCKDATA: {
                    //construct return obj
                    BlockChunkData data = new BlockChunkData();
                    data.setWorldX(message.getworldX());
                    data.setWorldY(message.getworldY());
                    data.setWorldZ(message.getworldZ());
                    data.setHomogenousValue((short)message.gethomogenousValue());

                    //read main data
                    if(data.getHomogenousValue() == BlockChunkData.NOT_HOMOGENOUS){
                        short[] type = BlockChunkPool.getShort();
                        short[] metadata = BlockChunkPool.getShort();
                        ByteBuffer buffer = ByteBuffer.wrap(message.getchunkData());
                        ShortBuffer shortBuffer = buffer.asShortBuffer();

                        for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                            type[i] = shortBuffer.get();
                        }
                        for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                            metadata[i] = shortBuffer.get();
                        }
                        data.setType(type);
                        data.setMetadata(metadata);
                    }
                    blockCache.add(message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution(), data);
                    //remove from request map
                    this.requestedBlockMap.remove(this.getRequestKey(message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution()));
                } break;
                default:
                    LoggerInterface.loggerEngine.WARNING("ClientTerrainManager: unhandled network message of type" + message.getMessageSubtype());
                    break;
            }
            Globals.clientState.clientConnection.release(message);
        }
        messageQueue.clear();
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
     * Evicts all cached terrain
     */
    public void evictAll(){
        this.terrainCache.evictAll();
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
     * Checks if the terrain cache contains chunk data at a given world position
     * @param worldX the x position
     * @param worldY the y position
     * @param worldZ the z position
     * @param stride The stride of the data
     * @return true if the data exists, false otherwise
     */
    public boolean containsChunkDataAtWorldPoint(int worldX, int worldY, int worldZ, int stride){
        return terrainCache.containsChunkDataAtWorldPoint(worldX, worldY, worldZ, stride);
    }

    /**
     * Checks if the terrain cache contains chunk data at a given world position
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
            Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestReducedChunkDataMessage(
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
    public ChunkData getChunkDataAtWorldPoint(int worldX, int worldY, int worldZ, int stride){
        return terrainCache.getSubChunkDataAtPoint(worldX, worldY, worldZ, stride);
    }

    /**
     * Gets the chunk data at a given world position
     * @param worldPos The world position as a joml vector
     * @param stride The stride of the data
     * @return The chunk data if it exists, otherwise null
     */
    public ChunkData getChunkDataAtWorldPoint(Vector3i worldPos, int stride){
        return this.getChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, stride);
    }
    
    

    /**
     * Queues a terrain chunk to be pushed to GPU based on chunk data
     * @param data The chunk data (triangles, normals, etc)
     * @return The model path that is promised to eventually reflect the terrain model when it makes it to gpu
     */
    public static String queueTerrainGridGeneration(TerrainChunkData data, VoxelTextureAtlas atlas, DrawCell notifyTarget, Entity toDelete){
        if(data.getFaceElements().length < 1){
            throw new Error("Invalid data!");
        }
        String promisedHash = "";
        UUID newUUID = UUID.randomUUID();
        promisedHash = newUUID.toString();
        TerrainChunkGenQueueItem queueItem = new TerrainChunkGenQueueItem(data, promisedHash, atlas, notifyTarget, toDelete);
        lock.acquireUninterruptibly();
        terrainChunkGenerationQueue.add(queueItem);
        lock.release();
        return promisedHash;
    }

    /**
     * Pushes all terrain data in queue to the gpu and registers the resulting models
     */
    public static void generateTerrainChunkGeometry(){
        Globals.profiler.beginCpuSample("ClientTerrainManager.generateTerrainChunkGeometry");
        lock.acquireUninterruptibly();
        for(TerrainChunkGenQueueItem queueItem : terrainChunkGenerationQueue){
            Model terrainModel = TransvoxelModelGeneration.generateTerrainModel(queueItem.getData(), queueItem.getAtlas());
            Globals.assetManager.registerModelWithPath(terrainModel, queueItem.getPromisedHash());
            if(queueItem.notifyTarget != null){
                queueItem.notifyTarget.alertToGeneration();
            }
            if(queueItem.toDelete != null){
                ClientEntityUtils.destroyEntity(queueItem.toDelete);
            }
        }
        terrainChunkGenerationQueue.clear();
        lock.release();
        Globals.profiler.endCpuSample();
    }

    /**
     * Gets all chunks in the terrain cache
     * @return The collection of all chunk data objects
     */
    public Collection<ChunkData> getAllChunks(){
        return terrainCache.getAllChunks();
    }

    /**
     * Gets the world position of a given chunk
     * @param chunk The chunk
     * @return The world position of the chunk
     */
    public Vector3i getPositionOfChunk(ChunkData chunk){
        return terrainCache.getChunkPosition(chunk);
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
