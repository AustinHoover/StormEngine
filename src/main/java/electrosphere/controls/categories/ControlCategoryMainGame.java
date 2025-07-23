package electrosphere.controls.categories;

import java.util.HashMap;
import java.util.List;

import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.entity.crosshair.Crosshair;
import electrosphere.client.interact.ButtonInteraction;
import electrosphere.client.interact.ItemActions;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.ingame.CraftingWindow;
import electrosphere.client.ui.menu.ingame.MenuGeneratorsInGame;
import electrosphere.client.ui.menu.ingame.InventoryMainWindow;
import electrosphere.controls.Control;
import electrosphere.controls.Control.ControlMethod;
import electrosphere.controls.Control.ControlType;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.controls.MouseState;
import electrosphere.controls.cursor.CursorState;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.state.movement.editor.ClientEditorMovementTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementRelativeFacing;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementTreeState;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.state.movement.sprint.ClientSprintTree;
import electrosphere.entity.state.movement.walk.ClientWalkTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.events.MouseEvent;
import electrosphere.renderer.ui.events.ScrollEvent;

/**
 * Main game controls
 */
public class ControlCategoryMainGame {

    public static final String INPUT_CODE_CAMERA_ROTATION = "cameraRotation";
    public static final String DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD = "moveForward";
    public static final String DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD = "moveBackward";
    public static final String DATA_STRING_INPUT_CODE_MOVEMENT_LEFT = "moveLeft";
    public static final String DATA_STRING_INPUT_CODE_MOVEMENT_RIGHT = "moveRight";
    public static final String DATA_STRING_INPUT_CODE_STRAFE_LEFT = "strafeLeft";
    public static final String DATA_STRING_INPUT_CODE_STRAFE_RIGHT = "strafeRight";
    public static final String DATA_STRING_INPUT_CODE_MOVEMENT_JUMP = "jump";
    public static final String DATA_STRING_INPUT_CODE_MOVEMENT_FALL = "fall";
    public static final String DATA_STRING_INPUT_CODE_ATTACK_PRIMARY = "attackPrimary";
    public static final String DATA_STRING_INPUT_CODE_IN_GAME_MAIN_MENU = "inGameMainMenu";
    public static final String DATA_STRING_INPUT_CODE_LOCK_CROSSHAIR = "crosshairLock";
    public static final String INPUT_CODE_SPRINT = "sprint";
    public static final String INPUT_CODE_WALK = "walk";
    public static final String INPUT_CODE_SINK = "sink";
    public static final String INPUT_CODE_INTERACT = "interact";
    public static final String INPUT_CODE_DROP = "drop";
    public static final String INPUT_CODE_INVENTORY_OPEN = "inventoryOpen";
    public static final String ITEM_SECONDARY = "actionItemSecondary";
    public static final String TOOLBAR_SCROLL = "toolbarScroll";
    public static final String OPEN_CRAFTING = "openCrafting";
    public static final String TOGGLE_RELEASE_MOUSE = "toggleReleaseMouse";
    
