package electrosphere.server.ai.trees.hierarchy.safety;

import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.SucceederNode;

/**
 * A tree that causes the entity to flee in terror if it deems it needs to
 */
public class FleeTree {

    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "FleeTree";
    
    /**
     * Creates an flee tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "FleeTree",
            new PublishStatusNode("Flee!"),
            new SucceederNode(null)
        );
    }

}
