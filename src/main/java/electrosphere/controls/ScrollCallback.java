package electrosphere.controls;

import org.lwjgl.glfw.GLFWScrollCallback;

/**
 * A callback for scroll events from the mouse
 */
public class ScrollCallback extends GLFWScrollCallback {

    //the offsets from the most recent scroll event
    double offsetX = 0;
    double offsetY = 0;

    @Override
    public void invoke(long window, double xoffset, double yoffset) {
        offsetX = xoffset;
        offsetY = yoffset;
    }

    /**
     * The x offset from the scroll, !!setting the stored value to 0 in the process!!
     * @return The x scroll offset
     */
    public double getOffsetX(){
        return offsetX;
    }
    
    /**
     * The y offset from the scroll, !!setting the stored value to 0 in the process!!
     * @return The y scroll offset
     */
    public double getOffsetY(){
        return offsetY;
    }

    /**
     * Clears the data cached in the callback
     */
    public void clear(){
        offsetX = 0;
        offsetY = 0;
    }

}
