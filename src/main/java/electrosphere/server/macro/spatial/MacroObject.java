package electrosphere.server.macro.spatial;

import org.joml.Vector3d;

/**
 * Interface for a macro object that has a spatial position
 */
public interface MacroObject {
    
    /**
     * Gets the position of this object
     * @return The position of this object
     */
    public Vector3d getPos();

    /**
     * Sets the position of this macro object
     * @param pos The macro object
     */
    public void setPos(Vector3d pos);

}
