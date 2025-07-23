package electrosphere.renderer.framebuffer;

import java.awt.image.BufferedImage;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.texture.Texture;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;

/**
 * Framebuffer object
 */
public class Framebuffer {

    /**
     * The default framebuffer's pointer
     */
    public static final int DEFAULT_FRAMEBUFFER_POINTER = 0;
    
    /**
     * the pointer to the framebuffer
     */
    private int framebufferPointer;

    /**
     * the mipmap level
     */
    private int mipMap = -1;

    /**
     * the map of attachment point to texture object
     */
    private Map<Integer,Texture> attachTextureMap = new HashMap<Integer,Texture>();

    /**
     * attached texture
     */
    private Texture texture;

    /**
     * the depth texture for the framebuffer
     */
    private Texture depthTexture;
    
    /**
     * Creates a framebuffer
     */
    public Framebuffer(){
        this.framebufferPointer = GL45.glGenFramebuffers();
        Globals.renderingEngine.checkError();
    }

    /**
     * Creates a framebuffer with a predefined id
     * @param framebufferId The predefined framebuffer id
     */
    public Framebuffer(int framebufferId){
        this.framebufferPointer = framebufferId;
    }

    /**
     * Sets the texture attached to the framebuffer
     * @param texture The texture attached to the framebuffer
     */
    public void setTexture(Texture texture){
        this.texture = texture;
    }

    /**
     * Gets the texture attached to this framebuffer
     * @return The texture
     */
    public Texture getTexture(){
        return texture;
    }

    /**
     * Gets the depth texture attached to this framebuffer
     * @return The depth texture
     */
    public Texture getDepthTexture(){
        return depthTexture;
    }
    
    /**
     * Binds the framebuffer
     * @param openGLState The opengl state
     */
    public void bind(OpenGLState openGLState){
        openGLState.glBindFramebuffer(GL45.GL_FRAMEBUFFER, this.framebufferPointer);
    }
    
    /**
     * Checks if the framebuffer compiled correctly
     * @return true if compiled correctly, false otherwise
     */
    public boolean isComplete(OpenGLState openGLState){
        if(this.framebufferPointer == DEFAULT_FRAMEBUFFER_POINTER){
            throw new Error("Pointer is the default framebuffer!");
        }
        return GL45.glCheckNamedFramebufferStatus(this.framebufferPointer,GL45.GL_FRAMEBUFFER) == GL45.GL_FRAMEBUFFER_COMPLETE;
    }

    
    /**
     * Checks the status of the framebuffer
     * @param openGLState The opengl state
     * @throws Exception 
     */
    public void shouldBeComplete(OpenGLState openGLState) throws Exception{
        if(!this.isComplete(openGLState)){
            int colorAttach0 = GL45.glGetFramebufferAttachmentParameteri(GL45.GL_FRAMEBUFFER, GL45.GL_COLOR_ATTACHMENT0, GL45.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            String attach0Type = "";
            switch(colorAttach0){
                case GL45.GL_NONE: {
                    attach0Type = "GL_NONE";
                } break;
                case  GL45.GL_TEXTURE: {
                    attach0Type = " GL_TEXTURE";
                } break;
            }
            this.texture.bind(openGLState);
            int[] storage = new int[1];
            int width = 0;
            int height = 0;
            GL45.glGetTexLevelParameteriv(GL45.GL_TEXTURE_2D, 0, GL45.GL_TEXTURE_WIDTH, storage);
            width = storage[0];
            GL45.glGetTexLevelParameteriv(GL45.GL_TEXTURE_2D, 0, GL45.GL_TEXTURE_HEIGHT, storage);
            height = storage[0];
            String message = "Framebuffer failed to build.\n" +
            "Framebuffer [status] - " + this.getStatus() + "\n" +
            "Texture: " + this.texture + "\n" +
            "attach0Type: " + attach0Type + "\n" +
            "attach0 Dims: " + width + "," + height + "\n" +
            "Depth: " + this.depthTexture + "\n"
            ;
            throw new Exception(message);
        }
    }

    /**
     * Gets the framebuffer's pointer
     * @return The framebuffer's pointer
     */
    public int getFramebufferPointer(){
        return framebufferPointer;
    }
    
    /**
     * Frees the framebuffer
     */
    public void free(){
        GL45.glDeleteFramebuffers(framebufferPointer);
    }
    
    /**
     * Blocks the thread until the framebuffer has compiled
     */
    public void blockUntilCompiled(){
        while(GL45.glCheckNamedFramebufferStatus(this.framebufferPointer,GL45.GL_FRAMEBUFFER) != GL45.GL_FRAMEBUFFER_UNDEFINED){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ex) {
                LoggerInterface.loggerEngine.ERROR("Failed to sleep in framebuffer blocker", ex);
            }
        }
    }

