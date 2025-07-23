package electrosphere.entity.state;

import electrosphere.logger.LoggerInterface;

/**
 * The list of animation priorities
 */
public class AnimationPriorities {
    

    //
    //1
    //
    private static final int STATE_FINAL_VALUE = 1;
    public static final String STATE_FINAL = "STATE_FINAL"; //A final state for the entity (ie dying/death)

    //
    //2
    //
    private static final int MODIFIER_MAX_VALUE = 2;
    public static final String MODIFIER_MAX = "MODIFIER_MAX"; //A modifier to a bone group that is maximum priority (ie if holding an item, the animation to hold the item is top priority)

    //
    //4
    //

    //
    //5
    //
    private static final int MOVEMENT_MODIFIER_VALUE = 5;
    public static final String MOVEMENT_MODIFIER = "MOVEMENT_MODIFIER"; //When a state is modifying the core movement tree active currently (think sprinting, jumping, landing, etc)

    //
    //6
    //
    private static final int MODIFIER_HIGH_VALUE = 6;
    public static final String MODIFIER_HIGH = "MODIFIER_HIGH"; //A modifier to a bone group that is high priority (think holding the arm upright when a sword is equipped)


    //
    //7
    //
    private static final int CORE_MOVEMENT_VALUE = 7;
    public static final String CORE_MOVEMENT = "CORE_MOVEMENT"; //A core movement tree (ie base ground movement)


    //
    //8
    //
    private static final int INTERACTION_VALUE = 8;
    public static final String INTERACTION = "INTERACTION"; //An interaction animation (ie grabbing an item, dropping an item, etc)



    //
    //10
    //
    private static final int IDLE_VALUE = 10;
    public static final String IDLE = "IDLE"; //An idle animation


    //
    //50
    //
    private static final int DEFAULT_VALUE = 50;
    public static final String DEFAULT = "DEFAULT"; //The default (low) priority

    /**
     * Gets the value of animation category
     * @param type The animation category
     * @return the priority
     */
    public static int getValue(String category){
        switch(category){
            case STATE_FINAL:
                return STATE_FINAL_VALUE;
            case MODIFIER_MAX:
                return MODIFIER_MAX_VALUE;
            case MOVEMENT_MODIFIER:
                return MOVEMENT_MODIFIER_VALUE;
            case MODIFIER_HIGH:
                return MODIFIER_HIGH_VALUE;
            case CORE_MOVEMENT:
                return CORE_MOVEMENT_VALUE;
            case INTERACTION:
                return INTERACTION_VALUE;
            case IDLE:
                return IDLE_VALUE;
            case DEFAULT:
                return DEFAULT_VALUE;
            default: {
                LoggerInterface.loggerEngine.WARNING("Trying to get priority of animation category that doesn't exist! " + category);
                return DEFAULT_VALUE;
            }
        }
    }

}
