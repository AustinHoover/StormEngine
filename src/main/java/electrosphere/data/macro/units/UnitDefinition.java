package electrosphere.data.macro.units;

import java.util.List;

import electrosphere.data.entity.creature.ai.AITreeData;

/**
 * A creature associated with some equipment and AI
 */
public class UnitDefinition {
    
    /**
     * The name of the unit
     */
    String id;

    /**
     * The creature that is the base of this unit
     */
    String creatureId;

    /**
     * The ai trees associated with this unit
     */
    List<AITreeData> ai;

    /**
     * The items equipped to the unit
     */
    List<UnitEquippedItem> equipment;

    /**
     * Gets the id of the unit
     * @return The id
     */
    public String getId(){
        return id;
    }

    /**
     * Gets the id of the creature associated with the unit
     * @return The id of the creature
     */
    public String getCreatureId(){
        return creatureId;
    }

    /**
     * Gets the AI associated with the unit
     * @return The ai
     */
    public List<AITreeData> getAI(){
        return ai;
    }

    /**
     * Gets the equipment attached to the unit
     * @return The equipment
     */
    public List<UnitEquippedItem> getEquipment(){
        return equipment;
    }

}
