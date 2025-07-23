package electrosphere.data.entity.creature;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;

/**
 * Sprint data
 */
public class SprintSystem {

    /**
     * The animation to play on starting to sprint
     */
    TreeDataAnimation animationStartUp;
    
    /**
     * The main animation to play while sprinting
     */
    TreeDataAnimation animationMain;

    /**
     * The animation to play while winding down from sprinting
     */
    TreeDataAnimation animationWindDown;

    /**
     * The modifier applied to the movement speed while sprinting
     */
    float modifier;

    /**
     * The maximum stamina
     */
    int staminaMax;

    /**
     * Gets The animation to play on starting to sprint
     * @return The animation to play on starting to sprint
     */
    public TreeDataAnimation getAnimationStartUp() {
        return animationStartUp;
    }

    /**
     * Gets The main animation to play while sprinting
     * @return The main animation to play while sprinting
     */
    public TreeDataAnimation getAnimationMain() {
        return animationMain;
    }
    
    /**
     * Gets The animation to play while winding down from sprinting
     * @return The animation to play while winding down from sprinting
     */
    public TreeDataAnimation getAnimationWindDown() {
        return animationWindDown;
    }

    /**
     * Gets The modifier applied to the movement speed while sprinting
     * @return The modifier applied to the movement speed while sprinting
     */
    public float getModifier() {
        return modifier;
    }

    /**
     * Gets The maximum stamina
     * @return The maximum stamina
     */
    public int getStaminaMax() {
        return staminaMax;
    }
    
    
}
