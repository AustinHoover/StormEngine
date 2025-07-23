package electrosphere.client.terrain.cache;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector3i;

import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.math.HashUtils;

/**
 * A container of data about a chunk of terrain
 */
public class ChunkData {

    /**
     * No stride
     */
    public static final int NO_STRIDE = 0;

    /**
     * The id for a non-homogenous data
     */
    public static final int NOT_HOMOGENOUS = -1;

    /**
     * The weight of a cell in a homogenous chunk
     */
    public static final float HOMOGENOUS_WEIGHT = 1.0f;

    /**
     * The size of the chunk data stored on the client
     */
    public static final int CHUNK_DATA_SIZE = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE;
    
    /**
     * What type of terrain is in this voxel, eg stone vs dirt vs grass, etc
     */
    int[][][] voxelType;

    /**
     * How much of that terrain type is in this voxel
     */
    float[][][] voxelWeight;

    /**
     * the list of positions modified since the last call to resetModifiedPositions
     * Used in DrawCell to keep track of which positions to invalidate
     */
    Set<Long> modifiedSinceLastGeneration = new HashSet<Long>();

    /**
     * The word x coordinate
     */
    int worldX;

    /**
     * The word y coordinate
     */
    int worldY;

    /**
     * The word z coordinate
     */
    int worldZ;

    /**
     * The stride of the data
     */
    int stride;

    /**
     * Tracks whether this chunk is homogenous or not
     */
    int homogenousValue = NOT_HOMOGENOUS;

    /**
     * Creates a chunk data
     * @param worldX The word x coordinate
     * @param worldY The word y coordinate
     * @param worldZ The word z coordinate
     * @param stride The stride of the data
     * @param homogenous Tracks whether this chunk is homogenous or not
     */
    public ChunkData(int worldX, int worldY, int worldZ, int stride, int homogenousValue){
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.stride = stride;
        this.homogenousValue = homogenousValue;
    }

    /**
     * Creates a chunk data object
     */
    public ChunkData(){
        this.voxelWeight = new float[ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE];
        this.voxelType = new int[ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE][ChunkData.CHUNK_DATA_SIZE];
    }


    /**
     * Gets the voxel type array in this container
     * @return The voxel type array
     */
    public int[][][] getVoxelType(){
        return voxelType;
    }

    /**
     * Sets the voxel type array in this container
     * @param voxelType The voxel type array
     */
    public void setVoxelType(int[][][] voxelType){
        //mark changed cells
        if(this.voxelType != null){
            for(int x = 0; x < CHUNK_DATA_SIZE; x++){
                for(int y = 0; y < CHUNK_DATA_SIZE; y++){
                    for(int z = 0; z < CHUNK_DATA_SIZE; z++){
                        if(voxelType[x][y][z] != this.voxelType[x][y][z]){
                            Long key = getVoxelPositionKey(new Vector3i(x,y,z));
                            if(!modifiedSinceLastGeneration.contains(key)){
                                modifiedSinceLastGeneration.add(key);
                            }
                        }
                    }
                }
            }
        }
        //update data
        this.voxelType = voxelType;
    }

    /**
     * Gets the voxel weight array in this container
     * @return The voxel weight array
     */
    public float[][][] getVoxelWeight(){
        return voxelWeight;
    }

    /**
     * Sets the voxel weight array in this container
     * @param voxelWeight The voxel weight array
     */
    public void setVoxelWeight(float[][][] voxelWeight){
        //mark changed cells
        if(this.voxelWeight != null){
            for(int x = 0; x < CHUNK_DATA_SIZE; x++){
                for(int y = 0; y < CHUNK_DATA_SIZE; y++){
                    for(int z = 0; z < CHUNK_DATA_SIZE; z++){
                        if(voxelWeight[x][y][z] != this.voxelWeight[x][y][z]){
                            Long key = getVoxelPositionKey(new Vector3i(x,y,z));
                            if(!modifiedSinceLastGeneration.contains(key)){
                                modifiedSinceLastGeneration.add(key);
                            }
                        }
                    }
                }
            }
        }
        //update data
        this.voxelWeight = voxelWeight;
    }