    /**
     * Maps the controls
     * @param handler
     */
    public static void mapControls(ControlHandler handler){
        handler.addControl(INPUT_CODE_CAMERA_ROTATION, new Control(ControlType.MOUSE_MOVEMENT,0,false,"",""));
        handler.addControl(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD, new Control(ControlType.KEY,GLFW.GLFW_KEY_W,false,"Move Foward","Moves the player forward"));
        handler.addControl(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD, new Control(ControlType.KEY,GLFW.GLFW_KEY_S,false,"Move Backward","Moves the player backward"));
        handler.addControl(DATA_STRING_INPUT_CODE_MOVEMENT_LEFT, new Control(ControlType.KEY,GLFW.GLFW_KEY_F24,false,"Move Left","Moves the player left"));
        handler.addControl(DATA_STRING_INPUT_CODE_MOVEMENT_RIGHT, new Control(ControlType.KEY,GLFW.GLFW_KEY_F24,false,"Move Right","Moves the player right"));
        handler.addControl(DATA_STRING_INPUT_CODE_STRAFE_LEFT, new Control(ControlType.KEY,GLFW.GLFW_KEY_A,false,"Strafe Left","Strafes the player left"));
        handler.addControl(DATA_STRING_INPUT_CODE_STRAFE_RIGHT, new Control(ControlType.KEY,GLFW.GLFW_KEY_D,false,"Strafe Right","Strafes the player right"));
        handler.addControl(DATA_STRING_INPUT_CODE_MOVEMENT_JUMP, new Control(ControlType.KEY,GLFW.GLFW_KEY_SPACE,false,"Jump","Jumps"));
        handler.addControl(DATA_STRING_INPUT_CODE_MOVEMENT_FALL, new Control(ControlType.KEY,GLFW.GLFW_KEY_LEFT_CONTROL,false,"Fall","Lowers the camera"));
        handler.addControl(DATA_STRING_INPUT_CODE_ATTACK_PRIMARY, new Control(ControlType.MOUSE_BUTTON,GLFW.GLFW_MOUSE_BUTTON_LEFT,false,"Attack","Attacks"));
        handler.addControl(DATA_STRING_INPUT_CODE_IN_GAME_MAIN_MENU, new Control(ControlType.KEY,GLFW.GLFW_KEY_ESCAPE,false,"Main Menu","Opens the main menu while in game"));
        handler.addControl(DATA_STRING_INPUT_CODE_LOCK_CROSSHAIR, new Control(ControlType.KEY,GLFW.GLFW_KEY_CAPS_LOCK,false,"Lock On","Locks the camera onto the target"));
        handler.addControl(INPUT_CODE_SPRINT, new Control(ControlType.KEY,GLFW.GLFW_KEY_LEFT_SHIFT,false,"Sprint (hold)","Causes the player to sprint"));
        handler.addControl(INPUT_CODE_WALK, new Control(ControlType.KEY,GLFW.GLFW_KEY_LEFT_ALT,false,"Walk (hold)","Causes the player to walk"));
        handler.addControl(INPUT_CODE_SINK, new Control(ControlType.KEY,GLFW.GLFW_KEY_LEFT_CONTROL,true,"Sink","Sinks the entity"));
        handler.addControl(INPUT_CODE_INTERACT, new Control(ControlType.KEY,GLFW.GLFW_KEY_E,false,"Interact","Interacts with whatever is targeted currently"));
        handler.addControl(INPUT_CODE_DROP, new Control(ControlType.KEY,GLFW.GLFW_KEY_Y,false,"Drop","Drops the currently equipped item"));
        handler.addControl(INPUT_CODE_INVENTORY_OPEN, new Control(ControlType.KEY,GLFW.GLFW_KEY_TAB,false,"Inventory","Opens the player's inventory"));
        handler.addControl(ITEM_SECONDARY, new Control(ControlType.MOUSE_BUTTON,GLFW.GLFW_MOUSE_BUTTON_RIGHT,false,"Secondary","Uses the secondary equipped item"));
        handler.addControl(TOOLBAR_SCROLL, new Control(ControlType.MOUSE_SCROLL,0,false,"",""));
        handler.addControl(OPEN_CRAFTING, new Control(ControlType.KEY,GLFW.GLFW_KEY_C,true,"Open Crafting Menu", "Opens the crafting menu"));
        handler.addControl(TOGGLE_RELEASE_MOUSE, new Control(ControlType.KEY,GLFW.GLFW_KEY_RIGHT_ALT,true,"Toggle Mouse", "Toggles whether the mouse is visible or not"));
    }

