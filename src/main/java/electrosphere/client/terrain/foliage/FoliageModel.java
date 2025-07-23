package electrosphere.client.terrain.foliage;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.queue.QueuedTexture;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.foliage.AmbientFoliage;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.actor.instance.TextureInstancedActor;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Generates a foliage model
 */
public class FoliageModel {

    /**
     * Number of frames to wait before destroying the chunk entity
     */
    public static final int FRAMES_TO_WAIT_BEFORE_DESTRUCTION = 25;

    /**
     * Number of child cells per parent cell
     */
    static final int CHILD_CELLS_PER_PARENT = 8;

    /**
     * Wiggle room in number of entries
     */
    static final int BUFFER_WIGGLE_ROOM = 200;

    /**
     * The interval to space along
     */
    static final int TARGET_FOLIAGE_SPACING = 200;

    /**
     * The number of floats we're passing per blade of grass
     * 3 for position
     * 2 for rotation
     * 1 for vertical scale
     */
    protected static final int NUM_PER_INSTANCE_VARS = 6;

    /**
     * <p>
     * Size of a single item of foliage in the texture buffer
     * </p>
     * A lot of these are x 4 to account for size of float
     * 3 x 4 for position
     * 2 x 4 for euler rotation
     * 1 x 4 for height scaling
     * 
     * 
     * eventually:
     * grass type
     * color
     * wind characteristics?
     */
    protected static final int SINGLE_FOLIAGE_DATA_SIZE_BYTES = NUM_PER_INSTANCE_VARS * 4;

    /**
     * Cutoff to place foliage at, weight-wise
     */
    static final double FOLIAGE_CUTOFF = -1.0;

    /**
     * Cutoff for the cummulative weight to place foliage at
     */
    static final double FOLIAGE_CUMMULATIVE_WEIGHT_CUTOFF = 0;

    /**
     * The target number of foliage to place per cell
     */
    static final int TARGET_FOLIAGE_PER_CELL = TARGET_FOLIAGE_SPACING * TARGET_FOLIAGE_SPACING + BUFFER_WIGGLE_ROOM;

    /**
     * Maximum allowed height of the data texture
     */
    static final int MAX_TEXTURE_HEIGHT = 2048;

    /**
     * The target width of the image
     */
    static final int TARGET_WIDTH_OF_IMAGE = TARGET_FOLIAGE_PER_CELL / MAX_TEXTURE_HEIGHT * (SINGLE_FOLIAGE_DATA_SIZE_BYTES / 4);

    /**
     * The length of the ray to ground test with
     */
    static final float RAY_LENGTH = 2.5f;

    /**
     * The height above the chunk to start from when sampling downwards
     */
    static final float SAMPLE_START_HEIGHT = 1.0f;

    /**
     * The ID of the air voxel
     */
    static final int AIR_VOXEL_ID = 0;

    /**
     * Offset to sample by
     */
    static final float SAMPLE_OFFSET = 0.499f;

    /**
     * Vertex shader path
     */
    protected static final String vertexPath = "Shaders/entities/foliage/foliage.vs";

    /**
     * Fragment shader path
     */
    protected static final String fragmentPath = "Shaders/entities/foliage/foliage.fs";

    /**
     * Name of the mesh to apply uniforms to
     */
    public static final String MESH_NAME = "Plane";

    /**
     * Name of the uniform for the tip color
     */
    public static final String UNIFORM_TIP_COLOR = "tipColor";

    /**
     * Name of the uniform for the base color
     */
    public static final String UNIFORM_BASE_COLOR = "baseColor";

    /**
     * Used for generating foliage cells
     */
    static final ExecutorService generationService = Executors.newFixedThreadPool(ThreadCounts.FOLIAGE_MESHGEN_THREADS);

