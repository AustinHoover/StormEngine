package electrosphere.controls;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.categories.ControlCategoryAlwaysOnDebug;
import electrosphere.controls.categories.ControlCategoryFreecam;
import electrosphere.controls.categories.ControlCategoryInGameDebug;
import electrosphere.controls.categories.ControlCategoryInventory;
import electrosphere.controls.categories.ControlCategoryMainGame;
import electrosphere.controls.categories.ControlCategoryMenuNav;
import electrosphere.controls.categories.ControlCategoryTyping;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.events.MouseEvent;
import electrosphere.renderer.ui.events.ScrollEvent;

/**
 * Main handler for controls
 */
public class ControlHandler {
    
    /**
     * The different buckets of inputs that the control handler be configured to scan for each frame
     */
    public static enum ControlsState {
        TITLE_PAGE,
        TITLE_MENU,
        MAIN_GAME,
        IN_GAME_MAIN_MENU,
        IN_GAME_FREE_CAMERA,
        INVENTORY,
        NO_INPUT,
    }
    
    //The bucket of inputs that the control handler is currently scanning for
    ControlsState state = ControlsState.TITLE_MENU;

    /**
     * The state of the mouse
     */
    MouseState mouseState = new MouseState();


    /**
     * The list of control states that have the mouse visible and enabled
     */
    static ControlsState[] mouseEnabledStates = new ControlsState[]{
        ControlsState.TITLE_PAGE,
        ControlsState.TITLE_MENU,
        ControlsState.IN_GAME_MAIN_MENU,
        ControlsState.INVENTORY,
    };
    

    /**
     * Controls whether the mouse is visible or not
     */
    boolean mouseIsVisible = true;

    /**
     * If set to true, opengl will try to capture the screen next frame
     */
    boolean shouldRecaptureScreen = false;

    /**
     * Controls whether the camera is first or third person
     */
    boolean cameraIsThirdPerson = false;

    /**
     * Private mouse event to prevent re-allocating
     */
    private MouseEvent mouseEvent = new MouseEvent();

    /**
     * The list of window strings that would block main game controls
     */
    static String[] controlBlockingWindows = new String[]{
        WindowStrings.LEVEL_EDTIOR_SIDE_PANEL,
        WindowStrings.VOXEL_TYPE_SELECTION,
        WindowStrings.SPAWN_TYPE_SELECTION,
        WindowStrings.WINDOW_CHARACTER,
        WindowStrings.WINDOW_INVENTORY_TARGET,
        WindowStrings.WINDOW_DEBUG,
        WindowStrings.WINDOW_MENU_INGAME_MAIN,
        WindowStrings.WINDOW_MENU_INVENTORY,
        WindowStrings.WINDOW_MENU_MAIN,
        WindowStrings.NPC_DIALOG,
    };


    /**
     * The map of control name (string) -> control (object)
     */
    HashMap<String, Control> controls;

    List<Control> mainGameControlList = new LinkedList<Control>();
    List<Control> mainGameDebugControlList = new LinkedList<Control>();
    List<Control> menuNavigationControlList = new LinkedList<Control>();
    List<Control> typingControlList = new LinkedList<Control>();
    List<Control> inventoryControlList = new LinkedList<Control>();
    List<Control> alwaysOnDebugControlList = new LinkedList<Control>();
    List<Control> freeCameraControlList = new LinkedList<Control>();
    
    /**
     * Constructor
     */
    private ControlHandler(){
        controls = new HashMap<String, Control>();
    }
    
    /**
     * Generates an example controls map
     * @return the example controls map object
     */
    public static ControlHandler generateExampleControlsMap(){
        ControlHandler handler = new ControlHandler();

        ControlCategoryAlwaysOnDebug.mapControls(handler);
        ControlCategoryMainGame.mapControls(handler);
        ControlCategoryMenuNav.mapControls(handler);
        ControlCategoryTyping.mapControls(handler);
        ControlCategoryInventory.mapControls(handler);
        ControlCategoryInGameDebug.mapControls(handler);
        ControlCategoryFreecam.mapControls(handler);

        
        /*
        Save to file
        */
//        Utilities.saveObjectToBakedJsonFile("/Config/keybinds.json", handler);

        /*
        return
        */
        return handler;
    }


    
    
