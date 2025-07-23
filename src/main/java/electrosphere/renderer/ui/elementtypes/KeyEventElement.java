package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.KeyboardEvent;

public interface KeyEventElement extends Element {
    
    public void setOnPress(KeyboardEventCallback event);

    public interface KeyboardEventCallback {
        public boolean execute(KeyboardEvent event);
    }

}
