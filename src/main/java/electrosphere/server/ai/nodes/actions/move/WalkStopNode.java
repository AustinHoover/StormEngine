package electrosphere.server.ai.nodes.actions.move;

import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.walk.ServerWalkTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

public class WalkStopNode implements AITreeNode {

    /**
     * Constructor
     */
    public WalkStopNode(){
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(ServerWalkTree.getServerWalkTree(entity) != null){
            ServerWalkTree serverGroundMovementTree = ServerWalkTree.getServerWalkTree(entity);
            if(serverGroundMovementTree.isWalking()){
                serverGroundMovementTree.stop();
                return AITreeNodeResult.RUNNING;
            } else {
                return AITreeNodeResult.SUCCESS;
            }
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }
}
