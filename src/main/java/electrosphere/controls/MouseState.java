package electrosphere.controls;

/**
 * Tracks the state of the mouse
 */
public class MouseState {
    
    /**
     * The x coordinate of the position last frame
     */
    float lastX = 400;

    /**
     * The y coordinate of the position last frame
     */
    float lastY = 300;

    /**
     * The x coordinate of the position this frame
     */
    double currentX = 400;

    /**
     * The y coordinate of the position this frame
     */
    double currentY = 300;
    
    /**
     * The delta in the x axis between last frame and this frame
     */
    double deltaX = 0;

    /**
     * The delta in the y axis between last frame and this frame
     */
    double deltaY = 0;

    /**
     * The buffer for storing x coordinates
     */

    double mouseBufferX[] = new double[1];

    /**
     * The buffer for storing y coordinates
     */
    double mouseBufferY[] = new double[1];

    /**
     * Tracks whether the mouse is doing a drag input or not
     */
    boolean dragging = false;

    /**
     * Gets the x coordinate of the position last frame
     * @return The x coordinate of the position last frame
     */
    public float getLastX() {
        return lastX;
    }

    /**
     * Sets the x coordinate of the position last frame
     * @param lastX The x coordinate of the position last frame
     */
    public void setLastX(float lastX) {
        this.lastX = lastX;
    }

    /**
     * Gets the y coordinate of the position last frame
     * @return The y coordinate of the position last frame
     */
    public float getLastY() {
        return lastY;
    }

    /**
     * Sets the y coordinate of the position last frame
     * @param lastY The y coordinate of the position last frame
     */
    public void setLastY(float lastY) {
        this.lastY = lastY;
    }

    /**
     * Gets the x coordinate of the position this frame
     * @return The x coordinate of the position this frame
     */
    public double getCurrentX() {
        return currentX;
    }

    /**
     * Sets the x coordinate of the position this frame
     * @param currentX The x coordinate of the position this frame
     */
    public void setCurrentX(double currentX) {
        this.currentX = currentX;
    }

    /**
     * Gets the y coordinate of the position this frame
     * @return The y coordinate of the position this frame
     */
    public double getCurrentY() {
        return currentY;
    }

    /**
     * Sets the y coordinate of the position this frame
     * @param currentY The y coordinate of the position this frame
     */
    public void setCurrentY(double currentY) {
        this.currentY = currentY;
    }

    /**
     * Gets the buffer for storing x coordinates
     * @return The buffer for storing x coordinates
     */
    public double[] getMouseBufferX() {
        return mouseBufferX;
    }

    /**
     * Gets the buffer for storing y coordinates
     * @return The buffer for storing y coordinates
     */
    public double[] getMouseBufferY() {
        return mouseBufferY;
    }

    /**
     * Checks whether the mouse is doing a drag input or not
     * @return true if the mouse is dragging, false otherwise
     */
    public boolean isDragging() {
        return dragging;
    }

    /**
     * Sets whether the mouse is doing a drag input or not
     * @param dragging true if the mouse is dragging, false otherwise
     */
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    /**
     * Gets the delta in the x axis between last frame and this frame
     * @return The delta in the x axis between last frame and this frame
     */
    public double getDeltaX() {
        return deltaX;
    }

    /**
     * Sets the delta in the x axis between last frame and this frame
     * @return The delta in the x axis between last frame and this frame
     */
    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    /**
     * Gets the delta in the y axis between last frame and this frame
     * @return The delta in the y axis between last frame and this frame
     */
    public double getDeltaY() {
        return deltaY;
    }

    /**
     * Sets the delta in the y axis between last frame and this frame
     * @return The delta in the y axis between last frame and this frame
     */
    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }

    
    

}
