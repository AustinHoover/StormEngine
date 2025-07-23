package electrosphere.test.template;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.test.template.extensions.StateCleanupCheckerExtension;
import electrosphere.test.template.extensions.UIExtension;

import static electrosphere.test.testutils.Assertions.*;

import electrosphere.test.testutils.TestEngineUtils;
import electrosphere.test.testutils.TestRenderingUtils;

/**
 * A test class that involves testing renders
 */
@Tag("integration")
@Tag("graphical")
@ExtendWith(StateCleanupCheckerExtension.class)
@ExtendWith(UIExtension.class)
public class UITestTemplate {

    /**
     * Checks the most recent render versus an existing image
     * @param renderName The name associated with the render
     * @param existingRenderPath The path to the existing image
     */
    public void checkRender(String renderName, String existingRenderPath){
        //of the format "electrosphere.renderer.ui.elements.WindowTest"
        String canonicalName = this.getClass().getCanonicalName();

        //check the render
        assertEqualsRender(existingRenderPath, () -> {

            //on failure, save the failed render
            String failureSavePath = "./.testcache/" + canonicalName + "-" + renderName + ".png";
            File saveFile = new File(failureSavePath);
            System.err.println("[[ATTACHMENT|" + saveFile.getAbsolutePath() + "]]");
            TestRenderingUtils.saveTestRender(failureSavePath);
        });
    }

    /**
     * Sets up a blank view
     */
    public void setupBlankView(){
        WindowUtils.replaceMainMenuContents(Div.createDiv());
        TestEngineUtils.flush();
        TestEngineUtils.simulateFrames(2);
    }

}
