package electrosphere.server.physics.fluid.diskmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.FileUtils;

/**
 * An interface for accessing the disk map of chunk information
 */
public class FluidDiskMap {

    //The map of world position+chunk type to the file that actually houses that information
    Map<String,String> worldPosFileMap = new HashMap<String,String>();

    /**
     * Constructor
     */
    public FluidDiskMap(){

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
    public void init(String saveName){
        LoggerInterface.loggerEngine.DEBUG("INIT CHUNK MAP " + saveName);
        if(FileUtils.getSaveFile(saveName, "chunk.map").exists()){
            worldPosFileMap = FileUtils.loadObjectFromSavePath(saveName, "fluid.map", FluidDiskMap.class).worldPosFileMap;
            LoggerInterface.loggerEngine.DEBUG("POS FILE MAP: " + worldPosFileMap.keySet());
        } else {
            worldPosFileMap = new HashMap<String,String>();
        }
    }

    /**
     * Saves the disk map to disk
     */
    public void save(){
        FileUtils.serializeObjectToSavePath(Globals.serverState.currentSave.getName(), "fluid.map", this);
    }

    /**
     * Checks if the map contains a given chunk position
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return True if the map contains the chunk, false otherwise
     */
    public boolean containsFluidAtPosition(int worldX, int worldY, int worldZ){
        return worldPosFileMap.containsKey(getFluidChunkKey(worldX, worldY, worldZ));
    }

    /**
     * Gets the server fluid chunk from disk if it exists, otherwise returns null
     * @param worldX The x coordinate
     * @param worldY The y coordinate
     * @param worldZ The z coordinate
     * @return The server fluid chunk if it exists, null otherwise
     */
    public ServerFluidChunk getFluidChunk(int worldX, int worldY, int worldZ){
        LoggerInterface.loggerEngine.INFO("Load chunk " + worldX + " " + worldY + " " + worldZ);
        ServerFluidChunk rVal = null;
        if(containsFluidAtPosition(worldX, worldY, worldZ)){
            //read file
            String fileName = worldPosFileMap.get(getFluidChunkKey(worldX, worldY, worldZ));
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
                int DIM = ServerFluidChunk.BUFFER_DIM;
                rVal = new ServerFluidChunk(worldX, worldY, worldZ);
                for(int x = 0; x < DIM; x++){
                    for(int y = 0; y < DIM; y++){
                        for(int z = 0; z < DIM; z++){
                            rVal.setWeight(x, y, z, floatView.get());
                        }
                    }
                }
                for(int x = 0; x < DIM; x++){
                    for(int y = 0; y < DIM; y++){
                        for(int z = 0; z < DIM; z++){
                            rVal.setVelocityX(x, y, z, floatView.get());
                        }
                    }
                }
                for(int x = 0; x < DIM; x++){
                    for(int y = 0; y < DIM; y++){
                        for(int z = 0; z < DIM; z++){
                            rVal.setVelocityY(x, y, z, floatView.get());
                        }
                    }
                }
                for(int x = 0; x < DIM; x++){
                    for(int y = 0; y < DIM; y++){
                        for(int z = 0; z < DIM; z++){
                            rVal.setVelocityZ(x, y, z, floatView.get());
                        }
                    }
                }
            }
        }
        return rVal;
    }

    /**
     * Saves a fluid chunk to disk
     * @param fluidChunk The fluid chunk
     */
    public void saveToDisk(ServerFluidChunk fluidChunk){
        LoggerInterface.loggerEngine.DEBUG("Save to disk: " + fluidChunk.getWorldX() + " " + fluidChunk.getWorldY() + " " + fluidChunk.getWorldZ());
        //get the file name for this chunk
        String fileName = null;
        String chunkKey = getFluidChunkKey(fluidChunk.getWorldX(),fluidChunk.getWorldY(),fluidChunk.getWorldZ());
        if(worldPosFileMap.containsKey(chunkKey)){
            fileName = worldPosFileMap.get(chunkKey);
        } else {
            fileName = chunkKey + ".dat";
        }
        //generate binary for the file
        int DIM = ServerTerrainChunk.CHUNK_DIMENSION;
        ByteBuffer buffer = ByteBuffer.allocate(DIM * DIM * DIM * 4 + DIM * DIM * DIM * 4 + DIM * DIM * DIM * 4 + DIM * DIM * DIM * 4);
        FloatBuffer floatView = buffer.asFloatBuffer();
        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    floatView.put(fluidChunk.getWeight(x, y, z));
                }
            }
        }
        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    floatView.put(fluidChunk.getVelocityX(x,y,z));
                }
            }
        }
        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    floatView.put(fluidChunk.getVelocityY(x,y,z));
                }
            }
        }
        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    floatView.put(fluidChunk.getVelocityZ(x,y,z));
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
    }

}
