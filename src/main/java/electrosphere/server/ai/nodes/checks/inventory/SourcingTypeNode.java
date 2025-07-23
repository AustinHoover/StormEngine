package electrosphere.server.ai.nodes.checks.inventory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import electrosphere.data.entity.item.source.ItemSourcingData;
import electrosphere.data.entity.item.source.ItemSourcingData.SourcingType;
import electrosphere.entity.Entity;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.plan.SolveSourcingTreeNode;
import electrosphere.server.ai.services.NearbyEntityService;

/**
 * Checks if the supplied type of sourcing is the path for the current target item to acquire
 */
public class SourcingTypeNode implements AITreeNode {
    
    /**
     * The type of sourcing
     */
    SourcingType sourcingType;

    /**
     * The blackboard key storing the target entity type id
     */
    String targetTypeKey;

    /**
     * Constructor
     * @param targetTypeKey The blackboard key storing the target entity type id
     * @param sourcingType The type of sourcing to check
     */
    public SourcingTypeNode(SourcingType sourcingType, String targetTypeKey){
        this.sourcingType = sourcingType;
        this.targetTypeKey = targetTypeKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(!SolveSourcingTreeNode.hasItemSourcingData(blackboard)){
            return AITreeNodeResult.FAILURE;
        }

        ItemSourcingData sourcingData = SolveSourcingTreeNode.getItemSourcingData(blackboard);
        if(sourcingData == null){
            throw new Error("Sourcing data is null!");
        }

        //succeed based on the type of sourcing that this node is set for
        switch(this.sourcingType){
            case PICKUP: {
                //see if the entity is in the vicinity
                String targetEntityType = (String)blackboard.get(this.targetTypeKey);
                Collection<Entity> nearbyEntities = NearbyEntityService.getNearbyEntities(blackboard);
                List<String> types = nearbyEntities.stream().map((Entity ent) -> {return CommonEntityUtils.getEntitySubtype(ent);}).collect(Collectors.toList());
                if(types.contains(targetEntityType)){
                    return AITreeNodeResult.SUCCESS;
                }
            } break;
            case RECIPE: {
                if(sourcingData.getRecipes().size() > 0){
                    return AITreeNodeResult.SUCCESS;
                }
            } break;
            case HARVEST: {
                if(sourcingData.getHarvestTargets().size() > 0){
                    return AITreeNodeResult.SUCCESS;
                }
            } break;
            case TREE: {
                if(sourcingData.getTrees().size() > 0){
                    return AITreeNodeResult.SUCCESS;
                }
            } break;
        }


        return AITreeNodeResult.FAILURE;
    }

}
