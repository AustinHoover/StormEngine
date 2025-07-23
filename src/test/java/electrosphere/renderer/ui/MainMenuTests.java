package electrosphere.renderer.ui;

import org.junit.jupiter.api.Disabled;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.MenuGeneratorsUITesting;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests rendering the main menu
 */
public class MainMenuTests extends UITestTemplate {

    /**
     * Tests creating a window
     */
    @IntegrationTest
    @Disabled
    public void test_UITestWindow_Create(){
        //create ui testing window
        TestEngineUtils.simulateFrames(1);
        WindowUtils.replaceMainMenuContents(MenuGeneratorsUITesting.createUITestMenu());

        //wait for ui updates
        TestEngineUtils.flush();

        TestEngineUtils.simulateFrames(3);
        
        // TestRenderingUtils.saveTestRender("./test/java/renderer/ui/elements/window.png");
        this.checkRender("Basic", "./test/java/renderer/ui/uitest.png");
    }
    
}
