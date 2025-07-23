package electrosphere.client.block;

import org.joml.Vector3i;

import electrosphere.mem.BlockChunkPool;
import electrosphere.renderer.meshgen.BlockMeshgenData;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Stores data about a chunk of blocks
 */
public class BlockChunkData implements BlockMeshgenData {
    
    /**
     * Number of blocks in each dimension of a chunk
     */
    public static final int CHUNK_DATA_WIDTH = 64;

    /**
     * Total width of the data arrays
     */
    public static final int TOTAL_DATA_WIDTH = CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH;

    /**
     * 2 - block type
     * 2 - metadata
     */
    public static final int BYTES_PER_BLOCK = 2 * 2;

    /**
     * Size of a buffer that stores this chunk's data
     */
    public static final int BUFFER_SIZE = TOTAL_DATA_WIDTH * BYTES_PER_BLOCK;

    /**
     * The number of blocks to place within each unit of distance
     */
    public static final int BLOCKS_PER_UNIT_DISTANCE = CHUNK_DATA_WIDTH / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;

    /**
     * The amount to scale block size by
     */
    public static final float BLOCK_SIZE_MULTIPLIER = 1.0f / BLOCKS_PER_UNIT_DISTANCE;

    /**
     * Set if this chunk is not homogenous
     */
    public static final short NOT_HOMOGENOUS = -1;

    /**
     * The LOD value for a full res chunk data
     */
    public static final int LOD_FULL_RES = 0;

    /**
     * Lod value for a half res chunk
     */
    public static final int LOD_HALF_RES = 1;

    /**
     * Lod value for a quarter res chunk
     */
    public static final int LOD_QUARTER_RES = 2;

    /**
     * Lod value for a eighth res chunk
     */
    public static final int LOD_EIGHTH_RES = 3;

    /**
     * Lod value for a sixteenth res chunk
     */
    public static final int LOD_SIXTEENTH_RES = 4;

    /**
     * Lod value for the lowest resolution possible
     */
    public static final int LOD_LOWEST_RES = LOD_SIXTEENTH_RES;

    /**
     * An empty block
     */
    public static final short BLOCK_TYPE_EMPTY = 0;


    /**
     * The type of block at a given position
     */
    private short[] type;

    /**
     * Metadata about a block
     * first 4 bits are the rotation of the block)
     */
    private short[] metadata;

    /**
     * If this block chunk is homogenously a single value, it will be the value of this short. Otherwise is 
     */
    private short homogenousValue = NOT_HOMOGENOUS;

    /**
     * The level of detail of the block data
     */
    private int lod;

    /**
     * The x coordinate of the world position of the chunk
     */
    private int worldX;

    /**
     * The y coordinate of the world position of the chunk
     */
    private int worldY;

    /**
     * The z coordinate of the world position of the chunk
     */
    private int worldZ;

    /**
     * Constructor
     */
    public BlockChunkData(){
    }

    /**
     * Allocates a BlockChunkData with empty values
     */
    public static BlockChunkData allocate(){
        BlockChunkData rVal = new BlockChunkData();
        rVal.setType(BlockChunkPool.getShort());
        rVal.setMetadata(BlockChunkPool.getShort());
        return rVal;
    }

    /**
     * Allocates a BlockChunkData with a homogenous value
     */
    public static BlockChunkData allocate(short homogenousValue){
        BlockChunkData rVal = new BlockChunkData();
        rVal.homogenousValue = homogenousValue;
        return rVal;
    }

    /**
     * Allocates the arrays in a given homogenous data chunk
     */
    private void allocateFromHomogenous(){
        this.setType(BlockChunkPool.getShort());
        this.setMetadata(BlockChunkPool.getShort());
    }

    /**
     * Clones a block chunk data
     * @param other The data to clone
     * @return The cloned data
     */
    public static BlockChunkData cloneShallow(BlockChunkData other){
        BlockChunkData rVal = new BlockChunkData();
        if(other.type != null){
            rVal.type = other.type;
        }
        if(other.metadata != null){
            rVal.metadata = other.metadata;
        }
        rVal.homogenousValue = other.homogenousValue;
        rVal.lod = other.lod;
        rVal.worldX = other.worldX;
        rVal.worldY = other.worldY;
        rVal.worldZ = other.worldZ;
        return rVal;
    }

    /**
     * Gets the type data for the chunk
     * @return The type data
     */
    public short[] getType() {
        return type;
    }

    /**
     * Sets the type data for the chunk
     * @param type The type data
     */
    public void setType(short[] type) {
        if(type.length != CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH){
            throw new Error("Set type with invalid length! " + type.length);
        }
        this.type = type;
    }

    /**
     * Gets the metadata for the chunk
     * @return The metadata
     */
    public short[] getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for the chunk
     * @param metadata The metadata
     */
    public void setMetadata(short[] metadata) {
        if(metadata.length != CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH){
            throw new Error("Set metadata with invalid length! " + metadata.length);
        }
        this.metadata = metadata;
    }

    

