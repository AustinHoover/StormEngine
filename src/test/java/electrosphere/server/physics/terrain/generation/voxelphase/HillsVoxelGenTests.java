package electrosphere.server.physics.terrain.generation.voxelphase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import electrosphere.test.annotations.FastTest;
import electrosphere.test.annotations.UnitTest;

/**
 * Unit tests for the test generation chunk generator
 */
public class HillsVoxelGenTests {

    @UnitTest
    @FastTest
    public void getSurfaceWeight_ValueTests(){
        assertEquals(0.5,HillsVoxelGen.getSurfaceWeight(0.5, 0, 1));
        assertEquals(0.1,HillsVoxelGen.getSurfaceWeight(0.1, 0, 1));
        assertEquals(0.9,HillsVoxelGen.getSurfaceWeight(0.9, 0, 1));
        assertEquals(0.95,HillsVoxelGen.getSurfaceWeight(1.9, 0, 2));
    }
    
}
