package electrosphere.renderer.buffer;

import org.lwjgl.opengl.GL45;

/**
 * An opengl buffer
 */
public interface OpenGLBuffer {

    /**
     * The type of the buffer
     */
    public static enum BufferType {
        /**
         * An array buffer
         */
        GL_ARRAY_BUFFER,

        /**
         * A storage buffer
         */
        GL_SHADER_STORAGE_BUFFER,
    }
    
    /**
     * Gets the id of the buffer on opengl side
     * @return The id
     */
    public int getId();

    /**
     * Gets the type of the buffer
     * @return The type
     */
    public BufferType getType();

    /**
     * Gets the int representing the buffer type
     * @param buffer The buffer
     */
    public static int getTypeInt(OpenGLBuffer buffer){
        if(buffer == null){
            throw new IllegalArgumentException("Passed null buffer into getTypeInt! " + buffer);
        }
        switch(buffer.getType()){
            case GL_ARRAY_BUFFER:
                return GL45.GL_ARRAY_BUFFER;
            case GL_SHADER_STORAGE_BUFFER:
                return GL45.GL_SHADER_STORAGE_BUFFER;
        }
        throw new IllegalStateException("Somehow reached unreachable code! " + buffer + " " + buffer.getType());
    }

}
