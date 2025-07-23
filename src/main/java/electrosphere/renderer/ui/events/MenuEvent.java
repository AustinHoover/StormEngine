package electrosphere.renderer.ui.events;

public class MenuEvent implements Event {
    
    public static enum MenuEventType {
        INCREMENT,
        DECREMENT,
    }

    MenuEventType type;

    public MenuEvent(MenuEventType type){
        this.type = type;
    }

    public MenuEventType getType(){
        return type;
    }

}
