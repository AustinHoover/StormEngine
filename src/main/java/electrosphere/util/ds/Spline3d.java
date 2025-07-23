package electrosphere.util.ds;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector4d;

/**
 * An implementation of a 3d Catmull-Rom spline
 */
public class Spline3d {

    /**
     * The points to interpolate between when calculating the spline
     */
    TreeMap<Double,Vector4d> pointTree;

    /**
     * The characteristic matrix for this spline
     */
    Matrix4d characteristicMatrix;

    /**
     * The type of spline to create
     */
    public static enum Spline3dType {
        /**
         * A catmull-rom spline
         */
        CATMULL_ROM,
        /**
         * A bezier spline
         */
        BEZIER,
        /**
         * A basis spline (b-spline)
         */
        B,
    }

    /**
     * Creates a spline with no points
     */
    public Spline3d(Spline3dType type){
        this.setCharacteristicMatrix(type);
        pointTree = new TreeMap<Double,Vector4d>();
    }

    /**
     * Creates a spline with predefined points
     * @param points The points
     */
    public Spline3d(Spline3dType type, List<Vector3d> points){
        this.setCharacteristicMatrix(type);
        pointTree = new TreeMap<Double,Vector4d>();
        int i = 0;
        for(Vector3d point : points){
            pointTree.put((double)i,new Vector4d(point,1.0));
            i++;
        }
    }

    /**
     * Creates a spline with predefined points
     * @param points The points
     */
    public Spline3d(Spline3dType type, Vector3d[] points){
        this.setCharacteristicMatrix(type);
        pointTree = new TreeMap<Double,Vector4d>();
        int i = 0;
        for(Vector3d point : points){
            pointTree.put((double)i,new Vector4d(point,1.0));
            i++;
        }
    }

    /**
     * Creates a catmull-rom spline
     * @return The spline
     */
    public static Spline3d createCatmullRom(){
        return new Spline3d(Spline3dType.CATMULL_ROM);
    }

    /**
     * Creates a catmull-rom spline
     * @param points The points to create the spline with
     * @return The spline
     */
    public static Spline3d createCatmullRom(Vector3d[] points){
        return new Spline3d(Spline3dType.CATMULL_ROM, points);
    }

    /**
     * Gets the position of a spline at a given position
     * @param t The time into the spline
     * @return The position along the spline at that position
     */
    public Vector3d getPos(double t){
        
        if(t < 0){
            throw new ArrayIndexOutOfBoundsException("Tried to get value outside of spline bounds! " + t);
        }
        if(t >= pointTree.size()){
            throw new ArrayIndexOutOfBoundsException("Tried to get value outside of spline bounds! " + t + " " + pointTree.size());
        }
        if(pointTree.size() < 2){
            throw new ArrayIndexOutOfBoundsException("Trying to get value along spline that only has " + pointTree.size() + " points!");
        }
        if(pointTree.floorEntry(t).getKey() == pointTree.lastEntry().getKey()){
            throw new ArrayIndexOutOfBoundsException("Trying to get value along spline after last control point! " + t + " " + pointTree.lastEntry().getKey() + " " + pointTree.size());
        }
        if(pointTree.ceilingEntry(t).getKey() == pointTree.firstEntry().getKey()){
            throw new ArrayIndexOutOfBoundsException("Trying to get value along spline before first control point! " + t + " " + pointTree.firstEntry().getKey() + " " + pointTree.size());
        }

        //get middle two points
        Vector4d p2 = pointTree.floorEntry(t).getValue();
        Vector4d p3 = pointTree.ceilingEntry(t).getValue();
        //
        //Get control points for current segment
        Vector4d p1;
        if(pointTree.floorKey(t) <= 0){
            //if we're at the beginning of the line, extrapolate from p2 based on the direction of p3
            p1 = new Vector4d(p2).add(new Vector4d(p2).sub(new Vector4d(p3)));
        } else {
            //else, use the point before p2
            p1 = pointTree.floorEntry(t-1).getValue();
        }
        Vector4d p4;
        if(pointTree.ceilingKey(t) >= pointTree.size() - 1){
            //if we're at the end of the line, extrapolate from p3 based on the direction of p2
            p4 = new Vector4d(p3).add(new Vector4d(p3).sub(new Vector4d(p2)));
        } else {
            //else, use the point after p3
            p4 = pointTree.ceilingEntry(t+1).getValue();
        }

        //t normalized to range [0,1]
        double tN = t - pointTree.floorKey(t);
        
        //
        //get the time vec
        Vector4d timeVec = new Vector4d(1.0, tN, tN*tN, tN*tN*tN);

        //
        //Perform calculation
        Matrix4d pointMat = new Matrix4d(p1,p2,p3,p4).transpose();
        Matrix4d matResult = characteristicMatrix.mul(pointMat, new Matrix4d());
        Vector4d rawResult = timeVec.mul(matResult.transpose());
        Vector4d normalizedResult = rawResult;
        if(rawResult.w != 0 && Double.isFinite(rawResult.w)){
            normalizedResult = rawResult.div(rawResult.w);
        }
        
        return new Vector3d(normalizedResult.x,normalizedResult.y,normalizedResult.z);
    }

