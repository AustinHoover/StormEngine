package electrosphere.renderer.shader;

import electrosphere.renderer.OpenGLState;

/**
 * Interface for all shader types
 */
public interface Shader {

    /**
     * ID to unbind a shader
     */
    public static final int UNBIND_SHADER_ID = 0;

    /**
     * Returned if the uniform isn't found
     */
    public static final int INVALID_UNIFORM_NAME = -1;
    
    /**
     * Gets the id for the shader program
     * @return The id
     */
    public int getId();

    /**
     * Sets the value of a uniform on the shader
     * @param openGLState The opengl state object
     * @param uniformName The uniform's name
     * @param value The value of the uniform
     */
    public void setUniform(OpenGLState openGLState, String uniformName, Object value);

}
