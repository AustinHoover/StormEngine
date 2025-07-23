package electrosphere.entity.types.terrain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.ode4j.ode.DGeom;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.terrain.cells.ClientDrawCellManager;
import electrosphere.client.terrain.cells.DrawCell;
import electrosphere.client.terrain.cells.VoxelTextureAtlas;
import electrosphere.client.terrain.data.TerrainChunkData;
import electrosphere.client.terrain.manager.ClientTerrainManager;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.meshgen.TransvoxelModelGeneration;
import electrosphere.renderer.meshgen.TransvoxelModelGeneration.TransvoxelChunkData;
import electrosphere.server.datacell.Realm;

/**
 * Utilities for creating terrain chunk entities
 */
public class TerrainChunk {
    
    /**
     * Used for generating terrain chunks
     */
    static final ExecutorService generationService = Executors.newFixedThreadPool(ThreadCounts.TERRAIN_MESHGEN_THREADS);

    /**
     * Creates a client terrain chunk based on weights and values provided
     * @param chunkData the chunk data to generate with
     * @param toDelete The entity to delete on full generation of this entity
     * @param notifyTarget The target draw cell to notify once this has successfully generated its model
     * @param levelOfDetail Increasing value that increments level of detail. 0 would be full resolution, 1 would be half resolution and so on. Only generates physics if levelOfDetail is 0
     * @param hasPolygons true if the chunk has polygons to generate a model with, false otherwise
     * @return The terrain chunk entity
     */
    public static Entity clientCreateTerrainChunkEntity(
        TransvoxelChunkData chunkData,
        DrawCell notifyTarget,
        Entity toDelete,
        int levelOfDetail,
        VoxelTextureAtlas atlas,
        Vector3i worldPos,
        boolean hasPolygons
    ){
        Globals.profiler.beginAggregateCpuSample("TerrainChunk.clientCreateTerrainChunkEntity");

        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        CommonEntityUtils.setEntityType(rVal, EntityType.ENGINE);
        if(hasPolygons && chunkData.terrainGrid != null && chunkData.textureGrid != null){
            generationService.submit(() -> {
                TerrainChunkData data;
                try {
                    data = TransvoxelModelGeneration.generateTerrainChunkData(chunkData);
                    data.constructBuffers();
                    if(Globals.clientState.clientScene.containsEntity(rVal) && data.getFaceElements().length > 0){
                        String modelPath = ClientTerrainManager.queueTerrainGridGeneration(data, atlas, notifyTarget, toDelete);
                        EntityCreationUtils.makeEntityDrawablePreexistingModel(rVal, modelPath);
                        if(levelOfDetail == BlockChunkData.LOD_FULL_RES && data.getFaceElements().length > 0){
                            PhysicsEntityUtils.clientAttachTriGeomCollider(rVal, data);
                            Vector3d finalPos = new Vector3d(EntityUtils.getPosition(rVal));
                            CollisionObjUtils.clientPositionCharacter(rVal, finalPos, new Quaterniond());
                        } else {
                            EntityCreationUtils.bypassShadowPass(rVal);
                            EntityCreationUtils.bypassVolumetics(rVal);
                        }
                        rVal.putData(EntityDataStrings.HAS_UNIQUE_MODEL, true);
                    } else {
                        if(notifyTarget != null){
                            notifyTarget.alertToGeneration();
                        }
                        if(toDelete != null){
                            ClientEntityUtils.destroyEntity(toDelete);
                        }
                        LoggerInterface.loggerEngine.DEBUG("Finished generating terrain polygons; however, entity has already been deleted.");
                    }
                } catch (Throwable e){
                    LoggerInterface.loggerEngine.ERROR(e);
                }
            });
        } else {
            if(notifyTarget != null){
                notifyTarget.alertToGeneration();
            }
            if(toDelete != null){
                ClientEntityUtils.destroyEntity(toDelete);
            }
        }

        Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.TERRAIN);
        rVal.putData(EntityDataStrings.TERRAIN_IS_TERRAIN, true);
        Globals.profiler.endCpuSample();
        return rVal;
    }

    /**
     * Generates terrain chunk data from a set of weights and values
     * @param weights The weights
     * @param values The values
     * @return The terrain chunk data
     */
    public static TerrainChunkData serverGenerateTerrainChunkData(float[][][] weights, int[][][] values){
        TransvoxelChunkData chunkData = new TransvoxelChunkData(weights, values, ClientDrawCellManager.FULL_RES_LOD);
        TerrainChunkData data = TransvoxelModelGeneration.generateTerrainChunkData(chunkData);
        return data;
    }

    /**
     * Creates a terrain chunk entity on the server
     * @param entity The entity to populate
     * @param data The terrain chunk data
     */
    public static void serverCreateTerrainChunkEntity(Entity entity, TerrainChunkData data){
        if(data.getVertices().length > 0){
            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            if(realm != null){
                PhysicsEntityUtils.serverAttachTriGeomCollider(entity, data);
                DGeom terrainGeom = PhysicsEntityUtils.getDGeom(entity);
                Vector3d entityPos = EntityUtils.getPosition(entity);
                Quaterniond entityRot = EntityUtils.getRotation(entity);
                PhysicsUtils.setGeomTransform(realm.getCollisionEngine(), entityPos, entityRot, terrainGeom);
                entity.putData(EntityDataStrings.TERRAIN_IS_TERRAIN, true);
                CommonEntityUtils.setEntityType(entity, EntityType.ENGINE);
            }
            // ServerEntityUtils.initiallyPositionEntity(realm, rVal, position);
                    // physicsObject = PhysicsUtils.attachTerrainRigidBody(physicsEntity,heightmap,true);
        // Realm realm = Globals.serverState.realmManager.getEntityRealm(physicsEntity);
        // realm.getCollisionEngine().registerPhysicsEntity(physicsEntity);

        }
    }

    /**
     * Halts all running generation threads
     */
    public static void haltThreads(){
        generationService.shutdownNow();
    }

    /**
     * Checks if this is a terrain entity
     * @param entity The entity
     * @return True if it is a terrain entity, false otherwise
     */
    public static boolean isTerrainEntity(Entity entity){
        return entity.containsKey(EntityDataStrings.TERRAIN_IS_TERRAIN);
    }

    /**
     * Checks if this is a block entity
     * @param entity The entity
     * @return True if it is a block entity, false otherwise
     */
    public static boolean isBlockEntity(Entity entity){
        return entity.containsKey(EntityDataStrings.BLOCK_ENTITY);
    }

}
