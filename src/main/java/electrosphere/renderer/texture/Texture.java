package electrosphere.renderer.texture;

import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.util.FileUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * A opengl in texture
 */
public class Texture {

    /**
     * Makes sure images are flipped the way opengl expects
     */
    static {
        STBImage.stbi_set_flip_vertically_on_load(true);
    }

    /**
     * Pointer for an uninitialized texture
     */
    public static final int UNINITIALIZED_TEXTURE = -1;

    /**
     * The texture name for the default texture (ie the screen)
     */
    public static final int DEFAULT_TEXTURE = 0;

    /**
     * the pointer for the texture
     */
    private int texturePointer = UNINITIALIZED_TEXTURE;

    /**
     * the width of the texture
     */
    private int width = -1;

    /**
     * the height of the texture
     */
    private int height = -1;

    /**
     * whether the texture has transparency or not
     */
    private boolean hasTransparency;

    /**
     * the path to the texture
     */
    private String path = "";

    /**
     * the border color
     */
    private float[] borderColor = null;

    /**
     * The min filter
     */
    private int minFilter = -1;

    /**
     * The max filter
     */
    private int maxFilter = -1;

    /**
     * the pixel format (ie RGB, ARGB, etc)
     */
    private int pixelFormat = -1;

    /**
     * the data type of a single component of a pixel (IE UNSIGNED_INT, BYTE, etc)
     */
    private int datatype = -1;

    /**
     * Creates a texture with a new opengl texture object
     */
    public Texture(){
        this.texturePointer = GL45.glGenTextures();
        Globals.renderingEngine.checkError();
    }

