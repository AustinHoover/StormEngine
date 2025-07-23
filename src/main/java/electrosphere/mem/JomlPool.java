package electrosphere.mem;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * A pool for temporary joml objects
 */
public class JomlPool {
    
    /**
     * Structure to store not-in-use objects
     */
    static List<Vector3f> vec3fPool = new LinkedList<Vector3f>();

    /**
     * Structure to store not-in-use objects
     */
    static List<Vector3d> vec3dPool = new LinkedList<Vector3d>();

    /**
     * Structure to store not-in-use objects
     */
    static List<Matrix4d> mat4dPool = new LinkedList<Matrix4d>();

    /**
     * Lock for thread-safeing operations
     */
    static ReentrantLock lock = new ReentrantLock();

    /**
     * Gets a Vector3f from the pool. Allocates if no free one is available.
     * @param type The type of the message
     * @return A Vector3f
     */
    public static Vector3f getF(){
        Vector3f rVal = null;
        lock.lock();
        if(vec3fPool.size() > 0){
            rVal = vec3fPool.remove(0);
        } else {
            rVal = new Vector3f();
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a Vector3f back into the pool
     * @param data The object to release
     */
    public static void release(Vector3f data){
        data.x = 0;
        data.y = 0;
        data.z = 0;
        lock.lock();
        if(JomlPool.vec3fPool.size() < 1000){
            JomlPool.vec3fPool.add(data);
        }
        lock.unlock();
    }

    /**
     * Gets a Vector3d from the pool. Allocates if no free one is available.
     * @return A Vector3d
     */
    public static Vector3d getD(){
        Vector3d rVal = null;
        lock.lock();
        if(vec3dPool.size() > 0){
            rVal = vec3dPool.remove(0);
        } else {
            rVal = new Vector3d();
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Gets a Matrix4d from the pool. Allocates if no free one is available.
     * @return A Matrix4d
     */
    public static Matrix4d getMat(){
        Matrix4d rVal = null;
        lock.lock();
        if(mat4dPool.size() > 0){
            rVal = mat4dPool.remove(0);
        } else {
            rVal = new Matrix4d();
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a Vector3d back into the pool
     * @param data The object to release
     */
    public static void release(Vector3d data){
        data.x = 0;
        data.y = 0;
        data.z = 0;
        lock.lock();
        if(JomlPool.vec3dPool.size() < 1000){
            JomlPool.vec3dPool.add(data);
        }
        lock.unlock();
    }

    /**
     * Releases a Matrix4d back into the pool
     * @param data The object to release
     */
    public static void release(Matrix4d data){
        data.identity();
        lock.lock();
        if(JomlPool.mat4dPool.size() < 1000){
            JomlPool.mat4dPool.add(data);
        }
        lock.unlock();
    }

}
