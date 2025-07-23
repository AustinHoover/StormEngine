package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.ClickEvent;

public interface ClickableElement extends Element {

    public void setOnClick(ClickEventCallback callback);

    public interface ClickEventCallback {
        public boolean execute(ClickEvent event);
    }
    
}
