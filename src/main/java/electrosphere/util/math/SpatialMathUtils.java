package electrosphere.util.math;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;

import electrosphere.logger.LoggerInterface;

/**
 * Utility functions for doing math
 */
public class SpatialMathUtils {
    

    /**
     * Gets the origin vector of the engine
     * @return The origin vector
     */
    public static Vector3d getOriginVector(){
        return new Vector3d(1,0,0);
    }

    /**
     * Gets the origin vector of the engine
     * @return The origin vector
     */
    public static Vector4d getOriginVector4(){
        return new Vector4d(1,0,0,1);
    }

    /**
     * Gets the origin vector of the engine, in Vector3f format
     * @return The origin vector
     */
    public static Vector3f getOriginVectorf(){
        return new Vector3f(1,0,0);
    }

    /**
     * Gets the origin vector of the engine
     * @return The origin vector
     */
    public static Vector3d getUpVector(){
        return new Vector3d(0,1,0);
    }

    /**
     * Sets the target vector to point up
     * @param target The target vector
     */
    public static void makeUpVector(Vector3d target){
        target.set(0,1,0);
    }

    /**
     * Gets the origin vector of the engine
     * @return The origin vector
     */
    public static Vector4d getUpVector4(){
        return new Vector4d(0,1,0,1);
    }

    /**
     * Gets the origin vector of the engine, in Vector3f format
     * @return The origin vector
     */
    public static Vector3f getUpVectorf(){
        return new Vector3f(0,1,0);
    }

    /**
     * Gets the origin vector of the engine
     * @return The origin vector
     */
    public static Vector3d getLeftVector(){
        return new Vector3d(0,0,1);
    }

    /**
     * Gets the origin vector of the engine
     * @return The origin vector
     */
    public static Vector4d getLeftVector4(){
        return new Vector4d(0,0,1,1);
    }

    /**
     * Gets the origin vector of the engine, in Vector3f format
     * @return The origin vector
     */
    public static Vector3f getLeftVectorf(){
        return new Vector3f(0,0,1);
    }

    /**
     * Gets the up rotation
     * @return The up rotation
     */
    public static Quaterniond getUpRotation(){
        return SpatialMathUtils.calculateRotationFromPointToPoint(SpatialMathUtils.getOriginVector(), SpatialMathUtils.getUpVector());
    }


    /**
     * Calculates the quaternion that rotates the origin vector to point from origin to destination
     * @param originPoint The point to begin at
     * @param destinationPoint The point end at
     * @return The quaternion
     */
    public static Quaterniond calculateRotationFromPointToPoint(Vector3d originPoint, Vector3d destinationPoint){
        if(originPoint == destinationPoint || originPoint.distance(destinationPoint) == 0.0){
            String message = "Trying to find rotation between same point!";
            LoggerInterface.loggerEngine.ERROR(new IllegalStateException(message));
            return new Quaterniond();
        }
        Quaterniond rVal = getOriginVector().rotationTo(new Vector3d(destinationPoint).sub(originPoint).normalize(), new Quaterniond());
        if(!Double.isFinite(rVal.w) || !Double.isFinite(rVal.x) || !Double.isFinite(rVal.y) || !Double.isFinite(rVal.z)){
            String message = "Rotation is NaN!\n" + 
            "originPoint: " + originPoint + "\n" +
            "destinationPoint: " + destinationPoint
            ;
            LoggerInterface.loggerEngine.ERROR(new IllegalStateException(message));
            rVal = new Quaterniond();
        }
        return rVal;
    }


}
