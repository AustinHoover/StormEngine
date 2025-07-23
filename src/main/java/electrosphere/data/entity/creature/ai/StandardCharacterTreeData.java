package electrosphere.data.entity.creature.ai;

import electrosphere.server.ai.trees.character.StandardCharacterTree;

/**
 * Tree data for controlling a standard character tree
 */
public class StandardCharacterTreeData implements AITreeData {

    /**
     * The name of the tree
     */
    String name;
    
    @Override
    public String getName() {
        return StandardCharacterTree.TREE_NAME;
    }

}