    /**
     * Gets the list of control points contained in the spline
     * @return The list of control points contained in the spline
     */
    public List<Vector3d> getPoints(){
        return this.pointTree.values().stream().map((vec) -> new Vector3d(vec.x,vec.y,vec.z)).collect(Collectors.toList());
    }

    /**
     * Adds a control point to the spline
     * @param controlPoint The control point to add
     */
    public void addPoint(Vector3d controlPoint){
        for(Vector3d existingPoint : this.getPoints()){
            if(existingPoint.equals(controlPoint)){
                throw new IllegalArgumentException("Tried adding existing control point to spline!");
            }
        }
        this.pointTree.put((double)pointTree.size(),new Vector4d(controlPoint,1.0));
    }

    /**
     * Checks if the spline contains a given control point
     * @param controlPoint The control point to check for
     * @return true if the point is contained, false otherwise
     */
    public boolean containsPoint(Vector3d controlPoint){
        for(Vector3d existingPoint : this.getPoints()){
            if(existingPoint.equals(controlPoint)){
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a control point from the spline
     * @param controlPoint The control point to remove
     */
    public void removePoint(Vector3d controlPoint){
        List<Vector3d> pointsToSet = this.pointTree.values().stream().map(v -> new Vector3d(v.x,v.y,v.z)).filter(v -> !v.equals(controlPoint)).collect(Collectors.toList());
        this.pointTree.clear();
        int i = 0;
        for(Vector3d point : pointsToSet){
            this.pointTree.put((double)i,new Vector4d(point,1.0));
            i++;
        }
    }


    /**
     * Sets the characteristic matrix of the spline
     * @param type The type of characteristic to use
     */
    private void setCharacteristicMatrix(Spline3dType type){
        switch(type){
            case CATMULL_ROM: {
                setAsCatmullRom();
            } break;
            case BEZIER: {
                setAsBezier();
            } break;
            case B: {
                setAsBasisSpline();
            } break;
        }
    }

    /**
     * Sets this spline to use the catmull-rom characteristic matrix
     */
    private void setAsCatmullRom(){
        this.characteristicMatrix = new Matrix4d(
               0,        1,    0,    0,
            -0.5,        0,  0.5,    0,
               1, -5.0/2.0,    2, -0.5,
            -0.5,      1.5, -1.5,  0.5
        ).transpose();
    }

    /**
     * Sets this spline to use the bezier characteristic matrix
     */
    private void setAsBezier(){
        this.characteristicMatrix = new Matrix4d(
             1,  0,  0,  0,
            -3,  3,  0,  0,
             3, -6,  3,  0,
            -1,  3, -3,  1
        ).transpose();
    }

    /**
     * Sets this spline to use the b-spline (basis spline) characteristic matrix
     */
    private void setAsBasisSpline(){
        this.characteristicMatrix = new Matrix4d(
             1,  4,  1,  0,
            -3,  0,  3,  0,
             3, -6,  3,  0,
            -1,  3, -3,  1
        ).transpose();
    }

}
