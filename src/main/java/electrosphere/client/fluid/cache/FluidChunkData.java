package electrosphere.client.fluid.cache;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.joml.Vector3i;
import org.lwjgl.BufferUtils;

import electrosphere.server.physics.fluid.manager.ServerFluidChunk;

/**
 * A container of data about a chunk of fluid
 */
public class FluidChunkData {

    //The size of a chunk in virtual data
    public static final int CHUNK_SIZE = ServerFluidChunk.BUFFER_DIM;
    //The size of the data passed into marching cubes/transvoxel algorithm to get a fully connected and seamless chunk
    public static final int CHUNK_DATA_GENERATOR_SIZE = ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE;

    /**
     * The float view of the center weight buffer
     */
    FloatBuffer weights = null;

    /**
     * The float view of the center velocity x buffer
     */
    FloatBuffer velocityX = null;

    /**
     * The float view of the center velocity y buffer
     */
    FloatBuffer velocityY = null;

    /**
     * The float view of the center velocity z buffer
     */
    FloatBuffer velocityZ = null;

    /**
     * The array of all adjacent weight buffers for the fluid sim
     */
    public ByteBuffer bWeights = null;

    /**
     * The array of all adjacent velocity x buffers for the fluid sim
     */
    public ByteBuffer bVelocityX = null;

    /**
     * The array of all adjacent velocity y buffers for the fluid sim
     */
    public ByteBuffer bVelocityY = null;

    /**
     * The array of all adjacent velocity z buffers for the fluid sim
     */
    public ByteBuffer bVelocityZ = null;

    /**
     * Tracks whether this is homogenous or not
     */
    boolean homogenous = false;

    /**
     * Allocates the buffers for this chunk
     */
    private native void allocate();

    /**
     * Frees all native memory
     */
    private native void free();


    /**
     * Constructor
     */
    public FluidChunkData(){
    }

    /**
     * Gets the inddex into the buffer
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return The index
     */
    public int IX(int x, int y, int z){
        return x + y * ServerFluidChunk.BUFFER_DIM  + z * ServerFluidChunk.BUFFER_DIM * ServerFluidChunk.BUFFER_DIM;
    }

    /**
     * Gets the weight of a voxel at a poisiton
     * @param localPosition The local position
     * @return The weight of the specified voxel
     */
    public float getWeight(Vector3i localPosition){
        return weights.get(this.IX(localPosition.x,localPosition.y,localPosition.z));
    }

    /**
     * Gets the weight of a voxel at a poisiton
     * @return The weight of the specified voxel
     */
    public float getWeight(int x, int y, int z){
        return weights.get(this.IX(x,y,z));
    }

    /**
     * Sets the weight at a given position
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param weight The weight
     */
    public void setWeight(int x, int y, int z, float weight){
        weights.put(this.IX(x,y,z),weight);
    }

    /**
     * Gets the x velocity of a voxel at a poisiton
     * @return The x velocity of the specified voxel
     */
    public float getVelocityX(int x, int y, int z){
        return velocityX.get(this.IX(x,y,z));
    }

    /**
     * Sets the x velocity at a given position
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param vel The x velocity
     */
    public void setVelocityX(int x, int y, int z, float vel){
        velocityX.put(this.IX(x,y,z),vel);
    }

    /**
     * Gets the y velocity of a voxel at a poisiton
     * @return The y velocity of the specified voxel
     */
    public float getVelocityY(int x, int y, int z){
        return velocityY.get(this.IX(x,y,z));
    }

    /**
     * Sets the y velocity at a given position
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param vel The y velocity
     */
    public void setVelocityY(int x, int y, int z, float vel){
        velocityY.put(this.IX(x,y,z),vel);
    }

    /**
     * Gets the z velocity of a voxel at a poisiton
     * @return The z velocity of the specified voxel
     */
    public float getVelocityZ(int x, int y, int z){
        return velocityZ.get(this.IX(x,y,z));
    }

    /**
     * Sets the z velocity at a given position
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param vel The z velocity
     */
    public void setVelocityZ(int x, int y, int z, float vel){
        velocityZ.put(this.IX(x,y,z),vel);
    }

    /**
     * Frees the buffers contained within this chunk
     */
    public void freeBuffers(){
        // this.free();
    }

    /**
     * Allocates the buffers for this data
     */
    public void allocateBuffs(){
        // this.allocate();
        this.bWeights = BufferUtils.createByteBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 4);
        this.bVelocityX = BufferUtils.createByteBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 4);
        this.bVelocityY = BufferUtils.createByteBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 4);
        this.bVelocityZ = BufferUtils.createByteBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 4);

        //reorder
        this.bWeights.order(ByteOrder.LITTLE_ENDIAN);
        this.bVelocityX.order(ByteOrder.LITTLE_ENDIAN);
        this.bVelocityY.order(ByteOrder.LITTLE_ENDIAN);
        this.bVelocityZ.order(ByteOrder.LITTLE_ENDIAN);

        //get float view
        this.weights = this.bWeights.asFloatBuffer();
        this.velocityX = this.bVelocityX.asFloatBuffer();
        this.velocityY = this.bVelocityY.asFloatBuffer();
        this.velocityZ = this.bVelocityZ.asFloatBuffer();
    }

    /**
     * Gets whether this chunk is homogenous or not
     * @return true if it is homogenous, false otherwise
     */
    public boolean isHomogenous() {
        return homogenous;
    }

    /**
     * Sets whether this chunk is homogenous or not
     * @param homogenous true if it is homogenous, false otherwise
     */
    public void setHomogenous(boolean homogenous) {
        this.homogenous = homogenous;
    }

    

}
