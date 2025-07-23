package electrosphere.server.ai.nodes.macro;

import org.joml.Vector3d;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.checks.spatial.BeginStructureNode;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.goal.CharacterGoal;
import electrosphere.server.macro.character.goal.CharacterGoal.CharacterGoalType;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.macro.structure.VirtualStructure;

/**
 * Node for interacting with macro character goals
 */
public class MacroCharacterGoalNode implements AITreeNode {

    /**
     * The type to check for
     */
    CharacterGoalType type;

    /**
     * Constructor
     * @param destinationKey The key to push data into
     */
    public static MacroCharacterGoalNode create(CharacterGoalType type){
        MacroCharacterGoalNode rVal = new MacroCharacterGoalNode();
        rVal.type = type;
        return rVal;
    }

    /**
     * Checks for any goal being present
     */
    public static MacroCharacterGoalNode createAny(){
        MacroCharacterGoalNode rVal = new MacroCharacterGoalNode();
        return rVal;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!ServerCharacterData.hasServerCharacterDataTree(entity)){
            return AITreeNodeResult.FAILURE;
        }
        ServerCharacterData serverCharacterData = ServerCharacterData.getServerCharacterData(entity);
        Character character = serverCharacterData.getCharacterData();
        CharacterGoal goal = CharacterGoal.getCharacterGoal(character);
        if(goal == null){
            return AITreeNodeResult.FAILURE;
        }
        //this is the conditional for the any-type goal
        if(type == null && goal != null){
            return AITreeNodeResult.SUCCESS;
        }
        //fail fast if the goal does not align with the type this node was initialized with
        if(type != goal.getType()){
            return AITreeNodeResult.FAILURE;
        }
        switch(goal.getType()){
            case LEAVE_SIM_RANGE: {
                // Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
                Vector3d entityPos = EntityUtils.getPosition(entity);
                Vector3d offset = new Vector3d(entityPos).add(1000,0,0);
                // GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)realm.getDataCellManager();
                Vector3d targetPos = offset;//griddedDataCellManager.getMacroEntryPoint(offset).add(10,0,0);
                blackboard.put(BlackboardKeys.POINT_TARGET, targetPos);
            } break;
            case BUILD_STRUCTURE: {
                Object targetRaw = goal.getTarget();
                if(!(targetRaw instanceof VirtualStructure)){
                    return AITreeNodeResult.FAILURE;
                }
                BeginStructureNode.setStructureTarget(blackboard, (VirtualStructure)goal.getTarget());
            } break;
            case ACQUIRE_ITEM: {
                Object targetRaw = goal.getTarget();
                if(!(targetRaw instanceof String)){
                    return AITreeNodeResult.FAILURE;
                }
                blackboard.put(BlackboardKeys.GOAL_ITEM_ACQUISITION_TARGET, targetRaw);
            } break;
            case MOVE_TO_MACRO_STRUCT: {
                Object targetRaw = goal.getTarget();
                if(!(targetRaw instanceof VirtualStructure) && !(targetRaw instanceof MacroRegion)){
                    throw new Error("Unsupported type! " + targetRaw);
                }
                MacroCharacterGoalNode.setMacroTarget(blackboard, (MacroObject)goal.getTarget());
            } break;
        }
        if(type == goal.getType()){
            return AITreeNodeResult.SUCCESS;
        }
        return AITreeNodeResult.FAILURE;
    }

    /**
     * Sets the macro object target for the entity
     * @param blackboard The blackboard
     * @param object The macro object to target
     */
    public static void setMacroTarget(Blackboard blackboard, MacroObject object){
        blackboard.put(BlackboardKeys.MACRO_TARGET, object);
    }

    /**
     * Checks if the blackboard has a object target
     * @param blackboard The blackboard
     */
    public static boolean hasMacroTarget(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.MACRO_TARGET);
    }

    /**
     * Gets the object target in the blackboard
     * @param blackboard The blackboard
     * @return The object if it exists, null otherwise
     */
    public static MacroObject getMacroTarget(Blackboard blackboard){
        return (MacroObject)blackboard.get(BlackboardKeys.MACRO_TARGET);
    }

}
