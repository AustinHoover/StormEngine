package electrosphere.util.ds.octree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joml.Vector3d;

/**
 * An octree implementation
 */
public class RealOctree<T> {

    /**
     * The number of children in a full, non-leaf node
     */
    private static final int FULL_NODE_SIZE = 8;
    

    //The root node
    private RealOctreeNode<T> root = null;

    /**
     * Table used for looking up the presence of nodes
     * Maps a position key to a corresponding Octree node
     */
    Map<String,RealOctreeNode<T>> lookupTable = new HashMap<String,RealOctreeNode<T>>();

    /**
     * Creates an octree
     * @param center The center of the root node of the octree
     */
    public RealOctree(Vector3d boundLower, Vector3d boundUpper){
        root = new RealOctreeNode<T>(boundLower, boundUpper);
    }

    /**
     * Adds a leaf to the octree
     * @param location The location of the leaf
     * @param data The data associated with the leaf
     * @throws IllegalArgumentException Thrown if a location is provided that already has an exact match
     */
    public void addLeaf(Vector3d location, T data) throws IllegalArgumentException {
        //check if the location is already occupied
        if(this.containsLeaf(location)){
            throw new IllegalArgumentException("Tried adding leaf that is already occupied!");
        }
        RealOctreeNode<T> node = new RealOctreeNode<T>(data, location);
        RealOctreeNode<T> current = this.root;
        while(current.children.size() == FULL_NODE_SIZE){
            if(current.hasChildInQuadrant(location)){
                RealOctreeNode<T> child = current.getChildInQuadrant(location);
                if(child.isLeaf()){
                    //get parent of the new fork
                    RealOctreeNode<T> parent = child.parent;
                    parent.removeChild(child);

                    //while the newly forked child isn't between the two children, keep forking
                    RealOctreeNode<T> nonLeaf = parent.forkQuadrant(child.getLocation());
                    while(nonLeaf.getQuadrant(child.getLocation()) == nonLeaf.getQuadrant(node.getLocation())){
                        nonLeaf = nonLeaf.forkQuadrant(child.getLocation());
                    }
                    

                    //add the child and the new node to the non-leaf node
                    nonLeaf.addChild(child);
                    nonLeaf.addChild(node);
                    this.lookupTable.put(this.getLocationKey(location),node);
                    break;
                } else {
                    current = child;
                }
            } else {
                current.addChild(node);
                this.lookupTable.put(this.getLocationKey(location),node);
                break;
            }
        }
    }

    /**
     * Checks if the octree contains a leaf at an exact location
     * @param location The location
     * @return true if a leaf exists for that exact location, false otherwise
     */
    public boolean containsLeaf(Vector3d location){
        return lookupTable.containsKey(this.getLocationKey(location));
    }

    

    /**
     * Gets the leaf at an exact location
     * @param location The location
     * @return The leaf
     * @throws ArrayIndexOutOfBoundsException Thrown if a location is queries that does not have an associated leaf in the octree
     */
    public RealOctreeNode<T> getLeaf(Vector3d location) throws ArrayIndexOutOfBoundsException {
        if(!this.containsLeaf(location)){
            throw new ArrayIndexOutOfBoundsException("Tried to get leaf at position that does not contain leaf!");
        }
        return lookupTable.get(this.getLocationKey(location));
    }

    /**
     * Removes a node from the octree
     * @param node The node
     */
    public void removeNode(RealOctreeNode<T> node){
        RealOctreeNode<T> parent = node.parent;
        parent.removeChild(node);
        if(node.isLeaf()){
            this.lookupTable.remove(this.getLocationKey(node.getLocation()));
        }
    }

    /**
     * Gets the root node
     * @return The root node
     */
    public RealOctreeNode<T> getRoot(){
        return this.root;
    }

    /**
     * Gets the number of leaves under this tree
     * @return The number of leaves
     */
    public int getNumLeaves(){
        return this.root.getNumLeaves();
    }

    /**
     * Gets the key from the location
     * @param location The location
     * @return The key
     */
    private String getLocationKey(Vector3d location){
        return location.x + "_" + location.y + "_" + location.z;
    }


