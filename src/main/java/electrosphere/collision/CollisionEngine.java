package electrosphere.collision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBhvSpace;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DCapsule;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DContactGeom;
import org.ode4j.ode.DContactJoint;
import org.ode4j.ode.DCylinder;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DRay;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DSphere;
import org.ode4j.ode.DTriMesh;
import org.ode4j.ode.DTriMeshData;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import electrosphere.collision.RayCastCallback.RayCastCallbackData;
import electrosphere.collision.collidable.Collidable;
import electrosphere.collision.collidable.SurfaceParams;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.engine.Globals;
import electrosphere.engine.time.Timekeeper;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.physicssync.ServerPhysicsSyncTree;
import electrosphere.logger.LoggerInterface;

/**
 * The main collision engine class. Tracks all entities that collide in its system and fires callbacks when they do.
 */
public class CollisionEngine {

    /**
     * gravity constant
     */
    public static final float GRAVITY_MAGNITUDE = 5f;

    /**
     * The damping applied to angular velocity
     */
    public static final double DEFAULT_ANGULAR_DAMPING = 0.01;

    /**
     * The damping applied to linear velocity
     */
    public static final double DEFAULT_LINEAR_DAMPING = 0.01;

    /**
     * Default max angular speed
     */
    public static final double DEFAULT_MAX_ANGULAR_SPEED = 100;

    /**
     * The number of times the physics engine should be simulated per frame of main game.
     * IE, if this value is 3, every main game engine frame, the physics simulation will run 3 frames.
     * This keeps the physics simulation much more stable than it would be otherwise.
     */
    public static final int PHYSICS_SIMULATION_RESOLUTION = 3;

    /**
     * Threshold after which the engine warns about collidable count
     */
    public static final int COLLIDABLE_COUNT_WARNING_THRESHOLD = 5000;

    /**
     * Quickstep iteration count
     */
    public static final int QUICKSTEP_ITERATION_COUNT = 50;

    /**
     * Default distance to interact with collidables at (ie picking where to place things)
     */
    public static final double DEFAULT_INTERACT_DISTANCE = 5.0;

    /**
     * Distance from current floating point origin to trigger a rebase
     */
    private static final double REBASE_TRIGGER_DISTANCE = 16;
    
    /**
     * world data that the collision engine leverages for position correction and the like
     */
    private CollisionWorldData collisionWorldData;

    /**
     * The world object
     */
    protected DWorld world;

    /**
     * The main space in the world
     */
    protected DBhvSpace space;

    /**
     * Lock for thread-safeing all ODE calls
     */
    private static ReentrantLock spaceLock = new ReentrantLock();

    /**
     * The contact group for caching collisions between collision and physics calls
     */
    protected DJointGroup contactgroup;

    /**
     * <p> Maximum number of contact points per body </p>
     * <p><b> Note: </b></p>
     * <p>
     * This value must be sufficiently high (I'd recommend 64), in order for large bodies to not sink into other large bodies.
     * I used a value of 10 for a long time and found that cubes were sinking into TriMeshes.
     * </p>
     */
    protected static final int MAX_CONTACTS = 64;

    /**
     * <p> Maximum number of contact points per geom-geom collision </p>
     * <p><b> Note: </b></p>
     */
    protected static final int MIN_CONTACTS = 4;

    /**
     * The list of dbodies ode should be tracking
     */
    protected List<DBody> bodies = new ArrayList<DBody>();

    /**
     * This is used to relate DBody's back to their collidables so that when the library detects a collision, the callback can know which collidables are involved.
     */
    protected Map<DBody,Collidable> bodyPointerMap = new HashMap<DBody,Collidable>();

    /**
     * This is used to relate DGeom's back to their collidables so that when the library detects a collision, the callback can know which collidables are involved.
     */
    protected Map<DGeom,Collidable> geomPointerMap = new HashMap<DGeom,Collidable>();

    /**
     * The list of all collidables the engine is currently tracking
     */
    protected List<Collidable> collidableList = new ArrayList<Collidable>();

    /**
     * Dynamic spatial offset applied to all operations on the space.
     * This is used for world origin rebasing.
     * This makes physics behave more consistently by moving far out objects to center around 0,0,0.
     */
    private Vector3d floatingOrigin = new Vector3d(0,0,0);

    /**
     * callbacks for collision check
     */
    private RayCastCallback rayCastCallback = new RayCastCallback();

    /**
     * the collision resolution callback
     */
    private CollisionResolutionCallback collisionResolutionCallback;

    /**
     * Callback for any near collisions in the broadphase of the collision check
     */
    private DNearCallback nearCallback;

    /**
     * Number of geometries
     */
    private int geomCount = 0;

    /**
     * The near collision count
     */
    protected int nearCollisionCount = 0;

    /**
     * The number of collisions that make it all the way to creating impulses
     */
    protected int finalCollisionCount = 0;

    /**
     * Tracks whether the engine has rebased or not
     */
    private boolean hasRebased = false;


    /**
     * buffer for storing potential collisions
     */
    protected DContactBuffer contacts = new DContactBuffer(MAX_CONTACTS);

    /**
     * The name of the collision engine
     */
    protected final String name;
    
    /**
     * Constructor
     */
    public CollisionEngine(String name){
        this.name = name;
        world = OdeHelper.createWorld();
        world.setGravity(0,-GRAVITY_MAGNITUDE,0);
        world.setQuickStepNumIterations(QUICKSTEP_ITERATION_COUNT);
        space = OdeHelper.createBHVSpace(Collidable.TYPE_STATIC_BIT);
        // world.setContactMaxCorrectingVel(0.1);
        // world.setContactSurfaceLayer(0.001);
        // world.setCFM(1e-10);

        contactgroup = OdeHelper.createJointGroup();
        this.nearCallback = new DNearCallback() {
            @Override
            public void call(Object data, DGeom o1, DGeom o2) {
                nearCallback( data, o1, o2);
            }
        };
    }

    /**
     * Creates a collision engine with a specified callback
     */
    public static CollisionEngine create(String name, CollisionResolutionCallback callback){
        CollisionEngine rVal = new CollisionEngine(name);
        rVal.setCollisionResolutionCallback(callback);
        return rVal;
    }

    /**
     * Creates a collision engine with a specified callback
     */
    public static CollisionEngine create(String name, PhysicsCallback callback){
        CollisionEngine rVal = new CollisionEngine(name);
        rVal.nearCallback = callback;
        callback.engine = rVal;
        return rVal;
    }


