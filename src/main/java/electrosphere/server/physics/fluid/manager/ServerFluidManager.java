package electrosphere.server.physics.fluid.manager;


import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.physics.fluid.diskmap.FluidDiskMap;
import electrosphere.server.physics.fluid.generation.FluidGenerator;
import electrosphere.server.physics.fluid.models.FluidModel;
import electrosphere.server.physics.fluid.simulator.FluidAcceleratedSimulator;
import electrosphere.server.physics.fluid.simulator.ServerFluidSimulator;
import electrosphere.server.physics.terrain.manager.ServerChunkCache;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;
import electrosphere.util.FileUtils;
import electrosphere.util.annotation.Exclude;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.joml.Vector3i;

/**
 * Provides an interface for the server to query information about fluid
 */
public class ServerFluidManager {

    /**
     * DEfault size of the cache
     */
    static final int DEFAULT_CACHE_SIZE = 50;

    /**
     * The number of frames to wait between updates
     */
    static final int UPDATE_RATE = 0;
        
    //the seed for the water    
    long seed;
    
    //The model of the fluid this manager is managing
    FluidModel model;
    
    
    //In memory cache of chunk data
    //Basic idea is we associate string that contains chunk x&y&z with elevation
    //While we incur a penalty with converting ints -> string, think this will
    //offset regenerating the array every time we want a new one
    int cacheSize = DEFAULT_CACHE_SIZE;
    @Exclude
    Map<String, ServerFluidChunk> chunkCache = new HashMap<String, ServerFluidChunk>();
    @Exclude
    List<String> chunkCacheContents = new LinkedList<String>();

    //The map of chunk position <-> file on disk containing chunk data
    FluidDiskMap chunkDiskMap = null;

    @Exclude
    //The generation algorithm for this fluid manager
    FluidGenerator chunkGenerator;

    @Exclude
    //the fluid simulator
    ServerFluidSimulator serverFluidSimulator;

    @Exclude
    //the terrain manager associated
    ServerTerrainManager serverTerrainManager;

    //controls whether fluid simulation should actually happen or not
    boolean simulate = EngineState.EngineFlags.RUN_FLUIDS;

    @Exclude
    /**
     * The parent world data
     */
    ServerWorldData parent;

    @Exclude
    /**
     * Locks the fluid manager
     */
    ReentrantLock lock = new ReentrantLock();

    @Exclude
    /**
     * The queued of chunks to simulate
     */
    List<ServerFluidChunk> simulationQueue = new ArrayList<ServerFluidChunk>();

    @Exclude
    /**
     * The queue of chunks to broadcast
     */
    List<ServerFluidChunk> broadcastQueue = new ArrayList<ServerFluidChunk>();

    @Exclude
    /**
     * The update frame-skipping tracking variable
     */
    int updatePhase = 0;

    @Exclude
    /**
     * The number of chunks broadcast this frame
     */
    int broadcastSize = 0;

    @Exclude
    /**
     * The number of chunks active this frame
     */
    int activeChunkCount = 0;
    
    
    /**
     * Constructor
     */
    public ServerFluidManager(
        ServerWorldData parent,
        ServerTerrainManager serverTerrainManager,
        long seed,
        FluidGenerator chunkGenerator
    ){
        this.parent = parent;
        this.serverTerrainManager = serverTerrainManager;
        this.seed = seed;
        this.chunkGenerator = chunkGenerator;
        this.serverFluidSimulator = new FluidAcceleratedSimulator();
    }
    
    ServerFluidManager(){
        
    }
    
    /**
     * Saves the fluid model backing this manager to a save file
     * @param saveName The name of the save
     */
    public void save(String saveName){
        lock.lock();
        if(model != null){
            ByteBuffer buffer = ByteBuffer.allocate(model.getElevation().length * model.getElevation()[0].length * 4);
            FloatBuffer floatView = buffer.asFloatBuffer();
            for(int x = 0; x < model.getElevation().length; x++){
                floatView.put(model.getElevation()[x]);
            }
            if(floatView.position() > 0){
                floatView.flip();
            }
            FileUtils.saveBinaryToSavePath(saveName, "./fluid.dat", buffer.array());
            FileUtils.serializeObjectToSavePath(saveName, "./fluid.json", model);
        }
        //for each chunk, save via disk map
        for(String chunkKey : chunkCacheContents){
            ServerFluidChunk chunk = chunkCache.get(chunkKey);
            chunkDiskMap.saveToDisk(chunk);
        }
        //save disk map itself
        if(chunkDiskMap != null){
            chunkDiskMap.save();
        }
        lock.unlock();
    }
    
