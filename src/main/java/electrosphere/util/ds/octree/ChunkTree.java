package electrosphere.util.ds.octree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3i;

/**
 * An octree that has a guaranteed subdivision strategy that makes it appealing to use for chunk-based operations
 */
public class ChunkTree<T> {   
    
    /**
     * The dimension of the tree
     */
    static final int DIMENSION = 16;

    /**
     * The maximum level
     */
    public static final int MAX_LEVEL = 4;

    //The root node
    private ChunkTreeNode<T> root = null;

    /**
     * The list of all nodes in the tree
     */
    List<ChunkTreeNode<T>> nodes = null;

    /**
     * Constructor
     */
    public ChunkTree(){
        this.nodes = new ArrayList<ChunkTreeNode<T>>();
        this.root = new ChunkTreeNode<T>(0, new Vector3i(0,0,0), new Vector3i(16,16,16));
        this.root.isLeaf = true;
        this.nodes.add(this.root);
    }

    /**
     * Splits a parent into child nodes
     * @param parent The parent
     * @return The new non-leaf node
     */
    public ChunkTreeNode<T> split(ChunkTreeNode<T> existing){
        if(!existing.isLeaf()){
            throw new IllegalArgumentException("Tried to split non-leaf!");
        }
        Vector3i min = existing.getMinBound();
        Vector3i max = existing.getMaxBound();
        int midX = (max.x - min.x) / 2 + min.x;
        int midY = (max.y - min.y) / 2 + min.y;
        int midZ = (max.z - min.z) / 2 + min.z;
        int currentLevel = existing.getLevel();
        ChunkTreeNode<T> newContainer = new ChunkTreeNode<>(currentLevel, min, max);
        //add children
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(min.x,min.y,min.z), new Vector3i(midX,midY,midZ)));
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(midX,min.y,min.z), new Vector3i(max.x,midY,midZ)));
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(min.x,midY,min.z), new Vector3i(midX,max.y,midZ)));
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(midX,midY,min.z), new Vector3i(max.x,max.y,midZ)));
        //
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(min.x,min.y,midZ), new Vector3i(midX,midY,max.z)));
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(midX,min.y,midZ), new Vector3i(max.x,midY,max.z)));
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(min.x,midY,midZ), new Vector3i(midX,max.y,max.z)));
        newContainer.addChild(new ChunkTreeNode<T>(currentLevel + 1, new Vector3i(midX,midY,midZ), new Vector3i(max.x,max.y,max.z)));

        //replace existing node
        replaceNode(existing,newContainer);

        //update tracking
        this.nodes.remove(existing);
        this.nodes.add(newContainer);
        this.nodes.addAll(newContainer.getChildren());

        return newContainer;
    }

    /**
     * Joins a non-leaf node's children into a single node
     * @param parent The non-leaf
     * @return The new leaf node
     */
    public ChunkTreeNode<T> join(ChunkTreeNode<T> existing){
        if(existing.isLeaf()){
            throw new IllegalArgumentException("Tried to split non-leaf!");
        }
        Vector3i min = existing.getMinBound();
        Vector3i max = existing.getMaxBound();
        int currentLevel = existing.getLevel();
        ChunkTreeNode<T> newContainer = new ChunkTreeNode<>(currentLevel, min, max);

        //replace existing node
        replaceNode(existing,newContainer);

        //update tracking
        this.nodes.remove(existing);
        this.nodes.removeAll(existing.getChildren());
        this.nodes.add(newContainer);

        return newContainer;
    }

    /**
     * Replaces an existing node with a new node
     * @param existing the existing node
     * @param newNode the new node
     */
    private void replaceNode(ChunkTreeNode<T> existing, ChunkTreeNode<T> newNode){
        if(existing == this.root){
            this.root = newNode;
        } else {
            ChunkTreeNode<T> parent = existing.getParent();
            parent.removeChild(existing);
            parent.addChild(newNode);
        }
    }

    /**
     * Gets the root node of the tree
     */
    public ChunkTreeNode<T> getRoot() {
        return this.root;
    }


    /**
     * A node in a chunk tree
     */
    public static class ChunkTreeNode<T> {

        //True if this is a leaf node, false otherwise
        private boolean isLeaf;

        //the parent node
        private ChunkTreeNode<T> parent;
        
        //the children of this node
        private List<ChunkTreeNode<T>> children = new LinkedList<ChunkTreeNode<T>>();

        //The data at the node
        private T data;

        /**
         * The min bound
         */
        private Vector3i min;

        /**
         * The max bound
         */
        private Vector3i max;

        /**
         * The level of the chunk tree node
         */
        int level;

        /**
         * Constructor for non-leaf node
         */
        private ChunkTreeNode(int level, Vector3i min, Vector3i max){
            if(min.x == 16 || min.y == 16 || min.z == 16){
                throw new IllegalArgumentException("Invalid minimum! " + min);
            }
            if(level < 0 || level > MAX_LEVEL){
                throw new IllegalArgumentException("Invalid level! " + level);
            }
            this.isLeaf = false;
            this.level = level;
            this.min = min;
            this.max = max;
        }

        /**
         * Converts this node to a leaf
         * @param data The data to put in the leaf
         */
        public void convertToLeaf(T data){
            this.isLeaf = true;
            this.data = data;
        }

        /**
         * Sets whether this node is a leaf or not
         * @param isLeaf true if it is a leaf, false otherwise
         */
        public void setLeaf(boolean isLeaf){
            this.isLeaf = isLeaf;
        }

        /**
         * Sets the data of the node
         * @param data The data
         */
        public void setData(T data){
            this.data = data;
        }

        /**
         * Gets the data associated with this node
         */
        public T getData() {
            return data;
        }

        /**
         * Gets the parent of this node
         */
        public ChunkTreeNode<T> getParent() {
            return parent;
        }

        /**
         * Gets the children of this node
         */
        public List<ChunkTreeNode<T>> getChildren() {
            return Collections.unmodifiableList(this.children);
        }

        /**
         * Checks if this node is a leaf
         * @return true if it is a leaf, false otherwise
         */
        public boolean isLeaf() {
            return isLeaf;
        }

        /**
         * Checks if the node can split
         * @return true if can split, false otherwise
         */
        public boolean canSplit(){
            return isLeaf && level < MAX_LEVEL;
        }

        /**
         * Gets the level of the node
         * @return The level of the node
         */
        public int getLevel(){
            return level;
        }

        /**
         * Gets the min bound of this node
         * @return The min bound
         */
        public Vector3i getMinBound(){
            return min;
        }

        /**
         * Gets the max bound of this node
         * @return The max bound
         */
        public Vector3i getMaxBound(){
            return max;
        }

        /**
         * Adds a child to this node
         * @param child The child
         */
        private void addChild(ChunkTreeNode<T> child){
            this.children.add(child);
            child.parent = this;
        }

        /**
         * Removes a child node
         * @param child the child
         */
        private void removeChild(ChunkTreeNode<T> child){
            this.children.remove(child);
            child.parent = null;
        }

    }
    
    
}
