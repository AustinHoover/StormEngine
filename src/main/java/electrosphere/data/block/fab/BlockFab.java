package electrosphere.data.block.fab;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.joml.Vector3i;

import com.google.gson.Gson;

import electrosphere.client.block.BlockChunkData;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.meshgen.BlockMeshgenData;
import electrosphere.util.FileUtils;

/**
 * A collection of blocks
 */
public class BlockFab implements BlockMeshgenData {

    /**
     * File version format
     */
    public static final int FILE_VER = 3;

    /**
     * Size of the header for a block fab file
     * 1 * 4 for integer ID of the version of this fab file
     * 3 * 4 for the dimensions at the front of the file
     */
    public static final int HEADER_SIZE = 
    1 * 4 +
    3 * 4
    ;

    /**
     * Default file ending for block fabs
     */
    public static final String DEFAULT_FILE_ENDING = ".fab";
    
    /**
     * Dimensions of the block fab
     */
    Vector3i dimensions;

    /**
     * Block type data
     */
    short[] types;

    /**
     * Block metadata
     */
    short[] metadata;
    
    /**
     * The metadata of the fab
     */
    BlockFabMetadata fabMetadata;

    /**
     * Creates a block fab
     * @param dimensions The dimensions of the fab
     * @param types The block types
     * @param metadata The block metadata
     * @return The block fab
     */
    public static BlockFab create(Vector3i dimensions, short[] types, short[] metadata){
        BlockFab rVal = new BlockFab();
        rVal.dimensions = dimensions;
        rVal.types = types;
        rVal.metadata = metadata;
        rVal.fabMetadata = new BlockFabMetadata();
        return rVal;
    }

    /**
     * Writes this fab to a file
     * @param file The file
     */
    public void write(File file){
        int blockCount = dimensions.x * dimensions.y * dimensions.z;

        Gson gson = new Gson();
        String serializedMetadata = gson.toJson(this.fabMetadata);
        byte[] serializedMetadataBytes = serializedMetadata.getBytes();

        ByteBuffer buff = ByteBuffer.allocate(HEADER_SIZE + blockCount * BlockChunkData.BYTES_PER_BLOCK + serializedMetadataBytes.length);

        buff.putInt(FILE_VER);
        buff.putInt(dimensions.x);
        buff.putInt(dimensions.y);
        buff.putInt(dimensions.z);
        
        ShortBuffer shortView = buff.asShortBuffer();

        shortView.put(types);
        shortView.put(metadata);
        buff.position(HEADER_SIZE + types.length * 2 + metadata.length * 2);
        buff.put(serializedMetadataBytes);
        shortView.flip();

        try {
            FileUtils.writeBufferToCompressedFile(file, buff);
        } catch (IOException e) {
            throw new Error("Failed to export selected blocks to a file!");
        }
    }

    /**
     * Reads a BlockFab from a specified file
     * @param file The file
     * @return The BlockFab
     */
    public static BlockFab read(File file){
        BlockFab rVal = null;
        ByteBuffer buff;
        try {
            buff = FileUtils.readBufferFromCompressedFile(file);
            int fileVer = buff.getInt();
            LoggerInterface.loggerFileIO.DEBUG("Read fab file with ver " + fileVer);
            if(fileVer != FILE_VER){
                LoggerInterface.loggerFileIO.WARNING("Reading unsupported fab file with version: " + fileVer);
            }

            int dimX = buff.getInt();
            int dimY = buff.getInt();
            int dimZ = buff.getInt();
            
            Vector3i dims = new Vector3i(dimX, dimY, dimZ);
            buff.position(HEADER_SIZE);

            int blockCount = dims.x * dims.y * dims.z;
            short[] types = new short[blockCount];
            short[] metadata = new short[blockCount];
            int i = 0;
            for(int x = 0; x < dims.x; x++){
                for(int y = 0; y < dims.y; y++){
                    for(int z = 0; z < dims.z; z++){
                        types[i] = buff.getShort();
                        i++;
                    }
                }
            }
            i = 0;
            for(int x = 0; x < dims.x; x++){
                for(int y = 0; y < dims.y; y++){
                    for(int z = 0; z < dims.z; z++){
                        metadata[i] = buff.getShort();
                        i++;
                    }
                }
            }

            //read the fab metadata
            BlockFabMetadata fabMetadata = new BlockFabMetadata();
            if(buff.remaining() > 0){
                byte[] fabMetadataBytes = new byte[buff.remaining()];
                buff.get(fabMetadataBytes);
                String fabMetadataString = new String(fabMetadataBytes);
                Gson gson = new Gson();
                fabMetadata = gson.fromJson(fabMetadataString, BlockFabMetadata.class);
            } else {
                LoggerInterface.loggerFileIO.WARNING("Fab file does not have metadata defined! " + file.getAbsolutePath());
            }

            //construct returned object
            rVal = new BlockFab();
            rVal.dimensions = dims;
            rVal.types = types;
            rVal.metadata = metadata;
            rVal.fabMetadata = fabMetadata;
        } catch (IOException e) {
            LoggerInterface.loggerFileIO.ERROR(e);
            throw new Error("Failed to read BlockFab " + file);
        }
        return rVal;
    }

    /**
     * Gets the dimensions of the fab
     * @return The dimensions of the fab
     */
    public Vector3i getDimensions() {
        return dimensions;
    }

    /**
     * Gets the type data of the fab
     * @return The type data
     */
    public short[] getTypes() {
        return types;
    }

    /**
     * Gets the metadata of the fab
     * @return The metadata
     */
    public short[] getMetadata() {
        return metadata;
    }

    @Override
    public boolean isEmpty(int x, int y, int z){
        boolean empty = this.getType(x,y,z) == BlockChunkData.BLOCK_TYPE_EMPTY;
        return empty;
    }

    @Override
    public short getType(int x, int y, int z) {
        if(x < 0 || y < 0 || z < 0){
            throw new Error("Negative bounds! " + x + " " + y + " " + z);
        }
        if(x >= dimensions.x || y >= dimensions.y || z >= dimensions.z){
            throw new Error("Out of bounds! " + x + " " + y + " " + z);
        }
        return this.types[x * dimensions.y * dimensions.z + y * dimensions.z + z];
    }

    /**
     * Gets the fab metadata for the fab
     * @return The metadata
     */
    public BlockFabMetadata getFabMetadata(){
        return fabMetadata;
    }

}
