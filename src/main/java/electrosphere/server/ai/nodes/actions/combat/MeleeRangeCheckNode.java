package electrosphere.server.ai.nodes.actions.combat;

import org.joml.Vector3d;

import electrosphere.data.entity.creature.ai.AttackerTreeData;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Checks if the target is in a range type
 */
public class MeleeRangeCheckNode implements AITreeNode {
    
    /**
     * The type of check to run
     */
    public static enum MeleeRangeCheckType {
        /**
         * Check if in aggro range
         */
        AGGRO,
        /**
         * Check if in attack range
         */
        ATTACK,
    }

    /**
     * The type of check to perform
     */
    MeleeRangeCheckType checkType = null;

    /**
     * The attacker tree data
     */
    AttackerTreeData attackerTreeData = null;

    /**
     * Constructor
     * @param attackerTreeData The attacker tree data
     * @param checkType The type of check to perform
     */
    public MeleeRangeCheckNode(AttackerTreeData attackerTreeData, MeleeRangeCheckType checkType){
        if(attackerTreeData == null){
            throw new IllegalArgumentException("Trying to create melee range check node with null tree data!");
        }
        if(checkType == null){
            throw new IllegalArgumentException("Trying to create melee range check node with null check type!");
        }
        this.attackerTreeData = attackerTreeData;
        this.checkType = checkType;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!MeleeTargetingNode.hasTarget(blackboard)){
            return AITreeNodeResult.FAILURE;
        }
        Entity target = MeleeTargetingNode.getTarget(blackboard);
        Vector3d targetPos = EntityUtils.getPosition(target);
        Vector3d parentPos = EntityUtils.getPosition(entity);
        switch(this.checkType){
            case AGGRO: {
                if(parentPos.distance(targetPos) < this.attackerTreeData.getAggroRange()){
                    return AITreeNodeResult.SUCCESS;
                }
            } break;
            case ATTACK: {
                if(parentPos.distance(targetPos) < this.attackerTreeData.getAttackRange()){
                    return AITreeNodeResult.SUCCESS;
                }
            } break;
        }
        return AITreeNodeResult.FAILURE;
    }


}
