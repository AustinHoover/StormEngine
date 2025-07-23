package electrosphere.client.fluid.cells;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.ode4j.ode.DBody;

import electrosphere.client.fluid.cache.FluidChunkData;
import electrosphere.collision.CollisionEngine;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.types.fluid.FluidChunk;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 *
 * @author satellite
 */
public class FluidCell {
    //the position of the draw cell in world coordinates
    Vector3i worldPos;
    
    FluidChunkData data;
    
    Entity modelEntity;
    
    VisualShader program;
    
    DBody physicsObject;

    float[][][] weights = new float[ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE][ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE][ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE];
    
    //the value of an empty fluid cell weight that is not neighbored by a fluid value
    public static final float ISO_SURFACE_EMPTY = -1;
    
    FluidCell(){
        
    }
    
    
    /**
     * Constructs a drawcell object
     */
    public static FluidCell generateFluidCell(
            Vector3i worldPos,
            FluidChunkData data,
            VisualShader program
    ){
        FluidCell rVal = new FluidCell();
        rVal.worldPos = worldPos;
        rVal.program = program;
        rVal.data = data;
        return rVal;
    }

    /**
     * Generates a drawable entity based on this chunk
     */
    public void generateDrawableEntity(){
        if(modelEntity != null){
            Globals.clientState.clientScene.deregisterEntity(modelEntity);
        }

        FluidChunkData currentChunk = Globals.clientState.clientFluidManager.getChunkDataAtWorldPoint(worldPos);
        if(!currentChunk.isHomogenous()){
            this.fillInData(currentChunk);
            modelEntity = FluidChunk.clientCreateFluidChunkEntity(weights);
            ClientEntityUtils.initiallyPositionEntity(modelEntity, getRealPos(), new Quaterniond());
        }
    }

    protected Vector3d getRealPos(){
        return new Vector3d(
            worldPos.x * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.y * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.z * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET
        );
    }
    
    /**
     * Destroys a drawcell including its physics
     */
    public void destroy(){
        CollisionEngine collisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine();
        if(modelEntity != null){
            collisionEngine.destroyPhysics(modelEntity);
            ClientEntityUtils.destroyEntity(modelEntity);
            //destruct model
            String modelPath = (String)modelEntity.getData(EntityDataStrings.DATA_STRING_MODEL_PATH);
            Globals.assetManager.deregisterModelPath(modelPath);
        }
    }

    /**
     * Gets the current chunk data for this draw cell
     * @return The chunk data
     */
    public FluidChunkData getData(){
        return data;
    }

    /**
     * Fills in the internal arrays of data for generate terrain models
     */
    private void fillInData(FluidChunkData currentChunk){

        //
        //fill in data
        //
        //main chunk
        if(!currentChunk.isHomogenous()){
            for(int x = ServerFluidChunk.TRUE_DATA_OFFSET; x < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE + ServerFluidChunk.TRUE_DATA_OFFSET; x++){
                for(int y = ServerFluidChunk.TRUE_DATA_OFFSET; y < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE + ServerFluidChunk.TRUE_DATA_OFFSET; y++){
                    for(int z = ServerFluidChunk.TRUE_DATA_OFFSET; z < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE + ServerFluidChunk.TRUE_DATA_OFFSET; z++){
                        weights[x-ServerFluidChunk.TRUE_DATA_OFFSET][y-ServerFluidChunk.TRUE_DATA_OFFSET][z-ServerFluidChunk.TRUE_DATA_OFFSET] = currentChunk.getWeight(x,y,z);
                    }
                }
            }
        }

        //now set neighboring air weights based on nearby fluid count
        //idea being that we dont have the snapping behavior from iso surface jumping from -1->0.01
        int[] neighborIndexX = new int[]{-1,1,0,0,0,0};
        int[] neighborIndexY = new int[]{0,0,-1,1,0,0};
        int[] neighborIndexZ = new int[]{0,0,0,0,-1,1};
        for(int x = 0; x < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE; x++){
            for(int y = 0; y < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE; y++){
                for(int z = 0; z < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE; z++){
                    if(weights[x][y][z] > 0){
                        continue;
                    }
                    for(int i = 0; i < 6; i++){
                        int currX = x + neighborIndexX[i];
                        int currY = y + neighborIndexY[i];
                        int currZ = z + neighborIndexZ[i];
                        if(
                            currX >= 0 && currX < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE &&
                            currY >= 0 && currY < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE &&
                            currZ >= 0 && currZ < ServerFluidChunk.TRUE_DATA_GENERATOR_SIZE &&
                            (1 + weights[x][y][z]) < weights[currX][currY][currZ]
                        ){
                            weights[x][y][z] = -(1 - weights[currX][currY][currZ]);
                            if(weights[x][y][z] >= 0){
                                weights[x][y][z] = -0.01f;
                            }
                        }
                    }
                }
            }
        }
    }
    
}
