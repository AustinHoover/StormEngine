package electrosphere.audio.collision;

import org.junit.jupiter.api.Assertions;

import electrosphere.test.annotations.UnitTest;
import electrosphere.test.annotations.FastTest;


/**
 * Unit tests for hitbox audio service
 */
public class HitboxAudioServiceUnitTests {
    
    @UnitTest
    @FastTest
    public void playAudioPositional_WithNull_NoThrow(){
        HitboxAudioService hitboxAudioService = new HitboxAudioService();
        Assertions.assertDoesNotThrow(() -> {
            hitboxAudioService.playAudioPositional(null, null, null, null, null);
        });
    }

    @UnitTest
    @FastTest
    public void getAudioPath_WithNullHitboxType_ReturnsNull(){
        HitboxAudioService hitboxAudioService = new HitboxAudioService();
        Assertions.assertEquals(null, hitboxAudioService.getAudioPath(null, null, null, null));
    }

}
