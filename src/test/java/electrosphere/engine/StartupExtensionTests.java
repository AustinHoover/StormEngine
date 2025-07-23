package electrosphere.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.extension.ExtendWith;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.extensions.EntityExtension;
import electrosphere.test.template.extensions.StateCleanupCheckerExtension;

/**
 * Tests for startup extensions
 */
@ExtendWith(StateCleanupCheckerExtension.class)
public class StartupExtensionTests {
    
    @IntegrationTest
    public void test_EntityExtension_Startup(){
        assertDoesNotThrow(() -> {
            EntityExtension ext = new EntityExtension();
            ext.beforeEach(null);
            ext.afterEach(null);
        });
    }

    @IntegrationTest
    public void test_EntityExtension_RepeatStartup(){
        assertDoesNotThrow(() -> {
            EntityExtension ext = new EntityExtension();
            int someNumberOfTests = 3;
            for(int i = 0; i < someNumberOfTests; i++){
                ext.beforeEach(null);
                ext.afterEach(null);
            }
        });
    }

}
