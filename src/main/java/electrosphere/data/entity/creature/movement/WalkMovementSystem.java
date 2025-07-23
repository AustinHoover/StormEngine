package electrosphere.data.entity.creature.movement;

/**
 * A walk movement system
 */
public class WalkMovementSystem implements MovementSystem {

    //move system type string
    public static final String WALK_MOVEMENT_SYSTEM = "WALK";

    @Override
    public String getType() {
        return WALK_MOVEMENT_SYSTEM;
    }

    /**
     * The movespeed modifier applied when walking
     */
    float modifier;

    /**
     * The movespeed modifier applied when walking
     * @return The modifier
     */
    public float getModifier(){
        return modifier;
    }
    
}