    /**
     * Populates the control callbacks
     * @param controlMap The control map
     * @param mainGameControlList The main game control list
     * @param inventoryControlList The inventory control list
     */
    public static void setCallbacks(
        HashMap<String, Control> controlMap,
        List<Control> mainGameControlList,
        List<Control> inventoryControlList
    ){
        /*
        Camera rotation
        */
        mainGameControlList.add(controlMap.get(INPUT_CODE_CAMERA_ROTATION));
        controlMap.get(INPUT_CODE_CAMERA_ROTATION).setOnMove(new Control.MouseCallback(){public void execute(MouseState mouseState, MouseEvent event){
            Globals.cameraHandler.handleMouseEvent(event);
        }});
        /*
        Move forward
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD));
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.FORWARD_LEFT);
                    } else if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.FORWARD_RIGHT);
                    } else {
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.FORWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.FORWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.FORWARD_LEFT);
                    } else if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.FORWARD_RIGHT);
                    } else {
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.FORWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.FORWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    groundTree.slowdown();
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});
        /*
        Move backward
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD));
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(3.0/4.0*Math.PI).normalize());
                        groundTree.start(MovementRelativeFacing.BACKWARD_LEFT);
                    } else if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(5.0/4.0*Math.PI).normalize());
                        groundTree.start(MovementRelativeFacing.BACKWARD_RIGHT);
                    } else {
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.BACKWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.BACKWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(3.0/4.0*Math.PI).normalize());
                        groundTree.start(MovementRelativeFacing.BACKWARD_LEFT);
                    } else if(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT).isState()){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(5.0/4.0*Math.PI).normalize());
                        groundTree.start(MovementRelativeFacing.BACKWARD_RIGHT);
                    } else {
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                        groundTree.start(MovementRelativeFacing.BACKWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.BACKWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    groundTree.slowdown();
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});
        /*
        move left
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_LEFT));
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_LEFT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    if(
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(Math.PI/2.0).normalize());
                        groundTree.start(MovementRelativeFacing.FORWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(Math.PI/2.0).normalize());
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.FORWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_LEFT).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    if(
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(Math.PI/2.0).normalize());
                        groundTree.start(MovementRelativeFacing.FORWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(Math.PI/2.0).normalize());
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.FORWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_LEFT).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    groundTree.slowdown();
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});
        /*
        move right
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_RIGHT));
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_RIGHT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    if(
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(-Math.PI/2.0).normalize());
                        groundTree.start(MovementRelativeFacing.FORWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(-Math.PI/2.0).normalize());
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.FORWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_RIGHT).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    if(
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(-Math.PI/2.0).normalize());
                        groundTree.start(MovementRelativeFacing.FORWARD);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    Vector3d cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).rotateY(-Math.PI/2.0).normalize());
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.FORWARD);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_RIGHT).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    groundTree.slowdown();
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});

        /*
        Move up
        */

        /*
        Move down
        */

        /*
        strafe left
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT));
        controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    if(
                        (groundTree.getState()==MovementTreeState.IDLE || groundTree.getState()==MovementTreeState.SLOWDOWN) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        groundTree.start(MovementRelativeFacing.LEFT);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.LEFT);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    if(
                        (groundTree.getState()==MovementTreeState.IDLE || groundTree.getState()==MovementTreeState.SLOWDOWN) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        groundTree.start(MovementRelativeFacing.LEFT);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.LEFT);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_LEFT).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    groundTree.slowdown();
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});
        /*
        strafe right
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT));
        controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    if(
                        (groundTree.getState()==MovementTreeState.IDLE || groundTree.getState()==MovementTreeState.SLOWDOWN) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        groundTree.start(MovementRelativeFacing.RIGHT);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.RIGHT);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    if(
                        (groundTree.getState()==MovementTreeState.IDLE || groundTree.getState()==MovementTreeState.SLOWDOWN) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_FORWARD).getKeyValue())) &&
                        (controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).isIsKey() && !Globals.controlCallback.getKey(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_BACKWARD).getKeyValue()))
                    ){
                        groundTree.start(MovementRelativeFacing.RIGHT);
                    }
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.RIGHT);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_STRAFE_RIGHT).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                BehaviorTree movementTree = CreatureUtils.clientGetEntityMovementTree(Globals.clientState.playerEntity);
                if(movementTree instanceof ClientGroundMovementTree){
                    ClientGroundMovementTree groundTree = (ClientGroundMovementTree) movementTree;
                    groundTree.slowdown();
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});
        /*
        Jump
        DATA_STRING_INPUT_CODE_MOVEMENT_JUMP
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_JUMP));
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_JUMP).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                ClientJumpTree jumpTree = ClientJumpTree.getClientJumpTree(Globals.clientState.playerEntity);
                if(jumpTree != null){
                    jumpTree.start();
                } else if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.UP);
                }
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MOVEMENT_JUMP).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});
        /**
         * Sink
         */
        mainGameControlList.add(controlMap.get(INPUT_CODE_SINK));
        controlMap.get(INPUT_CODE_SINK).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                if(ClientToolbarState.hasClientToolbarState(Globals.clientState.playerEntity)){
                    ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity);
                    if(clientToolbarState.getCurrentPrimaryItem() != null){
                        Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(clientToolbarState.getCurrentPrimaryItem());
                        if(Globals.cursorState.playerCursor != null && Globals.cursorState.playerBlockCursor != null){
                            if(itemData.getTokens().contains(CursorState.CURSOR_BLOCK_TOKEN)) {
                                return;
                            }
                        }
                    }
                }
                if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.start(electrosphere.entity.state.movement.editor.ClientEditorMovementTree.EditorMovementRelativeFacing.DOWN);
                }
            }
        }});
        controlMap.get(INPUT_CODE_SINK).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                if(ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity) != null){
                    ClientEditorMovementTree clientEditorMovementTree = ClientEditorMovementTree.getClientEditorMovementTree(Globals.clientState.playerEntity);
                    clientEditorMovementTree.slowdown();
                }
            }
        }});
        /*
        Sprint
        */
        mainGameControlList.add(controlMap.get(INPUT_CODE_SPRINT));
        controlMap.get(INPUT_CODE_SPRINT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                ClientSprintTree sprintTree = ClientSprintTree.getClientSprintTree(Globals.clientState.playerEntity);
                if(sprintTree != null){
                    sprintTree.start();
                }
            }
        }});
        controlMap.get(INPUT_CODE_SPRINT).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                ClientSprintTree sprintTree = ClientSprintTree.getClientSprintTree(Globals.clientState.playerEntity);
                if(sprintTree != null){
                    sprintTree.interrupt();
                }
            }
        }});

        /**
         * Walk
         */
        mainGameControlList.add(controlMap.get(INPUT_CODE_WALK));
        controlMap.get(INPUT_CODE_WALK).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null && ClientWalkTree.getClientWalkTree(Globals.clientState.playerEntity) != null){
                ClientWalkTree clientWalkTree = ClientWalkTree.getClientWalkTree(Globals.clientState.playerEntity);
                clientWalkTree.start();
            }
        }});
        controlMap.get(INPUT_CODE_WALK).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null && ClientWalkTree.getClientWalkTree(Globals.clientState.playerEntity) != null){
                ClientWalkTree clientWalkTree = ClientWalkTree.getClientWalkTree(Globals.clientState.playerEntity);
                clientWalkTree.interrupt();
            }
        }});


        /*
        Attack
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_ATTACK_PRIMARY));
        controlMap.get(DATA_STRING_INPUT_CODE_ATTACK_PRIMARY).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            ItemActions.attemptPrimaryItemAction();
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_ATTACK_PRIMARY).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            ItemActions.repeatPrimaryItemAction();
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_ATTACK_PRIMARY).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            ItemActions.releasePrimaryItemAction();
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_ATTACK_PRIMARY).setRepeatTimeout(0.5f * Main.targetFrameRate);

        /**
         * Secondary item actions
         */
        mainGameControlList.add(controlMap.get(ITEM_SECONDARY));
        controlMap.get(ITEM_SECONDARY).setOnPress(new ControlMethod() {public void execute(MouseState mouseState) {
            ItemActions.attemptSecondaryItemAction();
        }});
        controlMap.get(ITEM_SECONDARY).setOnRepeat(new ControlMethod() {public void execute(MouseState mouseState) {
            ItemActions.repeatSecondaryItemAction();
        }});
        controlMap.get(ITEM_SECONDARY).setRepeatTimeout(0.5f * Main.targetFrameRate);
        controlMap.get(ITEM_SECONDARY).setOnRelease(new ControlMethod() {public void execute(MouseState mouseState) {
            ItemActions.releaseSecondaryItemAction();
        }});

        /**
         * Toolbar scrolling
         */
        mainGameControlList.add(controlMap.get(TOOLBAR_SCROLL));
        controlMap.get(TOOLBAR_SCROLL).setOnScroll(new Control.ScrollCallback() {public void execute(MouseState mouseState, ScrollEvent scrollEvent){
            boolean handled = false;
            if(
                Globals.controlCallback.getKey(GLFW.GLFW_KEY_LEFT_CONTROL) &&
                !Globals.controlCallback.getKey(GLFW.GLFW_KEY_LEFT_SHIFT)
            ){
                //if the block cursor is visible, capture this input and instead modify block cursor
                if(Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAWABLE).contains(Globals.cursorState.playerBlockCursor)){
                    Globals.cursorState.updateCursorSize(scrollEvent);
                    handled = true;
                }
            }
            
            if(
                !Globals.controlCallback.getKey(GLFW.GLFW_KEY_LEFT_CONTROL) &&
                Globals.controlCallback.getKey(GLFW.GLFW_KEY_LEFT_SHIFT)
            ){
                //if the fab cursor is visible, capture this input and instead modify fab cursor
                if(Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAWABLE).contains(CursorState.getFabCursor())){
                    Globals.cursorState.rotateBlockCursor(scrollEvent);
                    handled = true;
                }
            }


            if(!handled){
                if(Globals.clientState.playerEntity != null && ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity) != null){
                    ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity);
                    if(scrollEvent.getScrollAmount() > 0){
                        clientToolbarState.attemptChangeSelection(clientToolbarState.getSelectedSlot() - 1);
                    } else {
                        clientToolbarState.attemptChangeSelection(clientToolbarState.getSelectedSlot() + 1);
                    }
                }
            }
        }});
        
        /*
        Interact
        */
        mainGameControlList.add(controlMap.get(INPUT_CODE_INTERACT));
        controlMap.get(INPUT_CODE_INTERACT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            ButtonInteraction.handleButtonInteraction();
        }});
        
        /*
        Drop
        */
        mainGameControlList.add(controlMap.get(INPUT_CODE_DROP));
        controlMap.get(INPUT_CODE_DROP).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.clientState.playerEntity != null){
                if(ClientEquipState.hasEquipState(Globals.clientState.playerEntity)){
                    UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(Globals.clientState.playerEntity);
                    if(inventory.getItems().size() > 0){
                        Entity itemToDrop = inventory.getItems().get(0);
                        ClientInventoryState.clientAttemptEjectItem(Globals.clientState.playerEntity,itemToDrop);
                    }
                }
            }
        }});
        
        
        /*
        Lock on crosshair
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_LOCK_CROSSHAIR));
        controlMap.get(DATA_STRING_INPUT_CODE_LOCK_CROSSHAIR).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            if(Crosshair.hasTarget()){
                Crosshair.setCrosshairActive(true);
            }
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_LOCK_CROSSHAIR).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(Crosshair.getCrosshairActive()){
                Crosshair.setCrosshairActive(false);
            }
        }});
        
        
        /*
        Main menu dialog toggle
        */
        mainGameControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_IN_GAME_MAIN_MENU));
        controlMap.get(DATA_STRING_INPUT_CODE_IN_GAME_MAIN_MENU).setOnClick(new ControlMethod(){public void execute(MouseState mouseState){
            // Globals.elementManager.registerWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, MenuGenerators.createInGameMainMenu());
            // Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);

            // Window mainMenuWindow = new Window(0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            Window mainMenuInGame = MenuGeneratorsInGame.createInGameMainMenu();
            // mainMenuWindow.addChild(mainMenuInGame);
            Globals.elementService.registerWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, mainMenuInGame);
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN), true);
            Globals.elementService.focusFirstElement();
            Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
            //play sound effect
            if(Globals.audioEngine != null){
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_TONE_CONFIRM_PRIMARY, VirtualAudioSourceType.UI, false);
            }
            Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(true);
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_IN_GAME_MAIN_MENU).setRepeatTimeout(0.5f * Main.targetFrameRate);

        /*
        Open inventory
        */
        mainGameControlList.add(controlMap.get(INPUT_CODE_INVENTORY_OPEN));
        inventoryControlList.add(controlMap.get(INPUT_CODE_INVENTORY_OPEN));
        controlMap.get(INPUT_CODE_INVENTORY_OPEN).setOnClick(new ControlMethod(){public void execute(MouseState mouseState){
            InventoryMainWindow.viewInventory(Globals.clientState.playerEntity);
        }});
        controlMap.get(INPUT_CODE_INVENTORY_OPEN).setRepeatTimeout(0.5f * Main.targetFrameRate);

        /*
        Open crafting
        */
        mainGameControlList.add(controlMap.get(OPEN_CRAFTING));
        inventoryControlList.add(controlMap.get(OPEN_CRAFTING));
        controlMap.get(OPEN_CRAFTING).setOnClick(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.clientState.interactionTarget = null;
            WindowUtils.openInteractionMenu(WindowStrings.CRAFTING, CraftingWindow.HAND_CRAFTING_DATA);
        }});
        controlMap.get(OPEN_CRAFTING).setRepeatTimeout(0.5f * Main.targetFrameRate);

        /*
        Open crafting
        */
        mainGameControlList.add(controlMap.get(TOGGLE_RELEASE_MOUSE));
        controlMap.get(TOGGLE_RELEASE_MOUSE).setOnClick(new ControlMethod(){public void execute(MouseState mouseState){
            if(Globals.controlHandler.isMouseVisible()){
                Globals.controlHandler.hideMouse();
            } else {
                Globals.controlHandler.showMouse();
            }
        }});
        controlMap.get(TOGGLE_RELEASE_MOUSE).setRepeatTimeout(0.5f * Main.targetFrameRate);
    }

}
