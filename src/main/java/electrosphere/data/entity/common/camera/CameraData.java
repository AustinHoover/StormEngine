package electrosphere.data.entity.common.camera;

import org.joml.Vector3d;

/**
 * Data about how the camera should interact with this entity
 */
public class CameraData {
    
    /**
     * The offset applied to the camera that tracks this entity when in third person
     */
    Vector3d thirdPersonCameraOffset;

    /**
     * Gets the offset applied to the camera that tracks this entity when in third person
     * @return
     */
    public Vector3d getThirdPersonCameraOffset() {
        return thirdPersonCameraOffset;
    }

    /**
     * Sets the offset applied to the camera that tracks this entity when in third person
     * @param thirdPersonCameraOffset
     */
    public void setThirdPersonCameraOffset(Vector3d thirdPersonCameraOffset) {
        this.thirdPersonCameraOffset = thirdPersonCameraOffset;
    }


    

}
