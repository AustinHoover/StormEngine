package electrosphere.server.ai.trees.hierarchy;

import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.checks.macro.IsCharacterNode;
import electrosphere.server.ai.nodes.checks.macro.MacroDataExists;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.trees.hierarchy.safety.MaslowSafetyTree;

/**
 * Arranges goals based on an approximation of Maslow's hierarchy of needs
 */
public class MaslowTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "Maslow";

    /**
     * Creates an attacker ai tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "MaslowTree",
            //check that dependencies exist
            new SequenceNode(
                "MaslowTree",
                new PublishStatusNode("Checking dependencies for maslow tree.."),
                new MacroDataExists(),
                new IsCharacterNode()
            ),

            //check the first tier of needs
            MaslowSafetyTree.create()
        );
    }

}
