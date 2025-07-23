package electrosphere.server.ai.nodes.checks;

import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Checks if the ai is moving
 */
public class IsMovingNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(ServerGroundMovementTree.getServerGroundMovementTree(entity) != null){
            ServerGroundMovementTree serverGroundMovementTree = ServerGroundMovementTree.getServerGroundMovementTree(entity);
            if(serverGroundMovementTree.isMoving()){
                return AITreeNodeResult.SUCCESS;
            } else {
                return AITreeNodeResult.FAILURE;
            }
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }
    
}
