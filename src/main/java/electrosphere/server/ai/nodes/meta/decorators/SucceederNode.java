package electrosphere.server.ai.nodes.meta.decorators;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Forces the return value to be success
 */
public class SucceederNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * Constructor
     * @param child (Optional) Child node to execute before returning
     */
    public SucceederNode(AITreeNode child){
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(child != null){
            child.evaluate(entity, blackboard);
        }
        return AITreeNodeResult.SUCCESS;
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }

}
