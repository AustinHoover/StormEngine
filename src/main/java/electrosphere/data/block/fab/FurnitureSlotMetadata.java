package electrosphere.data.block.fab;

import electrosphere.client.interact.select.AreaSelection;

/**
 * Metadata for a slot that furniture can be placed into
 */
public class FurnitureSlotMetadata {
    
    /**
     * The area encompasing the slot
     */
    AreaSelection area;


    /**
     * Constructor
     * @param area The area that furniture can be placed into
     */
    public FurnitureSlotMetadata(AreaSelection area){
        this.area = area;
    }

    /**
     * Gets the area that encompasses the furniture slot
     * @return The area
     */
    public AreaSelection getArea(){
        return area;
    }

}
