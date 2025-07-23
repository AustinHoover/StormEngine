package electrosphere.renderer.ui.elements;
import org.junit.jupiter.api.Disabled;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the button ui component
 */
public class ButtonTests extends UITestTemplate {
    
    @Disabled
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        Button button = Button.createButton("test", () -> {});
        WindowUtils.replaceMainMenuContents(button);


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/button1.png");
    }

}
