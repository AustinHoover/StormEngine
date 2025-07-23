package electrosphere.util.ds.octree;

import java.util.List;

/**
 * An octree node
 */
public interface OctreeNode<T> {
    
    /**
     * Gets the data in the node
     * @return The data
     */
    public T getData();

    /**
     * Gets the parent node of this node
     * @return The parent node
     */
    public OctreeNode<T> getParent();

    /**
     * Gets the children of this node
     * @return The children
     */
    public List<OctreeNode<T>> getChildren();

    /**
     * Checks if this is a leaf node or not
     * @return true if it is a leaf, false otherwise
     */
    public boolean isLeaf();

}
