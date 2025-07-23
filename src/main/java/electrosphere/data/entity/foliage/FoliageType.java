package electrosphere.data.entity.foliage;

import java.util.List;

import electrosphere.data.entity.common.CommonEntityType;

/**
 * A foliage object, ambient or otherwise
 */
public class FoliageType extends CommonEntityType {

    /**
     * Denotes an ambient foliage that will be placed on a voxel
     */
    public static final String TOKEN_AMBIENT = "AMBIENT";

    /**
     * Denotes an tree object
     */
    public static final String TOKEN_TREE = "TREE";
    
    /**
     * The physics object(s) for the foliage
     */
    List<PhysicsObject> physicsObjects;

    /**
     * Data controlling the procedural grass
     */
    GrassData grassData;

    /**
     * Gets the physics object(s)
     * @return The physics object(s)
     */
    public List<PhysicsObject> getPhysicsObjects() {
        return physicsObjects;
    }

    /**
     * Gets the grass data of the foliage
     * @return The grass data
     */
    public GrassData getGrassData() {
        return grassData;
    }
    
    
}