    /**
     * Updates the value of a single voxel in the chunk
     * @param localX The local position X
     * @param localY The local position Y
     * @param localZ The local position Z
     * @param weight The weight to set it to
     * @param type The type to set the voxel to
     */
    public void updatePosition(int localX, int localY, int localZ, float weight, int type){
        voxelWeight[localX][localY][localZ] = weight;
        voxelType[localX][localY][localZ] = type;
        //store as modified in cache
        Long key = this.getVoxelPositionKey(new Vector3i(localX,localY,localZ));
        if(!modifiedSinceLastGeneration.contains(key)){
            modifiedSinceLastGeneration.add(key);
        }
    }

    /**
     * Gets the weight of a voxel at a poisiton
     * @param localPosition The local position
     * @return The weight of the specified voxel
     */
    public float getWeight(Vector3i localPosition){
        return getWeight(localPosition.x,localPosition.y,localPosition.z);
    }

    /**
     * Gets the weight of a voxel at a poisiton
     * @param localX The x coordinate
     * @param localY The y coordinate
     * @param localZ The z coordinate
     * @return The weight of the specified voxel
     */
    public float getWeight(int localX, int localY, int localZ){
        return voxelWeight[localX][localY][localZ];
    }

    /**
     * Sets the weight of a voxel at a poisiton
     * @param localX The x coordinate
     * @param localY The y coordinate
     * @param localZ The z coordinate
     * @param weight THe value
     */
    public void setWeight(int localX, int localY, int localZ, float weight){
        voxelWeight[localX][localY][localZ] = weight;
    }

    /**
     * Gets the type of a voxel at a position
     * @param localPosition The local position
     * @return The type of the specified voxel
     */
    public int getType(Vector3i localPosition){
        return getType(localPosition.x,localPosition.y,localPosition.z);
    }

    /**
     * Sets the type of a voxel at a position
     * @param localX The x coordinate
     * @param localY The y coordinate
     * @param localZ The z coordinate
     * @param type The type value
     */
    public void setType(int localX, int localY, int localZ, int type){
        voxelType[localX][localY][localZ] = type;
    }

    /**
     * Gets the type of a voxel at a position
     * @param localX The x coordinate
     * @param localY The y coordinate
     * @param localZ The z coordinate
     * @return The type of the specified voxel
     */
    public int getType(int localX, int localY, int localZ){
        if(this.homogenousValue != ChunkData.NOT_HOMOGENOUS){
            return this.homogenousValue;
        }
        return voxelType[localX][localY][localZ];
    }

    /**
     * Resets the cache of modified positions
     */
    public void resetModifiedPositions(){
        this.modifiedSinceLastGeneration.clear();
    }

    /**
     * Gets the set of all modified positions since the last call to resetModifiedPositions
     * @return The set of all modified positions
     */
    public Set<Vector3i> getModifiedPositions(){
        Set<Vector3i> rVal = new HashSet<Vector3i>();
        for(Long key : modifiedSinceLastGeneration){
            int x = HashUtils.unhashIVec(key, HashUtils.UNHASH_COMPONENT_X);
            int y = HashUtils.unhashIVec(key, HashUtils.UNHASH_COMPONENT_Y);
            int z = HashUtils.unhashIVec(key, HashUtils.UNHASH_COMPONENT_Z);
            rVal.add(new Vector3i(x,y,z));
        }
        return rVal;
    }

    /**
     * Gets a key for the modifiedSinceLastGeneration set based on a voxel position
     * @param position The voxel position
     * @return The key
     */
    private Long getVoxelPositionKey(Vector3i position){
        return HashUtils.hashIVec(worldX, worldY, worldZ);
    }

    /**
     * Gets the world position of the chunk data
     * @return The world position
     */
    public Vector3i getWorldPos(){
        return new Vector3i(worldX,worldY,worldZ);
    }

    /**
     * Gets the stride of the data
     * @return The stride of the data
     */
    public int getStride(){
        return stride;
    }

    /**
     * The homogenous value of the chunk data
     * @return if the data is homogenous, will return the id of the voxel that comprises the whole data block. Otherwise will return ChunkData.NOT_HOMOGENOUS
     */
    public int getHomogenousValue(){
        return homogenousValue;
    }

    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    public void setWorldZ(int worldZ) {
        this.worldZ = worldZ;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public void setHomogenousValue(int homogenousValue) {
        this.homogenousValue = homogenousValue;
    }

    


}
