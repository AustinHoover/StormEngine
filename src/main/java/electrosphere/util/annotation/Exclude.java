package electrosphere.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Used to exclude single fields from a gson serialization in a black list manner.
 * Refer to https://stackoverflow.com/a/27986860/ for reference for why this works and what it is.
 */
public @interface Exclude {
    
}