    /**
     * Creates a client foliage chunk based on weights and values provided
     * @param toDelete The entity to delete on full generation of this entity
     * @param notifyTarget The target draw cell to notify once this has successfully generated its model
     * @param levelOfDetail Increasing value that increments level of detail. 0 would be full resolution, 1 would be half resolution and so on. Only generates physics if levelOfDetail is 0
     * @param hasFoliage true if the chunk has polygons to generate a model with, false otherwise
     * @return The terrain chunk entity
     */
    public static Entity clientCreateFoliageChunkEntity(
        List<String> foliageTypesSupported,
        int scale,
        Vector3d realPos,
        Vector3i worldPos,
        Vector3i voxelPos,
        FoliageCell notifyTarget,
        Entity toDelete
    ){
        Globals.profiler.beginAggregateCpuSample("FoliageModel.clientCreateFoliageChunkEntity");

        Entity rVal = EntityCreationUtils.createClientSpatialEntity();

        generationService.submit(() -> {
            try {
                Random placementRandomizer = new Random();
                //get type
                String foliageTypeName = foliageTypesSupported.get(0);
                FoliageType foliageType = Globals.gameConfigCurrent.getFoliageMap().getType(foliageTypeName);
                CommonEntityUtils.setCommonData(rVal, foliageType);
                CommonEntityUtils.setEntityType(rVal, EntityType.FOLIAGE);
                CommonEntityUtils.setEntitySubtype(rVal, foliageType.getId());
                rVal.putData(EntityDataStrings.FOLIAGE_TYPE, foliageType);

                //create cell and buffer
                ByteBuffer buffer = BufferUtils.createByteBuffer(MAX_TEXTURE_HEIGHT * TARGET_WIDTH_OF_IMAGE * SINGLE_FOLIAGE_DATA_SIZE_BYTES);
                if(buffer.capacity() < TARGET_FOLIAGE_PER_CELL * SINGLE_FOLIAGE_DATA_SIZE_BYTES){
                    LoggerInterface.loggerEngine.WARNING("Failed to allocate data for foliage cell! " + buffer.limit());
                }
                FloatBuffer floatBufferView = buffer.asFloatBuffer();
                int drawCount = 0;
                Vector3i currWorldPos = new Vector3i();
                Vector3i currVoxelPos = new Vector3i(voxelPos);
                Vector3d currRealPos = new Vector3d();
                for(int x = 0; x < scale; x++){
                    for(int z = 0; z < scale; z++){
                        for(int y = 0; y < scale; y++){
                            currVoxelPos.set(voxelPos).add(x,y,z);
                            currWorldPos.set(worldPos).add(
                                x / ServerTerrainChunk.CHUNK_DIMENSION,
                                y / ServerTerrainChunk.CHUNK_DIMENSION,
                                z / ServerTerrainChunk.CHUNK_DIMENSION
                            );
                            currRealPos.set(
                                currVoxelPos.x + currWorldPos.x * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
                                currVoxelPos.y + currWorldPos.y * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
                                currVoxelPos.z + currWorldPos.z * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET
                            );
                            ChunkData data = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(currWorldPos,ChunkData.NO_STRIDE);
                            if(data == null){
                                continue;
                            }
                            List<String> currentList = Globals.gameConfigCurrent.getVoxelData().getTypeFromId(data.getType(currVoxelPos)).getAmbientFoliage();
                            if(currentList == null){
                                continue;
                            }
                            if(data.getType(currVoxelPos.x,currVoxelPos.y+1,currVoxelPos.z) != AIR_VOXEL_ID){
                                continue;
                            }
                            int numGenerated = FoliageModel.insertBlades(
                                currWorldPos, currRealPos, currVoxelPos,
                                scale, placementRandomizer,
                                x, y, z,
                                data.getType(currVoxelPos.x,currVoxelPos.y,currVoxelPos.z),
                                foliageType,
                                floatBufferView, data
                            );
                            drawCount = drawCount + numGenerated;
                            // if(numGenerated > 0){
                            //     break;
                            // }
                        }
                    }
                }
                if(drawCount > 0){
                    buffer.position(0);
                    buffer.limit(TARGET_FOLIAGE_PER_CELL * SINGLE_FOLIAGE_DATA_SIZE_BYTES);

                    int textureHeight = MAX_TEXTURE_HEIGHT;
                    if(drawCount < MAX_TEXTURE_HEIGHT){
                        textureHeight = drawCount;
                    }
                    int textureWidth = (1 + (drawCount / MAX_TEXTURE_HEIGHT)) * (SINGLE_FOLIAGE_DATA_SIZE_BYTES / 4);

                    //construct data texture
                    QueuedTexture queuedAsset = QueuedTexture.createFromBuffer(buffer,textureWidth,textureHeight);
                    Globals.assetManager.queuedAsset(queuedAsset);

                    TextureInstancedActor actor = TextureInstancedActor.attachTextureInstancedActor(rVal, foliageType.getGraphicsTemplate().getModel().getPath(), vertexPath, fragmentPath, queuedAsset, drawCount, textureWidth / NUM_PER_INSTANCE_VARS);
                    ClientEntityUtils.initiallyPositionEntity(rVal, realPos, new Quaterniond());
                    EntityUtils.getScale(rVal).set(1,1,1);
                    //add ambient foliage behavior tree
                    AmbientFoliage.attachAmbientFoliageTree(rVal, 1.0f, foliageType.getGrowthModel().getGrowthRate());
                    Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.DRAW_FOLIAGE_PASS);

                    //apply grass uniforms if present in definition
                    if(foliageType.getGrassData() != null){
                        actor.setUniformOnMesh(FoliageModel.MESH_NAME, FoliageModel.UNIFORM_BASE_COLOR, foliageType.getGrassData().getBaseColor());
                        actor.setUniformOnMesh(FoliageModel.MESH_NAME, FoliageModel.UNIFORM_TIP_COLOR, foliageType.getGrassData().getTipColor());
                    }
                }
                if(toDelete != null){
                    ClientEntityUtils.destroyEntity(toDelete);
                }
                if(notifyTarget != null){
                    notifyTarget.alertToGeneration();
                }
            } catch (Error e){
                LoggerInterface.loggerEngine.ERROR(e);
            } catch(Exception e){
                LoggerInterface.loggerEngine.ERROR(e);
            }
        });

