package electrosphere.data.entity.common.life;

import electrosphere.data.entity.common.life.loot.LootPool;
import electrosphere.data.entity.common.treedata.TreeDataState;

/**
 * Data about the health of a creature
 */
public class HealthSystem {

    /**
     * The maximum health
     */
    int maxHealth;

    /**
     * The number of iframes on taking damage
     */
    int onDamageIFrames;

    /**
     * The dying state
     */
    TreeDataState dyingState;

    /**
     * The loot pool that can be dropped by this entity on death
     */
    LootPool lootPool;

    /**
     * Gets the maximum health
     * @return The maximum health
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Gets the number of iframes on damage
     * @return The number of iframes
     */
    public int getOnDamageIFrames() {
        return onDamageIFrames;
    }

    /**
     * Gets the dying state data
     * @return The dying state data
     */
    public TreeDataState getDyingState(){
        return dyingState;
    }

    /**
     * Gets the loot pool that can be dropped by this entity on death
     * @return The loot pool that can be dropped by this entity on death
     */
    public LootPool getLootPool(){
        return this.lootPool;
    }
    
}
