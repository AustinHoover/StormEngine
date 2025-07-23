package electrosphere.server.ai.nodes.meta.decorators.conditional;

import java.util.function.Supplier;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.decorators.DecoratorNode;

/**
 * Executes a child node only if the provided conditional succeeds
 */
public class ConditionalNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * The conditional to check for repetition
     */
    Supplier<Boolean> conditional;

    /**
     * Constructor
     * @param child The child node
     */
    public ConditionalNode(Supplier<Boolean> conditional, AITreeNode child){
        if(child == null){
            throw new IllegalArgumentException("Trying to create conditional node with no children!");
        }
        if(conditional == null){
            throw new IllegalArgumentException("Trying to create conditional node with no conditional!");
        }
        this.conditional = conditional;
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        
        //
        //check if should run child
        boolean conditionCheck = conditional.get();
        if(conditionCheck){
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
