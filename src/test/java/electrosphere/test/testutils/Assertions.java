package electrosphere.test.testutils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import java.util.function.Supplier;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import electrosphere.engine.Globals;
import electrosphere.engine.Main;

/**
 * Custom assertion macros
 */
public class Assertions {
    
    /**
     * The threshold at which we say the colors are 'close enough'
     */
    static final int COLOR_COMPARE_THRESHOLD = 4;

    /**
     * A very small number for comparisons
     */
    static final double VERY_SMALL_NUMBER = 0.0001;
    
    /**
     * Asserts that the most recent render matches the image stored at the provided filepath
     * @param existingRenderPath The filepath of the existing render
     */
    public static void assertEqualsRender(String existingRenderPath, Runnable onFailure){
        BufferedImage testData = null;
        try {
            testData = ImageIO.read(new File(existingRenderPath));
        } catch (IOException e){
            Main.shutdown();
            fail("Failed to read existing image path " + existingRenderPath);
        }
        BufferedImage screenshot = Globals.renderingEngine.defaultFramebuffer.getPixels(Globals.renderingEngine.getOpenGLState());
        //check basic data
        //
        //width
        if(testData.getWidth() != screenshot.getWidth()){
            onFailure.run();
            Main.shutdown();
        }
        assertEquals(testData.getWidth(), screenshot.getWidth());

        //
        //height
        if(testData.getHeight() != screenshot.getHeight()){
            onFailure.run();
            Main.shutdown();
        }
        assertEquals(testData.getHeight(), screenshot.getHeight());

        //
        //pixel-by-pixel check
        //
        for(int x = 0; x < testData.getWidth(); x++){
            for(int y = 0; y < testData.getHeight(); y++){

                //get from-disk rgba
                int sourceRed = testData.getRGB(x, y) & 0xff;
                int sourceGreen = (testData.getRGB(x, y) & 0xff00) >> 8;
                int sourceBlue = (testData.getRGB(x, y) & 0xff0000) >> 16;
                int sourceAlpha = (testData.getRGB(x, y) & 0xff000000) >>> 24;

                //get from-render rgba
                int renderRed = screenshot.getRGB(x, y) & 0xff;
                int renderGreen = (screenshot.getRGB(x, y) & 0xff00) >> 8;
                int renderBlue = (screenshot.getRGB(x, y) & 0xff0000) >> 16;
                int renderAlpha = (screenshot.getRGB(x, y) & 0xff000000) >>> 24;

                if(
                    Math.abs(sourceRed - renderRed) > COLOR_COMPARE_THRESHOLD ||
                    Math.abs(sourceGreen - renderGreen) > COLOR_COMPARE_THRESHOLD ||
                    Math.abs(sourceBlue - renderBlue) > COLOR_COMPARE_THRESHOLD ||
                    Math.abs(sourceAlpha - renderAlpha) > COLOR_COMPARE_THRESHOLD
                ){

                    onFailure.run();
                    Main.shutdown();
                    String failMessage = "Colors aren't approximately the same!\n" +
                    "Color from disk:   " + sourceRed + "," + sourceGreen + "," + sourceBlue + "," + sourceAlpha + "\n" +
                    "Color from render: " + renderRed + "," + renderGreen + "," + renderBlue + "," + renderAlpha + "\n"
                    ;
                    fail(failMessage);
                }
            }
        }
    }

    /**
     * Asserts that some test is true within a given number of frame simulations
     * @param test The test
     * @param maxFrames The number of frames
     */
    public static void assertEventually(Supplier<Boolean> test, int maxFrames){
        int frameCount = 0;
        boolean testResult = false;
        while(!(testResult = test.get()) && frameCount < maxFrames){
            TestEngineUtils.simulateFrames(1);
            frameCount++;
        }
        org.junit.jupiter.api.Assertions.assertTrue(testResult);
    }

    /**
     * Asserts that some runnable
     * @param test
     */
    public static void assertEventually(Supplier<Boolean> test){
        assertEventually(test, 100);
    }

    /**
     * Asserts that two numbers are very close
     * @param d1
     * @param d2
     */
    public static void assertVeryClose(double d1, double d2){
        if(Math.abs(d1 - d2) < VERY_SMALL_NUMBER){
            assertTrue(true);
        } else {
            fail("Values not close! " + d1 + " vs " + d2);
        }
    }

}