    /**
     * Polls the currently set bucket of controls
     */
    public void pollControls(){
        switch(state){
            
            
            case MAIN_GAME: {
                this.runHandlers(mainGameControlList);
                this.runHandlers(mainGameDebugControlList);
                this.runHandlers(alwaysOnDebugControlList);
            } break;
                
                
                
            case TITLE_PAGE:
            break;
                
                
            case TITLE_MENU: {
                this.runHandlers(menuNavigationControlList);
                /*
                Typing..
                */
                this.runHandlers(typingControlList);
                this.runHandlers(alwaysOnDebugControlList);
            } break;
                
            case IN_GAME_MAIN_MENU: {
                this.runHandlers(menuNavigationControlList);
                this.runHandlers(typingControlList);
                this.runHandlers(alwaysOnDebugControlList);
                // pollMenuNavigationControls();
            } break;

            case IN_GAME_FREE_CAMERA: {
                this.runHandlers(freeCameraControlList);
                this.runHandlers(mainGameDebugControlList);
                this.runHandlers(alwaysOnDebugControlList);
            } break;

            case INVENTORY: {
                this.runHandlers(inventoryControlList);
                this.runHandlers(menuNavigationControlList);
                this.runHandlers(alwaysOnDebugControlList);
            } break;
                
            case NO_INPUT: {
                this.runHandlers(alwaysOnDebugControlList);
            } break;
                
        }
        Globals.scrollCallback.clear();
    }

    /**
     * Attaches callbacks to each of the control objects
     */
    public void setCallbacks(){
        ControlCategoryMainGame.setCallbacks(controls, mainGameControlList, inventoryControlList);
        ControlCategoryInGameDebug.setCallbacks(controls, mainGameDebugControlList, alwaysOnDebugControlList);
        ControlCategoryMenuNav.setCallbacks(controls, menuNavigationControlList, mainGameDebugControlList, inventoryControlList);
        ControlCategoryTyping.setCallbacks(controls, typingControlList);
        ControlCategoryInventory.setCallbacks(controls, inventoryControlList);
        ControlCategoryAlwaysOnDebug.setCallbacks(controls, alwaysOnDebugControlList);
        ControlCategoryFreecam.setCallbacks(controls, freeCameraControlList);
    }

    /**
     * Gets the main controls list
     * @return The main controls
     */
    public List<Control> getMainControlsList(){
        return mainGameControlList;
    }


    /**
     * Checks a list of controls to see if the corresponding key/mouse event is firing this frame
     * @param controls The list of controls to check
     */
    private void runHandlers(List<Control> controls){
        //Fills the buffer
        this.getMousePositionInBuffer();
        this.mouseState.setLastX((float)this.mouseState.getCurrentX());
        this.mouseState.setLastY((float)this.mouseState.getCurrentY());
        this.mouseState.setCurrentX(this.mouseState.getMouseBufferX()[0]);
        this.mouseState.setCurrentY(this.mouseState.getMouseBufferY()[0]);
        this.mouseState.setDeltaX(this.mouseState.getCurrentX() - this.mouseState.getLastX());
        this.mouseState.setDeltaY(this.mouseState.getLastY() - this.mouseState.getCurrentY());
        float xoffset = (float) (this.mouseState.getCurrentX() - this.mouseState.getLastX());
        float yoffset = (float) (this.mouseState.getLastY() - this.mouseState.getCurrentY());
        this.mouseEvent.set(
            (int)this.mouseState.getCurrentX(),
            (int)this.mouseState.getCurrentY(),
            (int)this.mouseState.getLastX(),
            (int)this.mouseState.getLastY(),
            (int)xoffset,
            (int)yoffset,
            this.getButton1Raw(),
            this.getButton2Raw()
        );
        boolean mouseMoveEvent = xoffset != 0 || yoffset != 0;


        for(Control control : controls){
            switch(control.getType()){
                case KEY: {
                    if(Globals.controlCallback.getKey(control.getKeyValue())){
                        if(!control.isState()){
                            //on press
                            control.onPress(this.mouseState);
                            control.setPressFrame((float)Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed());
                        } else {
                            //on repeat
                            if((float)Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed() - control.getPressFrame() > control.getRepeatTimeout()){
                                control.onRepeat(this.mouseState);
                            }
                        }
                        control.setState(true);
                    } else {
                        if(control.isState()){
                            //on release
                            control.onRelease(this.mouseState);
                            //on click
                            if((float)Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed() - control.getPressFrame() < control.getRepeatTimeout()){
                                control.onClick(this.mouseState);
                            }
                        } else {
                        }
                        control.setState(false);
                    }
                } break;
                case MOUSE_BUTTON: {
                    if(Globals.mouseCallback.getButton(control.getKeyValue())){
                        if(!control.isState()){
                            //on press
                            control.onPress(this.mouseState);
                            control.setPressFrame((float)Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed());
                        } else {
                            //on repeat
                            control.onRepeat(this.mouseState);
                        }
                        control.setState(true);
                    } else {
                        if(control.isState()){
                            //on release
                            control.onRelease(this.mouseState);
                            if((float)Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed() - control.getPressFrame() < control.getRepeatTimeout()){
                                control.onClick(this.mouseState);
                            }
                        } else {
                        }
                        control.setState(false);
                    }
                } break;
                case MOUSE_MOVEMENT: {
                    if(mouseMoveEvent){
                        control.onMove(this.mouseState,this.mouseEvent);
                    }
                } break;
                case MOUSE_SCROLL: {
                    double yScroll = Globals.scrollCallback.getOffsetY();
                    if(yScroll != 0){
                        ScrollEvent event = new ScrollEvent(this.mouseState.getCurrentX(),this.mouseState.getCurrentY(),yScroll);
                        control.onScroll(this.mouseState,event);
                    }
                } break;
            }
        }
    }

