package electrosphere.collision.hitbox;

import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;

import org.joml.Vector3d;

/**
 * Utilities for working with hitboxes
 */
public class HitboxUtils {
    
    /**
     * Gets the data for a hitbox
     * @param e the entity encapsulating the hitbox
     * @return the hitbox data
     */
    public static HitboxData getHitboxData(Entity e){
        return (HitboxData)e.getData(EntityDataStrings.HITBOX_DATA);
    }

    /**
     * Intended to be implemented as an anonoymous class when needed
     */
    public interface HitboxPositionCallback {
        /**
         * Gets the current position this hitbox should be at
         * @return The position this hitbox should be at
         */
        public Vector3d getPosition();
    }
    
}
