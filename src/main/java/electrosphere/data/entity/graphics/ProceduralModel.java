package electrosphere.data.entity.graphics;

import electrosphere.data.entity.foliage.TreeModel;

/**
 * A procedurally-generated model
 */
public class ProceduralModel {
    
    /**
     * Model for generating a procedural tree
     */
    TreeModel treeModel;

    /**
     * Gets the procedural tree model
     * @return The procedural tree model if it exists, null otherwise
     */
    public TreeModel getTreeModel(){
        return treeModel;
    }

}
