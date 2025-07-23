package electrosphere.server.ai.trees.creature.inventory;

import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.actions.inventory.EquipToolbarNode;
import electrosphere.server.ai.nodes.checks.inventory.InventoryContainsNode;
import electrosphere.server.ai.nodes.meta.DataTransferNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;

/**
 * Tries to equip an item into the toolbar
 */
public class EquipToolbarTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "EquipToolbar";

    /**
     * Creates an equip toolbar tree
     * @param itemType The type of item to try to equip in the toolbar
     * @return The root node of the tree
     */
    public static AITreeNode create(String targetKey){
        return new SequenceNode(
            "EquipToolbar",
            new PublishStatusNode("Equip an item"),
            //check that we have this type of item
            new DataTransferNode(targetKey, BlackboardKeys.INVENTORY_CHECK_TYPE),
            new InventoryContainsNode(BlackboardKeys.INVENTORY_CHECK_TYPE),

            //try to equip the item to the toolbar
            EquipToolbarNode.equipItem(BlackboardKeys.INVENTORY_CHECK_TYPE)
        );
    }

}
