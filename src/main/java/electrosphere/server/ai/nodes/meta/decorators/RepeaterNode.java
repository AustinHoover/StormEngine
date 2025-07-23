package electrosphere.server.ai.nodes.meta.decorators;

import java.util.function.Supplier;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Repeats execution of a node until a condition is satisfied
 */
public class RepeaterNode implements DecoratorNode {

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
    public RepeaterNode(Supplier<Boolean> conditional, AITreeNode child){
        if(child == null){
            throw new IllegalArgumentException("Trying to create repeater node with no children!");
        }
        if(conditional == null){
            throw new IllegalArgumentException("Trying to create repeater node with no conditional for repeat!");
        }
        this.conditional = conditional;
        this.child = child;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        //
        //evaluate child
        child.evaluate(entity, blackboard);

        //
        //determine result
        boolean conditionCheck = conditional.get();
        if(conditionCheck){
            return AITreeNodeResult.SUCCESS;
        }
        return AITreeNodeResult.RUNNING;
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }
    
}
