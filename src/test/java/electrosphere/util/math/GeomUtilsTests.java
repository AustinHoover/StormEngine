package electrosphere.util.math;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3d;

import electrosphere.test.annotations.UnitTest;
import electrosphere.util.ds.Spline3d;

/**
 * Tests for geometry utils
 */
public class GeomUtilsTests {
    
    @UnitTest
    public void test_getMinDistanceAABB_center_0(){
        assertEquals(0, GeomUtils.getMinDistanceAABB(new Vector3d(1,1,1), new Vector3d(0,0,0), new Vector3d(2,2,2)));
    }

    @UnitTest
    public void test_getMinDistanceAABB_xSide_1(){
        assertEquals(1, GeomUtils.getMinDistanceAABB(new Vector3d(3,0,0), new Vector3d(0,0,0), new Vector3d(2,2,2)));
    }

    @UnitTest
    public void test_getMinDistanceAABB_ySide_1(){
        assertEquals(1, GeomUtils.getMinDistanceAABB(new Vector3d(0,3,0), new Vector3d(0,0,0), new Vector3d(2,2,2)));
    }

    @UnitTest
    public void test_getMinDistanceAABB_zSide_1(){
        assertEquals(1, GeomUtils.getMinDistanceAABB(new Vector3d(0,0,3), new Vector3d(0,0,0), new Vector3d(2,2,2)));
    }

    @UnitTest
    public void test_getMinDistanceAABB_angle_1(){
        assertEquals(Math.sqrt(2), GeomUtils.getMinDistanceAABB(new Vector3d(3,3,1), new Vector3d(0,0,0), new Vector3d(2,2,2)));
    }

    @UnitTest
    public void test_getMinDistanceAABB_angle_2(){
        assertEquals(Math.sqrt(3), GeomUtils.getMinDistanceAABB(new Vector3d(3,3,3), new Vector3d(0,0,0), new Vector3d(2,2,2)));
    }

    @UnitTest
    public void test_getPointSplineDist_1(){
        Spline3d testSpline = Spline3d.createCatmullRom(new Vector3d[]{
            new Vector3d(0,5,5),
            new Vector3d(10,5,5),
            new Vector3d(20,5,5),
            new Vector3d(30,5,5)
        });
        assertTrue(GeomUtils.getPointSplineDist(new Vector3d(10,3,5), testSpline) < 3);
        assertTrue(GeomUtils.getPointSplineDist(new Vector3d(10,3,5), testSpline) > 0);
    }

    @UnitTest
    public void test_getPointSplineDist_2(){
        Spline3d testSpline = Spline3d.createCatmullRom(new Vector3d[]{
            new Vector3d(500000,10,500000),
            new Vector3d(500010,10,500000),
            new Vector3d(500020,10,500000),
            new Vector3d(500030,10,500000)
        });
        assertTrue(GeomUtils.getPointSplineDist(new Vector3d(500010,11,500000), testSpline) < 3);
        assertTrue(GeomUtils.getPointSplineDist(new Vector3d(500010,11,500000), testSpline) > 0);
    }

    @UnitTest
    public void test_getPointSplineDist_3(){
        Spline3d testSpline = Spline3d.createCatmullRom(new Vector3d[]{
            new Vector3d(524268,10,524288),
            new Vector3d(524278,10,524288),
            new Vector3d(524298,10,524288),
            new Vector3d(524308,10,524288)
        });
        double dist = GeomUtils.getPointSplineDist(new Vector3d(524298,10,524288), testSpline);
        assertTrue(dist < 3, dist + "");
        assertTrue(dist > 0, dist + "");
    }

    @UnitTest
    public void test_pointIntersectsSpline_1(){
        Spline3d testSpline = Spline3d.createCatmullRom(new Vector3d[]{
            new Vector3d(500000,10,500000),
            new Vector3d(500010,10,500000),
            new Vector3d(500020,10,500000),
            new Vector3d(500030,10,500000)
        });
        assertTrue(GeomUtils.pointIntersectsSpline(new Vector3d(500010,8,500000), testSpline, 3));
    }

    @UnitTest
    public void test_pointIntersectsSpline_2(){
        Spline3d testSpline = Spline3d.createCatmullRom(new Vector3d[]{
            new Vector3d(500000,10,500000),
            new Vector3d(500010,10,500000),
            new Vector3d(500020,10,500000),
            new Vector3d(500030,10,500000)
        });
        assertFalse(GeomUtils.pointIntersectsSpline(new Vector3d(500010,20,500000), testSpline, 3));
    }

    @UnitTest
    public void test_pointIntersectsSpline_3(){
        Spline3d testSpline = Spline3d.createCatmullRom(new Vector3d[]{
            new Vector3d(524268,10,524288),
            new Vector3d(524278,10,524288),
            new Vector3d(524298,10,524288),
            new Vector3d(524308,10,524288)
        });
        assertTrue(GeomUtils.pointIntersectsSpline(new Vector3d(524298.0,10,524288.0), testSpline, 3));
    }

    @UnitTest
    public void test_pointIntersectsConvexPrism_1(){
        //prism
        Vector3d[] basePoints = new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(3,0,10),
            new Vector3d(10,0,10),
            new Vector3d(10,0,7),
        };
        double height = 2;

        //point
        Vector3d testPt = new Vector3d(5,1,5);

        assertTrue(GeomUtils.pointIntersectsConvexPrism(testPt, basePoints, height));
    }

    @UnitTest
    public void test_pointIntersectsConvexPrism_2(){
        //prism
        Vector3d[] basePoints = new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(3,0,10),
            new Vector3d(10,0,10),
            new Vector3d(10,0,7),
        };
        double height = 2;

        //point
        Vector3d testPt = new Vector3d(10,1,5);

        assertFalse(GeomUtils.pointIntersectsConvexPrism(testPt, basePoints, height));
    }

    @UnitTest
    public void test_pointIntersectsConvexPrism_3(){
        //prism
        Vector3d[] basePoints = new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(3,0,10),
            new Vector3d(10,0,10),
            new Vector3d(10,0,7),
        };
        double height = 2;

        //point
        Vector3d testPt = new Vector3d(10,5,5);

        assertFalse(GeomUtils.pointIntersectsConvexPrism(testPt, basePoints, height));
    }

    @UnitTest
    public void test_pointIntersectsConvexPrism_4(){
        //prism
        Vector3d[] basePoints = new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(3,0,10),
            new Vector3d(10,0,10),
            new Vector3d(10,0,7),
        };
        double height = 2;

        //point
        Vector3d testPt = new Vector3d(10,-1,5);

        assertFalse(GeomUtils.pointIntersectsConvexPrism(testPt, basePoints, height));
    }

}
