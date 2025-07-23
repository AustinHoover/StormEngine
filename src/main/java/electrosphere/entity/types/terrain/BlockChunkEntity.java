package electrosphere.entity.types.terrain;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DGeom;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.block.cells.BlockDrawCell;
import electrosphere.client.block.cells.BlockTextureAtlas;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.queue.QueuedModel;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.meshgen.BlockMeshgen;
import electrosphere.renderer.meshgen.BlockMeshgen.BlockMeshData;
import electrosphere.server.datacell.Realm;

/**
 * Generates block chunk entities
 */
public class BlockChunkEntity {
    
    /**
     * Used for generating block chunks
     */
    static final ExecutorService generationService = Executors.newFixedThreadPool(ThreadCounts.BLOCK_MESHGEN_THREADS);

    /**
     * Creates a client block chunk based on weights and values provided
     * @param chunkData the chunk data to generate with
     * @param toDelete The entity to delete on full generation of this entity
     * @param notifyTarget The target draw cell to notify once this has successfully generated its model
     * @param levelOfDetail Increasing value that increments level of detail. 0 would be full resolution, 1 would be half resolution and so on. Only generates physics if levelOfDetail is 0
     * @param hasPolygons true if the chunk has polygons to generate a model with, false otherwise
     * @return The block chunk entity
     */
    public static List<Entity> clientCreateBlockChunkEntity(
        BlockChunkData chunkData,
        BlockDrawCell notifyTarget,
        List<Entity> toDelete,
        int levelOfDetail,
        BlockTextureAtlas atlas,
        boolean hasPolygons
    ){
        Globals.profiler.beginAggregateCpuSample("BlockChunk.clientCreateBlockChunkEntity");
        List<Entity> rVal = new LinkedList<Entity>();


        Map<Integer,Boolean> solidsMap = Globals.gameConfigCurrent.getBlockData().getSolidsMap();
        //
        //Create the entity for rendering solid blocks
        Entity solidsEnt = EntityCreationUtils.createClientSpatialEntity();
        if(hasPolygons && chunkData.getType() != null && chunkData.getMetadata() != null){
            generationService.submit(() -> {
                BlockMeshData data;
                try {
                    data = BlockMeshgen.rasterize(chunkData, true, solidsMap,(int)Math.pow(2,levelOfDetail));
                    if(Globals.clientState.clientScene.containsEntity(solidsEnt) && data.getFaceElements().length > 0){
                        String modelPath = Globals.assetManager.queuedAsset(new QueuedModel(() -> {
                            return BlockMeshgen.generateBlockModel(data);
                        }));
                        EntityCreationUtils.makeEntityDrawablePreexistingModel(solidsEnt, modelPath);
                        if(levelOfDetail == BlockChunkData.LOD_FULL_RES){
                            PhysicsEntityUtils.clientAttachTriGeomCollider(solidsEnt, data);
                            ClientEntityUtils.repositionEntity(solidsEnt, new Vector3d(EntityUtils.getPosition(solidsEnt)), new Quaterniond());
                        } else {
                            EntityCreationUtils.bypassShadowPass(solidsEnt);
                            EntityCreationUtils.bypassVolumetics(solidsEnt);
                        }
                        solidsEnt.putData(EntityDataStrings.HAS_UNIQUE_MODEL, true);
                    } else {
                        LoggerInterface.loggerEngine.DEBUG("Finished generating block polygons; however, entity has already been deleted.");
                    }
                    if(notifyTarget != null){
                        notifyTarget.alertToGeneration();
                    }
                    if(toDelete != null){
                        for(Entity target : toDelete){
                            ClientEntityUtils.destroyEntity(target);
                        }
                    }
                } catch (Error e){
                    LoggerInterface.loggerEngine.ERROR(e);
                } catch(Exception e){
                    LoggerInterface.loggerEngine.ERROR(e);
                }
            });
        } else {
            if(notifyTarget != null){
                notifyTarget.alertToGeneration();
            }
            if(toDelete != null){
                for(Entity target : toDelete){
                    ClientEntityUtils.destroyEntity(target);
                }
            }
        }
        solidsEnt.putData(EntityDataStrings.TERRAIN_IS_TERRAIN, true);
        solidsEnt.putData(EntityDataStrings.BLOCK_ENTITY, true);
        rVal.add(solidsEnt);

        //
        //Create the entity for rendering solid blocks
        Entity transparentEnt = EntityCreationUtils.createClientSpatialEntity();
        if(hasPolygons && chunkData.getType() != null && chunkData.getMetadata() != null){
            generationService.submit(() -> {
                BlockMeshData data;
                try {
                    data = BlockMeshgen.rasterize(chunkData, false, solidsMap, (int)Math.pow(2,levelOfDetail));
                    if(Globals.clientState.clientScene.containsEntity(transparentEnt) && data.getFaceElements().length > 0){
                        String modelPath = Globals.assetManager.queuedAsset(new QueuedModel(() -> {
                            return BlockMeshgen.generateBlockModel(data);
                        }));
                        EntityCreationUtils.makeEntityDrawablePreexistingModel(transparentEnt, modelPath);
                        if(levelOfDetail == BlockChunkData.LOD_FULL_RES){
                            PhysicsEntityUtils.clientAttachMultiShapeTriGeomCollider(transparentEnt, data);
                            CollisionObjUtils.clientPositionCharacter(transparentEnt, new Vector3d(EntityUtils.getPosition(transparentEnt)), new Quaterniond());
                        } else {
                            EntityCreationUtils.bypassShadowPass(transparentEnt);
                            EntityCreationUtils.bypassVolumetics(transparentEnt);
                        }
                        transparentEnt.putData(EntityDataStrings.HAS_UNIQUE_MODEL, true);
                        DrawableUtils.makeEntityTransparent(transparentEnt);
                    } else {
                        LoggerInterface.loggerEngine.DEBUG("Finished generating block polygons; however, entity has already been deleted.");
                    }
                    if(notifyTarget != null){
                        notifyTarget.alertToGeneration();
                    }
                    if(toDelete != null){
                        for(Entity target : toDelete){
                            ClientEntityUtils.destroyEntity(target);
                        }
                    }
                } catch (Error e){
                    LoggerInterface.loggerEngine.ERROR(e);
                } catch(Exception e){
                    LoggerInterface.loggerEngine.ERROR(e);
                }
            });
        } else {
            if(notifyTarget != null){
                notifyTarget.alertToGeneration();
            }
            if(toDelete != null){
                for(Entity target : toDelete){
                    ClientEntityUtils.destroyEntity(target);
                }
            }
        }
        transparentEnt.putData(EntityDataStrings.TERRAIN_IS_TERRAIN, true);
        transparentEnt.putData(EntityDataStrings.BLOCK_ENTITY, true);
        rVal.add(transparentEnt);



        Globals.profiler.endCpuSample();
        return rVal;
    }

    /**
     * Creates a block chunk entity on the server
     * @param entity The entity to populate
     * @param weights The weights for the block chunk
     * @param values The values of each voxel in the chunk
     * @return The block entity
     */
    public static void serverCreateBlockChunkEntity(Entity entity, BlockMeshData blockChunkData){
        if(blockChunkData.getVertices().length > 0){
            PhysicsEntityUtils.serverAttachTriGeomCollider(entity, blockChunkData);
            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            DGeom terrainCollider = PhysicsEntityUtils.getDGeom(entity);
            Vector3d entityPos = EntityUtils.getPosition(entity);
            Quaterniond entityRot = EntityUtils.getRotation(entity);
            PhysicsUtils.setGeomTransform(realm.getCollisionEngine(), entityPos, entityRot, terrainCollider);
            entity.putData(EntityDataStrings.TERRAIN_IS_TERRAIN, true);
            entity.putData(EntityDataStrings.BLOCK_ENTITY, true);
            CommonEntityUtils.setEntityType(entity, EntityType.ENGINE);
        }
    }

    /**
     * Halts all running generation threads
     */
    public static void haltThreads(){
        generationService.shutdownNow();
    }

}
