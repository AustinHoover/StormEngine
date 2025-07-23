package electrosphere.collision;

import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactJoint;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DRay;
import org.ode4j.ode.OdeHelper;

import electrosphere.collision.collidable.Collidable;
import electrosphere.collision.collidable.SurfaceParams;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;

/**
 * Near callback for main physics engine
 */
public class PhysicsCallback implements DNearCallback {

    /**
     * The collision engine that will invoke this callback
     */
    protected CollisionEngine engine;

    /**
     * Enables geom-geom collisions between non-statics
     */
    private boolean enableGeomGeom = false;

    /**
     * Constructor
     */
    public PhysicsCallback(){
    }

    @Override
    public void call(Object data, DGeom o1, DGeom o2) {
        if(engine.name.equals("serverPhysics")){
            engine.nearCollisionCount++;
        }

        //ie if both are in static space
        if(o1 == o2){
            return;
        }

        //if neither are bodies
        if(
            o1.getBody() == null && o2.getBody() == null &&
            (
                !this.enableGeomGeom ||
                (o1.getCategoryBits() == Collidable.TYPE_STATIC_BIT && o2.getCategoryBits() == Collidable.TYPE_STATIC_BIT)
            )
        ){
            return;
        }

		// exit without doing anything if the two bodies are connected by a joint
		DBody b1 = o1.getBody();
		DBody b2 = o2.getBody();
		if(b1 != null && b2 != null && OdeHelper.areConnectedExcluding(b1,b2,DContactJoint.class)){
            return;
        }

        //get the collidables for each geom
        Collidable c1 = null;
        if(b1 != null){
            c1 = engine.bodyPointerMap.get(b1);
        } else if(o1.getBody() == null) {
            c1 = engine.geomPointerMap.get(o1);
        }
        Collidable c2 = null;
        if(b2 != null){
            c2 = engine.bodyPointerMap.get(b2);
        } else if(o2.getBody() == null) {
            c2 = engine.geomPointerMap.get(o2);
        }

        //make sure we have collidables for both
        if(c1 == null || c2 == null){
            String message = "Collidable is undefined!\n" +
            "Geoms:\n" +
            o1 + " \n" +
            o2 + " \n" +
            "Bodies:\n" +
            b1 + " \n" +
            b2 + " \n" +
            "Colliders:\n" +
            c1 + " \n" +
            c2 + " \n" +
            "Obj 1 pointers:\n" +
            engine.bodyPointerMap.get(b1) + " \n" +
            engine.geomPointerMap.get(o1) + " \n" +
            "Obj 2 pointers:\n" +
            engine.bodyPointerMap.get(b2) + " \n" +
            engine.geomPointerMap.get(o2) + " \n" +
            ""
            ;
            throw new Error(message);
        }

        //Controls whether we should grab MAX_CONTACTS or MIN_CONTACTS
        boolean isGeomGeomCollision = o1.getBody() == null && o2.getBody() == null;

        //Number of contacts to poll for
        int contactCount = CollisionEngine.MAX_CONTACTS;
        if(isGeomGeomCollision && this.enableGeomGeom){
            contactCount = CollisionEngine.MIN_CONTACTS;
        }


        Globals.profiler.beginAggregateCpuSample("CollisionEngine.nearCallback - Full collision phase");
        try {
            Globals.profiler.beginAggregateCpuSample("CollisionEngine.nearCallback - setup");
            //null out the contact buffer
            engine.contacts.nullify();
            SurfaceParams surfaceParams1 = c1.getSurfaceParams();
            SurfaceParams surfaceParams2 = null;
            if(c2 != null){
                surfaceParams2 = c2.getSurfaceParams();
            }
            for (int i=0; i< contactCount; i++) {
                DContact contact = engine.contacts.get(i);
                contact.surface.mode = surfaceParams1.getMode();
                contact.surface.mu = surfaceParams1.getMu();
                if(surfaceParams1.getRho() != null){
                    contact.surface.rho = surfaceParams1.getRho();
                } else if(surfaceParams2 != null && surfaceParams2.getRho() != null){
                    contact.surface.rho = surfaceParams2.getRho();
                }
                if(surfaceParams1.getRho2() != null){
                    contact.surface.rho2 = surfaceParams1.getRho2();
                } else if(surfaceParams2 != null && surfaceParams2.getRho2() != null){
                    contact.surface.rho = surfaceParams2.getRho2();
                }
                if(surfaceParams1.getRhoN() != null){
                    contact.surface.rhoN = surfaceParams1.getRhoN();
                } else if(surfaceParams2 != null && surfaceParams2.getRhoN() != null){
                    contact.surface.rho = surfaceParams2.getRhoN();
                }
                if(surfaceParams1.getBounce() != null){
                    contact.surface.bounce = surfaceParams1.getBounce();
                } else if(surfaceParams2 != null && surfaceParams2.getBounce() != null){
                    contact.surface.rho = surfaceParams2.getBounce();
                }
                if(surfaceParams1.getBounceVel() != null){
                    contact.surface.bounce_vel = surfaceParams1.getBounceVel();
                } else if(surfaceParams2 != null && surfaceParams2.getBounceVel() != null){
                    contact.surface.rho = surfaceParams2.getBounceVel();
                }
                if(surfaceParams1.getSoftErp() != null){
                    contact.surface.soft_erp = surfaceParams1.getSoftErp();
                } else if(surfaceParams2 != null && surfaceParams2.getSoftErp() != null){
                    contact.surface.rho = surfaceParams2.getSoftErp();
                }
                if(surfaceParams1.getSoftCfm() != null){
                    contact.surface.soft_cfm = surfaceParams1.getSoftCfm();
                } else if(surfaceParams2 != null && surfaceParams2.getSoftCfm() != null){
                    contact.surface.rho = surfaceParams2.getSoftCfm();
                }
            }
            Globals.profiler.endCpuSample();
            //calculate collisions
            Globals.profiler.beginAggregateCpuSample("CollisionEngine.nearCallback - OdeHelper.collide");
            int numc = OdeHelper.collide(o1,o2,contactCount,engine.contacts.getGeomBuffer());
            Globals.profiler.endCpuSample();
            //create DContacts based on each collision that occurs
            Globals.profiler.beginAggregateCpuSample("CollisionEngine.nearCallback - contact iterations");
            if(numc != 0){
                for(int i=0; i<numc; i++){
                    DContact contact = engine.contacts.get(i);

                    //special code for ray casting
                    if (o1 instanceof DRay || o2 instanceof DRay){
                        DVector3 end = new DVector3();
                        end.eqSum(contact.geom.pos, contact.geom.normal, contact.geom.depth);
                        continue;
                    }

                    //
                    //add contact to contact group - don't create contacts for non-geom collisions
                    if(!isGeomGeomCollision){
                        DJoint c = OdeHelper.createContactJoint(engine.world,engine.contactgroup,contact);
                        if(b1 == null){
                            if(b2 == null){
                            } else {
                                c.attach(null,b2);
                            }
                        } else {
                            if(b2 == null){
                                c.attach(b1,null);
                            } else {
                                c.attach(b1,b2);
                            }
                        }
                    }

                    // Use the default collision resolution
                    CollisionEngine.resolveCollision(
                        contact.geom,
                        c1,
                        c2,
                        PhysicsUtils.odeVecToJomlVec(contact.geom.normal).mul(-1.0),
                        PhysicsUtils.odeVecToJomlVec(contact.fdir1).mul(-1.0),
                        PhysicsUtils.odeVecToJomlVec(contact.geom.pos),
                        (float)contact.geom.depth
                    );
                    CollisionEngine.resolveCollision(
                        contact.geom,
                        c2,
                        c1,
                        PhysicsUtils.odeVecToJomlVec(contact.geom.normal),
                        PhysicsUtils.odeVecToJomlVec(contact.fdir1),
                        PhysicsUtils.odeVecToJomlVec(contact.geom.pos),
                        (float)contact.geom.depth
                    );

                    //tracking updates
                    engine.finalCollisionCount++;
                }
            }
            Globals.profiler.endCpuSample();
        } catch(ArrayIndexOutOfBoundsException ex){
            //I've found that ode4j occasionally throws an exception on the OdeHelper.collide function.
            //I don't know why it has out of bounds elements, but it's happening.
            //Catching the exception here allows the engine to keep running at least.
            LoggerInterface.loggerEngine.ERROR("ode4j error", ex);
        }
        Globals.profiler.endCpuSample();
    }
    
}
