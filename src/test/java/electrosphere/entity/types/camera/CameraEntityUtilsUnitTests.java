package electrosphere.entity.types.camera;

import org.junit.jupiter.api.Assertions;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.test.annotations.FastTest;
import electrosphere.test.annotations.UnitTest;

/**
 * Camera entity utils unit tests
 */
public class CameraEntityUtilsUnitTests {

    @UnitTest
    @FastTest
    public void getCameraCenter_NullValue_NoThrow(){
        Assertions.assertDoesNotThrow(() -> {
            CameraEntityUtils.getCameraCenter(null);
        });
    }

    @UnitTest
    @FastTest
    public void getCameraEye_NullValue_NoThrow(){
        Assertions.assertDoesNotThrow(() -> {
            CameraEntityUtils.getCameraEye(null);
        });
    }

    @UnitTest
    @FastTest
    public void getCameraYaw_NullValue_NoThrow(){
        Assertions.assertDoesNotThrow(() -> {
            CameraEntityUtils.getCameraYaw(null);
        });
    }
    
}
