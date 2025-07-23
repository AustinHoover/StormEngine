package electrosphere.server.ai.trees.creature;

import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementRelativeFacing;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.actions.move.CollidableElevationLerpNode;
import electrosphere.server.ai.nodes.actions.move.FaceTargetNode;
import electrosphere.server.ai.nodes.actions.move.MoveStartNode;
import electrosphere.server.ai.nodes.actions.move.MoveStopNode;
import electrosphere.server.ai.nodes.checks.spatial.TargetRangeCheckNode;
import electrosphere.server.ai.nodes.meta.DataDeleteNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.decorators.RunnerNode;
import electrosphere.server.ai.nodes.meta.decorators.SucceederNode;
import electrosphere.server.ai.nodes.plan.MacroPathfindingNode;
import electrosphere.server.ai.nodes.plan.PathfindingNode;

/**
 * Tree that moves an entity to a macro structure
 */
public class MacroMoveToTree {

    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "MacroMoveTo";

    /**
     * Default distance to be within
     */
    static final double DEFAULT_DIST = 0.5f;

    /**
     * Creates a move-to-target tree
     * @param targetKey The key to lookup the target under
     * @return The root node of the move-to-target tree
     */
    public static AITreeNode create(String targetKey){
        return MacroMoveToTree.create(DEFAULT_DIST, targetKey);
    }

    /**
     * Creates a move-to-target tree
     * @param dist The target distance to be within
     * @param targetKey The key to lookup the target under
     * @return The root node of the move-to-target tree
     */
    public static AITreeNode create(double dist, String targetKey){
        if(dist < PathfindingNode.CLOSENESS_CHECK_BOUND_HORIZONTAL){
            throw new Error("Dist less than minimal amount! " + dist);
        }
        return new SelectorNode(
            new SequenceNode(
                "MacroMoveToTree",
                //check if in range of target
                new TargetRangeCheckNode(dist, targetKey),
                new DataDeleteNode(BlackboardKeys.PATHFINDING_POINT),
                new DataDeleteNode(BlackboardKeys.PATHFINDING_DATA),
                //if in range, stop moving fowards and return SUCCESS
                new SucceederNode(new MoveStopNode())
            ),

            //not in range of target, keep moving towards it
            new SequenceNode(
                "MacroMoveToTree",
                MacroPathfindingNode.createPathEntity(targetKey),
                new FaceTargetNode(BlackboardKeys.PATHFINDING_POINT),
                new CollidableElevationLerpNode(BlackboardKeys.PATHFINDING_POINT),
                new RunnerNode(new MoveStartNode(MovementRelativeFacing.FORWARD))
            )
        );
    }
}
