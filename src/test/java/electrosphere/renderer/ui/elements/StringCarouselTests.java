package electrosphere.renderer.ui.elements;

import java.util.Arrays;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Unit tests for a string carousel
 */
public class StringCarouselTests extends UITestTemplate  {
    
    @IntegrationTest
    public void test_Create(){
        //setup
        this.setupBlankView();
        WindowUtils.replaceMainMenuContents(StringCarousel.create(Arrays.asList(new String[]{"Test"}), (ValueChangeEvent event) -> {
        }));


        //wait for ui updates
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/stringcarousel1.png");
    }

}
