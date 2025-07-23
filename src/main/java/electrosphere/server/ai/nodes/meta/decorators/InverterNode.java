package electrosphere.server.ai.nodes.meta.decorators;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Inverts a node's result
 */
public class InverterNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * Constructor
     * @param child The child node
     */
    public InverterNode(AITreeNode child){
        if(child == null){
            throw new IllegalArgumentException("Trying to create inverter node with no children!");
        }
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        AITreeNodeResult childResult = child.evaluate(entity, blackboard);
        switch(childResult){
            case SUCCESS:
                return AITreeNodeResult.FAILURE;
            case FAILURE:
                return AITreeNodeResult.SUCCESS;
            case RUNNING:
                return AITreeNodeResult.RUNNING;
        }
        return AITreeNodeResult.RUNNING;
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }
    
}
