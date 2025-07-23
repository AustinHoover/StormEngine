package electrosphere.data.entity.creature.movement;

import electrosphere.data.entity.common.treedata.TreeDataState;

/**
 * Data about a falling movement system
 */
public class FallMovementSystem implements MovementSystem {

    /**
     * The name of this movement system type in particular
     */
    public static final String FALL_MOVEMENT_SYSTEM = "FALL";

    //The type of movement syste,
    String type;
    
    //Falling data
    TreeDataState fallState;

    //Landing data
    TreeDataState landState;

    /**
     * Gets the fall state data
     * @return The fall state data
     */
    public TreeDataState getFallState(){
        return fallState;
    }

    /**
     * Gets the land state data
     * @return The land state data
     */
    public TreeDataState getLandState(){
        return landState;
    }

    @Override
    public String getType() {
        return type;
    }

}
