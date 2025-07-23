package electrosphere.server.ai.nodes.actions.combat;

import electrosphere.entity.Entity;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Starts an attack
 */
public class AttackStartNode implements AITreeNode {

    /**
     * Constructor
     */
    public AttackStartNode(){
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!ServerAttackTree.hasAttackTree(entity)){
            return AITreeNodeResult.FAILURE;
        }
        ServerAttackTree serverAttackTree = ServerAttackTree.getServerAttackTree(entity);
        if(!serverAttackTree.canAttack()){
            return AITreeNodeResult.FAILURE;
        }
        if(serverAttackTree.isAttacking()){
            return AITreeNodeResult.RUNNING;
        }
        serverAttackTree.start();
        return AITreeNodeResult.SUCCESS;
    }

}