    /**
     * Creates an in engine texture object from a java bufferedimage object
     * @param bufferedImage The java bufferedimage object
     * @param data The pre-parsed buffer of data from the buffered image
     */
    public Texture(OpenGLState openGlState, BufferedImage bufferedImage, ByteBuffer data){
        this.texturePointer = GL45.glGenTextures();
        Globals.renderingEngine.checkError();
        //bind the new texture
        openGlState.glBindTexture(GL45.GL_TEXTURE_2D, texturePointer);
        //how are we gonna wrap the texture??
        this.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_MIRRORED_REPEAT);
        this.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_MIRRORED_REPEAT);
        //set the border color to black
        this.setBorderColor(openGlState, new float[]{ 0.0f, 0.0f, 0.0f, 1.0f });
        //set magnification and minification operation sampling strategies
        this.setMinFilter(openGlState, GL45.GL_LINEAR);
        this.setMagFilter(openGlState, GL45.GL_LINEAR);
        //load the image here
        BufferedImage image_data = bufferedImage;
        if (
                image_data.getType() == BufferedImage.TYPE_3BYTE_BGR ||
                image_data.getType() == BufferedImage.TYPE_INT_RGB
                ){
            hasTransparency = false;
        } else if(
                image_data.getType() == BufferedImage.TYPE_4BYTE_ABGR ||
                image_data.getType() == BufferedImage.TYPE_INT_ARGB
                ){
            hasTransparency = true;
        }
        width = image_data.getWidth();
        height = image_data.getHeight();
        //call if width != height so opengl figures out how to unpack it properly
        if(width != height){
            GL45.glPixelStorei(GL45.GL_UNPACK_ALIGNMENT, 1);
        }
        //buffer the texture information
        if(hasTransparency){
            this.pixelFormat = GL45.GL_RGBA;
            this.datatype = GL45.GL_UNSIGNED_BYTE;
            this.glTexImage2D(openGlState, width, height, GL45.GL_RGBA, GL45.GL_UNSIGNED_BYTE, data);
        } else {
            this.pixelFormat = GL45.GL_RGB;
            this.datatype = GL45.GL_UNSIGNED_BYTE;
            this.glTexImage2D(openGlState, width, height, GL45.GL_RGB, GL45.GL_UNSIGNED_BYTE, data);
        }
        GL45.glGenerateMipmap(GL45.GL_TEXTURE_2D);
        //check build status
        String errorMessage = RenderingEngine.getErrorInEnglish(Globals.renderingEngine.getError());
        if(errorMessage != null){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Texture Constructor[from bufferedimage]: " + errorMessage));
        }
        openGlState.glBindTexture(GL45.GL_TEXTURE_2D, 0);
    }

    /**
     * Creates a texture from an existing file
     * @param path The path to the image file
     */
    public Texture(OpenGLState openGlState, String path){
        LoggerInterface.loggerRenderer.DEBUG("Create texture " + path);
        if(path == null){
            throw new Error("Path is null");
        }
        if(path.length() == 0){
            throw new Error("Path is empty");
        }
        this.path = path;
        if(!EngineState.EngineFlags.HEADLESS){
            LoggerInterface.loggerRenderer.DEBUG("Setup texture object");
            //generate the texture object on gpu
            this.texturePointer = GL45.glGenTextures();
            Globals.renderingEngine.checkError();
            //bind the new texture
            openGlState.glBindTexture(GL45.GL_TEXTURE_2D, texturePointer);
            //how are we gonna wrap the texture??
            this.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_MIRRORED_REPEAT);
            this.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_MIRRORED_REPEAT);
            //set the border color to black
            this.setBorderColor(openGlState, new float[]{ 0.0f, 0.0f, 0.0f, 1.0f });
            //set magnification and minification operation sampling strategies
            this.setMinFilter(openGlState, GL45.GL_LINEAR);
            this.setMagFilter(openGlState, GL45.GL_LINEAR);

            LoggerInterface.loggerRenderer.DEBUG("Create texture data buffers");
            //load the image here
            ByteBuffer data;
            width = 1;
            height = 1;
            try(MemoryStack stack = MemoryStack.stackPush()) {

                //read
                LoggerInterface.loggerRenderer.DEBUG("Read image");
                IntBuffer xBuf = stack.mallocInt(1);
                IntBuffer yBuf = stack.mallocInt(1);
                IntBuffer chanBuf = stack.mallocInt(1);
                data = STBImage.stbi_load(FileUtils.getAssetFile(path).getAbsolutePath(), xBuf, yBuf, chanBuf, 4);
                if(data == null || data.limit() < 2){
                    throw new IOException("Failed to read " + FileUtils.getAssetFile(path).getAbsolutePath());
                }


                //grab values from read data
                width = xBuf.get();
                height = yBuf.get();
                this.hasTransparency = true;
            } catch (IOException ex) {
                LoggerInterface.loggerRenderer.DEBUG("Failed to read image");
                ex.printStackTrace();
                hasTransparency = false;
                data = BufferUtils.createByteBuffer(3);
                data.put((byte)0);
                data.put((byte)0);
                data.put((byte)0);
            }

            LoggerInterface.loggerRenderer.DEBUG("Flip buffer");
            
            //call if width != height so opengl figures out how to unpack it properly
            if(width != height){
                GL45.glPixelStorei(GL45.GL_UNPACK_ALIGNMENT, 1);
            }

            LoggerInterface.loggerRenderer.DEBUG("Upload texture buffer");
            //buffer the texture information
            if(hasTransparency){
                this.pixelFormat = GL45.GL_RGBA;
                this.datatype = GL45.GL_UNSIGNED_BYTE;
                this.glTexImage2D(openGlState, width, height, GL45.GL_RGBA, GL45.GL_UNSIGNED_BYTE, data);
            } else {
                this.pixelFormat = GL45.GL_RGB;
                this.datatype = GL45.GL_UNSIGNED_BYTE;
                this.glTexImage2D(openGlState, width, height, GL45.GL_RGB, GL45.GL_UNSIGNED_BYTE, data);
            }

            LoggerInterface.loggerRenderer.DEBUG("Generate Mipmap");
            GL45.glGenerateMipmap(GL45.GL_TEXTURE_2D);
            Globals.renderingEngine.checkError();
            //OPTIONAL free the original image data now that it's on the gpu
            // System.gc();
            //check build status
            String errorMessage = RenderingEngine.getErrorInEnglish(Globals.renderingEngine.getError());
            if(errorMessage != null){
                LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Texture Constructor[from bufferedimage]: " + errorMessage));
            }
        }
    }

    /**
     * Generates a texture based on a buffer (for use passing data to gpu)
     * @param buffer The buffer of data
     * @param width the 'width' of the 'texture'
     * @param height the 'height' of the 'texture'
     */
    public Texture(OpenGLState openGlState, ByteBuffer buffer, int width, int height){
        if(!EngineState.EngineFlags.HEADLESS){
            //generate the texture object on gpu
            this.texturePointer = GL45.glGenTextures();
            Globals.renderingEngine.checkError();
            //bind the new texture
            openGlState.glBindTexture(GL45.GL_TEXTURE_2D, texturePointer);
            //how are we gonna wrap the texture??
            this.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_EDGE);
            this.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_EDGE);
            //disable mipmap
            this.setMinFilter(openGlState, GL45.GL_LINEAR);
            //call if width != height so opengl figures out how to unpack it properly
            GL45.glPixelStorei(GL45.GL_UNPACK_ALIGNMENT, 4);
            //GL_RED = 32bit r value
            //buffer the texture information
            this.pixelFormat = GL45.GL_RED;
            this.datatype = GL45.GL_FLOAT;
            this.glTexImage2D(openGlState, GL45.GL_R32F, width, height, GL45.GL_RED, GL45.GL_FLOAT, buffer);
            //check build status
            String errorMessage = RenderingEngine.getErrorInEnglish(Globals.renderingEngine.getError());
            if(errorMessage != null){
                LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Texture Constructor[from bytebuffer]: " + errorMessage));
            }
        }
    }

    /**
     * Generates a texture based on a buffer (for use passing data to gpu)
     * @param openGlState The OpenGL state
     * @param buffer The buffer of data
     * @param width the 'width' of the 'texture'
     * @param height the 'height' of the 'texture'
     */
    public static Texture createBitmap(OpenGLState openGlState, ByteBuffer buffer, int width, int height){
        Texture rVal = null;
        if(!EngineState.EngineFlags.HEADLESS){
            rVal = new Texture();
            //generate the texture object on gpu
            rVal.texturePointer = GL45.glGenTextures();
            Globals.renderingEngine.checkError();
            //bind the new texture
            openGlState.glBindTexture(GL45.GL_TEXTURE_2D, rVal.getTexturePointer());
            //how are we gonna wrap the texture??
            rVal.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_S, GL45.GL_CLAMP_TO_EDGE);
            rVal.setWrap(openGlState, GL45.GL_TEXTURE_WRAP_T, GL45.GL_CLAMP_TO_EDGE);
            //disable mipmap
            rVal.setMinFilter(openGlState, GL45.GL_LINEAR);
            rVal.setMagFilter(openGlState, GL45.GL_LINEAR);
            //call if width != height so opengl figures out how to unpack it properly
            GL45.glPixelStorei(GL45.GL_UNPACK_ALIGNMENT, 4);
            //GL_RED = 32bit r value
            //buffer the texture information
            rVal.pixelFormat = GL45.GL_RED;
            rVal.datatype = GL45.GL_FLOAT;
            rVal.glTexImage2D(openGlState, GL45.GL_RED, width, height, GL45.GL_RED, GL45.GL_UNSIGNED_BYTE, buffer);
            //check build status
            String errorMessage = RenderingEngine.getErrorInEnglish(Globals.renderingEngine.getError());
            if(errorMessage != null){
                LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Texture Constructor[from bytebuffer]: " + errorMessage));
            }
        }
        return rVal;
    }
    
    /**
     * Binds the texture to unit 0
     * @param openGLState The opengl state
     */
    public void bind(OpenGLState openGLState){
        if(texturePointer == -1){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Tring to bind a texture object that has not been initialized yet"));
        }
        if(texturePointer == 0){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Trying to bind texture object that has texturepointer of 0"));
        }
        // openGLState.glActiveTexture(GL_TEXTURE0);
        // openGLState.glBindTexture(GL_TEXTURE_2D, texturePointer);
        openGLState.glBindTextureUnit(GL45.GL_TEXTURE0,this.texturePointer,GL45.GL_TEXTURE_2D);
    }
    
    /**
     * Binds the texture
     * @param openGLState The opengl state
     * @param attrib_val The texture unit number
     */
    public void bind(OpenGLState openGLState, int attrib_val){
        if(texturePointer == -1){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Tring to bind a texture object that has not been initialized yet"));
        }
        if(texturePointer == 0){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException("Trying to bind texture object that has texturepointer of 0"));
        }
        openGLState.glBindTextureUnit(GL45.GL_TEXTURE0 + attrib_val,this.texturePointer,GL45.GL_TEXTURE_2D);
        Globals.renderingEngine.checkError();
        // openGLState.glActiveTexture(GL_TEXTURE0 + attrib_val);
        // openGLState.glBindTexture(GL_TEXTURE_2D, texturePointer);
    }
    
    /**
     * Checks if the texture has transparency or not
     * @return true if transparent, false otherwise
     */
    public boolean isTransparent(){
        return hasTransparency;
    }
    
    /**
     * Gets the path of the texture
     * @return The path
     */
    public String getPath(){
        return path;
    }
    
    /**
     * Gets the pointer of the texture
     * @return The pointer
     */
    public int getTexturePointer(){
        return texturePointer;
    }

    /**
     * Sets the wrap strategy of the texture
     * @param wrapDir The direction to wrap
     * @param wrapType The type of wrapping to perform
     */
    public void setWrap(OpenGLState openGlState, int wrapDir, int wrapType){
        //TODO: store wrap type for the direction in this object
        openGlState.glBindTexture(GL45.GL_TEXTURE_2D,texturePointer);
        Globals.renderingEngine.checkError();
        GL45.glTexParameteri(GL45.GL_TEXTURE_2D, wrapDir, wrapType);
        Globals.renderingEngine.checkError();
    }

    /**
     * Sets the border color
     * @param borderColor The color (must be 4 floats)
     */
    public void setBorderColor(OpenGLState openGlState, float borderColor[]){
        this.borderColor = borderColor;
        openGlState.glBindTexture(GL45.GL_TEXTURE_2D,texturePointer);
        Globals.renderingEngine.checkError();
        GL45.glTexParameterfv(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_BORDER_COLOR, this.borderColor);
        Globals.renderingEngine.checkError();
    }

    /**
     * Sets the min filter
     * @param minFilter The min filter
     */
    public void setMinFilter(OpenGLState openGlState, int minFilter){
        this.minFilter = minFilter;
        openGlState.glBindTexture(GL45.GL_TEXTURE_2D,texturePointer);
        Globals.renderingEngine.checkError();
        GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MIN_FILTER, this.minFilter);
        Globals.renderingEngine.checkError();
    }

    /**
     * Sets the max filter
     * @param maxFilter The max filter
     */
    public void setMagFilter(OpenGLState openGlState, int maxFilter){
        this.maxFilter = maxFilter;
        openGlState.glBindTexture(GL45.GL_TEXTURE_2D,texturePointer);
        Globals.renderingEngine.checkError();
        GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MAG_FILTER, this.maxFilter);
        Globals.renderingEngine.checkError();
    }

    
    /**
     * Specifies a 2d image
     * @param width The width of the image
     * @param height The height of the image
     * @param format The format of the pixels (ie GL_RGB, GL_RGBA, etc)
     * @param datatype The data type of a single component of a pixel (ie GL_BYTE, GL_UNSIGNED_INT, etc)
     */
    public void glTexImage2D(OpenGLState openGLState, int width, int height, int format, int datatype){
        if(width < 1){
            throw new Error("Invalid texture width " + width);
        }
        if(height < 1){
            throw new Error("Invalid texture height " + height);
        }
        //store provided values
        this.width = width;
        this.height = height;
        this.pixelFormat = format;
        this.datatype = datatype;
        int internalFormat = format;
        if(internalFormat == GL45.GL_DEPTH_COMPONENT){
            internalFormat = GL45.GL_DEPTH_COMPONENT24;
        }
        //static values going into call
        int level = 0;
        int border = 0; //this must be 0 according to docs
        openGLState.glBindTexture(GL45.GL_TEXTURE_2D,texturePointer);
        Globals.renderingEngine.checkError();
        GL45.glTexImage2D(GL45.GL_TEXTURE_2D, level, internalFormat, width, height, border, format, datatype, MemoryUtil.NULL);
        Globals.renderingEngine.checkError();
        int[] storage = new int[1];
        int discoveredWidth = 0;
        int discoveredHeight = 0;
        GL45.glGetTexLevelParameteriv(GL45.GL_TEXTURE_2D, 0, GL45.GL_TEXTURE_WIDTH, storage);
        discoveredWidth = storage[0];
        GL45.glGetTexLevelParameteriv(GL45.GL_TEXTURE_2D, 0, GL45.GL_TEXTURE_HEIGHT, storage);
        discoveredHeight = storage[0];
        if(width != discoveredWidth || height != discoveredHeight){
            throw new Error("Found dims aren't the same! " + width + "," + height + " vs " + discoveredWidth + "," + discoveredHeight);
        }
    }

    /**
     * Specifies a 2d image
     * @param width The width of the image
     * @param height The height of the image
     * @param datatype The data type of a single component of a pixel (ie GL_BYTE, GL_UNSIGNED_INT, etc)
     */
    public void glTextureStorage2D(OpenGLState openGLState, int width, int height, int format, int datatype){
        if(width < 1){
            throw new Error("Invalid texture width " + width);
        }
        if(height < 1){
            throw new Error("Invalid texture height " + height);
        }
        //store provided values
        this.width = width;
        this.height = height;
        this.datatype = datatype;
        GL45.glTextureStorage2D(this.texturePointer, 1, datatype, width, height);
        Globals.renderingEngine.checkError();
    }

    /**
     * Specifies a 2d image
     * @param width The width of the image
     * @param height The height of the image
     * @param format The format of the pixels (ie GL_RGB, GL_RGBA, etc)
     * @param datatype The data type of a single component of a pixel (ie GL_BYTE, GL_UNSIGNED_INT, etc)
     * @param data The data to populate the image with
     */
    public void glTexImage2D(OpenGLState openGLState, int width, int height, int format, int datatype, ByteBuffer data){
        if(width < 1){
            throw new Error("Invalid texture width " + width);
        }
        if(height < 1){
            throw new Error("Invalid texture height " + height);
        }
        //store provided values
        this.width = width;
        this.height = height;
        this.pixelFormat = format;
        this.datatype = datatype;
        //static values going into call
        int level = 0;
        int border = 0; //this must be 0 according to docs
        int internalFormat = format;
        openGLState.glBindTexture(GL45.GL_TEXTURE_2D,texturePointer);
        Globals.renderingEngine.checkError();
        GL45.glTexImage2D(GL45.GL_TEXTURE_2D, level, internalFormat, width, height, border, format, datatype, data);
        Globals.renderingEngine.checkError();
    }

    /**
     * Specifies a 2d image
     * @param width The width of the image
     * @param height The height of the image
     * @param format The format of the pixels (ie GL_RGB, GL_RGBA, etc)
     * @param datatype The data type of a single component of a pixel (ie GL_BYTE, GL_UNSIGNED_INT, etc)
     * @param data The data to populate the image with
     */
    public void glTexImage2D(OpenGLState openGLState, int internalFormat, int width, int height, int format, int datatype, ByteBuffer data){
        if(width < 1){
            throw new Error("Invalid texture width " + width);
        }
        if(height < 1){
            throw new Error("Invalid texture height " + height);
        }
        //store provided values
        this.width = width;
        this.height = height;
        this.pixelFormat = format;
        this.datatype = datatype;
        //static values going into call
        int level = 0;
        int border = 0; //this must be 0 according to docs
        openGLState.glBindTexture(GL45.GL_TEXTURE_2D,texturePointer);
        Globals.renderingEngine.checkError();
        GL45.glTexImage2D(GL45.GL_TEXTURE_2D, level, internalFormat, width, height, border, format, datatype, data);
        Globals.renderingEngine.checkError();
    }

    /**
     * Gets the width of the texture
     * @return The width
     */
    public int getWidth(){
        if(width == -1){
            throw new IllegalStateException(
                "The width of the texture you are trying to query from has not been set yet." + 
                " The texture was likely constructed by passing the opengl texture pointer into the texture object."
            );
        }
        return width;
    }

    /**
     * Gets the height of the texture
     * @return The height
     */
    public int getHeight(){
        if(height == -1){
            throw new IllegalStateException(
                "The height of the texture you are trying to query from has not been set yet." + 
                " The texture was likely constructed by passing the opengl texture pointer into the texture object."
            );
        }
        return height;
    }

    /**
     * Gets the format of the pixels
     * @return The format of the pixels (ie GL_RGBA, GL_RGB, etc)
     */
    public int getFormat(){
        if(pixelFormat == -1){
            throw new IllegalStateException(
                "The pixel format of the texture you are trying to query from has not been set yet." + 
                " The texture was likely constructed by passing the opengl texture pointer into the texture object."
            );
        }
        return pixelFormat;
    }

    /**
     * Gets the datatype of the pixels
     * @return The datatype (IE GL_FLOAT, GL_BYTE, etc)
     */
    public int getDataType(){
        if(datatype == -1){
            throw new IllegalStateException(
                "The datatype of the texture you are trying to query from has not been set yet." +
                " The texture was likely constructed by passing the opengl texture pointer into the texture object."
            );
        }
        return datatype;
    }

    /**
     * Gets for errors with the texture
     * @param state The opengl state
     */
    public void checkStatus(OpenGLState state){
        //try bind approach
        this.bind(state);
        int errorCode = Globals.renderingEngine.getError();
        if(errorCode != GL45.GL_NO_ERROR){
            switch(errorCode){
                case GL45.GL_INVALID_VALUE: {
                    if(this.width < 0){
                        LoggerInterface.loggerRenderer.ERROR("Texture has width less than 0", new IllegalStateException("Texture has width less than 0"));
                    }
                    if(this.width > state.getMAX_TEXTURE_WIDTH()){
                        LoggerInterface.loggerRenderer.ERROR("Texture is greater width than environment allows", new IllegalStateException("Texture is greater width than environment allows"));
                    }
                } break;
                case GL45.GL_INVALID_ENUM: {

                } break;
                default: {
                    String message = "Texture undefined error status! " + errorCode;
                    LoggerInterface.loggerRenderer.ERROR(new IllegalStateException(message));
                } break;
            }
        }
        //try dedicated approach
        boolean isTexture = GL45.glIsTexture(this.texturePointer);
        if(!isTexture){
            String message = "Texture is not complete!";
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException(message));
        }
    }

    /**
     * Frees the texture
     */
    public void free(){
        GL45.glDeleteTextures(this.texturePointer);
    }

    @Override
    public String toString(){
        String rVal = "" +
        "Texture[" + 
        "path=\"" + path + "\", " +
        "texturePointer=\"" + texturePointer + "\", " +
        "width=\"" + width + "\", " +
        "height=\"" + height + "\", " +
        "]"
        ;
        return rVal;
    }

}
