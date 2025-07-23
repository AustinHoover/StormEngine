package electrosphere.server.physics.terrain.generation.voxelphase;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import electrosphere.test.annotations.UnitTest;

/**
 * Tests for noise voxel gen
 */
public class NoiseVoxelGenTests {
    
    @UnitTest
    public void test_getSurfaceWeight_1(){
        double surfaceHeight = 1.5;
        double realY = 1.0;
        double stride = 1.0;
        double finalWeight = NoiseVoxelGen.getSurfaceWeight(surfaceHeight, realY, stride);
        assertNotEquals(0.5, finalWeight);
    }

    @UnitTest
    public void test_getSurfaceWeight_2(){
        double surfaceHeight = 1.3;
        double realY = 1.0;
        double stride = 1.0;
        double finalWeight = NoiseVoxelGen.getSurfaceWeight(surfaceHeight, realY, stride);
        assertNotEquals(0.3, finalWeight);
    }

    @UnitTest
    public void test_getSurfaceWeight_3(){
        double surfaceHeight = 1.7;
        double realY = 1.0;
        double stride = 1.0;
        double finalWeight = NoiseVoxelGen.getSurfaceWeight(surfaceHeight, realY, stride);
        assertNotEquals(0.7, finalWeight);
    }

    @UnitTest
    public void test_getSurfaceWeight_4(){
        double surfaceHeight = 1.9;
        double realY = 1.0;
        double stride = 1.0;
        double finalWeight = NoiseVoxelGen.getSurfaceWeight(surfaceHeight, realY, stride);
        assertNotEquals(0.9, finalWeight);
    }

}