    /**
     * Loads a fluid manager from a save file
     * @param saveName The name of the save
     */
    public void load(String saveName){
        //load fluid model
        model = FileUtils.loadObjectFromSavePath(saveName, "./fluid.json", FluidModel.class);
        chunkGenerator.setModel(model);
        byte[] data = FileUtils.loadBinaryFromSavePath(saveName, "./fluid.dat");
        ByteBuffer buffer = ByteBuffer.wrap(data);
        FloatBuffer floatView = buffer.asFloatBuffer();
        float[][] elevation = new float[parent.getWorldSizeDiscrete()][parent.getWorldSizeDiscrete()];
        for(int x = 0; x < parent.getWorldSizeDiscrete(); x++){
            for(int y = 0; y < parent.getWorldSizeDiscrete(); y++){
                elevation[x][y] = floatView.get();
            }
        }
        model.setElevationArray(elevation);
        //load chunk disk map
        chunkDiskMap = new FluidDiskMap();
        chunkDiskMap.init(saveName);
    }
    
    /**
     * Gets the key for a given world position
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return The key
     */
    public String getKey(int worldX, int worldY, int worldZ){
        return worldX + "_" + worldY + "_" + worldZ;
    }
    
    /**
     * Gets a server fluid chunk
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @return The ServerFluidChunk
     */
    public ServerFluidChunk getChunk(int worldX, int worldY, int worldZ){
        ServerFluidChunk returnedChunk = null;
        lock.lock();
        //THIS FIRES IF THERE IS A MAIN GAME WORLD RUNNING
        String key = getKey(worldX,worldY,worldZ);
        if(chunkCache.containsKey(key)){
            chunkCacheContents.remove(key);
            chunkCacheContents.add(0, key);
            returnedChunk = chunkCache.get(key);
        } else {
            if(chunkCacheContents.size() > cacheSize){
                String oldChunkKey = chunkCacheContents.remove(chunkCacheContents.size() - 1);
                ServerFluidChunk oldChunk = chunkCache.remove(oldChunkKey);
                oldChunk.freeBuffers();
                this.linkNeighbors(null, oldChunk.getWorldX(), oldChunk.getWorldY(), oldChunk.getWorldZ());
            }
            //pull from disk if it exists
            if(chunkDiskMap != null){
                if(chunkDiskMap.containsFluidAtPosition(worldX, worldY, worldZ)){
                    returnedChunk = chunkDiskMap.getFluidChunk(worldX, worldY, worldZ);
                }
            }
            //generate if it does not exist
            if(returnedChunk == null){
                returnedChunk = this.generateChunk(worldX, worldY, worldZ);
            }
            chunkCache.put(key, returnedChunk);
            chunkCacheContents.add(key);
        }
        lock.unlock();
        return returnedChunk;
    }

    /**
     * Saves a given position's chunk to disk. 
     * Uses the current global save name
     * @param position The position to save
     */
    public void savePositionToDisk(Vector3i position){
        lock.lock();
        chunkDiskMap.saveToDisk(getChunk(position.x, position.y, position.z));
        lock.unlock();
    }
    
    /**
     * Applies a deform to fluid at a given location
     * @param worldPos The world coordinates of the chunk to modify
     * @param voxelPos The voxel coordinates of the voxel to modify
     * @param weight The weight to set it to
     * @param value The value to set it to
     */
    public void deformFluidAtLocationToValue(Vector3i worldPos, Vector3i voxelPos, float weight, int value){
        if(voxelPos.x < 0 || voxelPos.y < 0 || voxelPos.z < 0){
            return;
        }
        if(worldPos.x < 0 || worldPos.y < 0 || worldPos.z < 0){
            return;
        }
        // TerrainModification modification = new TerrainModification(worldPos,voxelPos,weight,value);
        // //could be null if, for instance, arena mode
        // if(model != null){
        //     model.addModification(modification);
        // }
        // String key = getKey(worldPos.x,worldPos.y,worldPos.z);
        // if(chunkCache.containsKey(key)){
        //     ServerFluidChunk chunk = chunkCache.get(key);
        //     chunk.addModification(modification);
        // }
        lock.lock();
        ServerFluidChunk fluidChunk = this.getChunk(worldPos.x, worldPos.y, worldPos.z);
        fluidChunk.setAsleep(false);
        fluidChunk.setWeightDelta(voxelPos.x, voxelPos.y, voxelPos.z, weight);
        fluidChunk.setPressure(voxelPos.x, voxelPos.y, voxelPos.z, weight);
        lock.unlock();
    }

    /**
     * Adds a chunk to the queue to be simulated
     * @param worldX The world x coordinate of the chunk
     * @param worldY The world y coordinate of the chunk
     * @param worldZ The world z coordinate of the chunk
     */
    public void queue(int worldX, int worldY, int worldZ){
        if(simulate){
            ServerFluidChunk fluidChunk = this.getChunk(worldX, worldY, worldZ);
            if(fluidChunk.isAllocated() && !fluidChunk.isAsleep()){
                this.simulationQueue.add(fluidChunk);
                for(int i = 0; i < 27; i++){
                    if(fluidChunk.neighbors[i] != null){
                        fluidChunk.pressureIncoming[i] = fluidChunk.neighbors[i].pressureOutgoing[ServerFluidChunk.CENTER_BUFF];
                        fluidChunk.pressureOutgoing[i] = 0;
                        fluidChunk.densityIncoming[i] = fluidChunk.neighbors[i].densityOutgoing[ServerFluidChunk.CENTER_BUFF];
                        fluidChunk.densityOutgoing[i] = 0;
                    } else {
                        fluidChunk.pressureIncoming[i] = 0;
                        fluidChunk.pressureOutgoing[i] = 0;
                        fluidChunk.densityIncoming[i] = 0;
                        fluidChunk.densityOutgoing[i] = 0;
                    }
                }
            }
        }
    }

