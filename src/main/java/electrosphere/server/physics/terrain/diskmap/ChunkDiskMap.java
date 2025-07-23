package electrosphere.server.physics.terrain.diskmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.physics.terrain.manager.ServerChunkCache;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.saves.SaveUtils;
import electrosphere.util.FileUtils;
import electrosphere.util.annotation.Exclude;

/**
 * An interface for accessing the disk map of chunk information
 */
public class ChunkDiskMap {

    /**
     * Name of the map file
     */
    static final String MAP_FILE_NAME = "voxelchunk.json";

    /**
     * The directory that stores the voxel data files
     */
    static final String VOXEL_DATA_DIR = "/terrain";

    /**
     * The map of world position+chunk type to the file that actually houses that information
     */
    Map<String,String> worldPosFileMap;

    /**
     * Locks the chunk disk map for thread safety
     */
    @Exclude
    ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor
     */
    private ChunkDiskMap(){
        worldPosFileMap = new HashMap<String,String>();
    }

    /**
     * Gets a key for a given chunk file based on a world coordinate
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return The key
     */
    private static String getTerrainChunkKey(int worldX, int worldY, int worldZ){
        return worldX + "_" + worldY + "_" + worldZ + "t";
    }

    /**
     * Gets a key for a given chunk file based on a world coordinate
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return The key
     */
    private static String getFluidChunkKey(int worldX, int worldY, int worldZ){
        return worldX + "_" + worldY + "_" + worldZ + "f";
    }

    /**
     * Initializes a diskmap based on a given save name
     * @param saveName The save name
     */
    public static ChunkDiskMap init(String saveName){
        ChunkDiskMap rVal = null;
        LoggerInterface.loggerEngine.DEBUG("INIT CHUNK MAP " + saveName);
        if(FileUtils.getSaveFile(saveName, MAP_FILE_NAME).exists()){
            rVal = FileUtils.loadObjectFromSavePath(saveName, MAP_FILE_NAME, ChunkDiskMap.class);
            LoggerInterface.loggerEngine.DEBUG("POS FILE MAP: " + rVal.worldPosFileMap.keySet());

            //make sure the subfolder for chunk files exists
            String dirPath = SaveUtils.deriveSaveDirectoryPath(Globals.serverState.currentSave.getName());
            if(!Files.exists(new File(dirPath + VOXEL_DATA_DIR).toPath())){
                try {
                    Files.createDirectories(new File(dirPath + VOXEL_DATA_DIR).toPath());
                } catch (IOException e) {
                    LoggerInterface.loggerFileIO.ERROR(e);
                }
            }
        } else {
            rVal = new ChunkDiskMap();
        }
        return rVal;
    }

    /**
     * Initializes a diskmap based on a given save name
     * @param saveName The save name
     */
    public static ChunkDiskMap init(){
        return new ChunkDiskMap();
    }

    /**
     * Saves the disk map to disk
     */
    public void save(){
        FileUtils.serializeObjectToSavePath(Globals.serverState.currentSave.getName(), MAP_FILE_NAME, this);
    }

    /**
     * Checks if the map contains a given chunk position
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @param stride The stride of the chunk
     * @return True if the map contains the chunk, false otherwise
     */
    public boolean containsTerrainAtPosition(int worldX, int worldY, int worldZ, int stride){
        if(stride != ServerChunkCache.STRIDE_FULL_RES){
            throw new Error("Server chunk diskmap does not currently support non-full-res chunks! " + stride);
        }
        lock.lock();
        boolean rVal = worldPosFileMap.containsKey(getTerrainChunkKey(worldX, worldY, worldZ));
        lock.unlock();
        return rVal;
    }

