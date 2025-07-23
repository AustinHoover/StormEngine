package electrosphere.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;


/**
 * Used to flag work that should be done before each integration test
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@BeforeEach
@Tag("integration")
public @interface IntegrationSetup {
    
}
