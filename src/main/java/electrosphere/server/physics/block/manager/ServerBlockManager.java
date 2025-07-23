package electrosphere.server.physics.block.manager;

import electrosphere.client.block.BlockChunkCache;
import electrosphere.client.block.BlockChunkData;
import electrosphere.engine.Globals;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.MacroData;
import electrosphere.server.physics.block.diskmap.ServerBlockChunkDiskMap;
import electrosphere.util.annotation.Exclude;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.joml.Vector3i;

/**
 * Provides an interface for the server to query information about block chunks
 */
public class ServerBlockManager {
    
    /**
     * The parent world data
     */
    protected ServerWorldData parent;
    
    /**
     * The cache of chunks
     */
    @Exclude
    private BlockChunkCache chunkCache;

    /**
     * The map of chunk position <-> file on disk containing chunk data
     */
    private ServerBlockChunkDiskMap chunkDiskMap = null;

    /**
     * The macro data for this world
     */
    @Exclude
    private MacroData macroData;

    /**
     * The threadpool for chunk generation
     */
    @Exclude
    private ExecutorService chunkExecutorService = Globals.engineState.threadManager.requestFixedThreadPool(ThreadCounts.SERVER_BLOCK_GENERATION_THREADS);
    
    /**
     * Constructor
     */
    public ServerBlockManager(
        ServerWorldData parent
    ){
        this.parent = parent;
    }
    
    /**
     * Inits the chunk disk map
     */
    public void generate(){
        this.chunkDiskMap = ServerBlockChunkDiskMap.init();
        this.chunkCache = new BlockChunkCache(this.chunkDiskMap);
    }
    
    /**
     * Saves the block cache backing this manager to a save file
     * @param saveName The name of the save
     */
    public void save(String saveName){
        //for each chunk, save via disk map
        if(this.chunkDiskMap != null){
            for(BlockChunkData chunk : this.chunkCache.getContents()){
                this.chunkDiskMap.saveToDisk(chunk);
            }
        }
        //save disk map itself
        if(this.chunkDiskMap != null){
            this.chunkDiskMap.save();
        }
    }
    
    /**
     * Loads a block manager from a save file
     * @param saveName The name of the save
     */
    public void load(String saveName){
        //load chunk disk map
        this.chunkDiskMap = ServerBlockChunkDiskMap.init(saveName);
        this.chunkCache = new BlockChunkCache(this.chunkDiskMap);
    }

    /**
     * Evicts all cached chunks
     */
    public void evictAll(){
        this.chunkCache.clear();
    }
    
    /**
     * Performs logic once a server chunk is available
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @return The BlockChunkData
     */
    public BlockChunkData getChunk(int worldX, int worldY, int worldZ){
        Globals.profiler.beginAggregateCpuSample("ServerBlockManager.getChunk");
        //THIS FIRES IF THERE IS A MAIN GAME WORLD RUNNING
        BlockChunkData returnedChunk = ServerBlockChunkGenerationThread.fetchOrGenerate(macroData, worldX, worldY, worldZ, BlockChunkData.LOD_FULL_RES, chunkDiskMap, chunkCache);
        Globals.profiler.endCpuSample();
        return returnedChunk;
    }

    /**
     * Performs logic once a server chunk is available
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @param stride The stride of the data
     * @param onLoad The logic to run once the chunk is available
     */
    public void getChunkAsync(int worldX, int worldY, int worldZ, int stride, Consumer<BlockChunkData> onLoad){
        Globals.profiler.beginAggregateCpuSample("ServerBlockManager.getChunkAsync");
        chunkExecutorService.submit(new ServerBlockChunkGenerationThread(this.macroData, chunkDiskMap, chunkCache, worldX, worldY, worldZ, stride, onLoad));
        Globals.profiler.endCpuSample();
    }

    /**
     * Checks if there is an already-generated chunk at the position
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @return true if the chunk exists, false otherwise
     */
    public boolean hasChunk(int worldX, int worldY, int worldZ){
        return chunkDiskMap.containsBlocksAtPosition(worldX, worldY, worldZ) || chunkCache.containsChunk(worldX, worldY, worldZ, BlockChunkData.LOD_FULL_RES);
    }

    /**
     * Saves a given position's chunk to disk. 
     * Uses the current global save name
     * @param position The position to save
     */
    public void savePositionToDisk(Vector3i position){
        if(chunkDiskMap != null){
            chunkDiskMap.saveToDisk(getChunk(position.x, position.y, position.z));
        }
    }
    
    /**
     * Applies an edit to a block at a given location
     * @param worldPos The world coordinates of the chunk to modify
     * @param voxelPos The voxel coordinates of the voxel to modify
     * @param type The type of block
     * @param metadata The metadata of the block
     */
    public void editBlockAtLocationToValue(Vector3i worldPos, Vector3i voxelPos, short type, short metadata){
        if(chunkCache.containsChunk(worldPos.x,worldPos.y,worldPos.z,BlockChunkData.LOD_FULL_RES)){
            BlockChunkData chunk = chunkCache.get(worldPos.x,worldPos.y,worldPos.z, BlockChunkData.LOD_FULL_RES);
            chunk.setType(voxelPos.x, voxelPos.y, voxelPos.z, type);
            chunk.setMetadata(voxelPos.x, voxelPos.y, voxelPos.z, metadata);
            chunk.setHomogenousValue(BlockChunkData.NOT_HOMOGENOUS);
        }
    }

    /**
     * Sets the parent world data of this manager
     * @param serverWorldData The parent world data
     */
    public void setParent(ServerWorldData serverWorldData){
        this.parent = serverWorldData;
    }
    
    /**
     * Closes the generation threadpool
     */
    public void closeThreads(){
        chunkExecutorService.shutdownNow();
    }

    /**
     * Sets the macro data for the block manager
     * @param macroData The macro data
     */
    public void setMacroData(MacroData macroData){
        this.macroData = macroData;
    }

}
