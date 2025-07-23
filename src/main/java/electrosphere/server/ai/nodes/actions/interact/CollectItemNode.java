package electrosphere.server.ai.nodes.actions.interact;

import org.joml.Vector3d;

import electrosphere.collision.CollisionEngine;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.plan.TargetEntityCategoryNode;

/**
 * Tries to collect an item
 */
public class CollectItemNode implements AITreeNode {

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
        ServerInventoryState.attemptStoreItemAnyInventory(entity, target);
        TargetEntityCategoryNode.setTarget(blackboard, null);
        return AITreeNodeResult.SUCCESS;
    }

}
