package electrosphere.data.entity.creature.ai;

import electrosphere.server.ai.trees.test.BlockerAITree;

/**
 * Data for a blocker ai tree
 */
public class BlockerTreeData implements AITreeData {
    
    @Override
    public String getName() {
        return BlockerAITree.TREE_NAME;
    }

}
