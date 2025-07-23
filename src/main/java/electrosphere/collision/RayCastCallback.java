package electrosphere.collision;

import static org.ode4j.ode.OdeHelper.areConnectedExcluding;

import java.util.List;
import java.util.Map;

import org.joml.Vector3d;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DContactJoint;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DRay;
import org.ode4j.ode.OdeHelper;

import electrosphere.collision.collidable.Collidable;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.logger.LoggerInterface;

public class RayCastCallback implements DNearCallback {

    /**
     * Maximum number of contacts allowed
     */
    static final int MAX_CONTACTS = 2;

    /**
     * Really far away from the ray origin point
     */
    static final double REALLY_LONG_DISTANCE = 1000000;

    /**
     * // Check ray collision against a space
void RayCallback(void *Data, dGeomID Geometry1, dGeomID Geometry2) {
    dReal *HitPosition = (dReal *)Data;

    // Check collisions
    dContact Contacts[MAX_CONTACTS];
    int Count = dCollide(Geometry1, Geometry2, MAX_CONTACTS, &Contacts[0].geom, sizeof(dContact));
    for(int i = 0; i < Count; i++) {

        // Check depth against current closest hit
        if(Contacts[i].geom.depth < HitPosition[3]) {
            dCopyVector3(HitPosition, Contacts[i].geom.pos);
            HitPosition[3] = Contacts[i].geom.depth;
        }
    }
}
     */

    //For the current execution, this stores the shortest length that has currently been encountered.
    //This is used to keep track of the closest body so that there doesn't need to be contact join creation.
    //This should be reset every time a ray cast is called in collision engine by calling setLength in this object.
    double shortestLength = REALLY_LONG_DISTANCE;

    /**
     * The contact buffer
     */
    DContactBuffer contacts = new DContactBuffer(MAX_CONTACTS);

    @Override
    public void call(Object data, DGeom o1, DGeom o2) {
        RayCastCallbackData rayCastData = (RayCastCallbackData)data;

        //Don't self-collide
        if(o1 == o2){
            return;
        }

        //null out potentially previous results
        // rayCastData.collisionPosition = null;
        // rayCastData.collidedEntity = null;
		// exit without doing anything if the two bodies are connected by a joint
		DBody b1 = o1.getBody();
		DBody b2 = o2.getBody();
		if(b1 != null && b2 != null && areConnectedExcluding(b1,b2,DContactJoint.class)){
            return;
        }

        Collidable collidable1 = rayCastData.bodyEntityMap.get(b1);
        Collidable collidable2 = rayCastData.bodyEntityMap.get(b2);

        if(collidable1 == null){
            collidable1 = rayCastData.geomEntityMap.get(o1);
        }
        if(collidable2 == null){
            collidable2 = rayCastData.geomEntityMap.get(o2);
        }
        
        //don't self cast -- should work on both server and client
        if(collidable1 != null && collidable1.getParent() == Globals.clientState.playerEntity){
            return;
        }
        if(collidable2 != null && collidable2.getParent() == Globals.clientState.playerEntity){
            return;
        }
        //don't collide with entities that are attached to the parent either
        if(collidable1 != null && AttachUtils.getParent(collidable1.getParent()) != null && AttachUtils.getParent(collidable1.getParent()) == Globals.clientState.playerEntity){
            return;
        }
        if(collidable2 != null && AttachUtils.getParent(collidable2.getParent()) != null && AttachUtils.getParent(collidable2.getParent()) == Globals.clientState.playerEntity){
            return;
        }

        Globals.profiler.beginAggregateCpuSample("RayCastCallback - try collisions");
        if(
            rayCastData.collidableTypeMask == null ||
            (o1 instanceof DRay && collidable2 != null && rayCastData.collidableTypeMask.contains(collidable2.getType())) ||
            (o2 instanceof DRay && collidable1 != null && rayCastData.collidableTypeMask.contains(collidable1.getType()))
        ){
            //reset contact buffer
            contacts.nullify();
            //calculate collisions
            int numc = OdeHelper.collide(o1,o2,MAX_CONTACTS,contacts.getGeomBuffer());
            //create DContacts based on each collision that occurs
            if(numc != 0){
                for(int i=0; i<numc; i++){
                    DContact contact = contacts.get(i);
                    double depth = contact.geom.depth;

                    //check if should be stored in ray cast data return
                    if(depth < shortestLength){
                        shortestLength = depth;
                        if(collidable1 != null){
                            rayCastData.collidedEntity = collidable1.getParent();
                            rayCastData.collisionPosition = new Vector3d(contact.geom.pos.get0(),contact.geom.pos.get1(),contact.geom.pos.get2());
                        } else if(collidable2 != null) {
                            rayCastData.collidedEntity = collidable2.getParent();
                            rayCastData.collisionPosition = new Vector3d(contact.geom.pos.get0(),contact.geom.pos.get1(),contact.geom.pos.get2());
                        } else if(rayCastData.collidableTypeMask == null){
                            rayCastData.collisionPosition = new Vector3d(contact.geom.pos.get0(),contact.geom.pos.get1(),contact.geom.pos.get2());
                        } else {
                            LoggerInterface.loggerEngine.ERROR(new Error("Collided with entity that is not defined in the rayCastData.bodyEntityMap! \"" + collidable1 + "\",\"" + collidable2 + "\""));
                        }
                    }
                }
                if(rayCastData.collisionPosition == null){
                    String errorMessage = "Collision error!\n" + 
                    "body1: " + b1 + "\n" +
                    "body2: " + b2 + "\n" +
                    "collidable1: " + collidable1 + "\n" +
                    "collidable2: " + collidable2 + "\n";
                    LoggerInterface.loggerEngine.ERROR(new Error(errorMessage));
                }
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Sets the length that the ray should travel
     * @param length The length
     */
    protected void setLength(double length){
        this.shortestLength = length + 0.1;
    }

    /**
     * Data object that contains the information for a ray cast check
     */
    static class RayCastCallbackData {

        /**
         * The map of ode DBody -> collidable
         */
        Map<DBody,Collidable> bodyEntityMap;

        /**
         * The map of ode DGeom -> collidable
         */
        Map<DGeom,Collidable> geomEntityMap;

        /**
         * The mask of collidable types to filter collisions by. Can be null.
         */
        List<String> collidableTypeMask;

        /**
         * The entity that the ray cast collided with. If null, no entity was collided with.
         */
        Entity collidedEntity = null;

        /**
         * The position in world space that the collision happened
         */
        Vector3d collisionPosition = null;

        /**
         * Constructor
         * @param bodyEntityMap The map of ode DBody -> collidable
         * @param geomEntityMap The map of ode DGeom -> collidable
         * @param collidableTypeMask The mask of collidable types to filter collisions by. Can be null.
         */
        public RayCastCallbackData(Map<DBody,Collidable> bodyEntityMap, Map<DGeom,Collidable> geomEntityMap, List<String> collidableTypeMask){
            this.bodyEntityMap = bodyEntityMap;
            this.geomEntityMap = geomEntityMap;
            this.collidableTypeMask = collidableTypeMask;
        }
    }
    
}
