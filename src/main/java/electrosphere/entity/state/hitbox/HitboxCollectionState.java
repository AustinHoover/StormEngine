package electrosphere.entity.state.hitbox;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

import electrosphere.collision.CollisionBodyCreation;
import electrosphere.collision.CollisionEngine;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.collision.hitbox.HitboxManager;
import electrosphere.collision.hitbox.HitboxUtils.HitboxPositionCallback;
import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.data.utils.DataFormatUtil;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState.HitboxShapeType;
import electrosphere.logger.LoggerInterface;
import electrosphere.mem.JomlPool;
import electrosphere.server.datacell.Realm;
import electrosphere.server.entity.poseactor.PoseActor;
import electrosphere.util.math.SpatialMathUtils;

/**
 * The state of the collection of all hitboxes on this entity
 * Ie, it stores the state of each hitbox that is attached to this entity
 */
public class HitboxCollectionState {

    /**
     * Types of hitboxes
     */
    public enum HitboxType {
        HIT, // damages another entity
        HURT, // receives damage from another entity
        BLOCK, // blocks a hit from another entity
    }

    /**
     * The subtype of hitbox
     */
    public enum HitboxSubtype {
        SWEET, //extra damage
        REGULAR, //regular damage
        SOUR, //less damage
    }

    /**
     * Distance after which hitboxes are disabled on the client
     */
    public static final double CLIENT_DISABLE_DISTANCE = 40;

    /**
     * Distance within which hitboxes are enabled on the client
     */
    public static final double CLIENT_ENABLE_DISTANCE = 35;

    /**
     * The parent entity of the hitbox state
     */
    Entity parent;

    /**
     * The body that contains all the hitbox shapes
     */
    DBody body;

    /**
     * The collidable associated with the body
     */
    Collidable collidable;

    /**
     * The list of all geoms in the collection state
     */
    List<DGeom> geoms = new LinkedList<DGeom>();

    /**
     * The map of bone -> list of states of individual shapes attached to that bone
     */
    Map<String,List<HitboxState>> boneHitboxMap = new HashMap<String,List<HitboxState>>();

    /**
     * Map of hitbox state -> geometry
     */
    Map<HitboxState,DGeom> stateGeomMap = new HashMap<HitboxState,DGeom>();

    /**
     * The map of geometry -> hitbox shape status, useful for finding data about a given hitbox during collision
     */
    Map<DGeom,HitboxState> geomStateMap = new HashMap<DGeom,HitboxState>();

    /**
     * The list of all hitbox states
     */
    List<HitboxState> allStates = new LinkedList<HitboxState>();

    /**
     * The list of non-bone hitboxes
     */
    List<HitboxState> nonBoneHitboxes = new LinkedList<HitboxState>();

    /**
     * Callback to provide a position for the hitbox each frame
     */
    HitboxPositionCallback positionCallback;

    /**
     * The raw data to pattern hitboxes off of
     */
    List<HitboxData> rawData;

    /**
     * Controls whether the hitbox state is active or not
     */
    boolean active = true;

    /**
     * Controls whether active hitboxes should be overwritten with block boxes
     */
    boolean blockOverride = false;

    /**
     * The associated manager
     */
    HitboxManager manager;

    /**
     * Controls whether this hitbox collection thinks its on the server or client
     */
    boolean isServer = true;


    /**
     * Create hitbox state for an entity
     * @param collisionEngine the collision engine
     * @param entity The entity to attach the state to
     * @param hitboxListRaw The list of hitbox data to apply
     * @return The hitbox state that has been attached to the entity
     */
    public static HitboxCollectionState attachHitboxState(HitboxManager manager, boolean isServer, Entity entity, List<HitboxData> hitboxListRaw){
        HitboxCollectionState rVal = new HitboxCollectionState();
        rVal.rawData = hitboxListRaw;
        rVal.manager = manager;
        rVal.isServer = isServer;
        //attach
        entity.putData(EntityDataStrings.HITBOX_DATA, rVal);
        rVal.parent = entity;
        //register
        rVal.createBody();
        manager.registerHitbox(rVal);

        return rVal;
    }

