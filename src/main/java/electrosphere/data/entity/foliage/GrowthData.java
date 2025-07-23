package electrosphere.data.entity.foliage;

import electrosphere.data.entity.common.life.loot.LootPool;

/**
 * Data that controls the growth characteristics of an entity that grows into something
 */
public class GrowthData {
    
    /**
     * The maximum value to grow to
     */
    Integer growthMax;

    /**
     * The scale to be at when the entity finishes growing
     */
    Double scaleMax;

    /**
     * The loot pool for when the entity completes growing
     */
    LootPool maxGrowthLoot;

    /**
     * Gets the maximum value to grow to
     * @return The maximum value to grow to
     */
    public Integer getGrowthMax() {
        return growthMax;
    }

    /**
     * Gets the loot pool to drop on finishing growing
     * @return The loot pool
     */
    public LootPool getMaxGrowthLoot() {
        return maxGrowthLoot;
    }

    /**
     * Gets the scale for when the entity finishes growing
     * @return The scale
     */
    public Double getScaleMax() {
        return scaleMax;
    }

}