    /**
     * Checks if any menus are open that would intercept player input (main menu, inventory, debug, etc)
     * @return true if such a menu is open, false otherwise
     */
    private boolean hasControlBlockingMenuOpen(){
        boolean rVal = false;
        //check main ui framework windows
        for(String windowString : controlBlockingWindows){
            rVal = rVal || WindowUtils.windowIsOpen(windowString);
        }
        //check if any generated inventory windows are open
        for(String windowId : Globals.elementService.getCurrentWindowIds()){
            rVal = rVal || (WindowUtils.windowIsOpen(windowId) && WindowUtils.isInventoryWindow(windowId));
        }
        //check imgui windows
        rVal = rVal || Globals.renderingEngine.getImGuiPipeline().shouldCaptureControls();
        return rVal;
    }

    /**
     * Checks if the only menus open are inventory menus
     * @return true if only inventory menus AND a menu is open, false otherwise
     */
    private boolean onlyInventoryMenusOpen(){
        boolean foundInventory = false;
        for(String windowId : Globals.elementService.getCurrentWindowIds()){
            if(WindowUtils.isInventoryWindow(windowId) || WindowStrings.WINDOW_MENU_INVENTORY.equals(windowId) || WindowStrings.WINDOW_CHARACTER.equals(windowId) || WindowStrings.WINDOW_INVENTORY_TARGET.equals(windowId)){
                foundInventory = true;
            } else if(Globals.elementService.getWindow(windowId) instanceof Window == false || ((Window)Globals.elementService.getWindow(windowId)).visible) {
                return false;
            }
        }
        return foundInventory;
    }

    /**
     * Hints to the engine that it should update the control state
     * The provided control state will be overwritten if, for instance,
     * there is a menu open that demands mouse input and you are trying
     * to tell the engine to convert to immediate player control
     * @param desiredState The desired control state
     */
    public void hintUpdateControlState(ControlsState desiredState){
        ControlsState properState = desiredState;
        //correct for freecam or actual ingame control based on value of getTrackPlayerEntity
        if(desiredState == ControlsState.IN_GAME_FREE_CAMERA && Globals.cameraHandler.getTrackPlayerEntity()){
            properState = ControlsState.MAIN_GAME;
        }
        if(desiredState == ControlsState.MAIN_GAME && !Globals.cameraHandler.getTrackPlayerEntity()){
            properState = ControlsState.IN_GAME_FREE_CAMERA;
        }

        //set to menu state if a menu is open, otherwise use the hinted control scheme
        if(this.onlyInventoryMenusOpen()){
            this.setHandlerState(ControlsState.INVENTORY);
        } else if(this.hasControlBlockingMenuOpen()){
            this.setHandlerState(ControlsState.IN_GAME_MAIN_MENU);
        } else {
            this.setHandlerState(properState);
        }
        //checks if the current handler state should have mouse enabled or not
        if(Arrays.binarySearch(mouseEnabledStates,getHandlerState()) >= 0){
            this.showMouse();
        } else {
            this.hideMouse();
        }
    }

    /**
     * Transfers the mouse position from the glfw buffer to variables stored inside the control handler
     */
    private void getMousePositionInBuffer(){
        //only if not headless, gather position
        if(!EngineState.EngineFlags.HEADLESS){
            glfwGetCursorPos(Globals.renderingEngine.getWindowPtr(), this.mouseState.getMouseBufferX(), this.mouseState.getMouseBufferY());
        }
    }

    /**
     * Checks if the mouse button 1 is currently pressed
     * @return true if pressed, false otherwise
     */
    private boolean getButton1Raw(){
        if(EngineState.EngineFlags.HEADLESS){
            return false;
        } else {
            return Globals.mouseCallback.getButton(GLFW_MOUSE_BUTTON_1);
        }
    }

