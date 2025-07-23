package electrosphere.server.ai.trees.creature;

import electrosphere.data.entity.creature.ai.AttackerTreeData;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.trees.creature.melee.MeleeAITree;

/**
 * Figures out how if it should attack and how it can, then executes that subtree
 */
public class AttackerAITree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "Attacker";

    /**
     * Creates an attacker ai tree
     * @return The root node of the tree
     */
    public static AITreeNode create(AttackerTreeData attackerTreeData){
        return new SequenceNode(
            "AttackerAITree",
            MeleeAITree.create(attackerTreeData)
        );
    }

}
