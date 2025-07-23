package electrosphere.server.ai.nodes.solvers;

import org.joml.AABBd;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.data.block.BlockType;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.checks.spatial.BeginStructureNode;
import electrosphere.server.ai.trees.struct.BuildStructureTree;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.macro.utils.StructureRepairUtils;

/**
 * Solves for the current build material
 */
public class SolveBuildMaterialNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!BuildStructureTree.hasCurrentMaterial(blackboard)){
            VirtualStructure struct = BeginStructureNode.getStructureTarget(blackboard);
            if(!struct.isRepairable()){
                return AITreeNodeResult.FAILURE;
            }
            //solve for repairable block
            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            Vector3i repairPos = StructureRepairUtils.getRepairablePosition(realm, struct);

            //get the id of item entity type for the block we need
            BlockFab fab = struct.getFab();
            short blockTypeId = fab.getType(repairPos.x, repairPos.y, repairPos.z);
            BlockType blockType = Globals.gameConfigCurrent.getBlockData().getTypeFromId(blockTypeId);
            String itemId = Item.getBlockTypeId(blockType);

            //store the position of the block we want to place
            AABBd structAABB = struct.getAABB();
            Vector3d realPos = new Vector3d(structAABB.minX,structAABB.minY,structAABB.minZ).add(
                repairPos.x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                repairPos.y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                repairPos.z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
            );
            BeginStructureNode.setStructureBuildTarget(blackboard, realPos);

            //set block type
            BeginStructureNode.setBuildBlock(blackboard, blockTypeId);

            //store
            BuildStructureTree.setCurrentMaterial(blackboard, itemId);
        }

        return AITreeNodeResult.SUCCESS;
    }

    /**
     * Clears the build data (ie if we just finished building)
     * @param blackboard The blackboard
     */
    public static void clearBuildTarget(Blackboard blackboard){
        blackboard.delete(BlackboardKeys.BUILD_BLOCK);
        blackboard.delete(BlackboardKeys.BUILDING_MATERIAL_CURRENT);
        blackboard.delete(BlackboardKeys.STRUCTURE_BUILD_TARGET);
    }
    
}
