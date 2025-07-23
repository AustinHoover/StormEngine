package electrosphere.data.entity.creature.ai;

import electrosphere.server.ai.trees.hierarchy.MaslowTree;

/**
 * Tree data for controlling a maslow tree
 */
public class MaslowTreeData implements AITreeData {

    /**
     * The name of the tree
     */
    String name;
    
    @Override
    public String getName() {
        return MaslowTree.TREE_NAME;
    }

}
