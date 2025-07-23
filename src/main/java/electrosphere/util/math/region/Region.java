package electrosphere.util.math.region;

import org.joml.AABBd;
import org.joml.Sphered;
import org.joml.Vector3d;

/**
 * A region of 3d space
 */
public interface Region {

    /**
     * Gets the type of region
     * @return The type of region
     */
    public String getType();

    /**
     * Checks if the region contains a point
     * @param point The point
     * @return true if it contains the point, false otherwise
     */
    public boolean intersects(Vector3d point);

    /**
     * Checks if this region intersects another region
     * @param other The other region
     * @return true if one intersects another, false otherwise
     */
    public boolean intersects(Region other);

    /**
     * Checks if this region intersects a sphere
     * @param sphere The sphere to check
     * @return true if intersects the sphere, false otherwise
     */
    public boolean intersects(Sphered sphere);

    /**
     * Gets the axis-aligned bounding box for the region
     * @return The axis-aligned bounding box
     */
    public AABBd getAABB();

}
