package electrosphere.util.math;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.renderer.actor.Actor;

/**
 * Math functions related to bones
 */
public class MathBones {



    /**
     * Gets the world position of a bone
     * @param actorEntity The entity that has a bone
     * @param boneName The name of the bone
     */
    public static Vector3d getBoneWorldPosition(Entity actorEntity, String boneName){
        Actor actor = EntityUtils.getActor(actorEntity);
        Vector3d localPos = new Vector3d(actor.getAnimationData().getBonePosition(boneName));

        //transform bone space
        Vector3d position = new Vector3d(localPos);
        position = position.mul(EntityUtils.getScale(actorEntity));
        position = position.rotate(new Quaterniond(EntityUtils.getRotation(actorEntity)));
        //transform worldspace
        position.add(new Vector3d(EntityUtils.getPosition(actorEntity)));
        return position;
    }

    /**
     * Gets the global rotation of the bone
     * @param actorEntity The entity with the bone
     * @param boneName The name of the bone
     * @return The global rotation of the bone
     */
    public static Quaterniond getBoneWorldRotation(Entity actorEntity, String boneName){
        Actor actor = EntityUtils.getActor(actorEntity);
        Quaterniond localRot = actor.getAnimationData().getBoneRotation(boneName);

        Vector3d facingAngle = CreatureUtils.getFacingVector(actorEntity);
        if(facingAngle == null){
            facingAngle = SpatialMathUtils.getOriginVector();
        }
        //calculate rotation of model
        return new Quaterniond()
            .rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(facingAngle.x,facingAngle.y,facingAngle.z))
            .mul(localRot)
            .normalize();
    }
    
    
}
