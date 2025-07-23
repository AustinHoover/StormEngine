package electrosphere.server.ai.trees.hierarchy.safety.shelter;

import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.checks.spatial.BeginStructureNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.SucceederNode;
import electrosphere.server.ai.trees.struct.BuildStructureTree;

/**
 * Tree for constructing shelter
 */
public class ConstructShelterTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "ConstructShelterTree";
    
    /**
     * Creates a construct shelter tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "ConstructShelter",
            new PublishStatusNode("Construct a shelter"),
            new BeginStructureNode(),
            BuildStructureTree.create(),
            new SucceederNode(null)
        );
    }
}
