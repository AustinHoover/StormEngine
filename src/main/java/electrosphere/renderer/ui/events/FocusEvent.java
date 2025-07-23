package electrosphere.renderer.ui.events;

public class FocusEvent implements Event {

    boolean isFocused;

    public FocusEvent(boolean isFocused){
        this.isFocused = isFocused;
    }

    public boolean isFocused(){
        return isFocused;
    }
    
}
