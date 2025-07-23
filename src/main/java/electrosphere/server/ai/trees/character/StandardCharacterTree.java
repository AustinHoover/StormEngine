package electrosphere.server.ai.trees.character;

import electrosphere.data.entity.creature.ai.StandardCharacterTreeData;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.trees.character.goals.CharacterGoalTree;
import electrosphere.server.ai.trees.hierarchy.MaslowTree;

/**
 * Standard behavior tree for macro characters
 */
public class StandardCharacterTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "StandardCharacter";

    /**
     * Creates a standard character tree
     * @param data The data for the tree
     * @return The root node of the tree
     */
    public static AITreeNode create(StandardCharacterTreeData data){
        return new SequenceNode(
            "StandardCharacter",
            new PublishStatusNode("StandardCharacter"),
            //check that dependencies exist
            new SelectorNode(
                CharacterGoalTree.create(),
                MaslowTree.create()
            )
        );
    }

}