    /**
     * Create hitbox state for an entity
     * @param collisionEngine the collision engine
     * @param entity The entity to attach the state to
     * @param data The hitbox data to apply
     * @param callback The callback that provides a position for the hitbox each frame
     * @return The hitbox state that has been attached to the entity
     */
    public static HitboxCollectionState attachHitboxStateWithCallback(HitboxManager manager, CollisionEngine collisionEngine, Entity entity, HitboxData data, HitboxPositionCallback callback){
        throw new UnsupportedOperationException("Not yet implemented!");
        // HitboxCollectionState rVal = new HitboxCollectionState();

        // //create the shapes
        // DGeom geom = CollisionBodyCreation.createShapeSphere(collisionEngine, data.getRadius(), Collidable.TYPE_OBJECT_BIT);

        // //create the state for the individual shape
        // HitboxState state = new HitboxState(data.getBone(), data, getType(data), getSubType(data), getShapeType(data), false);
        // rVal.addHitbox(data.getBone(), state);
        // rVal.registerGeom(geom,state);

        // //create body with all the shapes
        // DGeom[] geomArray = rVal.geoms.toArray(new DGeom[rVal.geoms.size()]);
        // rVal.body = CollisionBodyCreation.createBodyWithShapes(collisionEngine, geomArray);

        // //register collidable with collision engine
        // Collidable collidable = new Collidable(entity, Collidable.TYPE_OBJECT);
        // collisionEngine.registerCollisionObject(rVal.body, collidable);

        // //attach
        // entity.putData(EntityDataStrings.HITBOX_DATA, rVal);
        // rVal.parent = entity;

        // //register
        // manager.registerHitbox(rVal);
        // rVal.manager = manager;

        // return rVal;
    }

    /**
     * Creates the body
     */
    private void createBody(){
        CollisionEngine collisionEngine = this.manager.getCollisionEngine();
        CollisionEngine.lockOde();
        //create the shapes
        for(HitboxData hitboxDataRaw : this.rawData){
            DGeom geom = null;
            HitboxType type = HitboxType.HIT;
            HitboxShapeType shapeType = HitboxShapeType.SPHERE;
            //
            //Get the type as an enum
            //
            switch(hitboxDataRaw.getType()){
                case HitboxData.HITBOX_TYPE_HIT: {
                    type = HitboxType.HIT;
                    shapeType = HitboxShapeType.SPHERE;
                    geom = CollisionBodyCreation.createShapeSphere(collisionEngine, hitboxDataRaw.getRadius(), Collidable.TYPE_OBJECT_BIT);
                } break;
                case HitboxData.HITBOX_TYPE_HURT: {
                    type = HitboxType.HURT;
                    shapeType = HitboxShapeType.SPHERE;
                    geom = CollisionBodyCreation.createShapeSphere(collisionEngine, hitboxDataRaw.getRadius(), Collidable.TYPE_OBJECT_BIT);
                } break;
                case HitboxData.HITBOX_TYPE_HIT_CONNECTED: {
                    type = HitboxType.HIT;
                    shapeType = HitboxShapeType.CAPSULE;
                    geom = CollisionBodyCreation.createShapeSphere(collisionEngine, hitboxDataRaw.getRadius(), Collidable.TYPE_OBJECT_BIT);
                } break;
                case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {
                    type = HitboxType.HURT;
                    shapeType = HitboxShapeType.CAPSULE;
                    geom = CollisionBodyCreation.createShapeSphere(collisionEngine, hitboxDataRaw.getRadius(), Collidable.TYPE_OBJECT_BIT);
                } break;
                case HitboxData.HITBOX_TYPE_STATIC_CAPSULE: {
                    type = HitboxType.HURT;
                    shapeType = HitboxShapeType.STATIC_CAPSULE;
                    geom = CollisionBodyCreation.createCapsuleShape(collisionEngine, hitboxDataRaw.getRadius(), hitboxDataRaw.getLength(), Collidable.TYPE_OBJECT_BIT);
                } break;
            }
            //
            //Get the subtype as an enum
            //
            HitboxSubtype subType;
            String subTypeRaw = hitboxDataRaw.getSubType();
            if(subTypeRaw == null){
                subTypeRaw = HitboxData.HITBOX_SUBTYPE_REUGLAR;
            }
            switch(subTypeRaw){
                case HitboxData.HITBOX_SUBTYPE_SWEET: {
                    subType = HitboxSubtype.SWEET;
                } break;
                case HitboxData.HITBOX_SUBTYPE_REUGLAR: {
                    subType = HitboxSubtype.REGULAR;
                } break;
                case HitboxData.HITBOX_SUBTYPE_SOUR: {
                    subType = HitboxSubtype.SOUR;
                } break;
                default: {
                    subType = HitboxSubtype.REGULAR;
                } break;
            }

            //create the state for the individual shape
            HitboxState state = new HitboxState(hitboxDataRaw.getBone(), hitboxDataRaw, type, subType, shapeType, false);

            //add to structures
            if(hitboxDataRaw.getBone() != null){
                this.addHitbox(hitboxDataRaw.getBone(),state);
            } else {
                this.addHitbox(state);
            }
            this.registerGeom(geom,state);
        }

        //create body with all the shapes
        DGeom[] geomArray = this.geoms.toArray(new DGeom[this.geoms.size()]);
        this.body = CollisionBodyCreation.createBodyWithShapes(collisionEngine, geomArray);
        CollisionBodyCreation.setKinematic(collisionEngine, this.body);

        //register collidable with collision engine
        this.collidable = new Collidable(this.parent, Collidable.TYPE_OBJECT, true);
        Vector3d entPos = EntityUtils.getPosition(this.parent);
        collisionEngine.registerCollisionObject(this.body, this.collidable, entPos);
        CollisionEngine.unlockOde();
    }

