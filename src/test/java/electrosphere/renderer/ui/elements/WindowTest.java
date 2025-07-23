package electrosphere.renderer.ui.elements;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.test.template.UITestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests for the window class
 */
public class WindowTest extends UITestTemplate {

    /**
     * Tests creating a window
     */
    @IntegrationTest
    public void testCreateWindow(){
        //create ui testing window
        TestEngineUtils.simulateFrames(1);
        Window window = Window.create(Globals.renderingEngine.getOpenGLState(),50,50,500,500,true);
        window.setParentAlignItem(YogaAlignment.Center);
        window.setParentJustifyContent(YogaJustification.Center);
        WindowUtils.replaceWindow(WindowStrings.WINDOW_MENU_MAIN, window);

        //wait for ui updates
        TestEngineUtils.flush();

        TestEngineUtils.simulateFrames(1);
        
        this.checkRender("Basic", "./test/java/renderer/ui/elements/window1.png");
    }

}
