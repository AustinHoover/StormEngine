package electrosphere.client.fluid.manager;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector3i;

import electrosphere.client.fluid.cache.ClientFluidCache;
import electrosphere.client.fluid.cache.FluidChunkData;
import electrosphere.client.fluid.cells.FluidCell;
import electrosphere.client.scene.ClientWorldData;
import electrosphere.engine.Globals;
import electrosphere.entity.types.fluid.FluidChunkModelData;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.renderer.meshgen.FluidChunkModelGeneration;
import electrosphere.renderer.model.Model;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;

/**
 * Manages fluid storage and access on the client
 */
public class ClientFluidManager {
    
    //queues messages from server
    List<TerrainMessage> messageQueue = new LinkedList<TerrainMessage>();
    
    //The interpolation ratio of fluid
    public static final int INTERPOLATION_RATIO = ServerTerrainManager.SERVER_TERRAIN_MANAGER_INTERPOLATION_RATIO;
    
    //caches chunks from server
    static final int CACHE_SIZE = 250;
    
    //used for caching the macro values
    ClientFluidCache fluidCache;
    
    //The world data for the client
    ClientWorldData clientWorldData;

    //The queue of fluid chunk data to be buffered to gpu
    static List<FluidChunkGenQueueItem> fluidChunkGenerationQueue = new LinkedList<FluidChunkGenQueueItem>();

    /**
     * The number of messages parsed this frame
     */
    int messageCount;

    /**
     * Lock for thread-safeing the manager
     */
    static ReentrantLock lock = new ReentrantLock();
    
    /**
     * Constructor
     */
    public ClientFluidManager(){
        fluidCache = new ClientFluidCache(CACHE_SIZE);
    }
    
    
    public void handleMessages(){
        lock.lock();
        List<TerrainMessage> bouncedMessages = new LinkedList<TerrainMessage>();
        messageCount = messageQueue.size();
        for(TerrainMessage message : messageQueue){
            switch(message.getMessageSubtype()){
                case SENDFLUIDDATA: {
                    ByteBuffer buffer = ByteBuffer.wrap(message.getchunkData());
                    FluidChunkData data = this.parseFluidDataBuffer(buffer);
                    fluidCache.addChunkDataToCache(
                        message.getworldX(), message.getworldY(), message.getworldZ(), 
                        data
                    );
                } break;
                case UPDATEFLUIDDATA: {
                    ByteBuffer buffer = ByteBuffer.wrap(message.getchunkData());
                    FluidChunkData data = this.parseFluidDataBuffer(buffer);
                    fluidCache.addChunkDataToCache(
                        message.getworldX(), message.getworldY(), message.getworldZ(), 
                        data
                    );
                    if(Globals.clientState.fluidCellManager != null){
                        Globals.clientState.fluidCellManager.markUpdateable(message.getworldX(), message.getworldY(), message.getworldZ());
                    }
                } break;
                default:
                    LoggerInterface.loggerEngine.WARNING("ClientFluidManager: unhandled network message of type" + message.getMessageSubtype());
                    break;
            }
        }
        messageQueue.clear();
        for(TerrainMessage message : bouncedMessages){
            messageQueue.add(message);
        }
        lock.unlock();
    }
    
    public void attachFluidMessage(TerrainMessage message){
        lock.lock();
        messageQueue.add(message);
        lock.unlock();
    }
    
    public boolean containsChunkDataAtWorldPoint(int worldX, int worldY, int worldZ){
        return fluidCache.containsChunkDataAtWorldPoint(worldX, worldY, worldZ);
    }
    
    public boolean containsChunkDataAtRealPoint(double x, double y, double z){
        assert clientWorldData != null;
        return fluidCache.containsChunkDataAtWorldPoint(
            ClientWorldData.convertRealToChunkSpace(x), 
            ClientWorldData.convertRealToChunkSpace(y), 
            ClientWorldData.convertRealToChunkSpace(z)
        );
    }
    