    /**
     * A single node in the octree
     */
    public static class RealOctreeNode<T> implements OctreeNode<T> {

        /*
          6 +----------+ 7
           /|        / |
          / |       /  |
         /  |      /   |
      2 +---------+ 3  + 5
        |   /4    |   /
        |  /      |  /
        | /       | /
        +---------+
        0         1



        Y   Z
        ^  /
        | /
        |/
        +---> X

         */

        //the location of the node
        private Vector3d midpoint;

        //the bounds of the node
        private Vector3d lowerBound;
        private Vector3d upperBound;

        //True if this is a leaf node, false otherwise
        private boolean isLeaf;

        //the parent node
        private RealOctreeNode<T> parent;
        
        //the children of this node
        private List<RealOctreeNode<T>> children = new LinkedList<RealOctreeNode<T>>();

        //The data at the node
        private T data;

        /**
         * Constructor
         * @param data The data at this octree node's position
         * @param location The location of the node
         */
        private RealOctreeNode(T data, Vector3d location){
            this.data = data;
            this.midpoint = location;
            this.isLeaf = true;
        }

        /**
         * Creates a non-leaf node
         * @param lowerBound The lower bound of the node
         * @param upperBound The upper bound of the node
         */
        private RealOctreeNode(Vector3d lowerBound, Vector3d upperBound){
            this.data = null;
            this.midpoint = new Vector3d(
                ((upperBound.x - lowerBound.x) / 2.0) + lowerBound.x,
                ((upperBound.y - lowerBound.y) / 2.0) + lowerBound.y,
                ((upperBound.z - lowerBound.z) / 2.0) + lowerBound.z
            );
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            for(int i = 0; i < FULL_NODE_SIZE; i++){
                this.children.add(null);
            }
            this.isLeaf = false;
        }

        /**
         * Creates a non-leaf node
         * @param midpoint The midpoint of the node
         */
        private RealOctreeNode(Vector3d midpoint){
            this.data = null;
            this.midpoint = midpoint;
            for(int i = 0; i < FULL_NODE_SIZE; i++){
                this.children.add(null);
            }
            this.isLeaf = false;
        }

        /**
         * Gets the data in the node
         * @return The data
         */
        public T getData(){
            return data;
        }

        /**
         * Gets the location of the node
         * @return The location
         */
        public Vector3d getLocation(){
            return midpoint;
        }

        /**
         * Gets the parent node of this node
         * @return The parent node
         */
        public RealOctreeNode<T> getParent(){
            return parent;
        }

        /**
         * Gets the children of this node
         * @return The children
         */
        public List<OctreeNode<T>> getChildren(){
            return this.children.stream().filter((node)->{return node != null;}).collect(Collectors.toList());
        }

        /**
         * Checks if this node has a child in a given quadrant
         * @param positionToCheck The position to check
         * @return true if there is a child in that quadrant, false otherwise
         */
        public boolean hasChildInQuadrant(Vector3d positionToCheck){
            int positionQuadrant = this.getQuadrant(positionToCheck);
            RealOctreeNode<T> child = this.children.get(positionQuadrant);
            return child != null;
        }

        /**
         * Gets the child in a given quadrant
         * @param positionToQuery The position of the quadrant
         * @return The child if it exists, null otherwise
         */
        public RealOctreeNode<T> getChildInQuadrant(Vector3d positionToQuery){
            int positionQuadrant = this.getQuadrant(positionToQuery);
            RealOctreeNode<T> child = this.children.get(positionQuadrant);
            return child;
        }

        /**
         * Checks if this is a leaf node or not
         * @return true if it is a leaf, false otherwise
         */
        public boolean isLeaf(){
            return this.isLeaf;
        }

        /**
         * Gets the number of child nodes
         * @return The number of child nodes
         */
        public int getNumChildren(){
            int acc = 0;
            for(RealOctreeNode<T> child : children){
                if(child != null){
                    acc++;
                }
            }
            return acc;
        }

