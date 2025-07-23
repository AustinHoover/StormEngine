package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.ValueChangeEvent;

/**
 * Describes an element that contains a changeable value which will be used by the program somewhere
 * IE a carousel, text input, radio dial, etc
 */
public interface ValueElement extends Element {
    
    public void setOnValueChangeCallback(ValueChangeEventCallback callback);

    public interface ValueChangeEventCallback {

        public void execute(ValueChangeEvent event);

    }

}