    /**
     * Gets the status of the framebuffer
     * @return The status
     */
    public String getStatus(){
        switch(GL45.glCheckNamedFramebufferStatus(this.framebufferPointer,GL45.GL_FRAMEBUFFER)){
            case GL45.GL_FRAMEBUFFER_UNDEFINED: {
                return "The specified framebuffer is the default read or draw framebuffer, but the default framebuffer does not exist.";
            }
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT: {
                return "Any of the framebuffer attachment points are framebuffer incomplete.";
            }
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: {
                return "The framebuffer does not have at least one image attached to it.";
            }
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER: {
                return "The value of GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE is GL_NONE for any color attachment point(s) named by GL_DRAW_BUFFERi.";
            }
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER: {
                return "GL_READ_BUFFER is not GL_NONE and the value of GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE is GL_NONE for the color attachment point named by GL_READ_BUFFER.";
            }
            case GL45.GL_FRAMEBUFFER_UNSUPPORTED: {
                return "The combination of internal formats of the attached images violates an implementation-dependent set of restrictions.";
            }
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE: {
                return "The value of GL_RENDERBUFFER_SAMPLES is not the same for all attached renderbuffers; if the value of GL_TEXTURE_SAMPLES is the not same for all attached textures; or, if the attached images are a mix of renderbuffers and textures, the value of GL_RENDERBUFFER_SAMPLES does not match the value of GL_TEXTURE_SAMPLES.";
            }
            case 0: {
                return RenderingEngine.getErrorInEnglish(Globals.renderingEngine.getError());
            }
        }
        return "Unknown framebuffer status";
    }

    /**
     * Sets the mipmap level
     * @param mipMap The mipmap level
     */
    public void setMipMapLevel(int mipMap){
        this.mipMap = mipMap;
    }

    /**
     * Attaches a color texture to the default texture unit
     * @param openGLState The opengl state
     * @param texture The texture
     */
    public void attachTexture(OpenGLState openGLState, Texture texture){
        attachTexture(openGLState, texture, 0);
    }

    /**
     * Attaches a texture to the framebuffer
     * @param openGLState The opengl state
     * @param texture The texture
     * @param attachmentNum The texture unit to attach to
     */
    public void attachTexture(OpenGLState openGLState, Texture texture, int attachmentNum){
        this.attachTextureMap.put(attachmentNum,texture);
        this.texture = texture;
        if(this.mipMap < 0){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Trying to attach a texture to a framebuffer where mipmap hasn't been set."));
        }
        if(this.framebufferPointer == Framebuffer.DEFAULT_FRAMEBUFFER_POINTER){
            throw new IllegalStateException("Trying to attach image to default frame buffer!");
        }
        if(texture.getTexturePointer() == Texture.UNINITIALIZED_TEXTURE){
            throw new IllegalStateException("Trying to attach uninitialized image to frame buffer!");
        }
        if(!GL45.glIsTexture(texture.getTexturePointer())){
            throw new IllegalStateException("Tried to attach incomplete texture to framebuffer!");
        }
        openGLState.glBindFramebuffer(GL45.GL_FRAMEBUFFER, this.framebufferPointer);
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_COLOR_ATTACHMENT0 + attachmentNum, GL45.GL_TEXTURE_2D, texture.getTexturePointer(), 0);
        Globals.renderingEngine.checkError();
        // check the attachment slot
        int colorAttach0 = GL45.glGetFramebufferAttachmentParameteri(GL45.GL_FRAMEBUFFER, GL45.GL_COLOR_ATTACHMENT0 + attachmentNum, GL45.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
        switch(colorAttach0){
            case GL45.GL_NONE: {
                throw new Error("Failed to attach!");
            }
            case  GL45.GL_TEXTURE: {
            } break;
        }
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
    }

