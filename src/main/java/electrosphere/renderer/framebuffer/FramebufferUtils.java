package electrosphere.renderer.framebuffer;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.pipelines.ShadowMapPipeline;
import electrosphere.renderer.texture.Texture;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;

/**
 * Utilities for framebuffer creation
 */
public class FramebufferUtils {

    public static Texture generateScreenTextureColor(OpenGLState openGLState, int width, int height){
        Texture texture = new Texture();
        texture.bind(openGLState);
        texture.glTexImage2D(openGLState, width, height, GL45.GL_RGB, GL45.GL_UNSIGNED_INT);
        texture.setMinFilter(openGLState, GL45.GL_LINEAR);
        texture.setMagFilter(openGLState, GL45.GL_LINEAR);
        //these make sure the texture actually clamps to the borders of the quad
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_EDGE);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_EDGE);
        texture.setBorderColor(openGLState, new float[]{0,0,0,1});

        //guarantees that the texture object has actually been created (calling gen buffers does not guarantee object creation)
        texture.bind(openGLState);
        openGLState.glBindTexture(GL45.GL_TEXTURE_2D, Texture.DEFAULT_TEXTURE);

        texture.checkStatus(openGLState);
        return texture;
    }

    public static Texture generateScreenTextureColorAlpha(OpenGLState openGLState, int width, int height){
        Texture texture = new Texture();
        texture.bind(openGLState);
        texture.glTexImage2D(openGLState, width, height, GL45.GL_RGBA, GL45.GL_UNSIGNED_INT);
        texture.setMinFilter(openGLState, GL45.GL_LINEAR);
        texture.setMagFilter(openGLState, GL45.GL_LINEAR);
        //these make sure the texture actually clamps to the borders of the quad
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_EDGE);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_EDGE);
        texture.setBorderColor(openGLState, new float[]{0,0,0,1});

        //guarantees that the texture object has actually been created (calling gen buffers does not guarantee object creation)
        texture.bind(openGLState);
        openGLState.glBindTextureUnitForce(GL45.GL_TEXTURE0, Texture.DEFAULT_TEXTURE, GL45.GL_TEXTURE_2D);

        texture.checkStatus(openGLState);
        return texture;
    }

    public static Texture generateScreenTextureDepth(OpenGLState openGLState, int width, int height){
        Texture texture = new Texture();
        texture.bind(openGLState);
        texture.glTexImage2D(openGLState, width, height, GL45.GL_DEPTH_COMPONENT, GL45.GL_FLOAT);

        texture.setMinFilter(openGLState, GL45.GL_LINEAR);
        texture.setMagFilter(openGLState, GL45.GL_LINEAR);
        //these make sure the texture actually clamps to the borders of the quad
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_EDGE);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_EDGE);
        texture.setBorderColor(openGLState, new float[]{0,0,0,1});

        //guarantees that the texture object has actually been created (calling gen buffers does not guarantee object creation)
        texture.bind(openGLState);
        openGLState.glBindTextureUnitForce(GL45.GL_TEXTURE0, Texture.DEFAULT_TEXTURE, GL45.GL_TEXTURE_2D);

        texture.checkStatus(openGLState);
        return texture;
    }
    
    
    public static Framebuffer generateScreenTextureFramebuffer(OpenGLState openGLState, int width, int height, Texture colorTexture, Texture depthTexture) throws Exception {
        Framebuffer buffer = new Framebuffer();
        //bind texture to fbo
        buffer.setMipMapLevel(0);
        buffer.attachTexture(openGLState,colorTexture);
        buffer.setDepthAttachment(openGLState,depthTexture);
        buffer.bind(openGLState);
        //check make sure compiled
        buffer.shouldBeComplete(openGLState);
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
        return buffer;
    }

    public static Framebuffer generateScreenTextureFramebuffer(OpenGLState openGLState, int width, int height, Texture colorTexture) throws Exception {
        Framebuffer buffer = new Framebuffer();
        //bind texture to fbo
        buffer.setMipMapLevel(0);
        buffer.attachTexture(openGLState,colorTexture);
        //check make sure compiled
        buffer.shouldBeComplete(openGLState);
        return buffer;
    }

    public static Framebuffer generateScreensizeTextureFramebuffer(OpenGLState openGLState) throws Exception {
        Framebuffer buffer = new Framebuffer();
        buffer.bind(openGLState);
        //texture
        Texture texture = new Texture();
        texture.glTexImage2D(openGLState, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, GL45.GL_RGB, GL45.GL_UNSIGNED_BYTE);
        texture.setMinFilter(openGLState, GL45.GL_LINEAR);
        texture.setMagFilter(openGLState, GL45.GL_LINEAR);
        //these make sure the texture actually clamps to the borders of the quad
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_EDGE);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_EDGE);
        texture.checkStatus(openGLState);
        //bind texture to fbo
        buffer.setMipMapLevel(0);
        buffer.attachTexture(openGLState,texture);
        //renderbuffer
        int renderBuffer = GL45.glGenRenderbuffers();
        GL45.glBindRenderbuffer(GL45.GL_RENDERBUFFER, renderBuffer);
        Globals.renderingEngine.checkError();
        GL45.glRenderbufferStorage(GL45.GL_RENDERBUFFER, GL45.GL_DEPTH24_STENCIL8, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT); 
        Globals.renderingEngine.checkError();
        //bind rbo to fbo
        GL45.glFramebufferRenderbuffer(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_STENCIL_ATTACHMENT, GL45.GL_RENDERBUFFER, renderBuffer);
        Globals.renderingEngine.checkError();
        //check make sure compiled
        buffer.shouldBeComplete(openGLState);
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
        return buffer;
    }
    
    /**
     * Creates a texture framebuffer
     * @param openGLState The opengl engine state
     * @param width The width of the texture
     * @param height The height of the texture
     * @return The texture
     * @throws Exception Thrown if the framebuffer fails to initialize
     */
    public static Framebuffer generateTextureFramebuffer(OpenGLState openGLState, int width, int height) throws Exception {
        Framebuffer buffer = new Framebuffer();
        buffer.bind(openGLState);
        //texture
        Texture texture = new Texture();
        texture.glTexImage2D(openGLState, width, height, GL45.GL_RGBA, GL45.GL_UNSIGNED_BYTE);
        texture.setMinFilter(openGLState, GL45.GL_LINEAR);
        texture.setMagFilter(openGLState, GL45.GL_LINEAR);
        //these make sure the texture actually clamps to the borders of the quad
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_EDGE);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_EDGE);
        texture.checkStatus(openGLState);
        //bind texture to fbo
        buffer.setMipMapLevel(0);
        buffer.attachTexture(openGLState,texture);
        //renderbuffer
        int renderBuffer = GL45.glGenRenderbuffers();
        GL45.glBindRenderbuffer(GL45.GL_RENDERBUFFER, renderBuffer);
        Globals.renderingEngine.checkError();
        GL45.glRenderbufferStorage(GL45.GL_RENDERBUFFER, GL45.GL_DEPTH24_STENCIL8, width, height); 
        Globals.renderingEngine.checkError();
        //bind rbo to fbo
        buffer.bind(openGLState);
        GL45.glFramebufferRenderbuffer(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_STENCIL_ATTACHMENT, GL45.GL_RENDERBUFFER, renderBuffer);
        Globals.renderingEngine.checkError();
        //check make sure compiled
        buffer.shouldBeComplete(openGLState);
        //re-bind default buffer
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
        return buffer;
    }
    
    
    
    
    
    public static Renderbuffer generateScreensizeStencilDepthRenderbuffer(OpenGLState openGLState){
        Renderbuffer buffer = new Renderbuffer();
        buffer.bind();
        GL45.glRenderbufferStorage(GL45.GL_RENDERBUFFER, GL45.GL_DEPTH24_STENCIL8, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        Globals.renderingEngine.checkError();
        return buffer;
    }
    
    
    public static Framebuffer generateDepthBuffer(OpenGLState openGLState) throws Exception {
        Framebuffer buffer = new Framebuffer();
        buffer.bind(openGLState);
        
        
        //texture
        Texture texture = new Texture();
        texture.glTexImage2D(openGLState, ShadowMapPipeline.SHADOW_MAP_RESOLUTION, ShadowMapPipeline.SHADOW_MAP_RESOLUTION, GL45.GL_DEPTH_COMPONENT, GL45.GL_FLOAT);
        texture.setMinFilter(openGLState, GL45.GL_NEAREST);
        texture.setMagFilter(openGLState, GL45.GL_NEAREST);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_BORDER);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_BORDER);
        texture.setBorderColor(openGLState, new float[]{ 1.0f, 1.0f, 1.0f, 1.0f });
        texture.checkStatus(openGLState);

        //bind texture to fbo
        buffer.setMipMapLevel(0);
        buffer.setDepthAttachment(openGLState,texture);
        GL45.glNamedFramebufferDrawBuffer(buffer.getFramebufferPointer(), GL45.GL_NONE);
        Globals.renderingEngine.checkError();
        GL45.glNamedFramebufferReadBuffer(buffer.getFramebufferPointer(), GL45.GL_NONE);
        Globals.renderingEngine.checkError();
        
        
        
        //check make sure compiled
        buffer.shouldBeComplete(openGLState);
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
        return buffer;
    }


    public static Texture generateDepthBufferTexture(OpenGLState openGLState, int width, int height){
        Texture texture = new Texture();
        texture.glTexImage2D(openGLState, width, height, GL45.GL_DEPTH_COMPONENT, GL45.GL_SHORT);
        texture.setMinFilter(openGLState, GL45.GL_NEAREST);
        texture.setMagFilter(openGLState, GL45.GL_NEAREST);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_BORDER);
        texture.setWrap(openGLState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_BORDER);
        texture.setBorderColor(openGLState, new float[]{ 1.0f, 1.0f, 1.0f, 1.0f });
        texture.checkStatus(openGLState);
        return texture;
    }

    public static Framebuffer generateDepthBuffer(OpenGLState openGLState, int width, int height, Texture texture) throws Exception {
        Framebuffer buffer = new Framebuffer();
        buffer.bind(openGLState);
        
        
        //bind texture to fbo
        buffer.setMipMapLevel(0);
        buffer.setDepthAttachment(openGLState,texture);
        GL45.glNamedFramebufferDrawBuffer(buffer.getFramebufferPointer(), GL45.GL_NONE);
        Globals.renderingEngine.checkError();
        GL45.glNamedFramebufferReadBuffer(buffer.getFramebufferPointer(), GL45.GL_NONE);
        Globals.renderingEngine.checkError();
        
        
        
        //check make sure compiled
        buffer.shouldBeComplete(openGLState);
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
        return buffer;
    }

    public static Texture generateOITAccumulatorTexture(OpenGLState openGLState, int width, int height){
        Texture texture = new Texture();
        texture.setMinFilter(openGLState, GL45.GL_LINEAR);
        texture.setMagFilter(openGLState, GL45.GL_LINEAR);
        texture.glTexImage2D(openGLState, width, height, GL45.GL_RGBA, GL45.GL_HALF_FLOAT);
        texture.checkStatus(openGLState);
        return texture;
    }

    public static Texture generateOITRevealageTexture(OpenGLState openGLState, int width, int height){
        Texture texture = new Texture();
        texture.setMinFilter(openGLState, GL45.GL_LINEAR);
        texture.setMagFilter(openGLState, GL45.GL_LINEAR);
        texture.glTexImage2D(openGLState, width, height, GL45.GL_RED, GL45.GL_FLOAT);
        texture.checkStatus(openGLState);
        return texture;
    }

    public static Framebuffer generateOITFramebuffer(OpenGLState openGLState, int width, int height, Texture accumulatorTex, Texture revealageTex, Texture depthTexture) throws Exception {
        Framebuffer buffer = new Framebuffer();
        buffer.bind(openGLState);
        
                
        //bind texture to fbo
        buffer.setMipMapLevel(0);
        buffer.attachTexture(openGLState,accumulatorTex,0);
        buffer.attachTexture(openGLState,revealageTex,1);
        buffer.setDepthAttachment(openGLState,depthTexture);
        
        // const GLenum transparentDrawBuffers[] = { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1 };
        // glDrawBuffers(2, transparentDrawBuffers);
        IntBuffer drawBuffers = BufferUtils.createIntBuffer(2);
        drawBuffers.put(GL45.GL_COLOR_ATTACHMENT0);
        drawBuffers.put(GL45.GL_COLOR_ATTACHMENT1);
        drawBuffers.flip();
        GL45.glNamedFramebufferDrawBuffers(buffer.getFramebufferPointer(),drawBuffers);
        Globals.renderingEngine.checkError();

        //check make sure compiled
        buffer.shouldBeComplete(openGLState);
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
        return buffer;
    }


}
