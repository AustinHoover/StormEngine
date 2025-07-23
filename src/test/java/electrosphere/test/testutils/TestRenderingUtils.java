package electrosphere.test.testutils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import electrosphere.engine.Globals;

/**
 * Utilities for comparing renders
 */
public class TestRenderingUtils {

    /**
     * Used for saving a copy of the current render (ie for generating test data)
     * @param existingRenderPath The filepath of the existing render
     */
    public static void saveTestRender(String existingRenderPath){
        BufferedImage screenshot = Globals.renderingEngine.defaultFramebuffer.getPixels(Globals.renderingEngine.getOpenGLState());
        try {
            Files.createDirectories(new File(existingRenderPath).toPath());
            ImageIO.write(screenshot, "png", new File(existingRenderPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
