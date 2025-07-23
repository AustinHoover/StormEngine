package electrosphere.server.ai.nodes.meta;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Stores a piece of data into a blackboard
 */
public class DataStorageNode implements AITreeNode {
    
    /**
     * The data to store in the key
     */
    Object data;

    /**
     * The key to push data into
     */
    String destinationKey;

    /**
     * Constructor
     * @param destinationKey The key to push data into
     * @param data The data to store at the key
     */
    public DataStorageNode(String destinationKey, Object data){
        this.data = data;
        this.destinationKey = destinationKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        blackboard.put(this.destinationKey, this.data);
        return AITreeNodeResult.SUCCESS;
    }

}
