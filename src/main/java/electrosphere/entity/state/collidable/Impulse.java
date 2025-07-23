package electrosphere.entity.state.collidable;

import org.joml.Vector3d;

/**
 * An impulse to be applied to a collidable
 */
public class Impulse {
    
    Vector3d direction;
    Vector3d collisionPoint;
    Vector3d worldPoint;
    double force;
    String type;
    
    public Impulse(Vector3d forceDir, Vector3d collisionPoint, Vector3d worldPoint, double force, String type){
        this.force = force;
        this.direction = forceDir;
        this.collisionPoint = collisionPoint;
        this.type = type;
        this.worldPoint = worldPoint;
    }

    /**
     * Constructor for collidable work
     */
    public Impulse(){
        this.direction = new Vector3d();
        this.collisionPoint = new Vector3d();
        this.worldPoint = new Vector3d();
        this.force = 0;
        this.type = null;
    }

    /**
     * Gets the direction of the impulse
     * @return The direction of the impulse
     */
    public Vector3d getDirection() {
        return direction;
    }

    /**
     * Gets the force of the impulse
     * @return The force of the impulse
     */
    public double getForce() {
        return force;
    }

    /**
     * Gets the type of the impulse
     * @return The type of the impulse
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the collision point of the impulse
     * @return The collision point of the impulse
     */
    public Vector3d getCollisionPoint() {
        return collisionPoint;
    }

    /**
     * Gets the world point of the impulse
     * @return The world point of the impulse
     */
    public Vector3d getWorldPoint(){
        return worldPoint;
    }

    /**
     * Sets the direction of the impulse
     * @param direction The direction of the impulse
     */
    public void setDirection(Vector3d direction) {
        this.direction = direction;
    }

    /**
     * Sets the collision point of the impulse
     * @param collisionPoint The collision point of the impulse
     */
    public void setCollisionPoint(Vector3d collisionPoint) {
        this.collisionPoint = collisionPoint;
    }

    /**
     * Sets the world point of the impulse
     * @param worldPoint The world point of the impulse
     */
    public void setWorldPoint(Vector3d worldPoint) {
        this.worldPoint = worldPoint;
    }

    /**
     * Sets the force of the impulse
     * @param force The force of the impulse
     */
    public void setForce(double force) {
        this.force = force;
    }

    /**
     * Sets the type of the impulse
     * @param type The type of the impulse
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Clears the data in the impulse
     */
    public void clear(){
        this.direction.set(0,0,0);
        this.collisionPoint.set(0,0,0);
        this.worldPoint.set(0,0,0);
        this.force = 0;
        this.type = null;
    }

    
    
    
}
