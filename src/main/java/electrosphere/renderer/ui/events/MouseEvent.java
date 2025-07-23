package electrosphere.renderer.ui.events;

public class MouseEvent implements Event {

    int currentX;
    int currentY;
    int previousX;
    int previousY;
    int deltaX;
    int deltaY;
    boolean button1;
    boolean button2;

    public void set(int currentX,int currentY,int previousX,int previousY,int deltaX,int deltaY,boolean button1,boolean button2){
        this.currentX = currentX;
        this.currentY = currentY;
        this.previousX = previousX;
        this.previousY = previousY;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.button1 = button1;
        this.button2 = button2;
    }

    public int getCurrentX(){
        return currentX;
    }

    public int getCurrentY(){
        return currentY;
    }

    public int getPreviousX(){
        return previousX;
    }

    public int getPreviousY(){
        return previousY;
    }

    public int getDeltaX(){
        return deltaX;
    }

    public int getDeltaY(){
        return deltaY;
    }

    public boolean getButton1(){
        return button1;
    }

    public boolean getButton2(){
        return button2;
    }

}
