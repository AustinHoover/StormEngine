package electrosphere.server.ai.nodes;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;

/**
 * A node in a behavior tree
 */
public interface AITreeNode {

    /**
     * Different results that a node can return
     */
    public static enum AITreeNodeResult {
        /**
         * Successfully executed
         */
        SUCCESS,
        /**
         * Still running
         */
        RUNNING,
        /**
         * Failed
         */
        FAILURE,
    }
    
    /**
     * Evaluates the node
     * @param entity The entity to evaluate this node on
     * @param blackboard The blackboard for the tree
     * @return The result of evaluating the node
     */
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard);

}