        Globals.profiler.endCpuSample();
        return rVal;
    }

    /**
     * Insert blades of grass into the entity
     * @param vX the x offset of the voxel
     * @param vY the y offset of the voxel
     * @param vZ the z offset of the voxel
     * @param floatBufferView the gpu data buffer
     * @param chunkData the chunk data
     * @return the number of blades of grass added
     */
    protected static int insertBlades(
        Vector3i worldPos, Vector3d realPos, Vector3i voxelPos,
        int scale, Random placementRandomizer,
        int vX, int vY, int vZ,
        int targetVoxelType,
        FoliageType foliageType,
        FloatBuffer floatBufferView, ChunkData chunkData
    ){
        int rVal = 0;

        float maxTipCurve = foliageType.getGrassData().getMaxTipCurve();
        float minimumHeight = foliageType.getGrassData().getMinHeight();
        float heightMultiplier = foliageType.getGrassData().getMaxHeight() - foliageType.getGrassData().getMinHeight();

        //construct simple grid to place foliage on
        // Vector3d sample_00 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add(-SAMPLE_OFFSET,SAMPLE_START_HEIGHT,-SAMPLE_OFFSET), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_01 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add(-SAMPLE_OFFSET,SAMPLE_START_HEIGHT,             0), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_02 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add(-SAMPLE_OFFSET,SAMPLE_START_HEIGHT, SAMPLE_OFFSET), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_10 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add(             0,SAMPLE_START_HEIGHT,-SAMPLE_OFFSET), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_11 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add(             0,SAMPLE_START_HEIGHT,             0), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_12 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add(             0,SAMPLE_START_HEIGHT, SAMPLE_OFFSET), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_20 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add( SAMPLE_OFFSET,SAMPLE_START_HEIGHT,-SAMPLE_OFFSET), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_21 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add( SAMPLE_OFFSET,SAMPLE_START_HEIGHT,             0), new Vector3d(0,-1,0), RAY_LENGTH);
        // Vector3d sample_22 = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(realPos).add( SAMPLE_OFFSET,SAMPLE_START_HEIGHT, SAMPLE_OFFSET), new Vector3d(0,-1,0), RAY_LENGTH);
        
        boolean sample_11 = true;
        boolean sample_12 = true;
        boolean sample_21 = true;
        boolean sample_22 = true;
        //get the heights of each sample
        float weight_11 = (chunkData.getWeight(voxelPos) + 1) / 2.0f;
        float height_11 = weight_11 + voxelPos.y + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
        
        float height_12 = height_11;
        float weight_12 = 0;
        if(voxelPos.z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1){
            if(voxelPos.y < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1 && chunkData.getWeight(voxelPos.x, voxelPos.y + 1, voxelPos.z + 1) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x, voxelPos.y + 1, voxelPos.z + 1) == targetVoxelType){
                weight_12 = (chunkData.getWeight(voxelPos.x, voxelPos.y + 1, voxelPos.z + 1) + 1) / 2.0f;
                height_12 = weight_12 + voxelPos.y + 1 + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else if(chunkData.getWeight(voxelPos.x, voxelPos.y, voxelPos.z + 1) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x, voxelPos.y, voxelPos.z + 1) == targetVoxelType){
                weight_12 = (chunkData.getWeight(voxelPos.x, voxelPos.y, voxelPos.z + 1) + 1) / 2.0f;
                height_12 = weight_12 + voxelPos.y + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else if(voxelPos.y > 0 && chunkData.getWeight(voxelPos.x, voxelPos.y - 1, voxelPos.z + 1) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x, voxelPos.y - 1, voxelPos.z + 1) == targetVoxelType){
                weight_12 = (chunkData.getWeight(voxelPos.x, voxelPos.y - 1, voxelPos.z + 1) + 1) / 2.0f;
                height_12 = weight_12 + voxelPos.y - 1 + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else {
                sample_12 = false;
            }
        } else {
            sample_12 = false;
        }

        float height_21 = height_11;
        float weight_21 = 0;
        if(voxelPos.x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1){
            if(voxelPos.y < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1 && chunkData.getWeight(voxelPos.x + 1, voxelPos.y + 1, voxelPos.z) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x + 1, voxelPos.y + 1, voxelPos.z) == targetVoxelType){
                weight_21 = (chunkData.getWeight(voxelPos.x + 1, voxelPos.y + 1, voxelPos.z) + 1) / 2.0f;
                height_21 = weight_21 + voxelPos.y + 1 + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else if(chunkData.getWeight(voxelPos.x + 1, voxelPos.y, voxelPos.z) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x + 1, voxelPos.y, voxelPos.z) == targetVoxelType){
                weight_21 = (chunkData.getWeight(voxelPos.x + 1, voxelPos.y, voxelPos.z) + 1) / 2.0f;
                height_21 = weight_21 + voxelPos.y + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else if(voxelPos.y > 0 && chunkData.getWeight(voxelPos.x + 1, voxelPos.y - 1, voxelPos.z) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x + 1, voxelPos.y - 1, voxelPos.z) == targetVoxelType){
                weight_21 = (chunkData.getWeight(voxelPos.x + 1, voxelPos.y - 1, voxelPos.z) + 1) / 2.0f;
                height_21 = weight_21 + voxelPos.y - 1 + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else {
                sample_21 = false;
            }
        } else {
            sample_21 = false;
        }
        
        float height_22 = height_11;
        float weight_22 = 0;
        if(voxelPos.x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1 && voxelPos.z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1){
            if(voxelPos.y < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1 && chunkData.getWeight(voxelPos.x + 1, voxelPos.y + 1, voxelPos.z + 1) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x + 1, voxelPos.y + 1, voxelPos.z + 1) == targetVoxelType){
                weight_22 = (chunkData.getWeight(voxelPos.x + 1, voxelPos.y + 1, voxelPos.z + 1) + 1) / 2.0f;
                height_22 = weight_22 + voxelPos.y + 1 + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else if(chunkData.getWeight(voxelPos.x + 1, voxelPos.y, voxelPos.z + 1) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x + 1, voxelPos.y, voxelPos.z + 1) == targetVoxelType){
                weight_22 = (chunkData.getWeight(voxelPos.x + 1, voxelPos.y, voxelPos.z + 1) + 1) / 2.0f;
                height_22 = weight_22 + voxelPos.y + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else if(voxelPos.y > 0 && chunkData.getWeight(voxelPos.x + 1, voxelPos.y - 1, voxelPos.z + 1) > FOLIAGE_CUTOFF && chunkData.getType(voxelPos.x + 1, voxelPos.y - 1, voxelPos.z + 1) == targetVoxelType){
                weight_22 = (chunkData.getWeight(voxelPos.x + 1, voxelPos.y - 1, voxelPos.z + 1) + 1) / 2.0f;
                height_22 = weight_22 + voxelPos.y - 1 + worldPos.y * ServerTerrainChunk.CHUNK_DIMENSION;
            } else {
                sample_22 = false;
            }
        } else {
            sample_22 = false;
        }

        //each height is in real world coordinates that are absolute
        //when rendering, there's already a y offset for the center of the field of grass (based on the model matrix)
        //so when offseting the position of the blade of grass RELATIVE to the overall instance being drawn, need to subtract the real world coordinates of the overall instance
        //in other words realPos SPECIFICALLY for the y dimension, for x and z you don't need to worry about it
        
        float cummulativeWeight = weight_11 + weight_12 + weight_21 + weight_22;

        //if we don't find data for the center sample, can't place grass so don't create entity
        //generate positions to place
        if(sample_11 && sample_12 && sample_21 && sample_22 && cummulativeWeight > FOLIAGE_CUMMULATIVE_WEIGHT_CUTOFF){
            for(int x = 0; x < TARGET_FOLIAGE_SPACING; x=x+scale){
                for(int z = 0; z < TARGET_FOLIAGE_SPACING; z=z+scale){
                    //get position to place
                    double rand1 = placementRandomizer.nextDouble();
                    double rand2 = placementRandomizer.nextDouble();
                    double relativePositionOnGridX = x /  (1.0 * TARGET_FOLIAGE_SPACING) + rand1 / TARGET_FOLIAGE_SPACING;
                    double relativePositionOnGridZ = z /  (1.0 * TARGET_FOLIAGE_SPACING) + rand2 / TARGET_FOLIAGE_SPACING;
                    //determine quadrant we're placing in
                    double offsetY = 0;
                    boolean addBlade = false;
                    double manualAdjustment = -0.50;
                    // System.out.println(relativePositionOnGridX + " " + relativePositionOnGridZ);
                    //if we have heights for all four surrounding spots, interpolate for y value
                    offsetY = 
                        height_11 * (1-relativePositionOnGridX) * (1-relativePositionOnGridZ) + 
                        height_12 * (1-relativePositionOnGridX) * (  relativePositionOnGridZ) + 
                        height_21 * (  relativePositionOnGridX) * (1-relativePositionOnGridZ) + 
                        height_22 * (  relativePositionOnGridX) * (  relativePositionOnGridZ) +
                        manualAdjustment
                    ;
                    addBlade = true;
                    // double percent = 
                    // (1-relativePositionOnGridX) * (1-relativePositionOnGridZ) + 
                    //     (1-relativePositionOnGridX) * (  relativePositionOnGridZ) + 
                    //     (  relativePositionOnGridX) * (1-relativePositionOnGridZ) + 
                    //     (  relativePositionOnGridX) * (  relativePositionOnGridZ);
                    // if(
                    //     percent < 0.99 || percent > 1.01
                    // ){
                    //     System.out.println(percent);
                    // }
                    if(addBlade){
                        //convert y to relative to chunk
                        offsetY = offsetY - realPos.y;
                        double rotVar = placementRandomizer.nextDouble() * Math.PI * 2;
                        double rotVar2 = placementRandomizer.nextDouble() * maxTipCurve;
                        double heightScale = placementRandomizer.nextDouble();
                        if(floatBufferView.limit() >= floatBufferView.position() + SINGLE_FOLIAGE_DATA_SIZE_BYTES / 4){
                            floatBufferView.put((float)relativePositionOnGridX + vX);
                            floatBufferView.put((float)offsetY + vY);
                            floatBufferView.put((float)relativePositionOnGridZ + vZ);
                            floatBufferView.put((float)rotVar);
                            floatBufferView.put((float)rotVar2);
                            floatBufferView.put((float)(heightScale * heightMultiplier + minimumHeight));
                            rVal++;
                        }
                    }
                }
            }
        }
        // else {
        //     String message = "Failed to collide with a chunk that definitely should already exist!\n";
        //     message = message + "sample pos: " + new Vector3d(realPos).add(0,SAMPLE_START_HEIGHT,0) + "\n";
        //     message = message + "generated physics: " + Globals.clientDrawCellManager.hasGeneratedPhysics(worldPos.x, worldPos.y, worldPos.z) + "\n";
        //     throw new Error(message);
        // }
        return rVal;
    }

    /**
     * Shuts down the model generation threads
     */
    public static void haltThreads(){
        generationService.shutdown();
    }
    
}
