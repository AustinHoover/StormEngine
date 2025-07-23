package electrosphere.entity.types.fluid;

import electrosphere.client.fluid.manager.ClientFluidManager;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.renderer.meshgen.FluidChunkModelGeneration;

/**
 * Creates a fluid chunk entity
 */
public class FluidChunk {
    
    /**
     * Creates a client fluid chunk based on weights and values provided
     * @param weights The fluid weights
     * @param values The values (block types)
     * @return The fluid chunk entity
     */
    public static Entity clientCreateFluidChunkEntity(float[][][] weights){
        
        FluidChunkModelData data = FluidChunkModelGeneration.generateFluidChunkData(weights);
        String modelPath = ClientFluidManager.queueFluidGridGeneration(data);

        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawablePreexistingModel(rVal, modelPath);
        // if(data.vertices.size() > 0 && levelOfDetail < 1){
        //     PhysicsUtils.clientAttachTerrainChunkRigidBody(rVal, data);
        // }

        rVal.putData(EntityDataStrings.FLUID_IS_FLUID, true);
        DrawableUtils.makeEntityTransparent(rVal);
        rVal.putData(EntityDataStrings.DATA_STRING_MODEL_PATH, modelPath);

        return rVal;
    }

}
