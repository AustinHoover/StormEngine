package electrosphere.net.synchronization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
/**
 * A field in a synchronized behavior tree that is synchronized between the server and the client.
 */
public @interface SyncedField {

    //how often should the server broadcast update messages, in frames
    public String updateInterval() default "";

    //the states that this should periodically update under
    //when provided, this field will only send updates from server when the behavior tree has the states listed
    public String[] periodicUpdateStates() default "";

    //when true, the server will bundle the current spatial information of the entity alongside the variable update
    //ie when idle state changes (to idle from not idle), tell the client the position of the entity when this update happened alongside the actual state update
    public boolean updatePositionOnStateChange() default false;

    //Instructs the server to send a state-transition packet instead of an immediate update packet
    //The state transition packet will invoke the transitionBTree function on the client instead of immediately overwriting the state's value
    public boolean serverSendTransitionPacket() default false;

}
