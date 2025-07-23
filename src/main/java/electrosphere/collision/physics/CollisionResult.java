package electrosphere.collision.physics;

import org.joml.Vector3d;

/**
 * The result of a collision
 */
public class CollisionResult {
    
    /**
     * The penetration of two bodies
     */
    double penetration;

    /**
     * The normal of the collision
     */
    Vector3d normal;

    /**
     * Gets the penetration of the collision result
     * @return The penetration
     */
    public double getPenetration() {
        return penetration;
    }

    /**
     * Sets the penetration of the collision result
     * @param penetration The penetration
     */
    public void setPenetration(double penetration) {
        this.penetration = penetration;
    }

    /**
     * Gets the normal of the collision result
     * @return The normal
     */
    public Vector3d getNormal() {
        return normal;
    }

    /**
     * Sets the normal of the collision result
     * @param normal The normal
     */
    public void setNormal(Vector3d normal) {
        this.normal = normal;
    }

    

}
