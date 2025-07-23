package electrosphere.renderer.meshgen;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;

import electrosphere.test.annotations.UnitTest;

/**
 * Tests for the transvoxel model generation functions
 */
public class TransvoxelModelGenerationTests {
    

    @UnitTest
    public void test_VertexInterp_1(){
        assertThrows(Error.class, () -> {
            TransvoxelModelGeneration.VertexInterp(
                TerrainChunkModelGeneration.MIN_ISO_VALUE,
                new Vector3f(15, 0, 6),
                new Vector3f(16, 0, 6),
                0.08262646198272705,
                0.009999990463256836,
                new Vector3f()
            );
        });
    }

    @UnitTest
    public void test_VertexInterp_2(){
        Vector3f vec1 = new Vector3f();
        TransvoxelModelGeneration.VertexInterp(
            TerrainChunkModelGeneration.MIN_ISO_VALUE,
            new Vector3f(1, 2, 0),
            new Vector3f(0, 2, 0),
            -0.338376522064209,
            0.0,
            vec1
        );
        Vector3f vec2 = new Vector3f();
        TransvoxelModelGeneration.VertexInterp(
            TerrainChunkModelGeneration.MIN_ISO_VALUE,
            new Vector3f(0, 2, 0),
            new Vector3f(0, 3, 0),
            0,
            -1.0,
            vec2
        );
        assertNotEquals(vec1, vec2);
    }

    @UnitTest
    public void test_VertexInterp_3(){
        Vector3f vec1 = new Vector3f();
        TransvoxelModelGeneration.VertexInterp(
            TerrainChunkModelGeneration.MIN_ISO_VALUE,
            new Vector3f(8, 8, 15.5f),
            new Vector3f(9, 8, 15.5f),
            0.0,
            -1.0,
            vec1
        );
        Vector3f vec2 = new Vector3f();
        TransvoxelModelGeneration.VertexInterp(
            TerrainChunkModelGeneration.MIN_ISO_VALUE,
            new Vector3f(9, 8, 15.5f),
            new Vector3f(9, 9, 15.5f),
            -1.0,
            0.0,
            vec2
        );
        assertNotEquals(vec1, vec2);
    }

    @UnitTest
    public void test_VertexInterp_4(){
        Vector3f vec1 = new Vector3f();
        TransvoxelModelGeneration.VertexInterp(
            TerrainChunkModelGeneration.MIN_ISO_VALUE,
            new Vector3f(8, 8, 15.5f),
            new Vector3f(9, 8, 15.5f),
            0.5,
            -1.0,
            vec1
        );
        assertNotEquals(0.5, vec1.y);
    }

    @UnitTest
    public void test_VertexInterp_5(){
        Vector3f vec1 = new Vector3f();
        TransvoxelModelGeneration.VertexInterp(
            TerrainChunkModelGeneration.MIN_ISO_VALUE,
            new Vector3f(8, 8, 15.5f),
            new Vector3f(9, 8, 15.5f),
            0.7,
            -1.0,
            vec1
        );
        assertNotEquals(0.7, vec1.y);
    }

    @UnitTest
    public void test_VertexInterp_6(){
        Vector3f vec1 = new Vector3f();
        TransvoxelModelGeneration.VertexInterp(
            TerrainChunkModelGeneration.MIN_ISO_VALUE,
            new Vector3f(8, 8, 15.5f),
            new Vector3f(9, 8, 15.5f),
            0.2,
            -1.0,
            vec1
        );
        assertNotEquals(0.2, vec1.y);
    }

}
