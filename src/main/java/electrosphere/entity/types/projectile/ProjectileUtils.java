package electrosphere.entity.types.projectile;

import java.util.LinkedList;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

import electrosphere.collision.hitbox.HitboxUtils.HitboxPositionCallback;
import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.data.entity.projectile.ProjectileType;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.movement.ProjectileTree;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.util.math.SpatialMathUtils;

public class ProjectileUtils {
    
    /**
     * Spawns a basic projectile entity on the client
     * @param model The model
     * @param initialPosition The initial position 
     * @param rotation The rotation
     * @param maxLife The maximum life
     * @param initialVector The initial vector
     * @param velocity The velocity
     * @return The projectile entity
     */
    public static Entity clientSpawnBasicProjectile(String model, Vector3d initialPosition, Quaterniond rotation, int maxLife, Vector3f initialVector, float velocity){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(rVal, model);
        Globals.assetManager.addModelPathToQueue(model);
        ProjectileTree tree = new ProjectileTree(rVal,maxLife,new Vector3d(initialVector),velocity);
        EntityUtils.getPosition(rVal).set(initialPosition);
        EntityUtils.getRotation(rVal).rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(initialVector.x,initialVector.y,initialVector.z)).normalize();
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(tree);
        return rVal;
    }

    /**
     * Spawns a basic projectile entity on the server
     * @param model The model
     * @param initialPosition The initial position 
     * @param rotation The rotation
     * @param maxLife The maximum life
     * @param initialVector The initial vector
     * @param velocity The velocity
     * @return The projectile entity
     */
    public static Entity serverSpawnBasicProjectile(Realm realm, String model, Vector3d initialPosition, Quaterniond rotation, int maxLife, Vector3f initialVector, float velocity){
        Entity rVal = EntityCreationUtils.createServerEntity(realm, initialPosition);
        Globals.assetManager.addModelPathToQueue(model);
        ProjectileTree tree = new ProjectileTree(rVal,maxLife,new Vector3d(initialVector),velocity);
        EntityUtils.getPosition(rVal).set(initialPosition);
        // EntityUtils.getRotation(currentEntity).rotationTo(MathUtils.ORIGIN_VECTORF, new Vector3f((float)facingAngle.x,(float)facingAngle.y,(float)facingAngle.z)).mul(parentActor.getBoneRotation(targetBone)).normalize();
        EntityUtils.getRotation(rVal).rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(initialVector.x,initialVector.y,initialVector.z)).normalize();
        // ParticleTree particleTree = new ParticleTree(rVal, maxLife, destination, velocity, acceleration, true);
        // rVal.putData(EntityDataStrings.PARTICLE_TREE, particleTree);
        // rVal.putData(EntityDataStrings.IS_PARTICLE, true);
        ServerBehaviorTreeUtils.attachBTreeToEntity(rVal, tree);
        return rVal;
    }

    /**
     * More sophisticated function for spawning projectiles. Uses the projectiles.json file to store data about types to spawn.
     * Also filters the parent from being hit by their own projectiles.
     * @param projectileType The type in projectiles.json to spawn
     * @param initialPosition The initial position of the projectile
     * @param initialVector The initial velocity of the projectile
     * @param parent The parent that fired said projectile
     * @return The projectile entity
     */
    public static Entity clientSpawnProjectile(String projectileType, Vector3d initialPosition, Vector3d initialVector, Entity parent){
        ProjectileType rawType = Globals.gameConfigCurrent.getProjectileMap().getType(projectileType);
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(rVal, rawType.getModelPath());
        //initial coordinates
        EntityUtils.getRotation(rVal).rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(initialVector.x,initialVector.y,initialVector.z)).normalize();
        EntityUtils.getPosition(rVal).set(initialPosition);
        //projectile behavior tree
        ProjectileTree tree = new ProjectileTree(rVal,rawType.getMaxLife(),initialVector,rawType.getVelocity(), rawType.getDamage());
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(tree);
        ProjectileTree.setProjectileTree(rVal, tree);
        //filter construction
        List<Entity> filter = new LinkedList<Entity>();
        filter.add(parent);
        //collidable
        HitboxData hitboxData = new HitboxData();
        hitboxData.setRadius(rawType.getHitboxRadius());
        HitboxCollectionState.attachHitboxStateWithCallback(Globals.clientState.clientSceneWrapper.getHitboxManager(), Globals.clientState.clientSceneWrapper.getCollisionEngine(), rVal, hitboxData, 
        new HitboxPositionCallback() {
                public Vector3d getPosition(){
                    return EntityUtils.getPosition(rVal);
                }
            }
        );
        return rVal;
    }

    /**
     * More sophisticated function for spawning projectiles. Uses the projectiles.json file to store data about types to spawn.
     * Also filters the parent from being hit by their own projectiles.
     * @param projectileType The type in projectiles.json to spawn
     * @param initialPosition The initial position of the projectile
     * @param initialVector The initial velocity of the projectile
     * @param parent The parent that fired said projectile
     * @return The projectile entity
     */
    public static Entity serverSpawnProjectile(Realm realm, String projectileType, Vector3d initialPosition, Vector3d initialVector, Entity parent){
        ProjectileType rawType = Globals.gameConfigCurrent.getProjectileMap().getType(projectileType);
        Entity rVal = EntityCreationUtils.createServerEntity(realm, initialPosition);
        //initial coordinates
        EntityUtils.getRotation(rVal).rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(initialVector.x,initialVector.y,initialVector.z)).normalize();
        EntityUtils.getPosition(rVal).set(initialPosition);
        //projectile behavior tree
        ProjectileTree tree = new ProjectileTree(rVal,rawType.getMaxLife(),initialVector,rawType.getVelocity(), rawType.getDamage());
        ServerBehaviorTreeUtils.attachBTreeToEntity(rVal, tree);
        ProjectileTree.setProjectileTree(rVal, tree);
        //filter construction
        List<Entity> filter = new LinkedList<Entity>();
        filter.add(parent);
        //collidable
        HitboxData hitboxData = new HitboxData();
        hitboxData.setRadius(rawType.getHitboxRadius());
        HitboxCollectionState.attachHitboxStateWithCallback(realm.getHitboxManager(), realm.getCollisionEngine(), rVal, hitboxData, 
        new HitboxPositionCallback() {
                public Vector3d getPosition(){
                    return EntityUtils.getPosition(rVal);
                }
            }
        );


        //position entity
        //this needs to be called at the end of this function.
        //Burried underneath this is function call to initialize a server side entity.
        //The server initialization logic checks what type of entity this is, if this function is called prior to its type being stored
        //the server will not be able to synchronize it properly.
        ServerEntityUtils.initiallyPositionEntity(realm,rVal,initialPosition);

        return rVal;
    }

}
