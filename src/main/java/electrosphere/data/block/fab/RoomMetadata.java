package electrosphere.data.block.fab;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3d;

import electrosphere.client.interact.select.AreaSelection;

/**
 * Metadata about a room
 */
public class RoomMetadata {
    
    /**
     * The area encompasing the room
     */
    AreaSelection area;

    /**
     * The list of slots that can have furniture placed on them
     */
    List<FurnitureSlotMetadata> furnitureSlots = new LinkedList<FurnitureSlotMetadata>();

    /**
     * The list of entrypoints to the room
     */
    List<Vector3d> entryPoints = new LinkedList<Vector3d>();

    /**
     * Constructor
     * @param area The area of the room
     */
    public RoomMetadata(AreaSelection area){
        this.area = area;
    }

    /**
     * Gets the area of the room
     * @return The area of the room
     */
    public AreaSelection getArea() {
        return area;
    }

    /**
     * Sets the area of the room
     * @param area The area of the room
     */
    public void setArea(AreaSelection area) {
        this.area = area;
    }

    /**
     * Gets the furniture slots of the room
     * @return The furniture slots of the room
     */
    public List<FurnitureSlotMetadata> getFurnitureSlots() {
        return furnitureSlots;
    }

    /**
     * Sets the furniture slots of the room
     * @param furnitureSlots The furniture slots
     */
    public void setFurnitureSlots(List<FurnitureSlotMetadata> furnitureSlots) {
        this.furnitureSlots = furnitureSlots;
    }

    /**
     * Gets the entry points of the room
     * @return The entry points of the room
     */
    public List<Vector3d> getEntryPoints() {
        return entryPoints;
    }

    /**
     * Sets the entry points of the room
     * @param entryPoints The entry points
     */
    public void setEntryPoints(List<Vector3d> entryPoints) {
        this.entryPoints = entryPoints;
    }

    

}
