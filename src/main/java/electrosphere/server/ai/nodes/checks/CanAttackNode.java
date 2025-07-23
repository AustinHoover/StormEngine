package electrosphere.server.ai.nodes.checks;

import electrosphere.entity.Entity;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Checks if the entity can attack
 */
public class CanAttackNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!ServerAttackTree.hasAttackTree(entity)){
            return AITreeNodeResult.FAILURE;
        }
        ServerAttackTree attackTree = ServerAttackTree.getServerAttackTree(entity);
        if(attackTree.canAttack()){
            return AITreeNodeResult.SUCCESS;
        } else {
            return AITreeNodeResult.FAILURE;
        }
    }
}
