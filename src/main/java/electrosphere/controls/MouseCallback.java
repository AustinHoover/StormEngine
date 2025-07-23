package electrosphere.controls;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import electrosphere.logger.LoggerInterface;

/**
 * A callback for mouse functions
 */
public class MouseCallback extends GLFWMouseButtonCallback {

    //the number of buttons available
    static final short KEY_VALUE_ARRAY_SIZE = 512;

    //current value of all buttons pressed
    boolean[] buttonValues = new boolean[KEY_VALUE_ARRAY_SIZE];

    @Override
    public void invoke(long window, int button, int action, int mods) {
        if(button >= 0 && button < KEY_VALUE_ARRAY_SIZE){
            if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT){
                buttonValues[button] = true;
            } else {
                buttonValues[button] = false;
            }
        }
    }

    /**
     * !!!WARNING!!!, will silently fail if opengl elements not defined or keycode is outside of key value array size or is not in main rendering thread
     * @param keycode The keycode
     * @return The button's pressed state
     */
    public boolean getButton(int keycode){
        if(keycode >= 0 && keycode < KEY_VALUE_ARRAY_SIZE){
            return buttonValues[keycode];
        } else {
            LoggerInterface.loggerEngine.WARNING("Trying to get button state where keycode is undefined (<0  or >400)");
        }
        return false;
    }
    
}