        /**
         * Gets the number of leaves beneath this node
         * @return The number of leaves
         */
        public int getNumLeaves(){
            int acc = 0;
            if(this.isLeaf()){
                return 1;
            } else {
                for(RealOctreeNode<T> child : this.children){
                    if(child != null){
                        if(child.isLeaf()){
                            acc++;
                        } else {
                            acc = acc + child.getNumLeaves();
                        }
                    }
                }
            }
            return acc;
        }

        /**
         * Adds a child to this node
         * @param child The child
         */
        private void addChild(RealOctreeNode<T> child){
            if(hasChildInQuadrant(child.midpoint)){
                throw new IllegalArgumentException("Trying to add child in occupied quadrant!");
            }
            int quadrant = this.getQuadrant(child.getLocation());
            this.children.set(quadrant,child);
            child.parent = this;
            this.setChildBounds(child);
        }
        
        /**
         * Removes a child from this node
         * @param child The child
         */
        private void removeChild(RealOctreeNode<T> child){
            if(child == null){
                throw new IllegalArgumentException("Child cannot be null!");
            }
            int quadrant = this.getQuadrant(child.getLocation());
            this.children.set(quadrant,null);
            child.parent = null;
        }

        /**
         * Gets the quadrant of a position relative to this node
         * @param positionToCheck The position to check
         * @return The quadrant
         */
        private int getQuadrant(Vector3d positionToCheck){
            if(positionToCheck.z < this.midpoint.z){
                if(positionToCheck.y < this.midpoint.y){
                    if(positionToCheck.x < this.midpoint.x){
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if(positionToCheck.x < this.midpoint.x){
                        return 2;
                    } else {
                        return 3;
                    }
                }
            } else {
                if(positionToCheck.y < this.midpoint.y){
                    if(positionToCheck.x < this.midpoint.x){
                        return 4;
                    } else {
                        return 5;
                    }
                } else {
                    if(positionToCheck.x < this.midpoint.x){
                        return 6;
                    } else {
                        return 7;
                    }
                }
            }
        }

        /**
         * Sets the bounds of the child based on the quadrant it falls under
         * @param child The child
         */
        private void setChildBounds(RealOctreeNode<T> child){
            if(child.midpoint.z < this.midpoint.z){
                if(child.midpoint.y < this.midpoint.y){
                    if(child.midpoint.x < this.midpoint.x){
                        child.lowerBound = new Vector3d(this.lowerBound);
                        child.upperBound = new Vector3d(this.midpoint);
                    } else {
                        child.lowerBound = new Vector3d(
                            this.midpoint.x,
                            this.lowerBound.y,
                            this.lowerBound.z
                        );
                        child.upperBound = new Vector3d(
                            this.upperBound.x,
                            this.midpoint.y,
                            this.midpoint.z
                        );
                    }
                } else {
                    if(child.midpoint.x < this.midpoint.x){
                        child.lowerBound = new Vector3d(
                            this.lowerBound.x,
                            this.midpoint.y,
                            this.lowerBound.z
                        );
                        child.upperBound = new Vector3d(
                            this.midpoint.x,
                            this.upperBound.y,
                            this.midpoint.z
                        );
                    } else {
                        child.lowerBound = new Vector3d(
                            this.midpoint.x,
                            this.midpoint.y,
                            this.lowerBound.z
                        );
                        child.upperBound = new Vector3d(
                            this.upperBound.x,
                            this.upperBound.y,
                            this.midpoint.z
                        );
                    }
                }
            } else {
                if(child.midpoint.y < this.midpoint.y){
                    if(child.midpoint.x < this.midpoint.x){
                        child.lowerBound = new Vector3d(
                            this.lowerBound.x,
                            this.lowerBound.y,
                            this.midpoint.z
                        );
                        child.upperBound = new Vector3d(
                            this.midpoint.x,
                            this.midpoint.y,
                            this.upperBound.z
                        );
                    } else {
                        child.lowerBound = new Vector3d(
                            this.midpoint.x,
                            this.lowerBound.y,
                            this.midpoint.z
                        );
                        child.upperBound = new Vector3d(
                            this.upperBound.x,
                            this.midpoint.y,
                            this.upperBound.z
                        );
                    }
                } else {
                    if(child.midpoint.x < this.midpoint.x){
                        child.lowerBound = new Vector3d(
                            this.lowerBound.x,
                            this.midpoint.y,
                            this.midpoint.z
                        );
                        child.upperBound = new Vector3d(
                            this.midpoint.x,
                            this.upperBound.y,
                            this.upperBound.z
                        );
                    } else {
                        child.lowerBound = new Vector3d(this.midpoint);
                        child.upperBound = new Vector3d(this.upperBound);
                    }
                }
            }
        }

        /**
         * Gets the midpoint of a given quadrant
         * @param quadrant The quadrant
         * @return The midpoint
         */
        private Vector3d getQuadrantMidpoint(int quadrant){
            Vector3d lowerBound = null;
            Vector3d upperBound = null;
            if(quadrant == 0){
                lowerBound = new Vector3d(this.lowerBound);
                upperBound = new Vector3d(this.midpoint);
            } else if(quadrant == 1) {
                lowerBound = new Vector3d(
                    this.midpoint.x,
                    this.lowerBound.y,
                    this.lowerBound.z
                );
                upperBound = new Vector3d(
                    this.upperBound.x,
                    this.midpoint.y,
                    this.midpoint.z
                );
            } else if(quadrant == 2){
                lowerBound = new Vector3d(
                    this.lowerBound.x,
                    this.midpoint.y,
                    this.lowerBound.z
                );
                upperBound = new Vector3d(
                    this.midpoint.x,
                    this.upperBound.y,
                    this.midpoint.z
                );
            } else if(quadrant == 3) {
                lowerBound = new Vector3d(
                    this.midpoint.x,
                    this.midpoint.y,
                    this.lowerBound.z
                );
                upperBound = new Vector3d(
                    this.upperBound.x,
                    this.upperBound.y,
                    this.midpoint.z
                );
            } else if(quadrant == 4){
                lowerBound = new Vector3d(
                    this.lowerBound.x,
                    this.lowerBound.y,
                    this.midpoint.z
                );
                upperBound = new Vector3d(
                    this.midpoint.x,
                    this.midpoint.y,
                    this.upperBound.z
                );
            } else if(quadrant == 5) {
                lowerBound = new Vector3d(
                    this.midpoint.x,
                    this.lowerBound.y,
                    this.midpoint.z
                );
                upperBound = new Vector3d(
                    this.upperBound.x,
                    this.midpoint.y,
                    this.upperBound.z
                );
            } else if(quadrant == 6){
                lowerBound = new Vector3d(
                    this.lowerBound.x,
                    this.midpoint.y,
                    this.midpoint.z
                );
                upperBound = new Vector3d(
                    this.midpoint.x,
                    this.upperBound.y,
                    this.upperBound.z
                );
            } else if(quadrant == 7) {
                lowerBound = new Vector3d(this.midpoint);
                upperBound = new Vector3d(this.upperBound);
            } else {
                throw new IllegalArgumentException("Trying to get midpoint of invalid quadrant!" + quadrant);
            }
            Vector3d midpoint = new Vector3d(
                ((upperBound.x - lowerBound.x) / 2.0) + lowerBound.x,
                ((upperBound.y - lowerBound.y) / 2.0) + lowerBound.y,
                ((upperBound.z - lowerBound.z) / 2.0) + lowerBound.z
            );
            return midpoint;
        }

        /**
         * Forks a quadrant
         * @param location The location within the quadrant
         * @return The midpoint of the new node
         */
        private RealOctreeNode<T> forkQuadrant(Vector3d location){
            int quadrant = this.getQuadrant(location);
            Vector3d midpoint = this.getQuadrantMidpoint(quadrant);
            //create and add the non-leaf node
            RealOctreeNode<T> nonLeaf = new RealOctreeNode<T>(midpoint);
            this.addChild(nonLeaf);
            return nonLeaf;
        }

    }

}
