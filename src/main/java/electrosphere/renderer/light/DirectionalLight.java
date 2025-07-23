package electrosphere.renderer.light;

import org.joml.Vector3f;

/**
 * A directional light source (global)
 */
public class DirectionalLight {

    /**
     * the direction of the light as a uniform vector
     */
    private Vector3f direction;

    /**
     * the color
     */
    private Vector3f color;

    /**
     * Sets the direection of the directional light
     * @param direction The direction
     */
    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    /**
     * Sets the color of the light
     * @param color The color
     */
    public void setColor(Vector3f color) {
        this.color = color;
    }

    /**
     * Gets the direction of the light
     * @return The direction
     */
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * Gets the color of the light
     * @return The color
     */
    public Vector3f getColor() {
        return color;
    }

    /**
     * Constructor
     * @param direction The direction of the light
     */
    public DirectionalLight(Vector3f direction){
        this.direction = direction;
        color = new Vector3f(1.0f);
        this.direction.normalize();
    }

    /**
     * Constructor
     * @param direction The direction of the light
     * @param color The color of the light
     */
    public DirectionalLight(Vector3f direction, Vector3f color){
        this.direction = direction;
        this.color = new Vector3f(color);
        this.direction.normalize();
    }
}