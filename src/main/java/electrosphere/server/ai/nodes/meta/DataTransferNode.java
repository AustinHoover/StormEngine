package electrosphere.server.ai.nodes.meta;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Transfers data from one blackboard key to another
 */
public class DataTransferNode implements AITreeNode {
    
    /**
     * The key to pull data from
     */
    String sourceKey;

    /**
     * The key to push data into
     */
    String destinationKey;

    /**
     * Constructor
     * @param sourceKey The key to pull data from
     * @param destinationKey The key to push data into
     */
    public DataTransferNode(String sourceKey, String destinationKey){
        this.sourceKey = sourceKey;
        this.destinationKey = destinationKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        Object data = blackboard.get(this.sourceKey);
        blackboard.put(this.destinationKey, data);
        return AITreeNodeResult.SUCCESS;
    }

}
