package electrosphere.entity.state.server;

import org.joml.Quaterniond;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.controls.CameraHandler;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.creature.CreatureUtils;


/**
 * Stores the direction that the server thinks the player is looking
 */
public class ServerPlayerViewDirTree implements BehaviorTree {

    //the last time this value was updated
    long lastUpdateTime = 0;

    //The yaw for the camera
    double yaw;

    //The pitch for the camera
    double pitch;

    //the parent entity
    Entity parent;

    @Override
    public void simulate(float deltaTime) {
        
    }

    /**
     * Constructor
     * @param parent
     */
    private ServerPlayerViewDirTree(Entity parent){
        this.parent = parent;
    }

    /**
     * Attaches a ServerPlayerViewDirTree to a given entity
     * @param entity The entity to add to
     */
    public static void attachServerPlayerViewDirTree(Entity entity){
        ServerPlayerViewDirTree tree = new ServerPlayerViewDirTree(entity);
        entity.putData(EntityDataStrings.TREE_SERVERPLAYERVIEWDIR, tree);
    }

    /**
     * Gets the server player view dir tree if it exists
     * @param entity The entity to get from
     * @return The ServerPlayerViewDirTree if it exists, null otherwise
     */
    public static ServerPlayerViewDirTree getTree(Entity entity){
        return (ServerPlayerViewDirTree)entity.getData(EntityDataStrings.TREE_SERVERPLAYERVIEWDIR);
    }

    /**
     * Checks whether the entity has a copy of this btree or not
     * @param entity The entity
     * @return true if the entity has the btree, false otherwise
     */
    public static boolean hasTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERPLAYERVIEWDIR);
    }

    /**
     * Gets the yaw
     * @return The yaw
     */
    public double getYaw(){
        return yaw;
    }

    /**
     * Gets the pitch
     * @return The pitch
     */
    public double getPitch(){
        return pitch;
    }

    /**
     * Sets the player view dir vector
     * @param playerViewDir The player view dir vector
     */
    public void setPlayerViewDir(int perspective, double yaw, double pitch, long time){
        if(time > lastUpdateTime){
            //set initial values
            this.yaw = yaw;
            this.pitch = pitch;

            //if first person, set facing angle
            if(perspective == CameraHandler.CAMERA_PERSPECTIVE_FIRST){
                CreatureUtils.setFacingVector(parent, CameraEntityUtils.getFacingVec(yaw, pitch));
                EntityUtils.getRotation(parent).set(CameraEntityUtils.getUprightQuat(yaw));
            }


            this.lastUpdateTime = time;
        }
    }

    /**
     * Gets the current rotation of the view dir tree
     * @return The current rotation of the view dir tree
     */
    public Quaterniond getRotationQuat(){
        return CameraEntityUtils.getRotationQuat(yaw,pitch);
    }
    
}
