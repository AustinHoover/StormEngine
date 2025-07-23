package electrosphere.data.entity.creature.movement;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;

/**
 * A jump tree's data
 */
public class JumpMovementSystem implements MovementSystem {

    /**
     * The name for this movement tree system
     */
    public static final String JUMP_MOVEMENT_SYSTEM = "JUMP";

    //The type of tree
    String type;

    /**
     * The animation to play when jumping
     */
    TreeDataAnimation animationJump;

    /**
     * The number of frames to apply the jump force
     */
    int jumpFrames;

    /**
     * The force to apply while jump state
     */
    float jumpForce;

    /**
     * Gets the number of frames to apply jump force for
     * @return The number of frames
     */
    public int getJumpFrames(){
        return jumpFrames;
    }

    /**
     * Gets the force to jump with
     * @return The force
     */
    public float getJumpForce(){
        return jumpForce;
    }

    /**
     * Gets the animation to play while jumping
     * @return The animation
     */
    public TreeDataAnimation getAnimationJump(){
        return animationJump;
    }

    @Override
    public String getType() {
        return type;
    }

}
