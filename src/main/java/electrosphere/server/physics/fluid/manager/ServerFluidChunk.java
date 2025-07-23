package electrosphere.server.physics.fluid.manager;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.joml.Vector3i;


/**
 * Is a single chunk of terrain on the server
 */
public class ServerFluidChunk {


    /**
     * Number of adjacent arrays
     */
    static final int ARRAY_CT = 27;

    /**
     * Index of the center buffer
     */
    static final int CENTER_BUFF = 13;

    /**
     * Size of the true data. This is the data that is for this world position in particular
     */
    public static final int TRUE_DATA_DIM = 16;

    /**
     * Size of the true data generator for meshing
     */
    public static final int TRUE_DATA_GENERATOR_SIZE = TRUE_DATA_DIM + 1;

    /**
     * Number of positions to offset into the buffer before you will access the true data
     */
    public static final int TRUE_DATA_OFFSET = 1;

    /**
     * Dimension of a fluid buffer. This includes positions that just store neighbor values
     */
    public static final int BUFFER_DIM = TRUE_DATA_DIM + 2;

    /**
     * Size of a fluid buffer
     */
    public static final int BUFFER_SIZE = ServerFluidChunk.BUFFER_DIM * ServerFluidChunk.BUFFER_DIM * ServerFluidChunk.BUFFER_DIM;

    /**
     * Fluid data dto value for homogenous chunk
     */
    public static final float IS_HOMOGENOUS = 1;

    /**
     * Fluid data dto value for non-homogenous chunk
     */
    public static final float IS_NOT_HOMOGENOUS = 0;

    /**
     * Size of the homogenous value at the front of the fluid chunk dto
     */
    public static final int HOMOGENOUS_BUFFER_SIZE = 1 * 4;

    /**
     * The world x coordinate of this chunk
     */
    int worldX;

    /**
     * The world y coordinate of this chunk
     */
    int worldY;

    /**
     * The world z coordinate of this chunk
     */
    int worldZ;


    /**
     * The float view of the center weight buffer
     */
    FloatBuffer weights;

    /**
     * The float view of the center weight delta buffer
     */
    FloatBuffer weightsAdd;

    /**
     * The float view of the center velocity x buffer
     */
    FloatBuffer velocityX;

    /**
     * The float view of the center velocity y buffer
     */
    FloatBuffer velocityY;

    /**
     * The float view of the center velocity z buffer
     */
    FloatBuffer velocityZ;

    /**
     * The float view of the center bounds buffer
     */
    FloatBuffer bounds;

    /**
     * The pressure cache
     */
    FloatBuffer pressureCache;

    /**
     * The divergence cache
     */
    FloatBuffer divergenceCache;

    /**
     * The array of all adjacent weight buffers for the fluid sim
     */
    public ByteBuffer[] bWeights = new ByteBuffer[ARRAY_CT];

    /**
     * The array of all adjacent velocity x buffers for the fluid sim
     */
    public ByteBuffer[] bVelocityX = new ByteBuffer[ARRAY_CT];

    /**
     * The array of all adjacent velocity y buffers for the fluid sim
     */
    public ByteBuffer[] bVelocityY = new ByteBuffer[ARRAY_CT];

    /**
     * The array of all adjacent velocity z buffers for the fluid sim
     */
    public ByteBuffer[] bVelocityZ = new ByteBuffer[ARRAY_CT];

    /**
     * The array of all adjacent weight buffers for the fluid sim
     */
    public ByteBuffer[] b0Weights = new ByteBuffer[ARRAY_CT];

    /**
     * The array of all adjacent velocity x buffers for the fluid sim
     */
    public ByteBuffer[] b0VelocityX = new ByteBuffer[ARRAY_CT];

    /**
     * The array of all adjacent velocity y buffers for the fluid sim
     */
    public ByteBuffer[] b0VelocityY = new ByteBuffer[ARRAY_CT];

    /**
     * The array of all adjacent velocity z buffers for the fluid sim
     */
    public ByteBuffer[] b0VelocityZ = new ByteBuffer[ARRAY_CT];

    /**
     * The array storing bounds data
     */
    public ByteBuffer[] bBounds = new ByteBuffer[ARRAY_CT];

    /**
     * The array storing cached divergence data
     */
    public ByteBuffer[] bDivergenceCache = new ByteBuffer[ARRAY_CT];

