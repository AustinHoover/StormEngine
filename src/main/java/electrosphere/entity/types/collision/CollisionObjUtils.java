package electrosphere.entity.types.collision;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

import electrosphere.collision.CollisionEngine;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.server.datacell.Realm;

/**
 * Collision object utility functions
 */
public class CollisionObjUtils {

    /**
     * Attach a collision object to a provided entity
     * @param entity The entity to attach a collision object to
     * @param collisionObject The jBulletF collision object to attach to the entity
     * @param mass The mass of the collidable
     * @param collidableType The type of collidable we are attaching. For instance, "Terrain", "Creature", "Item", etc. Refer to Collidable class for options.
     */
    public static void clientAttachCollisionObjectToEntity(Entity entity, DBody collisionObject, float mass, String collidableType){
        Vector3d position = EntityUtils.getPosition(entity);
        Quaterniond rotation = EntityUtils.getRotation(entity);
        Collidable collidable = new Collidable(entity, collidableType, true);
        Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(collisionObject, collidable, EntityUtils.getPosition(entity));

        PhysicsEntityUtils.setDBody(entity, collisionObject);

        //update world transform of collision object
        clientPositionCharacter(entity,position,rotation);
        
        entity.putData(EntityDataStrings.COLLISION_ENTITY_COLLISION_OBJECT, collisionObject);
        entity.putData(EntityDataStrings.COLLISION_ENTITY_COLLIDABLE, collidable);
        entity.putData(EntityDataStrings.PHYSICS_MASS, mass);
    }

    /**
     * Attach a collision object to a provided entity
     * @param entity The entity to attach a collision object to
     * @param collisionObject The jBulletF collision object to attach to the entity
     * @param mass The mass of the collidable
     * @param collidableType The type of collidable we are attaching. For instance, "Terrain", "Creature", "Item", etc. Refer to Collidable class for options.
     */
    public static void serverAttachCollisionObjectToEntity(Entity entity, DBody collisionObject, float mass, String collidableType){
        Vector3d position = EntityUtils.getPosition(entity);
        Collidable collidable = new Collidable(entity, collidableType, true);
        Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
        realm.getCollisionEngine().registerCollisionObject(collisionObject, collidable, EntityUtils.getPosition(entity));

        PhysicsEntityUtils.setDBody(entity, collisionObject);

        //update world transform of collision object
        serverPositionCharacter(entity,position);
        
        entity.putData(EntityDataStrings.COLLISION_ENTITY_COLLISION_OBJECT, collisionObject);
        entity.putData(EntityDataStrings.COLLISION_ENTITY_COLLIDABLE, collidable);
        entity.putData(EntityDataStrings.PHYSICS_MASS, mass);
    }
    
    /**
     * Repositions an entity on the server
     * @param e The entity
     * @param position The server
     */
    public static void serverPositionCharacter(Entity e, Vector3d position){
        CollisionEngine.lockOde();
        double startX = position.x;
        double startY = position.y;
        double startZ = position.z;
        EntityUtils.setPosition(e, position);
        Vector3d entPos = EntityUtils.getPosition(e);
        if(startX != entPos.x || startX != entPos.x || startX != entPos.x){
            throw new Error("Failed to position entity! " + startX + "," + startY + "," + startZ + "   " + entPos.x + "," + entPos.y + "," + entPos.z);
        }
        Quaterniond rotation = EntityUtils.getRotation(e);
        DBody body = PhysicsEntityUtils.getDBody(e);
        DGeom geom = PhysicsEntityUtils.getDGeom(e);
        CollisionEngine collisionEngine = Globals.serverState.realmManager.getEntityRealm(e).getCollisionEngine();
        if(body != null){
            PhysicsUtils.setRigidBodyTransform(collisionEngine, position, rotation, body);
        }
        if(geom != null){
            CollidableTemplate template = PhysicsEntityUtils.getPhysicsTemplate(e);
            if(template == null){
                PhysicsUtils.setGeomTransform(
                    collisionEngine,
                    new Vector3d(position),
                    rotation,
                    geom
                );
            } else {
                PhysicsUtils.setGeomTransform(
                    collisionEngine,
                    new Vector3d(position).add(template.getOffsetX(),template.getOffsetY(),template.getOffsetZ()),
                    rotation,
                    geom
                );
            }
        }
        // Vector3d finalRef = EntityUtils.getPosition(e);
        // if(finalRef != entPos){
        //     throw new Error("Reference changed while positioning character! " + entPos.x + "," + entPos.y + "," + entPos.z + "  " + finalRef.x + "," + finalRef.y + "," + finalRef.z);
        // }
        if(startX != entPos.x || startX != entPos.x || startX != entPos.x){
            throw new Error("Position not preserved while positioning entity! " + startX + "," + startY + "," + startZ + "   " + entPos.x + "," + entPos.y + "," + entPos.z + "   " + position.x + "," + position.y + "," + position.z);
        }
        CollisionEngine.unlockOde();
    }

    /**
     * Positions an entity on the client
     * @param e The entity
     * @param position The position
     * @param rotation The rotation
     */
    public static void clientPositionCharacter(Entity e, Vector3d position, Quaterniond rotation){
        EntityUtils.setPosition(e, position);
        DBody body = PhysicsEntityUtils.getDBody(e);
        if(body != null){
            PhysicsUtils.setRigidBodyTransform(Globals.clientState.clientSceneWrapper.getCollisionEngine(), position, rotation, body);
        }
        DGeom geom = PhysicsEntityUtils.getDGeom(e);
        if(geom != null){
            CollidableTemplate template = PhysicsEntityUtils.getPhysicsTemplate(e);
            if(template == null){
                PhysicsUtils.setGeomTransform(
                    Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                    new Vector3d(position),
                    rotation,
                    geom
                );
            } else {
                PhysicsUtils.setGeomTransform(
                    Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                    new Vector3d(position).add(template.getOffsetX(),template.getOffsetY(),template.getOffsetZ()),
                    rotation,
                    geom
                );
            }
        }
    }
    
    public static Collidable getCollidable(Entity e){
        return (Collidable)e.getData(EntityDataStrings.PHYSICS_COLLIDABLE);
    }

    public static float getMass(Entity e){
        return (float)e.getData(EntityDataStrings.PHYSICS_MASS);
    }

    
}
