package electrosphere.server.datacell;

import electrosphere.client.block.BlockChunkData;
import electrosphere.server.physics.block.manager.ServerBlockManager;
import electrosphere.server.physics.fluid.generation.DefaultFluidGenerator;
import electrosphere.server.physics.fluid.manager.ServerFluidManager;
import electrosphere.server.physics.terrain.generation.DefaultChunkGenerator;
import electrosphere.server.physics.terrain.generation.ProceduralChunkGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;
import electrosphere.server.physics.terrain.models.TerrainModel;
import electrosphere.util.FileUtils;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Server data about the world
 */
public class ServerWorldData {
    
    /**
     * The size of the procedural world
     */
    public static final int PROCEDURAL_WORLD_SIZE = TerrainModel.MAX_WORLD_SIZE_DISCRETE;

    public static enum WorldType {
        GAME_WORLD,
        ARENA_WORLD,
        LEVEL,
    }
    
    WorldType type;
    
    
    /*
    
                       world max
    +---------------------+
    |                     |
    |                     |
    |                     |
    |                     |
    |                     |
    +---------------------+
 world min
    
    
    basically we're saying what the maximum and minimum x and z something can occupy are
    
    FOR THE TIME BEING DOES NOT ACCOUNT FOR Y
    */
    Vector3f worldMinPoint;
    Vector3f worldMaxPoint;
    
    int worldSizeDiscrete;
    int worldSizeDiscreteVertical;
    int dynamicInterpolationRatio;
    float randomDampener;
    boolean isArena = false;

    
    /**
     * terrain data
     */
    private ServerTerrainManager serverTerrainManager;

    /**
     * fluid data
     */
    private ServerFluidManager serverFluidManager;

    /**
     * The block manager
     */
    private ServerBlockManager serverBlockManager;
    

    /**
     * Creates a server world data object based on a discrete world size
     * @param discreteWorldSize The discrete world size
     * @return The server world data object
     */
    public static ServerWorldData createGriddedRealmWorldData(int discreteWorldSize){
        ServerWorldData rVal = new ServerWorldData();
        rVal.type = WorldType.LEVEL;

        //min and max real points
        rVal.worldMinPoint = new Vector3f(0,0,0);
        int worldDim = discreteWorldSize * ServerTerrainChunk.CHUNK_DIMENSION;
        rVal.worldMaxPoint = new Vector3f(worldDim,worldDim, worldDim);

        //misc values
        rVal.dynamicInterpolationRatio = 1;
        rVal.worldSizeDiscrete = discreteWorldSize;
        rVal.worldSizeDiscreteVertical = discreteWorldSize;
        rVal.randomDampener = ServerTerrainManager.SERVER_TERRAIN_MANAGER_DAMPENER;

        return rVal;
    }

    /**
     * Creates a server world data object with a fixed size
     * @param minPoint The minimum point of the world
     * @param maxPoint The maximum point of the world
     * @return The server world data object
     */
    public static ServerWorldData createFixedWorldData(Vector3d minPoint, Vector3d maxPoint){
        ServerWorldData rVal = new ServerWorldData();
        rVal.type = WorldType.LEVEL;

        //min and max real points
        rVal.worldMinPoint = new Vector3f((float)minPoint.x,(float)minPoint.y,(float)minPoint.z);
        rVal.worldMaxPoint = new Vector3f((float)maxPoint.x,(float)maxPoint.y,(float)maxPoint.z);

        //misc values
        rVal.dynamicInterpolationRatio = 1;
        rVal.worldSizeDiscrete = 1;
        rVal.worldSizeDiscreteVertical = 1;
        rVal.randomDampener = ServerTerrainManager.SERVER_TERRAIN_MANAGER_DAMPENER;

        return rVal;
    }

