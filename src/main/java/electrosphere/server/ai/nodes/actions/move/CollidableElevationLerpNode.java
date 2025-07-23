package electrosphere.server.ai.nodes.actions.move;

import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.util.math.BasicMathUtils;

/**
 * Lerps the elevation of non-body collidables
 */
public class CollidableElevationLerpNode implements AITreeNode {

    /**
     * The key to lookup the target under
     */
    private String targetKey;

    /**
     * Constructor
     * @param targetKey The key to lookup the target under
     */
    public CollidableElevationLerpNode(String targetKey){
        this.targetKey = targetKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(PhysicsEntityUtils.containsDGeom(entity)){
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
            Vector3d currentPos = EntityUtils.getPosition(entity);
            double dist = currentPos.distance(targetPos);
            if(ServerGroundMovementTree.hasServerGroundMovementTree(entity)){
                ServerGroundMovementTree tree = ServerGroundMovementTree.getServerGroundMovementTree(entity);
                tree.setCollidableElevationTarget(BasicMathUtils.lerp(currentPos.y,targetPos.y,1.0 / Math.max(1.0,dist)));
            }
        }
        return AITreeNodeResult.SUCCESS;
    }
}
