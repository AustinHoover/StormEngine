package electrosphere.server.ai.nodes.actions.move;

import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.walk.ServerWalkTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

public class WalkStartNode implements AITreeNode {

    /**
     * Constructor
     */
    public WalkStartNode(){
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(ServerWalkTree.getServerWalkTree(entity) != null){
            ServerWalkTree serverWalkTree = ServerWalkTree.getServerWalkTree(entity);
            if(serverWalkTree.isWalking()){
                return AITreeNodeResult.SUCCESS;
            } else {
                serverWalkTree.start();
                return AITreeNodeResult.RUNNING;
            }
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }
}
