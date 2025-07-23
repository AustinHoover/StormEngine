package electrosphere.util.math.region;

import org.joml.AABBd;
import org.joml.Sphered;
import org.joml.Vector3d;

/**
 * A rectangular region
 */
public class RegionRectangular implements Region {

    /**
     * The prism region type
     */
    public static final String TYPE_STRING = "RECTANGULAR";

    /**
     * The type of region
     */
    private final String type = RegionRectangular.TYPE_STRING;

    /**
     * The AABB of the area selection
     */
    private AABBd aabb;

    /**
     * Private constructor
     */
    private RegionRectangular(){ }

    /**
     * Creates a rectangular selection
     * @param start The start point
     * @param end The end point
     * @return The selection
     */
    public static RegionRectangular create(Vector3d start, Vector3d end){
        if(start.x > end.x){
            throw new Error("Start x is less than end x! " + start.x + " " + end.x);
        }
        if(start.y > end.y){
            throw new Error("Start y is less than end y! " + start.y + " " + end.y);
        }
        if(start.z > end.z){
            throw new Error("Start y is less than end y! " + start.z + " " + end.z);
        }
        RegionRectangular rVal = new RegionRectangular();
        rVal.aabb = new AABBd(start, end);
        return rVal;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean intersects(Vector3d point) {
        return aabb.testPoint(point);
    }

    @Override
    public boolean intersects(Region other) {
        if(other.getType() != RegionRectangular.TYPE_STRING){
            throw new Error("One of the areas to test is not rectangular! " + other.getType());
        }
        return aabb.testAABB(other.getAABB());
    }

    @Override
    public boolean intersects(Sphered sphere) {
        return aabb.testSphere(sphere.x, sphere.y, sphere.z, sphere.r);
    }

    @Override
    public AABBd getAABB() {
        return this.aabb;
    }
    
}
