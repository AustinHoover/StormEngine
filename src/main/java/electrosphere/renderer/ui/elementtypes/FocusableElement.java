package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.FocusEvent;

/**
 * A focusable element
 */
public interface FocusableElement extends Element {

    /**
     * Gets the focused status of the element
     * @return true if focused, false otherwise
     */
    public boolean isFocused();

    /**
     * Set the focused status of the elemtn
     * @param focused true if focused, false otherwise
     */
    public void setFocused(boolean focused);

    /**
     * Sets the on focus callback
     * @param callback The callback
     */
    public abstract void setOnFocus(FocusEventCallback callback);

    /**
     * Sets the on lose focus callback
     * @param callback The callback
     */
    public abstract void setOnLoseFocus(FocusEventCallback callback);
    
    /**
     * A focus event callback
     */
    public abstract interface FocusEventCallback {

        /**
         * Executes the callback
         * @param event The focus event
         * @return true if the event should propagate to the parent of this element, false otherwise
         */
        public boolean execute(FocusEvent event);

    }
    
}
