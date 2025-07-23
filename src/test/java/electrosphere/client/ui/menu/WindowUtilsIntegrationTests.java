package electrosphere.client.ui.menu;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import electrosphere.engine.Globals;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Integration tests for WindowUtils
 */
public class WindowUtilsIntegrationTests extends EntityTestTemplate {
    
    /**
     * Make sure item drop event capture window size accounts for whole screen
     */
    @IntegrationTest
    public void test_ItemDropWindow_size(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        Window window = (Window)Globals.elementService.getWindow(WindowStrings.WINDDOW_ITEM_DROP);
        Div captureDiv = (Div)window.getChildren().get(0);

        assertNotEquals(captureDiv.getWidth(),0);
        assertNotEquals(captureDiv.getHeight(),0);
    }

}
