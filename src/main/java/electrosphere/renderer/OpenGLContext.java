package electrosphere.renderer;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;

/**
 * Data about the opengl context (ie, card-defined limits and so on)
 */
public class OpenGLContext {
    
    /**
     * The maximum number of textures supported
     */
    private int maxTextureImageUnits = 0;

    /**
     * The maximum texture size
     */
    private int maxTextureSize = 0;

    /**
     * Constructor
     */
    protected OpenGLContext(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer lookupBuf = stack.ints(1);
            GL45.glGetIntegerv(GL45.GL_MAX_TEXTURE_IMAGE_UNITS, lookupBuf);
            maxTextureImageUnits = lookupBuf.get(0);
            GL45.glGetIntegerv(GL45.GL_MAX_TEXTURE_SIZE,lookupBuf);
            maxTextureSize = lookupBuf.get(0);
        }
    }

    /**
     * Gets GL_MAX_TEXTURE_IMAGE_UNITS
     * @return GL_MAX_TEXTURE_IMAGE_UNITS
     */
    public int getMaxTextureImageUnits(){
        return maxTextureImageUnits;
    }

    /**
     * Gets the max texture size
     * @return The max texture size
     */
    public int getMaxTextureSize(){
        return maxTextureSize;
    }

}
