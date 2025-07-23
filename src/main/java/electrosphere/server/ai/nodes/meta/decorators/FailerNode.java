package electrosphere.server.ai.nodes.meta.decorators;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Forces the return value from a child to be failure
 */
public class FailerNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * Constructor
     * @param child (Optional) Child node to execute before returning
     */
    public FailerNode(AITreeNode child){
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(child != null){
            child.evaluate(entity, blackboard);
        }
        return AITreeNodeResult.FAILURE;
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }

}