    /**
     * Loads world data from a scene or a save
     * @param sceneOrSaveName The name of the scene or save
     * @param isScene true if loading from a scene, false if loading from a save
     * @return The server world data
     */
    public static ServerWorldData loadWorldData(String sceneOrSaveName, boolean isScene){
        //
        //Read world data if it exists
        //
        ServerWorldData serverWorldData = null;
        ServerTerrainManager serverTerrainManager = null;
        ServerFluidManager serverFluidManager = null;
        ServerBlockManager serverBlockManager = null;
        if(isScene){
            serverWorldData = FileUtils.loadObjectFromSavePath(sceneOrSaveName, "world.json", ServerWorldData.class);
            serverTerrainManager = new ServerTerrainManager(serverWorldData, 0, new DefaultChunkGenerator());
            serverTerrainManager.load(sceneOrSaveName);
            serverFluidManager = new ServerFluidManager(serverWorldData, serverTerrainManager, 0, new DefaultFluidGenerator());
            serverBlockManager = new ServerBlockManager(serverWorldData);
            serverBlockManager.load(sceneOrSaveName);
        } else {
            serverWorldData = FileUtils.loadObjectFromSavePath(sceneOrSaveName, "world.json", ServerWorldData.class);
            serverTerrainManager = new ServerTerrainManager(serverWorldData, 0, new ProceduralChunkGenerator(serverWorldData, false));
            serverTerrainManager.load(sceneOrSaveName);
            serverFluidManager = new ServerFluidManager(serverWorldData, serverTerrainManager, 0, new DefaultFluidGenerator());
            serverBlockManager = new ServerBlockManager(serverWorldData);
            serverBlockManager.load(sceneOrSaveName);
        }
        serverWorldData.setManagers(serverTerrainManager, serverFluidManager, serverBlockManager);
        return serverWorldData;
    }

    /**
     * Creates world data for testing generation
     * @return The server world data
     */
    public static ServerWorldData createGenerationTestWorldData(){
        //
        //Read world data if it exists
        //
        ServerWorldData serverWorldData = null;
        ServerTerrainManager serverTerrainManager = null;
        ServerFluidManager serverFluidManager = null;
        ServerBlockManager serverBlockManager = null;
        serverWorldData = ServerWorldData.createFixedWorldData(new Vector3d(0),new Vector3d(ProceduralChunkGenerator.GENERATOR_REALM_SIZE * ServerTerrainChunk.CHUNK_DIMENSION));
        serverWorldData.worldSizeDiscrete = ProceduralChunkGenerator.GENERATOR_REALM_SIZE;
        serverWorldData.worldSizeDiscreteVertical = ProceduralChunkGenerator.GENERATOR_REALM_SIZE;

        //test terrain gen
        {
            ProceduralChunkGenerator chunkGen = new ProceduralChunkGenerator(serverWorldData, ProceduralChunkGenerator.DEFAULT_USE_JAVASCRIPT);
            serverTerrainManager = new ServerTerrainManager(serverWorldData, 0, chunkGen);
            serverTerrainManager.genTestData(chunkGen);
        }
        serverFluidManager = new ServerFluidManager(serverWorldData, serverTerrainManager, 0, new DefaultFluidGenerator());
        serverBlockManager = new ServerBlockManager(serverWorldData);
        serverWorldData.setManagers(serverTerrainManager, serverFluidManager, serverBlockManager);
        return serverWorldData;
    }
    
    
    public Vector3f getWorldBoundMin(){
        return worldMinPoint;
    }
    
    public Vector3f getWorldBoundMax(){
        return worldMaxPoint;
    }

    /**
     * Gets the discrete size of the world (in chunks)
     * @return The discrete size of the world (in chunks)
     */
    public int getWorldSizeDiscrete() {
        return worldSizeDiscrete;
    }

    public int getDynamicInterpolationRatio() {
        return dynamicInterpolationRatio;
    }

    public static int convertRealToChunkSpace(double real){
        return (int)Math.floor(real / ServerTerrainChunk.CHUNK_DIMENSION);
    }

    public static float convertChunkToRealSpace(int chunk){
        return chunk * ServerTerrainChunk.CHUNK_DIMENSION;
    }

    /**
     * Converts a chunk space position to a real space position
     * @param chunk The chunk space position
     * @return The real space position
     */
    public static Vector3d convertChunkToRealSpace(Vector3i chunk){
        return new Vector3d(
            ServerWorldData.convertChunkToRealSpace(chunk.x),
            ServerWorldData.convertChunkToRealSpace(chunk.y),
            ServerWorldData.convertChunkToRealSpace(chunk.z)
        );
    }

    /**
     * Converts a chunk space position to a real space position
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return The real space position
     */
    public static Vector3d convertChunkToRealSpace(int x, int y, int z){
        return new Vector3d(
            ServerWorldData.convertChunkToRealSpace(x),
            ServerWorldData.convertChunkToRealSpace(y),
            ServerWorldData.convertChunkToRealSpace(z)
        );
    }

    /**
     * Converts a real position to a local block grid position
     * @param real The real position
     * @return The local block grid position
     */
    public static int convertRealToLocalBlockSpace(double real){
        return (int)Math.floor(real * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE % BlockChunkData.CHUNK_DATA_WIDTH);
    }

