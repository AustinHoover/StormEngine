package electrosphere.data.utils;

import java.util.Arrays;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Converts data structures between formats saved to disk vs formats used in engine
 */
public class DataFormatUtil {
    
    /**
     * Gets the rotation in quaterniond form
     * @param values The list of raw float values
     * @return The quaterniond containing those values or an identity quaterniond if no such values exist
     */
    public static Quaterniond getDoubleListAsQuaternion(List<Double> values){
        if(values == null){
            return new Quaterniond();
        }
        if(values.size() > 0){
            return new Quaterniond(values.get(0),values.get(1),values.get(2),values.get(3));
        } else {
            return new Quaterniond();
        }
    }

    /**
     * Gets a quaterniond as a list of doubles
     * @param quat The quaternion
     * @return The list of doubles
     */
    public static List<Double> getQuatAsDoubleList(Quaterniond quat){
        return Arrays.asList((Double)quat.x,(Double)quat.y,(Double)quat.z,(Double)quat.w);
    }

    /**
     * Gets the vector in vector3d form
     * @param values The list of raw float values
     * @return The vector containing those values or an identity vector if no such values exist
     */
    public static Vector3d getDoubleListAsVector(List<Double> values){
        if(values == null){
            return new Vector3d();
        }
        if(values.size() > 0){
            return new Vector3d(values.get(0),values.get(1),values.get(2));
        } else {
            return new Vector3d();
        }
    }

    /**
     * Gets the vector in vector3d form
     * @param values The list of raw float values
     * @param tempVec The vec to set the values to
     * @return The vector containing those values or an identity vector if no such values exist
     */
    public static Vector3d getDoubleListAsVector(List<Double> values, Vector3d tempVec){
        if(values == null){
            return tempVec;
        }
        if(values.size() > 0){
            return tempVec.set(values.get(0),values.get(1),values.get(2));
        } else {
            return tempVec;
        }
    }

    /**
     * Gets a vector as a list of doubles
     * @param vec The vector
     * @return The list of doubles
     */
    public static List<Double> getVectorAsDoubleList(Vector3d vec){
        return Arrays.asList((Double)vec.x,(Double)vec.y,(Double)vec.z);
    }

}
