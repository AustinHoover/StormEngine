package electrosphere.renderer.ui.events;

public class NavigationEvent implements Event {
    public static enum NavigationEventType {
        FORWARD,
        BACKWARD,
    }

    NavigationEventType type;

    public NavigationEvent(NavigationEventType type){
        this.type = type;
    }

    public NavigationEventType getType(){
        return type;
    }
    
}
