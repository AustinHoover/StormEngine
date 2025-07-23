package electrosphere.server.physics.terrain.manager;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.engine.Globals;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.entity.scene.RealmDescriptor;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.MacroData;
import electrosphere.server.physics.terrain.diskmap.ChunkDiskMap;
import electrosphere.server.physics.terrain.generation.ProceduralChunkGenerator;
import electrosphere.server.physics.terrain.generation.interfaces.ChunkGenerator;
import electrosphere.server.physics.terrain.generation.macro.DefaultMacroGenerator;
import electrosphere.server.physics.terrain.generation.macro.HomogenousMacroGenerator;
import electrosphere.server.physics.terrain.generation.macro.MacroGenerator;
import electrosphere.server.physics.terrain.models.TerrainModel;
import electrosphere.server.physics.terrain.models.TerrainModification;
import electrosphere.util.FileUtils;
import electrosphere.util.annotation.Exclude;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.joml.Vector3d;
import org.joml.Vector3i;

/**
 * Provides an interface for the server to query information about terrain
 */
public class ServerTerrainManager {
    
    /**
     * Full world discrete size
     */
    public static final int WORLD_SIZE_DISCRETE = 1000;

    /**
     * Vertical interp ratio
     */
    public static final int VERTICAL_INTERPOLATION_RATIO = 50;
    
    /**
     * the interpolation width of a server terrain manager is hard coded at the moment to make sure it's divisible by the sub chunk calculations
     */
    public static final int SERVER_TERRAIN_MANAGER_INTERPOLATION_RATIO = 128;

    /**
     * The dampening ratio for ground noise
     */
    public static final float SERVER_TERRAIN_MANAGER_DAMPENER = 1.0f;
    
    /**
     * The parent world data
     */
    protected ServerWorldData parent;

    /**
     * the seed for terrain generation
     */
    private long seed;
    
    /**
     * The model of the terrain this manager is managing
     */
    private TerrainModel model;
    
    /**
     * The cache of chunks
     */
    @Exclude
    private ServerChunkCache chunkCache;

    /**
     * The map of chunk position <-> file on disk containing chunk data
     */
    private ChunkDiskMap chunkDiskMap = null;

    /**
     * The generation algorithm for this terrain manager
     */
    @Exclude
    private ChunkGenerator chunkGenerator;

    /**
     * The macro data for this world
     */
    @Exclude
    private MacroData macroData;

    /**
     * The threadpool for chunk generation
     */
    @Exclude
    private ExecutorService chunkExecutorService = Globals.engineState.threadManager.requestFixedThreadPool(ThreadCounts.SERVER_TERRAIN_GENERATION_THREADS);    
    
    /**
     * Constructor
     */
    public ServerTerrainManager(
        ServerWorldData parent,
        long seed,
        ChunkGenerator chunkGenerator
    ){
        this.parent = parent;
        this.seed = seed;
        this.chunkGenerator = chunkGenerator;
    }
    
    /**
     * Generates a terrain model for the manager
     */
    public void generate(){
        this.model = TerrainModel.create(this.seed);
        DefaultMacroGenerator generator = new DefaultMacroGenerator();
        generator.generate(this.model);
        this.chunkGenerator.setModel(this.model);
        this.chunkDiskMap = ChunkDiskMap.init();
        this.chunkCache = new ServerChunkCache(this.chunkDiskMap);
    }

    /**
     * Generates a terrain model for the manager
     * @param realmDescriptor The realm description to draw parameters from while generating
     */
    public void generate(RealmDescriptor realmDescriptor){
        this.model = TerrainModel.create(this.seed);
        MacroGenerator generator = new DefaultMacroGenerator();
        if(realmDescriptor.getWorldType().equals(RealmDescriptor.PROCEDURAL_TYPE_HOMOGENOUS)){
            generator = new HomogenousMacroGenerator(realmDescriptor.getBiomeType());
        }
        generator.generate(this.model);
        this.chunkGenerator.setModel(this.model);
        this.chunkDiskMap = ChunkDiskMap.init();
        this.chunkCache = new ServerChunkCache(this.chunkDiskMap);
    }
    
