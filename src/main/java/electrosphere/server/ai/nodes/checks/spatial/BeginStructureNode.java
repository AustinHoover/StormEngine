package electrosphere.server.ai.nodes.checks.spatial;

import org.joml.Vector3d;

import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.macro.struct.StructureData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.macro.utils.StructurePlacementUtils;
import electrosphere.util.FileUtils;

/**
 * Tries to begin building a structure
 */
public class BeginStructureNode implements AITreeNode {

    /**
     * Constructor
     */
    public BeginStructureNode(){
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!BeginStructureNode.hasStructureTarget(blackboard)){
            //requisite data
            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            MacroData macroData = realm.getMacroData();
            Vector3d position = EntityUtils.getPosition(entity);

            //get the structures this race can build
            ServerCharacterData charData = ServerCharacterData.getServerCharacterData(entity);
            Race race = Race.getRace(charData.getCharacterData());
            if(race.getStructureIds().size() < 1){
                throw new Error("Race has no associated structures! " + race.getId());
            }
            StructureData structureData = Globals.gameConfigCurrent.getStructureData().getType(race.getStructureIds().get(0));

            //solve where to place
            Vector3d placementPos = StructurePlacementUtils.getPlacementPosition(macroData, structureData, position);

            //add to macro data
            VirtualStructure struct = VirtualStructure.createStructure(macroData, structureData, placementPos, VirtualStructure.ROT_FACE_NORTH);
            struct.setRepairable(true);
            struct.setFab(BlockFab.read(FileUtils.getAssetFile(struct.getFabPath())));
            // macroData.getStructures().add(struct);

            BeginStructureNode.setStructureTarget(blackboard, struct);
        }
        return AITreeNodeResult.SUCCESS;
    }

    /**
     * Sets the structure target for the entity
     * @param blackboard The blackboard
     * @param structure The structure to target
     */
    public static void setStructureTarget(Blackboard blackboard, VirtualStructure structure){
        blackboard.put(BlackboardKeys.STRUCTURE_TARGET, structure);
    }

    /**
     * Checks if the blackboard has a structure target
     * @param blackboard The blackboard
     */
    public static boolean hasStructureTarget(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.STRUCTURE_TARGET);
    }

    /**
     * Gets the structure target in the blackboard
     * @param blackboard The blackboard
     * @return The structure if it exists, null otherwise
     */
    public static VirtualStructure getStructureTarget(Blackboard blackboard){
        return (VirtualStructure)blackboard.get(BlackboardKeys.STRUCTURE_TARGET);
    }

    /**
     * Sets the position of the voxel to target for the entity
     * @param blackboard The blackboard
     * @param structure The position of the voxel to target
     */
    public static void setStructureBuildTarget(Blackboard blackboard, Vector3d position){
        blackboard.put(BlackboardKeys.STRUCTURE_BUILD_TARGET, position);
    }

    /**
     * Checks if the blackboard has a voxel position target
     * @param blackboard The blackboard
     */
    public static boolean hasStructureBuildTarget(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.STRUCTURE_BUILD_TARGET);
    }

    /**
     * Gets the position of the voxel to target in the blackboard
     * @param blackboard The blackboard
     * @return The position of the voxel to target if it exists, null otherwise
     */
    public static Vector3d getStructureBuildTarget(Blackboard blackboard){
        return (Vector3d)blackboard.get(BlackboardKeys.STRUCTURE_BUILD_TARGET);
    }

    /**
     * Sets the block type to build with
     * @param blackboard The blackboard
     * @param structure The block type
     */
    public static void setBuildBlock(Blackboard blackboard, short position){
        blackboard.put(BlackboardKeys.BUILD_BLOCK, position);
    }

    /**
     * Checks if the blackboard has a block type to build with
     * @param blackboard The blackboard
     */
    public static boolean hasBuildBlock(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.BUILD_BLOCK);
    }

    /**
     * Gets block type to build with
     * @param blackboard The blackboard
     * @return The block type to build with
     */
    public static short getBuildBlock(Blackboard blackboard){
        return (short)blackboard.get(BlackboardKeys.BUILD_BLOCK);
    }

}
