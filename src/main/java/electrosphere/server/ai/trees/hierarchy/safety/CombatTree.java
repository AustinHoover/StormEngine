package electrosphere.server.ai.trees.hierarchy.safety;

import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.SucceederNode;

/**
 * A tree that causes the entity to engage in combat when it determines it needs to in order to maintain its safety
 */
public class CombatTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "CombatTree";
    
    /**
     * Creates a combat tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "CombatTree",
            new PublishStatusNode("Engaged in mortal combat"),
            new SucceederNode(null)
        );
    }

}
