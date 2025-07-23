package electrosphere.server.ai.trees.test;

import electrosphere.data.entity.creature.ai.BlockerTreeData;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.actions.BlockStartNode;
import electrosphere.server.ai.nodes.actions.combat.MeleeTargetingNode;
import electrosphere.server.ai.nodes.actions.move.FaceTargetNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;

/**
 * Creates a blocker test tree
 */
public class BlockerAITree {

    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "Blocker";
    
    /**
     * Creates a blocker tree
     * @return The root node of the tree
     */
    public static AITreeNode create(BlockerTreeData data){
        return new SequenceNode(
            "BlockerAITree",
            new BlockStartNode(),
            new MeleeTargetingNode(5.0f),
            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET)
        );
    }

}
