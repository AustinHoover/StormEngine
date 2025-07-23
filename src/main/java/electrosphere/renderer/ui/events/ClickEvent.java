package electrosphere.renderer.ui.events;

/**
 * A UI Event where the mouse clicks a button
 */
public class ClickEvent implements Event {
    
    //absolute positions
    int currentX;
    int currentY;
    //relative positions
    int relativeX;
    int relativeY;
    boolean button1;
    boolean button2;

    public ClickEvent(int currentX,int currentY,boolean button1,boolean button2){
        this.currentX = currentX;
        this.currentY = currentY;
        this.button1 = button1;
        this.button2 = button2;
    }

    public int getCurrentX(){
        return currentX;
    }

    public int getCurrentY(){
        return currentY;
    }

    public boolean getButton1(){
        return button1;
    }

    public boolean getButton2(){
        return button2;
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
