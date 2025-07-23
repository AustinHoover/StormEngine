package electrosphere.server.ai.nodes.plan;

import java.util.Collection;

import electrosphere.entity.Entity;
import electrosphere.entity.state.life.ServerLifeTree;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.services.NearbyEntityService;

/**
 * Targets a nearby entity of a specific type
 */
public class TargetEntityCategoryNode implements AITreeNode {
    

    /**
     * The blackboard key to pull the entity type from
     */
    String sourceKey;

    /**
     * Constructor
     * @param sourceKey The blackboard key to pull the entity type from
     */
    public TargetEntityCategoryNode(String sourceKey){
        this.sourceKey = sourceKey;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        String goalEntityId = (String)blackboard.get(sourceKey);

        if(goalEntityId == null){
            throw new Error("Entity id to search for is null!");
        }

        if(TargetEntityCategoryNode.hasTarget(blackboard)){
            Entity currentTarget = TargetEntityCategoryNode.getTarget(blackboard);
            if(currentTarget == null){
                TargetEntityCategoryNode.clearTarget(blackboard);
            } else {
                String typeId = CommonEntityUtils.getEntitySubtype(currentTarget);
                if(!typeId.equals(goalEntityId)){
                    TargetEntityCategoryNode.clearTarget(blackboard);
                }
                if(ServerLifeTree.hasServerLifeTree(currentTarget)){
                    ServerLifeTree serverLifeTree = ServerLifeTree.getServerLifeTree(currentTarget);
                    if(!serverLifeTree.isAlive()){
                        TargetEntityCategoryNode.clearTarget(blackboard);
                    }
                }
            }
        }
        if(!TargetEntityCategoryNode.hasTarget(blackboard)){
            Collection<Entity> nearbyEntities = NearbyEntityService.getNearbyEntities(blackboard);
            for(Entity potential : nearbyEntities){
                //get id -- skip empty ids
                String potentialId = CommonEntityUtils.getEntitySubtype(potential);
                if(potentialId == null){
                    continue;
                }
                //set to target if id match
                if(potentialId.equals(goalEntityId)){
                    TargetEntityCategoryNode.setTarget(blackboard, potential);
                    break;
                }
            }
        }
        if(!TargetEntityCategoryNode.hasTarget(blackboard)){
            return AITreeNodeResult.FAILURE;
        }
        return AITreeNodeResult.SUCCESS;
    }

    /**
     * Sets the target in the blackboard
     * @param blackboard The blackboard
     * @param target The target
     */
    public static void setTarget(Blackboard blackboard, Entity target){
        blackboard.put(BlackboardKeys.ENTITY_TARGET, target);
    }

    /**
     * Gets the currently targeted entity
     * @param blackboard The blackboard
     * @return The entity target if it exists, null otherwise
     */
    public static Entity getTarget(Blackboard blackboard){
        return (Entity)blackboard.get(BlackboardKeys.ENTITY_TARGET);
    }

    /**
     * Checks if the blackboard has a currently targeted entity
     * @param blackboard The blackboard
     * @return true if it has a currently targeted entity, false otherwise
     */
    public static boolean hasTarget(Blackboard blackboard){
        return blackboard.has(BlackboardKeys.ENTITY_TARGET);
    }

    /**
     * Clears the target
     * @param blackboard The target
     */
    public static void clearTarget(Blackboard blackboard){
        blackboard.delete(BlackboardKeys.ENTITY_TARGET);
    }

}
