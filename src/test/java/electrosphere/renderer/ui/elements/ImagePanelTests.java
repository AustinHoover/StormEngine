package electrosphere.renderer.ui.elements;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the image panel ui component
 */
public class ImagePanelTests extends UITestTemplate {
    
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        WindowUtils.replaceMainMenuContents(ImagePanel.createImagePanelAbsolute(0,0,50,50,"Textures/default_diffuse.png"));


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/imagepanel1.png");
    }

    @IntegrationTest
    public void test_CreateInsideScrollable(){
        //setup
        this.setupBlankView();
        ScrollableContainer container = ScrollableContainer.createScrollable();
        container.addChild(ImagePanel.createImagePanelAbsolute(0,0,50,50,"Textures/default_diffuse.png"));
        WindowUtils.replaceMainMenuContents(container);


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/imagepanel1.png");
    }

}
