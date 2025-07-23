package electrosphere.entity.state.physicssync;

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
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;

/**
 * Synchronized physics state to client
 */
public class ServerPhysicsSyncTree implements BehaviorTree {

    //The parent entity for the tree
    Entity parent;

    //the last sent transforms
    Vector3d lastSentPosition = new Vector3d();
    Quaterniond lastSentRotation = new Quaterniond();

    //threshold of position/rotation difference after which a sync packet is sent to client
    static final double UPDATE_THRESHOLD = 0.01;

    /**
     * Constructor
     * @param parent
     * @param params
     */
    private ServerPhysicsSyncTree(Entity parent, Object ... params){
        this.parent = parent;
    }

    @Override
    public void simulate(float deltaTime) {
        Vector3d position = EntityUtils.getPosition(parent);
        Quaterniond rotation = EntityUtils.getRotation(parent);
        DBody body = PhysicsEntityUtils.getDBody(parent);
        DGeom geom = PhysicsEntityUtils.getDGeom(parent);
        if(body != null){
            //velocities
            Vector3d linearVel = PhysicsUtils.odeVecToJomlVec(body.getLinearVel());
            Vector3d angularVel = PhysicsUtils.odeVecToJomlVec(body.getAngularVel());
            //forces
            Vector3d linearForce = PhysicsUtils.odeVecToJomlVec(body.getForce());
            Vector3d angularForce = PhysicsUtils.odeVecToJomlVec(body.getTorque());
            if(position.distance(lastSentPosition) > UPDATE_THRESHOLD || 1.0 - rotation.dot(lastSentRotation) > UPDATE_THRESHOLD){
                lastSentPosition.set(position);
                lastSentRotation.set(rotation);
                DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                    EntityMessage.constructsyncPhysicsMessage(
                        parent.getId(),
                        Globals.engineState.timekeeper.getNumberOfSimFramesElapsed(),
                        position.x,
                        position.y,
                        position.z,
                        rotation.x,
                        rotation.y,
                        rotation.z,
                        rotation.w,
                        linearVel.x,
                        linearVel.y,
                        linearVel.z,
                        angularVel.x,
                        angularVel.y,
                        angularVel.z,
                        linearForce.x,
                        linearForce.y,
                        linearForce.z,
                        angularForce.x,
                        angularForce.y,
                        angularForce.z,
                        body.isEnabled()
                    )
                );
            }
        }
        if(geom != null){
            if(position.distance(lastSentPosition) > UPDATE_THRESHOLD || 1.0 - rotation.dot(lastSentRotation) > UPDATE_THRESHOLD){
                lastSentPosition.set(position);
                lastSentRotation.set(rotation);
                DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(
                    EntityMessage.constructsyncPhysicsMessage(
                        parent.getId(),
                        Globals.engineState.timekeeper.getNumberOfSimFramesElapsed(),
                        position.x,
                        position.y,
                        position.z,
                        rotation.x,
                        rotation.y,
                        rotation.z,
                        rotation.w,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        false
                    )
                );
            }
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
    public static ServerPhysicsSyncTree attachTree(Entity parent, Object ... params){
        ServerPhysicsSyncTree rVal = new ServerPhysicsSyncTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERPHYSICSSYNCTREE, rVal);
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
        ServerBehaviorTreeUtils.detatchBTreeFromEntity(entity, tree);
    }

    /**
     * Returns whether the entity has a physics sync tree
     * @param entity The entity to check
     * @return True if the entity contains a physics sync tree, false otherwise
     */
    public static boolean hasTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERPHYSICSSYNCTREE);
    }

    /**
     * Gets the server physics sync tree on the entity
     * @param entity The entity
     * @return The tree if it exists, null otherwise
     */
    public static ServerPhysicsSyncTree getTree(Entity entity){
        return (ServerPhysicsSyncTree)entity.getData(EntityDataStrings.TREE_SERVERPHYSICSSYNCTREE);
    }
    
}
