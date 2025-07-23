package electrosphere.renderer.ui.events;

/**
 * Triggered when an element is hovered over
 */
public class HoverEvent implements Event {
    
    //if true, this element is hovered over, false otherwise
    boolean isHovered;

    /**
     * The mouse position of the event
     */
    int currentX;
    int currentY;

    /**
     * Creates the hover event
     * @param isHovered true if hovered over, false otherwise
     */
    public HoverEvent(boolean isHovered, int currentX, int currentY){
        this.isHovered = isHovered;
        this.currentX = currentX;
        this.currentY = currentY;
    }

    /**
     * Returns if the element is hovered over
     * @return true if hovered over, false otherwise
     */
    public boolean isHovered(){
        return isHovered;
    }

    /**
     * Gets the x position of the mouse
     * @return The x position
     */
    public int getCurrentX() {
        return currentX;
    }

    /**
     * Gets the y position of the mouse
     * @return The y position
     */
    public int getCurrentY() {
        return currentY;
    }

    

}
