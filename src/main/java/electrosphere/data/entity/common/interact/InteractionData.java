package electrosphere.data.entity.common.interact;

import electrosphere.data.entity.collidable.CollidableTemplate;

/**
 * Controls handling when interacting with this entity
 */
public class InteractionData {

    /**
     * Pulls up a menu on interaction
     */
    public static final String ON_INTERACT_MENU = "menu";

    /**
     * Try harvesting the entity on interaction
     */
    public static final String ON_INTERACT_HARVEST = "harvest";

    /**
     * Try opening/closing a door
     */
    public static final String ON_INTERACT_DOOR = "door";

    /**
     * A dialog interaction
     */
    public static final String ON_INTERACT_DIALOG = "dialog";

    /**
     * An inventory interaction
     */
    public static final String ON_INTERACT_INVENTORY = "inventory";
    
    /**
     * The function to run on interaction
     */
    String onInteract;

    /**
     * The window to open on interaction
     */
    String windowTarget;

    /**
     * The data to pass alongside the window
     */
    String windowData;

    /**
     * The collidable shape used for ray casting to pick entities to interact with
     */
    CollidableTemplate interactionShape;

    /**
     * Configures the interaction to be client-side only
     */
    Boolean clientOnly;

    /**
     * Gets the function to run on interaction
     * @return The function to run on interaction
     */
    public String getOnInteract() {
        return onInteract;
    }

    /**
     * Gets the window to open on interaction
     * @return The window to open on interaction
     */
    public String getWindowTarget(){
        return windowTarget;
    }

    /**
     * Gets the template for the shape used to ray cast for interaction targets
     * @return The collidable template
     */
    public CollidableTemplate getInteractionShape(){
        return interactionShape;
    }

    /**
     * Gets the data to pass alongside the window
     * @return the data to pass alongside the window
     */
    public String getWindowData() {
        return windowData;
    }

    

}
