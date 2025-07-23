package electrosphere.server.pathfinding.recast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.joml.Vector3d;
import org.recast4j.detour.MeshData;
import org.recast4j.recast.ContourSet;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.geom.SingleTrimeshInputGeomProvider;

import electrosphere.entity.state.collidable.TriGeomData;
import electrosphere.test.annotations.UnitTest;
import electrosphere.util.math.GeomUtils;

/**
 * Tests for navmesh constructor
 */
public class NavMeshConstructorTests {

    /**
     * Expected size of the geom 1 aabb
     */
    static final int GEOM_1_EXPECTED_AABB = 1;

    /**
     * Number of spans expected for geom 1
     */
    static final int GEOM_1_SPAN_COUNT = 496;
    
    /**
     * Test constructing a simple navmesh
     */
    @UnitTest
    public void test_constructNavmesh_geom1(){
        TriGeomData geom = new TriGeomData() {
            @Override
            public float[] getVertices() {
                return new float[]{
                    0f, 0f, 0f,
                    10f, 0f, 0f,
                    0f, 0f, 10f
                };
            }
            @Override
            public int[] getFaceElements() {
                return new int[]{
                    2, 1, 0
                };
            }
        };

        MeshData meshData = NavMeshConstructor.constructNavmesh(geom);

        assertNotNull(meshData);
    }

    /**
     * Test constructing a simple navmesh
     */
    @UnitTest
    public void test_countSpans_geom1(){
        TriGeomData geom = new TriGeomData() {
            @Override
            public float[] getVertices() {
                return new float[]{
                    0f, 0f, 0f,
                    10f, 0f, 0f,
                    0f, 0f, 10f
                };
            }
            @Override
            public int[] getFaceElements() {
                return new int[]{
                    2, 1, 0
                };
            }
        };

        //actually build polymesh
        RecastBuilderResult recastBuilderResult = NavMeshConstructor.buildPolymesh(geom);
        int spanCount = NavMeshConstructor.countSpans(recastBuilderResult);

        assertEquals(GEOM_1_SPAN_COUNT, spanCount);
    }

    /**
     * Test constructing a simple navmesh
     */
    @UnitTest
    public void test_countWalkableSpans_geom1(){
        TriGeomData geom = new TriGeomData() {
            @Override
            public float[] getVertices() {
                return new float[]{
                    0f, 0f, 0f,
                    10f, 0f, 0f,
                    0f, 0f, 10f
                };
            }
            @Override
            public int[] getFaceElements() {
                return new int[]{
                    2, 1, 0
                };
            }
        };

        //actually build polymesh
        RecastBuilderResult recastBuilderResult = NavMeshConstructor.buildPolymesh(geom);
        int spanCount = NavMeshConstructor.countWalkableSpans(recastBuilderResult);

        assertEquals(GEOM_1_SPAN_COUNT, spanCount);
    }

    /**
     * Test constructing a simple navmesh
     */
    @UnitTest
    public void test_ContourCount_geom1(){
        TriGeomData geom = new TriGeomData() {
            @Override
            public float[] getVertices() {
                return new float[]{
                    0f, 0f, 0f,
                    10f, 0f, 0f,
                    0f, 0f, 10f
                };
            }
            @Override
            public int[] getFaceElements() {
                return new int[]{
                    2, 1, 0
                };
            }
        };

        //actually build polymesh
        RecastBuilderResult recastBuilderResult = NavMeshConstructor.buildPolymesh(geom);

        GeomUtils.checkWinding(geom.getVertices(), geom.getFaceElements());

        ContourSet contourSet = recastBuilderResult.getContourSet();
        assertNotEquals(0,contourSet.width);
        assertNotEquals(0,contourSet.height);
        assertNotEquals(0,contourSet.conts.size());
    }

    /**
     * Test constructing a simple navmesh
     */
    @UnitTest
    public void test_getSingleTrimeshInputGeomProvider_geom1(){
        TriGeomData geom = new TriGeomData() {
            @Override
            public float[] getVertices() {
                return new float[]{
                    0f, 0f, 0f,
                    10f, 0f, 0f,
                    0f, 0f, 10f
                };
            }
            @Override
            public int[] getFaceElements() {
                return new int[]{
                    2, 1, 0
                };
            }
        };

        SingleTrimeshInputGeomProvider geomProvider = NavMeshConstructor.getSingleTrimeshInputGeomProvider(geom);
        assertNotNull(geomProvider);

        Vector3d aabbStart = new Vector3d(geomProvider.getMeshBoundsMin()[0],geomProvider.getMeshBoundsMin()[1],geomProvider.getMeshBoundsMin()[2]);
        Vector3d aabbEnd = new Vector3d(geomProvider.getMeshBoundsMax()[0],geomProvider.getMeshBoundsMax()[1],geomProvider.getMeshBoundsMax()[2]);

        boolean greaterThan = aabbStart.distance(aabbEnd) > GEOM_1_EXPECTED_AABB;
        assertEquals(true, greaterThan);
    }

}
