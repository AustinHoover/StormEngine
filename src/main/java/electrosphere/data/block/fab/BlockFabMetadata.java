package electrosphere.data.block.fab;

import java.util.LinkedList;
import java.util.List;

import electrosphere.client.interact.select.AreaSelection;

/**
 * Metdata associated with the fab
 */
public class BlockFabMetadata {
    
    /**
     * Area data for the fab
     */
    private List<AreaSelection> areas;

    /**
     * The structure metadata for the fab
     */
    private StructureMetadata structureData;

    /**
     * Constructor
     */
    protected BlockFabMetadata(){
        this.areas = new LinkedList<AreaSelection>();
    }

    /**
     * Gets the areas defined in the metadata
     * @return The areas
     */
    public List<AreaSelection> getAreas() {
        return areas;
    }

    /**
     * Sets the areas defined in the metadata
     * @param areas The areas
     */
    public void setAreas(List<AreaSelection> areas) {
        this.areas = areas;
    }

    /**
     * Gets the structure data for the fab
     * @return The structure data
     */
    public StructureMetadata getStructureData(){
        return structureData;
    }

    /**
     * Sets the structure data for the fab
     * @param structureMetadata The structure data
     */
    public void setStructureData(StructureMetadata structureData){
        this.structureData = structureData;
    }
    
}
