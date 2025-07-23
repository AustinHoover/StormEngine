package electrosphere.server.ai.nodes.checks.equip;

import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ServerEquipState;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Checks if the entity has a weapon
 */
public class HasWeaponNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!ServerEquipState.hasEquipState(entity)){
            return AITreeNodeResult.FAILURE;
        }
        ServerEquipState serverEquipState = ServerEquipState.getEquipState(entity);
        for(String equippedPointId : serverEquipState.equippedPoints()){
            Entity item = serverEquipState.getEquippedItemAtPoint(equippedPointId);
            if(ItemUtils.isWeapon(item)){
                return AITreeNodeResult.SUCCESS;
            }
        }
        return AITreeNodeResult.FAILURE;
    }
    
}
