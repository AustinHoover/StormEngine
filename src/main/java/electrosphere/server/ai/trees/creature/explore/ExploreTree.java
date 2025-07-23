package electrosphere.server.ai.trees.creature.explore;

import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.DataDeleteNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.plan.TargetExploreNode;
import electrosphere.server.ai.trees.creature.MoveToTree;

/**
 * A tree for exploring new chunks
 */
public class ExploreTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "ExploreTree";

    /**
     * Creates an explore tree
     * @return The root node of the explore tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "Explore",
            new PublishStatusNode("Explore"),
            //resolve point to explore towards
            new TargetExploreNode(BlackboardKeys.MOVE_TO_TARGET),
            //move towards the point
            MoveToTree.create(BlackboardKeys.MOVE_TO_TARGET),
            //clear position after moving towards it
            new DataDeleteNode(BlackboardKeys.MOVE_TO_TARGET)
        );
    }

}
