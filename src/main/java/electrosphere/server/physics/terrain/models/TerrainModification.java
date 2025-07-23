package electrosphere.server.physics.terrain.models;

import org.joml.Vector3i;

/**
 * A modification made to a terrain chunk
 */
public class TerrainModification {

    /**
     * The world position of the modification
     */
    Vector3i worldPos;

    /**
     * The voxel position within the chunk that was modified
     */
    Vector3i voxelPos;

    /**
     * The new weight
     */
    float weight;

    /**
     * The new type of voxel
     */
    int value;

    /**
     * Constructor
     * @param worldPos The world position of the modification
     * @param voxelPos The voxel position within the chunk that was modified
     * @param weight The new weight
     * @param value The new type of voxel
     */
    public TerrainModification(Vector3i worldPos, Vector3i voxelPos, float weight, int value) {
        this.worldPos = worldPos;
        this.voxelPos = voxelPos;
        this.weight = weight;
        this.value = value;
    }

    /**
     * Gets the world position of the modification
     * @return The world position
     */
    public Vector3i getWorldPos() {
        return worldPos;
    }

    /**
     * Gets the new relative voxel position of the modification
     * @return The relative voxel position
     */
    public Vector3i getVoxelPos() {
        return voxelPos;
    }

    /**
     * Gets the new weight
     * @return The new weight
     */
    public float getWeight(){
        return weight;
    }

    /**
     * Gets the new type of voxel
     * @return The new type of voxel
     */
    public int getValue() {
        return value;
    }
    
    
}
