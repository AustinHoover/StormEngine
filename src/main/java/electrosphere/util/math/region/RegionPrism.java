package electrosphere.util.math.region;

import org.joml.AABBd;
import org.joml.Sphered;
import org.joml.Vector3d;

import electrosphere.util.math.GeomUtils;

/**
 * A prism region
 */
public class RegionPrism implements Region {

    /**
     * The prism region type
     */
    public static final String TYPE_STRING = "Y_ALIGNED_PRISM";

    /**
     * The type of region
     */
    private final String type = TYPE_STRING;


    /**
     * The height of the prism
     */
    private double height;

    /**
     * The points that make up the base polygon of the prism
     */
    private Vector3d[] points;

    /**
     * The aabb for the prism
     */
    private AABBd aabb;

    /**
     * Private constructor
     */
    private RegionPrism(){ }

    /**
     * Creates a prism region
     * @param points The points that define the base polygon of the prism
     * @param height The height of the prism
     * @return The region
     */
    public static RegionPrism create(Vector3d[] points, double height){
        RegionPrism rVal = new RegionPrism();
        rVal.height = height;
        rVal.points = points;
        //compute aabb
        rVal.aabb = new AABBd(points[0].x,points[0].y,points[0].z,points[0].x,points[0].y,points[0].z);
        for(int i = 1; i < points.length; i++){
            if(rVal.aabb.minX > points[i].x){
                rVal.aabb.minX = points[i].x;
            }
            if(rVal.aabb.minZ > points[i].z){
                rVal.aabb.minZ = points[i].z;
            }
            if(rVal.aabb.maxX < points[i].x){
                rVal.aabb.maxX = points[i].x;
            }
            if(rVal.aabb.maxZ < points[i].z){
                rVal.aabb.maxZ = points[i].z;
            }
        }
        rVal.aabb.minY = points[0].y;
        rVal.aabb.maxY = points[0].y + height;
        return rVal;
    }

    /**
     * Gets the height of the prism
     * @return The height
     */
    public double getHeight(){
        return this.height;
    }

    /**
     * Gets the points of the prism
     * @return The points of the prism
     */
    public Vector3d[] getPoints(){
        return this.points;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean intersects(Vector3d point) {
        return GeomUtils.pointIntersectsConvexPrism(point,points,height);
    }

    @Override
    public boolean intersects(Region other) {
        throw new UnsupportedOperationException("Unimplemented method 'intersects'");
    }

    @Override
    public boolean intersects(Sphered sphere) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersects'");
    }

    @Override
    public AABBd getAABB() {
        return this.aabb;
    }
    
}
