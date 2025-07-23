package electrosphere.server.ai.nodes.plan;

import org.joml.Vector3d;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Sets the move-to target position to the position of the targeted entity
 */
public class TargetPositionNode implements AITreeNode {

    /**
     * The key to lookup the target under
     */
    String targetKey;

    /**
     * constructor
     * @param targetKey The key to lookup the target under
     */
    public TargetPositionNode(String targetKey){
        this.targetKey = targetKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard){
        Object targetRaw = blackboard.get(this.targetKey);
        Vector3d targetPos = null;
        if(targetRaw == null){
            throw new Error("Target undefined!");
        }
        if(targetRaw instanceof Vector3d){
            targetPos = (Vector3d)targetRaw;
        }
        if(targetRaw instanceof Entity){
            targetPos = EntityUtils.getPosition((Entity)targetRaw);
        }
        TargetPositionNode.setMoveToTarget(blackboard, targetPos);
        return AITreeNodeResult.SUCCESS;
    }

    /**
     * Sets the move-to target of the blackboard
     * @param blackboard The blackboard
     * @param position The target position
     */
    public static void setMoveToTarget(Blackboard blackboard, Vector3d position){
        blackboard.put(BlackboardKeys.MOVE_TO_TARGET, position);
    }

    /**
     * Gets the move-to target of the blackboard
     * @param blackboard The blackboard
     * @return The move-to target if it exists, null otherwise
     */
    public static Vector3d getMoveToTarget(Blackboard blackboard){
        return (Vector3d)blackboard.get(BlackboardKeys.MOVE_TO_TARGET);
    }

    /**
     * Checks if the blackboard has a move to target
     * @param blackboard the blackboard
     * @return true if it has a move-to target, false otherwise
     */
    public static boolean hasMoveToTarget(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.MOVE_TO_TARGET);
    }

}
