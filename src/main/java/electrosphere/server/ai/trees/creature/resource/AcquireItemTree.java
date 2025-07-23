package electrosphere.server.ai.trees.creature.resource;

import electrosphere.collision.CollisionEngine;
import electrosphere.data.entity.item.source.ItemSourcingData.SourcingType;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.actions.interact.CollectItemNode;
import electrosphere.server.ai.nodes.actions.interact.CraftNode;
import electrosphere.server.ai.nodes.actions.interact.HarvestNode;
import electrosphere.server.ai.nodes.checks.inventory.SourcingTypeNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.RunnerNode;
import electrosphere.server.ai.nodes.meta.decorators.SucceederNode;
import electrosphere.server.ai.nodes.plan.SolveSourcingTreeNode;
import electrosphere.server.ai.nodes.plan.TargetEntityCategoryNode;
import electrosphere.server.ai.trees.creature.MoveToTree;
import electrosphere.server.ai.trees.creature.explore.ExploreTree;

/**
 * A tree to acquire an item
 */
public class AcquireItemTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "AcquireItem";

    /**
     * Creates a acquire-item tree
     * @param key The blackboard key to search for the item name under
     * @return The root node of the acquire-item tree
     */
    public static AITreeNode create(String blackboardKey){
        return new SequenceNode(
            "AcquireItemTree",
            new PublishStatusNode("Acquire an item"),
            //solve how we're going to get this top level item
            new SolveSourcingTreeNode(blackboardKey),
            new SelectorNode(
                new SequenceNode(
                    "AcquireItemTree",
                    new PublishStatusNode("Pick up an item"),
                    //check if we should be sourcing this item by picking it up
                    new SourcingTypeNode(SourcingType.PICKUP, BlackboardKeys.ITEM_TARGET_CATEGORY),
                    //logic to pick up the item
                    new TargetEntityCategoryNode(BlackboardKeys.ITEM_TARGET_CATEGORY),
                    MoveToTree.create(CollisionEngine.DEFAULT_INTERACT_DISTANCE, BlackboardKeys.ENTITY_TARGET),
                    new CollectItemNode(),
                    new RunnerNode(null)
                ),
                new SequenceNode(
                    "AcquireItemTree",
                    new PublishStatusNode("Craft an item"),
                    //check if we should be sourcing this from a recipe
                    new SourcingTypeNode(SourcingType.RECIPE, blackboardKey),
                    new CraftNode(),
                    new RunnerNode(null)
                ),
                new SequenceNode(
                    "AcquireItemTree",
                    new PublishStatusNode("Harvest an item"),
                    //check if we should be sourcing this from harvesting foliage
                    new SourcingTypeNode(SourcingType.HARVEST, blackboardKey),
                    new TargetEntityCategoryNode(BlackboardKeys.HARVEST_TARGET_TYPE),
                    MoveToTree.create(CollisionEngine.DEFAULT_INTERACT_DISTANCE, BlackboardKeys.ENTITY_TARGET),
                    new HarvestNode(),
                    new RunnerNode(null)
                ),
                new SequenceNode(
                    "AcquireItemTree",
                    new PublishStatusNode("Fell a tree"),
                    //check if we should be sourcing this from felling a tree
                    new SourcingTypeNode(SourcingType.TREE, blackboardKey),
                    new TargetEntityCategoryNode(BlackboardKeys.HARVEST_TARGET_TYPE),
                    FellTree.create(BlackboardKeys.ENTITY_TARGET),
                    new RunnerNode(null)
                ),
                new SequenceNode(
                    "AcquireItemTree",
                    new PublishStatusNode("Explore new chunks for resources"),
                    //Failed to find sources of material in existing chunks, must move for new chunks
                    ExploreTree.create()
                )
            ),
            new SucceederNode(null)
        );
    }

}
