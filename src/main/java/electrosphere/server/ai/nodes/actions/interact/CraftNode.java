package electrosphere.server.ai.nodes.actions.interact;

import electrosphere.data.crafting.RecipeData;
import electrosphere.data.entity.item.source.ItemSourcingData;
import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.plan.SolveSourcingTreeNode;
import electrosphere.server.player.CraftingActions;

/**
 * Tries to craft an item
 */
public class CraftNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        ItemSourcingData sourcingData = SolveSourcingTreeNode.getItemSourcingData(blackboard);
        for(RecipeData recipe : sourcingData.getRecipes()){
            if(CraftingActions.canCraft(entity, null, recipe)){
                CraftingActions.attemptCraft(entity, null, recipe);
                return AITreeNodeResult.SUCCESS;
            }
        }
        return AITreeNodeResult.FAILURE;
    }
    
}
