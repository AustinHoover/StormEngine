package electrosphere.server.datacell.physics;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.client.terrain.data.TerrainChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.types.terrain.BlockChunkEntity;
import electrosphere.entity.types.terrain.TerrainChunk;
import electrosphere.renderer.meshgen.BlockMeshgen;
import electrosphere.renderer.meshgen.BlockMeshgen.BlockMeshData;
import electrosphere.server.datacell.Realm;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * An entity which contains physics for terrain for a given chunk on the server
 */
public class PhysicsDataCell {

    Entity physicsEntity;
    Entity blockPhysicsEntity;

    ServerTerrainChunk terrainChunk;
    BlockChunkData blockChunk;
    
    float[][][] weights;
    int[][][] types;

    /**
     * The terrain vertex data
     */
    TerrainChunkData terrainChunkData;

    /**
     * The block vertex data
     */
    BlockMeshData blockData;
    
    /**
     * Creates a physics cell
     * @param realm The realm to create it in
     * @param worldPos The world position of the cell
     * @return The cell
     */
    public static PhysicsDataCell createPhysicsCell(
        Entity physicsEntity,
        Entity blockPhysicsEntity

    ){
        PhysicsDataCell rVal = new PhysicsDataCell();
        rVal.physicsEntity = physicsEntity;
        rVal.blockPhysicsEntity = blockPhysicsEntity;
        return rVal;
    }
    
    /**
     * Retires a physics data cell
     */
    public void retireCell(){
        ServerEntityUtils.destroyEntity(physicsEntity);
        this.physicsEntity = null;
        ServerEntityUtils.destroyEntity(blockPhysicsEntity);
        this.blockPhysicsEntity = null;
    }
    
    /**
     * Generates the physics entity for this chunk
     */
    public void generatePhysics(){
        //if the entity hasn't already been created for some reason, need to create it
            
        //
        //fill in weights and types maps
        //
        this.fillInData();

        //grab local reference to this cell's entities
        Entity localPhysicsEnt = this.physicsEntity;
        Entity localBlockPhysicsEntity = this.blockPhysicsEntity;

        //check if cell has already been retired
        if(localBlockPhysicsEntity == null || localPhysicsEnt == null){
            return;
        }

        //generate terrain
        this.terrainChunkData = TerrainChunk.serverGenerateTerrainChunkData(weights, types);
        TerrainChunk.serverCreateTerrainChunkEntity(localPhysicsEnt, this.terrainChunkData);
        localPhysicsEnt.putData(EntityDataStrings.TERRAIN_IS_TERRAIN, true);

        //generate blocks
        this.blockData = BlockMeshgen.rasterize(this.blockChunk);
        BlockChunkEntity.serverCreateBlockChunkEntity(localBlockPhysicsEntity, this.blockData);
        localBlockPhysicsEntity.putData(EntityDataStrings.TERRAIN_IS_TERRAIN, true);
        localBlockPhysicsEntity.putData(EntityDataStrings.BLOCK_ENTITY, true);
    }
    
    /**
     * Destroys the physics for this data cell
     */
    public void destroyPhysics(){
        Realm realm = Globals.serverState.realmManager.getEntityRealm(physicsEntity);
        if(realm != null){
            realm.getCollisionEngine().destroyPhysics(physicsEntity);
            realm.getCollisionEngine().destroyPhysics(blockPhysicsEntity);
        }
    }

