package electrosphere.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

/**
 * An integration test. Has defaults for timeout on the test
 */

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("integration")
@Test
@Timeout( value = 2, unit = TimeUnit.MINUTES, threadMode = ThreadMode.SAME_THREAD )
public @interface IntegrationTest {
    
}
