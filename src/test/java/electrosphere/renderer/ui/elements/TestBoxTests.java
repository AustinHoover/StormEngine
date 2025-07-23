package electrosphere.renderer.ui.elements;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the text box ui component
 */
public class TestBoxTests extends UITestTemplate {
    
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        WindowUtils.replaceMainMenuContents(TextBox.createTextBox("test text", false));


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/textbox1.png");
    }

}
