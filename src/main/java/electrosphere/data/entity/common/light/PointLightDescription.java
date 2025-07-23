package electrosphere.data.entity.common.light;

import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Description of a point light
 */
public class PointLightDescription {
    
    /**
     * The constant attenuation factor
     */
    float constant;

    /**
     * The linear attenuation factor
     */
    float linear;
    
    /**
     * The quadratic attenuation factor
     */
    float quadratic;

    /**
     * The radius of the light
     */
    float radius;

    /**
     * The color of the light
     */
    Vector3f color;

    /**
     * The offset from the entity base to place the point light at
     */
    Vector3d offset;

    /**
     * Sets the constant attenuation factor
     * @param constant The constant attenuation factor
     */
    public void setConstant(float constant) {
        this.constant = constant;
    }

    /**
     * Sets the linear attenuation factor
     * @param linear The linear attenuation factor
     */
    public void setLinear(float linear) {
        this.linear = linear;
    }

    /**
     * Sets the quadratic attenuation factor
     * @param quadratic The quadratic attenuation factor
     */
    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }

    /**
     * Sets the radius of the light
     * @param radius The radius of the light
     */
    public void setRadius(float radius){
        this.radius = radius;
    }

    /**
     * Sets the color of the light
     * @param color The color of the light
     */
    public void setColor(Vector3f color){
        this.color = color;
    }

    /**
     * Gets the constant attenuation factor
     * @return The constant attenuation factor
     */
    public float getConstant() {
        return constant;
    }

    /**
     * Gets the linear attenuation factor
     * @return The linear attenuation factor
     */
    public float getLinear() {
        return linear;
    }

    /**
     * Gets the quadratic attenuation factor
     * @return The quadratic attenuation factor
     */
    public float getQuadratic() {
        return quadratic;
    }

    /**
     * Gets the color of the light
     * @return The color of the light
     */
    public Vector3f getColor(){
        return color;
    }

    /**
     * Gets the radius of the light
     * @return The radius of the light
     */
    public float getRadius(){
        return radius;
    }

    /**
     * Gets the offset from the base of the entity to place the point light at
     * @return The offset
     */
    public Vector3d getOffset(){
        return offset;
    }

}
