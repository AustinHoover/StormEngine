package electrosphere.entity.state.physicssync;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.terrain.foliage.FoliageCellManager;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.parser.net.message.EntityMessage;

/**
 * Receives synchronization data from the client
 */
public class ClientPhysicsSyncTree implements BehaviorTree {
    
    /**
     * The parent entity for the tree
     */
    Entity parent;

    /**
     * The most recent message received from the client
     */
    EntityMessage latestMessage = null;

    /**
     * checks if the message has been pushed to the physics engine or not
     */
    boolean hasPushesMessage = true;

    /**
     * Constructor
     * @param parent
     * @param params
     */
    private ClientPhysicsSyncTree(Entity parent, Object ... params){
        this.parent = parent;
    }

    @Override
    public void simulate(float deltaTime) {
        if(!hasPushesMessage && latestMessage != null){
            this.hasPushesMessage = true;
            //
            //get values
            Vector3d position = new Vector3d(latestMessage.getpositionX(),latestMessage.getpositionY(),latestMessage.getpositionZ());
            Quaterniond rotationFromServer = new Quaterniond(latestMessage.getrotationX(),latestMessage.getrotationY(),latestMessage.getrotationZ(),latestMessage.getrotationW());
            Vector3d linearVelocity = new Vector3d(latestMessage.getlinVelX(), latestMessage.getlinVelY(), latestMessage.getlinVelZ());
            Vector3d angularVelocity = new Vector3d();
            Vector3d linearForce = new Vector3d(latestMessage.getlinForceX(), latestMessage.getlinForceY(), latestMessage.getlinForceZ());
            Vector3d angularForce = new Vector3d();
            boolean enabled = latestMessage.getbodyEnabled();
            DBody body = PhysicsEntityUtils.getDBody(parent);
            DGeom geom = PhysicsEntityUtils.getDGeom(parent);

            //
            //bust distance caches if this is the player's entity and we've traveled a long distance suddenly
            if(parent == Globals.clientState.playerEntity){
                if(position.distance(EntityUtils.getPosition(parent)) > FoliageCellManager.TELEPORT_DISTANCE){
                    if(Globals.clientState.clientDrawCellManager != null){
                        Globals.clientState.clientDrawCellManager.bustDistanceCache();
                    }
                    if(Globals.clientState.foliageCellManager != null){
                        Globals.clientState.foliageCellManager.bustDistanceCache();
                    }
                }
            }

            //
            //Synchronize data
            EntityUtils.setPosition(parent, position);
            EntityUtils.getRotation(parent).set(rotationFromServer);
            if(body != null){
                PhysicsUtils.synchronizeData(Globals.clientState.clientSceneWrapper.getCollisionEngine(), body, position, rotationFromServer, linearVelocity, angularVelocity, linearForce, angularForce, enabled);
            }
            if(geom != null){
                PhysicsUtils.setGeomTransform(Globals.clientState.clientSceneWrapper.getCollisionEngine(), position, rotationFromServer, geom);
            }

            //
            //update facing vector if relevant
            if(CreatureUtils.getFacingVector(parent) != null){
                CreatureUtils.setFacingVector(parent, CameraEntityUtils.getFacingVec(rotationFromServer));
            }
        }
    }

    /**
     * Sets the message for this sync tree
     * @param syncMessage The synchronization message received from the server
     */
    public void setMessage(EntityMessage syncMessage){
        if(this.latestMessage == null){
            this.latestMessage = syncMessage;
            this.hasPushesMessage = false;
        } else if(this.latestMessage.gettime() < syncMessage.gettime()){
            this.latestMessage = syncMessage;
            this.hasPushesMessage = false;
        }
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ClientPhysicsSyncTree attachTree(Entity parent, Object ... params){
        ClientPhysicsSyncTree rVal = new ClientPhysicsSyncTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTPHYSICSSYNCTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        return rVal;
    }

    /**
     * <p> Automatically generated </p>
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
        return entity.containsKey(EntityDataStrings.TREE_CLIENTPHYSICSSYNCTREE);
    }

    /**
     * Gets the client physics sync tree on the entity
     * @param entity The entity
     * @return The tree if it exists, null otherwise
     */
    public static ClientPhysicsSyncTree getTree(Entity entity){
        return (ClientPhysicsSyncTree)entity.getData(EntityDataStrings.TREE_CLIENTPHYSICSSYNCTREE);
    }

}