    /**
     * Checks if the mouse button 2 is currently pressed
     * @return true if pressed, false otherwise
     */
    private boolean getButton2Raw(){
        if(EngineState.EngineFlags.HEADLESS){
            return false;
        } else {
            return Globals.mouseCallback.getButton(GLFW_MOUSE_BUTTON_2);
        }
    }
    
    /**
     * Gets a control
     * @param controlName The name of the control
     * @return The control if it exists, null otherwise
     */
    public Control getControl(String controlName){
        return controls.get(controlName);
    }
    
    /**
     * Checks if the handler contains a control
     * @param controlName The name of the control
     * @return true if the control exists, false otherwise
     */
    public boolean containsControl(String controlName){
        return controls.containsKey(controlName);
    }
    
    /**
     * Removes a control from the handler
     * @param controlName The name of the control
     */
    public void removeControl(String controlName){
        controls.remove(controlName);
    }
    
    /**
     * Adds a control to the handler
     * @param controlName The name of the control
     * @param c The control itself
     */
    public void addControl(String controlName, Control c){
        if(controls.containsKey(controlName)){
            throw new Error("Control handler already contains control " + controlName + " " + c + " " + controls.get(controlName));
        }
        controls.put(controlName, c);
    }
    
    /**
     * Sets the state of the controls handler
     * @param state the state
     */
    private void setHandlerState(ControlsState state){
        this.state = state;
    }

    /**
     * Gets the current state of the controls handler
     * @return the state
     */
    public ControlsState getHandlerState(){
        return state;
    }
    
    /**
     * Hides the mouse
     */
    public void hideMouse(){
        glfwSetInputMode(Globals.renderingEngine.getWindowPtr(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        glfwSetInputMode(Globals.renderingEngine.getWindowPtr(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        mouseIsVisible = false;
    }
    
    /**
     * Shows the mouse
     */
    public void showMouse(){
        glfwSetInputMode(Globals.renderingEngine.getWindowPtr(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        mouseIsVisible = true;
    }
    
    /**
     * Gets whether the mouse is visible or not
     * @return true if visible, false otherwise
     */
    public boolean isMouseVisible(){
        return mouseIsVisible;
    }
    
    /**
     * Gets the mouse position as a vector2f
     * @return The vector containing the mouse position
     */
    public Vector2f getMousePosition(){
        double posX[] = new double[1];
        double posY[] = new double[1];
        GLFW.glfwGetCursorPos(Globals.renderingEngine.getWindowPtr(), posX, posY);
        Vector2f rVal = new Vector2f((float)posX[0],(float)posY[0]);
        return rVal;
    }

    /**
     * Gets the mouse position as a vector2f
     * @return The vector containing the mouse position
     */
    public Vector2f getMousePositionNormalized(){
        double posX[] = new double[1];
        double posY[] = new double[1];
        GLFW.glfwGetCursorPos(Globals.renderingEngine.getWindowPtr(), posX, posY);
        int sizeX[] = new int[1];
        int sizeY[] = new int[1];
        GLFW.glfwGetWindowSize(Globals.renderingEngine.getWindowPtr(), sizeX, sizeY);
        Vector2f rVal = new Vector2f((float)((2.0 * posX[0] / sizeX[0]) - 1.0),(float)(1.0 - (2.0 * posY[0] / sizeY[0])));
        return rVal;
    }
    
    /**
     * Sets whether the engine should try to recapture window focus next frame or not
     * @param shouldRecapture true if should try to recapture next frame, false otherwise
     */
    public void setRecapture(boolean shouldRecapture){
        this.shouldRecaptureScreen = shouldRecapture;
    }

    /**
     * Returns whether the engine should try to recapture window focus next frame or not
     * @return true if it should try to recapture, false otherwise
     */
    public boolean shouldRecapture(){
        return this.shouldRecaptureScreen;
    }

    /**
     * Checks if the camera is third person
     * @return true if third person, false if first person
     */
    public boolean cameraIsThirdPerson(){
        return cameraIsThirdPerson;
    }

    /**
     * Sets the 1st/3rd person status of the camera
     * @param isThirdPerson True for 3rd person, false for 1st person
     */
    public void setIsThirdPerson(boolean isThirdPerson){
        this.cameraIsThirdPerson = isThirdPerson;
        if(Globals.clientState.playerEntity != null){
            CameraEntityUtils.initCamera();
            ClientEquipState playerEquipState = ClientEquipState.getClientEquipState(Globals.clientState.playerEntity);
            if(playerEquipState != null){
                playerEquipState.evaluatePlayerAttachments();
            }
        }
    }
    
    
}
