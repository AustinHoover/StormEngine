package electrosphere.server.ai.nodes.plan;

import java.util.Random;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;

/**
 * Generates a point to explore towards
 */
public class TargetExploreNode implements AITreeNode {

    /**
     * The key to store the point under
     */
    String targetKey;

    /**
     * Distance to travel in whatever direction
     */
    static final double OFFSET_DIST = 50;

    /**
     * Offset applied to calculated height to align with voxel rasterization better
     */
    static final double HEIGHT_OFFSET = 0.1f;

    /**
     * constructor
     * @param targetKey The key to store the point under
     */
    public TargetExploreNode(String targetKey){
        this.targetKey = targetKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard){
        Vector3d targetPos = null;
        
        if(!blackboard.has(targetKey)){
            Vector3d entPos = new Vector3d(EntityUtils.getPosition(entity));
            Random rand = new Random();
            Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
            Vector3d offsetVec = new Vector3d(rand.nextDouble(),0,rand.nextDouble()).normalize().mul(OFFSET_DIST);
            targetPos = entPos.add(offsetVec);
            //solve for height via world data
            Vector3i voxelPos = ServerWorldData.convertRealToVoxelSpace(targetPos);
            Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(targetPos);
            double height = realm.getServerWorldData().getServerTerrainManager().getElevation(chunkPos.x, chunkPos.z, voxelPos.x, voxelPos.z);
            targetPos.y = height + HEIGHT_OFFSET;

            //store
            blackboard.put(targetKey, targetPos);
        }
        return AITreeNodeResult.SUCCESS;
    }

}
