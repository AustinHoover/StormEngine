package electrosphere.entity.state.physicssync.upright;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.physicssync.ServerPhysicsSyncTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.util.math.SpatialMathUtils;

public class ServerAlwaysUprightTree implements BehaviorTree {

    //The parent entity for the tree
    Entity parent;

    /**
     * Constructor
     * @param parent
     * @param params
     */
    private ServerAlwaysUprightTree(Entity parent, Object ... params){
        this.parent = parent;
    }

    @Override
    public void simulate(float deltaTime) {
        DBody body = PhysicsEntityUtils.getDBody(parent);
        if(body != null){
            Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
            Vector3d position = EntityUtils.getPosition(parent);
            Quaterniond sourceRotation = new Quaterniond(EntityUtils.getRotation(parent));
            Vector3d linearVelocity = PhysicsUtils.odeVecToJomlVec(body.getLinearVel());
            Vector3d angularVelocity = new Vector3d();
            Vector3d linearForce = PhysicsUtils.odeVecToJomlVec(body.getForce());
            Vector3d angularForce = new Vector3d();

            //make sure rotation is vertical
            // sourceRotation = sourceRotation.mul(0.001, 0.001, 0.001, 1).normalize();
            //calculate rotation based on facing vector
            if(CreatureUtils.getFacingVector(parent) != null){
                Vector3d facingVector = CreatureUtils.getFacingVector(parent);
                sourceRotation = new Quaterniond().rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(facingVector.x,0,facingVector.z)).normalize();
            }

            EntityUtils.setPosition(parent, position);
            EntityUtils.getRotation(parent).set(sourceRotation);
            PhysicsUtils.synchronizeData(realm.getCollisionEngine(), body, position, sourceRotation, linearVelocity, angularVelocity, linearForce, angularForce);
        }
        DGeom geom = PhysicsEntityUtils.getDGeom(parent);
        if(geom != null){
            Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
            Vector3d position = EntityUtils.getPosition(parent);
            Quaterniond sourceRotation = new Quaterniond(EntityUtils.getRotation(parent));

            //make sure rotation is vertical
            // sourceRotation = sourceRotation.mul(0.001, 0.001, 0.001, 1).normalize();
            //calculate rotation based on facing vector
            if(CreatureUtils.getFacingVector(parent) != null){
                Vector3d facingVector = CreatureUtils.getFacingVector(parent);
                sourceRotation = new Quaterniond().rotationTo(SpatialMathUtils.getOriginVector(), new Vector3d(facingVector.x,0,facingVector.z)).normalize();
            }

            EntityUtils.setPosition(parent, position);
            EntityUtils.getRotation(parent).set(sourceRotation);
            PhysicsUtils.setGeomTransform(realm.getCollisionEngine(), position, sourceRotation, geom);
        }
    }

    /**
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ServerAlwaysUprightTree attachTree(Entity parent, Object ... params){
        ServerAlwaysUprightTree rVal = new ServerAlwaysUprightTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERALWAYSUPRIGHTTREE, rVal);
        return rVal;
    }

    /**
     * <p>
     * Detatches this tree from the entity.
     * </p>
     * @param entity The entity to detach to
     * @param tree The behavior tree to detach
     */
    public static void detachTree(Entity entity, BehaviorTree tree){
    }

    /**
     * Returns whether the entity has a physics sync tree
     * @param entity The entity to check
     * @return True if the entity contains a physics sync tree, false otherwise
     */
    public static boolean hasTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERALWAYSUPRIGHTTREE);
    }

    /**
     * Gets the server physics sync tree on the entity
     * @param entity The entity
     * @return The tree if it exists, null otherwise
     */
    public static ServerPhysicsSyncTree getTree(Entity entity){
        return (ServerPhysicsSyncTree)entity.getData(EntityDataStrings.TREE_SERVERALWAYSUPRIGHTTREE);
    }
    
}
