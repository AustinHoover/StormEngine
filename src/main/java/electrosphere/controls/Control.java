package electrosphere.controls;

import electrosphere.renderer.ui.events.MouseEvent;
import electrosphere.renderer.ui.events.ScrollEvent;

/**
 * A low level control inside the engine
 */
public class Control {

    /**
     * The different types of controls
     */
    public static enum ControlType {
        /**
         * A key control
         */
        KEY,
        /**
         * A mouse button control
         */
        MOUSE_BUTTON,
        /**
         * A mouse movement control
         */
        MOUSE_MOVEMENT,
        /**
         * A mouse scroll control
         */
        MOUSE_SCROLL,
    }

    /**
     * The type of this control
     */
    ControlType type;

    /**
     * The activated state of the control
     */
    boolean state;

    /**
     * The key value of the control if it is a key control
     */
    int keyValue;

    /**
     * The name of the control
     */
    String name;

    /**
     * The description of the control
     */
    String description;

    /**
     * True if this control is rebindable, false otherwise
     */
    boolean rebindable;

    /**
     * The method to fire on pressing the button/key
     */
    ControlMethod onPress;

    /**
     * The method to fire on releasing the button/key
     */
    ControlMethod onRelease;

    /**
     * The method to fire on repeating the button/key
     */
    ControlMethod onRepeat;

    /**
     * The method to fire on clicking with the mouse
     */
    ControlMethod onClick;

    /**
     * The method to fire on moving the mouse
     */
    MouseCallback onMove;

    /**
     * The method to fire on scrollign with the mouse
     */
    ScrollCallback onScroll;

    /**
     * The frame the control was pressed on
     */
    float pressFrame = 0;

    /**
     * The repeat timeout of the control
     */
    float repeatTimeout = 0;

    /**
     * Constructor
     * @param type
     * @param keyValue
     * @param rebindable
     * @param name
     * @param description
     */
    public Control(ControlType type, int keyValue, boolean rebindable, String name, String description) {
        this.type = type;
        this.keyValue = keyValue;
        this.state = false;
        this.rebindable = rebindable;
        this.name = name;
        this.description = description;
    }

    /**
     * Gets if this is a key control
     * @return true if it is a key control, false otherwise
     */
    public boolean isIsKey() {
        return type == ControlType.KEY;
    }

    /**
     * Gets if this is a mouse control
     * @return true if it is a mouse control, false otheriwse
     */
    public boolean isIsMouse() {
        return type == ControlType.MOUSE_BUTTON;
    }

    /**
     * Gets the state of the control
     * @return true if it is active, false otherwise
     */
    public boolean isState() {
        return state;
    }

    /**
     * Gets the type of control
     * @return The type
     */
    public ControlType getType(){
        return type;
    }

    /**
     * Gets the key value of the control
     * @return The key value
     */
    public int getKeyValue() {
        return keyValue;
    }

    /**
     * Sets the state of the control
     * @param state The state
     */
    public void setState(boolean state) {
        this.state = state;
    }

    /**
     * Sets the on press callback of the control
     * @param method The callback
     */
    public void setOnPress(ControlMethod method){
        onPress = method;
    }

    /**
     * Sets the on release callback of the control
     * @param method The callback
     */
    public void setOnRelease(ControlMethod method){
        onRelease = method;
    }

    /**
     * Sets the on repeat callback of the control
     * @param method The callback
     */
    public void setOnRepeat(ControlMethod method){
        onRepeat = method;
    }

    /**
     * Sets the on mouse move callback of the control
     * @param method The callback
     */
    public void setOnMove(MouseCallback method){
        onMove = method;
    }

    /**
     * Sets the on mouse click of the control
     * @param method The callback
     */
    public void setOnClick(ControlMethod method){
        onClick = method;
    }

    /**
     * Sets the on mouse scroll callback of the control
     * @param method The callback
     */
    public void setOnScroll(ScrollCallback callback){
        onScroll = callback;
    }

    /**
     * Sets the on press callback of the control
     * @param method The callback
     */
    public void onPress(MouseState mouseState){
        if(onPress != null){
            onPress.execute(mouseState);
        }
    }

    /**
     * Sets the on release callback of the control
     * @param method The callback
     */
    public void onRelease(MouseState mouseState){
        if(onRelease != null){
            onRelease.execute(mouseState);
        }
    }

    /**
     * Sets the on repeat callback of the control
     * @param method The callback
     */
    public void onRepeat(MouseState mouseState){
        if(onRepeat != null){
            onRepeat.execute(mouseState);
        }
    }

    /**
     * Sets the on mouse move callback of the control
     * @param method The callback
     */
    public void onMove(MouseState mouseState, MouseEvent event){
        if(onMove != null){
            onMove.execute(mouseState, event);
        }
    }

    /**
     * Sets the on mouse click callback of the control
     * @param method The callback
     */
    public void onClick(MouseState mouseState){
        if(onClick != null){
            onClick.execute(mouseState);
        }
    }

    /**
     * Sets the on mouse scroll callback of the control
     * @param method The callback
     */
    public void onScroll(MouseState mouseState, ScrollEvent event){
        if(onScroll != null){
            onScroll.execute(mouseState, event);
        }
    }

    /**
     * Gets the frame the control was pressed
     * @return The frame
     */
    public float getPressFrame(){
        return this.pressFrame;
    }

    /**
     * Sets the frame the control was pressed
     * @param frame The frame
     */
    public void setPressFrame(float frame){
        pressFrame =  frame;
    }

    /**
     * Gets the repeat timeout of the control
     * @return The repeat timeout
     */
    public float getRepeatTimeout(){
        return this.repeatTimeout;
    }

    /**
     * Sets the repeat timeout of the control
     * @param timeout The repeat timeout
     */
    public void setRepeatTimeout(float timeout){
        repeatTimeout = timeout;
    }

    /**
     * Gets whether this control is rebindable
     * @return true if it is rebindable, false otherwise
     */
    public boolean isRebindable(){
        return rebindable;
    }

    /**
     * Gets the name of the control
     * @return The name
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the description of the control
     * @return The description
     */
    public String getDescription(){
        return description;
    }
    

    /**
     * A callback to fire on a control event
     */
    public interface ControlMethod {
        /**
         * Excecutes the callback
         * @param mouseState The state of the mouse
         */
        public void execute(MouseState mouseState);
    }

    /**
     * A callback to fire on a mouse event
     */
    public interface MouseCallback {
        /**
         * Executes the callback
         * @param mouseState The state of the mouse
         * @param event The mouse event
         */
        public void execute(MouseState mouseState, MouseEvent event);
    }

    /**
     * A callback to fire on a mouse scroll event
     */
    public interface ScrollCallback {
        /**
         * Executes the callback
         * @param mouseState The state of the mouse
         * @param event The mouse event
         */
        public void execute(MouseState mouseState, ScrollEvent event);
    }
    
}
