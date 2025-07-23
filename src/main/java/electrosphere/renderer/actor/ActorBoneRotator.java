package electrosphere.renderer.actor;

import org.joml.Quaternionf;

/**
 * Optional rotation to be applied to a bone that can be programmatically controlled and applied alongside another animation.
 * For instance, having a character look at something, turn their torso mid animation, or hair rotating around.
 */
public class ActorBoneRotator {
    
    /**
     * The rotation to apply
     */
    Quaternionf rotation = new Quaternionf().identity();

    /**
     * Gets the rotation to apply
     * @return The rotation to apply
     */
    public Quaternionf getRotation(){
        return rotation;
    }

}
