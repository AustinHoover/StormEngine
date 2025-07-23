package electrosphere.renderer.target;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joml.Matrix4d;
import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.terrain.TerrainChunk;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.pipelines.MainContentPipeline;
import electrosphere.renderer.pipelines.NormalsForOutlinePipeline;
import electrosphere.renderer.pipelines.ShadowMapPipeline;

/**
 * Evaluates the draw targets
 */
public class DrawTargetEvaluator {


    /**
     * Cutoff after which we start using LOD models
     */
    public static final int LOD_STATIC_CUTOFF = 50;

    /**
     * Cutoff after which we draw lower resolution models
     */
    public static final int LOD_LOWER_CUTOFF = 30;

    /**
     * Set for storing entities of a specific tag
     */
    private static final HashSet<Entity> drawableSet = new HashSet<Entity>();

    /**
     * Set for storing entities of a specific tag
     */
    private static final HashSet<Entity> shadowSet = new HashSet<Entity>();
    
    /**
     * Evaluates the draw targets
     */
    public static void evaluate(){
        Globals.profiler.beginCpuSample("DrawTargetEvaluator.evaluate");
        //main content pipeline structures
        DrawTargetAccumulator mainAccumulator = Globals.renderingEngine.getMainContentPipeline().getDrawTargetAccumulator();
        List<Entity> mainQueue = Globals.renderingEngine.getMainContentPipeline().getStandardEntityQueue();
        mainAccumulator.clearCalls();
        mainQueue.clear();

        //shadow map pipeline structures
        DrawTargetAccumulator shadowAccumulator = Globals.renderingEngine.getShadowMapPipeline().getDrawTargetAccumulator();
        List<Entity> shadowQueue = Globals.renderingEngine.getShadowMapPipeline().getStandardEntityQueue();
        shadowAccumulator.clearCalls();
        shadowQueue.clear();
        
        //normals pipeline structures
        DrawTargetAccumulator normalAccumulator = Globals.renderingEngine.getNormalsForOutlinePipeline().getDrawTargetAccumulator();
        List<Entity> normalQueue = Globals.renderingEngine.getNormalsForOutlinePipeline().getStandardEntityQueue();
        normalAccumulator.clearCalls();
        normalQueue.clear();

        //reused objects
        Vector3d posVec = new Vector3d();
        Matrix4d modelTransformMatrix = new Matrix4d();


        //calculate camera-modified vector3d
        Vector3d cameraCenter = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        Vector3d positionVec = new Vector3d();

        //different entity lists
        Set<Entity> drawables = Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAWABLE, drawableSet);
        Set<Entity> shadowList = Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_CAST_SHADOW, shadowSet);

        for(Entity currentEntity : drawables){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            //fetch actor
            Actor currentActor = EntityUtils.getActor(currentEntity);

            //get distance from camera
            posVec.set(position);
            double dist = posVec.distance(cameraCenter);
            
            //evaluate LOD level
            if(currentActor.getLowResBaseModelPath() != null){
                if(dist < LOD_LOWER_CUTOFF){
                    currentActor.setLodLevel(Actor.LOD_LEVEL_FULL);
                } else if(dist < LOD_STATIC_CUTOFF) {
                    currentActor.setLodLevel(Actor.LOD_LEVEL_LOWER);
                } else {
                    currentActor.setLodLevel(Actor.LOD_LEVEL_STATIC);
                }
            }

            //queue calls accordingly
            if(!DrawableUtils.hasUniqueModel(currentEntity) && currentActor.isStaticDrawCall()){
                //calculate camera-modified vector3d
                Vector3d cameraModifiedPosition = positionVec.set(position).sub(cameraCenter);
                //calculate and apply model transform
                modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                modelTransformMatrix.scale(positionVec.set(EntityUtils.getScale(currentEntity)));
                currentActor.applySpatialData(modelTransformMatrix,position);
                //draw
                if(currentActor.frustumTest(Globals.renderingEngine.getRenderPipelineState(), modelTransformMatrix.getTranslation(posVec))){
                    //queue for batching
                    if(MainContentPipeline.shouldDrawSolidPass(currentEntity)){
                        mainAccumulator.addCall(currentActor.getBaseModelPath(), position, modelTransformMatrix);
                    }
                    if(dist < ShadowMapPipeline.DRAW_CUTOFF_DIST && shadowList.contains(currentEntity)){
                        shadowAccumulator.addCall(currentActor.getBaseModelPath(), position, modelTransformMatrix);
                    }
                    if(dist < NormalsForOutlinePipeline.DRAW_CUTOFF_DIST && NormalsForOutlinePipeline.shouldDraw(currentEntity)){
                        normalAccumulator.addCall(currentActor.getBaseModelPath(), position, modelTransformMatrix);
                    }
                }
            } else if(TerrainChunk.isBlockEntity(currentEntity)){
                if(MainContentPipeline.shouldDrawSolidPass(currentEntity)){
                    mainAccumulator.addBlockCall(currentEntity);
                }
                if(dist < ShadowMapPipeline.DRAW_CUTOFF_DIST && shadowList.contains(currentEntity)){
                    shadowAccumulator.addBlockCall(currentEntity);
                }
                if(dist < NormalsForOutlinePipeline.DRAW_CUTOFF_DIST && NormalsForOutlinePipeline.shouldDraw(currentEntity)){
                    normalAccumulator.addBlockCall(currentEntity);
                }
            } else if(TerrainChunk.isTerrainEntity(currentEntity)) {
                if(MainContentPipeline.shouldDrawSolidPass(currentEntity)){
                    mainAccumulator.addTerrainCall(currentEntity);
                }
                if(dist < ShadowMapPipeline.DRAW_CUTOFF_DIST && shadowList.contains(currentEntity)){
                    shadowAccumulator.addTerrainCall(currentEntity);
                }
                if(dist < NormalsForOutlinePipeline.DRAW_CUTOFF_DIST && NormalsForOutlinePipeline.shouldDraw(currentEntity)){
                    normalAccumulator.addTerrainCall(currentEntity);
                }
            } else {
                if(MainContentPipeline.shouldDrawSolidPass(currentEntity)){
                    mainQueue.add(currentEntity);
                }
                if(dist < ShadowMapPipeline.DRAW_CUTOFF_DIST && shadowList.contains(currentEntity)){
                    shadowQueue.add(currentEntity);
                }
                if(dist < NormalsForOutlinePipeline.DRAW_CUTOFF_DIST && NormalsForOutlinePipeline.shouldDraw(currentEntity)){
                    normalQueue.add(currentEntity);
                }
            }
        }


        // //iterate over all shadow cast entities
        // for(Entity currentEntity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_CAST_SHADOW)){
        //     if(currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)!=null){
        //         Vector3d position = EntityUtils.getPosition(currentEntity);
        //         //fetch actor
        //         Actor currentActor = EntityUtils.getActor(currentEntity);
        //         if(currentActor.isStaticDrawCall()){
        //             Vector3d cameraModifiedPosition = posVec.set(position).sub(cameraCenter);
        //             //calculate and apply model transform
        //             modelTransformMatrix = modelTransformMatrix.identity();
        //             modelTransformMatrix.translate(cameraModifiedPosition);
        //             modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
        //             modelTransformMatrix.scale(scaleVec.set(EntityUtils.getScale(currentEntity)));
        //             posVec.set(0,0,0);
        //             if(currentActor.frustumTest(Globals.renderingEngine.getRenderPipelineState(), modelTransformMatrix.getTranslation(posVec))){
        //                 //queue for batching
        //                 shadowAccumulator.addCall(currentActor.getModelPath(), position, modelTransformMatrix);
        //             }
        //         } else {
        //             //queue for not-batching
        //             shadowQueue.add(currentEntity);
        //         }
        //     }
        // }

        Globals.profiler.endCpuSample();
    }

}
