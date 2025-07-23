package electrosphere.controls;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import electrosphere.logger.LoggerInterface;

/**
 * Callback that is invoked on key input received by GLFW
 */
public class ControlCallback implements GLFWKeyCallbackI {

    /**
     * The size of the key state tracking array
     */
    static final short KEY_VALUE_ARRAY_SIZE = 512;

    /**
     * Array that tracks the state of keys
     */
    boolean[] keyValues = new boolean[KEY_VALUE_ARRAY_SIZE];

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods){
        if(key >= 0 && key < KEY_VALUE_ARRAY_SIZE){
            if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT){
                keyValues[key] = true;
            } else {
                keyValues[key] = false;
            }
        }
    }

    /**
     * !!!WARNING!!!, will silently fail if opengl elementsn ot defined or keycode is outside of key value array size or is not in main rendering thread
     * @param keycode The keycode to check
     * @return true if it is pressed, false otherwise
     */
    public boolean getKey(int keycode){
        if(keycode >= 0 && keycode < KEY_VALUE_ARRAY_SIZE){
            return keyValues[keycode];
        } else {
            LoggerInterface.loggerEngine.WARNING("Trying to get key state where keycode is undefined (<0  or >400)");
        }
        return false;
    }

}
