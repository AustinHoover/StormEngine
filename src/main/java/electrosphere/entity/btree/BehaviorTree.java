package electrosphere.entity.btree;

/**
 * A behavior tree
 */
public interface BehaviorTree {

    /**
     * Simulates the behavior tree
     * @param deltaTime The time since the last call to the simulate function
     */
    public void simulate(float deltaTime);
    
}
