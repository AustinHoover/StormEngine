package electrosphere.server.physics.block.diskmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import electrosphere.client.block.BlockChunkData;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.mem.BlockChunkPool;
import electrosphere.util.FileUtils;
import electrosphere.util.annotation.Exclude;
import electrosphere.util.math.HashUtils;

/**
 * An interface for accessing the disk map of chunk information
 */
public class ServerBlockChunkDiskMap {

    /**
     * Name of the map file
     */
    static final String MAP_FILE_NAME = "blockchunk.json";

    /**
     * Directory that stores block data
     */
    static final String BLOCK_DATA_DIR = "/block/";

    /**
     * 1 x 4 int that stores whether it is a homogenous chunk or not
     */
    static final int FILE_HEADER = 4;
    
    /**
     * Header value for it being a non-homogenous chunk
     */
    static final int HEADER_NON_HOMOGENOUS = 0;

    /**
     * Header value for it being a homogenous chunk
     */
    static final int HEADER_HOMOGENOUS = 1;

    /**
     * Total size of the file potentially
     */
    static final int FILE_MAX_SIZE = FILE_HEADER + BlockChunkData.TOTAL_DATA_WIDTH * (2 * 2);

    /**
     * The map of world position+chunk type to the file that actually houses that information
     */
    private Map<Long,String> worldPosFileMap;

    /**
     * Locks the chunk disk map for thread safety
     */
    @Exclude
    private ReentrantLock lock = new ReentrantLock();

    /**
     * The buffer used for writing out files
     */
    @Exclude
    private ByteBuffer outputBuffer;

    /**
     * Buffer for compression input
     */
    @Exclude
    private ByteBuffer compressInputBuffer;

    /**
     * The buffer used for reading in files
     */
    @Exclude
    private ByteBuffer inputBuffer;

    /**
     * Deflater used for compressing outgoing files
     */
    @Exclude
    private Deflater deflater;

    /**
     * Inflater used for decompressing incoming files
     */
    @Exclude
    private Inflater inflater;

    /**
     * Constructor
     */
    private ServerBlockChunkDiskMap(){
        worldPosFileMap = new HashMap<Long,String>();
        outputBuffer = ByteBuffer.allocate(FILE_MAX_SIZE);
        compressInputBuffer = ByteBuffer.allocate(FILE_MAX_SIZE);
        inputBuffer = ByteBuffer.allocate(FILE_MAX_SIZE);
        deflater = new Deflater();
        deflater.setInput(compressInputBuffer);
        inflater = new Inflater();
        inflater.setInput(compressInputBuffer);
    }

    /**
     * Gets a key for a given chunk file based on a world coordinate
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return The key
     */
    private static long getBlockChunkKey(int worldX, int worldY, int worldZ){
        return HashUtils.hashIVec(worldX, worldY, worldZ);
    }

    /**
     * Initializes a diskmap based on a given save name
     * @param saveName The save name
     */
    public static ServerBlockChunkDiskMap init(String saveName){
        ServerBlockChunkDiskMap rVal = null;
        LoggerInterface.loggerEngine.DEBUG("INIT CHUNK MAP " + saveName);
        if(FileUtils.getSaveFile(saveName, MAP_FILE_NAME).exists()){
            rVal = FileUtils.loadObjectFromSavePath(saveName, MAP_FILE_NAME, ServerBlockChunkDiskMap.class);
            LoggerInterface.loggerEngine.DEBUG("POS FILE MAP: " + rVal.worldPosFileMap.keySet());
        } else {
            rVal = new ServerBlockChunkDiskMap();
        }
        return rVal;
    }