    /**
     * The array storing cached pressure data
     */
    public ByteBuffer[] bPressureCache = new ByteBuffer[ARRAY_CT];

    /**
     * The array of pointers to neighboring chunks
     */
    public ServerFluidChunk[] neighbors = new ServerFluidChunk[ARRAY_CT];

    /**
     * The chunk mask -- Stores which adjacent chunks are populated and which aren't
     */
    public int chunkMask;

    /**
     * Tracks whether this chunk was updated or not
     */
    public boolean updated = false;

    /**
     * Tracks whether this chunk is asleep or not
     */
    public boolean asleep = false;

    /**
     * Tracks whether this chunk is homogenous or not
     */
    public boolean isHomogenous = true;

    /**
     * The total density of the chunk
     */
    public float totalDensity = 0;

    /**
     * Total pressure of this chunk
     */
    public float totalPressure = 0;

    /**
     * Total velocity magnitude of this chunk
     */
    public float totalVelocityMag = 0;

    /**
     * The normalization ratio used to smooth fluid simulation steps
     */
    public static float normalizationRatio = 0;

    /**
     * The amount of mass in the simulation
     */
    public static float massCount = 0;

    /**
     * The amount of pressure outgoing to other chunks
     */
    public float[] pressureOutgoing = new float[ARRAY_CT];

    /**
     * The amount of pressure incoming from other chunks
     */
    public float[] pressureIncoming = new float[ARRAY_CT];

    /**
     * The amount of density outgoing to other chunks
     */
    public float[] densityOutgoing = new float[ARRAY_CT];

    /**
     * The amount of density incoming from other chunks
     */
    public float[] densityIncoming = new float[ARRAY_CT];

    /**
     * Allocates the central arrays for this chunk
     */
    private native void allocate();

    /**
     * Frees all native memory
     */
    private native void free();

    /**
     * Constructor
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     */
    public ServerFluidChunk(int worldX, int worldY, int worldZ) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;

        //allocate
        this.allocate();

