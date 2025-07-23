package electrosphere.server.ai.nodes.solvers;

import electrosphere.entity.Entity;
import electrosphere.entity.state.block.ServerBlockTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Finds a target given some criteria
 */
public class FindEntityTargetNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard){
        if(ServerBlockTree.getServerBlockTree(entity) != null){
            ServerBlockTree serverBlockTree = ServerBlockTree.getServerBlockTree(entity);
            if(serverBlockTree.isIdle()){
                serverBlockTree.start();
                return AITreeNodeResult.SUCCESS;
            } else {
                return AITreeNodeResult.RUNNING;
            }
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }

}