    /**
     * Checks if the map contains a given chunk position
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return True if the map contains the chunk, false otherwise
     */
    public boolean containsFluidAtPosition(int worldX, int worldY, int worldZ){
        lock.lock();
        boolean rVal = worldPosFileMap.containsKey(getFluidChunkKey(worldX, worldY, worldZ));
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the server terrain chunk from disk if it exists, otherwise returns null
     * @param worldX The x coordinate
     * @param worldY The y coordinate
     * @param worldZ The z coordinate
     * @return The server terrain chunk if it exists, null otherwise
     */
    public ServerTerrainChunk getTerrainChunk(int worldX, int worldY, int worldZ, int stride){
        if(stride != ServerChunkCache.STRIDE_FULL_RES){
            throw new Error("Server chunk diskmap does not currently support non-full-res chunks! " + stride);
        }
        lock.lock();
        LoggerInterface.loggerEngine.INFO("Load chunk " + worldX + " " + worldY + " " + worldZ);
        ServerTerrainChunk rVal = null;
        if(this.containsTerrainAtPosition(worldX, worldY, worldZ, stride)){
            //read file
            String fileName = worldPosFileMap.get(ChunkDiskMap.getTerrainChunkKey(worldX, worldY, worldZ));
            byte[] rawDataCompressed = FileUtils.loadBinaryFromSavePath(Globals.serverState.currentSave.getName(), fileName);
            //decompress
            byte[] rawData = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InflaterOutputStream inflaterInputStream = new InflaterOutputStream(out);
            try {
                inflaterInputStream.write(rawDataCompressed);
                inflaterInputStream.flush();
                inflaterInputStream.close();
                rawData = out.toByteArray();
            } catch (IOException e) {
                LoggerInterface.loggerFileIO.ERROR(e);
            }
            //parse
            if(rawData != null){
                ByteBuffer buffer = ByteBuffer.wrap(rawData);
                FloatBuffer floatView = buffer.asFloatBuffer();
                int DIM = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE;
                float[][][] weights = new float[DIM][DIM][DIM];
                int[][][] values = new int[DIM][DIM][DIM];
                for(int x = 0; x < DIM; x++){
                    for(int y = 0; y < DIM; y++){
                        for(int z = 0; z < DIM; z++){
                            weights[x][y][z] = floatView.get();
                        }
                    }
                }
                IntBuffer intView = buffer.asIntBuffer();
                intView.position(DIM * DIM * DIM);
                int firstType = -1;
                boolean homogenous = true;
                for(int x = 0; x < DIM; x++){
                    for(int y = 0; y < DIM; y++){
                        for(int z = 0; z < DIM; z++){
                            values[x][y][z] = intView.get();
                            if(firstType == -1){
                                firstType = values[x][y][z];
                            } else if(homogenous && firstType == values[x][y][z]){
                                homogenous = false;
                            }
                        }
                    }
                }
                rVal = new ServerTerrainChunk(worldX, worldY, worldZ, homogenous ? firstType : ChunkData.NOT_HOMOGENOUS, weights, values);
            }
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Saves a terrain chunk to disk
     * @param terrainChunk The terrain chunk
     */
    public void saveToDisk(ServerTerrainChunk terrainChunk){
        lock.lock();
        LoggerInterface.loggerEngine.DEBUG("Save to disk: " + terrainChunk.getWorldX() + " " + terrainChunk.getWorldY() + " " + terrainChunk.getWorldZ());
        //get the file name for this chunk
        String fileName = null;
        String chunkKey = ChunkDiskMap.getTerrainChunkKey(terrainChunk.getWorldX(),terrainChunk.getWorldY(),terrainChunk.getWorldZ());
        if(worldPosFileMap.containsKey(chunkKey)){
            fileName = worldPosFileMap.get(chunkKey);
        } else {
            fileName = VOXEL_DATA_DIR + "/" + chunkKey + ".dat";
        }
        //generate binary for the file
        float[][][] weights = terrainChunk.getWeights();
        int[][][] values = terrainChunk.getValues();
        int DIM = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(DIM * DIM * DIM * 4 + DIM * DIM * DIM * 4);
        FloatBuffer floatView = buffer.asFloatBuffer();
        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    floatView.put(weights[x][y][z]);
                }
            }
        }
        buffer.position(DIM * DIM * DIM * 4);
        IntBuffer intView = buffer.asIntBuffer();
        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    intView.put(values[x][y][z]);
                }
            }
        }
        //compress
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterInputStream = new DeflaterOutputStream(out);
        try {
            deflaterInputStream.write(buffer.array());
            deflaterInputStream.flush();
            deflaterInputStream.close();
            //write to disk
            FileUtils.saveBinaryToSavePath(Globals.serverState.currentSave.getName(), fileName, out.toByteArray());
            //save to the map of filenames
            worldPosFileMap.put(chunkKey,fileName);
        } catch (IOException e) {
            LoggerInterface.loggerFileIO.ERROR(e);
        }
        lock.unlock();
    }

}
