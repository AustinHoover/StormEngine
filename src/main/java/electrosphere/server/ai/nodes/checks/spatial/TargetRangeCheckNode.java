package electrosphere.server.ai.nodes.checks.spatial;

import org.joml.Vector3d;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.macro.spatial.MacroObject;

/**
 * Checks if the target is inside a given range of the entity
 */
public class TargetRangeCheckNode implements AITreeNode {

    /**
     * The distance to succeed within
     */
    double dist;

    /**
     * The key to lookup the target under
     */
    String targetKey;

    /**
     * Constructor
     * @param dist The distance outside of which the node will fail
     * @param targetKey The key to lookup the target under
     */
    public TargetRangeCheckNode(double dist, String targetKey){
        this.dist = dist;
        this.targetKey = targetKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        Object targetRaw = blackboard.get(this.targetKey);
        Vector3d targetPos = null;
        if(targetRaw == null){
            return AITreeNodeResult.FAILURE;
        }
        if(targetRaw instanceof Vector3d){
            targetPos = (Vector3d)targetRaw;
        } else if(targetRaw instanceof Entity){
            targetPos = EntityUtils.getPosition((Entity)targetRaw);
        } else if(targetRaw instanceof MacroObject macroObject){
            targetPos = macroObject.getPos();
        } else {
            throw new Error("Unsupported target type " + targetRaw);
        }
        Vector3d entPos = EntityUtils.getPosition(entity);
        
        if(targetPos.distance(entPos) < this.dist){
            return AITreeNodeResult.SUCCESS;
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }
}
