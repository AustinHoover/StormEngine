package electrosphere.collision;

import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DQuaternionC;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.collidable.CollidableTemplate;

/**
 * Utilities for leveraging the collision system to perform physics
 */
public class PhysicsUtils {
    
    /**
     * Gets the rotation of an ode body as a joml quaternion
     * @param body
     * @return
     */
    public static Quaterniond getRigidBodyRotation(DBody body){
        return odeQuatToJomlQuat(body.getQuaternion());
    }

    /**
     * Gets the rotation of the geom as a joml quaternion
     * @param geom The geom
     * @return The rotation
     */
    public static Quaterniond getGeomRotation(DGeom geom){
        return odeQuatToJomlQuat(geom.getQuaternion());
    }
    
    /**
     * Converts an Ode vector to a joml vector
     * @param vector Ode vector
     * @return joml vector
     */
    public static Vector3d odeVecToJomlVec(org.ode4j.math.DVector3C vector){
        return new Vector3d(vector.get0(),vector.get1(),vector.get2());
    }

    /**
     * Converts a joml vector to an Ode vector
     * @param vector joml vector
     * @return Ode vector
     */
    public static org.ode4j.math.DVector3C jomlVecToOdeVec(Vector3d vector){
        return new org.ode4j.math.DVector3(vector.x,vector.y,vector.z);
    }
    
    /**
     * Converts an Ode quaternion to a joml quaternion
     * @param quaternion Ode quat
     * @return joml quat
     */
    public static Quaterniond odeQuatToJomlQuat(DQuaternionC quaternion){
        return new Quaterniond(quaternion.get1(), quaternion.get2(), quaternion.get3(), quaternion.get0());
    }

    /**
     * Converts a joml quat to an ode quat
     * @param quaternion The joml quat
     * @return The ode quata
     */
    public static DQuaternion jomlQuatToOdeQuat(Quaterniond quaternion){
        return new DQuaternion(quaternion.w, quaternion.x, quaternion.y, quaternion.z);
    }

    /**
     * Converts a joml quat to an ode quat
     * @param quaternion The joml quat
     * @return The ode quata
     */
    public static DMatrix3 jomlQuatToOdeMat(Quaterniond quaternion){
        Matrix3d rotMat = quaternion.get(new Matrix3d());
        DMatrix3 mat = new DMatrix3(
            rotMat.m00, rotMat.m10, rotMat.m20,
            rotMat.m01, rotMat.m11, rotMat.m21,
            rotMat.m02, rotMat.m12, rotMat.m22
        );
        return mat;
    }
    
    /**
     * Sets the position + rotation of a body
     * @param position The position
     * @param rotation The rotation
     * @param body The body
     */
    public static void setRigidBodyTransform(CollisionEngine collisionEngine, Vector3d position, Quaterniond rotation, DBody body){
        collisionEngine.setBodyTransform(body, position, rotation);
    }

    /**
     * Sets the transform of a geom
     * @param collisionEngine The engine
     * @param position The position
     * @param rotation The rotation
     * @param geom The geom
     */
    public static void setGeomTransform(CollisionEngine collisionEngine, Vector3d position, Quaterniond rotation, DGeom geom){
        collisionEngine.setGeomTransform(geom, position, rotation);
    }

    /**
     * Synchronizes the data on a body
     * @param body The body
     * @param position The position
     * @param rotation The rotation
     * @param linearVel The linear velocity
     * @param angularVel The angular velocity
     * @param linearForce The linear force
     * @param angularForce The angular force
     * @param enabled Whether the body is enabled or not -- true to enable, false to disable
     */
    public static void synchronizeData(CollisionEngine collisionEngine, DBody body, Vector3d position, Quaterniond rotation, Vector3d linearVel, Vector3d angularVel, Vector3d linearForce, Vector3d angularForce, boolean enabled){
        collisionEngine.synchronizeData(body, position, rotation, linearVel, angularVel, linearForce, angularForce, enabled);
    }

    /**
     * Synchronizes the data on a body
     * @param body The body
     * @param position The position
     * @param rotation The rotation
     * @param linearVel The linear velocity
     * @param angularVel The angular velocity
     * @param linearForce The linear force
     * @param angularForce The angular force
     */
    public static void synchronizeData(CollisionEngine collisionEngine, DBody body, Vector3d position, Quaterniond rotation, Vector3d linearVel, Vector3d angularVel, Vector3d linearForce, Vector3d angularForce){
        collisionEngine.synchronizeData(body, position, rotation, linearVel, angularVel, linearForce, angularForce);
    }


    /**
     * Sets the position + rotation + scale of a body
     * @param position The position
     * @param rotation The rotation
     * @param scale The scale
     * @param body The body
     */
    public static void setRigidBodyTransform(CollisionEngine collisionEngine, CollidableTemplate template, Vector3d position, Quaterniond rotation, Vector3d scale, DBody body){
        collisionEngine.setBodyTransform(body, template, position, rotation, scale);
    }

    /**
     * Destroys a body
     * @param collisionEngine The collision engine
     * @param body The body
     */
    public static void destroyBody(CollisionEngine collisionEngine, DBody body){
        collisionEngine.destroyDBody(body);
    }

    /**
     * Destroys a body + collidable pair
     * @param body The body
     * @param collidable The collidable
     */
    public static void destroyPhysicsPair(CollisionEngine collisionEngine, DBody body, Collidable collidable){
        collisionEngine.destroyPhysicsPair(body, collidable);
    }

    /**
     * Disables a body
     * @param collisionEngine The collision engine
     * @param body The body
     */
    public static void disableBody(CollisionEngine collisionEngine, DBody body){
        collisionEngine.disable(body);
    }

    /**
     * Enables a body
     * @param collisionEngine The collision engine
     * @param body The body
     */
    public static void enableBody(CollisionEngine collisionEngine, DBody body){
        collisionEngine.enable(body);
    }

    /**
     * Checks if a body is enabled
     * @param collisionEngine The collision engine
     * @param body The body
     * @return true if the body is enabled, false otherwise
     */
    public static boolean isBodyEnabled(CollisionEngine collisionEngine, DBody body){
        return collisionEngine.isEnabled(body);
    }

    
}
