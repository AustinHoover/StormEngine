package electrosphere.server.macro.spatial;

import org.joml.AABBd;

/**
 * A macro object that takes up an area of space instead of just a point
 */
public interface MacroAreaObject extends MacroObject {

    /**
     * Gets the AABB for the object
     * @return The AABB
     */
    public AABBd getAABB();
    
    
}
