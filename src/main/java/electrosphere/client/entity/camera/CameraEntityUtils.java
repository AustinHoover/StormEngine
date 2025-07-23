package electrosphere.client.entity.camera;

import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.mem.JomlPool;
import electrosphere.util.math.SpatialMathUtils;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector4d;

/**
 * Camera entity utility functions
 */
public class CameraEntityUtils {
    
    
    public static Entity spawnBasicCameraEntity(Vector3d center, Vector3d eye){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_TYPE, EntityDataStrings.DATA_STRING_CAMERA_TYPE_BASIC);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_CENTER, center);
        rVal.putData(EntityDataStrings .DATA_STRING_CAMERA_EYE, eye);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_ORBIT_DISTANCE, 2.0f);
        rVal.putData(EntityDataStrings.CAMERA_ORBIT_RADIAL_OFFSET, new Vector3d(0,1,0));
        rVal.putData(EntityDataStrings.CAMERA_PITCH, 0.0f);
        rVal.putData(EntityDataStrings.CAMERA_YAW, 0.0f);
        return rVal;
    }
    
    public static Entity spawnEntityTrackingCameraEntity(Vector3d center, Vector3d eye, Entity toTrack){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_TYPE, EntityDataStrings.DATA_STRING_CAMERA_TYPE_ORBIT);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_CENTER, center);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_EYE, eye);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_ORBIT_DISTANCE, 2.0f);
        rVal.putData(EntityDataStrings.CAMERA_ORBIT_RADIAL_OFFSET, new Vector3d(0,1,0));
        rVal.putData(EntityDataStrings.CAMERA_PITCH, 0.0f);
        rVal.putData(EntityDataStrings.CAMERA_YAW, 0.0f);
        BehaviorTree entityTrackingTree = new BehaviorTree() {
            @Override
            public void simulate(float deltaTime) {
                if(toTrack != null){
                    Vector3d entityPos = EntityUtils.getPosition(toTrack);
                    CameraEntityUtils.setCameraCenter(rVal, new Vector3d(entityPos).add(getOrbitalCameraRadialOffset(rVal)));
                }
            }
        };
        Globals.clientState.clientScene.registerBehaviorTree(entityTrackingTree);
        return rVal;
    }


    /**
     * <p>
     * Spawns a camera that tracks the player entity.
     * </p>
     * <p>
     * This uses more intelligent logic to automatically set offset based on the type of entity the player is.
     * </p>
     * @return The camera entity
     */
    public static Entity spawnPlayerEntityTrackingCameraEntity(){
        Vector3d center = new Vector3d(0,0,0);
        Vector3d eye = SpatialMathUtils.getOriginVector();
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_TYPE, EntityDataStrings.DATA_STRING_CAMERA_TYPE_ORBIT);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_CENTER, center);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_EYE, eye);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_ORBIT_DISTANCE, 2.0f);
        rVal.putData(EntityDataStrings.CAMERA_ORBIT_RADIAL_OFFSET, new Vector3d(0,1,0));
        rVal.putData(EntityDataStrings.CAMERA_PITCH, 0.0f);
        rVal.putData(EntityDataStrings.CAMERA_YAW, 0.0f);
        Globals.cameraHandler.setTrackPlayerEntity(true);
        //if camera data is defined, use the camera data third person offset instead of the default offset
        if(Globals.clientState.playerEntity != null && CommonEntityUtils.getCommonData(Globals.clientState.playerEntity) != null){
            CommonEntityType type = CommonEntityUtils.getCommonData(Globals.clientState.playerEntity);
            if(type.getCameraData() != null && type.getCameraData().getThirdPersonCameraOffset() != null){
                rVal.putData(EntityDataStrings.CAMERA_ORBIT_RADIAL_OFFSET, new Vector3d(
                    (float)type.getCameraData().getThirdPersonCameraOffset().x,
                    (float)type.getCameraData().getThirdPersonCameraOffset().y,
                    (float)type.getCameraData().getThirdPersonCameraOffset().z
                ));
            }
        }
        return rVal;
    }

    /**
     * Spawns a first person camera that tracks the player
     * @param center the center of the camera
     * @param eye the eye of the camera
     * @return the camera
     */
    public static Entity spawnPlayerEntityTrackingCameraFirstPersonEntity(Vector3d center, Vector3d eye){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_TYPE, EntityDataStrings.DATA_STRING_CAMERA_TYPE_ORBIT);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_CENTER, center);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_EYE, eye);
        rVal.putData(EntityDataStrings.DATA_STRING_CAMERA_ORBIT_DISTANCE, 0.01f);
        rVal.putData(EntityDataStrings.CAMERA_ORBIT_RADIAL_OFFSET, new Vector3d(0,1.3f,0));
        rVal.putData(EntityDataStrings.CAMERA_PITCH, 0.0f);
        rVal.putData(EntityDataStrings.CAMERA_YAW, 0.0f);
        Globals.cameraHandler.setTrackPlayerEntity(true);
        return rVal;
    }

    /**
     * Initializes the camera
     */
    public static void initCamera(){
        //destroy if it already exists
        if(Globals.clientState.playerCamera != null){
            Globals.clientState.clientSceneWrapper.getScene().deregisterEntity(Globals.clientState.playerCamera);
        }
        //create
        if(Globals.controlHandler.cameraIsThirdPerson()){
            Globals.clientState.playerCamera = CameraEntityUtils.spawnPlayerEntityTrackingCameraEntity();
        } else {
            Globals.clientState.playerCamera = CameraEntityUtils.spawnPlayerEntityTrackingCameraFirstPersonEntity(new Vector3d(0,0,0), SpatialMathUtils.getOriginVector());
        }
    }
    
    public static Entity getOrbitalCameraTarget(Entity camera){
        return (Entity)camera.getData(EntityDataStrings.DATA_STRING_CAMERA_ORBIT_TARGET);
    }
    
    public static float getOrbitalCameraDistance(Entity camera){
        return (float)camera.getData(EntityDataStrings.DATA_STRING_CAMERA_ORBIT_DISTANCE);
    }

    public static void setOrbitalCameraDistance(Entity camera, float distance){
        camera.putData(EntityDataStrings.DATA_STRING_CAMERA_ORBIT_DISTANCE, distance);
    }

    public static Vector3d getOrbitalCameraRadialOffset(Entity camera){
        return (Vector3d)camera.getData(EntityDataStrings.CAMERA_ORBIT_RADIAL_OFFSET);
    }

    public static void setOrbitalCameraRadialOffset(Entity camera, Vector3d offset){
        camera.putData(EntityDataStrings.CAMERA_ORBIT_RADIAL_OFFSET, offset);
    }
    
    public static void setCameraCenter(Entity camera, Vector3d center){
        camera.putData(EntityDataStrings.DATA_STRING_CAMERA_CENTER, center);
    }
    
    public static Vector3d getCameraCenter(Entity camera){
        if(camera == null){
            return null;
        }
        return (Vector3d)camera.getData(EntityDataStrings.DATA_STRING_CAMERA_CENTER);
    }
    
    public static void setCameraEye(Entity camera, Vector3d eye){
        camera.putData(EntityDataStrings.DATA_STRING_CAMERA_EYE, eye);
    }
    
    public static Vector3d getCameraEye(Entity camera){
        if(camera == null){
            return null;
        }
        return (Vector3d)camera.getData(EntityDataStrings.DATA_STRING_CAMERA_EYE);
    }
    
    public static void setCameraPitch(Entity camera, float pitch){
        camera.putData(EntityDataStrings.CAMERA_PITCH, pitch);
    }
    
    public static float getCameraPitch(Entity camera){
        return (float)camera.getData(EntityDataStrings.CAMERA_PITCH);
    }
    
    public static void setCameraYaw(Entity camera, float yaw){
        camera.putData(EntityDataStrings.CAMERA_YAW, yaw);
    }
    
    public static float getCameraYaw(Entity camera){
        if(camera == null){
            return 0f;
        }
        return (float)camera.getData(EntityDataStrings.CAMERA_YAW);
    }

    /**
     * Gets the quaternion containing the camera pitch
     * @param camera The pitch of the camera
     * @return The quaternion
     */
    public static Quaternionf getPitchQuat(double pitch){
        Quaternionf pitchQuat = new Quaternionf().fromAxisAngleDeg(SpatialMathUtils.getLeftVectorf(), -(float)pitch);
        return pitchQuat;
    }

    /**
     * Gets the quaternion containing the camera yaw
     * @param yaw The yaw of the camera
     * @return The quaternion
     */
    public static Quaternionf getYawQuat(double yaw){
        Quaternionf yawQuat = new Quaternionf().fromAxisAngleDeg(SpatialMathUtils.getUpVectorf(), -(float)yaw);
        return yawQuat;
    }
    
    public static void destroyCameraEntity(Entity e){
        if(e != null){
            Globals.clientState.clientScene.deregisterEntity(e);
        }
    }
    
    /**
     * Gets the camera view matrix for a given camera entity
     * @param camera The camera
     * @return The view matrix for the camera
     */
    public static Matrix4d getCameraViewMatrix(Entity camera){
        //alloc
        Vector3d cameraCenter = JomlPool.getD();
        Vector3d cameraEye = JomlPool.getD();
        Vector3d cameraUp = JomlPool.getD();

        //perform math
        cameraCenter.set(0,0,0);
        cameraEye.set(cameraCenter).add(getCameraEye(camera));
        SpatialMathUtils.makeUpVector(cameraUp);
        //!!before you make the same mistake I made, cameraEye is NOT NECESSARILY normalized/unit vector
        //the orbital distance and offset are included in this vector
        Matrix4d rVal = new Matrix4d().setLookAt(
                    cameraEye, //eye
                    cameraCenter, //center
                    cameraUp // up
            ).scale(1.0f, 1.0f, 1.0f);

        //free
        JomlPool.release(cameraCenter);
        JomlPool.release(cameraEye);
        JomlPool.release(cameraUp);
        return rVal;
    }

    /**
     * Gets the near clip of the camera
     * @param camera The camera
     * @return The near clip
     */
    public static float getNearClip(Entity camera){
        return Globals.renderingEngine.getNearClip();
    }

    /**
     * Gets the far clip of the camera
     * @param camera The camera
     * @return The far clip
     */
    public static float getFarClip(Entity camera){
        return Globals.gameConfigCurrent.getSettings().getGraphicsViewDistance();
    }

    /**
     * Gets the rotation quaternion from a camera entity
     * @param entity The entity
     * @return The rotation quaternion
     */
    public static Quaterniond getRotationQuat(Entity entity){
        double yaw = CameraEntityUtils.getCameraYaw(entity);
        double pitch = CameraEntityUtils.getCameraPitch(entity);
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaternionf quatRaw = CameraEntityUtils.getYawQuat(yaw).mul(CameraEntityUtils.getPitchQuat(pitch)).mul(new Quaternionf().rotateY((float)Math.PI));
        Quaterniond quatd = new Quaterniond(quatRaw).normalize();
        return quatd;
    }

    /**
     * Gets the rotation quaternion from a yaw and pitch
     * @param yaw The yaw
     * @param pitch The pitch
     * @return The rotation quaternion
     */
    public static Quaterniond getRotationQuat(double yaw, double pitch){
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaternionf quatRaw = CameraEntityUtils.getYawQuat(yaw).mul(CameraEntityUtils.getPitchQuat(pitch)).mul(new Quaternionf().rotateY((float)Math.PI));
        Quaterniond quatd = new Quaterniond(quatRaw).normalize();
        return quatd;
    }

    /**
     * Gets the entity rotation for the upright-aligned entity with a given yaw
     * @param yaw The yaw of the camera
     * @return The upright-aligned entity rotation
     */
    public static Quaterniond getUprightQuat(double yaw){
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaternionf quatRaw = CameraEntityUtils.getYawQuat(yaw).mul(new Quaternionf().rotateY((float)Math.PI));
        Quaterniond quatd = new Quaterniond(quatRaw).normalize();
        return quatd;
    }

    /**
     * Gets the rotation matrix from a yaw and pitch
     * @param yaw The yaw
     * @param pitch The pitch
     * @return The rotation matrix
     */
    public static Matrix4d getRotationMat(double yaw, double pitch){
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaternionf quatRaw = CameraEntityUtils.getYawQuat(yaw).mul(CameraEntityUtils.getPitchQuat(pitch)).mul(new Quaternionf().rotateY((float)Math.PI));
        Quaterniond quatd = new Quaterniond(quatRaw).normalize();
        Matrix4d rotationMat = new Matrix4d().rotate(quatd);
        return rotationMat;
    }

    /**
     * Gets the facing vector from the camera angles
     * @param cameraEntity The camera entity
     * @return The facing vector
     */
    public static Vector3d getFacingVec(Entity cameraEntity){
        float yaw = CameraEntityUtils.getCameraYaw(cameraEntity);
        float pitch = CameraEntityUtils.getCameraPitch(cameraEntity);
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaternionf quatRaw = CameraEntityUtils.getYawQuat(yaw).mul(CameraEntityUtils.getPitchQuat(pitch)).mul(new Quaternionf().rotateY((float)Math.PI));
        Quaterniond quatd = new Quaterniond(quatRaw).normalize();
        Matrix4d rotationMat = new Matrix4d().rotate(quatd);
        Vector4d rotationVecRaw = SpatialMathUtils.getOriginVector4();
        rotationVecRaw = rotationMat.transform(rotationVecRaw);
        return new Vector3d(rotationVecRaw.x,0,rotationVecRaw.z);
    }

    /**
     * Gets the facing vector from the camera angles
     * @param yaw The yaw of the camera
     * @param pitch The pitch of the camera
     * @return The facing vector
     */
    public static Vector3d getFacingVec(double yaw, double pitch){
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaternionf quatRaw = CameraEntityUtils.getYawQuat(yaw).mul(CameraEntityUtils.getPitchQuat(pitch)).mul(new Quaternionf().rotateY((float)Math.PI));
        Quaterniond quatd = new Quaterniond(quatRaw).normalize();
        Matrix4d rotationMat = new Matrix4d().rotate(quatd);
        Vector4d rotationVecRaw = SpatialMathUtils.getOriginVector4();
        rotationVecRaw = rotationMat.transform(rotationVecRaw);
        return new Vector3d(rotationVecRaw.x,0,rotationVecRaw.z);
    }

    /**
     * Gets the facing vector from the quaternion
     * @param rotation The rotation to construct the facing vector with
     * @return The facing vector
     */
    public static Vector3d getFacingVec(Quaterniond rotation){
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaterniond quatd = new Quaterniond(rotation).normalize();
        Matrix4d rotationMat = new Matrix4d().rotate(quatd);
        Vector4d rotationVecRaw = SpatialMathUtils.getOriginVector4();
        rotationVecRaw = rotationMat.transform(rotationVecRaw);
        if(rotationVecRaw.length() < 0.001){
            rotationVecRaw.set(SpatialMathUtils.getOriginVector4());
        }
        return new Vector3d(rotationVecRaw.x,0,rotationVecRaw.z);
    }

    /**
     * Gets the rotation matrix from a yaw and pitch
     * @param yaw The yaw
     * @param pitch The pitch
     * @return The rotation matrix
     */
    public static Matrix4d getRotationMat(Entity entity){
        float yaw = CameraEntityUtils.getCameraYaw(entity);
        float pitch = CameraEntityUtils.getCameraPitch(entity);
        //quaternion is multiplied by pi because we want to point away from the eye of the camera, NOT towards it
        Quaternionf quatRaw = CameraEntityUtils.getYawQuat(yaw).mul(CameraEntityUtils.getPitchQuat(pitch)).mul(new Quaternionf().rotateY((float)Math.PI));
        Quaterniond quatd = new Quaterniond(quatRaw).normalize();
        Matrix4d rotationMat = new Matrix4d().rotate(quatd);
        return rotationMat;
    }
    
}
