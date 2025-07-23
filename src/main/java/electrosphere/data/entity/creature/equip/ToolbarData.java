package electrosphere.data.entity.creature.equip;

/**
 * Data about the toolbar
 */
public class ToolbarData {
    
    /**
     * The id for the primary toolbar equip point
     */
    String primarySlot;

    /**
     * The id for the combined toolbar equip point
     */
    String combinedSlot;

    /**
     * Gets the id for the primary toolbar equip point
     * @return The id for the primary toolbar equip point
     */
    public String getPrimarySlot(){
        return primarySlot;
    }

    /**
     * Gets the id for the combined toolbar equip point
     * @return The id for the combined toolbar equip point
     */
    public String getCombinedSlot(){
        return combinedSlot;
    }

}
