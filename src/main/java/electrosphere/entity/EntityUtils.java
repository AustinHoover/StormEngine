package electrosphere.entity;

import electrosphere.engine.Globals;
import electrosphere.renderer.actor.Actor;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.entity.poseactor.PoseActor;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Utilties for dealing with entities
 */
public class EntityUtils {
    
    /**
     * Gets the position of the entity
     * @param e The entity
     * @return The position of the entity
     */
    public static Vector3d getPosition(Entity e){
        return (Vector3d)e.getData(EntityDataStrings.DATA_STRING_POSITION);
    }

    /**
     * Sets the position of the entity
     * @param e The entity
     * @param newVec The new position
     */
    public static void setPosition(Entity e, Vector3d newVec){
        ((Vector3d)e.getData(EntityDataStrings.DATA_STRING_POSITION)).set(newVec);
    }
    
    public static Quaterniond getRotation(Entity e){
        return (Quaterniond)e.getData(EntityDataStrings.DATA_STRING_ROTATION);
    }
    
    public static Vector3f getScale(Entity e){
        return (Vector3f)e.getData(EntityDataStrings.DATA_STRING_SCALE);
    }
    
    /**
     * Cleans up the entity and deregisters it from all tracking datastructures
     * @param e The entity to clean up
     */
    protected static void cleanUpEntity(Entity e){
        //remove from client
        if(Globals.clientState.clientSceneWrapper != null){
            Globals.clientState.clientSceneWrapper.getScene().deregisterEntity(e);
            Globals.clientState.clientSceneWrapper.deregisterTranslationMapping(e);
        }
        //remove from all server classes
        if(Globals.serverState.realmManager != null){
            Realm realm = Globals.serverState.realmManager.getEntityRealm(e);
            if(realm != null){
                //get data cell
                ServerDataCell dataCell = Globals.serverState.entityDataCellMapper.getEntityDataCell(e);
                if(dataCell != null){
                    dataCell.getScene().deregisterEntity(e);
                }
            }
            Globals.serverState.realmManager.removeEntity(e);
        }
        Globals.serverState.entityDataCellMapper.ejectEntity(e);
        EntityLookupUtils.removeEntity(e);
    }
    
    /**
     * Gets the actor on the entity
     * @param e The entity
     * @return The actor
     */
    public static Actor getActor(Entity e){
        return (Actor)e.getData(EntityDataStrings.DATA_STRING_ACTOR);
    }

    /**
     * Gets the pose actor on the entity
     * @param e The entity
     * @return the pose actor
     */
    public static PoseActor getPoseActor(Entity e){
        return (PoseActor)e.getData(EntityDataStrings.POSE_ACTOR);
    }
    
}
