package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.ScrollEvent;

/**
 * An element that accepts scroll event
 */
public interface ScrollableElement extends Element {
    
    /**
     * Sets the scroll event handler for the element
     * @param callback
     */
    public void setOnScrollCallback(ScrollEventCallback callback);

    /**
     * A callback that handles scroll events
     */
    public interface ScrollEventCallback {

        /**
         * 
         * @param event
         * @return
         */
        public boolean execute(ScrollEvent event);

    }

}