    /**
     * Destroys the body
     */
    private void destroyBody(){
        //destroy body
        if(this.body != null){
            PhysicsUtils.destroyPhysicsPair(this.manager.getCollisionEngine(), this.body, this.collidable);
            //clear tracking structures
            this.geoms.clear();
            this.boneHitboxMap.clear();
            this.stateGeomMap.clear();
            this.geomStateMap.clear();
            this.allStates.clear();
            this.nonBoneHitboxes.clear();
            this.body = null;
            this.collidable = null;
        }
    }

    /**
     * Clears the collision status of all shapes
     */
    public void clearCollisions(){
        for(DGeom geom : this.geoms){
            HitboxState shapeStatus = this.geomStateMap.get(geom);
            shapeStatus.setHadCollision(false);
        }
    }

    /**
     * Updates the positions of all hitboxes
     */
    public void updateHitboxPositions(CollisionEngine collisionEngine){
        if(parent != null && !isServer && EntityUtils.getActor(parent) != null){
            if(!this.geoms.isEmpty()){
                Vector3d entityPosition = EntityUtils.getPosition(parent);
                double distanceToPlayer = 0;
                if(Globals.clientState.playerEntity != null){
                    Vector3d playerPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
                    distanceToPlayer = entityPosition.distance(playerPos);
                }
                if(distanceToPlayer > CLIENT_DISABLE_DISTANCE){
                    if(this.body != null){
                        this.destroyBody();
                    }
                } else if(distanceToPlayer < CLIENT_ENABLE_DISTANCE) {
                    if(this.body == null){
                        this.createBody();
                    }
                    PhysicsUtils.setRigidBodyTransform(collisionEngine, entityPosition, new Quaterniond(), body);
                    for(String boneName : this.boneHitboxMap.keySet()){
                        if(EntityUtils.getActor(parent).getAnimationData().containsBone(boneName)){
                            Vector3d bonePosition = EntityUtils.getActor(parent).getAnimationData().getBonePosition(boneName);
                            for(HitboxState state : this.boneHitboxMap.get(boneName)){
                                DGeom geom = this.stateGeomMap.get(state);
                                HitboxState shapeStatus = this.geomStateMap.get(geom);
                                switch(shapeStatus.shapeType){
                                    case SPHERE: {
                                        this.updateSphereShapePosition(collisionEngine,boneName,shapeStatus,bonePosition);
                                    } break;
                                    case CAPSULE: {
                                        this.updateCapsuleShapePosition(collisionEngine,boneName,shapeStatus,bonePosition);
                                    } break;
                                    case STATIC_CAPSULE: {
                                    } break;
                                }
                            }
                        }
                    }
                    for(HitboxState state : this.nonBoneHitboxes){
                        DGeom geom = this.stateGeomMap.get(state);
                        HitboxState shapeStatus = this.geomStateMap.get(geom);
                        switch(shapeStatus.shapeType){
                            case SPHERE: {
                                this.updateSphereShapePosition(collisionEngine,null,shapeStatus,null);
                            } break;
                            case CAPSULE: {
                                this.updateCapsuleShapePosition(collisionEngine,null,shapeStatus,null);
                            } break;
                            case STATIC_CAPSULE: {
                            } break;
                        }
                    }
                }
            }

        //update bone-attached hitboxes on server
        } else if(parent != null && isServer && EntityUtils.getPoseActor(parent) != null){
            if(!this.geoms.isEmpty()){
                Vector3d entityPosition = EntityUtils.getPosition(parent);
                PhysicsUtils.setRigidBodyTransform(collisionEngine, entityPosition, new Quaterniond(), body);
                //
                for(String boneName : this.boneHitboxMap.keySet()){
                    if(EntityUtils.getPoseActor(parent).containsBone(boneName)){
                        PoseActor poseActor = EntityUtils.getPoseActor(parent);
                        Vector3d bonePosition = poseActor.getBonePosition(boneName);
                        //
                        for(HitboxState state : this.boneHitboxMap.get(boneName)){
                            if(state == null){
                                throw new IllegalStateException("Geometry not assigned to a hitbox state!");
                            }
                            //
                            switch(state.shapeType){
                                case SPHERE: {
                                    this.updateSphereShapePosition(collisionEngine,boneName,state,bonePosition);
                                } break;
                                case CAPSULE: {
                                    this.updateCapsuleShapePosition(collisionEngine,boneName,state,bonePosition);
                                } break;
                                case STATIC_CAPSULE: {
                                } break;
                            }
                        }
                    }
                }
                for(HitboxState state : this.nonBoneHitboxes){
                    DGeom geom = this.stateGeomMap.get(state);
                    HitboxState shapeStatus = this.geomStateMap.get(geom);
                    switch(shapeStatus.shapeType){
                        case SPHERE: {
                            this.updateSphereShapePosition(collisionEngine,null,shapeStatus,null);
                        } break;
                        case CAPSULE: {
                            this.updateCapsuleShapePosition(collisionEngine,null,shapeStatus,null);
                        } break;
                        case STATIC_CAPSULE: {
                        } break;
                    }
                }
            }
        //update non-bone attached static objects on server
        } else if(parent != null && isServer){
            for(DGeom geom : this.geoms){
                HitboxState shapeStatus = this.geomStateMap.get(geom);
                switch(shapeStatus.shapeType){
                    case SPHERE: {
                        this.updateSphereShapePosition(collisionEngine,null,shapeStatus,null);
                    } break;
                    case CAPSULE: {
                        this.updateCapsuleShapePosition(collisionEngine,null,shapeStatus,null);
                    } break;
                    case STATIC_CAPSULE: {
                        this.updateStaticCapsulePosition(collisionEngine, geom, shapeStatus);
                    } break;
                }
            }
        }
    }