    /**
     * Resolves collisions in the engine
     * @param contactGeom the ode4j contact geometry
     * @param impactor the instigator of the collision
     * @param receiver the receiver of the collision
     * @param normal the normal to the collision surface
     * @param localPosition the local position of the collision
     * @param worldPos the world position of the collision
     * @param magnitude the magnitude of the collision
     */
    public static void resolveCollision(DContactGeom contactGeom, Collidable impactor, Collidable receiver, Vector3d normal, Vector3d localPosition, Vector3d worldPos, float magnitude){
        switch(receiver.getType()){
            case Collidable.TYPE_CREATURE: {
                switch(impactor.getType()){
                    case Collidable.TYPE_STATIC: {
                        receiver.addImpulse(normal, localPosition, worldPos, magnitude * 2, Collidable.TYPE_STATIC);
                    } break;
                    case Collidable.TYPE_CREATURE: {
                        receiver.addImpulse(normal, localPosition, worldPos, magnitude, Collidable.TYPE_CREATURE);
                    } break;
                    case Collidable.TYPE_OBJECT: {
                        receiver.addImpulse(normal, localPosition, worldPos, magnitude, Collidable.TYPE_OBJECT);
                    } break;
                }
            } break;
        }
    }
    
    /**
     * Clear collidable impulse list
     */
    public void clearCollidableImpulseLists(){
        spaceLock.lock();
        Globals.profiler.beginCpuSample("CollisionEngine.clearCollidableImpulseLists");
        for(Collidable collidable : collidableList){
            collidable.clear();
        }
        Globals.profiler.endCpuSample();
        spaceLock.unlock();
    }

    /**
     * Gets the list of collidables
     * @return The list of collidables
     */
    public List<Collidable> getCollidables(){
        spaceLock.lock();
        List<Collidable> rVal = Collections.unmodifiableList(this.collidableList);
        spaceLock.unlock();
        return rVal;
    }
    
    
    /**
     * 
     * @param e the entity that wants to move
     * @param positionToCheck the position that it wants to move to
     * @return true if it can occupy that position, false otherwise
     */
    public boolean checkCanOccupyPosition(CollisionWorldData w, Entity e, Vector3d positionToCheck){
        boolean rVal = true;
        //
        // check world bounds
        //
        if(
                positionToCheck.x < collisionWorldData.getWorldBoundMin().x ||
                positionToCheck.y < collisionWorldData.getWorldBoundMin().y ||
                positionToCheck.z < collisionWorldData.getWorldBoundMin().z ||
                positionToCheck.x > collisionWorldData.getWorldBoundMax().x ||
                positionToCheck.y > collisionWorldData.getWorldBoundMax().y ||
                positionToCheck.z > collisionWorldData.getWorldBoundMax().z
                ){
            return false;
        }
        return rVal;
    }
    
    /**
     * Performs the collision and simulation phases for this collision engine
     */
    public void simulatePhysics(){
        Globals.profiler.beginCpuSample("physics");
        spaceLock.lock();
        //reset tracking
        this.nearCollisionCount = 0;
        this.finalCollisionCount = 0;
        // remove all contact joints
        contactgroup.empty();
        //main simulation
        for(int i = 0; i < PHYSICS_SIMULATION_RESOLUTION; i++){
            Globals.profiler.beginCpuSample("collide");
            OdeHelper.spaceCollide(space, 0, nearCallback);
            Globals.profiler.endCpuSample();

            //simulate physics
            Globals.profiler.beginCpuSample("step physics");
            world.quickStep(Timekeeper.ENGINE_STEP_SIZE);
            Globals.profiler.endCpuSample();

            // remove all contact joints
            contactgroup.empty();
        }
        spaceLock.unlock();
        Globals.profiler.endCpuSample();
    }

    /**
     * Runs a collision cycle on all bodies in the collision engine
     */
    public void collide(){
        Globals.profiler.beginCpuSample("physics");
        spaceLock.lock();
        contactgroup.empty();
        Globals.profiler.beginCpuSample("collide");
        OdeHelper.spaceCollide(space, 0, nearCallback);
        Globals.profiler.endCpuSample();

		// remove all contact joints
		contactgroup.empty();
        spaceLock.unlock();
        Globals.profiler.endCpuSample();
    }

