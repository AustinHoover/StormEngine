package electrosphere.server.ai.nodes.actions.move;

import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementRelativeFacing;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Starts walking
 */
public class MoveStartNode implements AITreeNode {

    /**
     * The facing to move in
     */
    MovementRelativeFacing facing = null;

    /**
     * Constructor
     */
    public MoveStartNode(MovementRelativeFacing facing){
        if(facing == null){
            throw new IllegalArgumentException("Trying to create move start tree node with null facing!");
        }
        this.facing = facing;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(ServerGroundMovementTree.getServerGroundMovementTree(entity) != null){
            ServerGroundMovementTree serverGroundMovementTree = ServerGroundMovementTree.getServerGroundMovementTree(entity);
            if(serverGroundMovementTree.isMoving()){
                return AITreeNodeResult.RUNNING;
            } else if(serverGroundMovementTree.canStartMoving()){
                serverGroundMovementTree.start(this.facing);
                if(!serverGroundMovementTree.isMoving()){
                    return AITreeNodeResult.FAILURE;
                }
                return AITreeNodeResult.SUCCESS;
            } else {
                return AITreeNodeResult.FAILURE;
            }
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }

}
