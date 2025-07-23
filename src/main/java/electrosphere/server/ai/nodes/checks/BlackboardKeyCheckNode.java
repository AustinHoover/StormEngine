package electrosphere.server.ai.nodes.checks;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Checks if a blackboard key exists
 */
public class BlackboardKeyCheckNode implements AITreeNode {

    /**
     * The key to check for
     */
    String key;

    /**
     * Constructor
     * @param key The key to check for
     */
    public BlackboardKeyCheckNode(String key){
        this.key = key;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(blackboard.has(this.key)){
            return AITreeNodeResult.SUCCESS;
        }
        return AITreeNodeResult.FAILURE;
    }
    
}