    /**
     * Saves the terrain model backing this manager to a save file
     * @param saveName The name of the save
     */
    public void save(String saveName){
        if(model != null){
            if(model.getDiscreteArrayDimension() < 1){
                throw new Error("Invalid terrain macro data size!");
            }

            //save the model itself
            FileUtils.serializeObjectToSavePath(saveName, "./terrain.json", model);

            //save the elevation array
            ByteBuffer floatBuff = ByteBuffer.allocate(model.getDiscreteArrayDimension() * model.getDiscreteArrayDimension() * 4);
            FloatBuffer floatView = floatBuff.asFloatBuffer();
            for(int x = 0; x < model.getDiscreteArrayDimension(); x++){
                for(int y = 0; y < model.getDiscreteArrayDimension(); y++){
                    floatView.put(model.getElevation()[x][y]);
                }
            }
            if(floatView.position() > 0){
                floatView.flip();
            }
            FileUtils.saveBinaryToSavePath(saveName, "./terrain.dat", floatBuff.array());
            
            //save the biome array
            ByteBuffer shortBuff = ByteBuffer.allocate(model.getDiscreteArrayDimension() * model.getDiscreteArrayDimension() * 2);
            ShortBuffer shortView = shortBuff.asShortBuffer();
            for(int x = 0; x < model.getDiscreteArrayDimension(); x++){
                for(int y = 0; y < model.getDiscreteArrayDimension(); y++){
                    shortView.put(model.getBiome()[x][y]);
                }
            }
            if(shortView.position() > 0){
                shortView.flip();
            }
            FileUtils.saveBinaryToSavePath(saveName, "./biome.dat", shortBuff.array());
        }
        //for each chunk, save via disk map
        if(this.chunkCache != null){
            for(ServerTerrainChunk chunk : this.chunkCache.getFullRes()){
                chunkDiskMap.saveToDisk(chunk);
            }
        }
        //save disk map itself
        if(chunkDiskMap != null){
            chunkDiskMap.save();
        }
    }
    
    /**
     * Loads a terrain manager from a save file
     * @param saveName The name of the save
     */
    public void load(String saveName){
        //load terrain model
        if(FileUtils.getSaveFile(saveName, "./terrain.json").exists()){

            //read the model itself
            model = FileUtils.loadObjectFromSavePath(saveName, "./terrain.json", TerrainModel.class);
            chunkGenerator.setModel(model);
            if(model.getDiscreteArrayDimension() < 1){
                throw new Error("Invalid terrain macro data size!");
            }

            //read the elevation data
            float[][] elevation = new float[model.getDiscreteArrayDimension()][model.getDiscreteArrayDimension()];
            if(FileUtils.checkSavePathExists(saveName, "./terrain.dat")){
                byte[] data = FileUtils.loadBinaryFromSavePath(saveName, "./terrain.dat");
                ByteBuffer buffer = ByteBuffer.wrap(data);
                FloatBuffer floatView = buffer.asFloatBuffer();
                for(int x = 0; x < model.getDiscreteArrayDimension(); x++){
                    for(int y = 0; y < model.getDiscreteArrayDimension(); y++){
                        elevation[x][y] = floatView.get();
                    }
                }
            }
            model.setElevationArray(elevation);

            //read the biome data
            short[][] biome = new short[model.getDiscreteArrayDimension()][model.getDiscreteArrayDimension()];
            if(FileUtils.checkSavePathExists(saveName, "./biome.dat")){
                byte[] data = FileUtils.loadBinaryFromSavePath(saveName, "./biome.dat");
                ByteBuffer buffer = ByteBuffer.wrap(data);
                ShortBuffer shortView = buffer.asShortBuffer();
                for(int x = 0; x < model.getDiscreteArrayDimension(); x++){
                    for(int y = 0; y < model.getDiscreteArrayDimension(); y++){
                        biome[x][y] = shortView.get();
                    }
                }
            }
            model.setBiome(biome);
        }
        //load chunk disk map
        chunkDiskMap = ChunkDiskMap.init(saveName);

        //init cache
        this.chunkCache = new ServerChunkCache(this.chunkDiskMap);
    }

    /**
     * Generates a test terrain model
     * @param chunkGen The chunk generator
     */
    public void genTestData(ProceduralChunkGenerator chunkGen){
        this.model = TerrainModel.generateTestModel();
        chunkGen.setModel(model);
    }

    /**
     * Evicts all cached terrain
     */
    public void evictAll(){
        this.chunkCache.clear();
    }
    
    public float getDiscreteValue(int x, int y){
        if(model != null){
            return model.getElevation()[x][y];
        } else {
            return 0;
        }
    }