    /**
     * Updates the position of the geom for a static capsule
     * @param collisionEngine The collision engine
     * @param boneName The name of the bone the static capsule is attached to
     * @param bonePosition The position of the bone
     */
    private void updateStaticCapsulePosition(CollisionEngine collisionEngine, DGeom geom, HitboxState shapeStatus){
        PhysicsEntityUtils.setGeometryOffsetPosition(collisionEngine, geom, new Vector3d(), new Quaterniond(0.707,0,0,0.707));
    }

    /**
     * Updates the position of a sphere-shape-type hitbox
     * @param collisionEngine The collision engine
     * @param boneName The name of the bone
     * @param bonePosition the position of the bone
     */
    private void updateSphereShapePosition(CollisionEngine collisionEngine, String boneName, HitboxState hitboxState, Vector3d bonePosition){
        DGeom geom = this.stateGeomMap.get(hitboxState);

        //get pooled objects
        Vector3d offsetPosition = JomlPool.getD();
        Vector3d parentScale = JomlPool.getD();
        Vector3d finalPos = JomlPool.getD();

        //get offset's transform
        offsetPosition = DataFormatUtil.getDoubleListAsVector(hitboxState.getHitboxData().getOffset(),offsetPosition);
        Quaterniond offsetRotation = new Quaterniond();

        //the bone's transform
        Vector3d bonePositionD = new Vector3d();
        if(bonePosition != null){
            bonePositionD.set(bonePosition);
        }
        Quaterniond boneRotation = new Quaterniond();

        //the parent's transform
        Vector3d parentPosition = EntityUtils.getPosition(parent);
        Quaterniond parentRotation = EntityUtils.getRotation(parent);
        parentScale.set(EntityUtils.getScale(parent));

        //calculate
        Vector3d hitboxPos = AttachUtils.calculateBoneAttachmentLocalPosition(finalPos, offsetPosition, offsetRotation, bonePositionD, boneRotation, parentPosition, parentRotation, parentScale);


        //actually set value
        PhysicsEntityUtils.setGeometryOffsetPosition(collisionEngine, geom, hitboxPos, new Quaterniond());

        //release pooled objects
        JomlPool.release(offsetPosition);
        JomlPool.release(parentScale);
        JomlPool.release(finalPos);
    }

