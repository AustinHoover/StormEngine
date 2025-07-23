package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.MenuEvent;

public interface MenuEventElement extends Element {
    
    public void setOnMenuEventCallback(MenuEventCallback callback);

    public abstract interface MenuEventCallback {

        public boolean execute(MenuEvent event);

    }

}
