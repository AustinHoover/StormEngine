package electrosphere.server.ai.nodes.meta.debug;

import electrosphere.entity.Entity;
import electrosphere.server.ai.AI;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Publishes a status to the parent ai
 */
public class PublishStatusNode implements AITreeNode {

    /**
     * The status
     */
    String status = "Idle";

    /**
     * Constructor
     * @param status The message to set the status to
     */
    public PublishStatusNode(String status){
        this.status = status;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        AI.getAI(entity).setStatus(status);
        return AITreeNodeResult.SUCCESS;
    }
    
}
