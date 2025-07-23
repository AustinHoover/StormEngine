package electrosphere.server.physics.terrain.manager;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3i;

import electrosphere.server.physics.terrain.models.TerrainModification;

/**
 * Is a single chunk of terrain on the server
 */
public class ServerTerrainChunk {

    /**
     * Size of a single voxel
     */
    public static final int VOXEL_SIZE = 1;

    /**
     * All chunks are 16x16x16
     */
    public static final int CHUNK_DIMENSION = 16 * VOXEL_SIZE;

    /**
     * The size of the data passed into marching cubes/transvoxel to generate a fully connected and seamless chunk
     */
    public static final int CHUNK_DATA_GENERATOR_SIZE = CHUNK_DIMENSION + 1;

    /**
     * The units that should be used when placing chunks in the scene
     */
    public static final int CHUNK_PLACEMENT_OFFSET = CHUNK_DATA_GENERATOR_SIZE - 1;

    /**
     * An empty terrain voxel
     */
    public static final int VOXEL_TYPE_AIR = 0;

    /**
     * Gets the x coordinate of the world position of the chunk
     */
    int worldX;

    /**
     * Gets the y coordinate of the world position of the chunk
     */
    int worldY;

    /**
     * Gets the z coordinate of the world position of the chunk
     */
    int worldZ;

    /**
     * The list of modifications applied to the chunk
     */
    List<TerrainModification> modifications = new LinkedList<TerrainModification>();

    /**
     * The weights of the chunk
     */
    float[][][] weights;

    /**
     * The values of the chunk
     */
    int[][][] values;

    /**
     * The homogenous value of this data, or ChunkData.NOT_HOMOGENOUS if it is not homogenous
     */
    int homogenousValue;

    /**
     * Constructor
     * @param worldX The world position x coordinate
     * @param worldY The world position y coordinate
     * @param worldZ The world position z coordinate
     * @param homogenousValue The homogenous value of the terrain chunk
     * @param weights The weights of the chunk
     * @param values The values of the chunk
     */
    public ServerTerrainChunk(int worldX, int worldY, int worldZ, int homogenousValue, float[][][] weights, int[][][] values) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.homogenousValue = homogenousValue;
        this.weights = weights;
        this.values = values;
    }

    /**
     * Constructor
     * @param worldX The world position x coordinate
     * @param worldY The world position y coordinate
     * @param worldZ The world position z coordinate
     */
    public ServerTerrainChunk(int worldX, int worldY, int worldZ){
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
    }

    /**
     * Gets the world position x coordinate
     * @return The x coordinate
     */
    public int getWorldX() {
        return worldX;
    }

    /**
     * Gets the world position y coordinate
     * @return The y coordinate
     */
    public int getWorldY() {
        return worldY;
    }

    /**
     * Gets the world position z coordinate
     * @return The z coordinate
     */
    public int getWorldZ() {
        return worldZ;
    }

    /**
     * Gets the world position of the chunk
     * @return The world position
     */
    public Vector3i getWorldPos(){
        return new Vector3i(worldX,worldY,worldZ);
    }

    /**
     * Gets the world position of this terrain chunk as a joml Vector
     * @return The vector
     */
    public Vector3i getWorldPosition(){
        return new Vector3i(worldX,worldY,worldZ);
    }

    /**
     * Gets the list of all modifications applied to the chunk
     * @return THe list of all modifications
     */
    public List<TerrainModification> getModifications() {
        return modifications;
    }

    /**
     * Gets the weights of the chunk
     * @return The weights of the chunk
     */
    public float[][][] getWeights() {
        return weights;
    }

    /**
     * Gets the values of the chunk
     * @return The values of the chunk
     */
    public int[][][] getValues() {
        return values;
    }

    /**
     * Adds a modification to the chunk
     * @param modification The modification
     */
    public void addModification(TerrainModification modification){
        modifications.add(modification);
        values[modification.getVoxelPos().x][modification.getVoxelPos().y][modification.getVoxelPos().z] = modification.getValue();
        weights[modification.getVoxelPos().x][modification.getVoxelPos().y][modification.getVoxelPos().z] = modification.getWeight();
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
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The weight of the specified voxel
     */
    public float getWeight(int x, int y, int z){
        return weights[x][y][z];
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
     * Gets the type of a voxel at a position
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The type of the specified voxel
     */
    public int getType(int x, int y, int z){
        return values[x][y][z];
    }
    
    /**
     * Gets the homogenous value of the chunk
     * @return The homogenous value of this data, or ChunkData.NOT_HOMOGENOUS if it is not homogenous
     */
    public int getHomogenousValue(){
        return homogenousValue;
    }

    /**
     * Sets the weights of the chunk
     * @param weights The weights
     */
    public void setWeights(float[][][] weights) {
        this.weights = weights;
    }

    /**
     * Sets the values of the chunk
     * @param values The values
     */
    public void setValues(int[][][] values) {
        this.values = values;
    }

    /**
     * Sets the homogenous value
     * @param homogenousValue The homogenous value
     */
    public void setHomogenousValue(int homogenousValue) {
        this.homogenousValue = homogenousValue;
    }

    
    
}