    /**
     * Converts a local block grid position to a real position
     * @param chunk The chunk pos
     * @param blockPos The block's local pos
     * @return The real position
     */
    public static double convertLocalBlockToRealSpace(int chunk, int blockPos){
        return ServerWorldData.convertChunkToRealSpace(chunk + blockPos / BlockChunkData.CHUNK_DATA_WIDTH) + (blockPos % BlockChunkData.CHUNK_DATA_WIDTH) * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
    }

    /**
     * Converts a local block grid position to a real position
     * @param chunk The chunk pos
     * @param blockPos The block's local pos
     * @return The real position
     */
    public static Vector3d convertLocalBlockToRealSpace(Vector3i chunk, Vector3i blockPos){
        return new Vector3d(
            ServerWorldData.convertLocalBlockToRealSpace(chunk.x, blockPos.x),
            ServerWorldData.convertLocalBlockToRealSpace(chunk.y, blockPos.y),
            ServerWorldData.convertLocalBlockToRealSpace(chunk.z, blockPos.z)
        );
    }

    /**
     * Converts a chunk space coordinate to a real space coordinate
     * @param voxelPos The voxel's position within the chunk
     * @param worldPos The world pos of the chunk
     * @return The real pos
     */
    public static double convertVoxelToRealSpace(int voxelPos, int worldPos){
        if(voxelPos > ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET){
            throw new Error("Voxel position outside of bounds, likely flipped args " + voxelPos + " " + worldPos);
        }
        return voxelPos + ServerWorldData.convertWorldToReal(worldPos);
    }

    /**
     * Converts a chunk space vector to a real space vector
     * @param voxelPos The voxel's position within the chunk
     * @param worldPos The world pos of the chunk
     * @return The real pos
     */
    public static Vector3d convertVoxelToRealSpace(Vector3i voxelPos, Vector3i worldPos){
        return new Vector3d(
            ServerWorldData.convertVoxelToRealSpace(voxelPos.x, worldPos.x),
            ServerWorldData.convertVoxelToRealSpace(voxelPos.y, worldPos.y),
            ServerWorldData.convertVoxelToRealSpace(voxelPos.z, worldPos.z)
        );
    }
    
    public double getRelativeLocation(double real, int world){
        return real - (world * dynamicInterpolationRatio);
    }

    public boolean isArena() {
        return isArena;
    }
    

    public int convertRealToWorld(double real){
        return convertRealToChunkSpace(real);
    }

    public static double convertWorldToReal(int world){
        return convertChunkToRealSpace(world);
    }

    public static Vector3i convertRealToChunkSpace(Vector3d position){
        return new Vector3i(
            ServerWorldData.convertRealToChunkSpace(position.x),
            ServerWorldData.convertRealToChunkSpace(position.y),
            ServerWorldData.convertRealToChunkSpace(position.z)
        );
    }

    
    public static void convertRealToChunkSpace(Vector3d position, Vector3i destVec){
        destVec.set(
            ServerWorldData.convertRealToChunkSpace(position.x),
            ServerWorldData.convertRealToChunkSpace(position.y),
            ServerWorldData.convertRealToChunkSpace(position.z)
        );
    }

    /**
     * Converts a world space vector to a real space vector
     * @param position The world space vector
     * @return The real space vector
     */
    public Vector3d convertWorldToRealSpace(Vector3i position){
        return new Vector3d(
            convertWorldToReal(position.x),
            convertWorldToReal(position.y),
            convertWorldToReal(position.z)
        );
    }

    /**
     * Converts a real-space position to a voxel-space position
     * @param position The real-space position
     * @return The voxel-space position
     */
    public static Vector3i convertRealToVoxelSpace(Vector3d position){
        return new Vector3i(
            ServerWorldData.convertRealToVoxelSpace(position.x),
            ServerWorldData.convertRealToVoxelSpace(position.y),
            ServerWorldData.convertRealToVoxelSpace(position.z)
        );
    }

    /**
     * Converts a real-space position to a voxel-space position
     * @param x The real-space position
     * @return The voxel-space position
     */
    public static int convertRealToVoxelSpace(double x){
        return (int)Math.floor(x - convertChunkToRealSpace(convertRealToChunkSpace(x)));
    }

    /**
     * Converts a real coordinate to a world space coordinate
     * @param position The real coordinate
     * @return The world space coordinate
     */
    public static Vector3i convertRealToWorldSpace(Vector3d position){
        return new Vector3i(
            ServerWorldData.convertRealToChunkSpace(position.x),
            ServerWorldData.convertRealToChunkSpace(position.y),
            ServerWorldData.convertRealToChunkSpace(position.z)
        );
    }

