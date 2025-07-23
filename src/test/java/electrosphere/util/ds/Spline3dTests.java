package electrosphere.util.ds;

import static org.junit.jupiter.api.Assertions.*;

import static electrosphere.test.testutils.Assertions.*;

import org.joml.Vector3d;

import electrosphere.test.annotations.UnitTest;
import electrosphere.util.ds.Spline3d.Spline3dType;

/**
 * Tests for the spline 3d implementation
 */
public class Spline3dTests {
    
    @UnitTest
    public void testBasicLine1(){
        double t = 1.5;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
            new Vector3d(3,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicLine2(){
        double t = 1.99;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
            new Vector3d(3,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicLine3(){
        double t = 1.01;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
            new Vector3d(3,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicYCurve1(){
        double t = 1.5;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,1,0),
            new Vector3d(3,1,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertEquals(0.5,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicYCurve2(){
        double t = 1.01;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,1,0),
            new Vector3d(3,1,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertVeryClose(0.00515,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicYCurve3(){
        double t = 1.99;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,1,0),
            new Vector3d(3,1,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertVeryClose(0.9948,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicZCurve1(){
        double t = 1.5;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,1),
            new Vector3d(3,0,1),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0.5,midpoint.z);
    }

    @UnitTest
    public void testBasicZCurve2(){
        double t = 1.01;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,1),
            new Vector3d(3,0,1),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertEquals(0,midpoint.y);
        assertVeryClose(0.00515,midpoint.z);
    }

    @UnitTest
    public void testBasicZCurve3(){
        double t = 1.99;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,1),
            new Vector3d(3,0,1),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(t,midpoint.x);
        assertEquals(0,midpoint.y);
        assertVeryClose(0.9948,midpoint.z);
    }

    @UnitTest
    public void testBasicLineMissingFirst1(){
        double t = 0.5;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
            new Vector3d(3,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(1.5,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicLineMissingFirst2(){
        double t = 0.99;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
            new Vector3d(3,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(1.99,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicLineMissingFirst3(){
        double t = 0.01;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
            new Vector3d(3,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(1.01,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicLineMissingLast1(){
        double t = 1.5;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(1.5,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicLineMissingLast2(){
        double t = 1.99;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(1.99,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testBasicLineMissingLast3(){
        double t = 1.01;
        Spline3d spline = new Spline3d(Spline3dType.CATMULL_ROM, new Vector3d[]{
            new Vector3d(0,0,0),
            new Vector3d(1,0,0),
            new Vector3d(2,0,0),
        });
        Vector3d midpoint = spline.getPos(t);
        assertEquals(1.01,midpoint.x);
        assertEquals(0,midpoint.y);
        assertEquals(0,midpoint.z);
    }

    @UnitTest
    public void testCanAddPoint(){
        Spline3d spline = Spline3d.createCatmullRom();
        spline.addPoint(new Vector3d(0,0,0));
        assertEquals(1, spline.getPoints().size());
    }

    @UnitTest
    public void testCanRemovePoint(){
        Spline3d spline = Spline3d.createCatmullRom();
        spline.addPoint(new Vector3d(0,0,0));
        spline.removePoint(new Vector3d(0,0,0));
        assertEquals(0, spline.getPoints().size());
    }
    
    @UnitTest
    public void testCanContainsCheckPoint(){
        Spline3d spline = Spline3d.createCatmullRom();
        spline.addPoint(new Vector3d(0,0,0));
        assertTrue(spline.containsPoint(new Vector3d(0,0,0)));
    }

}
