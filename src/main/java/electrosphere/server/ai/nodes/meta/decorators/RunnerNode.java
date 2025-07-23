package electrosphere.server.ai.nodes.meta.decorators;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * A node that always returns running
 */
public class RunnerNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * Constructor
     * @param child (Optional) Child node to execute before returning
     */
    public RunnerNode(AITreeNode child){
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(child != null){
            child.evaluate(entity, blackboard);
        }
        return AITreeNodeResult.RUNNING;
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }

}