    /**
     * Gets the type at a given position
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return The type at that position
     */
    public short getType(int x, int y, int z){
        if(this.homogenousValue != BlockChunkData.NOT_HOMOGENOUS){
            return this.homogenousValue;
        }
        if(this.type == null){
            this.allocateFromHomogenous();
        }
        return this.type[x * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH + z * CHUNK_DATA_WIDTH + y];
    }

    /**
     * Sets a specific block's type
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param type The type
     */
    public void setType(int x, int y, int z, short type){
        if(this.type == null && this.homogenousValue != type){
            this.allocateFromHomogenous();
        }
        if(this.type != null){
            this.type[x * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH + z * CHUNK_DATA_WIDTH + y] = type;
        }
    }

    /**
     * Sets a specific block's type
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param type The type
     */
    public void setType(int x, int y, int z, int type){
        if(this.type == null && this.homogenousValue != type){
            this.allocateFromHomogenous();
        }
        if(this.type != null){
            this.type[x * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH + z * CHUNK_DATA_WIDTH + y] = (short)type;
        }
    }

    /**
     * Sets a specific block's metadata
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param metadata The metadata
     */
    public void setMetadata(int x, int y, int z, short metadata){
        if(this.metadata == null && metadata != 0){
            this.allocateFromHomogenous();
        }
        if(this.metadata != null){
            this.metadata[x * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH + z * CHUNK_DATA_WIDTH + y] = metadata;
        }
    }

    /**
     * Sets a specific block's metadata
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param metadata The metadata
     */
    public void setMetadata(int x, int y, int z, int metadata){
        if(this.metadata == null && metadata != 0){
            this.allocateFromHomogenous();
        }
        if(this.metadata != null){
            this.metadata[x * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH + z * CHUNK_DATA_WIDTH + y] = (short)metadata;
        }
    }

    /**
     * Gets the metadata at a given position
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return The metadata at that position
     */
    public short getMetadata(int x, int y, int z){
        if(this.homogenousValue != BlockChunkData.NOT_HOMOGENOUS){
            return 0;
        }
        if(this.metadata == null){
            this.allocateFromHomogenous();
        }
        return this.metadata[x * CHUNK_DATA_WIDTH * CHUNK_DATA_WIDTH + z * CHUNK_DATA_WIDTH + y];
    }


    /**
     * Checks if a given location is empty
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return true if empty, false otherwise
     */
    public boolean isEmpty(int x, int y, int z){
        boolean empty = this.getType(x,y,z) == BlockChunkData.BLOCK_TYPE_EMPTY;
        return empty;
    }

    /**
     * Checks if a given location is empty
     * @param vec The position
     * @return true if empty, false otherwise
     */
    public boolean isEmpty(Vector3i vec){
        return this.isEmpty(vec.x,vec.y,vec.z);
    }
    
    /**
     * Checks if this block chunk is homogenous or not
     * @return true if it is homogenous, false otherwise
     */
    public boolean isHomogenous(){
        return this.homogenousValue != NOT_HOMOGENOUS;
    }

    /**
     * Gets the level of detail of the block data
     * @return The level of detail of the block data
     */
    public int getLod() {
        return lod;
    }

    /**
     * Sets 
     * @return 
     */
    public void setLod(int lod) {
        this.lod = lod;
    }

    /**
     * Gets the x coordinate of the world position of the chunk
     * @return The x coordinate of the world position of the chunk
     */
    public int getWorldX() {
        return worldX;
    }

    /**
     * Sets the x coordinate of the world position of the chunk
     * @return The x coordinate of the world position of the chunk
     */
    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    /**
     * Gets the y coordinate of the world position of the chunk
     * @return The y coordinate of the world position of the chunk
     */
    public int getWorldY() {
        return worldY;
    }

    /**
     * Sets the y coordinate of the world position of the chunk
     * @return The y coordinate of the world position of the chunk
     */
    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    /**
     * Gets the z coordinate of the world position of the chunk
     * @return The z coordinate of the world position of the chunk
     */
    public int getWorldZ() {
        return worldZ;
    }

    /**
     * Sets the z coordinate of the world position of the chunk
     * @return The z coordinate of the world position of the chunk
     */
    public void setWorldZ(int worldZ) {
        this.worldZ = worldZ;
    }

    /**
     * Gets the world position of this chunk
     * @return The world position
     */
    public Vector3i getWorldPos(){
        return new Vector3i(worldX, worldY, worldZ);
    }

    /**
     * Gets the homogenous value for this chunk
     * @return The homogenous value
     */
    public short getHomogenousValue(){
        return this.homogenousValue;
    }

    /**
     * Sets the homogenous value
     * @param homogenousValue The homogenous value
     */
    public void setHomogenousValue(short homogenousValue){
        this.homogenousValue = homogenousValue;
    }

    /**
     * Sets the homogenous value
     * @param homogenousValue The homogenous value
     */
    public void setHomogenousValue(int homogenousValue){
        this.setHomogenousValue((short)homogenousValue);
    }

    @Override
    public Vector3i getDimensions() {
        return new Vector3i(
            BlockChunkData.CHUNK_DATA_WIDTH,
            BlockChunkData.CHUNK_DATA_WIDTH,
            BlockChunkData.CHUNK_DATA_WIDTH
        );
    }

}
