package electrosphere.server.ai.trees.creature.resource;

import electrosphere.data.entity.item.ItemIdStrings;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.AITreeNode.AITreeNodeResult;
import electrosphere.server.ai.nodes.actions.combat.AttackStartNode;
import electrosphere.server.ai.nodes.actions.move.FaceTargetNode;
import electrosphere.server.ai.nodes.actions.move.MoveStopNode;
import electrosphere.server.ai.nodes.checks.IsMovingNode;
import electrosphere.server.ai.nodes.checks.spatial.TargetRangeCheckNode;
import electrosphere.server.ai.nodes.meta.DataStorageNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.RunnerNode;
import electrosphere.server.ai.nodes.meta.decorators.TimerNode;
import electrosphere.server.ai.nodes.meta.decorators.UntilNode;
import electrosphere.server.ai.trees.creature.MoveToTree;
import electrosphere.server.ai.trees.creature.inventory.EquipToolbarTree;

/**
 * A behavior tree to fell a tree entity
 */
public class FellTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "Fell";

    /**
     * Distance to start attacking at
     */
    static final float FELL_RANGE = 0.7f;

    /**
     * Creates a fell tree
     * @param targetKey The key to lookup the target entity under
     * @return The root node of the tree
     */
    public static AITreeNode create(String targetKey){

        return new SequenceNode(
            "FellTree",
            //preconditions here
            new DataStorageNode(BlackboardKeys.INVENTORY_CHECK_TYPE, ItemIdStrings.ITEM_STONE_AXE),
            EquipToolbarTree.create(BlackboardKeys.INVENTORY_CHECK_TYPE),

            //perform different actions based on distance to target
            new SelectorNode(

                //in attack range
                new SequenceNode(
                    "FellTree",
                    //check if in range of target
                    new TargetRangeCheckNode(FellTree.FELL_RANGE, targetKey),
                    //stop walking now that we're in range
                    new PublishStatusNode("Slowing down"),
                    new MoveStopNode(),
                    new UntilNode(AITreeNodeResult.FAILURE, new IsMovingNode()),

                    //attack
                    new SequenceNode(
                        "FellTree",
                        new PublishStatusNode("Attacking"),
                        new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                        new AttackStartNode(),
                        new TimerNode(new RunnerNode(null), 300)
                    )
                ),

                //move to target
                MoveToTree.create(FellTree.FELL_RANGE, targetKey),

                //movement succeeded, but failed to attack -- tree is currently running
                new RunnerNode(null)
            )
        );
    }

}
