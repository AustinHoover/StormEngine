package electrosphere.util.ds.octree;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3i;

import electrosphere.engine.Globals;
import io.github.studiorailgun.MathUtils;

/**
 * A power of two oct tree that supports arbitrary world size (not fixed)
 */
public class WorldOctTree <T> {   
    /**
     * The maximum level of the chunk tree
     */
    int maxLevel;

    /**
     * The root node
     */
    private WorldOctTreeNode<T> root = null;

    /**
     * The minimum position
     */
    Vector3i min;

    /**
     * The maximum position
     */
    Vector3i max;

    /**
     * Constructor
     * @param min The minimum position of the world
     * @param max The maximum position of the world
     */
    public WorldOctTree(Vector3i min, Vector3i max){
        //check that dimensions are a multiple of 2
        if(
            ((max.x - min.x) & (max.x - min.x - 1)) != 0 ||
            ((max.y - min.y) & (max.y - min.y - 1)) != 0 ||
            ((max.z - min.z) & (max.z - min.z - 1)) != 0
        ){
            throw new Error("Invalid dimensions! Must be a power of two! " + min + " " + max);
        }
        if(max.x - min.x != max.y - min.y || max.x - min.x != max.z - min.z){
            throw new Error("Invalid dimensions! Must be the same size along all three axis! " + min + " " + max);
        }
        this.min = new Vector3i(min);
        this.max = new Vector3i(max);
        //calculate max level
        int dimRaw = max.x - min.x;
        this.maxLevel = (int)MathUtils.log2(dimRaw);
        this.root = new WorldOctTreeNode<T>(this, 0, new Vector3i(min), new Vector3i(max));
        this.root.setLeaf(true);
    }

