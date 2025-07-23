package electrosphere.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;

/**
 * Flags for work to be done tearing down after running an integration test
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@AfterEach
@Tag("integration")
public @interface IntegrationTeardown {
    
}
