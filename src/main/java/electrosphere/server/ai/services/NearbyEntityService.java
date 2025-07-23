package electrosphere.server.ai.services;

import java.util.Collection;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.AI;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.datacell.Realm;

/**
 * Gets the entities that are near this entity
 */
public class NearbyEntityService implements AIService {

    /**
     * Distance to search for nearby entities
     */
    static final double SEARCH_DIST = 32;

    @Override
    public void exec(){
        for(AI ai : Globals.serverState.aiManager.getAIList()){
            if(!ai.shouldExecute()){
                continue;
            }
            Entity entity = ai.getParent();
            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            if(realm != null){
                Vector3d position = EntityUtils.getPosition(entity);
                Collection<Entity> nearbyEntities = realm.getDataCellManager().entityLookup(position, NearbyEntityService.SEARCH_DIST);
                NearbyEntityService.setNearbyEntities(ai.getBlackboard(), nearbyEntities);
            }
        }
    }

    /**
     * Sets the nearby entities
     * @param blackboard The blackboard
     * @param entities The nearby entities
     */
    public static void setNearbyEntities(Blackboard blackboard, Collection<Entity> entities){
        blackboard.put(BlackboardKeys.NEARBY_ENTITIES, entities);
    }

    /**
     * Gets the nearby entities
     * @param blackboard The blackboard
     * @return The nearby entities
     */
    @SuppressWarnings("unchecked")
    public static Collection<Entity> getNearbyEntities(Blackboard blackboard){
        return (Collection<Entity>)blackboard.get(BlackboardKeys.NEARBY_ENTITIES);
    }

    /**
     * Checks if the blackboard stores nearby entities
     * @param blackboard The blackboard
     * @return true if it stores nearby entities, false otherwise
     */
    public static boolean hasNearbyEntities(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.NEARBY_ENTITIES);
    }

    @Override
    public void shutdown() {
    }

}
