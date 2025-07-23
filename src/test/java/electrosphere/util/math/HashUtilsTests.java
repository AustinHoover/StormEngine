package electrosphere.util.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import electrosphere.test.annotations.UnitTest;

/**
 * Tests for hash utils
 */
public class HashUtilsTests {
    
    @UnitTest
    public void test_unhashIVec(){
        int x = 1;
        int y = 2;
        int z = 3;
        long hash = HashUtils.hashIVec(x, y, z);
        
        int x_r = HashUtils.unhashIVec(hash, HashUtils.UNHASH_COMPONENT_X);
        int y_r = HashUtils.unhashIVec(hash, HashUtils.UNHASH_COMPONENT_Y);
        int z_r = HashUtils.unhashIVec(hash, HashUtils.UNHASH_COMPONENT_Z);

        assertEquals(x, x_r);
        assertEquals(y, y_r);
        assertEquals(z, z_r);
    }

}
