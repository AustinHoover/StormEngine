package electrosphere.net.synchronization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
/**
 * A behavior tree that will have modifications by code generation.
 * It is synchronized between the server and client with auto-generated netcode.
 */
public @interface SynchronizedBehaviorTree {

    //The name of the behavior tree
    public String name() default "";

    //True if this is a server-side behavior tree
    public boolean isServer() default false;

    //The corresponding behavior tree. If this is a server tree, it is the corresponding client tree. If this is a client tree, it is the corresponding server tree
    public String correspondingTree() default "";

    //If true, auto generation tooling will generate start() and interrupt() methods for this tree. SHOULD ONLY BE USED ON CLIENT
    public boolean genStartInt() default false;
    
}
