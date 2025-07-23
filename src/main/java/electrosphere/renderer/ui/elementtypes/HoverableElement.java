package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.HoverEvent;

/**
 * Hover state handling
 */
public interface HoverableElement extends Element {
    
    //Sets the callback that is triggered when this element is hovered over
    public void setOnHoverCallback(HoverEventCallback callback);

    /**
     * The callback triggered when this element is hovered over
     */
    public interface HoverEventCallback {

        /**
         * Executes the callback
         * @param event The event to handle
         * @return true if should propagate up to parents, false otherwise
         */
        public boolean execute(HoverEvent event);

    }

}
