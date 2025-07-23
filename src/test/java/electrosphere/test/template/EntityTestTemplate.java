package electrosphere.test.template;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import electrosphere.test.template.extensions.EntityExtension;
import electrosphere.test.template.extensions.StateCleanupCheckerExtension;

/**
 * Template for writing tests that do stuff with entities in a proper scene
 */
@Tag("integration")
@Tag("graphical")
@Tag("entity")
@ExtendWith(StateCleanupCheckerExtension.class)
@ExtendWith(EntityExtension.class)
public class EntityTestTemplate {

}
