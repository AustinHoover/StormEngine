package electrosphere.data.entity.creature.movement;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.creature.SprintSystem;

/**
 * A ground movement system's data
 */
public class GroundMovementSystem implements MovementSystem {

    //move system type string
    public static final String GROUND_MOVEMENT_SYSTEM = "GROUND";
    
    //type of move system
    String type;

    //core physics numbers
    float acceleration;
    float maxVelocity;

    //The multiplier for movement speed when strafing
    Float strafeMultiplier;

    //The multiplier for movement speed when backpedaling
    Float backpedalMultiplier;

    //startup data
    TreeDataAnimation animationStartup;

    //loop data
    TreeDataAnimation animationLoop;

    //wind down data
    TreeDataAnimation animationWindDown;

    //sprint data
    SprintSystem sprintSystem;

    /**
     * The offset into the main animation to play the first footstep sound
     */
    Float footstepFirstAudioOffset;

    /**
     * The offset into the main animation to play the second footstep sound
     */
    Float footstepSecondAudioOffset;
    

    /**
     * Gets the acceleration factor for this creature
     * @return the acceleration factor
     */
    public float getAcceleration() {
        return acceleration;
    }

    /**
     * Gets the maximum velocity the creature can move at with this system
     * @return the max velocity
     */
    public float getMaxVelocity() {
        return maxVelocity;
    }

    /**
     * Gets the animation to play for startup
     * @return The animation data
     */
    public TreeDataAnimation getAnimationStartup() {
        return animationStartup;
    }

    /**
     * Gets the animation to loop
     * @return The animation data
     */
    public TreeDataAnimation getAnimationLoop() {
        return animationLoop;
    }

    /**
     * Gets the animation to play to wind down
     * @return The animation data
     */
    public TreeDataAnimation getAnimationWindDown() {
        return animationWindDown;
    }

    /**
     * Gets the sprint system data
     * @return The sprint system data
     */
    public SprintSystem getSprintSystem() {
        return sprintSystem;
    }

    /**
     * Gets the multiplier applied to the movement speed while the creature is strafing
     * @return The multiplier
     */
    public Float getStrafeMultiplier(){
        return this.strafeMultiplier;
    }

    /**
     * Gets the multiplier applied to the movement speed while the creature is backpedaling
     * @return The multiplier
     */
    public Float getBackpedalMultiplier(){
        return this.backpedalMultiplier;
    }

    /**
     * Gets the offset into the main animation to play the audio for the first footstep
     * @return The offset into the audio
     */
    public Float getFootstepFirstAudioOffset(){
        return footstepFirstAudioOffset;
    }

    /**
     * Gets the offset into the main animation to play the audio for the second footstep
     * @return The offset into the audio
     */
    public Float getFootstepSecondAudioOffset(){
        return footstepSecondAudioOffset;
    }

    @Override
    public String getType() {
        return type;
    }
}
