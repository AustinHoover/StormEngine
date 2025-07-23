package electrosphere.util.ds.octree;

import org.joml.Vector3d;

/**
 * An octree implementation
 */
public interface Octree<T> {
    
    /**
     * Adds a leaf to the octree
     * @param location The location of the leaf
     * @param data The data associated with the leaf
     * @throws IllegalArgumentException Thrown if a location is provided that already has an exact match
     */
    public void addLeaf(Vector3d location, T data) throws IllegalArgumentException;

    /**
     * Checks if the octree contains a leaf at an exact location
     * @param location The location
     * @return true if a leaf exists for that exact location, false otherwise
     */
    public boolean containsLeaf(Vector3d location);

    /**
     * Gets the leaf at an exact location
     * @param location The location
     * @return The leaf
     * @throws ArrayIndexOutOfBoundsException Thrown if a location is queries that does not have an associated leaf in the octree
     */
    public OctreeNode<T> getLeaf(Vector3d location) throws ArrayIndexOutOfBoundsException;

    /**
     * Removes a node from the octree
     * @param node The node
     */
    public void removeNode(OctreeNode<T> node);

    /**
     * Gets the root node
     * @return The root node
     */
    public OctreeNode<T> getRoot();

    /**
     * Gets the number of leaves under this tree
     * @return The number of leaves
     */
    public int getNumLeaves();

}
