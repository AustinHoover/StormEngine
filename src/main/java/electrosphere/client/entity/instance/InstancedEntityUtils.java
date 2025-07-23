package electrosphere.client.entity.instance;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.renderer.actor.instance.InstancedActor;
import electrosphere.renderer.buffer.ShaderAttribute;

/**
 * Utilities for working with entities that are instanced models
 */
public class InstancedEntityUtils {
    
    /**
     * Makes an already created entity a drawable, instanced entity (client only) by backing it with an InstancedActor
     * @param entity The entity
     * @param template The instance template to create the entity off of
     * @return The instanced actor that is attached to this entity
     */
    public static InstancedActor makeEntityInstanced(Entity entity, InstanceTemplate template){
        InstancedActor instancedActor = null;
        if(template.attributes != null){
            instancedActor = Globals.clientInstanceManager.createInstancedActor(
                template.modelPath,
                template.vertexPath,
                template.fragmentPath,
                template.attributes,
                template.capacity
            );
        } else if(template.stridedInstanceData != null) {
            instancedActor = Globals.clientInstanceManager.createInstancedActor(
                template.modelPath,
                template.vertexPath,
                template.fragmentPath,
                template.stridedInstanceData,
                template.capacity
            );
        }
        entity.putData(EntityDataStrings.INSTANCED_ACTOR, instancedActor);
        entity.putData(EntityDataStrings.DATA_STRING_POSITION, new Vector3d(0,0,0));
        entity.putData(EntityDataStrings.DATA_STRING_ROTATION, new Quaterniond().identity());
        entity.putData(EntityDataStrings.DATA_STRING_SCALE, new Vector3f(1,1,1));
        entity.putData(EntityDataStrings.DRAW_SOLID_PASS, true);
        entity.putData(EntityDataStrings.DATA_STRING_DRAW, true);
        Globals.clientState.clientScene.registerEntity(entity);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_INSTANCED);
        return instancedActor;
    }

    /**
     * Makes an already created entity a drawable, instanced entity (client only) by backing it with an InstancedActor
     * This variation also specifies a shader attribute that represents the model transform of the instance
     * The model transform is used to constantly automatically calculate the position of the mesh
     * @param entity The entity
     * @param template The instance template to create the entity off of
     * @param modelTransformAttribute The shader attribute that is the model transform
     * @return The instanced actor that is attached to this entity
     */
    public static InstancedActor makeEntityInstancedWithModelTransform(Entity entity, InstanceTemplate template, ShaderAttribute modelTransformAttribute){
        InstancedActor instancedActor = null;
        if(template.attributes != null){
            instancedActor = Globals.clientInstanceManager.createInstancedActor(
                template.modelPath,
                template.vertexPath,
                template.fragmentPath,
                template.attributes,
                template.capacity
            );
        } else if(template.stridedInstanceData != null) {
            instancedActor = Globals.clientInstanceManager.createInstancedActor(
                template.modelPath,
                template.vertexPath,
                template.fragmentPath,
                template.stridedInstanceData,
                template.capacity
            );
        }
        entity.putData(EntityDataStrings.INSTANCED_ACTOR, instancedActor);
        entity.putData(EntityDataStrings.DATA_STRING_POSITION, new Vector3d(0,0,0));
        entity.putData(EntityDataStrings.DATA_STRING_ROTATION, new Quaterniond().identity());
        entity.putData(EntityDataStrings.DATA_STRING_SCALE, new Vector3f(1,1,1));
        entity.putData(EntityDataStrings.DRAW_SOLID_PASS, true);
        entity.putData(EntityDataStrings.DATA_STRING_DRAW, true);
        entity.putData(EntityDataStrings.INSTANCED_MODEL_ATTRIBUTE, modelTransformAttribute);
        Globals.clientState.clientScene.registerEntity(entity);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_INSTANCED);
        return instancedActor;
    }

}
