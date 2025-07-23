package electrosphere.server.ai.nodes.meta.decorators.conditional;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.decorators.DecoratorNode;

/**
 * Runs the child if the conditional fails, otherwise returns failure
 */
public class OnFailureNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * The conditional to check for repetition
     */
    AITreeNode conditional;

    /**
     * Constructor
     * @param child The child node
     */
    public OnFailureNode(AITreeNode conditional, AITreeNode child){
        if(child == null){
            throw new IllegalArgumentException("Trying to create on failure node with no children!");
        }
        if(conditional == null){
            throw new IllegalArgumentException("Trying to create on failure node with no conditional!");
        }
        this.conditional = conditional;
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard){
        
        //
        //check if should run child
        AITreeNodeResult conditionalResult = conditional.evaluate(entity, blackboard);
        if(conditionalResult == AITreeNodeResult.FAILURE){
            //
            //evaluate child
            return child.evaluate(entity, blackboard);
        }

        //
        //Failed to run child
        return AITreeNodeResult.FAILURE;
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }
}