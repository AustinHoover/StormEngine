package electrosphere.data.entity.creature.ai;

import electrosphere.server.ai.trees.creature.AttackerAITree;

/**
 * Configuration data for an attacker tree
 */
public class AttackerTreeData implements AITreeData {
    
    /**
     * The range at which this entity will locate enemies
     */
    float aggroRange;

    /**
     * The range at which attack animations will be attempted
     */
    float attackRange;

    /**
     * Gets the range at which this entity will locate enemies
     * @return The range
     */
    public float getAggroRange(){
        return aggroRange;
    }

    /**
     * Gets the range at which attack animations will be attempted
     * @return The range
     */
    public float getAttackRange(){
        return attackRange;
    }

    @Override
    public String getName() {
        return AttackerAITree.TREE_NAME;
    }

}
