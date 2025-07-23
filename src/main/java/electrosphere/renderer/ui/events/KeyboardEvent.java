package electrosphere.renderer.ui.events;

public class KeyboardEvent implements Event {

    String key;

    public KeyboardEvent(String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }
    
}
