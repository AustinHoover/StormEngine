package electrosphere.server.ai.trees.hierarchy.safety;

import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;

/**
 * A tree that runs all of the tier 1 maslow need trees
 */
public class MaslowSafetyTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "MaslowSafetyTree";
    
    /**
     * Creates a tier 1 maslow tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "MaslowSafetyTree",
            new PublishStatusNode("Evaluate safety"),
            new SelectorNode(
                FleeTree.create(),
                CombatTree.create()
            )
        );
    }

}
