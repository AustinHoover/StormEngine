package electrosphere.net.synchronization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
/**
 * Annotation that delineates an enum that should be synchronized over the network
 */
public @interface SynchronizableEnum {
    
}