    /**
     * Splits a parent into child nodes
     * @param parent The parent
     * @return The new non-leaf node
     */
    public WorldOctTreeNode<T> split(WorldOctTreeNode<T> existing){
        if(!existing.isLeaf()){
            throw new IllegalArgumentException("Tried to split non-leaf!");
        }
        Vector3i min = existing.getMinBound();
        Vector3i max = existing.getMaxBound();
        int midX = (max.x - min.x) / 2 + min.x;
        int midY = (max.y - min.y) / 2 + min.y;
        int midZ = (max.z - min.z) / 2 + min.z;
        int currentLevel = existing.getLevel();
        WorldOctTreeNode<T> newContainer = new WorldOctTreeNode<>(this, currentLevel, min, max);
        //add children
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(min.x,min.y,min.z), new Vector3i(midX,midY,midZ)));
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(midX,min.y,min.z), new Vector3i(max.x,midY,midZ)));
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(min.x,midY,min.z), new Vector3i(midX,max.y,midZ)));
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(midX,midY,min.z), new Vector3i(max.x,max.y,midZ)));
        //
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(min.x,min.y,midZ), new Vector3i(midX,midY,max.z)));
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(midX,min.y,midZ), new Vector3i(max.x,midY,max.z)));
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(min.x,midY,midZ), new Vector3i(midX,max.y,max.z)));
        newContainer.addChild(new WorldOctTreeNode<T>(this, currentLevel + 1, new Vector3i(midX,midY,midZ), new Vector3i(max.x,max.y,max.z)));

        boolean foundMin = false;
        for(WorldOctTreeNode<T> child : newContainer.getChildren()){
            if(child.getMinBound().distance(newContainer.getMinBound()) == 0){
                foundMin = true;
            }
        }
        if(!foundMin){
            String message = "Failed to sanity check!\n";
            message = message + min + " " + max + "\n";
            message = message + midX + " " + midY + " " + midZ + "\n";
            message = message + "container mid: " + newContainer.getMinBound();
            for(WorldOctTreeNode<T> child : newContainer.getChildren()){
                message = message + "child min: " + child.getMinBound() + "\n";
            }
            throw new Error(message);
        }

        //replace existing node
        replaceNode(existing,newContainer);

        return newContainer;
    }

    /**
     * Joins a non-leaf node's children into a single node
     * @param parent The non-leaf
     * @param data The container's data
     * @return The new leaf node
     */
    public WorldOctTreeNode<T> join(WorldOctTreeNode<T> existing, T data){
        if(existing.isLeaf()){
            throw new IllegalArgumentException("Tried to split non-leaf!");
        }
        Globals.profiler.beginCpuSample("WorldOctTree.join - allocation");
        Vector3i min = existing.getMinBound();
        Vector3i max = existing.getMaxBound();
        int currentLevel = existing.getLevel();
        WorldOctTreeNode<T> newContainer = new WorldOctTreeNode<>(this, currentLevel, min, max);
        newContainer.setData(data);
        newContainer.setLeaf(true);
        Globals.profiler.endCpuSample();

        //replace existing node
        Globals.profiler.beginCpuSample("WorldOctTree.join - replace");
        this.replaceNode(existing,newContainer);
        Globals.profiler.endCpuSample();

        //update tracking
        Globals.profiler.beginCpuSample("WorldOctTree.join - tracking");
        Globals.profiler.endCpuSample();

        return newContainer;
    }

    /**
     * Replaces an existing node with a new node
     * @param existing the existing node
     * @param newNode the new node
     */
    private void replaceNode(WorldOctTreeNode<T> existing, WorldOctTreeNode<T> newNode){
        if(existing == this.root){
            this.root = newNode;
        } else {
            WorldOctTreeNode<T> parent = existing.getParent();
            int index = parent.children.indexOf(existing);
            parent.removeChild(existing);
            parent.addChild(index, newNode);
        }
    }

    /**
     * Gets the root node of the tree
     */
    public WorldOctTreeNode<T> getRoot() {
        return this.root;
    }

    /**
     * Sets the max level of the tree
     * @param maxLevel The max level
     */
    public void setMaxLevel(int maxLevel){
        this.maxLevel = maxLevel;
    }

    /**
     * Gets the max level allowed for the tree
     * @return The max level
     */
    public int getMaxLevel(){
        return maxLevel;
    }

    /**
     * Clears the tree
     */
    public void clear(){
        this.root = new WorldOctTreeNode<T>(this, 0, new Vector3i(min), new Vector3i(max));
        this.root.isLeaf = true;
    }

    /**
     * Searches for the node at a given position
     * @param position The position
     * @param returnNonLeaf If true, the function can return non-leaf nodes, otherwise will only return leaf nodes
     * @return The leaf if it exists, null otherwise
     */
    public WorldOctTreeNode<T> search(Vector3i position, boolean returnNonLeaf){
        return this.search(position,returnNonLeaf,this.maxLevel);
    }

    /**
     * Searches for the node at a given position
     * @param position The position
     * @param returnNonLeaf If true, the function can return non-leaf nodes, otherwise will only return leaf nodes
     * @param maxLevel The maximum level to search for
     * @return The leaf if it exists, null otherwise
     */
    public WorldOctTreeNode<T> search(Vector3i position, boolean returnNonLeaf, int maxLevel){
        //out of bounds check
        if(
            position.x < min.x || position.x > max.x ||
            position.y < min.y || position.y > max.y ||
            position.z < min.z || position.z > max.z
        ){
            throw new Error("Trying to search for node outside tree range!");
        }
        WorldOctTreeNode<T> searchResult = this.recursiveSearchUnsafe(root,position,maxLevel);
        if(!returnNonLeaf && !searchResult.isLeaf()){
            return null;
        }
        return searchResult;
    }

    /**
     * Recursively searches for the node at the position. Unsafe because it does not bounds check.
     * @param currentNode The current node searching from
     * @param position The position to search at
     * @param maxLevel The maximum level to search for
     * @return The found node
     */
    private WorldOctTreeNode<T> recursiveSearchUnsafe(WorldOctTreeNode<T> currentNode, Vector3i position, int maxLevel){
        if(maxLevel < 0){
            throw new Error("Provided invalid max level! Must be created than 0! " + maxLevel);
        }
        if(currentNode.level > maxLevel){
            throw new Error("Failed to stop before max level!");
        }
        if(currentNode.level == maxLevel){
            return currentNode;
        }
        if(currentNode.getChildren().size() > 0){
            for(WorldOctTreeNode<T> child : currentNode.getChildren()){
                if(
                    position.x < child.getMaxBound().x && position.x >= child.getMinBound().x &&
                    position.y < child.getMaxBound().y && position.y >= child.getMinBound().y &&
                    position.z < child.getMaxBound().z && position.z >= child.getMinBound().z
                ){
                    return recursiveSearchUnsafe(child, position, maxLevel);
                }
            }
            String message = "Current node is within range, but no children are! This does not make any sense.\n";
            
            message = message + " current pos: " + currentNode.getMinBound() + " " + currentNode.getMaxBound() + "\n";
            for(WorldOctTreeNode<T> child : currentNode.getChildren()){
                message = message + " child " + child + " pos: " + child.getMinBound() + " " + child.getMaxBound() + "\n";
            }
            message = message + "position to search: " + position + "\n";
            throw new Error(message);
        } else {
            return currentNode;
        }
    }

    /**
     * The number of nodes in the tree
     * @return The number of nodes
     */
    public int getNodeCount(){
        return this.recursivelyCountNotes(this.root);
    }

    /**
     * Recursively counts all nodes
     * @param searchTarget The root node
     * @return The number of nodes underneath this one
     */
    private int recursivelyCountNotes(WorldOctTreeNode<T> searchTarget){
        int rVal = 1;
        if(searchTarget.children.size() > 0){
            for(WorldOctTreeNode<T> child : searchTarget.children){
                rVal += this.recursivelyCountNotes(child);
            }
        }
        return rVal;
    }


    /**
     * A node in a chunk tree
     */
    public static class WorldOctTreeNode<T> {

        //True if this is a leaf node, false otherwise
        private boolean isLeaf;

        //the parent node
        private WorldOctTreeNode<T> parent;

        /**
         * The tree containing this node
         */
        private WorldOctTree<T> containingTree;
        
        //the children of this node
        private List<WorldOctTreeNode<T>> children = new LinkedList<WorldOctTreeNode<T>>();

        //The data at the node
        private T data;

        /**
         * The min bound
         */
        private Vector3i min;

        /**
         * The max bound.
         * !!NOTE!! max is exclusive, not inclusive
         */
        private Vector3i max;

        /**
         * The level of the chunk tree node
         */
        int level;

        /**
         * Constructor for non-leaf node
         * @param tree The parent tree
         * @param level The level of the node
         * @param min The minimum position of the node
         * @param max The maximum position of then ode
         */
        private WorldOctTreeNode(WorldOctTree<T> tree, int level, Vector3i min, Vector3i max){
            if(tree == null){
                throw new Error("Invalid tree provided " + tree);
            }
            int maxPos = (int)Math.pow(2,tree.getMaxLevel());
            if(min.x == maxPos || min.y == maxPos || min.z == maxPos){
                throw new IllegalArgumentException("Invalid minimum! " + min);
            }
            if(level < 0 || level > tree.getMaxLevel()){
                throw new IllegalArgumentException("Invalid level! " + level);
            }
            this.containingTree = tree;
            this.isLeaf = false;
            this.level = level;
            this.min = min;
            this.max = max;
        }

        /**
         * Constructor for use in tests
         * @param tree The Tree
         * @param level The level
         * @param min The min point
         * @param max The max point
         * @return The node
         */
        public static <T> WorldOctTreeNode<T> constructorForTests(WorldOctTree<T> tree, int level, Vector3i min, Vector3i max){
            return new WorldOctTreeNode<T>(tree, level, min, max);
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
        public WorldOctTreeNode<T> getParent() {
            return parent;
        }

        /**
         * Gets the children of this node
         */
        public List<WorldOctTreeNode<T>> getChildren() {
            return this.children;
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
            return isLeaf && level < containingTree.getMaxLevel();
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
            return new Vector3i(min);
        }

        /**
         * Gets the max bound of this node
         * @return The max bound
         */
        public Vector3i getMaxBound(){
            return new Vector3i(max);
        }

        /**
         * Adds a child to this node
         * @param child The child
         */
        private void addChild(WorldOctTreeNode<T> child){
            this.children.add(child);
            child.parent = this;
        }

        /**
         * Adds a child to this node
         * @param index The index of the child
         * @param child The child
         */
        private void addChild(int index, WorldOctTreeNode<T> child){
            this.children.add(index, child);
            child.parent = this;
        }

        /**
         * Removes a child node
         * @param child the child
         */
        private void removeChild(WorldOctTreeNode<T> child){
            this.children.remove(child);
            child.parent = null;
        }

    }
}
