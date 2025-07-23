package electrosphere.renderer.ui.elements;

import org.junit.jupiter.api.Disabled;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the text input ui component
 */
public class TestInputTests extends UITestTemplate {
    
    @Disabled
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        TextInput input = TextInput.createTextInput();
        input.setText("asdf");
        WindowUtils.replaceMainMenuContents(input);


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/textinput1.png");
    }

}
