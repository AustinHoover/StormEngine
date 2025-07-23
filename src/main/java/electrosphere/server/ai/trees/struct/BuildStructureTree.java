package electrosphere.server.ai.trees.struct;

import electrosphere.collision.CollisionEngine;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.actions.interact.PlaceBlockNode;
import electrosphere.server.ai.nodes.actions.inventory.EquipToolbarNode;
import electrosphere.server.ai.nodes.checks.inventory.InventoryContainsNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.InverterNode;
import electrosphere.server.ai.nodes.meta.decorators.RunnerNode;
import electrosphere.server.ai.nodes.meta.decorators.SucceederNode;
import electrosphere.server.ai.nodes.solvers.SolveBuildMaterialNode;
import electrosphere.server.ai.trees.creature.MoveToTree;

/**
 * A tree to build whatever the current structure target is
 */
public class BuildStructureTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "BuildStructureTree";
    
    /**
     * Creates a construct structure tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "BuildStructureTree",
            new PublishStatusNode("Construct a structure"),
            //figure out current task
            new SelectorNode(
                //make sure we know what material we need to build with currently
                new SequenceNode(
                    "BuildStructureTree",
                    new SolveBuildMaterialNode(),
                    new PublishStatusNode("Trying to place block in structure"),
                    //if has building materials
                    new SequenceNode(
                        "BuildStructureTree",
                        new InventoryContainsNode(BlackboardKeys.BUILDING_MATERIAL_CURRENT),
                        //if we're within range to place the material
                        new SequenceNode(
                            "BuildStructureTree",
                            //not in range, move to within range
                            MoveToTree.create(CollisionEngine.DEFAULT_INTERACT_DISTANCE, BlackboardKeys.STRUCTURE_TARGET),
                            //equip the type of block to place
                            EquipToolbarNode.equipBlock(BlackboardKeys.BUILDING_MATERIAL_CURRENT),
                            //in range, place block
                            new PlaceBlockNode()
                        )
                    )
                ),
                //does not have building materials
                new SequenceNode(
                    "BuildStructureTree",
                    new InverterNode(new InventoryContainsNode(BlackboardKeys.BUILDING_MATERIAL_CURRENT)),
                    new RunnerNode(new PublishStatusNode("Waiting on macro character to set goal to find materials to build with"))
                ),
                //has building materials AND we've already built the structure
                new SucceederNode(null)
            )
        );
    }

    /**
     * Sets the current needed building material
     * @param blackboard The blackboard
     * @param entityTypeId The id of the material
     */
    public static void setCurrentMaterial(Blackboard blackboard, String entityTypeId){
        blackboard.put(BlackboardKeys.BUILDING_MATERIAL_CURRENT, entityTypeId);
    }

    /**
     * Checks if the blackboard stores the currently sought after material
     * @param blackboard The blackboard
     * @return true if there is a currently desired material, false otherwise
     */
    public static boolean hasCurrentMaterial(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.BUILDING_MATERIAL_CURRENT);
    }

    /**
     * Gets the currently sought after material
     * @param blackboard The blackboard
     * @return The id of the entity type of the sought after material if it exists, null otherwise
     */
    public static String getCurrentMaterial(Blackboard blackboard){
        return (String)blackboard.get(BlackboardKeys.BUILDING_MATERIAL_CURRENT);
    }

}
