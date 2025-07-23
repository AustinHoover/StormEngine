package electrosphere.client.scene;

import org.joml.Vector3d;

import electrosphere.data.block.fab.BlockFab;

/**
 * Stores the data for the client's level edits
 */
public class ClientLevelEditorData {
    
    /**
     * The currently edited fab
     */
    private BlockFab currentFab;

    /**
     * The origin point of the current fab
     */
    private Vector3d currentFabOrigin;

    /**
     * Gets the currently edited fab
     * @return The fab if it exists, null otherwise
     */
    public BlockFab getCurrentFab() {
        return currentFab;
    }

    /**
     * Sets the fab currently being edited
     * @param currentFab The fab
     */
    public void setCurrentFab(BlockFab currentFab) {
        this.currentFab = currentFab;
    }

    /**
     * Gets the origin point of the current fab
     * @return The origin point
     */
    public Vector3d getCurrentFabOrigin() {
        return currentFabOrigin;
    }

    /**
     * Sets the origin point of the current fab
     * @param currentFabOrigin The origin point
     */
    public void setCurrentFabOrigin(Vector3d currentFabOrigin) {
        this.currentFabOrigin = currentFabOrigin;
    }

    

}
