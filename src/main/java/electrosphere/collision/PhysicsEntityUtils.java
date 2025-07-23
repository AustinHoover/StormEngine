package electrosphere.collision;

import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.state.collidable.ClientCollidableTree;
import electrosphere.entity.state.collidable.MultiShapeTriGeomData;
import electrosphere.entity.state.collidable.ServerCollidableTree;
import electrosphere.entity.state.collidable.TriGeomData;
import electrosphere.entity.state.gravity.ClientGravityTree;
import electrosphere.entity.state.gravity.ServerGravityTree;
import electrosphere.entity.state.physicssync.ClientPhysicsSyncTree;
import electrosphere.entity.state.physicssync.ServerPhysicsSyncTree;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;

/**
 * Utilities for performing physics-related operations on entities (particularly attaching physics to them in the first place)
 */
public class PhysicsEntityUtils {

    /**
     * The linear velocity threshold to disable under
     */
    static final double LINEAR_THRESHOLD = 3;

    /**
     * The angular velocity threshold to disable under
     */
    static final double ANGULAR_THRESHOLD = 3;

    /**
     * The number of steps below threshold to disable after
     */
    static final int STEP_THRESHOLD = 4;

    /**
     * How close to the edge to position an entity if it overruns the edge
     */
    static final double WORLD_MARGIN = 0.001;

    /**
     * Distance after which client does not generate collidables by default
     */
    public static final double CLIENT_LOD_DIST = 50;