    /**
     * Converts a real coordinate to a local block grid space coordinate
     * @param position The real coordinate
     * @return The local block grid space coordinate
     */
    public static Vector3i convertRealToLocalBlockSpace(Vector3d position){
        return new Vector3i(
            ServerWorldData.convertRealToLocalBlockSpace(position.x),
            ServerWorldData.convertRealToLocalBlockSpace(position.y),
            ServerWorldData.convertRealToLocalBlockSpace(position.z)
        );
    }

    /**
     * Converts a real coordinate to a local block grid space coordinate
     * @param position The real coordinate
     * @param destVec The vector to store the result in
     */
    public static void convertRealToLocalBlockSpace(Vector3d position, Vector3i destVec){
        destVec.set(
            ServerWorldData.convertRealToLocalBlockSpace(position.x),
            ServerWorldData.convertRealToLocalBlockSpace(position.y),
            ServerWorldData.convertRealToLocalBlockSpace(position.z)
        );
    }

    /**
     * Converts a world coordinate to a macro scale coordinate
     * @param worldPos The world position
     * @return The macro scale position
     */
    public Vector3i convertWorldToMacroScale(Vector3i worldPos){
        return new Vector3i(
            worldPos.x / this.serverTerrainManager.getModel().getMacroDataScale(),
            worldPos.y / this.serverTerrainManager.getModel().getMacroDataScale(),
            worldPos.z / this.serverTerrainManager.getModel().getMacroDataScale()
        );
    }

    /**
     * Converts a world coordinate to a macro scale coordinate
     * @param worldPos The world position
     * @return The macro scale position
     */
    public int convertWorldToMacroScale(int worldPos){
        return worldPos / this.serverTerrainManager.getModel().getMacroDataScale();
    }

    /**
     * Clamps the world position to the floored macro value in world pos
     * @param worldPos The world position
     * @return The floor macro value in world pos
     */
    public int clampWorldToMacro(int worldPos){
        return (worldPos / this.serverTerrainManager.getModel().getMacroDataScale()) * this.serverTerrainManager.getModel().getMacroDataScale();
    }

    /**
     * Clamps a real space position to the closest block space position
     * @param realPos The real space position
     * @return The real space position that is clamped to the closest block space position
     */
    public static Vector3d clampRealToBlock(Vector3d realPos){
        return new Vector3d(
            realPos.x - realPos.x % BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            realPos.y - realPos.y % BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            realPos.z - realPos.z % BlockChunkData.BLOCK_SIZE_MULTIPLIER
        );
    }

    /**
     * Clamps a real position within bounds of the world
     * @param realPos The real position
     */
    public void clampWithinBounds(Vector3d realPos){
        if(realPos.x < 0){
            realPos.x = 0;
        }
        if(realPos.y < 0){
            realPos.y = 0;
        }
        if(realPos.z < 0){
            realPos.z = 0;
        }
    }
    
    /**
     * Gets the terrain manager for this world
     * @return The terrain manager if it exists, null otherwise
     */
    public ServerTerrainManager getServerTerrainManager(){
        return this.serverTerrainManager;
    }

    /**
     * Gets the fluid manager for this world
     * @return The fluid manager if it exists, null otherwise
     */
    public ServerFluidManager getServerFluidManager(){
        return this.serverFluidManager;
    }

    /**
     * Gets the block manager for this world
     * @return The block manager if it exists, null otherwise
     */
    public ServerBlockManager getServerBlockManager(){
        return this.serverBlockManager;
    }

    /**
     * Sets the chunk managers
     * @param serverTerrainManager The terrain manager
     * @param serverFluidManager The fluid manager
     * @param serverBlockManager The server block manager
     */
    public void setManagers(ServerTerrainManager serverTerrainManager, ServerFluidManager serverFluidManager, ServerBlockManager serverBlockManager){
        this.serverTerrainManager = serverTerrainManager;
        this.serverFluidManager = serverFluidManager;
        this.serverBlockManager = serverBlockManager;
        this.serverTerrainManager.setParent(this);
        this.serverFluidManager.setParent(this);
        if(this.serverTerrainManager == null || this.serverFluidManager == null){
            throw new IllegalStateException("Setting world data managers to a null manager " + this.serverTerrainManager + " " + this.serverFluidManager);
        }
    }

}