    /**
     * Updates the position of a capsule-shape hitbox
     * @param collisionEngine
     * @param boneName
     * @param bonePosition
     */
    private void updateCapsuleShapePosition(CollisionEngine collisionEngine, String boneName, HitboxState hitboxState, Vector3d bonePosition){

        //get data about the hitbox
        DGeom geom = this.stateGeomMap.get(hitboxState);
        Vector3d previousWorldPos = hitboxState.getPreviousWorldPos();
        double length = hitboxState.getHitboxData().getRadius();


        //get offset's transform
        Vector3d offsetPosition = DataFormatUtil.getDoubleListAsVector(hitboxState.getHitboxData().getOffset());
        Quaterniond offsetRotation = new Quaterniond();

        //the bone's transform
        Vector3d bonePositionD = new Vector3d();
        if(bonePosition != null){
            bonePositionD = new Vector3d(bonePosition);
        }
        Quaterniond boneRotation = new Quaterniond();

        //the parent's transform
        Vector3d parentPosition = EntityUtils.getPosition(parent);
        Quaterniond parentRotation = EntityUtils.getRotation(parent);
        Vector3d parentScale = new Vector3d(EntityUtils.getScale(parent));

        //calculate
        Vector3d worldPosition = AttachUtils.calculateBoneAttachmentLocalPosition(offsetPosition, offsetRotation, bonePositionD, boneRotation, parentPosition, parentRotation, parentScale);
        Quaterniond worldRotation = new Quaterniond();

        if(previousWorldPos != null){
            //called all subsequent updates to hitbox position

            //destroy old capsule
            this.destroyGeom(collisionEngine, geom);

            //calculate position between new world point and old world point
            Vector3d bodyPosition = new Vector3d(worldPosition).lerp(previousWorldPos, 0.5);

            //calculate rotation from old position to new position
            //the second quaternion is a rotation along the x axis. This is used to put the hitbox rotation into ode's space
            //ode is Z-axis-up
            if(previousWorldPos.distance(worldPosition) > 0.0){
                worldRotation = SpatialMathUtils.calculateRotationFromPointToPoint(previousWorldPos,worldPosition).mul(new Quaterniond(0,0.707,0,0.707));
            }

            //create new capsule
            length = previousWorldPos.distance(worldPosition) / 2.0;
            if(length > 5000 || Double.isNaN(length) || Double.isInfinite(length) || length < 0){
                if(length < 0){
                    LoggerInterface.loggerEngine.WARNING("Length is too short! " + length);
                }
                if(length > 5000){
                    LoggerInterface.loggerEngine.WARNING("Length is too long! " + length);
                }
                if(Double.isNaN(length) || Double.isInfinite(length)){
                    LoggerInterface.loggerEngine.WARNING("Length is invalid number! " + length);
                }
                if(Double.isNaN(previousWorldPos.x) || Double.isInfinite(previousWorldPos.x)){
                    LoggerInterface.loggerEngine.WARNING("Previous hitbox position isn't valid!");
                }
                if(Double.isNaN(worldPosition.x) || Double.isInfinite(worldPosition.x)){
                    LoggerInterface.loggerEngine.WARNING("Current hitbox position isn't valid!");
                }
                length = 0.1;
            }
            geom = CollisionBodyCreation.createCapsuleShape(manager.getCollisionEngine(), hitboxState.getHitboxData().getRadius(), length, Collidable.TYPE_OBJECT_BIT);
            CollisionBodyCreation.attachGeomToBody(collisionEngine,body,geom);
            PhysicsEntityUtils.setGeometryOffsetPosition(collisionEngine, geom, bodyPosition, worldRotation);
        } else {
            //called first time the hitbox updates position
            this.destroyGeom(collisionEngine, geom);

            //create new capsule
            geom = CollisionBodyCreation.createCapsuleShape(manager.getCollisionEngine(), hitboxState.getHitboxData().getRadius(), length, Collidable.TYPE_OBJECT_BIT);
            CollisionBodyCreation.attachGeomToBody(collisionEngine,body,geom);
        }
        //update maps and other variables for next frame
        this.registerGeom(geom, hitboxState);
        hitboxState.setPreviousWorldPos(worldPosition);
    }