    /**
     * [CLIENT ONLY] Attaches a collidable template to a given entity
     * @param rVal The entity
     * @param physicsTemplate The collidable template
     */
    public static void clientAttachCollidableTemplate(Entity rVal, CollidableTemplate physicsTemplate){
        Collidable collidable;
        double mass = 1.0f;
        if(physicsTemplate.getMass() != null){
            mass = physicsTemplate.getMass();
        }
        long categoryBit = Collidable.TYPE_CREATURE_BIT;
        if(physicsTemplate.getKinematic()){
            categoryBit = Collidable.TYPE_STATIC_BIT;
        }
        CollisionEngine.lockOde();
        if(physicsTemplate.getKinematic()){
            PhysicsEntityUtils.clientAttachGeom(rVal, physicsTemplate, EntityUtils.getPosition(rVal));
        } else {
            DBody rigidBody = null;
            switch(physicsTemplate.getType()){
                case CollidableTemplate.COLLIDABLE_TYPE_CYLINDER: {

                    //
                    //create dbody
                    rigidBody = CollisionBodyCreation.createCylinderBody(
                        Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                        physicsTemplate.getDimension1(),
                        physicsTemplate.getDimension2(),
                        categoryBit
                    );
                    if(physicsTemplate.getMass() != null){
                        CollisionBodyCreation.setCylinderMass(
                            Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                            rigidBody,
                            mass,
                            physicsTemplate.getDimension1(),
                            physicsTemplate.getDimension2(),
                            new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ()),
                            new Quaterniond(physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW())
                        );
                    }
                    CollisionBodyCreation.setAutoDisable(Globals.clientState.clientSceneWrapper.getCollisionEngine(), rigidBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);

                    //
                    //set offset from center of entity position
                    CollisionBodyCreation.setOffsetPosition(
                        Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                        rigidBody,
                        new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                    );

                    //
                    //create collidable and link to structures
                    collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                    if(physicsTemplate.getLinearFriction() != null){
                        collidable.getSurfaceParams().setLinearFriction(physicsTemplate.getLinearFriction());
                    }
                    if(physicsTemplate.getRollingFriction() != null){
                        collidable.getSurfaceParams().setRollingFriction(physicsTemplate.getRollingFriction());
                    }
                    ClientCollidableTree tree = new ClientCollidableTree(rVal,collidable,rigidBody);
                    PhysicsEntityUtils.setDBody(rVal,rigidBody);

                    //
                    //store values
                    Matrix4d offsetTransform = new Matrix4d().translationRotate(
                        physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                        physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW() //rotate
                    );
                    if(physicsTemplate.isAngularlyStatic()){
                        Globals.clientState.clientSceneWrapper.getCollisionEngine().setAngularlyStatic(rigidBody, true);
                    }
                    if(physicsTemplate.getKinematic()){
                        Globals.clientState.clientSceneWrapper.getCollisionEngine().setKinematic(rigidBody);
                        rigidBody.disable();
                    }
                    rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                    rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                    rVal.putData(EntityDataStrings.CLIENT_COLLIDABLE_TREE, tree);
                    rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);

                    Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(rVal));
                    Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.COLLIDABLE);
                } break;
                case CollidableTemplate.COLLIDABLE_TYPE_CUBE: {
                    //
                    //create dbody
                    rigidBody = CollisionBodyCreation.createCubeBody(
                        Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                        new Vector3d(physicsTemplate.getDimension1(),physicsTemplate.getDimension2(),physicsTemplate.getDimension3()),
                        categoryBit
                    );
                    if(physicsTemplate.getMass() != null){
                        CollisionBodyCreation.setBoxMass(
                            Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                            rigidBody,
                            mass,
                            new Vector3d(physicsTemplate.getDimension1(),physicsTemplate.getDimension2(),physicsTemplate.getDimension3()),
                            new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ()),
                            new Quaterniond(physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW())
                        );
                    }
                    CollisionBodyCreation.setAutoDisable(Globals.clientState.clientSceneWrapper.getCollisionEngine(), rigidBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);

                    //
                    //set offset from center of entity position
                    CollisionBodyCreation.setOffsetPosition(
                        Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                        rigidBody,
                        new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                    );

                    //
                    //create collidable and link to structures
                    collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                    if(physicsTemplate.getLinearFriction() != null){
                        collidable.getSurfaceParams().setLinearFriction(physicsTemplate.getLinearFriction());
                    }
                    if(physicsTemplate.getRollingFriction() != null){
                        collidable.getSurfaceParams().setRollingFriction(physicsTemplate.getRollingFriction());
                    }
                    ClientCollidableTree tree = new ClientCollidableTree(rVal,collidable,rigidBody);
                    PhysicsEntityUtils.setDBody(rVal,rigidBody);

                    //
                    //store values
                    Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                        physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                        physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                        1, 1, 1 //scale
                    );
                    if(physicsTemplate.isAngularlyStatic()){
                        Globals.clientState.clientSceneWrapper.getCollisionEngine().setAngularlyStatic(rigidBody, true);
                    }
                    if(physicsTemplate.getKinematic()){
                        Globals.clientState.clientSceneWrapper.getCollisionEngine().setKinematic(rigidBody);
                        rigidBody.disable();
                    }
                    rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                    rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                    rVal.putData(EntityDataStrings.CLIENT_COLLIDABLE_TREE, tree);
                    rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);

                    Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(rVal));
                    Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.COLLIDABLE);
                } break;
                case CollidableTemplate.COLLIDABLE_TYPE_CAPSULE: {
                    //
                    //create dbody
                    rigidBody = CollisionBodyCreation.createCapsuleBody(
                        Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                        physicsTemplate.getDimension1(),
                        physicsTemplate.getDimension2(),
                        categoryBit
                    );
                    if(physicsTemplate.getMass() != null){
                        CollisionBodyCreation.setCapsuleMass(
                            Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                            rigidBody,
                            mass,
                            physicsTemplate.getDimension1(),
                            physicsTemplate.getDimension2(),
                            new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ()),
                            new Quaterniond(physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW())
                        );
                    }
                    CollisionBodyCreation.setAutoDisable(Globals.clientState.clientSceneWrapper.getCollisionEngine(), rigidBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);

                    //
                    //set offset from center of entity position
                    CollisionBodyCreation.setOffsetPosition(
                        Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                        rigidBody,
                        new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                    );

                    //
                    //create collidable and link to structures
                    collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                    if(physicsTemplate.getLinearFriction() != null){
                        collidable.getSurfaceParams().setLinearFriction(physicsTemplate.getLinearFriction());
                    }
                    if(physicsTemplate.getRollingFriction() != null){
                        collidable.getSurfaceParams().setRollingFriction(physicsTemplate.getRollingFriction());
                    }
                    ClientCollidableTree tree = new ClientCollidableTree(rVal,collidable,rigidBody);
                    PhysicsEntityUtils.setDBody(rVal,rigidBody);

                    //
                    //store values
                    Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                        physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                        physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                        1, 1, 1 //scale
                    );
                    if(physicsTemplate.isAngularlyStatic()){
                        Globals.clientState.clientSceneWrapper.getCollisionEngine().setAngularlyStatic(rigidBody, true);
                    }
                    if(physicsTemplate.getKinematic()){
                        Globals.clientState.clientSceneWrapper.getCollisionEngine().setKinematic(rigidBody);
                        rigidBody.disable();
                    }
                    rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                    rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                    rVal.putData(EntityDataStrings.CLIENT_COLLIDABLE_TREE, tree);

                    rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);

                    Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(rVal));
                    Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.COLLIDABLE);
                } break;
            }
            //if we successfully attached the body, add a sync tree
            ClientPhysicsSyncTree.attachTree(rVal);
            if(ClientGravityTree.hasClientGravityTree(rVal)){
                ClientGravityTree.getClientGravityTree(rVal).updatePhysicsPair(PhysicsEntityUtils.getCollidable(rVal),PhysicsEntityUtils.getDBody(rVal));
            }
        }
        CollisionEngine.unlockOde();
    }

    /**
     * [SERVER ONLY] Attaches a collidable template to a given entity
     * @param rVal The entity
     * @param physicsTemplate The collidable template
     * @return The geometry object
     */
    public static DGeom clientAttachGeom(Entity rVal, CollidableTemplate physicsTemplate, Vector3d position){
        DGeom geom = null;
        Collidable collidable;
        double mass = 1.0f;
        long categoryBit = Collidable.TYPE_CREATURE_BIT;
        if(physicsTemplate.getKinematic()){
            categoryBit = Collidable.TYPE_STATIC_BIT;
        }
        CollisionEngine engine = Globals.clientState.clientSceneWrapper.getCollisionEngine();
        CollisionEngine.lockOde();
        switch(physicsTemplate.getType()){
            case CollidableTemplate.COLLIDABLE_TYPE_CYLINDER: {

                //
                //create dbody
                geom = CollisionBodyCreation.createCylinderShape(
                    engine, 
                    physicsTemplate.getDimension1(),
                    physicsTemplate.getDimension2(),
                    categoryBit
                );

                //
                //create collidable and link to structures
                collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                PhysicsEntityUtils.setDGeom(rVal, geom);

                //
                //store values
                Matrix4d offsetTransform = new Matrix4d().translationRotate(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW() //rotate
                );
                rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);

                Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.COLLIDABLE);
                engine.registerCollisionObject(geom, collidable, EntityUtils.getPosition(rVal));
            } break;
            case CollidableTemplate.COLLIDABLE_TYPE_CUBE: {
                //
                //create dbody
                geom = CollisionBodyCreation.createCubeShape(
                    Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                    new Vector3d(physicsTemplate.getDimension1(),physicsTemplate.getDimension2(),physicsTemplate.getDimension3()),
                    categoryBit
                );

                //
                //create collidable and link to structures
                collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                PhysicsEntityUtils.setDGeom(rVal,geom);

                //
                //store values
                Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                    1, 1, 1 //scale
                );
                rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);

                Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.COLLIDABLE);
                engine.registerCollisionObject(geom, collidable, EntityUtils.getPosition(rVal));
            } break;
            case CollidableTemplate.COLLIDABLE_TYPE_CAPSULE: {
                //
                //create dbody
                geom = CollisionBodyCreation.createCapsuleShape(
                    Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                    physicsTemplate.getDimension1(),
                    physicsTemplate.getDimension2(),
                    categoryBit
                );

                //
                //create collidable and link to structures
                collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                PhysicsEntityUtils.setDGeom(rVal,geom);

                //
                //store values
                Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                    1, 1, 1 //scale
                );
                rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);

                Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.COLLIDABLE);
                engine.registerCollisionObject(geom, collidable, EntityUtils.getPosition(rVal));
            } break;
        }
        //if we successfully attached the body, add a sync tree
        ClientPhysicsSyncTree.attachTree(rVal);
        if(ClientGravityTree.hasClientGravityTree(rVal)){
            ClientGravityTree.getClientGravityTree(rVal).updatePhysicsPair(PhysicsEntityUtils.getCollidable(rVal),PhysicsEntityUtils.getDBody(rVal));
        }
        CollisionEngine.unlockOde();
        return geom;
    }


    /**
     * [SERVER ONLY] Attaches a collidable template to a given entity
     * @param realm The realm the entity is inside of
     * @param rVal The entity
     * @param physicsTemplate The collidable template
     * @param position the position of the body
     */
    public static void serverAttachCollidableTemplate(Realm realm, Entity rVal, CollidableTemplate physicsTemplate, Vector3d position){
        Collidable collidable;
        double mass = 1.0f;
        if(physicsTemplate.getMass() != null){
            mass = physicsTemplate.getMass();
        }
        long categoryBit = Collidable.TYPE_CREATURE_BIT;
        if(physicsTemplate.getKinematic()){
            categoryBit = Collidable.TYPE_STATIC_BIT;
        }
        CollisionEngine.lockOde();
        if(physicsTemplate.getKinematic()){
            PhysicsEntityUtils.serverAttachGeom(realm,rVal,physicsTemplate,position);
        } else {
            DBody rigidBody = null;
            switch(physicsTemplate.getType()){
                case CollidableTemplate.COLLIDABLE_TYPE_CYLINDER: {

                    //
                    //create dbody
                    rigidBody = CollisionBodyCreation.createCylinderBody(
                        realm.getCollisionEngine(),
                        physicsTemplate.getDimension1(),
                        physicsTemplate.getDimension2(),
                        categoryBit
                    );
                    if(physicsTemplate.getMass() != null){
                        CollisionBodyCreation.setCylinderMass(
                            realm.getCollisionEngine(),
                            rigidBody,
                            mass,
                            physicsTemplate.getDimension1(),
                            physicsTemplate.getDimension2(),
                            new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ()),
                            new Quaterniond(physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW())
                        );
                    }
                    CollisionBodyCreation.setAutoDisable(realm.getCollisionEngine(), rigidBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);

                    //
                    //set offset from entity center
                    CollisionBodyCreation.setOffsetPosition(
                        realm.getCollisionEngine(),
                        rigidBody,
                        new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                    );

                    //
                    //create collidable and attach tracking
                    collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                    if(physicsTemplate.getLinearFriction() != null){
                        collidable.getSurfaceParams().setLinearFriction(physicsTemplate.getLinearFriction());
                    }
                    if(physicsTemplate.getRollingFriction() != null){
                        collidable.getSurfaceParams().setRollingFriction(physicsTemplate.getRollingFriction());
                    }
                    ServerCollidableTree tree = new ServerCollidableTree(rVal,collidable,rigidBody);
                    PhysicsEntityUtils.setDBody(rVal,rigidBody);

                    //
                    //store data
                    Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                        physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                        physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                        1, 1, 1 //scale
                    );
                    if(physicsTemplate.isAngularlyStatic()){
                        realm.getCollisionEngine().setAngularlyStatic(rigidBody, true);
                    }
                    if(physicsTemplate.getKinematic()){
                        realm.getCollisionEngine().setKinematic(rigidBody);
                        rigidBody.disable();
                    }
                    rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                    rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                    rVal.putData(EntityDataStrings.SERVER_COLLIDABLE_TREE, tree);
                    rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);


                    realm.getCollisionEngine().registerCollisionObject(rigidBody, collidable, position);
                    ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.COLLIDABLE);
                } break;
                case CollidableTemplate.COLLIDABLE_TYPE_CUBE: {
                    //
                    //create dbody
                    rigidBody = CollisionBodyCreation.createCubeBody(
                        realm.getCollisionEngine(),
                        new Vector3d(physicsTemplate.getDimension1(),physicsTemplate.getDimension2(),physicsTemplate.getDimension3()),
                        categoryBit
                    );
                    if(physicsTemplate.getMass() != null){
                        CollisionBodyCreation.setBoxMass(
                            realm.getCollisionEngine(),
                            rigidBody,
                            mass,
                            new Vector3d(physicsTemplate.getDimension1(),physicsTemplate.getDimension2(),physicsTemplate.getDimension3()),
                            new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ()),
                            new Quaterniond(physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW())
                        );
                    }
                    CollisionBodyCreation.setAutoDisable(realm.getCollisionEngine(), rigidBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);

                    //
                    //set offset from entity center
                    CollisionBodyCreation.setOffsetPosition(
                        realm.getCollisionEngine(),
                        rigidBody,
                        new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                    );

                    //
                    //create collidable and attach tracking
                    collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                    if(physicsTemplate.getLinearFriction() != null){
                        collidable.getSurfaceParams().setLinearFriction(physicsTemplate.getLinearFriction());
                    }
                    if(physicsTemplate.getRollingFriction() != null){
                        collidable.getSurfaceParams().setRollingFriction(physicsTemplate.getRollingFriction());
                    }
                    ServerCollidableTree tree = new ServerCollidableTree(rVal,collidable,rigidBody);
                    PhysicsEntityUtils.setDBody(rVal,rigidBody);

                    //
                    //store data
                    Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                        physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                        physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                        1, 1, 1 //scale
                    );
                    if(physicsTemplate.isAngularlyStatic()){
                        realm.getCollisionEngine().setAngularlyStatic(rigidBody, true);
                    }
                    if(physicsTemplate.getKinematic()){
                        realm.getCollisionEngine().setKinematic(rigidBody);
                        rigidBody.disable();
                    }
                    rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                    rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                    rVal.putData(EntityDataStrings.SERVER_COLLIDABLE_TREE, tree);
                    rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);


                    realm.getCollisionEngine().registerCollisionObject(rigidBody, collidable, position);
                    ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.COLLIDABLE);
                } break;
                case CollidableTemplate.COLLIDABLE_TYPE_CAPSULE: {
                    //
                    //create dbody
                    rigidBody = CollisionBodyCreation.createCapsuleBody(
                        realm.getCollisionEngine(),
                        physicsTemplate.getDimension1(),
                        physicsTemplate.getDimension2(),
                        categoryBit
                    );
                    if(physicsTemplate.getMass() != null){
                        CollisionBodyCreation.setCapsuleMass(
                            realm.getCollisionEngine(),
                            rigidBody,
                            mass,
                            physicsTemplate.getDimension1(),
                            physicsTemplate.getDimension2(),
                            new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ()),
                            new Quaterniond(physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW())
                        );
                    }
                    CollisionBodyCreation.setAutoDisable(realm.getCollisionEngine(), rigidBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);

                    //
                    //set offset from entity center
                    CollisionBodyCreation.setOffsetPosition(
                        realm.getCollisionEngine(),
                        rigidBody,
                        new Vector3d(physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ())
                    );

                    //
                    //create collidable and attach tracking
                    collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                    if(physicsTemplate.getLinearFriction() != null){
                        collidable.getSurfaceParams().setLinearFriction(physicsTemplate.getLinearFriction());
                    }
                    if(physicsTemplate.getRollingFriction() != null){
                        collidable.getSurfaceParams().setRollingFriction(physicsTemplate.getRollingFriction());
                    }
                    ServerCollidableTree tree = new ServerCollidableTree(rVal,collidable,rigidBody);
                    PhysicsEntityUtils.setDBody(rVal,rigidBody);

                    //
                    //store data
                    Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                        physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                        physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                        1, 1, 1 //scale
                    );
                    if(physicsTemplate.isAngularlyStatic()){
                        realm.getCollisionEngine().setAngularlyStatic(rigidBody, true);
                    }
                    if(physicsTemplate.getKinematic()){
                        realm.getCollisionEngine().setKinematic(rigidBody);
                        rigidBody.disable();
                    }
                    rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                    rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                    rVal.putData(EntityDataStrings.SERVER_COLLIDABLE_TREE, tree);
                    rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);


                    realm.getCollisionEngine().registerCollisionObject(rigidBody, collidable, position);
                    ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.COLLIDABLE);
                } break;
            }
            //if we successfully attached the body, add a sync tree
            ServerPhysicsSyncTree.attachTree(rVal);
            if(ServerGravityTree.hasServerGravityTree(rVal)){
                ServerGravityTree.getServerGravityTree(rVal).updatePhysicsPair(PhysicsEntityUtils.getCollidable(rVal),PhysicsEntityUtils.getDBody(rVal));
            }
        }
        CollisionEngine.unlockOde();
    }

    /**
     * [SERVER ONLY] Attaches a collidable template to a given entity
     * @param realm The realm the entity is inside of
     * @param rVal The entity
     * @param physicsTemplate The collidable template
     * @return The geometry object
     */
    public static DGeom serverAttachGeom(Realm realm, Entity rVal, CollidableTemplate physicsTemplate, Vector3d position){
        if(physicsTemplate == null){
            throw new Error("Physics template is null!");
        }
        Collidable collidable;
        double mass = 1.0f;
        long categoryBit = Collidable.TYPE_CREATURE_BIT;
        if(physicsTemplate.getKinematic()){
            categoryBit = Collidable.TYPE_STATIC_BIT;
        }
        CollisionEngine.lockOde();
        DGeom geom = null;
        switch(physicsTemplate.getType()){
            case CollidableTemplate.COLLIDABLE_TYPE_CYLINDER: {

                //
                //create dbody
                geom = CollisionBodyCreation.createCylinderShape(
                    realm.getCollisionEngine(),
                    physicsTemplate.getDimension1(),
                    physicsTemplate.getDimension2(),
                    categoryBit
                );

                //
                //create collidable and attach tracking
                collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                ServerCollidableTree tree = new ServerCollidableTree(rVal,collidable,geom);
                PhysicsEntityUtils.setDGeom(rVal, geom);

                //
                //store data
                Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                    1, 1, 1 //scale
                );
                rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                rVal.putData(EntityDataStrings.SERVER_COLLIDABLE_TREE, tree);
                rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);

                ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.COLLIDABLE);
                realm.getCollisionEngine().registerCollisionObject(geom, collidable, position);
            } break;
            case CollidableTemplate.COLLIDABLE_TYPE_CUBE: {
                //
                //create dbody
                geom = CollisionBodyCreation.createCubeShape(
                    realm.getCollisionEngine(),
                    new Vector3d(physicsTemplate.getDimension1(),physicsTemplate.getDimension2(),physicsTemplate.getDimension3()),
                    categoryBit
                );

                //
                //create collidable and attach tracking
                collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                ServerCollidableTree tree = new ServerCollidableTree(rVal,collidable,geom);
                PhysicsEntityUtils.setDGeom(rVal, geom);

                //
                //store data
                Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                    1, 1, 1 //scale
                );
                rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                rVal.putData(EntityDataStrings.SERVER_COLLIDABLE_TREE, tree);
                rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);

                ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.COLLIDABLE);
                realm.getCollisionEngine().registerCollisionObject(geom, collidable, position);
            } break;
            case CollidableTemplate.COLLIDABLE_TYPE_CAPSULE: {
                //
                //create dbody
                geom = CollisionBodyCreation.createCapsuleShape(
                    realm.getCollisionEngine(),
                    physicsTemplate.getDimension1(),
                    physicsTemplate.getDimension2(),
                    categoryBit
                );

                //
                //create collidable and attach tracking
                collidable = new Collidable(rVal, Collidable.TYPE_CREATURE, true);
                ServerCollidableTree tree = new ServerCollidableTree(rVal,collidable,geom);
                PhysicsEntityUtils.setDGeom(rVal, geom);

                //
                //store data
                Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
                    physicsTemplate.getOffsetX(), physicsTemplate.getOffsetY(), physicsTemplate.getOffsetZ(), //translate
                    physicsTemplate.getRotX(), physicsTemplate.getRotY(), physicsTemplate.getRotZ(), physicsTemplate.getRotW(), //rotate
                    1, 1, 1 //scale
                );
                rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
                rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
                rVal.putData(EntityDataStrings.SERVER_COLLIDABLE_TREE, tree);
                rVal.putData(EntityDataStrings.PHYSICS_MASS, mass);

                ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.COLLIDABLE);
                realm.getCollisionEngine().registerCollisionObject(geom, collidable, position);
            } break;
        }
        ServerPhysicsSyncTree.attachTree(rVal);
        if(ServerGravityTree.hasServerGravityTree(rVal)){
            ServerGravityTree.getServerGravityTree(rVal).updatePhysicsPair(PhysicsEntityUtils.getCollidable(rVal),PhysicsEntityUtils.getDBody(rVal));
        }
        CollisionEngine.unlockOde();
        return geom;
    }


    /**
     * [CLIENT ONLY] Given an entity and a terrain chunk description, create physics for the chunk and attach it to the entity
     * @param terrain The entity
     * @param data The terrain description
     * @return The rigid body created (note, attachment has already been performed)
     */
    public static void clientAttachTriGeomRigidBody(Entity terrain, TriGeomData data){
        CollisionEngine.lockOde();
        DBody terrainBody = CollisionBodyCreation.generateBodyFromTerrainData(Globals.clientState.clientSceneWrapper.getCollisionEngine(), data, Collidable.TYPE_STATIC_BIT);
        CollisionBodyCreation.setAutoDisable(Globals.clientState.clientSceneWrapper.getCollisionEngine(), terrainBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);
        Collidable collidable = new Collidable(terrain,Collidable.TYPE_STATIC, false);
        Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(terrainBody, collidable, EntityUtils.getPosition(terrain));
        PhysicsEntityUtils.setDBody(terrain,terrainBody);
        CollisionEngine.unlockOde();
        terrain.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
    }

    /**
     * [CLIENT ONLY] Given an entity and a terrain chunk description, create physics for the chunk and attach it to the entity
     * @param terrain The entity
     * @param data The terrain description
     * @return The rigid body created (note, attachment has already been performed)
     */
    public static void clientAttachTriGeomCollider(Entity terrain, TriGeomData data){
        CollisionEngine.lockOde();
        DGeom terrainGeom = CollisionBodyCreation.generateGeomFromTerrainData(Globals.clientState.clientSceneWrapper.getCollisionEngine(), data, Collidable.TYPE_STATIC_BIT);
        Collidable collidable = new Collidable(terrain,Collidable.TYPE_STATIC, true);
        PhysicsEntityUtils.setCollidable(terrain, collidable);
        Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(terrainGeom, collidable, EntityUtils.getPosition(terrain));
        CollisionEngine.unlockOde();
        PhysicsEntityUtils.setDGeom(terrain,terrainGeom);
    }

    /**
     * [CLIENT ONLY] Given an entity and a multi-shape trimesh description, create physics for the shapes and attach it to the entity
     * @param terrain The entity
     * @param data The trimesh description
     * @return The rigid body created (note, attachment has already been performed)
     */
    public static void clientAttachMultiShapeTriGeomRigidBody(Entity terrain, MultiShapeTriGeomData data){
        DBody terrainBody = CollisionBodyCreation.generateBodyFromMultiShapeMeshData(Globals.clientState.clientSceneWrapper.getCollisionEngine(), data, Collidable.TYPE_STATIC_BIT);
        CollisionBodyCreation.setAutoDisable(Globals.clientState.clientSceneWrapper.getCollisionEngine(), terrainBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);
        Collidable collidable = new Collidable(terrain,Collidable.TYPE_STATIC, false);
        Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(terrainBody, collidable, EntityUtils.getPosition(terrain));
        PhysicsEntityUtils.setDBody(terrain,terrainBody);
        terrain.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
    }

    /**
     * [CLIENT ONLY] Given an entity and a multi-shape trimesh description, create physics for the shapes and attach it to the entity
     * @param terrain The entity
     * @param data The trimesh description
     * @return The collider created (note, attachment has already been performed)
     */
    public static void clientAttachMultiShapeTriGeomCollider(Entity terrain, MultiShapeTriGeomData data){
        DGeom terrainBody = CollisionBodyCreation.generateColliderFromMultiShapeMeshData(Globals.clientState.clientSceneWrapper.getCollisionEngine(), data, Collidable.TYPE_STATIC_BIT);
        Collidable collidable = new Collidable(terrain,Collidable.TYPE_STATIC, false);
        PhysicsEntityUtils.setCollidable(terrain, collidable);
        Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(terrainBody, collidable, EntityUtils.getPosition(terrain));
        PhysicsEntityUtils.setDGeom(terrain,terrainBody);
    }


    /**
     * [SERVER ONLY] Given an entity and a terrain chunk description, create physics for the chunk and attach it to the entity
     * @param terrain The entity
     * @param data The terrain description
     * @return The rigid body created (note, attachment has already been performed)
     */
    public static DBody serverAttachTriGeomRigidBody(Entity terrain, TriGeomData data){
        Realm realm = Globals.serverState.realmManager.getEntityRealm(terrain);
        DBody terrainBody = CollisionBodyCreation.generateBodyFromTerrainData(realm.getCollisionEngine(),data,Collidable.TYPE_STATIC_BIT);
        CollisionBodyCreation.setAutoDisable(realm.getCollisionEngine(), terrainBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);
        
        realm.getCollisionEngine().registerCollisionObject(terrainBody, new Collidable(terrain,Collidable.TYPE_STATIC, false), EntityUtils.getPosition(terrain));
        PhysicsEntityUtils.setDBody(terrain,terrainBody);
        
        return terrainBody;
    }

    /**
     * [SERVER ONLY] Given an entity and a terrain chunk description, create physics for the chunk and attach it to the entity
     * @param terrain The entity
     * @param data The terrain description
     * @return The rigid body created (note, attachment has already been performed)
     */
    public static DGeom serverAttachTriGeomCollider(Entity terrain, TriGeomData data){
        Realm realm = Globals.serverState.realmManager.getEntityRealm(terrain);
        DGeom terrainCollider = CollisionBodyCreation.generateGeomFromTerrainData(realm.getCollisionEngine(),data,Collidable.TYPE_STATIC_BIT);
        
        Collidable collidable = new Collidable(terrain,Collidable.TYPE_STATIC, true);
        PhysicsEntityUtils.setCollidable(terrain, collidable);
        realm.getCollisionEngine().registerCollisionObject(terrainCollider, collidable, EntityUtils.getPosition(terrain));
        PhysicsEntityUtils.setDGeom(terrain,terrainCollider);
        
        return terrainCollider;
    }

    /**
     * [SERVER ONLY] Given an entity and a multi-shape trimesh description, create physics for the shapes and attach it to the entity
     * @param terrain The entity
     * @param data The trimesh description
     * @return The rigid body created (note, attachment has already been performed)
     */
    public static DBody serverAttachMultiShapeTriGeomRigidBody(Entity terrain, MultiShapeTriGeomData data){
        Realm realm = Globals.serverState.realmManager.getEntityRealm(terrain);
        DBody terrainBody = CollisionBodyCreation.generateBodyFromMultiShapeMeshData(realm.getCollisionEngine(),data,Collidable.TYPE_STATIC_BIT);
        CollisionBodyCreation.setAutoDisable(realm.getCollisionEngine(), terrainBody, true, LINEAR_THRESHOLD, ANGULAR_THRESHOLD, STEP_THRESHOLD);
        
        realm.getCollisionEngine().registerCollisionObject(terrainBody, new Collidable(terrain,Collidable.TYPE_STATIC, false), EntityUtils.getPosition(terrain));
        PhysicsEntityUtils.setDBody(terrain,terrainBody);
        
        return terrainBody;
    }

    /**
     * [SERVER ONLY] Given an entity and a multi-shape trimesh description, create physics for the shapes and attach it to the entity
     * @param terrain The entity
     * @param data The trimesh description
     * @return The rigid body created (note, attachment has already been performed)
     */
    public static DGeom serverAttachMultiShapeTriGeomCollider(Entity terrain, MultiShapeTriGeomData data){
        Realm realm = Globals.serverState.realmManager.getEntityRealm(terrain);
        DGeom terrainBody = CollisionBodyCreation.generateColliderFromMultiShapeMeshData(realm.getCollisionEngine(),data,Collidable.TYPE_STATIC_BIT);
        
        Collidable collidable = new Collidable(terrain,Collidable.TYPE_STATIC, false);
        PhysicsEntityUtils.setCollidable(terrain, collidable);
        realm.getCollisionEngine().registerCollisionObject(terrainBody, collidable, EntityUtils.getPosition(terrain));
        PhysicsEntityUtils.setDGeom(terrain,terrainBody);
        
        return terrainBody;
    }

    /**
     * Repositions all active physics-scoped entities on a given realm
     * @param collisionEngine The realm's collision engine
     */
    public static void serverRepositionEntities(Realm realm, CollisionEngine collisionEngine){
        List<Entity> toReposition = new LinkedList<Entity>();
        List<Collidable> collidableList = collisionEngine.getCollidables();
        if(collidableList == null){
            collisionEngine.getCollidables();
            throw new Error("Collision engine collidables are null!");
        }
        for(int i = 0; i < collidableList.size(); i++){
            Collidable collidable = collidableList.get(i);
            Entity entity = collidable.getParent();
            DBody body = PhysicsEntityUtils.getDBody(entity);
            if(body != null && body.isEnabled() && !body.isKinematic()){
                toReposition.add(entity);
            }
            DGeom geom = PhysicsEntityUtils.getDGeom(entity);
            if(geom != null && geom.getCategoryBits() != Collidable.TYPE_STATIC_BIT){
                toReposition.add(entity);
            }
        }
        ServerWorldData worldDat = realm.getServerWorldData();
        for(Entity parent : toReposition){
            Vector3d parentPos = EntityUtils.getPosition(parent);
            if(ServerWorldData.convertRealToChunkSpace(parentPos.x) >= worldDat.getWorldSizeDiscrete()){
                parentPos.x = ServerWorldData.convertWorldToReal(worldDat.getWorldSizeDiscrete()) - WORLD_MARGIN;
            }
            if(ServerWorldData.convertRealToChunkSpace(parentPos.y) >= worldDat.getWorldSizeDiscrete()){
                parentPos.y = ServerWorldData.convertWorldToReal(worldDat.getWorldSizeDiscrete()) - WORLD_MARGIN;
            }
            if(ServerWorldData.convertRealToChunkSpace(parentPos.z) >= worldDat.getWorldSizeDiscrete()){
                parentPos.z = ServerWorldData.convertWorldToReal(worldDat.getWorldSizeDiscrete()) - WORLD_MARGIN;
            }
            ServerEntityUtils.repositionEntity(parent,parentPos);
        }
    }

    /**
     * Destroys the physics attached to an entity without outright destroying the entity
     * @param entity The entity
     */
    public static void serverDestroyPhysics(Entity entity){
        Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
        realm.getCollisionEngine().destroyPhysics(entity);
        Vector3d entityPos = EntityUtils.getPosition(entity);
        ServerEntityUtils.repositionEntity(entity, entityPos);
    }

    /**
     * Gets the transform from parent entity position to rigid body
     * @param entity The entity
     * @return The transform
     */
    public static Matrix4d getEntityCollidableTransform(Entity entity){
        return (Matrix4d) entity.getData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM);
    }

    /**
     * Checks if the entity contains a dbody
     * @param entity the entity
     * @return true if contains, false otherwise
     */
    public static boolean containsDBody(Entity entity){
        return entity.containsKey(EntityDataStrings.PHYSICS_COLLISION_BODY);
    }

    /**
     * Sets the dbody on the entity
     * @param entity The entity
     * @param body The body
     */
    public static void setDBody(Entity entity, DBody body){
        if(body == null){
            throw new Error("Trying to set null DBody!");
        }
        entity.putData(EntityDataStrings.PHYSICS_COLLISION_BODY, body);
    }

    /**
     * Gets the DBody attached to an entity
     * @param entity the entity
     * @return The dbody if it exists, null otherwise
     */
    public static DBody getDBody(Entity entity){
        return (DBody)entity.getData(EntityDataStrings.PHYSICS_COLLISION_BODY);
    }

    /**
     * Checks if the entity contains a dgeom
     * @param entity the entity
     * @return true if contains, false otherwise
     */
    public static boolean containsDGeom(Entity entity){
        return entity.containsKey(EntityDataStrings.PHYSICS_GEOM);
    }

    /**
     * Sets the static geom on the entity
     * @param entity The entity
     * @param geom The static geom
     */
    public static void setDGeom(Entity entity, DGeom geom){
        if(geom == null){
            throw new Error("Trying to set null geom!");
        }
        entity.putData(EntityDataStrings.PHYSICS_GEOM, geom);
    }

    /**
     * Gets the geom attached to the entity
     * @param entity The entity
     * @return The geom if it exists, null otherwise
     */
    public static DGeom getDGeom(Entity entity){
        return (DGeom)entity.getData(EntityDataStrings.PHYSICS_GEOM);
    }

    /**
     * Clears the geom and body on an entity
     * @param entity The entity
     */
    public static void clearGeomAndBody(Entity entity){
        if(PhysicsEntityUtils.getCollidable(entity) != null){
            throw new Error("Trying to clear geom and body on an entity that still has a collidable!");
        }
        entity.removeData(EntityDataStrings.PHYSICS_COLLISION_BODY);
        entity.removeData(EntityDataStrings.PHYSICS_GEOM);
    }

    /**
     * Sets the position of a DGeom
     * @param collisionEngine the collision engine
     * @param geom the geometry
     */
    public static void setGeometryOffsetPosition(CollisionEngine collisionEngine, DGeom geom, Vector3d position, Quaterniond rotation){
        collisionEngine.setGeomOffsetTransform(geom, position, rotation);
    }

    /**
     * Gets the collidable attached to the entity
     * @param entity The entity
     * @return The collidable if it exists, null otherwise
     */
    public static Collidable getCollidable(Entity entity){
        return (Collidable)entity.getData(EntityDataStrings.PHYSICS_COLLIDABLE);
    }

    /**
     * Gets the collidable attached to the entity
     * @param entity The entity
     * @param collidable The collidable
     */
    public static void setCollidable(Entity entity, Collidable collidable){
        entity.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
    }

    /**
     * Enables a body
     * @param collisionEngine The collision engine
     * @param entity The entity which contains the body to enable
     */
    public static void enableBody(CollisionEngine collisionEngine, Entity entity){
        DBody body = PhysicsEntityUtils.getDBody(entity);
        if(body != null){
            PhysicsUtils.enableBody(collisionEngine, body);
        }
    }

    /**
     * Disables a body
     * @param collisionEngine The collision engine
     * @param entity The entity which contains the body to disable
     */
    public static void disableBody(CollisionEngine collisionEngine, Entity entity){
        DBody body = PhysicsEntityUtils.getDBody(entity);
        if(body != null){
            PhysicsUtils.disableBody(collisionEngine, body);
        }
    }

    /**
     * Gets the physics template for the entity
     * @param entity The entity
     * @return The template
     */
    public static CollidableTemplate getPhysicsTemplate(Entity entity){
        return (CollidableTemplate)entity.getData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE);
    }
    
}
