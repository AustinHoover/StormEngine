package electrosphere.util.math;

/**
 * Utilities for hashing things
 */
public class HashUtils {

    /**
     * Bits to shift the y value by
     */
    private static final int SHIFT_Y = 16;
    
    /**
     * Bits to shift the z value by
     */
    private static final int SHIFT_Z = 32;

    /**
     * Mask used when unhashing components
     */
    private static final long UNHASH_MASK = 0xFFFFL;

    /**
     * Unhashes the x component
     */
    public static final int UNHASH_COMPONENT_X = 0;

    /**
     * Unhashes the y component
     */
    public static final int UNHASH_COMPONENT_Y = 1;

    /**
     * Unhashes the z component
     */
    public static final int UNHASH_COMPONENT_Z = 2;
    
    /**
     * Hashes an integer vector. Must be within the range [0,65536]
     * @param x The x component
     * @param y The y component
     * @param z The z component
     * @return The hashed value
     */
    public static long hashIVec(int x, int y, int z){
        if (x < 0 || x > 65536 || y < 0 || y > 65536 || z < 0 || z > 65536) {
            throw new IllegalArgumentException("Values must be in range [0, 65536] " + x + "," + y + "," + z);
        }
        return ((long) x) | ((long) y << SHIFT_Y) | ((long) z << SHIFT_Z);
    }

    /**
     * Hashes a voxel position
     * @param chunkX The chunk x position
     * @param chunkY The chunk y position
     * @param chunkZ The chunk z position
     * @param voxelX THe voxel x position
     * @param voxelY THe voxel y position
     * @param voxelZ THe voxel z position
     * @return The resultant hashed long
     */
    public static long hashVoxel(int chunkX, int chunkY, int chunkZ, int voxelX, int voxelY, int voxelZ){
        return (
            ((long)chunkX & 0xFFl) |
            ((((long) chunkY) << 8 ) & 0xFF00l) |
            ((((long) chunkZ) << 16) & 0xFF0000l) |
            ((((long) voxelX) << 24) & 0xFF000000l) |
            ((((long) voxelY) << 32) & 0xFF00000000l) |
            ((((long) voxelZ) << 40) & 0xFF0000000000l)
        );
    }

    /**
     * Unhashes an ivec-hashed value
     * @param hash The hash
     * @param component The component (x, y, or z) to pull from the hash (x=0, y=1, z=2)
     * @return The value
     */
    public static int unhashIVec(long hash, int component){
        switch(component){
            case 0: {
                return (int)(hash & UNHASH_MASK);
            }
            case 1: {
                return (int)(hash >> SHIFT_Y & UNHASH_MASK);
            }
            case 2: {
                return (int)(hash >> SHIFT_Z & UNHASH_MASK);
            }
            default: {
                throw new Error("Provided undefined component! " + component);
            }
        }
    }

}
