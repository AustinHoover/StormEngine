package electrosphere.renderer.ui.events;

import electrosphere.renderer.ui.elementtypes.Element;

public class DragEvent implements Event {

    public static enum DragEventType {
        START,
        DRAG,
        RELEASE,
    }

    int currentX;
    int currentY;
    int previousX;
    int previousY;
    //relative positions
    int relativeX;
    int relativeY;
    int deltaX;
    int deltaY;
    DragEventType type;
    Element target;

    public DragEvent(int currentX,int currentY,int previousX,int previousY,int deltaX,int deltaY,DragEventType type,Element target){
        this.currentX = currentX;
        this.currentY = currentY;
        this.previousX = previousX;
        this.previousY = previousY;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.type = type;
        this.target = target;
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

    public DragEventType getType(){
        return type;
    }

    public Element getTarget(){
        return target;
    }

    public void setTarget(Element target){
        this.target = target;
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