    /**
     * Gets the status of a shape in the hitbox object
     * @param geom The geometry that is the shape within the hitbox data
     * @return The status of the shape
     */
    public HitboxState getShapeStatus(DGeom geom){
        return this.geomStateMap.get(geom);
    }

    /**
     * Gets the geom from the state object
     * @param state The state object
     * @return The associated geom
     */
    public DGeom getGeom(HitboxState state){
        return this.stateGeomMap.get(state);
    }

    /**
     * Gets the hitbox state of the entity
     * @param entity the entity
     * @return the hitbox state if it exists
     */
    public static HitboxCollectionState getHitboxState(Entity entity){
        if(!entity.containsKey(EntityDataStrings.HITBOX_DATA)){
            return null;
        }
        return (HitboxCollectionState)entity.getData(EntityDataStrings.HITBOX_DATA);
    }

    /**
     * Checks whether the entity has hitbox state or not
     * @param entity the entity to check
     * @return true if there is hitbox state, false otherwise
     */
    public static boolean hasHitboxState(Entity entity){
        return entity.containsKey(EntityDataStrings.HITBOX_DATA);
    }

    /**
     * Destroys the hitbox state and removes it from the entity
     * @param entity the entity
     * @param isServer true if this is the server, false otherwise
     * @return The hitbox state if it exists, null otherwise
     */
    public static HitboxCollectionState destroyHitboxState(Entity entity, boolean isServer){
        HitboxCollectionState state = null;
        if(HitboxCollectionState.hasHitboxState(entity)){
            state = HitboxCollectionState.getHitboxState(entity);
            state.manager.deregisterHitbox(state);
            state.destroy(isServer);
            entity.removeData(EntityDataStrings.HITBOX_DATA);
        }
        return state;
    }

    /**
     * Destroys the content of the state
     * @param true if this is the server, false otherwise
     */
    protected void destroy(boolean isServer){
        CollisionEngine engine = null;
        if(isServer){
            Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
            if(realm != null){
                engine = realm.getHitboxManager().getCollisionEngine();
            }
        } else {
            engine = Globals.clientState.clientSceneWrapper.getHitboxManager().getCollisionEngine();
        }
        if(engine != null && body != null){
            PhysicsUtils.destroyBody(engine, body);
        }
    }
    
    /**
     * Gets whether the hitbox state is active or not
     * @return true if active, false otherwise
     */
    public boolean isActive(){
        return active;
    }

    /**
     * Sets the active state of the hitbox
     * @param state true to make it active, false otherwise
     */
    public void setActive(boolean state){
        this.active = state;
        for(DGeom geom : this.geoms){
            HitboxState shapeState = this.getShapeStatus(geom);
            shapeState.setActive(state);
        }
    }

    /**
     * Gets the block override status
     * @return true if should override hitboxes with blockboxes, false otherwise
     */
    public boolean isBlockOverride(){
        return this.blockOverride;
    }

    /**
     * Sets the block override of the hitbox
     * @param state true to override attack hitboxes with block boxes, false otherwise
     */
    public void setBlockOverride(boolean state){
        this.blockOverride = state;
        for(DGeom geom : this.geoms){
            HitboxState shapeState = this.getShapeStatus(geom);
            shapeState.setBlockOverride(state);
        }
    }

    /**
     * Gets the list of all DGeoms in the data
     * @return the list of all DGeoms
     */
    public List<DGeom> getGeometries(){
        return this.geoms;
    }

    /**
     * Gets the list of all hitboxes
     * @return The list of all hitboxes
     */
    public List<HitboxState> getHitboxes(){
        return this.allStates;
    }

