package electrosphere.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

import electrosphere.data.entity.graphics.NonproceduralModel;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.queue.QueuedModel;
import electrosphere.entity.state.idle.ClientIdleTree;
import electrosphere.renderer.RenderUtils;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.ActorUtils;
import electrosphere.renderer.model.Mesh;

/**
 * Utilities to manipulating drawable entities (eg making an entity transparent)
 */
public class DrawableUtils {
    
    /**
     * Edits entity data to make the entity transparent
     * @param entity The entity to edit
     */
    public static void makeEntityTransparent(Entity entity){
        entity.putData(EntityDataStrings.DRAW_TRANSPARENT_PASS, true);
        entity.removeData(EntityDataStrings.DRAW_SOLID_PASS);
    }

    /**
     * Disables culling for the actor on a given entity
     * @param entity The entity
     */
    public static void disableCulling(Entity entity){
        Actor actor = EntityUtils.getActor(entity);
        if(actor != null){
            actor.setFrustumCull(false);
        }
    }

    /**
     * Checks if the entity has a unique model
     * @param entity The entity
     * @return true if it has a unique model, false otherwise
     */
    public static boolean hasUniqueModel(Entity entity){
        return entity.containsKey(EntityDataStrings.HAS_UNIQUE_MODEL);
    }

    /**
     * Applies non-procedural model data to the entity
     * @param entity The entity
     * @param modelData The model data
     */
    public static void applyNonproceduralModel(Entity entity, NonproceduralModel modelData){
        if(modelData == null){
            throw new Error("Null model data!");
        }
        if(modelData.getPath() == null){
            throw new Error("Path undefined!");
        }
        //make the entity drawable
        EntityCreationUtils.makeEntityDrawable(entity, modelData.getPath());
        Actor creatureActor = EntityUtils.getActor(entity);

        //add low-res model path to actor if present
        if(modelData.getLODPath() != null){
            creatureActor.setLowResBaseModelPath(modelData.getLODPath());
            Globals.assetManager.addModelPathToQueue(modelData.getLODPath());
        }


        //idle tree & generic stuff all entities have
        if(modelData.getIdleData() != null){
            ClientIdleTree.attachTree(entity, modelData.getIdleData());
        }

        //apply uniforms
        if(modelData.getUniforms() != null){
            Map<String,Map<String,Object>> meshUniformMap = modelData.getUniforms();
            Set<String> meshNames = meshUniformMap.keySet();
            for(String meshName : meshNames){
                Map<String,Object> uniforms = meshUniformMap.get(meshName);
                Set<String> uniformNames = uniforms.keySet();
                for(String uniformName : uniformNames){
                    Object value = uniforms.get(uniformName);
                    creatureActor.setUniformOnMesh(meshName, uniformName, value);
                }
            }
        }

        //apply mesh color map
        if(modelData.getMeshColorMap() != null){
            Map<String,Vector3f> meshColorMap = modelData.getMeshColorMap();
            for(Entry<String,Vector3f> entry : meshColorMap.entrySet()){
                creatureActor.setUniformOnMesh(entry.getKey(), "color", entry.getValue());
            }
        }
    }

    /**
     * Makes an entity drawable with a mesh to be generated via a callback
     * @param entity The entity
     * @param meshGenerator The callback to generate a mesh
     */
    public static void makeEntityDrawable(Entity entity, Callable<Mesh> meshGenerator){
        QueuedModel model = new QueuedModel(() -> {
            Mesh mesh = meshGenerator.call();
            return RenderUtils.wrapMeshInModel(mesh);
        });
        String path = Globals.assetManager.queuedAsset(model);
        entity.putData(EntityDataStrings.DATA_STRING_ACTOR, ActorUtils.createActorOfLoadingModel(path));
        if(!entity.containsKey(EntityDataStrings.DATA_STRING_POSITION)){
            entity.putData(EntityDataStrings.DATA_STRING_POSITION, new Vector3d(0,0,0));
        }
        entity.putData(EntityDataStrings.DATA_STRING_ROTATION, new Quaterniond().identity());
        entity.putData(EntityDataStrings.DATA_STRING_SCALE, new Vector3f(1,1,1));
        entity.putData(EntityDataStrings.DATA_STRING_DRAW, true);
        entity.putData(EntityDataStrings.DRAW_SOLID_PASS, true);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAWABLE);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_VOLUMETIC_SOLIDS_PASS);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_CAST_SHADOW);
    }

    /**
     * Makes an entity drawable with a model at a given path
     * @param entity The entity
     * @param path The path to the model
     */
    public static void makeEntityDrawable(Entity entity, String path){
        entity.putData(EntityDataStrings.DATA_STRING_ACTOR, ActorUtils.createActorFromModelPath(path));
        if(!entity.containsKey(EntityDataStrings.DATA_STRING_POSITION)){
            entity.putData(EntityDataStrings.DATA_STRING_POSITION, new Vector3d(0,0,0));
        }
        entity.putData(EntityDataStrings.DATA_STRING_ROTATION, new Quaterniond().identity());
        entity.putData(EntityDataStrings.DATA_STRING_SCALE, new Vector3f(1,1,1));
        entity.putData(EntityDataStrings.DATA_STRING_DRAW, true);
        entity.putData(EntityDataStrings.DRAW_SOLID_PASS, true);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAWABLE);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_VOLUMETIC_SOLIDS_PASS);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_CAST_SHADOW);
    }

}
