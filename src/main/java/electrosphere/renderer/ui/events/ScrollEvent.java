package electrosphere.renderer.ui.events;

/**
 * Fired when the user scrolls the mouse wheel
 */
public class ScrollEvent implements Event {
    
    //the amount scrolled by
    double scrollAmount;

    double mouseX;
    double mouseY;

    int relativeX;
    int relativeY;

    /**
     * Constructor
     * @param scrollAmount
     */
    public ScrollEvent(double mouseX, double mouseY, double scrollAmount){
        this.scrollAmount = scrollAmount;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.relativeX = (int)mouseX;
        this.relativeY = (int)mouseY;
    }

    /**
     * Gets the amount scrolled by
     * @return
     */
    public double getScrollAmount(){
        return this.scrollAmount;
    }

    /**
     * Gets the x position of the mouse
     * @return
     */
    public double getMouseX(){
        return mouseX;
    }

    /**
     * Gets the y position of the mouse
     * @return
     */
    public double getMouseY(){
        return mouseY;
    }

    public int getRelativeX(){
        return relativeX;
    }

    public int getRelativeY(){
        return relativeY;
    }

    public void setRelativeX(int relX){
        this.relativeX = relX;
    }

    public void setRelativeY(int relY){
        this.relativeY = relY;
    }


}
