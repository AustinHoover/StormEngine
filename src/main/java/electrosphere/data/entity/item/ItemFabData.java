package electrosphere.data.entity.item;

/**
 * Data for placing fabs
 */
public class ItemFabData {
    
    /**
     * The path for the fab to place when this item is consumed
     */
    String fabPath;

    /**
     * Gets the path for the corresponding fab file
     * @return The path for the corresponding fab file
     */
    public String getFabPath() {
        return fabPath;
    }

    /**
     * Sets the path for the corresponding fab file
     * @param fabPath The path for the corresponding fab file
     */
    public void setFabPath(String fabPath) {
        this.fabPath = fabPath;
    }

}
