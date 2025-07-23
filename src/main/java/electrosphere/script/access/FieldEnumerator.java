package electrosphere.script.access;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3d;

/**
 * Enumerates the fields to allow access to
 */
public class FieldEnumerator {
    
    /**
     * Gets the list of fields to allow to script context
     * @return The list of fields
     */
    public static List<Field> getFields(){
        List<Field> rVal = new LinkedList<Field>();
        try {
            rVal.add(Vector3d.class.getField("x"));
            rVal.add(Vector3d.class.getField("y"));
            rVal.add(Vector3d.class.getField("z"));
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        return rVal;
    }

    /**
     * Gets the list of methods to allow to script context
     * @return The list of methods
     */
    public static List<Method> getMethods(){
        List<Method> rVal = new LinkedList<Method>();
        try {
            rVal.add(Vector3d.class.getMethod("x"));
            rVal.add(Vector3d.class.getMethod("y"));
            rVal.add(Vector3d.class.getMethod("z"));
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return rVal;
    }

}
