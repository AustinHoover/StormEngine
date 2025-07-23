package electrosphere.util.ds.octree;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3i;

import electrosphere.test.annotations.UnitTest;

/**
 * Tests the floating position chunk tree implementation
 */
public class WorldOctTreeTests {

    /**
     * Creates a chunk tree
     */
    @UnitTest
    public void testCreateFloatingChunkTree(){
        WorldOctTree<String> tree = new WorldOctTree<String>(new Vector3i(0,0,0), new Vector3i(64,64,64));
        assertNotNull(tree);
    }

    /**
     * Test changing the centered point of the floating tree
     */
    @UnitTest
    public void testMaxLevelSetting(){
        WorldOctTree<String> tree = new WorldOctTree<String>(new Vector3i(0,0,0), new Vector3i(64,64,64));
        assertEquals(6,tree.getMaxLevel());
    }

    /**
     * Assert non-power-of-two dims fail
     */
    @UnitTest
    public void testFailOnNonPowTwoDim(){
        assertThrows(Error.class, () -> {
            new WorldOctTree<String>(new Vector3i(0,0,0), new Vector3i(63,63,63));
        });
    }

    /**
     * Assert unequal dims fail
     */
    @UnitTest
    public void testFailOnUnequalDim(){
        assertThrows(Error.class, () -> {
            new WorldOctTree<String>(new Vector3i(0,0,0), new Vector3i(64,1,64));
        });
    }

    
    
}