        //order
        this.bWeights[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.bVelocityX[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.bVelocityY[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.bVelocityZ[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.b0Weights[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.b0VelocityX[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.b0VelocityY[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.b0VelocityZ[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.bBounds[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.bDivergenceCache[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);
        this.bPressureCache[CENTER_BUFF].order(ByteOrder.LITTLE_ENDIAN);

        //get float view
        this.weights = this.bWeights[CENTER_BUFF].asFloatBuffer();
        this.weightsAdd = this.b0Weights[CENTER_BUFF].asFloatBuffer();
        this.velocityX = this.bVelocityX[CENTER_BUFF].asFloatBuffer();
        this.velocityY = this.bVelocityY[CENTER_BUFF].asFloatBuffer();
        this.velocityZ = this.bVelocityZ[CENTER_BUFF].asFloatBuffer();
        this.bounds = this.bBounds[CENTER_BUFF].asFloatBuffer();
        this.divergenceCache = this.bDivergenceCache[CENTER_BUFF].asFloatBuffer();
        this.pressureCache = this.bPressureCache[CENTER_BUFF].asFloatBuffer();
    }

    /**
     * Gets the world x coordinate
     * @return The world x coordinate
     */
    public int getWorldX() {
        return worldX;
    }

    /**
     * Gets the world y coordinate
     * @return The world y coordinate
     */
    public int getWorldY() {
        return worldY;
    }

    /**
     * Gets the world z coordinate
     * @return The world z coordinate
     */
    public int getWorldZ() {
        return worldZ;
    }

    /**
     * Gets the world position of this terrain chunk as a joml Vector
     * @return The vector
     */
    public Vector3i getWorldPosition(){
        return new Vector3i(worldX,worldY,worldZ);
    }

    /**
     * Gets the weights buffer
     * @return The weight buffer
     */
    public FloatBuffer getWeights() {
        return weights;
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
        return weights.get(this.IX(x,y,z));
    }

    /**
     * Sets a weight
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param weight The weight
     */
    public void setWeight(int x, int y, int z, float weight){
        weights.put(this.IX(x,y,z),weight);
    }

    /**
     * Gets the weight delta of a voxel at a poisiton
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The weight of the specified voxel
     */
    public float getWeightDelta(int x, int y, int z){
        return weightsAdd.get(this.IX(x,y,z));
    }

    /**
     * Sets a weight delta
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param weight The weight
     */
    public void setWeightDelta(int x, int y, int z, float weight){
        weightsAdd.put(this.IX(x,y,z),weight);
    }

    /**
     * Gets the velocity x buffer
     * @return The velocity x buffer
     */
    public FloatBuffer getVelocityX() {
        return velocityX;
    }

    /**
     * Gets the x velocity at the point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The x velocity at the point
     */
    public float getVelocityX(int x, int y, int z){
        return velocityX.get(this.IX(x, y, z));
    }


    /**
     * Sets the velocity x buffer
     * @param velocityX The velocity x buffer
     */
    public void setVelocityX(FloatBuffer velocityX) {
        this.velocityX = velocityX;
    }

    /**
     * Sets the x velocity at the point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param velocity The x velocity
     */
    public void setVelocityX(int x, int y, int z, float velocity){
        this.velocityX.put(this.IX(x,y,z),velocity);
    }

    /**
     * Gets the velocity y buffer
     * @return The velocity y buffer
     */
    public FloatBuffer getVelocityY() {
        return velocityY;
    }
    
    /**
     * Gets the y velocity at the point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The y velocity at the point
     */
    public float getVelocityY(int x, int y, int z){
        return velocityY.get(this.IX(x, y, z));
    }

    /**
     * Sets the velocity y buffer
     * @param velocityY The velocity y buffer
     */
    public void setVelocityY(FloatBuffer velocityY) {
        this.velocityY = velocityY;
    }

    /**
     * Sets the y velocity at the point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param velocity The y velocity
     */
    public void setVelocityY(int x, int y, int z, float velocity){
        this.velocityY.put(this.IX(x,y,z),velocity);
    }


    /**
     * Gets the velocity z buffer
     * @return The velocity z buffer
     */
    public FloatBuffer getVelocityZ() {
        return velocityZ;
    }

    /**
     * Gets the z velocity at the point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The z velocity at the point
     */
    public float getVelocityZ(int x, int y, int z){
        return velocityZ.get(this.IX(x, y, z));
    }

    /**
     * Sets the velocity z buffer
     * @param velocityZ The velocity z buffer
     */
    public void setVelocityZ(FloatBuffer velocityZ) {
        this.velocityZ = velocityZ;
    }

    /**
     * Sets the z velocity at the point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param velocity The z velocity
     */
    public void setVelocityZ(int x, int y, int z, float velocity){
        this.velocityZ.put(this.IX(x,y,z),velocity);
    }

    /**
     * Gets the velocity at a given point as a vector3f
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The velocity
     */
    public Vector3f getVelocity(int x, int y, int z){
        int index = this.IX(x,y,z);
        return new Vector3f(
            velocityX.get(index),
            velocityY.get(index),
            velocityZ.get(index)
        );
    }

    //set a velocity at a given x, y, and z given three ints
    /**
     * Sets the full velocity at a given point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param velX The x component of the velocity
     * @param velY The y component of the velocity
     * @param velZ The z component of the velocity
     */
    public void setVelocity(int x, int y, int z, float velX, float velY, float velZ){
        int index = this.IX(x,y,z);
        velocityX.put(index, velX);
        velocityY.put(index, velY);
        velocityZ.put(index, velZ);
    }

    /**
     * Gets the bounds value at a given position
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The bounds value
     */
    public float getBound(int x, int y, int z){
        return bounds.get(this.IX(x,y,z));
    }

    /**
     * Sets the bounds value at a given position
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param bound The bounds value
     */
    public void setBound(int x, int y, int z, float bound){
        this.bounds.put(this.IX(x,y,z),bound);
    }

    /**
     * Sets a pressure
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param pressure The pressure
     */
    public void setPressure(int x, int y, int z, float pressure){
        pressureCache.put(this.IX(x,y,z),pressure);
    }

    /**
     * Gets the pressure at a point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The pressure
     */
    public float getPressure(int x, int y, int z){
        return pressureCache.get(this.IX(x,y,z));
    }

    /**
     * Gets the divergence at a point
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The divergence
     */
    public float getDivergence(int x, int y, int z){
        return divergenceCache.get(this.IX(x,y,z));
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
     * Gets the chunk mask for this chunk
     * @return The chunk mask
     */
    public int getChunkMask(){
        return this.chunkMask;
    }

    /**
     * Gets whether this chunk updated in its most recent frame or not
     * @return true if it updated, false otherwise
     */
    public boolean getUpdated(){
        return updated;
    }

    /**
     * Gets the neighbor index given an offset in each dimension
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The neighbor index
     */
    public static final int getNeighborIndex(int x, int y, int z){
        return x + y * 3 + z * 3 * 3;
    }

    /**
     * Sets the neighbor of this chunk
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param neighbor The neighbor ServerFluidChunk
     */
    public void setNeighbor(int x, int y, int z, ServerFluidChunk neighbor){
        int index = ServerFluidChunk.getNeighborIndex(x, y, z);
        if(neighbor == null){
            bWeights[index ]= null;
            b0Weights[index] = null;
            bVelocityX[index] = null;
            bVelocityY[index] = null;
            bVelocityZ[index] = null;
            b0VelocityX[index] = null;
            b0VelocityY[index] = null;
            b0VelocityZ[index] = null;
            bBounds[index] = null;
            bDivergenceCache[index] = null;
            bPressureCache[index] = null;
            neighbors[index] = null;
        } else {
            bWeights[index] = neighbor.bWeights[CENTER_BUFF];
            b0Weights[index] = neighbor.b0Weights[CENTER_BUFF];
            bVelocityX[index] = neighbor.bVelocityX[CENTER_BUFF];
            bVelocityY[index] = neighbor.bVelocityY[CENTER_BUFF];
            bVelocityZ[index] = neighbor.bVelocityZ[CENTER_BUFF];
            b0VelocityX[index] = neighbor.b0VelocityX[CENTER_BUFF];
            b0VelocityY[index] = neighbor.b0VelocityY[CENTER_BUFF];
            b0VelocityZ[index] = neighbor.b0VelocityZ[CENTER_BUFF];
            b0VelocityZ[index] = neighbor.b0VelocityZ[CENTER_BUFF];
            bBounds[index] = neighbor.bBounds[CENTER_BUFF];
            bDivergenceCache[index] = neighbor.bDivergenceCache[CENTER_BUFF];
            bPressureCache[index] = neighbor.bPressureCache[CENTER_BUFF];
            neighbors[index] = neighbor;
        }
    }

    /**
     * Checks if this chunk is allocated or not
     * @return true if it is allocated, false otherwise
     */
    public boolean isAllocated(){
        return this.bWeights[CENTER_BUFF] != null;
    }
    
    /**
     * Gets whether this chunk is asleep or not
     * @return true if it is asleep, false otherwise
     */
    public boolean isAsleep() {
        return asleep;
    }

    /**
     * Sets whether this chunk is asleep or not
     * @return true if it is asleep, false otherwise
     */
    public void setAsleep(boolean asleep) {
        this.asleep = asleep;
    }

    /**
     * Frees the buffers contained within this chunk
     */
    public void freeBuffers(){
        this.free();
        for(int i = 0; i < 27; i++){
            bWeights[i] = null;
            b0Weights[i] = null;
            bVelocityX[i] = null;
            bVelocityY[i] = null;
            bVelocityZ[i] = null;
            b0VelocityX[i] = null;
            b0VelocityY[i] = null;
            b0VelocityZ[i] = null;
            bBounds[i] = null;
            bDivergenceCache[i] = null;
            bPressureCache[i] = null;
        }
    }

    /**
     * Gets the total density of this chunk
     * @return The total density
     */
    public float getTotalDensity(){
        return this.totalDensity;
    }

    /**
     * Gets whether this chunk is homogenous or not
     * @return true if it is homogenous, false otherwise
     */
    public boolean isHomogenous(){
        return this.isHomogenous;
    }

    /**
     * Gets the normalization ratio
     * @return The normalization ratio
     */
    public static float getNormalizationRatio() {
        return normalizationRatio;
    }

    /**
     * Gets the amount of mass in the simulation
     * @return The amount of mass
     */
    public static double getMassCount() {
        return massCount;
    }

    /**
     * Gets the total pressure of this chunk
     * @return The total pressure of this chunk
     */
    public float getTotalPressure(){
        return totalPressure;
    }

    /**
     * Gets the total velocity magnitude of this chunk
     * @return The total velocity magnitude of this chunk
     */
    public float getTotalVelocityMag(){
        return totalVelocityMag;
    }

    

}
