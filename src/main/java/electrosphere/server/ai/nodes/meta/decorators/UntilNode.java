package electrosphere.server.ai.nodes.meta.decorators;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Executes until the child returns a specific result
 */
public class UntilNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * The result to check for repetition
     */
    AITreeNodeResult requiredResult = null;

    /**
     * Constructor
     * @param child The child node
     */
    public UntilNode(AITreeNodeResult requiredResult, AITreeNode child){
        if(child == null){
            throw new IllegalArgumentException("Trying to create until node with no children!");
        }
        if(requiredResult == null){
            throw new IllegalArgumentException("Trying to create until node with no required result!");
        }
        this.requiredResult = requiredResult;
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        AITreeNodeResult result = child.evaluate(entity, blackboard);
        if(result == requiredResult){
            return AITreeNodeResult.SUCCESS;
        }
        return AITreeNodeResult.RUNNING;
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }
}
