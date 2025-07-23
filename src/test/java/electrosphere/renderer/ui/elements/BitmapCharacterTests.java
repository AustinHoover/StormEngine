package electrosphere.renderer.ui.elements;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.Globals;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the button ui component
 */
public class BitmapCharacterTests extends UITestTemplate {
    
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        BitmapCharacter el = new BitmapCharacter(Globals.fontManager.getFont("default"), 16, 24, 1.0f, 'A');
        WindowUtils.replaceMainMenuContents(el);


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/bitmapchar1.png");
    }

}
