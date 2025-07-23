package electrosphere.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A unit test
 */

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("unit")
@Test
public @interface UnitTest {
    
}