    /**
     * Gets the chunk data at a given world position
     * @param worldX The x component of the world coordinate
     * @param worldY The y component of the world coordinate
     * @param worldZ The z component of the world coordinate
     * @return The chunk data if it exists, otherwise null
     */
    public FluidChunkData getChunkDataAtWorldPoint(int worldX, int worldY, int worldZ){
        return fluidCache.getSubChunkDataAtPoint(worldX, worldY, worldZ);
    }

    /**
     * Gets the chunk data at a given world position
     * @param worldPos The world position as a joml vector
     * @return The chunk data if it exists, otherwise null
     */
    public FluidChunkData getChunkDataAtWorldPoint(Vector3i worldPos){
        return fluidCache.getSubChunkDataAtPoint(worldPos.x, worldPos.y, worldPos.z);
    }
    
    

    /**
     * Queues a fluid chunk to be pushed to GPU based on chunk data
     * @param data The chunk data (triangles, normals, etc)
     * @return The model path that is promised to eventually reflect the fluid model when it makes it to gpu
     */
    public static String queueFluidGridGeneration(FluidChunkModelData data){
        String promisedHash = "";
        UUID newUUID = UUID.randomUUID();
        promisedHash = newUUID.toString();
        FluidChunkGenQueueItem queueItem = new FluidChunkGenQueueItem(data, promisedHash);
        lock.lock();
        fluidChunkGenerationQueue.add(queueItem);
        lock.unlock();
        return promisedHash;
    }

    /**
     * Pushes all fluid data in queue to the gpu and registers the resulting models
     */
    public static void generateFluidChunkGeometry(){
        Globals.profiler.beginCpuSample("generateFluidChunkGeometry");
        lock.lock();
        for(FluidChunkGenQueueItem queueItem : fluidChunkGenerationQueue){
            Model fluidModel = FluidChunkModelGeneration.generateFluidModel(queueItem.getData());
            Globals.assetManager.registerModelWithPath(fluidModel, queueItem.getPromisedHash());
        }
        fluidChunkGenerationQueue.clear();
        lock.unlock();
        Globals.profiler.endCpuSample();
    }

    /**
     * Parses a byte buffer into a fluid data object
     * @param buffer the buffer
     * @return the object
     */
    private FluidChunkData parseFluidDataBuffer(ByteBuffer buffer){
        FluidChunkData data = new FluidChunkData();
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        float homogenous = buffer.getFloat();
        if(homogenous == ServerFluidChunk.IS_HOMOGENOUS){
            data.setHomogenous(true);
        } else {
            data.setHomogenous(false);
            data.allocateBuffs();
            for(int x = 0; x < FluidChunkData.CHUNK_SIZE; x++){
                for(int y = 0; y < FluidChunkData.CHUNK_SIZE; y++){
                    for(int z = 0; z < FluidChunkData.CHUNK_SIZE; z++){
                        data.setWeight(x,y,z,floatBuffer.get());
                        if(data.getWeight(x, y, z) <= 0){
                            data.setWeight(x, y, z, FluidCell.ISO_SURFACE_EMPTY);
                        }
                    }
                }
            }
            for(int x = 0; x < FluidChunkData.CHUNK_SIZE; x++){
                for(int y = 0; y < FluidChunkData.CHUNK_SIZE; y++){
                    for(int z = 0; z < FluidChunkData.CHUNK_SIZE; z++){
                        data.setVelocityX(x, y, z, floatBuffer.get());
                    }
                }
            }
            for(int x = 0; x < FluidChunkData.CHUNK_SIZE; x++){
                for(int y = 0; y < FluidChunkData.CHUNK_SIZE; y++){
                    for(int z = 0; z < FluidChunkData.CHUNK_SIZE; z++){
                        data.setVelocityY(x, y, z, floatBuffer.get());
                    }
                }
            }
            for(int x = 0; x < FluidChunkData.CHUNK_SIZE; x++){
                for(int y = 0; y < FluidChunkData.CHUNK_SIZE; y++){
                    for(int z = 0; z < FluidChunkData.CHUNK_SIZE; z++){
                        data.setVelocityZ(x, y, z, floatBuffer.get());
                    }
                }
            }
        }
        return data;
    }

    /**
     * Gets the number of messages processed this frame
     * @return The number of messages
     */
    public int getMessageCount(){
        return messageCount;
    }
    
}
