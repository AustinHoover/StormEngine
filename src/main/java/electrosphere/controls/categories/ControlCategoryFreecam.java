package electrosphere.controls.categories;

import java.util.HashMap;
import java.util.List;

import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.controls.Control;
import electrosphere.controls.Control.ControlMethod;
import electrosphere.controls.Control.ControlType;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.MouseState;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.events.MouseEvent;

public class ControlCategoryFreecam {

    public static final String FREECAM_UP = "freecamUp";
    public static final String FREECAM_DOWN = "freecamDown";
    public static final String FREECAM_FORWARD = "freecamForward";
    public static final String FREECAM_BACKWARD = "freecamBackward";
    public static final String FREECAM_LEFT = "freecamLeft";
    public static final String FREECAM_RIGHT = "freecamRight";
    public static final String FREECAM_MOUSE = "freecamMouse";
    
    /**
     * Maps the controls
     * @param handler
     */
    public static void mapControls(ControlHandler handler){
        handler.addControl(FREECAM_UP,       new Control(ControlType.KEY,GLFW.GLFW_KEY_SPACE,false,"Up","Moves the camera up"));
        handler.addControl(FREECAM_DOWN,     new Control(ControlType.KEY,GLFW.GLFW_KEY_LEFT_CONTROL,false,"Down","Moves the camera down"));
        handler.addControl(FREECAM_FORWARD,  new Control(ControlType.KEY,GLFW.GLFW_KEY_W,false,"Forward","Moves the camera forward"));
        handler.addControl(FREECAM_BACKWARD, new Control(ControlType.KEY,GLFW.GLFW_KEY_S,false,"Backward","Moves the camera backward"));
        handler.addControl(FREECAM_LEFT,     new Control(ControlType.KEY,GLFW.GLFW_KEY_A,false,"Left","Moves the camera left"));
        handler.addControl(FREECAM_RIGHT,    new Control(ControlType.KEY,GLFW.GLFW_KEY_D,false,"Right","Moves the camera right"));
        handler.addControl(FREECAM_MOUSE,    new Control(ControlType.MOUSE_MOVEMENT,0,false,"",""));
    }
    
    /**
     * Populates the in-game debug controls list
     * @param controlMap
     */
    public static void setCallbacks(
        HashMap<String, Control> controlMap,
        List<Control> freeCameraControlList
    ){
        freeCameraControlList.add(controlMap.get(FREECAM_UP));
        controlMap.get(FREECAM_UP).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            Vector3d playerCameraCenterPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
            playerCameraCenterPos.add(0,0.1f,0);
            CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera,playerCameraCenterPos);
        }});


        freeCameraControlList.add(controlMap.get(FREECAM_DOWN));
        controlMap.get(FREECAM_DOWN).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            Vector3d playerCameraCenterPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
            playerCameraCenterPos.add(0,-0.1f,0);
            CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera,playerCameraCenterPos);
        }});


        freeCameraControlList.add(controlMap.get(FREECAM_FORWARD));
        ControlMethod freeCamForwardCallback = new ControlMethod(){public void execute(MouseState mouseState){
            Vector3d playerCameraCenterPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
            Vector3d playerCameraEyePos = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
            playerCameraCenterPos.add(new Vector3d(playerCameraEyePos).normalize().mul(-0.1f));
            CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera,playerCameraCenterPos);
        }};
        controlMap.get(FREECAM_FORWARD).setOnClick(freeCamForwardCallback);
        controlMap.get(FREECAM_FORWARD).setOnRepeat(freeCamForwardCallback);

        freeCameraControlList.add(controlMap.get(FREECAM_BACKWARD));
        ControlMethod freeCamBackwardCallback = new ControlMethod(){public void execute(MouseState mouseState){
            Vector3d playerCameraCenterPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
            Vector3d playerCameraEyePos = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
            playerCameraCenterPos.add(new Vector3d(playerCameraEyePos).normalize().mul(0.1f));
            CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera,playerCameraCenterPos);
        }};
        controlMap.get(FREECAM_BACKWARD).setOnClick(freeCamBackwardCallback);
        controlMap.get(FREECAM_BACKWARD).setOnRepeat(freeCamBackwardCallback);

        freeCameraControlList.add(controlMap.get(FREECAM_LEFT));
        ControlMethod freeCamLeftCallback = new ControlMethod(){public void execute(MouseState mouseState){
            Vector3d playerCameraCenterPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
            Vector3d playerCameraEyePos = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
            Vector3d modifiedVec = new Vector3d(playerCameraEyePos.x,0,playerCameraEyePos.z).rotateY((float)(-90 * Math.PI / 180)).normalize();
            playerCameraCenterPos.add(new Vector3d(modifiedVec).mul(0.1f));
            CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera,playerCameraCenterPos);
        }};
        controlMap.get(FREECAM_LEFT).setOnClick(freeCamLeftCallback);
        controlMap.get(FREECAM_LEFT).setOnRepeat(freeCamLeftCallback);

        freeCameraControlList.add(controlMap.get(FREECAM_RIGHT));
        ControlMethod freeCamRightCallback = new ControlMethod(){public void execute(MouseState mouseState){
            Vector3d playerCameraCenterPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
            Vector3d playerCameraEyePos = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
            Vector3d modifiedVec = new Vector3d(playerCameraEyePos.x,0,playerCameraEyePos.z).rotateY((float)(90 * Math.PI / 180)).normalize();
            playerCameraCenterPos.add(new Vector3d(modifiedVec).mul(0.1f));
            CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera,playerCameraCenterPos);
        }};
        controlMap.get(FREECAM_RIGHT).setOnClick(freeCamRightCallback);
        controlMap.get(FREECAM_RIGHT).setOnRepeat(freeCamRightCallback);

        freeCameraControlList.add(controlMap.get(FREECAM_MOUSE));
        controlMap.get(FREECAM_MOUSE).setOnMove(new Control.MouseCallback(){public void execute(MouseState mouseState, MouseEvent event){
            Globals.cameraHandler.handleMouseEvent(event);
        }});

        freeCameraControlList.add(controlMap.get(ControlCategoryMainGame.DATA_STRING_INPUT_CODE_IN_GAME_MAIN_MENU));
    }

}
