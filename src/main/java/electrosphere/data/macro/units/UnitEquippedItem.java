package electrosphere.data.macro.units;

/**
 * An item equipped to a unit
 */
public class UnitEquippedItem {
    
    /**
     * The equip point that has an item equipped
     */
    String pointId;

    /**
     * The id of the item attached to the point
     */
    String itemId;

    /**
     * Gets the equip point that has an item attached
     * @return The equip point
     */
    public String getPointId(){
        return pointId;
    }

    /**
     * Gets the id of the item equipped to the point
     * @return The id of the item
     */
    public String getItemId(){
        return itemId;
    }

}
