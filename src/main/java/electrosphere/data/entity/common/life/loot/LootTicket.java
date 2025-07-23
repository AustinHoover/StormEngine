package electrosphere.data.entity.common.life.loot;

/**
 * A ticket that can be selected when dropping from the loot pool
 */
public class LootTicket {

    /**
     * The id of the type of item that can be dropped
     */
    String itemId;

    /**
     * The rarity of this item dropping
     */
    double rarity;

    /**
     * The minimum quantity that can be dropped from this ticket
     */
    int minQuantity;

    /**
     * The maximum quantity that can be dropped from this ticket
     */
    int maxQuantity;

    /**
     * Gets the id of the type of item that can be dropped
     * @return The id of the type of item that can be dropped
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets the id of the type of item that can be dropped
     * @param itemId The id of the type of item that can be dropped
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets the rarity of this item dropping
     * @return The rarity of this item dropping
     */
    public double getRarity() {
        return rarity;
    }

    /**
     * Sets the rarity of this item dropping
     * @param rarity The rarity of this item dropping
     */
    public void setRarity(double rarity) {
        this.rarity = rarity;
    }

    /**
     * Gets the minimum quantity that can be dropped from this ticket
     * @return The minimum quantity that can be dropped from this ticket
     */
    public int getMinQuantity() {
        return minQuantity;
    }

    /**
     * Sets the minimum quantity that can be dropped from this ticket
     * @param minQuantity The minimum quantity that can be dropped from this ticket
     */
    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    /**
     * Gets the maximum quantity that can be dropped from this ticket
     * @return The maximum quantity that can be dropped from this ticket
     */
    public int getMaxQuantity() {
        return maxQuantity;
    }

    /**
     * Sets the maximum quantity that can be dropped from this ticket
     * @param maxQuantity The maximum quantity that can be dropped from this ticket
     */
    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    
    
}