    /**
     * Sets the depth attachment for the framebuffer
     * @param openGLState The opengl state
     * @param texturePointer The depth attachment's pointer
     */
    public void setDepthAttachment(OpenGLState openGLState, Texture depthTexture){
        if(this.framebufferPointer == Framebuffer.DEFAULT_FRAMEBUFFER_POINTER){
            throw new IllegalStateException("Trying to attach image to default frame buffer!");
        }
        if(depthTexture.getTexturePointer() == Texture.UNINITIALIZED_TEXTURE){
            throw new IllegalStateException("Trying to attach uninitialized image to frame buffer!");
        }
        if(!GL45.glIsTexture(depthTexture.getTexturePointer())){
            throw new IllegalStateException("Tried to attach incomplete texture to framebuffer!");
        }
        this.depthTexture = depthTexture;
        openGLState.glBindFramebuffer(GL45.GL_FRAMEBUFFER, this.framebufferPointer);
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_ATTACHMENT, GL45.GL_TEXTURE_2D, this.depthTexture.getTexturePointer(), 0);
        Globals.renderingEngine.checkError();
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);
    }

    /**
     * Gets the pixels currently stored in this framebuffer
     */
    public BufferedImage getPixels(OpenGLState openGLState){
        BufferedImage rVal = null;
        int offsetX = 0;
        int offsetY = 0;
        int width = openGLState.getViewport().x;
        int height = openGLState.getViewport().y;

        //the formats we want opengl to return with
        int pixelFormat = GL45.GL_RGBA;
        int type = GL45.GL_UNSIGNED_BYTE;


        this.bind(openGLState);
        if(this.framebufferPointer == Framebuffer.DEFAULT_FRAMEBUFFER_POINTER){
            //this is the default framebuffer, read from backbuffer because it is default
            GL45.glReadBuffer(GL45.GL_BACK);

            //set viewport
            openGLState.glViewport(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        } else if(attachTextureMap.containsKey(0)){
            //this is NOT the default framebuffer, read from the first color attachment
            GL45.glReadBuffer(GL45.GL_COLOR_ATTACHMENT0);

            //set viewport
            Texture texture = attachTextureMap.get(0);
            width = texture.getWidth();
            height = texture.getHeight();
            openGLState.glViewport(width, height);
        } else {
            LoggerInterface.loggerRenderer.ERROR(new Error("Tried to get pixels from a framebuffer that does not have a texture attached to attachment point 0."));
        }

        //error check
        if(width < 1){
            throw new Error("Invalid width! " + width);
        }
        if(height < 1){
            throw new Error("Invalid height! " + height);
        }

        //get pixel data
        try {
            int bytesPerPixel = Framebuffer.pixelFormatToBytes(pixelFormat,type);
            int bufferSize = width * height * bytesPerPixel;
            ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize);
            if(buffer == null || buffer.limit() < bufferSize){
                throw new Error("Failed to create buffer!");
            }
            GL45.glReadPixels(offsetX, offsetY, width, height, pixelFormat, type, buffer);
            Globals.renderingEngine.checkError();
            //convert to a buffered images
            rVal = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    int i = (x + (width * y)) * bytesPerPixel;
                    int red = buffer.get(i) & 0xFF;
                    int green = buffer.get(i + 1) & 0xFF;
                    int blue = buffer.get(i + 2) & 0xFF;
                    int alpha = 255;
                    if(pixelFormat == GL45.GL_RGBA){
                        alpha = buffer.get(i + 3) & 0xFF;
                    }
                    rVal.setRGB(x, height - (y + 1), (alpha << 24) | (red << 16) | (green << 8) | blue);
                }
            }
        } catch (OutOfMemoryError e){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException(e.getMessage()));
        }
        return rVal;
    }

    /**
     * Gets the number of bytes per pixel based on the format and type of pixel
     * @param format The format of the pixels
     * @param type The datatype of as single component of the pixel
     * @return The number of bytes
     */
    private static int pixelFormatToBytes(int format, int type){
        int multiplier = 1;
        switch(format){
            case GL45.GL_RGBA: {
                multiplier = 4;
            } break;
            case GL45.GL_RGB: {
                multiplier = 3;
            } break;
            default: {
                LoggerInterface.loggerRenderer.WARNING("Trying to export framebuffer that has image of unsupported pixel format");
            } break;
        }
        return multiplier;
    }

    /**
     * Gets the width of a framebuffer
     * @return The width
     */
    public int getWidth(){
        if(texture != null){
            return texture.getWidth();
        }
        if(this.framebufferPointer == DEFAULT_FRAMEBUFFER_POINTER){
            return Globals.WINDOW_WIDTH;
        }
        throw new Error("Calling getWidth on framebuffer with no texture assigned!");
    }

    /**
     * Gets the height of a framebuffer
     * @return The height
     */
    public int getHeight(){
        if(texture != null){
            return texture.getHeight();
        }
        if(this.framebufferPointer == DEFAULT_FRAMEBUFFER_POINTER){
            return Globals.WINDOW_HEIGHT;
        }
        throw new Error("Calling getHeight on framebuffer with no texture assigned!");
    }

}
