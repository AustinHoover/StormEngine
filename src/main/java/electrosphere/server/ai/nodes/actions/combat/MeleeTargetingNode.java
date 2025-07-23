package electrosphere.server.ai.nodes.actions.combat;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.DataCellSearchUtils;

/**
 * BTree node to seek targets for melee attacks
 */
public class MeleeTargetingNode implements AITreeNode {

    /**
     * The aggro range
     */
    float aggroRange = 0.0f;

    /**
     * Constructor
     * @param aggroRange The range at which we will pick targets
     * @param onSuccess The node to execute when a target is found
     * @param onFailure (Optional) The next node to execute when a target is not found
     */
    public MeleeTargetingNode(float aggroRange){
        this.aggroRange = aggroRange;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard){
        if(MeleeTargetingNode.hasTarget(blackboard) && this.targetIsValid(MeleeTargetingNode.getTarget(blackboard))){
            return AITreeNodeResult.SUCCESS;
        }

        //search
        Entity target = this.searchForTarget(entity, blackboard);
        if(target == null){
            return AITreeNodeResult.FAILURE;
        } else {
            MeleeTargetingNode.setTarget(blackboard, target);
            return AITreeNodeResult.SUCCESS;
        }
    }

    /**
     * Searches for a valid target
     */
    private Entity searchForTarget(Entity parent, Blackboard blackboard){
        Vector3d position = EntityUtils.getPosition(parent);
        Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
        for(Entity current : DataCellSearchUtils.getEntitiesWithTagAroundLocation(realm,position,EntityTags.LIFE_STATE)){
            if(current != parent){
                Vector3d potentialTargetPosition = EntityUtils.getPosition(current);
                if(position.distance(potentialTargetPosition) < aggroRange){
                    return current;
                }
            }
        }
        return null;
    }

    /**
     * Makes sure the target is valid
     * @param entity The target entity
     * @return true if valid, false otherwise
     */
    private boolean targetIsValid(Entity entity){
        return
        Globals.serverState.realmManager.getEntityRealm(entity) != null
        ;
    }

    /**
     * checks if the blackboard contains a melee target
     * @param blackboard The blackboard
     * @return true if there is a target, false otherwise
     */
    public static boolean hasTarget(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.MELEE_TARGET);
    }

    /**
     * Gets the target stored in the blackboard
     * @param blackboard The blackboard
     * @return The target
     */
    public static Entity getTarget(Blackboard blackboard){
        return (Entity)blackboard.get(BlackboardKeys.MELEE_TARGET);
    }

    /**
     * Sets the melee target stored in the blackboard
     * @param blackboard The blackboard
     * @param target The target entity
     */
    public static void setTarget(Blackboard blackboard, Entity target){
        blackboard.put(BlackboardKeys.MELEE_TARGET, target);
    }

}
