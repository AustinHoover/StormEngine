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
 * A fast test that does not depend on setup/shutdown -- A unit test
 */

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("fast")
@Test
@Timeout( value = 10, unit = TimeUnit.MILLISECONDS, threadMode = ThreadMode.SAME_THREAD )
public @interface FastTest {
    
}
