package electrosphere.server.ai.nodes.actions.interact;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.checks.spatial.BeginStructureNode;
import electrosphere.server.ai.nodes.solvers.SolveBuildMaterialNode;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.player.BlockActions;

/**
 * Places a block
 */
public class PlaceBlockNode implements AITreeNode {

    /**
     * Size of the edit to apply
     */
    static final int EDIT_SIZE = 1;

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        Vector3d position = BeginStructureNode.getStructureBuildTarget(blackboard);
        Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(position);
        Vector3i blockPos = ServerWorldData.convertRealToLocalBlockSpace(position);
        
        short blockType = BeginStructureNode.getBuildBlock(blackboard);

        BlockActions.editBlockArea(entity, chunkPos, blockPos, blockType, EDIT_SIZE);

        SolveBuildMaterialNode.clearBuildTarget(blackboard);
        
        return AITreeNodeResult.SUCCESS;
    }
    
}