    /**
     * Gets the terrain model backing this terrain manager
     * @return The terrain model
     */
    public TerrainModel getModel() {
        return model;
    }

    /**
     * Overrides the chunk generator
     * @param generator The new chunk generator
     */
    public void overrideChunkGenerator(ChunkGenerator generator){
        this.chunkGenerator = generator;
    }
    
    /**
     * Performs logic once a server chunk is available
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @param stride The stride of the data
     * @return The ServerTerrainChunk
     */
    public ServerTerrainChunk getChunk(int worldX, int worldY, int worldZ, int stride){
        Globals.profiler.beginAggregateCpuSample("ServerTerrainManager.getChunk");
        //THIS FIRES IF THERE IS A MAIN GAME WORLD RUNNING
        ServerTerrainChunk returnedChunk = ChunkGenerationThread.getChunk(macroData, worldX, worldY, worldZ, stride, chunkDiskMap, chunkCache, chunkGenerator);
        if(returnedChunk == null){
            LoggerInterface.loggerEngine.WARNING("Failed to generate chunk at " + worldX + " " + worldY + " " + worldZ + " synchronously");
        }
        Globals.profiler.endCpuSample();
        return returnedChunk;
    }

    /**
     * Performs logic once a server chunk is available
     * @param worldX The world x position
     * @param worldZ The world z position
     * @param chunkX The chunk x position
     * @param chunkZ THe chunk z position
     * @return The ServerTerrainChunk
     */
    public double getElevation(int worldX, int worldZ, int chunkX, int chunkZ){
        Globals.profiler.beginAggregateCpuSample("ServerTerrainManager.getChunk");
        double elevation = chunkGenerator.getElevation(worldX, worldZ, chunkX, chunkZ);
        Globals.profiler.endCpuSample();
        return elevation;
    }

    /**
     * Gets the elevation at a real x and z (ignores y component)
     * @param realPos The real position
     * @return The elevation
     */
    public double getElevation(Vector3d realPos){
        Globals.profiler.beginAggregateCpuSample("ServerTerrainManager.getChunk");
        Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(realPos);
        Vector3i voxelPos = ServerWorldData.convertRealToVoxelSpace(realPos);
        double elevation = chunkGenerator.getElevation(chunkPos.x, chunkPos.z, voxelPos.x, voxelPos.z);
        Globals.profiler.endCpuSample();
        return elevation;
    }

    /**
     * Performs logic once a server chunk is available
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @param stride The stride of the data
     * @param onLoad The logic to run once the chunk is available
     */
    public void getChunkAsync(int worldX, int worldY, int worldZ, int stride, Consumer<ServerTerrainChunk> onLoad){
        Globals.profiler.beginAggregateCpuSample("ServerTerrainManager.getChunkAsync");
        chunkExecutorService.submit(new ChunkGenerationThread(this.macroData, chunkDiskMap, chunkCache, chunkGenerator, worldX, worldY, worldZ, stride, onLoad));
        Globals.profiler.endCpuSample();
    }

    /**
     * Saves a given position's chunk to disk. 
     * Uses the current global save name
     * @param position The position to save
     */
    public void savePositionToDisk(Vector3i position){
        if(chunkDiskMap != null && chunkCache.containsChunk(position.x, position.y, position.z, ChunkData.NO_STRIDE)){
            chunkDiskMap.saveToDisk(this.getChunk(position.x, position.y, position.z, ServerChunkCache.STRIDE_FULL_RES));
        }
    }
    
    /**
     * Applies a deform to terrain at a given location
     * @param worldPos The world coordinates of the chunk to modify
     * @param voxelPos The voxel coordinates of the voxel to modify
     * @param weight The weight to set it to
     * @param value The value to set it to
     */
    public void deformTerrainAtLocationToValue(Vector3i worldPos, Vector3i voxelPos, float weight, int value){
        TerrainModification modification = new TerrainModification(worldPos,voxelPos,weight,value);
        if(chunkCache.containsChunk(worldPos.x,worldPos.y,worldPos.z,ChunkData.NO_STRIDE)){
            ServerTerrainChunk chunk = chunkCache.get(worldPos.x,worldPos.y,worldPos.z, ChunkData.NO_STRIDE);
            chunk.addModification(modification);
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
     * Gets the chunk generator of the terrain manager
     * @return The chunk generator
     */
    public ChunkGenerator getChunkGenerator(){
        return chunkGenerator;
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
