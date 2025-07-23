package electrosphere.server.ai.nodes.actions.move;

import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Stops walking
 */
public class MoveStopNode implements AITreeNode {

    /**
     * Constructor
     */
    public MoveStopNode(){
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(ServerGroundMovementTree.getServerGroundMovementTree(entity) != null){
            ServerGroundMovementTree serverGroundMovementTree = ServerGroundMovementTree.getServerGroundMovementTree(entity);
            if(serverGroundMovementTree.isMoving()){
                serverGroundMovementTree.slowdown();
                return AITreeNodeResult.RUNNING;
            } else {
                return AITreeNodeResult.SUCCESS;
            }
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }
}