    /**
     * Initializes a diskmap based on a given save name
     * @param saveName The save name
     */
    public static ServerBlockChunkDiskMap init(){
        return new ServerBlockChunkDiskMap();
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
     * @return True if the map contains the chunk, false otherwise
     */
    public boolean containsBlocksAtPosition(int worldX, int worldY, int worldZ){
        lock.lock();
        boolean rVal = worldPosFileMap.containsKey(getBlockChunkKey(worldX, worldY, worldZ));
        lock.unlock();
        return rVal;
    }


    /**
     * Gets the block data chunk from disk if it exists, otherwise returns null
     * @param worldX The x coordinate
     * @param worldY The y coordinate
     * @param worldZ The z coordinate
     * @return The block data chunk if it exists, null otherwise
     */
    public BlockChunkData getBlockChunk(int worldX, int worldY, int worldZ){
        lock.lock();
        LoggerInterface.loggerEngine.INFO("Load chunk " + worldX + " " + worldY + " " + worldZ);
        BlockChunkData rVal = null;
        if(this.containsBlocksAtPosition(worldX, worldY, worldZ)){
            //read file
            String fileName = worldPosFileMap.get(getBlockChunkKey(worldX, worldY, worldZ));


            try {
                //Construct the channel
                InputStream inputStream = FileUtils.getSavePathAsInputStream(Globals.serverState.currentSave.getName(), fileName);
                ReadableByteChannel channel = Channels.newChannel(inputStream);

                //setup compression input buffer
                compressInputBuffer.position(0);
                compressInputBuffer.limit(FILE_MAX_SIZE);

                //Read the file into the channel
                channel.read(compressInputBuffer);
                compressInputBuffer.flip();

                //setup the inflater
                inflater.setInput(compressInputBuffer);
                
                //decompress
                inflater.inflate(inputBuffer);
                inputBuffer.flip();

                //error check
                if(!inflater.finished()){
                    throw new Error("Failed to read!");
                }

                //parse
                rVal = new BlockChunkData();


                int headerHomogenousType = inputBuffer.getInt();
                if(headerHomogenousType == HEADER_NON_HOMOGENOUS){

                    //read a non-homogenous chunk
                    ShortBuffer shortView = inputBuffer.asShortBuffer();
                    short[] type = BlockChunkPool.getShort();
                    short[] metadata = BlockChunkPool.getShort();
                    short firstType = -1;
                    boolean homogenous = true;
                    for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                        type[i] = shortView.get();
                        if(firstType == -1){
                            firstType = type[i];
                        } else if(homogenous && firstType == type[i]){
                            homogenous = false;
                        }
                    }
                    for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                        metadata[i] = shortView.get();
                    }

                    
                    rVal.setType(type);
                    rVal.setMetadata(metadata);
                    rVal.setHomogenousValue(homogenous ? firstType : BlockChunkData.NOT_HOMOGENOUS);
                } else {

                    //read a homogenous chunk
                    short homogenousValue = inputBuffer.getShort();
                    rVal.setHomogenousValue(homogenousValue);
                }

                //set metadata
                rVal.setWorldX(worldX);
                rVal.setWorldY(worldY);
                rVal.setWorldZ(worldZ);
                rVal.setLod(BlockChunkData.LOD_FULL_RES);


                //close channel
                channel.close();
                inputStream.close();

                //reset buffers
                inflater.reset();
                compressInputBuffer.position(0);
                compressInputBuffer.limit(FILE_MAX_SIZE);
                inputBuffer.position(0);
                inputBuffer.limit(FILE_MAX_SIZE);
            } catch (IOException ex){
                inflater.reset();
                compressInputBuffer.position(0);
                compressInputBuffer.limit(FILE_MAX_SIZE);
                inputBuffer.position(0);
                inputBuffer.limit(FILE_MAX_SIZE);
                LoggerInterface.loggerFileIO.ERROR(ex);
            } catch (DataFormatException e) {
                inflater.reset();
                compressInputBuffer.position(0);
                compressInputBuffer.limit(FILE_MAX_SIZE);
                inputBuffer.position(0);
                inputBuffer.limit(FILE_MAX_SIZE);
                LoggerInterface.loggerFileIO.ERROR(e);
            }
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Saves a block data chunk to disk
     * @param chunkData The block data chunk
     */
    public void saveToDisk(BlockChunkData chunkData){
        lock.lock();
        LoggerInterface.loggerEngine.DEBUG("Save to disk: " + chunkData.getWorldX() + " " + chunkData.getWorldY() + " " + chunkData.getWorldZ());
        //get the file name for this chunk
        String fileName = null;
        Long chunkKey = getBlockChunkKey(chunkData.getWorldX(),chunkData.getWorldY(),chunkData.getWorldZ());
        if(worldPosFileMap.containsKey(chunkKey)){
            fileName = worldPosFileMap.get(chunkKey);
        } else {
            fileName = BLOCK_DATA_DIR + chunkKey + "b.dat";
        }
        //compress
        try {
            //generate binary for the file
            short[] type = chunkData.getType();
            short[] metadata = chunkData.getMetadata();

            //push data
            compressInputBuffer.position(0);
            if(chunkData.getHomogenousValue() == BlockChunkData.NOT_HOMOGENOUS){
                //put header
                compressInputBuffer.putInt(HEADER_NON_HOMOGENOUS);
                //put data
                for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                    compressInputBuffer.putShort(type[i]);
                }
                for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                    compressInputBuffer.putShort(metadata[i]);
                }
            } else {
                //put header
                compressInputBuffer.putInt(HEADER_HOMOGENOUS);
                //put data
                compressInputBuffer.putShort(chunkData.getHomogenousValue());
            }
            compressInputBuffer.flip();

            //setup deflater
            deflater.setInput(compressInputBuffer);
            deflater.finish();

            //construct channel
            OutputStream out = FileUtils.getBinarySavePathOutputStream(Globals.serverState.currentSave.getName(), fileName);
            WritableByteChannel channel = Channels.newChannel(out);

            //write
            while(!deflater.finished()){
                deflater.deflate(outputBuffer);
                outputBuffer.flip();
                channel.write(outputBuffer);
            }

            //flush and close
            channel.close();
            out.flush();
            out.close();

            //reset buffers
            deflater.reset();
            compressInputBuffer.position(0);
            compressInputBuffer.limit(FILE_MAX_SIZE);
            outputBuffer.position(0);
            outputBuffer.limit(FILE_MAX_SIZE);

            //save to the map of filenames
            worldPosFileMap.put(chunkKey,fileName);
        } catch (IOException e) {
            deflater.reset();
            compressInputBuffer.position(0);
            compressInputBuffer.limit(FILE_MAX_SIZE);
            outputBuffer.position(0);
            outputBuffer.limit(FILE_MAX_SIZE);
            LoggerInterface.loggerFileIO.ERROR(e);
        }
        lock.unlock();
    }

}
