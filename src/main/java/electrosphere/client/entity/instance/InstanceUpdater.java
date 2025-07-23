package electrosphere.client.entity.instance;

import java.util.Set;

import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.renderer.actor.instance.InstancedActor;

/**
 * Used for updating instance priority
 */
public class InstanceUpdater {
    
    /**
     * Updates all instanced actors to have priority based on distance from camera
     */
    public static void updateInstancedActorPriority(){
        Globals.profiler.beginCpuSample("updateInstancedActorPriority");
        if(Globals.clientState.playerCamera != null){
            Vector3d eyePosition = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
            Set<Entity> instancedEntities = Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_INSTANCED);
            for(Entity entity : instancedEntities){
                //set priority equal to distance
                Vector3d entityPosition = EntityUtils.getPosition(entity);
                int priority = (int)entityPosition.distance(eyePosition);
                InstancedActor.getInstancedActor(entity).setPriority(priority);
            }
        }
        Globals.profiler.endCpuSample();
    }

}
