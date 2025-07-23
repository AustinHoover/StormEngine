package electrosphere.server.ai.nodes.meta;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * A node that deletes a key
 */
public class DataDeleteNode implements AITreeNode {

    /**
     * The key to push data into
     */
    String destinationKey;

    /**
     * Constructor
     * @param destinationKey The key to push data into
     */
    public DataDeleteNode(String destinationKey){
        this.destinationKey = destinationKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        blackboard.delete(this.destinationKey);
        return AITreeNodeResult.SUCCESS;
    }

}
