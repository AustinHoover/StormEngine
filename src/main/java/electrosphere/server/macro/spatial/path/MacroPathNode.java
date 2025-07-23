package electrosphere.server.macro.spatial.path;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.joml.Vector3d;

import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.structure.VirtualStructure;

/**
 * A macro pathfinding node
 */
public class MacroPathNode {

    //
    // Node types
    //

    /**
     * A node that represents a point in a node
     */
    private static final int TYPE_ROAD_NODE = 0;

    /**
     * A node that represents a structure
     */
    private static final int TYPE_VIRTUAL_STRUCTURE = 1;

    /**
     * A region
     */
    private static final int TYPE_REGION = 2;


    //
    // cost values
    //

    /**
     * Cost of a virtual structure
     */
    private static final int COST_VIRTUAL_STRUCTURE = 10;

    /**
     * Cost of a region object
     */
    private static final int COST_REGION = 8;

    /**
     * Cost of a road node
     */
    private static final int COST_ROAD_NODE = 1;


    //
    // node data
    //
    
    /**
     * The id of this node
     */
    private long id;

    /**
     * The id of the object that this node corresponds to
     */
    private long objectId;

    /**
     * The type of object at the node
     */
    private int objectType;

    /**
     * The ids of neighboring path nodes
     */
    private List<Long> neighborNodes = new LinkedList<Long>();

    /**
     * The position of the node
     */
    private Vector3d position;

    /**
     * The cost of this node
     */
    private int cost;

    /**
     * Private constructor
     */
    private MacroPathNode(){ }

    /**
     * Creates a macro path node
     * @param cache The path cache
     * @param correspondingObject The object that this pathing node corresponds to
     * @param position The position of this node
     * @return The macro path node
     */
    public static MacroPathNode create(MacroPathCache cache, Object correspondingObject, Vector3d position){
        MacroPathNode rVal = new MacroPathNode();
        cache.registerNode(rVal);

        //set data on the node
        rVal.position = position;

        //set data based on corresponding object
        if(correspondingObject instanceof VirtualStructure structObj){
            rVal.cost = COST_VIRTUAL_STRUCTURE;
            rVal.objectId = structObj.getId();
            rVal.objectType = TYPE_VIRTUAL_STRUCTURE;
        } else if(correspondingObject instanceof MacroRegion regionObj){
            rVal.cost = COST_REGION;
            rVal.objectId = regionObj.getId();
            rVal.objectType = TYPE_REGION;
        } else {
            throw new Error("Unsupported object type! " + correspondingObject);
        }

        return rVal;
    }

    /**
     * Creates a macro path node that represents a node in a road
     * @param cache The path cache
     * @param position The position of this node
     * @return The macro path node
     */
    public static MacroPathNode createRoadNode(MacroPathCache cache, Vector3d position){
        MacroPathNode rVal = new MacroPathNode();
        cache.registerNode(rVal);

        //set data on the node
        rVal.position = position;

        //set data based on corresponding object
        rVal.cost = COST_ROAD_NODE;
        rVal.objectType = TYPE_ROAD_NODE;

        return rVal;
    }

    /**
     * Gets the associated id 
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id of this path node
     * @param id The id of this path node
     */
    protected void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the type of the object associated with this node
     * @return The type of the object
     */
    public int getObjectType() {
        return objectType;
    }

    /**
     * Sets the type of the object associated with this node
     * @param type The type of the object
     */
    public void setObjectType(int type) {
        this.objectType = type;
    }

    /**
     * Gets the id of the object associated with this node
     * @return The id of the object
     */
    public long getObjectId() {
        return objectId;
    }

    /**
     * Sets the id of the object associated with this node
     * @param objectId The id of the object
     */
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    /**
     * Gets the list of node ids that are neighbors to this node
     * @param cache The pathing cache
     * @return The list of node ids
     */
    public List<MacroPathNode> getNeighborNodes(MacroPathCache cache) {
        return this.neighborNodes.stream().map((Long neighborId) -> cache.getNodeById(neighborId)).collect(Collectors.toList());
    }

    /**
     * Gets the list of neighbor ids
     * @return The list of neighbor ids
     */
    public List<Long> getNeighborIds(){
        return this.neighborNodes;
    }

    /**
     * Adds a neighbor to this node
     * @param neighbor The neighbor
     */
    public void addNeighbor(MacroPathNode neighbor){
        if(neighbor == null){
            throw new Error("Invalid neighbor! " + neighbor);
        }
        if(!this.neighborNodes.contains(neighbor.getId())){
            this.neighborNodes.add(neighbor.getId());
            neighbor.addNeighbor(this);
        }
    }

    /**
     * Gets the position of the node
     * @return The position of the node
     */
    public Vector3d getPosition() {
        return position;
    }

    /**
     * Gets the cost of the node
     * @return The cost of the node
     */
    public int getCost() {
        return cost;
    }

}
