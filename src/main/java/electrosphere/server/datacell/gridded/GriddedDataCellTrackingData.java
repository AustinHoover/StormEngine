package electrosphere.server.datacell.gridded;

/**
 * Data associated with a ServerDataCell by the GriddedDataCellManager
 */
public class GriddedDataCellTrackingData {

    /**
     * A really large distance used to reset the position
     */
    public static final double REALLY_LARGE_DISTANCE = 1000;

    /**
     * The from the cell to the closest player
     */
    double closestPlayer;

    /**
     * The number of creatures in the cell
     */
    int creatureCount;

    /**
     * Gets the distance from the cell to the closest player
     * @return The distance
     */
    public double getClosestPlayer() {
        return closestPlayer;
    }

    /**
     * Sets the distance to the closest player
     * @param closestPlayer The distance to the closest player
     */
    public void setClosestPlayer(double closestPlayer) {
        this.closestPlayer = closestPlayer;
    }

    /**
     * Gets the number of creatures in this data cell
     * @return The number of creatures
     */
    public int getCreatureCount() {
        return creatureCount;
    }

    /**
     * Gets the number of creatures in this cell
     * @param creatureCount The number of creatures in the cell
     */
    public void setCreatureCount(int creatureCount) {
        this.creatureCount = creatureCount;
    }
    
}