    /**
     * Fills in the internal arrays of data for generate terrain models
     */
    private void fillInData(){
        //
        //fill in data
        //
        //main chunk
        this.weights = new float[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
        this.types = new int[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
        for(int x = 0; x < ChunkData.CHUNK_DATA_SIZE; x++){
            for(int y = 0; y < ChunkData.CHUNK_DATA_SIZE; y++){
                for(int z = 0; z < ChunkData.CHUNK_DATA_SIZE; z++){
                    weights[x][y][z] = terrainChunk.getWeight(x,y,z);
                    types[x][y][z] = terrainChunk.getType(x,y,z);
                }
            }
        }
        // //face X
        // if(worldPos.x + 1 < realm.getServerWorldData().getWorldSizeDiscrete()){
        //     currentChunk = serverTerrainManager.getChunk(worldPos.x + 1, worldPos.y, worldPos.z);
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         for(int j = 0; j < ChunkData.CHUNK_SIZE; j++){
        //             weights[ChunkData.CHUNK_SIZE][i][j] = currentChunk.getWeight(0, i, j);
        //             types[ChunkData.CHUNK_SIZE][i][j] = currentChunk.getType(0, i, j);
        //         }
        //     }
        // } else {
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         for(int j = 0; j < ChunkData.CHUNK_SIZE; j++){
        //             weights[ChunkData.CHUNK_SIZE][i][j] = 0;
        //             types[ChunkData.CHUNK_SIZE][i][j] = 0;
        //         }
        //     }
        // }
        // //face Y
        // if(worldPos.y + 1 < realm.getServerWorldData().getWorldSizeDiscrete()){
        //     currentChunk = serverTerrainManager.getChunk(worldPos.x, worldPos.y + 1, worldPos.z);
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         for(int j = 0; j < ChunkData.CHUNK_SIZE; j++){
        //             weights[i][ChunkData.CHUNK_SIZE][j] = currentChunk.getWeight(i, 0, j);
        //             types[i][ChunkData.CHUNK_SIZE][j] = currentChunk.getType(i, 0, j);
        //         }
        //     }
        // } else {
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         for(int j = 0; j < ChunkData.CHUNK_SIZE; j++){
        //             weights[i][ChunkData.CHUNK_SIZE][j] = 0;
        //             types[i][ChunkData.CHUNK_SIZE][j] = 0;
        //         }
        //     }
        // }
        // //face Z
        // if(worldPos.z + 1 < realm.getServerWorldData().getWorldSizeDiscrete()){
        //     currentChunk = serverTerrainManager.getChunk(worldPos.x, worldPos.y, worldPos.z + 1);
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         for(int j = 0; j < ChunkData.CHUNK_SIZE; j++){
        //             weights[i][j][ChunkData.CHUNK_SIZE] = currentChunk.getWeight(i, j, 0);
        //             types[i][j][ChunkData.CHUNK_SIZE] = currentChunk.getType(i, j, 0);
        //         }
        //     }
        // } else {
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         for(int j = 0; j < ChunkData.CHUNK_SIZE; j++){
        //             weights[i][j][ChunkData.CHUNK_SIZE] = 0;
        //             types[i][j][ChunkData.CHUNK_SIZE] = 0;
        //         }
        //     }
        // }
        //edge X-Y
        // if(
        //     worldPos.x + 1 < realm.getServerWorldData().getWorldSizeDiscrete() &&
        //     worldPos.y + 1 < realm.getServerWorldData().getWorldSizeDiscrete()
        // ){
        //     currentChunk = serverTerrainManager.getChunk(worldPos.x + 1, worldPos.y + 1, worldPos.z);
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         weights[ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][i] = currentChunk.getWeight(0, 0, i);
        //         types  [ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][i] = currentChunk.getType(0, 0, i);
        //     }
        // } else {
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         weights[ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][i] = 0;
        //         types  [ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][i] = 0;
        //     }
        // }
        // //edge X-Z
        // if(
        //     worldPos.x + 1 < realm.getServerWorldData().getWorldSizeDiscrete() &&
        //     worldPos.z + 1 < realm.getServerWorldData().getWorldSizeDiscrete()
        // ){
        //     currentChunk = serverTerrainManager.getChunk(worldPos.x + 1, worldPos.y, worldPos.z + 1);
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         weights[ChunkData.CHUNK_SIZE][i][ChunkData.CHUNK_SIZE] = currentChunk.getWeight(0, i, 0);
        //         types  [ChunkData.CHUNK_SIZE][i][ChunkData.CHUNK_SIZE] = currentChunk.getType(0, i, 0);
        //     }
        // } else {
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         weights[ChunkData.CHUNK_SIZE][i][ChunkData.CHUNK_SIZE] = 0;
        //         types  [ChunkData.CHUNK_SIZE][i][ChunkData.CHUNK_SIZE] = 0;
        //     }
        // }
        // //edge Y-Z
        // if(
        //     worldPos.y + 1 < realm.getServerWorldData().getWorldSizeDiscrete() &&
        //     worldPos.z + 1 < realm.getServerWorldData().getWorldSizeDiscrete()
        // ){
        //     currentChunk = serverTerrainManager.getChunk(worldPos.x, worldPos.y + 1, worldPos.z + 1);
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         weights[i][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = currentChunk.getWeight(i, 0, 0);
        //         types  [i][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = currentChunk.getType(i, 0, 0);
        //     }
        // } else {
        //     for(int i = 0; i < ChunkData.CHUNK_SIZE; i++){
        //         weights[i][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = 0;
        //         types  [i][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = 0;
        //     }
        // }
        // if(
        //     worldPos.z + 1 < realm.getServerWorldData().getWorldSizeDiscrete() &&
        //     worldPos.y + 1 < realm.getServerWorldData().getWorldSizeDiscrete() &&
        //     worldPos.z + 1 < realm.getServerWorldData().getWorldSizeDiscrete()
        // ){
        //     currentChunk = serverTerrainManager.getChunk(worldPos.x + 1, worldPos.y + 1, worldPos.z + 1);
        //     weights[ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = currentChunk.getWeight(0, 0, 0);
        //     types[ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = currentChunk.getType(0, 0, 0);
        // } else {
        //     weights[ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = 0;
        //     types[ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE][ChunkData.CHUNK_SIZE] = 0;
        // }
    }

    /**
     * Sets the terrain chunk data for the physics cell
     * @param terrainChunk The terrain chunk data
     */
    public void setTerrainChunk(ServerTerrainChunk terrainChunk) {
        this.terrainChunk = terrainChunk;
    }

    /**
     * Sets the block chunk data for the physics cell
     * @param blockChunk The block chunk data
     */
    public void setBlockChunk(BlockChunkData blockChunk) {
        this.blockChunk = BlockChunkData.cloneShallow(blockChunk);
    }

    /**
     * Gets the terrain vertex data
     * @return The terrain vertex data
     */
    public TerrainChunkData getTerrainChunkData() {
        return terrainChunkData;
    }

    /**
     * Gets the block vertex data
     * @return The block vertex data
     */
    public BlockMeshData getBlockData() {
        return blockData;
    }
    
}