    /**
     * Gets the hitboxes associated with a bone
     * @param bone The bone
     * @return The list of hitboxes if at least one is present, null otherwise
     */
    public List<HitboxState> getHitboxes(String bone){
        return this.boneHitboxMap.get(bone);
    }

    /**
     * Adds a hitbox to a bone
     * @param bone The bone
     * @param state The hitbox
     */
    private void addHitbox(String bone, HitboxState state){
        if(this.boneHitboxMap.containsKey(bone)){
            this.boneHitboxMap.get(bone).add(state);
        } else {
            List<HitboxState> states = new LinkedList<HitboxState>();
            states.add(state);
            this.boneHitboxMap.put(bone,states);
        }
        this.allStates.add(state);
    }

    /**
     * Adds a hitbox with an offset
     * @param state The hitbox
     */
    private void addHitbox(HitboxState state){
        this.nonBoneHitboxes.add(state);
        this.allStates.add(state);
    }

    /**
     * Registers a geometry
     * @param geom The geom
     * @param state The state associated with the geom
     */
    private void registerGeom(DGeom geom, HitboxState state){
        if(state == null){
            throw new IllegalArgumentException("Null hitbox state provided to geometry register!");
        }
        this.geoms.add(geom);
        this.geomStateMap.put(geom,state);
        this.stateGeomMap.put(state,geom);
    }

    /**
     * Destroys a geometry
     * @param collisionEngine The collision engine for the body
     * @param geom The geometry
     */
    private void destroyGeom(CollisionEngine collisionEngine, DGeom geom){
        HitboxState state = this.geomStateMap.remove(geom);
        this.stateGeomMap.remove(state);
        this.geoms.remove(geom);
        CollisionBodyCreation.destroyShape(collisionEngine, geom);
    }

    /**
     * Gets the hitbox type of a given hitbox data
     * @param data The data
     * @return The type of hitbox
     */
    protected static HitboxType getType(HitboxData data){
        switch(data.getType()){
            case HitboxData.HITBOX_TYPE_HIT: {
                return HitboxType.HIT;
            }
            case HitboxData.HITBOX_TYPE_HURT: {
                return HitboxType.HURT;
            }
            case HitboxData.HITBOX_TYPE_HIT_CONNECTED: {
                return HitboxType.HIT;
            }
            case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {
                return HitboxType.HURT;
            }
            case HitboxData.HITBOX_TYPE_STATIC_CAPSULE: {
                return HitboxType.HURT;
            }
            default: {
                LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Trying to parse undefined hitbox type " + data.getType()));
                return HitboxType.HIT;
            }
        }
    }

    /**
     * Gets the shape type of a hitbox data object
     * @param data The data
     * @return The shape type
     */
    protected static HitboxShapeType getShapeType(HitboxData data){
        switch(data.getType()){
            case HitboxData.HITBOX_TYPE_HIT:
            case HitboxData.HITBOX_TYPE_HURT: {
                return HitboxShapeType.SPHERE;
            }
            case HitboxData.HITBOX_TYPE_HIT_CONNECTED:
            case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {
                return HitboxShapeType.CAPSULE;
            }
            case HitboxData.HITBOX_TYPE_STATIC_CAPSULE: {
                return HitboxShapeType.STATIC_CAPSULE;
            }
            default: {
                LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Trying to parse undefined hitbox shape type " + data.getType()));
                return HitboxShapeType.SPHERE;
            }
        }
    }

    /**
     * Gets the hitbox subtype
     * @param data The data
     * @return The subtype
     */
    protected static HitboxSubtype getSubType(HitboxData data){
        switch(data.getSubType()){
            case HitboxData.HITBOX_SUBTYPE_SWEET: {
                return HitboxSubtype.SWEET;
            }
            case HitboxData.HITBOX_SUBTYPE_REUGLAR: {
                return HitboxSubtype.REGULAR;
            }
            case HitboxData.HITBOX_SUBTYPE_SOUR: {
                return HitboxSubtype.SOUR;
            }
            default: {
                LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Trying to parse undefined hitbox subtype " + data.getSubType()));
                return HitboxSubtype.REGULAR;
            }
        }
    }

    /**
     * The status of a single shape inside the overall hitbox data
     * IE a single sphere on the overall body
     */
    public static class HitboxState {

