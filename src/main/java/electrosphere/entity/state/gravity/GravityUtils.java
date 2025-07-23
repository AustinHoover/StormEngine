package electrosphere.entity.state.gravity;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;

public class GravityUtils {
    
    public static void clientAttemptActivateGravity(Entity target){
        if(target.containsKey(EntityDataStrings.GRAVITY_ENTITY) && PhysicsEntityUtils.containsDBody(target)){
            ClientGravityTree tree = ClientGravityTree.getClientGravityTree(target);
            tree.start();
        }
    }

    public static void serverAttemptActivateGravity(Entity target){
        if(target.containsKey(EntityDataStrings.GRAVITY_ENTITY) && PhysicsEntityUtils.containsDBody(target)){
            ServerGravityTree tree = ServerGravityTree.getServerGravityTree(target);
            tree.start();
        }
    }

    public static void clientAttemptDeactivateGravity(Entity target){
        if(target.containsKey(EntityDataStrings.GRAVITY_ENTITY)){
            ClientGravityTree tree = ClientGravityTree.getClientGravityTree(target);
            tree.stop();
        }
    }

    public static void serverAttemptDeactivateGravity(Entity target){
        if(target.containsKey(EntityDataStrings.GRAVITY_ENTITY)){
            ServerGravityTree tree = ServerGravityTree.getServerGravityTree(target);
            tree.stop();
        }
    }

}