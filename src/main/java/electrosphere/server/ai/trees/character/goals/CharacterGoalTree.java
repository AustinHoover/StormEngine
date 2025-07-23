package electrosphere.server.ai.trees.character.goals;

import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementRelativeFacing;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.actions.move.FaceTargetNode;
import electrosphere.server.ai.nodes.actions.move.MoveStartNode;
import electrosphere.server.ai.nodes.checks.spatial.BeginStructureNode;
import electrosphere.server.ai.nodes.macro.MacroCharacterGoalNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.RunnerNode;
import electrosphere.server.ai.trees.creature.MacroMoveToTree;
import electrosphere.server.ai.trees.creature.resource.AcquireItemTree;
import electrosphere.server.ai.trees.struct.BuildStructureTree;
import electrosphere.server.macro.character.goal.CharacterGoal.CharacterGoalType;

/**
 * Performs macro-character-defined goals
 */
public class CharacterGoalTree {

    /**
     * Creates a character goal tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "CharacterGoalTree",
            //check if we have goals
            MacroCharacterGoalNode.createAny(),
            //select based on the type of goals the character has
            new SelectorNode(

                //character's goal is to leave sim range
                new SequenceNode(
                    "CharacterGoalTree",
                    MacroCharacterGoalNode.create(CharacterGoalType.LEAVE_SIM_RANGE),
                    new PublishStatusNode("Leaving simulation range"),
                    new FaceTargetNode(BlackboardKeys.POINT_TARGET),
                    new RunnerNode(new MoveStartNode(MovementRelativeFacing.FORWARD))
                ),

                //character's goal is to build a structure
                new SequenceNode(
                    "CharacterGoalTree",
                    MacroCharacterGoalNode.create(CharacterGoalType.BUILD_STRUCTURE),
                    new PublishStatusNode("Construct a shelter"),
                    new BeginStructureNode(),
                    BuildStructureTree.create()
                ),

                //character's goal is to acquire an item
                new SequenceNode(
                    "CharacterGoalTree",
                    MacroCharacterGoalNode.create(CharacterGoalType.ACQUIRE_ITEM),
                    new PublishStatusNode("Acquire building material"),
                    //try to find building materials
                    AcquireItemTree.create(BlackboardKeys.GOAL_ITEM_ACQUISITION_TARGET)
                ),

                //character's goal is to move to a macro virtual structure
                new SequenceNode(
                    "CharacterGoalTree",
                    MacroCharacterGoalNode.create(CharacterGoalType.MOVE_TO_MACRO_STRUCT),
                    new PublishStatusNode("Move to macro structure"),
                    MacroMoveToTree.create(BlackboardKeys.MACRO_TARGET)
                )
            )
        );
    }

}
