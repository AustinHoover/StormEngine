package electrosphere.server.ai.nodes.actions.move;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.util.math.SpatialMathUtils;

/**
 * Faces the target
 */
public class FaceTargetNode implements AITreeNode {

    /**
     * The key to lookup the target under
     */
    String targetKey;

    /**
     * Constructor
     * @param targetKey The key to lookup the target under
     */
    public FaceTargetNode(String targetKey){
        this.targetKey = targetKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        Object targetRaw = blackboard.get(this.targetKey);
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
        Vector3d parentPos = EntityUtils.getPosition(entity);
        Quaterniond rotation = SpatialMathUtils.calculateRotationFromPointToPoint(parentPos, targetPos);
        EntityUtils.getRotation(entity).set(rotation);
        Vector3d faceVec = CameraEntityUtils.getFacingVec(rotation);
        CreatureUtils.setFacingVector(entity, faceVec);
        return AITreeNodeResult.SUCCESS;
    }

}
