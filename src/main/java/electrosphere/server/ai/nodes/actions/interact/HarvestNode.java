package electrosphere.server.ai.nodes.actions.interact;

import org.joml.Vector3d;

import electrosphere.collision.CollisionEngine;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.plan.TargetEntityCategoryNode;
import electrosphere.server.player.PlayerActions;

/**
 * Tries to harvest an entity
 */
public class HarvestNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard){
        if(!TargetEntityCategoryNode.hasTarget(blackboard)){
            return AITreeNodeResult.FAILURE;
        }
        Entity target = TargetEntityCategoryNode.getTarget(blackboard);
        Vector3d parentPos = EntityUtils.getPosition(entity);
        Vector3d targetPos = EntityUtils.getPosition(target);
        if(parentPos.distance(targetPos) > CollisionEngine.DEFAULT_INTERACT_DISTANCE){
            return AITreeNodeResult.FAILURE;
        }
        PlayerActions.harvest(entity, target);
        TargetEntityCategoryNode.setTarget(blackboard, null);
        return AITreeNodeResult.SUCCESS;
    }

    /**
     * sets the type of entity to try to harvest
     * @param blackboard The blackboard
     * @param type The type of entity to try to harvest
     */
    public static void setHarvestTargetType(Blackboard blackboard, String type){
        blackboard.put(BlackboardKeys.HARVEST_TARGET_TYPE, type);
    }

    /**
     * checks if this blackboard has a type of entity it wants to try to harvest
     * @param blackboard The blackboard
     * @return true if the type is defined, false otherwise
     */
    public static boolean hasHarvestTargetType(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.HARVEST_TARGET_TYPE);
    }

    /**
     * Gets the type of entity to try to harvest
     * @param blackboard The blackboard
     * @return The type of entity to try to harvest
     */
    public static String getHarvestTargetType(Blackboard blackboard){
        return (String)blackboard.get(BlackboardKeys.HARVEST_TARGET_TYPE);
    }

}
