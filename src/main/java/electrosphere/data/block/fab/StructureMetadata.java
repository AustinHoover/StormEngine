package electrosphere.data.block.fab;

import java.util.LinkedList;
import java.util.List;

import electrosphere.client.interact.select.AreaSelection;

/**
 * Structure data
 */
public class StructureMetadata {

    /**
     * The bounding area of the structure
     */
    AreaSelection boundingArea;

    /**
     * The rooms defined within the structure
     */
    List<RoomMetadata> rooms = new LinkedList<RoomMetadata>();


    /**
     * Creatures a structure data object
     * @param boundingArea The bounding area
     * @return The structure data
     */
    public static StructureMetadata create(AreaSelection boundingArea){
        StructureMetadata rVal = new StructureMetadata();
        rVal.boundingArea = boundingArea;
        return rVal;
    }

    /**
     * Gets the bounding area
     * @return The bounding area
     */
    public AreaSelection getBoundingArea(){
        return boundingArea;
    }

    /**
     * Gets the list of areas that encompass rooms
     * @return The list of areas
     */
    public List<RoomMetadata> getRooms(){
        return rooms;
    }

}