        /**
         * Types of geometry that can be used as individual shapes within a hitbox
         */
        public enum HitboxShapeType {
            //this is a true sphere. It will teleport every frame to its new position
            SPHERE,
            //for this one, the shape is a capsule in the collision engine, however
            //the capsule is used to have continuity between the last position the hitbox occupied and the current one
            CAPSULE,
            //this is a true static capsule, it doesn't act as two connected spheres but is instead a capsule that teleports between frames
            STATIC_CAPSULE,
        }

        //the name of the bone the hitbox is attached to
        String boneName;

        //the type of hitbox
        HitboxType type;

        //the subtype
        HitboxSubtype subType;

        //the type of geometry
        HitboxShapeType shapeType;

        //controls whether the hitbox is active
        boolean isActive;

        //controls whether the block override is active or not
        //if it is active, hitboxes should behave like block boxes
        boolean blockOverride = false;

        //the previous position of this hitbox shape
        Vector3d previousWorldPos = null;

        //if true, just had a collision
        boolean hadCollision = false;

        //the data of the hitbox
        HitboxData data;

        /**
         * Creates a status object for a hitbox
         * @param boneName The name of the bone the hitbox is attached to, if any
         * @param data the hitbox data object
         * @param type The type of hitbox
         * @param subType The subtype of hitbox
         * @param shapeType The type of shape the hitbox is
         * @param isActive if the hitbox is active or not
         */
        public HitboxState(String boneName, HitboxData data, HitboxType type, HitboxSubtype subType, HitboxShapeType shapeType, boolean isActive){
            this.boneName = boneName;
            this.data = data;
            this.type = type;
            this.subType = subType;
            this.shapeType = shapeType;
            this.isActive = isActive;
        }

        /**
         * Gets the name of the bone the hitbox is attached to
         * @return The name of the bone
         */
        public String getBoneName(){
            return boneName;
        }

        /**
         * Sets the name of the bone the hitbox is attached to
         * @param boneName The bone name
         */
        public void setBoneName(String boneName){
            this.boneName = boneName;
        }

        /**
         * Gets the hitbox data for this shape
         * @return The data
         */
        public HitboxData getHitboxData(){
            return this.data;
        }

        /**
         * Sets the hitbox data for this shape
         * @param data The data
         */
        public void setHitboxData(HitboxData data){
            this.data = data;
        }

        /**
         * Gets the type of hitbox
         * @return The type
         */
        public HitboxType getType(){
            return type;
        }

        /**
         * Sets the type of hitbox
         * @param type The type
         */
        public void setType(HitboxType type){
            this.type = type;
        }

        /**
         * Gets the subtype of the hitbox
         * @return The subtype
         */
        public HitboxSubtype getSubType(){
            return subType;
        }

        /**
         * Sets the subtype of the hitbox
         * @param subType The subtype
         */
        public void setSubType(HitboxSubtype subType){
            this.subType = subType;
        }

        /**
         * Gets whether the hitbox is active or not
         * @return true if active, false otherwise
         */
        public boolean isActive(){
            return isActive;
        }

        /**
         * Sets whether the hitbox is active or not
         * @param active true for active, false otherwise
         */
        public void setActive(boolean active){
            this.isActive = active;
        }

        /**
         * Gets whether the block override is active or not
         * @return true if the block override is active, false otherwise
         */
        public boolean isBlockOverride(){
            return blockOverride;
        }

        /**
         * Sets whether the block override is active or not
         * @param blockOverride true if the block override is active, false otherwise
         */
        public void setBlockOverride(boolean blockOverride){
            this.blockOverride = blockOverride;
        }

        /**
         * Gets the previous world position of this hitbox
         * @return The previous world position
         */
        public Vector3d getPreviousWorldPos(){
            return this.previousWorldPos;
        }

        /**
         * sets the previous world position of this hitbox shape
         * @param previousWorldPos The previous world position
         */
        public void setPreviousWorldPos(Vector3d previousWorldPos){
            this.previousWorldPos = previousWorldPos;
        }

        /**
         * Sets the status of whether this hitbox just had a collision or not
         * @param hadCollision true if had a collision, false otherwise
         */
        public void setHadCollision(boolean hadCollision){
            this.hadCollision = hadCollision;
        }

        /**
         * Gets the collision status of the hitbox
         * @return true if had a collision, false otherwise
         */
        public boolean getHadCollision(){
            return this.hadCollision;
        }


    }

}
