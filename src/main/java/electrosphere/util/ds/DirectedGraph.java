package electrosphere.util.ds;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A directed graph
 */
public class DirectedGraph {

    /**
     * The list of nodes in the graph
     */
    List<GraphNode> nodes;
    
    /**
     * Constructor
     */
    public DirectedGraph(){
        this.nodes = new LinkedList<GraphNode>();
    }

    /**
     * Creates a node in the graph
     * @param data The data in the node
     * @return The node
     */
    public GraphNode createNode(Object data){
        GraphNode rVal = new GraphNode();
        this.nodes.add(rVal);
        return rVal;
    }

    /**
     * Adds direction as a neighbor of source. Does not create a connection from direction to source
     * @param source The source node
     * @param direction The destination node
     */
    public void pointNode(GraphNode source, GraphNode direction){
        if(!source.containsNeighbor(direction)){
            source.addNeighbor(direction);
        }
    }

    /**
     * Mutually connects two nodes
     * @param node1 Node 1
     * @param node2 Node 2
     */
    public void connectNodes(GraphNode node1, GraphNode node2){
        if(!node1.containsNeighbor(node2)){
            node1.addNeighbor(node2);
        }
        if(!node2.containsNeighbor(node1)){
            node2.addNeighbor(node1);
        }
    }

    /**
     * Destroys a node
     * @param node The node to destroy
     */
    public void destroyNode(GraphNode node){
        for(GraphNode toEval : this.nodes){
            if(toEval != node){
                if(toEval.containsNeighbor(node)){
                    toEval.removeNeighbor(node);
                }
            }
        }
        this.nodes.remove(node);
    }

    /**
     * Gets the nodes in the graph
     * @return The list of all nodes in the graph
     */
    public List<GraphNode> getNodes(){
        return Collections.unmodifiableList(this.nodes);
    }
    


    /**
     * A node in a graph
     */
    public static class GraphNode {

        /**
         * The data at the node
         */
        Object data;

        /**
         * The neighbors of this graph node
         */
        List<GraphNode> neighbors;

        /**
         * Creates a graph node
         * @param data The data to put in the node
         */
        public GraphNode(Object data){
            this.data = data;
            this.neighbors = new LinkedList<GraphNode>();
        }

        /**
         * Creates an empty graph node
         */
        public GraphNode(){
            this.data = null;
            this.neighbors = new LinkedList<GraphNode>();
        }

        /**
         * Gets the data at this node
         * @return The data
         */
        public Object getData(){
            return this.data;
        }

        /**
         * Gets the neighbors of this node
         * @return The list of neighbors
         */
        public List<GraphNode> getNeighbors(){
            return this.neighbors;
        }

        /**
         * Adds a neighbor to this node
         * @param neighbor The neighbor
         */
        public void addNeighbor(GraphNode neighbor){
            this.neighbors.add(neighbor);
        }

        /**
         * Removes a neighbor from this node
         * @param neighbor THe neighbor
         */
        public void removeNeighbor(GraphNode neighbor){
            this.neighbors.remove(neighbor);
        }

        /**
         * Checks if this node contains a given node as a neighbor
         * @param node The potential neighbor to check
         * @return true if the node is a neighbor, false otherwise
         */
        public boolean containsNeighbor(GraphNode node){
            return this.neighbors.contains(node);
        }

    }

    
}
