package electrosphere.server.ai.nodes.plan;

import electrosphere.data.entity.item.source.ItemSourcingData;
import electrosphere.data.entity.item.source.ItemSourcingTree;
import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.actions.interact.HarvestNode;

/**
 * Solves a dependency tree for how to acquire a given item
 */
public class SolveSourcingTreeNode implements AITreeNode {
    
    /**
     * Blackboard key that stores the id of the item to source
     */
    String itemIdKey;

    /**
     * Constructor
     * @param itemIdKey The blackboard key that stores the id of the item to calculate sourcing for
     */
    public SolveSourcingTreeNode(String itemIdKey){
        this.itemIdKey = itemIdKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!blackboard.has(itemIdKey)){
            return AITreeNodeResult.FAILURE;
        }
        String itemId = (String)blackboard.get(itemIdKey);
        if(!SolveSourcingTreeNode.hasItemSourcingTree(blackboard) || !SolveSourcingTreeNode.getItemSourcingTree(blackboard).getRootItem().equals(itemId)){
            ItemSourcingTree sourcingTree = ItemSourcingTree.create(itemId);
            SolveSourcingTreeNode.setItemSourcingTree(blackboard, sourcingTree);
        }
        ItemSourcingTree sourcingTree = SolveSourcingTreeNode.getItemSourcingTree(blackboard);
        ItemSourcingData sourcingData = sourcingTree.getCurrentDependency(entity);
        if(sourcingData == null){
            throw new Error("Source data is null! " + itemId);
        }
        //set the type to harvest if this is a harvest type
        if(sourcingData.getHarvestTargets().size() > 0){
            HarvestNode.setHarvestTargetType(blackboard, sourcingData.getHarvestTargets().get(0).getId());
        } else if(sourcingData.getTrees().size() > 0){
            HarvestNode.setHarvestTargetType(blackboard, sourcingData.getTrees().get(0).getId());
        }
        SolveSourcingTreeNode.setItemSourcingData(blackboard, sourcingData);
        SolveSourcingTreeNode.setItemTargetCategory(blackboard, sourcingData.getGoalItem());
        return AITreeNodeResult.SUCCESS;
    }

    /**
     * Sets the item sourcing tree of the blackboard
     * @param blackboard The blackboard
     * @param tree The tree
     */
    public static void setItemSourcingTree(Blackboard blackboard, ItemSourcingTree tree){
        blackboard.put(BlackboardKeys.ITEM_SOURCING_TREE, tree);
    }

    /**
     * Checks if the blackboard has an item sourcing tree
     * @param blackboard The blackboard
     * @return The item sourcing tree
     */
    public static boolean hasItemSourcingTree(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.ITEM_SOURCING_TREE);
    }

    /**
     * Gets the item sourcing tree of the blackboard
     * @param blackboard The blackboard
     * @return The item sourcing tree
     */
    public static ItemSourcingTree getItemSourcingTree(Blackboard blackboard){
        return (ItemSourcingTree)blackboard.get(BlackboardKeys.ITEM_SOURCING_TREE);
    }

    /**
     * Sets the item sourcing data of the blackboard
     * @param blackboard The blackboard
     * @param tree The data
     */
    public static void setItemSourcingData(Blackboard blackboard, ItemSourcingData data){
        blackboard.put(BlackboardKeys.ITEM_SOURCING_DATA, data);
    }

    /**
     * Checks if the blackboard has an item sourcing data
     * @param blackboard The blackboard
     * @return The item sourcing data
     */
    public static boolean hasItemSourcingData(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.ITEM_SOURCING_DATA);
    }

    /**
     * Gets the item sourcing data of the blackboard
     * @param blackboard The blackboard
     * @return The item sourcing data
     */
    public static ItemSourcingData getItemSourcingData(Blackboard blackboard){
        return (ItemSourcingData)blackboard.get(BlackboardKeys.ITEM_SOURCING_DATA);
    }

    /**
     * Sets the item target category of the blackboard
     * @param blackboard The blackboard
     * @param tree The data
     */
    public static void setItemTargetCategory(Blackboard blackboard, String data){
        blackboard.put(BlackboardKeys.ITEM_TARGET_CATEGORY, data);
    }

    /**
     * Checks if the blackboard has an item target category
     * @param blackboard The blackboard
     * @return The item sourcing data
     */
    public static boolean hasItemTargetCategory(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.ITEM_TARGET_CATEGORY);
    }

    /**
     * Gets the item target category of the blackboard
     * @param blackboard The blackboard
     * @return The item target category
     */
    public static String getItemTargetCategory(Blackboard blackboard){
        return (String)blackboard.get(BlackboardKeys.ITEM_TARGET_CATEGORY);
    }

}
