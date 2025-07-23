package electrosphere.renderer.light;

import org.joml.Vector3f;

import electrosphere.data.entity.common.light.PointLightDescription;

/**
 * Data about a point light
 */
public class PointLight {

    /**
     * The position of the point light
     */
    private Vector3f position;

    /**
     * The falloff constant of the point light
     */
    private float constant;

    /**
     * The linear constant of the point light
     */
    private float linear;

    /**
     * The quadratic constant of the point light
     */
    private float quadratic;

    /**
     * The radius of the point light
     */
    private float radius;

    /**
     * The color of the point light
     */
    private Vector3f color;

    /**
     * Sets the position of the point light
     * @param position The position
     */
    public void setPosition(Vector3f position) {
        this.position = position;
    }

    /**
     * Sets the constant of the falloff
     * @param constant The constant
     */
    public void setConstant(float constant) {
        this.constant = constant;
    }

    /**
     * Sets the linear constant of the falloff
     * @param linear The linear constant
     */
    public void setLinear(float linear) {
        this.linear = linear;
    }

    /**
     * Sets the quadratic of the falloff
     * @param quadratic The quadratic constant
     */
    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }

    /**
     * Sets the radius of the point light
     * @param radius The radius
     */
    public void setRadius(float radius){
        this.radius = radius;
    }

    /**
     * Sets the color of the point light
     * @param color The color
     */
    public void setColor(Vector3f color){
        this.color = color;
    }

    /**
     * Gets the position of the point light
     * @return The position
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Gets the constant of the point light
     * @return The constant
     */
    public float getConstant() {
        return constant;
    }

    /**
     * Gets the linear constant of the point light
     * @return The linear constant
     */
    public float getLinear() {
        return linear;
    }

    /**
     * Gets the quadratic constant of the point light
     * @return The quadratic constant
     */
    public float getQuadratic() {
        return quadratic;
    }

    /**
     * Gets the color of the point light
     * @return The color
     */
    public Vector3f getColor(){
        return color;
    }

    /**
     * Gets the radius of the point light
     * @return The radius
     */
    public float getRadius(){
        return radius;
    }

    /**
     * Constructs a point light
     * @param position The position of the light
     */
    protected PointLight(Vector3f position){
        this.position = position;
        radius = 1;
        constant = 1.0f;
        linear = 0.7f;
        quadratic = 1.8f;
        color = new Vector3f(1.0f);
    }

    /**
     * Constructor
     * @param position The position of the point light
     * @param color The color of the point light
     */
    protected PointLight(Vector3f position, Vector3f color){
        this.position = position;
        radius = 1;
        constant = 1.0f;
        linear = 0.7f;
        quadratic = 1.8f;
        this.color = color;
    }

    /**
     * Creates a point light from a light description
     * @param description The description
     */
    protected PointLight(PointLightDescription description){
        this.position = new Vector3f();
        this.radius = description.getRadius();
        this.constant = description.getConstant();
        this.linear = description.getLinear();
        this.quadratic = description.getQuadratic();
        this.color = description.getColor();
    }
}