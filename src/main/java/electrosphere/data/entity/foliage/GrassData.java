package electrosphere.data.entity.foliage;

import org.joml.Vector3f;

/**
 * Data specific to the grass
 */
public class GrassData {
    
    /**
     * Color at the base of the model
     */
    Vector3f baseColor;

    /**
     * Color at the tip of the model
     */
    Vector3f tipColor;

    /**
     * The maximum curve allowed on the tip
     */
    float maxTipCurve;

    /**
     * Minimum height of the grass
     */
    float minHeight;

    /**
     * Maximum height of the grass
     */
    float maxHeight;

    /**
     * Gets the base color of the grass
     * @return The base color
     */
    public Vector3f getBaseColor() {
        return baseColor;
    }

    /**
     * Gets the tip color of the grass
     * @return The tip color
     */
    public Vector3f getTipColor() {
        return tipColor;
    }

    /**
     * Gets the maximum curve allowed on the tip
     * @return The maximum curve allowed on the tip
     */
    public float getMaxTipCurve() {
        return maxTipCurve;
    }

    /**
     * Sets the maximum curve allowed on the tip
     * @param maxTipCurve The maximum curve allowed on the tip
     */
    public void setMaxTipCurve(float maxTipCurve) {
        this.maxTipCurve = maxTipCurve;
    }

    /**
     * Gets the minimum height of the grass
     * @return The minimum height of the grass
     */
    public float getMinHeight() {
        return minHeight;
    }

    /**
     * Sets the minimum height of the grass
     * @param minHeight the minimum height of the grass
     */
    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }

    /**
     * Gets the maximum height of the grass
     * @return The maximum height of the grass
     */
    public float getMaxHeight() {
        return maxHeight;
    }

    /**
     * Sets the maximum height of the grass
     * @param minHeight the maximum height of the grass
     */
    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    
    

}
