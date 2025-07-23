package electrosphere.server.ai.nodes.checks.macro;

import electrosphere.entity.Entity;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Checks if this entity is a character
 */
public class IsCharacterNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(ServerCharacterData.hasServerCharacterDataTree(entity)){
            return AITreeNodeResult.SUCCESS;
        }
        return AITreeNodeResult.FAILURE;
    }
    
}
