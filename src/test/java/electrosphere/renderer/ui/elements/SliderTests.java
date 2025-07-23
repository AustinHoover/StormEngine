package electrosphere.renderer.ui.elements;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests for a slider element
 */
public class SliderTests extends UITestTemplate  {
    
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        WindowUtils.replaceMainMenuContents(Slider.createSlider((ValueChangeEvent event) -> {
        }));


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/slider1.png");
    }

}
