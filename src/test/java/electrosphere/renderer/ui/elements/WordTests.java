package electrosphere.renderer.ui.elements;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the text box ui component
 */
public class WordTests extends UITestTemplate {
    
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        WindowUtils.replaceMainMenuContents(Word.createWord("some word"));


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/word1.png");
    }

}