    /**
     * This is called by dSpaceCollide when two objects in space are potentially colliding.
     * @param data The data
     * @param o1 the first collision body
     * @param o2 the second collision body
     */
	private void nearCallback(Object data, DGeom o1, DGeom o2){
        if(this.name.equals("serverPhysics")){
            this.nearCollisionCount++;
        }
		// if (o1->body && o2->body) return;

        //ie if both are in static space
        if(o1 == o2){
            return;
        }

        //if the collision is static-on-static, skip
        if(o1.getCategoryBits() == Collidable.TYPE_STATIC_BIT && o2.getCategoryBits() == Collidable.TYPE_STATIC_BIT){
            return;
        }

		// exit without doing anything if the two bodies are connected by a joint
		DBody b1 = o1.getBody();
		DBody b2 = o2.getBody();
		if(b1 != null && b2 != null && OdeHelper.areConnectedExcluding(b1,b2,DContactJoint.class)){
            return;
        }

        //if collision is between static and disabled, skip
        if(o1.getCategoryBits() == Collidable.TYPE_STATIC_BIT && b2 != null && !b2.isEnabled()){
            return;
        }
        if(o2.getCategoryBits() == Collidable.TYPE_STATIC_BIT && b1 != null && !b1.isEnabled()){
            return;
        }

        //get the collidables for each geom
        Collidable c1 = null;
        if(b1 != null){
            c1 = bodyPointerMap.get(b1);
        } else if(o1.getBody() == null) {
            c1 = geomPointerMap.get(o1);
        }
        Collidable c2 = null;
        if(b2 != null){
            c2 = bodyPointerMap.get(b2);
        } else if(o2.getBody() == null) {
            c2 = geomPointerMap.get(o2);
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
            this.bodyPointerMap.get(b1) + " \n" +
            this.geomPointerMap.get(o1) + " \n" +
            "Obj 2 pointers:\n" +
            this.bodyPointerMap.get(b2) + " \n" +
            this.geomPointerMap.get(o2) + " \n" +
            ""
            ;
            throw new Error(message);
        }

        Globals.profiler.beginAggregateCpuSample("CollisionEngine.nearCallback - Full collision phase");
        try {
            Globals.profiler.beginAggregateCpuSample("CollisionEngine.nearCallback - setup");
            //null out the contact buffer
            contacts.nullify();
            SurfaceParams surfaceParams1 = c1.getSurfaceParams();
            SurfaceParams surfaceParams2 = null;
            if(c2 != null){
                surfaceParams2 = c2.getSurfaceParams();
            }
            for (int i=0; i<MAX_CONTACTS; i++) {
                DContact contact = contacts.get(i);
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
            int numc = OdeHelper.collide(o1,o2,MAX_CONTACTS,contacts.getGeomBuffer());
            Globals.profiler.endCpuSample();
            //create DContacts based on each collision that occurs
            Globals.profiler.beginAggregateCpuSample("CollisionEngine.nearCallback - contact iterations");
            if(numc != 0){
                for(int i=0; i<numc; i++){
                    DContact contact = contacts.get(i);


                    //special code for ray casting
                    if (o1 instanceof DRay || o2 instanceof DRay){
                        DVector3 end = new DVector3();
                        end.eqSum(contact.geom.pos, contact.geom.normal, contact.geom.depth);
                        continue;
                    }

                    //
                    //add contact to contact group
                    DJoint c = OdeHelper.createContactJoint(world,contactgroup,contact);
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

                    // Use the default collision resolution
                    if(collisionResolutionCallback == null) {
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
                    } else {
                        //use custom collision resolution
                        collisionResolutionCallback.resolve(
                            contact.geom,
                            o1,
                            o2,
                            c1,
                            c2,
                            PhysicsUtils.odeVecToJomlVec(contact.geom.normal).mul(-1.0),
                            PhysicsUtils.odeVecToJomlVec(contact.fdir1).mul(-1.0),
                            PhysicsUtils.odeVecToJomlVec(contact.geom.pos),
                            (float)contact.geom.depth
                        );
                        collisionResolutionCallback.resolve(
                            contact.geom,
                            o2,
                            o1,
                            c2,
                            c1,
                            PhysicsUtils.odeVecToJomlVec(contact.geom.normal),
                            PhysicsUtils.odeVecToJomlVec(contact.fdir1),
                            PhysicsUtils.odeVecToJomlVec(contact.geom.pos),
                            (float)contact.geom.depth
                        );
                    }

                    //tracking updates
                    this.finalCollisionCount++;
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

    /**
     * Sets the function that is called once a collision has happened
     * @param collisionResolutionCallback the function
     */
    public void setCollisionResolutionCallback(CollisionResolutionCallback collisionResolutionCallback){
        this.collisionResolutionCallback = collisionResolutionCallback;
    }
    
    /**
     * 
     * @param w The collision world data
     * @param positionToCheck the position the entity wants to be at
     * @return the position the engine recommends it move to instead (this is
     * guaranteed to be a valid position)
     */
    public Vector3d suggestMovementPosition(CollisionWorldData w, Vector3d positionToCheck){
        Vector3d suggestedPosition = new Vector3d(positionToCheck);
        //
        // adjust for world bounds
        //
        if(suggestedPosition.x < collisionWorldData.getWorldBoundMin().x){
            suggestedPosition.x = collisionWorldData.getWorldBoundMin().x;
        }
        if(suggestedPosition.y < collisionWorldData.getWorldBoundMin().y){
            suggestedPosition.y = collisionWorldData.getWorldBoundMin().y;
        }
        if(suggestedPosition.z < collisionWorldData.getWorldBoundMin().z){
            suggestedPosition.z = collisionWorldData.getWorldBoundMin().z;
        }
        if(suggestedPosition.x > collisionWorldData.getWorldBoundMax().x){
            suggestedPosition.x = collisionWorldData.getWorldBoundMax().x;
        }
        if(suggestedPosition.y > collisionWorldData.getWorldBoundMax().y){
            suggestedPosition.y = collisionWorldData.getWorldBoundMax().y;
        }
        if(suggestedPosition.z > collisionWorldData.getWorldBoundMax().z){
            suggestedPosition.z = collisionWorldData.getWorldBoundMax().z;
        }
        return suggestedPosition;
    }
    
    
    /**
     * Sets the collision world data
     * @param collisionWorldData The collision world data
     */
    public void setCollisionWorldData(CollisionWorldData collisionWorldData){
        this.collisionWorldData = collisionWorldData;
    }
    
    
    public boolean collisionSphereCheck(Entity hitbox1, HitboxData hitbox1data, Entity hitbox2, HitboxData hitbox2data){
        Vector3d position1 = EntityUtils.getPosition(hitbox1);
        Vector3d position2 = EntityUtils.getPosition(hitbox2);
        float radius1 = hitbox1data.getRadius();
        float radius2 = hitbox2data.getRadius();
        double distance = position1.distance(position2);
        if(distance < radius1 + radius2){
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Main function to resynchronize entity positions with physics object positions after impulses are applied.
     */
    public void updateDynamicObjectTransforms(){
        Globals.profiler.beginCpuSample("updateDynamicObjectTransforms");
        spaceLock.lock();
        Matrix4d inverseTransform = new Matrix4d();
        if(this.collisionWorldData != null){
            for(Collidable collidable : collidableList){
                if(collidable.getParentTracksCollidable() && collidable.getReady()){
                    Entity physicsEntity = collidable.getParent();
                    DBody rigidBody = PhysicsEntityUtils.getDBody(physicsEntity);
                    DGeom geom = PhysicsEntityUtils.getDGeom(physicsEntity);
                    Vector3d rawPos = null;
                    inverseTransform.identity();
                    if(rigidBody != null){
                        rawPos = PhysicsUtils.odeVecToJomlVec(rigidBody.getPosition()).add(this.floatingOrigin);
                    } else if(geom != null){
                        rawPos = PhysicsUtils.odeVecToJomlVec(geom.getPosition()).add(this.floatingOrigin);
                    } else {
                        continue;
                    }
                    Vector3d calculatedPosition = new Vector3d(rawPos.x,rawPos.y,rawPos.z);
                    Vector3d suggestedPosition = this.suggestMovementPosition(collisionWorldData, calculatedPosition);
                    if(calculatedPosition.distance(suggestedPosition) > 0){
                        collidable.addImpulse(Collidable.TYPE_WORLD_BOUND);
                    }
                    Quaterniond newRotation = null;
                    if(rigidBody != null){
                        newRotation = PhysicsUtils.getRigidBodyRotation(rigidBody);
                    } else {
                        newRotation = PhysicsUtils.getGeomRotation(geom);
                        if(geom instanceof DCylinder || geom instanceof DCapsule){
                            newRotation.rotateX(-Math.PI/2.0);
                        }
                    }
                    if(geom != null){
                        CollidableTemplate template = PhysicsEntityUtils.getPhysicsTemplate(physicsEntity);
                        if(template != null){
                            suggestedPosition.sub(template.getOffsetX(),template.getOffsetY(),template.getOffsetZ());
                            suggestedPosition = this.suggestMovementPosition(collisionWorldData, suggestedPosition);
                            newRotation.mul(new Quaterniond(template.getRotX(),template.getRotY(),template.getRotZ(),template.getRotW()).invert());
                        }
                    }
                    EntityUtils.setPosition(physicsEntity, suggestedPosition);
                    EntityUtils.getRotation(physicsEntity).set(newRotation);
                }
            }
        }
        spaceLock.unlock();
        Globals.profiler.endCpuSample();
    }

    /**
     * Rebases the world origin
     */
    public void rebaseWorldOrigin(){
        Globals.profiler.beginCpuSample("rebaseWorldOrigin");
        spaceLock.lock();
        if(this.collisionWorldData != null){
            int collected = 0;
            Vector3d newOrigin = new Vector3d();
            //calculate new origin
            //only reference the rigid bodies because the rebase is principally concerned with physics sim
            //pure colliders (no rigid body) don't do physics sim in ode so don't need to worry about being quite as close to 0,0,0
            for(Collidable collidable : collidableList){
                Entity physicsEntity = collidable.getParent();
                DBody rigidBody = PhysicsEntityUtils.getDBody(physicsEntity);
                if(rigidBody != null){
                    Vector3d currentBodyOffset = PhysicsUtils.odeVecToJomlVec(rigidBody.getPosition());
                    currentBodyOffset.add(this.floatingOrigin);
                    if(collected == 0){
                        newOrigin.set(currentBodyOffset);
                    } else {
                        newOrigin.add(currentBodyOffset);
                    }
                    collected++;
                }
            }
            if(collected > 0){
                newOrigin = newOrigin.mul(1.0/(double)collected);
            }
            newOrigin = newOrigin.round();
            Vector3d delta = new Vector3d(this.floatingOrigin);
            delta = delta.sub(newOrigin);


            //only perform rebase if sufficiently far away
            if(delta.length() > REBASE_TRIGGER_DISTANCE){
                //error checking
                // if(collected > 1 && delta.length() > MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN){
                //     System.out.println("newOrigin: " + newOrigin);
                //     System.out.println("delta: " + delta);
                //     throw new Error(this.getDebugStatus());
                // }
                // System.out.println("REbase");
                // System.out.println(this.getStatus());
                // System.out.println("newOrigin: " + newOrigin);
                // System.out.println("delta: " + delta);
                // if(delta.y > 100 || delta.y < -100){
                //     throw new Error(this.getDebugStatus());
                // }


                this.floatingOrigin = newOrigin;
                //apply new origin to all geoms
                //calculate new origin
                for(Collidable collidable : collidableList){
                    Entity physicsEntity = collidable.getParent();
                    DBody rigidBody = PhysicsEntityUtils.getDBody(physicsEntity);
                    if(rigidBody != null){
                        Vector3d existingPosition = PhysicsUtils.odeVecToJomlVec(rigidBody.getPosition());
                        rigidBody.setPosition(PhysicsUtils.jomlVecToOdeVec(existingPosition.add(delta)));
                    }
                    DGeom geom = PhysicsEntityUtils.getDGeom(physicsEntity);
                    if(geom != null){
                        if(geom instanceof DSpace space){
                            for(DGeom child : space.getGeoms()){
                                Vector3d existingPosition = PhysicsUtils.odeVecToJomlVec(child.getPosition());
                                child.setPosition(PhysicsUtils.jomlVecToOdeVec(existingPosition.add(delta)));
                            }
                        } else {
                            Vector3d existingPosition = PhysicsUtils.odeVecToJomlVec(geom.getPosition());
                            geom.setPosition(PhysicsUtils.jomlVecToOdeVec(existingPosition.add(delta)));
                        }
                    }
                }
            }
            this.hasRebased = true;
        }
        spaceLock.unlock();
        Globals.profiler.endCpuSample();
    }
    
    /**
     * Registers a collision object with the server
     * @param body The body
     * @param collidable The corresponding collidable
     * @param position The position of the body
     */
    public void registerCollisionObject(DBody body, Collidable collidable, Vector3d position){
        if(collidable == null){
            throw new Error("Collidable is null!");
        }
        spaceLock.lock();

        //Body transform needs to be set before the body is added to the collidable list
        //this makes sure that dynamic update transforms and floating origin work correctly
        this.setBodyTransform(body, new Vector3d(position), new Quaterniond());

        //actually attach to tracking structures
        this.registerPhysicsObject(body);
        bodyPointerMap.put(body,collidable);
        collidableList.add(collidable);
        spaceLock.unlock();
    }

    /**
     * Registers a collision object with the server
     * @param geom The geom
     * @param collidable The corresponding collidable
     */
    public void registerCollisionObject(DGeom geom, Collidable collidable, Vector3d position){
        if(collidable == null){
            throw new Error("Collidable is null!");
        }
        spaceLock.lock();

        //Body transform needs to be set before the body is added to the collidable list
        //this makes sure that dynamic update transforms and floating origin work correctly
        this.setGeomTransform(geom, position, new Quaterniond());

        geomPointerMap.put(geom,collidable);
        collidableList.add(collidable);
        spaceLock.unlock();
    }

    /**
     * Registers a collision object with the server
     * @param geoms The list of geoms
     * @param collidable The corresponding collidable
     */
    public void registerCollisionObject(List<DGeom> geoms, Collidable collidable, Vector3d position){
        if(collidable == null){
            throw new Error("Collidable is null!");
        }
        spaceLock.lock();
        for(DGeom geom : geoms){
            //Body transform needs to be set before the body is added to the collidable list
            //this makes sure that dynamic update transforms and floating origin work correctly
            this.setGeomTransform(geom, position, new Quaterniond());

            geomPointerMap.put(geom,collidable);
        }
        collidableList.add(collidable);
        spaceLock.unlock();
    }

    /**
     * Deregisters a collidable from the physics engine
     * @param collidable The collidable
     */
    public void deregisterCollisionObject(DBody body, Collidable collidable){
        spaceLock.lock();
        bodyPointerMap.remove(body);
        bodies.remove(body);
        collidableList.remove(collidable);
        spaceLock.unlock();
    }

    /**
     * Deregisters a collidable from the physics engine
     * @param collidable The collidable
     */
    public void deregisterCollisionObject(DGeom geom, Collidable collidable){
        spaceLock.lock();
        geomPointerMap.remove(geom);
        collidableList.remove(collidable);
        spaceLock.unlock();
    }
    
    public void listBodyPositions(){
        for(DBody body : bodies){
            LoggerInterface.loggerEngine.INFO("" + body);
            LoggerInterface.loggerEngine.INFO("" + PhysicsUtils.odeVecToJomlVec(body.getPosition()).add(this.floatingOrigin));
        }
    }
    

    /**
     * Casts a ray into the scene and returns the first entity that the ray collides with.
     * This will collide with any type of collidable object.
     * @param start THe start position of the ray
     * @param direction The direction the ray will travel in
     * @param length The length of the ray to cast
     * @return The entity that the ray collides with if successful, null otherwise
     */
    public Entity rayCast(Vector3d start, Vector3d direction, double length){
        return this.rayCast(start,direction,length,null);
    }

    /**
     * Casts a ray into the collision space and returns the first entity that the ray collides with.
     * The type mask is a list of collidable types that are valid collisions.
     * For instance, if the typeMask only contains Collidable.TYPE_TERRAIN, only entities with the type terrain will
     * be returned from the raycast.
     * @param start The start position of the way
     * @param length The length to cast the ray out to
     * @param typeMask The mask of types to collide the ray with
     * @return The entity that the ray cast collided with. Will be null if no entity was collided with.
     */
    public Entity rayCast(Vector3d start, Vector3d direction, double length, List<String> typeMask){
        spaceLock.lock();
        Vector3d unitDir = new Vector3d(direction).normalize();
        //create the ray
        DRay ray = OdeHelper.createRay(space, length);
        ray.set(start.x - this.floatingOrigin.x, start.y - this.floatingOrigin.y, start.z - this.floatingOrigin.z, unitDir.x, unitDir.y, unitDir.z);
        //collide
        RayCastCallbackData data = new RayCastCallbackData(bodyPointerMap, geomPointerMap, typeMask);
        rayCastCallback.setLength(length);
        space.collide2(space, data, rayCastCallback);
        //destroy ray
        ray.destroy();
        spaceLock.unlock();
        return data.collidedEntity;
    }

    /**
     * Ray casts into the scene and gets the position of the closest collision's position in world space.
     * Will collide with any collidable types including characters and items.
     * @param start The start position of the ray to cast
     * @param direction The direction of the ray to cast (this will automatically be converted into a unit vector if it isn't already)
     * @param length The length of the ray to cast
     * @return The position, in world coordinates, of the closest collision of the way, or null if it did not collide with anything.
     */
    public Vector3d rayCastPosition(Vector3d start, Vector3d direction, double length){
        Globals.profiler.beginCpuSample("CollisionEngine.rayCastPosition");
        spaceLock.lock();
        Vector3d unitDir = new Vector3d(direction).normalize();
        //create the ray
        DRay ray = OdeHelper.createRay(space, length);
        ray.set(start.x - this.floatingOrigin.x, start.y - this.floatingOrigin.y, start.z - this.floatingOrigin.z, unitDir.x, unitDir.y, unitDir.z);
        ray.setCategoryBits(Collidable.TYPE_OBJECT_BIT);
        //collide
        RayCastCallbackData data = new RayCastCallbackData(bodyPointerMap, geomPointerMap, null);
        rayCastCallback.setLength(length);
        space.collide2(ray, data, rayCastCallback);
        //destroy ray
        ray.destroy();
        spaceLock.unlock();
        Globals.profiler.endCpuSample();
        return data.collisionPosition;
    }

    /**
     * Ray casts into the scene and gets the position of the closest collision's position in world space.
     * @param start The start position of the ray to cast
     * @param direction The direction of the ray to cast
     * @param length The length of the ray to cast
     * @param typeMask The mask of types to collide the ray with
     * @return The position, in world coordinates, of the closest collision of the way, or null if it did not collide with anything.
     */
    public Vector3d rayCastPositionMasked(Vector3d start, Vector3d direction, double length, List<String> typeMask){
        spaceLock.lock();
        Vector3d unitDir = new Vector3d(direction).normalize();
        //create the ray
        DRay ray = OdeHelper.createRay(space, length);
        ray.set(start.x - this.floatingOrigin.x, start.y - this.floatingOrigin.y, start.z - this.floatingOrigin.z, unitDir.x, unitDir.y, unitDir.z);
        ray.setCategoryBits(Collidable.TYPE_OBJECT_BIT);
        //collide
        RayCastCallbackData data = new RayCastCallbackData(bodyPointerMap, geomPointerMap, typeMask);
        rayCastCallback.setLength(length);
        space.collide2(ray, data, rayCastCallback);
        //destroy ray
        ray.destroy();
        spaceLock.unlock();
        return data.collisionPosition;
    }
    
    /**
     * Registers a body
     * @param body The body
     */
    public void registerPhysicsObject(DBody body){
        if(!bodies.contains(body)){
            bodies.add(body);
            if(bodies.size() > COLLIDABLE_COUNT_WARNING_THRESHOLD){
                LoggerInterface.loggerEngine.WARNING("Body count has superceded the warning threshold! " + bodies.size());
            }
        }
    }
    
    /**
     * Destroys a body and all geometry under the body
     * @param body The DBody to destroy
     */
    protected void destroyDBody(DBody body){
        spaceLock.lock();
        try {
            if(bodies.contains(body)){
                bodies.remove(body);
            }
            //destroy all geometries
            Iterator<DGeom> geomIterator = body.getGeomIterator();
            while(geomIterator.hasNext()){
                DGeom geom = geomIterator.next();
                space.remove(geom);
                this.geomCount--;
                geom.destroy();
            }
            //destroy all joints
            for(int i = 0; i < body.getNumJoints(); i++){
                DJoint joint = body.getJoint(i);
                joint.DESTRUCTOR();
                joint.destroy();
            }
            //destroy actual body
            body.destroy();
        } catch (NullPointerException ex){
            LoggerInterface.loggerEngine.ERROR(ex);
            spaceLock.unlock();
        }
        spaceLock.unlock();
    }

    /**
     * Destroys a geom
     * @param body The DGeom to destroy
     */
    protected void destroyDGeom(DGeom geom){
        spaceLock.lock();
        try {
            geom.destroy();
            this.geomCount--;
        } catch (NullPointerException ex){
            LoggerInterface.loggerEngine.ERROR(ex);
            spaceLock.unlock();
        }
        spaceLock.unlock();
    }
    
    /**
     * Destroys the physics on an entity
     * @param e The entity
     */
    public void destroyPhysics(Entity e){
        //make uncollidable
        if(PhysicsEntityUtils.containsDBody(e)){
            DBody rigidBody = PhysicsEntityUtils.getDBody(e);
            if(rigidBody == null){
                throw new Error("DBody key set to null rigid body! " + rigidBody);
            }
            this.deregisterCollisionObject(rigidBody,PhysicsEntityUtils.getCollidable(e));
            e.removeData(EntityDataStrings.PHYSICS_COLLISION_BODY);
            this.destroyDBody(rigidBody);
        }
        if(PhysicsEntityUtils.containsDGeom(e)){
            DGeom geom = PhysicsEntityUtils.getDGeom(e);
            if(geom == null){
                throw new Error("DGeom key set to null rigid body! " + geom);
            }
            if(geom instanceof DSpace space){
                for(DGeom child : space.getGeoms()){
                    this.destroyDGeom(child);
                }
            }
            this.deregisterCollisionObject(geom,PhysicsEntityUtils.getCollidable(e));
            e.removeData(EntityDataStrings.PHYSICS_GEOM);
            this.destroyDGeom(geom);
        }
        if(ServerPhysicsSyncTree.hasTree(e)){
            ServerPhysicsSyncTree.detachTree(e, ServerPhysicsSyncTree.getTree(e));
        }
    }

    /**
     * Destroys a body + collidable pair
     * @param body The body
     * @param collidable The collidable
     */
    protected void destroyPhysicsPair(DBody body, Collidable collidable){
        spaceLock.lock();
        this.deregisterCollisionObject(body, collidable);
        this.destroyDBody(body);
        spaceLock.unlock();
    }

    /**
     * Disables a body
     * @param body The body
     */
    protected void disable(DBody body){
        spaceLock.lock();
        body.disable();
        spaceLock.unlock();
    }

    /**
     * Enables a body
     * @param body The body
     */
    protected void enable(DBody body){
        spaceLock.lock();
        body.enable();
        spaceLock.unlock();
    }

    /**
     * Checks if a body is enabled
     * @param body The body
     * @return true if it is enabled, false otherwise
     */
    protected boolean isEnabled(DBody body){
        spaceLock.lock();
        boolean rVal = body.isEnabled();
        spaceLock.unlock();
        return rVal;
    }

    /**
     * Creates a trimesh from a given set of vertices and indices
     * @param verts The vertices
     * @param indices The indices
     * @return The DTriMesh
     */
    protected DTriMesh createTrimeshGeom(float[] verts, int[] indices, long categoryBits){
        spaceLock.lock();
        DTriMeshData data = OdeHelper.createTriMeshData();
        data.build(verts, indices);
        final int preprocessFlags = 
        (1 << DTriMeshData.dTRIDATAPREPROCESS_BUILD.CONCAVE_EDGES) | 
        (1 << DTriMeshData.dTRIDATAPREPROCESS_BUILD.FACE_ANGLES) | 
        (1 << DTriMeshData.dTRIDATAPREPROCESS_FACE_ANGLES_EXTRA__MAX)
        ;
        data.preprocess2(preprocessFlags,null);
        DTriMesh rVal = OdeHelper.createTriMesh(this.space, data);
        rVal.setTrimeshData(data);
        rVal.setCategoryBits(categoryBits);
        this.geomCount++;
        spaceLock.unlock();
        return rVal;
    }

    /**
     * Creates a trimesh from a given set of vertices and indices
     * @param verts The vertices
     * @param indices The indices
     * @param space The space to create it within
     * @return The DTriMesh
     */
    protected DTriMesh createTrimeshGeom(float[] verts, int[] indices, long categoryBits, DSpace space){
        spaceLock.lock();
        DTriMeshData data = OdeHelper.createTriMeshData();
        data.build(verts, indices);
        final int preprocessFlags = 
        (1 << DTriMeshData.dTRIDATAPREPROCESS_BUILD.CONCAVE_EDGES) | 
        (1 << DTriMeshData.dTRIDATAPREPROCESS_BUILD.FACE_ANGLES) | 
        (1 << DTriMeshData.dTRIDATAPREPROCESS_FACE_ANGLES_EXTRA__MAX)
        ;
        data.preprocess2(preprocessFlags,null);
        DTriMesh rVal = OdeHelper.createTriMesh(space, data);
        rVal.setTrimeshData(data);
        rVal.setCategoryBits(categoryBits);
        this.geomCount++;
        spaceLock.unlock();
        return rVal;
    }

    /**
     * Creates a cube geometry. Dimensions vector control the total length of the x, y, and z dimensions respectively.
     * @param dimensions The dimensions of the box
     * @return The DBox
     */
    protected DBox createCubeGeom(Vector3d dimensions, long categoryBits){
        spaceLock.lock();
        DBox boxGeom = OdeHelper.createBox(space, dimensions.x, dimensions.y, dimensions.z);
        boxGeom.setCategoryBits(categoryBits);
        this.geomCount++;
        spaceLock.unlock();
        return boxGeom;
    }

    /**
     * Creates a cylinder geometry in the physics space
     * @param dimensions The dimensions of the cylinder. X is the radius, y is the total height.
     * @return The cylinder geometry
     */
    protected DCylinder createCylinderGeom(double radius, double length, long categoryBits){
        spaceLock.lock();
        DCylinder cylinderGeom = OdeHelper.createCylinder(space, radius, length);
        cylinderGeom.setCategoryBits(categoryBits);
        this.geomCount++;
        spaceLock.unlock();
        return cylinderGeom;
    }

    /**
     * Creates a sphere geometry in the physics space
     * @param radius The radius of the sphere
     * @return The sphere geometry
     */
    protected DSphere createSphereGeom(double radius, long categoryBits){
        spaceLock.lock();
        DSphere sphereGeom = OdeHelper.createSphere(space, radius);
        sphereGeom.setCategoryBits(categoryBits);
        this.geomCount++;
        spaceLock.unlock();
        return sphereGeom;
    }

    /**
     * Creates a capsule geometry in the physics space
     * @param radius The radius of the capsule
     * @param length The length of the capsule
     * @return The capsule geometry
     */
    protected DCapsule createCapsuleGeom(double radius, double length, long categoryBits){
        spaceLock.lock();
        DCapsule capsuleGeom = OdeHelper.createCapsule(space, radius, length);
        capsuleGeom.setCategoryBits(categoryBits);
        this.geomCount++;
        spaceLock.unlock();
        return capsuleGeom;
    }

    /**
     * Creates a space
     * @return The space
     */
    protected DSpace createSpace(){
        spaceLock.lock();
        DSpace rVal = OdeHelper.createSimpleSpace();
        this.space.add(rVal);
        this.geomCount++;
        spaceLock.unlock();
        return rVal;
    }

    /**
     * Creates a DBody. Can optionally be passed DGeom objects to be attached to the body when it is created.
     * @param geom The geometry objects to attach to the body on creation
     * @return The DBody
     */
    protected DBody createDBody(DGeom ...geom){
        spaceLock.lock();
        DBody body = OdeHelper.createBody(world);
        // body.setDamping(DEFAULT_LINEAR_DAMPING, DEFAULT_ANGULAR_DAMPING);
        body.setMaxAngularSpeed(DEFAULT_MAX_ANGULAR_SPEED);
        if(geom != null){
            for(int i = 0; i < geom.length; i++){
                if(geom != null){
                    geom[i].setBody(body);
                }
            }
        }
        spaceLock.unlock();
        return body;
    }

    /**
     * Creates a DMass and attaches a body to it
     * @param massValue The amount of mass for the object
     * @param radius The radius of the cylinder
     * @param length The length of the cylinder
     * @param body The DBody to attach the mass to
     * @return The DMass
     */
    protected DMass createCylinderMass(double massValue, double radius, double length, DBody body, Vector3d offset, Quaterniond rotation){
        spaceLock.lock();
        DMass mass = OdeHelper.createMass();
        mass.setCylinder(massValue, 2, radius, length);
        body.setMass(mass);
        spaceLock.unlock();
        return mass;
    }

    /**
     * Creates a DMass and attaches a body to it
     * @param massValue The amount of mass for the object
     * @param dims The dimensions of the box
     * @param body The DBody to attach the mass to
     * @return The DMass
     */
    protected DMass createBoxMass(double massValue, Vector3d dims, DBody body, Vector3d offset, Quaterniond rotation){
        spaceLock.lock();
        DMass mass = OdeHelper.createMass();
        mass.setBox(massValue, dims.x, dims.y, dims.z);
        body.setMass(mass);
        spaceLock.unlock();
        return mass;
    }

    /**
     * Creates a DMass and attaches a body to it
     * @param massValue The amount of mass for the object
     * @param radius The radius of the capsule
     * @param length The length of the capsule
     * @param body The DBody to attach the mass to
     * @return The DMass
     */
    protected DMass createCapsuleMass(double massValue, double radius, double length, DBody body, Vector3d offset, Quaterniond rotation){
        spaceLock.lock();
        DMass mass = OdeHelper.createMass();
        mass.setCapsule(massValue, 2, radius, length);
        body.setMass(mass);
        spaceLock.unlock();
        return mass;
    }

    /**
     * Sets the auto disable flags for the body
     * @param body The body
     * @param autoDisable whether auto disable should be used or not
     * @param linearThreshold The linear velocity threshold to disable under
     * @param angularThreshold The angular velocity threshold to disable under
     * @param steps The number of steps the body must be beneath the threshold before disabling
     */
    protected void setAutoDisableFlags(DBody body, boolean autoDisable, double linearThreshold, double angularThreshold, int steps){
        spaceLock.lock();
        body.setAutoDisableFlag(autoDisable);
        body.setAutoDisableLinearThreshold(linearThreshold);
        body.setAutoDisableAngularThreshold(angularThreshold);
        body.setAutoDisableSteps(steps);
        spaceLock.unlock();
    }

    /**
     * Sets the damping of the body
     * @param body The body
     * @param linearDamping The linear damping
     * @param angularDamping The angular damping
     */
    protected void setDamping(DBody body, double linearDamping, double angularDamping){
        spaceLock.lock();
        body.setDamping(linearDamping, angularDamping);
        spaceLock.unlock();
    }

    /**
     * Sets the transform on a body
     * @param body The body
     * @param position The position
     * @param rotation The rotation
     */
    protected void setBodyTransform(DBody body, Vector3d position, Quaterniond rotation){
        spaceLock.lock();

        // if(this.name.equals("serverPhysics")){
        //     if(position.distance(0,0,0) < 100){
        //         throw new Error("Reposition server body " + position);
        //     }
        // }
        body.setPosition(position.x - this.floatingOrigin.x, position.y - this.floatingOrigin.y, position.z - this.floatingOrigin.z);
        body.setQuaternion(PhysicsUtils.jomlQuatToOdeQuat(rotation));
        // if(this.name.equals("serverPhysics")){
        //     System.out.println("SetBodyTransform " + body.getPosition());
        // }
        spaceLock.unlock();
    }

    /**
     * Synchronizes the data on a body
     * @param body The body
     * @param position The position
     * @param rotation The rotation
     * @param linearVel The linear velocity
     * @param angularVel The angular velocity
     * @param linearForce The linear force
     * @param angularForce The angular force
     * @param enabled Whether the body is enabled or not -- true to enable, false to disable
     */
    protected void synchronizeData(DBody body, Vector3d position, Quaterniond rotation, Vector3d linearVel, Vector3d angularVel, Vector3d linearForce, Vector3d angularForce, boolean enabled){
        if(body != null){
            spaceLock.lock();
            this.synchronizeData(body, position, rotation, linearVel, angularVel, linearForce, angularForce);
            if(enabled){
                body.enable();
            } else {
                body.disable();
            }
            spaceLock.unlock();
        }
    }

    /**
     * Synchronizes the data on a body
     * @param body The body
     * @param position The position
     * @param rotation The rotation
     * @param linearVel The linear velocity
     * @param angularVel The angular velocity
     * @param linearForce The linear force
     * @param angularForce The angular force
     */
    protected void synchronizeData(DBody body, Vector3d position, Quaterniond rotation, Vector3d linearVel, Vector3d angularVel, Vector3d linearForce, Vector3d angularForce){
        if(!this.hasRebased){
            return;
        }
        if(body != null){
            spaceLock.lock();


            // if(this.name.equals("clientPhysics") || this.name.equals("serverPhysics")){
            //     double posX = position.x - this.floatingOrigin.x;
            //     double posY = position.y - this.floatingOrigin.y;
            //     double posZ = position.z - this.floatingOrigin.z;
            //     if((posX > MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN || posZ > MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN || posX < -MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN || posZ < -MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN) && this.floatingOrigin.length() == 0 && this.hasRebased){
            //         System.out.println("Sync body pos: " + posX + "," + posY + "," + posZ);
            //         spaceLock.unlock();
            //         return;
            //     }
            //     if(this.bodies.size() > 2 && this.floatingOrigin.length() == 0 && this.hasRebased){
            //         throw new Error(this.getDebugStatus());
            //     }
            // }
            body.setPosition(position.x - this.floatingOrigin.x, position.y - this.floatingOrigin.y, position.z - this.floatingOrigin.z);
            body.setQuaternion(PhysicsUtils.jomlQuatToOdeQuat(rotation));
            body.setLinearVel(PhysicsUtils.jomlVecToOdeVec(linearVel));
            body.setAngularVel(PhysicsUtils.jomlVecToOdeVec(angularVel));
            body.setForce(PhysicsUtils.jomlVecToOdeVec(linearForce));
            body.setTorque(PhysicsUtils.jomlVecToOdeVec(angularForce));
            spaceLock.unlock();
        }
    }

    /**
     * Sets the transform on a body
     * @param body The body
     * @param position The position
     * @param rotation The rotation
     * @param scale The scale
     */
    protected void setBodyTransform(DBody body, CollidableTemplate template, Vector3d position, Quaterniond rotation, Vector3d scale){
        spaceLock.lock();
        // if(this.name.equals("clientPhysics") || this.name.equals("serverPhysics")){
        //     double posX = position.x - this.floatingOrigin.x;
        //     double posY = position.y - this.floatingOrigin.y;
        //     double posZ = position.z - this.floatingOrigin.z;
        //     if((posX > MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN || posZ > MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN || posX < -MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN || posZ < -MAX_EXPECTED_DIST_FROM_LOCAL_ORIGIN) && this.floatingOrigin.length() == 0 && this.hasRebased){
        //         System.out.println("Set body pos: " + posX + "," + posY + "," + posZ);
        //         spaceLock.unlock();
        //         return;
        //     }
        //     if(this.bodies.size() > 2 && this.floatingOrigin.length() == 0 && this.hasRebased){
        //         throw new Error(this.getDebugStatus());
        //     }
        // }
        body.setPosition(position.x - this.floatingOrigin.x, position.y - this.floatingOrigin.y, position.z - this.floatingOrigin.z);
        body.setQuaternion(PhysicsUtils.jomlQuatToOdeQuat(rotation));
        DGeom firstGeom = body.getFirstGeom();
        if(firstGeom instanceof DCylinder){
            ((DCylinder)firstGeom).setParams(template.getDimension1() * scale.x,template.getDimension2() * scale.y);
        } else if(firstGeom instanceof DBox){
            ((DBox)firstGeom).setLengths(template.getDimension1() * scale.x,template.getDimension2() * scale.y,template.getDimension3() * scale.z);
        } else if(firstGeom instanceof DCapsule){
            ((DCapsule)firstGeom).setParams(template.getDimension1() * scale.x,template.getDimension2() * scale.y);
        }
        spaceLock.unlock();
    }

    /**
     * Sets the transform of a geometry (local to the parent)
     * @param geom The geometry
     * @param position The position
     * @param rotation The rotation
     */
    protected void setGeomOffsetTransform(DGeom geom, Vector3d position, Quaterniond rotation){
        spaceLock.lock();
        geom.setOffsetPosition(position.x, position.y, position.z);
        geom.setOffsetQuaternion(PhysicsUtils.jomlQuatToOdeQuat(rotation));
        spaceLock.unlock();
    }

    /**
     * Sets the transform of a geometry (local to the parent)
     * @param geom The geometry
     * @param position The position
     * @param rotation The rotation
     */
    protected void setGeomTransform(DGeom geom, Vector3d position, Quaterniond rotation){
        spaceLock.lock();
        if(geom instanceof DSpace space){
            for(DGeom child : space.getGeoms()){
                this.setGeomTransform(child, position, rotation);
            }
        } else {
            geom.setPosition(position.x - this.floatingOrigin.x, position.y - this.floatingOrigin.y, position.z - this.floatingOrigin.z);
            if(geom instanceof DCylinder || geom instanceof DCapsule){
                geom.setQuaternion(PhysicsUtils.jomlQuatToOdeQuat(new Quaterniond(rotation).rotateX(Math.PI/2.0)));
            } else {
                geom.setQuaternion(PhysicsUtils.jomlQuatToOdeQuat(rotation));
            }
        }
        spaceLock.unlock();
    }

    /**
     * Corrects the initial axis of eg cylinders or capsules
     * @param geom the geometry to correct
     */
    protected void setOffsetRotation(DGeom geom){
        spaceLock.lock();
        geom.setOffsetRotation(CollisionBodyCreation.AXIS_CORRECTION_MATRIX);
        spaceLock.unlock();
    }

    /**
     * Gets the position of the body in a thread-safe way
     * @param body The body to get the position of
     * @return The position
     */
    protected Vector3d getBodyPosition(DBody body){
        Vector3d rVal = null;
        spaceLock.lock();
        rVal = PhysicsUtils.odeVecToJomlVec(body.getPosition()).add(this.floatingOrigin);
        spaceLock.unlock();
        return rVal;
    }

    /**
     * Gets the rotation of the body in a thread-safe way
     * @param body The body to get the rotation of
     * @return The rotation
     */
    protected Quaterniond getBodyRotation(DBody body){
        Quaterniond rVal = null;
        spaceLock.lock();
        rVal = PhysicsUtils.odeQuatToJomlQuat(body.getQuaternion());
        spaceLock.unlock();
        return rVal;
    }

    /**
     * Sets a body to be kinematic (infinite mass, not affected by gravity)
     * @param body The body to set
     */
    protected void setKinematic(DBody body){
        spaceLock.lock();
        body.setKinematic();
        spaceLock.unlock();
    }

    /**
     * Sets the gravity mode of the body
     * @param body the body
     * @param gravityMode the gravity mode
     */
    protected void setGravityMode(DBody body, boolean gravityMode){
        spaceLock.lock();
        body.setGravityMode(gravityMode);
        spaceLock.unlock();
    }

    /**
     * Sets the offset position of the first geometry in the body
     * @param body The body
     * @param offsetVector The offset position
     */
    protected void setOffsetPosition(DBody body, Vector3d offsetVector){
        spaceLock.lock();
        body.getGeomIterator().next().setOffsetPosition(offsetVector.x,offsetVector.y,offsetVector.z);
        spaceLock.unlock();
    }

    /**
     * Sets whether the body is angularly static or not
     * @param body The body
     * @param angularlyStatic true if angularly static, false otherwise
     */
    protected void setAngularlyStatic(DBody body, boolean angularlyStatic){
        spaceLock.lock();
        if(angularlyStatic){
            body.setMaxAngularSpeed(0);
        } else {
            body.setMaxAngularSpeed(DEFAULT_MAX_ANGULAR_SPEED);
        }
        spaceLock.unlock();
    }

    /**
     * Removes the geometry from the body
     * @param body the body
     * @param geom the geometry
     */
    protected void removeGeometryFromBody(DBody body, DGeom geom){
        geom.setBody(null);
    }

    /**
     * Attaches a geom to a body
     * @param body the body
     * @param geom the geom
     */
    protected void attachGeomToBody(DBody body, DGeom geom){
        geom.setBody(body);
    }

    /**
     * Locks the ode library
     */
    public static void lockOde(){
        spaceLock.lock();
    }

    /**
     * Unlocks the ode library
     */
    public static void unlockOde(){
        spaceLock.unlock();
    }

    /**
     * Gets the status of the collision engine
     * @return The status of the collision engine
     */
    public String getStatus(){
        CollisionEngine.lockOde();
        String message = "" +
        "Name: " + this.name + "\n" +
        "Bodies: " + this.bodies.size() + "\n" +
        "Body Ptrs: " + this.bodyPointerMap.size() + "\n" +
        "Geom Ptrs: " + this.geomPointerMap.size() + "\n" +
        "Collidables: " + this.collidableList.size() + "\n" +
        "  (Static) Collidables: " + this.collidableList.stream().filter((Collidable collidable) -> collidable.getType().matches(Collidable.TYPE_STATIC)).collect(Collectors.toList()).size() + "\n" +
        "  (Creature) Collidables: " + this.collidableList.stream().filter((Collidable collidable) -> collidable.getType().matches(Collidable.TYPE_CREATURE)).collect(Collectors.toList()).size() + "\n" +
        "Space geom count: " + this.space.getNumGeoms() + "\n" +
        "Tracked geom count: " + this.geomCount + "\n" +
        "Floating origin: " + this.floatingOrigin.x + "," + this.floatingOrigin.y + "," + this.floatingOrigin.z + "\n" +
        "Near Collision Count: " + this.nearCollisionCount + "\n" +
        "Final Collision Count: " + this.finalCollisionCount + "\n" +
        ""
        ;
        CollisionEngine.unlockOde();
        return message;
    }

    /**
     * Gets the status of the collision engine
     * @return The status of the collision engine
     */
    public String getDebugStatus(){
        String message = this.getStatus();
        for(Collidable collidable : collidableList){
            DBody rigidBody = PhysicsEntityUtils.getDBody(collidable.getParent());
            if(rigidBody != null){
                Vector3d existingPosition = PhysicsUtils.odeVecToJomlVec(rigidBody.getPosition());
                message = message + existingPosition.x + "," + existingPosition.y + "," + existingPosition.z + "\n";
            }
        }
        return message;
    }

    /**
     * Gets the floating origin of the collision engine
     * @return The floating origin
     */
    public Vector3d getFloatingOrigin(){
        return new Vector3d(this.floatingOrigin);
    }

    /**
     * Gets the world of the engine
     * @return The world of the engine
     */
    protected DWorld getWorld(){
        return this.world;
    }

    /**
     * Gets the space of the engine
     * @return The space of the engine
     */
    protected DSpace getSpace(){
        return this.space;
    }

    /**
     * Gets the near collision count
     * @return The near collision count
     */
    protected int getNearCollisionCount(){
        return nearCollisionCount;
    }

    /**
     * Gets the number of collisions that make it to the point of creating joints/impulses
     * @return The number of collisions
     */
    protected int getFinalCollisionCount(){
        return finalCollisionCount;
    }
    

    /**
     * A callback for resolving collisions between two entities
     */
    public interface CollisionResolutionCallback {
        /**
         * Resolves a collision between two collidables in the engine
         * @param contactGeom the ode4j contact geom
         * @param geom1 The first geometry
         * @param geom2 The second geometry
         * @param impactor The collidable initiating the contact
         * @param receiver The collidable recieving the contact
         * @param normal The normal of the collision
         * @param localPosition The local position of the collision
         * @param worldPos The world position of the collision
         * @param magnitude The magnitude of the collision
         */
        public void resolve(DContactGeom contactGeom, DGeom geom1, DGeom geom2, Collidable impactor, Collidable receiver, Vector3d normal, Vector3d localPosition, Vector3d worldPos, float magnitude);
    }

}
