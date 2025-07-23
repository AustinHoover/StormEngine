package electrosphere.server.ai.nodes.checks.inventory;

import java.util.List;
import java.util.stream.Collectors;

import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Checks if any of the inventories on a given entity contain the target item type
 */
public class InventoryContainsNode implements AITreeNode {

    /**
     * The key to look for the item type in
     */
    String key = BlackboardKeys.INVENTORY_CHECK_TYPE;

    /**
     * Constructor
     * @param key The key to lookup the entity type under
     */
    public InventoryContainsNode(String key){
        this.key = key;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {

        //key isn't defined
        if(!blackboard.has(key)){
            return AITreeNodeResult.FAILURE;
        }

        //type to look for
        String type = (String)blackboard.get(this.key);

        List<Entity> items = InventoryUtils.getAllInventoryItems(entity);
        List<String> itemIds = items.stream().map((Entity itemEnt) -> {return CommonEntityUtils.getEntitySubtype(itemEnt);}).collect(Collectors.toList());
        if(itemIds.contains(type)){
            return AITreeNodeResult.SUCCESS;
        }
        return AITreeNodeResult.FAILURE;
    }

    /**
     * Sets the type of item to check for
     * @param blackboard The blackboard
     * @param type The type of item to check for
     */
    public static void setInventoryCheckType(Blackboard blackboard, String type){
        blackboard.put(BlackboardKeys.INVENTORY_CHECK_TYPE, type);
    }

    /**
     * Checks if this has an item type to search for
     * @param blackboard The blackboard
     * @return true if there is an item type to check for, false otherwise
     */
    public static boolean hasInventoryCheckType(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.INVENTORY_CHECK_TYPE);
    }

    /**
     * Gets the item type to check for
     * @param blackboard The blackboard
     * @return The item type to check for if it exists, null otherwise
     */
    public static String getInventoryCheckType(Blackboard blackboard){
        return (String)blackboard.get(BlackboardKeys.INVENTORY_CHECK_TYPE);
    }
    
}
