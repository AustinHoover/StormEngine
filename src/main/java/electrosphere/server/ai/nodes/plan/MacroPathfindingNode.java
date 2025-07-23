package electrosphere.server.ai.nodes.plan;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.AI;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.server.macro.spatial.path.MacroPathNode;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.pathfinding.recast.PathingProgressiveData;

/**
 * A node that uses macro pathfinding structures to accelerate calculating the pathfinding 
 */
public class MacroPathfindingNode implements AITreeNode {


    /**
     * The value used to check if the entity is close to a pathing point horizontally
     */
    public static final double CLOSENESS_CHECK_BOUND_HORIZONTAL = 0.3;

    /**
     * The value used to check if the entity is close to a pathing point vertically
     */
    public static final double CLOSENESS_CHECK_BOUND_VERTICAL = 0.7;

    /**
     * The blackboard key to lookup the target entity under
     */
    private String targetEntityKey;

    /**
     * 
     * @param targetEntityKey
     */
    public static MacroPathfindingNode createPathEntity(String targetEntityKey){
        MacroPathfindingNode rVal = new MacroPathfindingNode();
        rVal.targetEntityKey = targetEntityKey;
        return rVal;
    }


    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard){
        //make sure that the solved pathfinding data is for the point we want
        if(PathfindingNode.hasPathfindingData(blackboard)){
            PathingProgressiveData pathingProgressiveData = PathfindingNode.getPathfindingData(blackboard);
            Vector3d actualPoint = pathingProgressiveData.getGoal();
            Object targetRaw = blackboard.get(this.targetEntityKey);
            Vector3d targetPos = null;
            if(targetRaw == null){
                throw new Error("Target undefined!");
            }
            if(targetRaw instanceof MacroRegion macroRegion){
                targetPos = macroRegion.getPos();
            } else {
                throw new Error("Unsupported target type " + targetRaw);
            }
            if(actualPoint.distance(targetPos) > CLOSENESS_CHECK_BOUND_HORIZONTAL){
                PathfindingNode.clearPathfindingData(blackboard);
                PathfindingNode.clearPathfindingPoint(blackboard);
            }
        }

        //create a path if we don't already have one
        if(!PathfindingNode.hasPathfindingData(blackboard)){

            //
            //figure out what we're targeting
            Object targetRaw = blackboard.get(this.targetEntityKey);
            if(targetRaw == null){
                throw new Error("Target undefined!");
            }
            if(targetRaw instanceof MacroAreaObject){
            } else {
                throw new Error("Unsupported target type " + targetRaw);
            }
            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            Vector3d targetPos = ((MacroAreaObject)targetRaw).getPos();
            MacroPathNode targetMacro = realm.getMacroData().getPathCache().getPathingNode(targetPos);


            //
            //Find where the entity is currently
            MacroPathNode currentPos = this.solveCurrentNode(entity);

            //
            //Queue the pathfinding operation
            PathingProgressiveData pathingProgressiveData = Globals.serverState.macroPathingService.queuePathfinding(realm, currentPos, targetMacro);
            pathingProgressiveData.setGoal(targetPos);
            PathfindingNode.setPathfindingData(blackboard, pathingProgressiveData);
        }

        //make sure we found a path
        if(!PathfindingNode.hasPathfindingData(blackboard)){
            throw new Error("Failed to find path! Unhandled");
        }

        //check if the path has been found
        PathingProgressiveData pathingProgressiveData = PathfindingNode.getPathfindingData(blackboard);
        if(!pathingProgressiveData.isReady()){
            AI.getAI(entity).setStatus("Thinking about pathing");
            return AITreeNodeResult.RUNNING;
        }


        //
        //walk along the path if it exists
        Vector3d entityPos = EntityUtils.getPosition(entity);
        Vector3d currentPathPos = null;
        if(pathingProgressiveData.getCurrentPoint() < pathingProgressiveData.getPoints().size()){
            currentPathPos = pathingProgressiveData.getPoints().get(pathingProgressiveData.getCurrentPoint());
        }
        double vertDist = Math.abs(currentPathPos.y - entityPos.y);
        double horizontalDist = Math.sqrt((currentPathPos.x - entityPos.x) * (currentPathPos.x - entityPos.x) + (currentPathPos.z - entityPos.z) * (currentPathPos.z - entityPos.z));
        while(
            currentPathPos != null &&
            vertDist < CLOSENESS_CHECK_BOUND_VERTICAL &&
            horizontalDist < CLOSENESS_CHECK_BOUND_HORIZONTAL &&
            pathingProgressiveData.getCurrentPoint() < pathingProgressiveData.getPoints().size() - 1
        ){
            pathingProgressiveData.setCurrentPoint(pathingProgressiveData.getCurrentPoint() + 1);
            currentPathPos = pathingProgressiveData.getPoints().get(pathingProgressiveData.getCurrentPoint());
            vertDist = Math.abs(currentPathPos.y - entityPos.y);
            horizontalDist = Math.sqrt((currentPathPos.x - entityPos.x) * (currentPathPos.x - entityPos.x) + (currentPathPos.z - entityPos.z) * (currentPathPos.z - entityPos.z));
        }

        //if we're close enough to the final pathing point, always path to actual final point
        if(
            vertDist < CLOSENESS_CHECK_BOUND_VERTICAL &&
            horizontalDist < CLOSENESS_CHECK_BOUND_HORIZONTAL &&
            pathingProgressiveData.getCurrentPoint() == pathingProgressiveData.getPoints().size() - 1
        ){
            Object targetRaw = blackboard.get(this.targetEntityKey);
            Vector3d targetPos = null;
            if(targetRaw == null){
                throw new Error("Target undefined!");
            }
            if(targetRaw instanceof Vector3d){
                targetPos = (Vector3d)targetRaw;
            } else if(targetRaw instanceof Entity){
                targetPos = new Vector3d(EntityUtils.getPosition((Entity)targetRaw));
            } else if(targetRaw instanceof VirtualStructure struct){
                targetPos = struct.getPos();
            } else if(targetRaw instanceof MacroRegion region){
                targetPos = region.getPos();
            } else {
                throw new Error("Unsupported target type " + targetRaw);
            }
            currentPathPos = targetPos;
        }

        PathfindingNode.setPathfindingPoint(blackboard, currentPathPos);

        return AITreeNodeResult.SUCCESS;
    }

    /**
     * Gets the pathing node of this entity
     * @param entity The entity
     * @return The pathing node
     */
    private MacroPathNode solveCurrentNode(Entity entity){
        Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
        if(realm == null){
            throw new Error("Entity is not attached to a realm!");
        }
        MacroData macroData = realm.getMacroData();
        if(macroData == null){
            throw new Error("Macro data undefined!");
        }
        Vector3d entityPos = new Vector3d(EntityUtils.getPosition(entity));
        return macroData.getPathCache().getPathingNode(entityPos);
    }

}
