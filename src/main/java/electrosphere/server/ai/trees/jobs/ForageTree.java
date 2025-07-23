package electrosphere.server.ai.trees.jobs;

import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;

/**
 * A tree that attempts to forage for items
 */
public class ForageTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "Forage";

    /**
     * Creates a foraging tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "ForageTree"
        );
    }

}
