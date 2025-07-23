package electrosphere.data.macro.struct;

import org.joml.Vector3d;

import electrosphere.data.block.fab.BlockFab;
import electrosphere.util.annotation.Exclude;

/**
 * Data about a structure
 */
public class StructureData {
    
    /**
     * The id of the structure
     */
    String id;

    /**
     * The display name of the structure
     */
    String displayName;

    /**
     * The path to the fab for the structure
     */
    String fabPath;

    /**
     * The actually loaded fab
     */
    @Exclude
    BlockFab fab;

    /**
     * The dimensions of the structure
     */
    Vector3d dimensions;

    /**
     * Offset when placing the structure (ie to place a foundation underneath the terrain)
     */
    Vector3d placementOffset;

    /**
     * Sets the id of the structure 
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the display name of the structure
     * @return The display name
     */
    public String getDisplayName(){
        return displayName;
    }

    /**
     * Sets the display name of the structure
     * @param name The display name
     */
    public void setDisplayName(String name){
        this.displayName = name;
    }

    /**
     * Gets the path to the fab for the structure
     * @return The path
     */
    public String getFabPath() {
        return fabPath;
    }

    /**
     * Sets the path to the fab for the struct
     * @param fabPath The path
     */
    public void setFabPath(String fabPath) {
        this.fabPath = fabPath;
    }

    /**
     * Gets the dimensions of the structure
     * @return The dimensions
     */
    public Vector3d getDimensions() {
        return dimensions;
    }

    /**
     * Sets the dimensions of the structure
     * @param dimensions The dimensions
     */
    public void setDimensions(Vector3d dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * Gets the placement offset
     * @return The placemeng offset
     */
    public Vector3d getPlacementOffset() {
        return placementOffset;
    }

    /**
     * Sets the placement offset
     * @param placementOffset The placement offset
     */
    public void setPlacementOffset(Vector3d placementOffset) {
        this.placementOffset = placementOffset;
    }

    /**
     * Gets the fab
     * @return The fab
     */
    public BlockFab getFab(){
        return fab;
    }

    /**
     * Sets the fab
     * @param fab The fab
     */
    public void setFab(BlockFab fab){
        this.fab = fab;
    }
    

}
