package electrosphere.data.entity.furniture;

import electrosphere.data.entity.common.treedata.TreeDataState;

/**
 * Data about how a door functions
 */
public class DoorData {
    
    /**
     * Tree data for when the door is already open
     */
    TreeDataState open;

    /**
     * Tree data for when the door is already closed
     */
    TreeDataState closed;

    /**
     * Tree data for when the door is beginning to open
     */
    TreeDataState opening;

    /**
     * Tree data for when the door is beginning to close
     */
    TreeDataState closing;

    /**
     * Gets the Tree data for when the door is already open
     * @return The Tree data for when the door is already open
     */
    public TreeDataState getOpen() {
        return open;
    }

    /**
     * Gets the Tree data for when the door is already closed
     * @return The Tree data for when the door is already closed
     */
    public TreeDataState getClosed() {
        return closed;
    }

    /**
     * Gets the Tree data for when the door is beginning to open
     * @return the Tree data for when the door is beginning to open
     */
    public TreeDataState getOpening() {
        return opening;
    }

    /**
     * Gets the Tree data for when the door is beginning to close
     * @return the Tree data for when the door is beginning to close
     */
    public TreeDataState getClosing() {
        return closing;
    }

    

}