    /**
     * Simulates a chunk
     */
    public void simulate(Consumer<ServerFluidChunk> onUpdate){
        Globals.profiler.beginAggregateCpuSample("ServerFluidManager.simulate");
        lock.lock();
        if(simulate){
            if(updatePhase == UPDATE_RATE){
                if(this.serverFluidSimulator != null){
                    this.serverFluidSimulator.simulate(this.simulationQueue,this.broadcastQueue);
                }
            }

            //set active chunk size
            this.activeChunkCount = this.simulationQueue.size();
            
            //clear both queues
            while(this.simulationQueue.size() > 0){
                this.simulationQueue.remove(0);
            }
            this.broadcastSize = this.broadcastQueue.size();
            while(this.broadcastQueue.size() > 0){
                ServerFluidChunk toBroadcast = this.broadcastQueue.remove(0);
                onUpdate.accept(toBroadcast);
            }

            updatePhase++;
            if(updatePhase > UPDATE_RATE){
                updatePhase = 0;
            }
        }
        lock.unlock();
        Globals.profiler.endCpuSample();
    }

    /**
     * Generates a fluid chunk
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @return The fluid chunk
     */
    private ServerFluidChunk generateChunk(int worldX, int worldY, int worldZ){
        lock.lock();
        ServerFluidChunk rVal = chunkGenerator.generateChunk(worldX, worldY, worldZ);
        ServerWorldData serverWorldData = this.parent;
        ServerTerrainChunk terrainChunk = serverWorldData.getServerTerrainManager().getChunk(worldX, worldY, worldZ, ServerChunkCache.STRIDE_FULL_RES);
        for(int x = ServerFluidChunk.TRUE_DATA_OFFSET; x < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET; x++){
            for(int y = ServerFluidChunk.TRUE_DATA_OFFSET; y < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET; y++){
                for(int z = ServerFluidChunk.TRUE_DATA_OFFSET; z < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET; z++){
                    rVal.setBound(x, y, z, terrainChunk.getWeight(x, y, z));
                    rVal.setWeight(x, y, z, 0);
                }
            }
        }
        this.linkNeighbors(rVal, worldX, worldY, worldZ);
        lock.unlock();
        return rVal;
    }

    /**
     * Links the world position to all its neighboring fluid chunks
     * @param current The chunk to link
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     */
    private void linkNeighbors(ServerFluidChunk current, int worldX, int worldY, int worldZ){
        String key = this.getKey(worldX,worldY,worldZ);
        for(int i = -1; i < 2; i++){
            for(int j = -1; j < 2; j++){
                for(int k = -1; k < 2; k++){
                    if(i == j && j == k && k == 0){
                        continue;
                    }
                    if(
                        0 <= worldX + i && worldX + i < this.parent.getWorldSizeDiscrete() &&
                        0 <= worldY + j && worldY + j < this.parent.getWorldSizeDiscrete() &&
                        0 <= worldZ + k && worldZ + k < this.parent.getWorldSizeDiscrete()
                    ){
                        key = this.getKey(worldX + i,worldY +j,worldZ + k);
                        ServerFluidChunk neighbor = this.chunkCache.get(key);
                        if(current != null){
                            current.setNeighbor(i+1,j+1,k+1,neighbor);
                        }
                        if(neighbor != null){
                            neighbor.setNeighbor(1-i,1-j,1-k,current);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the broadcast queue
     * @return The broadcast queue
     */
    public List<ServerFluidChunk> getBroadcastQueue(){
        return this.broadcastQueue;
    }

    //getter for simulate
    public boolean getSimulate(){
        return simulate;
    }

    //setter for simulate
    public void setSimulate(boolean simulate){
        this.simulate = simulate;
    }

    /**
     * Sets the parent world data of this manager
     * @param serverWorldData The parent world data
     */
    public void setParent(ServerWorldData serverWorldData){
        this.parent = serverWorldData;
    }

    /**
     * Gets the server fluid simulator for this manager
     * @return The server fluid simulator
     */
    public ServerFluidSimulator getSimulator(){
        return serverFluidSimulator;
    }
    
    /**
     * Gets the number of chunks broadcast this frame
     * @return The number of chunks
     */
    public int getBroadcastSize(){
        return broadcastSize;
    }

    /**
     * The number of chunks active this frame
     * @return The number of chunks active this frame
     */
    public int getActiveChunkCount(){
        return this.activeChunkCount;
    }

    
}
