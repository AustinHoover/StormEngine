package electrosphere.server.physics.chemistry;

import org.joml.Vector3d;
import org.ode4j.ode.DContactGeom;
import org.ode4j.ode.DGeom;

import electrosphere.collision.CollisionEngine.CollisionResolutionCallback;
import electrosphere.collision.collidable.Collidable;
import electrosphere.entity.Entity;

/**
 * The collision callback for the chemistry system
 */
public class ServerChemistryCollisionCallback implements CollisionResolutionCallback {

    @Override
    public void resolve(
            DContactGeom contactGeom,
            DGeom geom1,
            DGeom geom2,
            Collidable impactor,
            Collidable receiver,
            Vector3d normal,
            Vector3d localPosition,
            Vector3d worldPos,
            float magnitude
        ) {
        Entity impactorEntity = impactor.getParent();
        Entity receiverEntity = receiver.getParent();

        //basic error checking
        if(impactorEntity == null){
            throw new IllegalStateException("Impactor's entity is null");
        }
        if(receiverEntity == null){
            throw new IllegalStateException("Receiver's entity is null");
        }

        
    }
    
}
