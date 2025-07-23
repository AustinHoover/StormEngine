package electrosphere.server.ai.nodes.plan;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.AI;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.interfaces.PathfindingManager;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.pathfinding.recast.PathingProgressiveData;

/**
 * A node that performs pathfinding
 */
public class PathfindingNode implements AITreeNode {


    /**
     * The value used to check if the entity is close to a pathing point horizontally
     */
    public static final double CLOSENESS_CHECK_BOUND_HORIZONTAL = 0.3f;

    /**
     * The value used to check if the entity is close to a pathing point vertically
     */
    public static final double CLOSENESS_CHECK_BOUND_VERTICAL = 0.7f;

    /**
     * The blackboard key to lookup the target entity under
     */
    String targetEntityKey;

    /**
     * 
     * @param targetEntityKey
     */
    public static PathfindingNode createPathEntity(String targetEntityKey){
        PathfindingNode rVal = new PathfindingNode();
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
            if(targetRaw instanceof Vector3d){
                targetPos = (Vector3d)targetRaw;
            } else if(targetRaw instanceof Entity){
                targetPos = EntityUtils.getPosition((Entity)targetRaw);
            } else if(targetRaw instanceof VirtualStructure){
                targetPos = ((VirtualStructure)targetRaw).getPos();
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
            Object targetRaw = blackboard.get(this.targetEntityKey);
            Vector3d targetPos = null;
            if(targetRaw == null){
                throw new Error("Target undefined!");
            }
            if(targetRaw instanceof Vector3d){
                targetPos = (Vector3d)targetRaw;
            } else if(targetRaw instanceof Entity){
                targetPos = EntityUtils.getPosition((Entity)targetRaw);
            } else if(targetRaw instanceof VirtualStructure){
                targetPos = ((VirtualStructure)targetRaw).getPos();
            } else {
                throw new Error("Unsupported target type " + targetRaw);
            }

            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            PathfindingManager pathfindingManager = realm.getPathfindingManager();

            Vector3d entityPos = EntityUtils.getPosition(entity);

            PathingProgressiveData pathingProgressiveData = pathfindingManager.findPathAsync(entityPos, targetPos);
            PathfindingNode.setPathfindingData(blackboard, pathingProgressiveData);
        }

        if(!PathfindingNode.hasPathfindingData(blackboard)){
            throw new Error("Failed to find path! Unhandled");
        }

        //check if the path has been found
        PathingProgressiveData pathingProgressiveData = PathfindingNode.getPathfindingData(blackboard);
        if(!pathingProgressiveData.isReady()){
            AI.getAI(entity).setStatus("Thinking about pathing");
            return AITreeNodeResult.RUNNING;
        }


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
                targetPos = EntityUtils.getPosition((Entity)targetRaw);
            } else if(targetRaw instanceof VirtualStructure){
                targetPos = ((VirtualStructure)targetRaw).getPos();
            } else {
                throw new Error("Unsupported target type " + targetRaw);
            }
            currentPathPos = targetPos;
        }

        PathfindingNode.setPathfindingPoint(blackboard, currentPathPos);

        return AITreeNodeResult.SUCCESS;
    }

    /**
     * Sets the pathfinding data in the blackboard
     * @param blackboard The blackboard
     * @param pathfindingData The pathfinding data
     */
    public static void setPathfindingData(Blackboard blackboard, PathingProgressiveData pathfindingData){
        blackboard.put(BlackboardKeys.PATHFINDING_DATA, pathfindingData);
    }

    /**
     * Gets the current pathfinding data
     * @param blackboard The blackboard
     * @return The pathfinding data if it exists, null otherwise
     */
    public static PathingProgressiveData getPathfindingData(Blackboard blackboard){
        return (PathingProgressiveData)blackboard.get(BlackboardKeys.PATHFINDING_DATA);
    }

    /**
     * Checks if the blackboard has pathfinding data
     * @param blackboard The blackboard
     * @return true if it has pathfinding data, false otherwise
     */
    public static boolean hasPathfindingData(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.PATHFINDING_DATA);
    }

    /**
     * Clears the pathfinding data
     * @param blackboard The pathfinding data
     */
    public static void clearPathfindingData(Blackboard blackboard){
        blackboard.delete(BlackboardKeys.PATHFINDING_DATA);
    }

    /**
     * Sets the pathfinding point in the blackboard
     * @param blackboard The blackboard
     * @param pathfindingPoint The pathfinding point
     */
    public static void setPathfindingPoint(Blackboard blackboard, Vector3d pathfindingPoint){
        blackboard.put(BlackboardKeys.PATHFINDING_POINT, pathfindingPoint);
    }

    /**
     * Gets the current pathfinding point
     * @param blackboard The blackboard
     * @return The pathfinding point if it exists, null otherwise
     */
    public static Vector3d getPathfindingPoint(Blackboard blackboard){
        return (Vector3d)blackboard.get(BlackboardKeys.PATHFINDING_POINT);
    }

    /**
     * Checks if the blackboard has pathfinding point
     * @param blackboard The blackboard
     * @return true if it has pathfinding point, false otherwise
     */
    public static boolean hasPathfindingPoint(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.PATHFINDING_POINT);
    }

    /**
     * Clears the pathfinding point
     * @param blackboard The pathfinding point
     */
    public static void clearPathfindingPoint(Blackboard blackboard){
        blackboard.delete(BlackboardKeys.PATHFINDING_POINT);
    }
    
}
