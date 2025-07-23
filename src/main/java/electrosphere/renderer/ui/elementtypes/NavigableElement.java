package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.NavigationEvent;

public interface NavigableElement extends Element {
    
    public void setOnNavigationCallback(NavigationEventCallback callback);

    public interface NavigationEventCallback {

        public boolean execute(NavigationEvent event);

    }

}
