package electrosphere.data.entity.collidable;

import org.joml.Vector3d;

/**
 * A template for a rigid body that should be attached to an entity
 */
public class CollidableTemplate {

    /**
     * Cube collidable shape
     */
    public static final String COLLIDABLE_TYPE_CUBE = "CUBE";

    /**
     * Cylinder collidable shape
     */
    public static final String COLLIDABLE_TYPE_CYLINDER = "CYLINDER";

    /**
     * Capsule collidable shape
     */
    public static final String COLLIDABLE_TYPE_CAPSULE = "CAPSULE";
    
    /**
     * The primitive shape type
     */
    String type;
    
    /**
     * The first dimension of the rigid body
     */
    float dimension1;

    /**
     * The second dimension of the rigid body
     */
    float dimension2;

    /**
     * The third dimension of the rigid body
     */
    float dimension3;

    /**
     * The x component of the quaternion controlling the offset rotation of the body
     */
    float rotX;

    /**
     * The y component of the quaternion controlling the offset rotation of the body
     */
    float rotY;

    /**
     * The z component of the quaternion controlling the offset rotation of the body
     */
    float rotZ;

    /**
     * The w component of the quaternion controlling the offset rotation of the body
     */
    float rotW;
    
    /**
     * The x component of the vector controlling the offset position of the body
     */
    float offsetX;

    /**
     * The y component of the vector controlling the offset position of the body
     */
    float offsetY;

    /**
     * The z component of the vector controlling the offset position of the body
     */
    float offsetZ;

    /**
     * The mass of the body
     */
    Double mass;

    /**
     * The linear friction of the body
     */
    Double linearFriction;

    /**
     * The rolling friction of the body (ie if it's a sphere, what's the friction keeping it from spinning)
     */
    Double rollingFriction;

    /**
     * Controls whether the body can rotate or not
     */
    boolean angularlyStatic;

    /**
     * Controls whether the body is kinematic (infinite mass) or not
     */
    boolean kinematic;

    /**
     * The type of body (ie creature, static, foliage, etc)
     */
    String collisionType;

    /**
     * Base constructor
     */
    public CollidableTemplate(){
    }

    /**
     * Gets a box template
     * @param dims The dimensions of the box
     * @return The template
     */
    public static CollidableTemplate getBoxTemplate(Vector3d dims){
        CollidableTemplate rVal = new CollidableTemplate();
        rVal.type = CollidableTemplate.COLLIDABLE_TYPE_CUBE;
        rVal.dimension1 = (float)dims.x;
        rVal.dimension2 = (float)dims.y;
        rVal.dimension3 = (float)dims.z;
        return rVal;
    }

    /**
     * The primitive shape type
     * @return The primitive shape
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the first dimension of the rigid body
     * @return The first dimension of the rigid body
     */
    public float getDimension1() {
        return dimension1;
    }

    /**
     * Gets the second dimension of the rigid body
     * @return The second dimension of the rigid body
     */
    public float getDimension2() {
        return dimension2;
    }

    /**
     * Gets the third dimension of the rigid body
     * @return The third dimension of the rigid body
     */
    public float getDimension3() {
        return dimension3;
    }

    /**
     * Gets the x component of the quaternion controlling the offset rotation of the body
     * @return The x component of the quaternion controlling the offset rotation of the body
     */
    public float getRotX(){
        return rotX;
    }
    
    /**
     * Gets the y component of the quaternion controlling the offset rotation of the body
     * @return The y component of the quaternion controlling the offset rotation of the body
     */
    public float getRotY(){
        return rotY;
    }

    /**
     * Gets the z component of the quaternion controlling the offset rotation of the body
     * @return The z component of the quaternion controlling the offset rotation of the body
     */
    public float getRotZ(){
        return rotZ;
    }

    /**
     * Gets the w component of the quaternion controlling the offset rotation of the body
     * @return The w component of the quaternion controlling the offset rotation of the body
     */
    public float getRotW(){
        return rotW;
    }

    /**
     * Gets the x component of the vector controlling the offset position of the body
     * @return The x component of the vector controlling the offset position of the body
     */
    public float getOffsetX() {
        return offsetX;
    }

    /**
     * Gets the y component of the vector controlling the offset position of the body
     * @return The y component of the vector controlling the offset position of the body
     */
    public float getOffsetY() {
        return offsetY;
    }

    /**
     * Gets the z component of the vector controlling the offset position of the body
     * @return The z component of the vector controlling the offset position of the body
     */
    public float getOffsetZ() {
        return offsetZ;
    }

    /**
     * Gets the mass of the body
     * @return The mass
     */
    public Double getMass(){
        return mass;
    }

    /**
     * Gets the linear friction
     * @return The linear friction
     */
    public Double getLinearFriction(){
        return linearFriction;
    }

    /**
     * Gets the rolling friction (ie if it's a sphere, what's the friction keeping it from spinning)
     * @return The rolling friction
     */
    public Double getRollingFriction(){
        return rollingFriction;
    }

    /**
     * Gets if the body should always be allowed to rotate or not
     * @return true if should always be allowed to rotate, false otherwise
     */
    public boolean isAngularlyStatic(){
        return this.angularlyStatic;
    }

    /**
     * Sets the first dimension of the rigid body
     * @return The first dimension of the rigid body
     */
    public void setDimension1(float dimension1) {
        this.dimension1 = dimension1;
    }

    /**
     * Sets the second dimension of the rigid body
     * @return The second dimension of the rigid body
     */
    public void setDimension2(float dimension2) {
        this.dimension2 = dimension2;
    }

    /**
     * Sets the third dimension of the rigid body
     * @return The third dimension of the rigid body
     */
    public void setDimension3(float dimension3) {
        this.dimension3 = dimension3;
    }

    /**
     * Sets the x component of the vector controlling the offset position of the body
     * @return The x component of the vector controlling the offset position of the body
     */
    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * Sets the y component of the vector controlling the offset position of the body
     * @return The y component of the vector controlling the offset position of the body
     */
    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * Sets the z component of the vector controlling the offset position of the body
     * @return The z component of the vector controlling the offset position of the body
     */
    public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }

    /**
     * Gets whether the body is kinematic (infinite mass) or not
     * @return true if the body is kinematic, false otherwise
     */
    public boolean getKinematic() {
        return kinematic;
    }

    /**
     * Sets whether the body is kinematic (infinite mass) or not
     * @param kinematic true if the body is kinematic, false otherwise
     */
    public void setKinematic(boolean kinematic) {
        this.kinematic = kinematic;
    }

    /**
     * Gets the type of body (ie creature, static, foliage, etc)
     * @return The type of body (ie creature, static, foliage, etc)
     */
    public String getCollisionType() {
        return collisionType;
    }

    /**
     * Sets the type of body (ie creature, static, foliage, etc)
     * @param collisionType The type of body (ie creature, static, foliage, etc)
     */
    public void setCollisionType(String collisionType) {
        this.collisionType = collisionType;
    }

    
    
    
    
}
