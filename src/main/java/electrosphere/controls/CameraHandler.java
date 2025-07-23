package electrosphere.controls;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.entity.crosshair.Crosshair;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityUtils;
import electrosphere.mem.JomlPool;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.renderer.ui.events.MouseEvent;
import electrosphere.util.math.SpatialMathUtils;

/**
 * Handler for camera-related events and controls
 */
public class CameraHandler {

    /**
     * The first person camera perspective
     */
    public static final int CAMERA_PERSPECTIVE_FIRST = 1;

    /**
     * The third person camera perspective
     */
    public static final int CAMERA_PERSPECTIVE_THIRD = 3;
    
    /**
     * the horizontal mouse sensitivity
     */
    float mouseSensitivityHorizontal = .1f;

    /**
     * the vertical mouse sensitivity
     */
    float mouseSensitivityVertical = .08f;

    /**
     * the speed of the freecam
     */
    float cameraSpeed;

    /**
     * the current yaw
     */
    float yaw = 150;

    /**
     * the current pitch
     */
    float pitch = 50;

    /**
     * the camera's rotation vector
     */
    Vector3d cameraRotationVector = new Vector3d();

    /**
     * the radial offset of the camera
     */
    Vector3d radialOffset = new Vector3d(0,1,0);

    /**
     * if set to true, the camera will track the player's entity
     */
    boolean trackPlayerEntity = true;

    /**
     * sets whether the camera handler should update the player's camera or not
     */
    boolean update = true;

    /**
     * Handles a mouse event
     * @param event The mouse event
     */
    public void handleMouseEvent(MouseEvent event){

        if(Globals.controlHandler != null && !Globals.controlHandler.isMouseVisible()){
            yaw = yaw + event.getDeltaX() * mouseSensitivityHorizontal;
            pitch = pitch + event.getDeltaY() * mouseSensitivityVertical;

            if (pitch >= 89.9f) {
                pitch = 89.9f;
            }
            if (pitch <= -89.9f) {
                pitch = -89.9f;
            }
        }

        this.updateGlobalCamera();
    }

    /**
     * Updates the radial offset
     * @param offset the radial offset
     */
    public void updateRadialOffset(Vector3d offset){
        radialOffset = offset;
    }

    /**
     * Updates the global camera
     */
    public void updateGlobalCamera(){
        Globals.profiler.beginCpuSample("updateGlobalCamera");
        if(update){
            if(Globals.clientState.playerCamera != null){
                cameraSpeed = 2.5f * (float)Globals.engineState.timekeeper.getMostRecentRawFrametime();

                if(Crosshair.getCrosshairActive()){
                    
                    Vector3d characterPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
                    Vector3d targetPos = Crosshair.getTargetPosition();
                    Vector3d diffed = JomlPool.getD();
                    diffed.set(targetPos).sub(characterPos).mul(-1).normalize();
                    cameraRotationVector.set((float)diffed.x, 0.5f, (float)diffed.z).normalize();
                    
                    yaw = (float)Math.toDegrees(Math.atan2(diffed.z, diffed.x));
                    
                    CameraEntityUtils.setCameraPitch(Globals.clientState.playerCamera, pitch);
                    CameraEntityUtils.setCameraYaw(Globals.clientState.playerCamera, yaw);
                    
                    JomlPool.release(diffed);
                } else {
                    CameraEntityUtils.setCameraPitch(Globals.clientState.playerCamera, pitch);
                    CameraEntityUtils.setCameraYaw(Globals.clientState.playerCamera, yaw);

                    Quaterniond pitchQuat = new Quaterniond().fromAxisAngleDeg(SpatialMathUtils.getLeftVector(), -pitch);
                    Quaterniond yawQuat = new Quaterniond().fromAxisAngleDeg(SpatialMathUtils.getUpVector(), -yaw);

                    cameraRotationVector = pitchQuat.transform(SpatialMathUtils.getOriginVector());
                    cameraRotationVector = yawQuat.transform(cameraRotationVector);
                    cameraRotationVector.normalize();
                }
                if(trackPlayerEntity && Globals.clientState.playerEntity != null){
                    //free previous vec
                    Vector3d oldCenter = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
                    Vector3d entityPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
                    Vector3d newCenter = JomlPool.getD();
                    newCenter.set(entityPos).add(CameraEntityUtils.getOrbitalCameraRadialOffset(Globals.clientState.playerCamera));
                    CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera, newCenter);
                    JomlPool.release(oldCenter);
                }
                //update view matrix offset
                float xFactor = (float)Math.cos(yaw / 180.0f * Math.PI);
                float yFactor = (float)Math.sin(yaw / 180.0f * Math.PI);

                //update offset
                Vector3d radialOffset = CameraEntityUtils.getOrbitalCameraRadialOffset(Globals.clientState.playerCamera);
                Vector3d oldOffset = CameraEntityUtils.getOrbitalCameraRadialOffset(Globals.clientState.playerCamera);
                Vector3d trueOffset = JomlPool.getD();
                trueOffset.set(radialOffset).mul(xFactor,1.0f,yFactor);
                CameraEntityUtils.setOrbitalCameraRadialOffset(Globals.clientState.playerCamera, trueOffset);
                JomlPool.release(oldOffset);

                //update rotation vec
                cameraRotationVector.mul(CameraEntityUtils.getOrbitalCameraDistance(Globals.clientState.playerCamera));
                CameraEntityUtils.setCameraEye(Globals.clientState.playerCamera, cameraRotationVector);

                //tell the server that we changed where we're looking, if we're in first person
                int perspectiveVal = CameraHandler.CAMERA_PERSPECTIVE_FIRST;
                if(Globals.controlHandler.cameraIsThirdPerson()){
                    perspectiveVal = CameraHandler.CAMERA_PERSPECTIVE_THIRD;
                }
                if(Globals.cameraHandler.getTrackPlayerEntity() && Globals.clientState.playerEntity != null){
                    Globals.clientState.clientConnection.queueOutgoingMessage(
                        EntityMessage.constructupdateEntityViewDirMessage(
                            Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId()),
                            Globals.engineState.timekeeper.getNumberOfSimFramesElapsed(),
                            perspectiveVal,
                            yaw,
                            pitch
                        )
                    );
                }

                //the view matrix
                Globals.renderingEngine.getViewMatrix().set(CameraEntityUtils.getCameraViewMatrix(Globals.clientState.playerCamera));

                //update the cursor on client side
                Globals.cursorState.updatePlayerCursor();
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Gets the yaw of the camera handler
     * @return the yaw
     */
    public float getYaw(){
        return yaw;
    }

    /**
     * Sets the yaw of the camera handler
     * @param yaw The yaw
     */
    public void setYaw(double yaw){
        this.yaw = (float)yaw;
    }

    /**
     * Gets the pitch of the camera handler
     * @return the pitch
     */
    public float getPitch(){
        return pitch;
    }

    /**
     * Sets the pitch of the camera handler
     * @param pitch The pitch
     */
    public void setPitch(double pitch){
        this.pitch = (float)pitch;
    }

    //set player tracking
    public void setTrackPlayerEntity(boolean track){
        trackPlayerEntity = track;
    }

    //get trackPlayerEntity
    public boolean getTrackPlayerEntity(){
        return trackPlayerEntity;
    }

    /**
     * Sets whether the camera should update with player input or not
     * @param update true to update with input, false otherwise
     */
    public void setUpdate(boolean update){
        this.update = update;
    }
    



}
