package electrosphere.renderer.framebuffer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import electrosphere.engine.Globals;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.texture.Texture;
import electrosphere.test.annotations.UnitTest;
import electrosphere.test.template.RenderingTestTemplate;

/**
 * Tests for framebuffer creation utilities
 */
public class FramebufferUtilsTests extends RenderingTestTemplate {
    
    @UnitTest
    public void testCreateScreenFramebuffer(){
        assertDoesNotThrow(() -> {
            Texture screenTextureColor = FramebufferUtils.generateScreenTextureColorAlpha(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            RenderingEngine.screenTextureColor = screenTextureColor;
            Texture screenTextureDepth = FramebufferUtils.generateScreenTextureDepth(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            RenderingEngine.screenTextureDepth = screenTextureDepth;
            Framebuffer screenFramebuffer = FramebufferUtils.generateScreenTextureFramebuffer(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, screenTextureColor, screenTextureDepth);
            RenderingEngine.screenFramebuffer = screenFramebuffer;
        });
    }

    @UnitTest
    public void testCreateScreenFramebufferRepeat(){
        assertDoesNotThrow(() -> {
            Globals.initGlobals();
            Globals.renderingEngine = new RenderingEngine();
            Globals.renderingEngine.createOpenglContext();
            Texture screenTextureColor = FramebufferUtils.generateScreenTextureColorAlpha(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            Texture screenTextureDepth = FramebufferUtils.generateScreenTextureDepth(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            FramebufferUtils.generateScreenTextureFramebuffer(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, screenTextureColor, screenTextureDepth);
            Globals.renderingEngine.destroy();
            Globals.resetGlobals();

            Globals.initGlobals();
            Globals.renderingEngine = new RenderingEngine();
            Globals.renderingEngine.createOpenglContext();
            screenTextureColor = FramebufferUtils.generateScreenTextureColorAlpha(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            screenTextureDepth = FramebufferUtils.generateScreenTextureDepth(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
            FramebufferUtils.generateScreenTextureFramebuffer(Globals.renderingEngine.getOpenGLState(), Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, screenTextureColor, screenTextureDepth);
        });
    }

}
