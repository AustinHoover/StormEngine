package electrosphere.server.macro.spatial.path;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3d;

import electrosphere.util.annotation.Exclude;

/**
 * A storage object for macro path nodes
 */
public class MacroPathCache {
    
    /**
     * The nodes in the cache
     */
    private List<MacroPathNode> nodes = new LinkedList<MacroPathNode>();


    /**
     * Map of node id -> node
     */
    @Exclude
    private Map<Long,MacroPathNode> idNodeMap = new HashMap<Long,MacroPathNode>();

    /**
     * Reconstructs the datastructures for this cache after deserialization
     */
    public void reconstruct(){
        for(MacroPathNode node : nodes){
            this.idNodeMap.put(node.getId(),node);
        }
    }

    /**
     * Gets the nodes in this cache
     * @return The list of nodes
     */
    public List<MacroPathNode> getNodes() {
        return nodes;
    }

    /**
     * Gets a node by its id
     * @param id The id
     * @return The corresponding node if it exists, null otherwise
     */
    public MacroPathNode getNodeById(long id){
        return idNodeMap.get(id);
    }

    /**
     * Registers a node with the pathing cache
     * @param node The node
     */
    public void registerNode(MacroPathNode node){
        node.setId(nodes.size());
        nodes.add(node);
        idNodeMap.put(node.getId(),node);
    }

    /**
     * Gets the pathing node at a given point
     * @param point The point
     * @return The corresponding pathing node
     */
    public MacroPathNode getPathingNode(Vector3d point){
        double minDist = 100;
        MacroPathNode rVal = null;
        for(MacroPathNode node : this.nodes){
            double dist = point.distance(node.getPosition());
            if(dist < minDist){
                minDist = dist;
                rVal = node;
            }
        }
        return rVal;
    }

}
