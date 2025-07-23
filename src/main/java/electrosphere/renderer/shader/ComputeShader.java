package electrosphere.renderer.shader;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderingEngine;

/**
 * A compute shader program (leaving object called "ComputeShader" because that's how most people refer to them)
 */
public class ComputeShader implements Shader {

    /**
     * The default local size
     */
    public static final int DEFAULT_LOCAL_SIZE = 1;
    
    /**
     * The id for the shader
     */
    private int programId;

    /**
     * Map of uniform index -> uniform value
     */
    public Map<Integer,Object> uniformMap = new HashMap<Integer,Object>();

    /**
     * keeps track of programs that have already been compiled and returns them instead of recompiling from scratch
     */
    static Map<String,VisualShader> alreadyCompiledMap = new HashMap<String,VisualShader>();

    /**
     * Creates a compute shader
     * @param source The source code for the shader
     * @return The shader object
     */
    public static ComputeShader create(String source){
        ComputeShader rVal = new ComputeShader();

        //create shader object
        int shaderId = GL45.glCreateShader(GL45.GL_COMPUTE_SHADER);
        Globals.renderingEngine.checkError();

        //attach source
        GL45.glShaderSource(shaderId, source);
        Globals.renderingEngine.checkError();

        //compile shader
        GL45.glCompileShader(shaderId);
        int success;
        success = GL45.glGetShaderi(shaderId, GL45.GL_COMPILE_STATUS);
        if (success != GL45.GL_TRUE) {
            LoggerInterface.loggerRenderer.WARNING("Compute Shader failed to compile!");
            LoggerInterface.loggerRenderer.WARNING("Source is: ");
            LoggerInterface.loggerRenderer.WARNING(GL45.glGetShaderSource(shaderId));
            LoggerInterface.loggerRenderer.ERROR("Runtime Exception", new RuntimeException(GL45.glGetShaderInfoLog(shaderId)));
        }

        //create program
        rVal.programId = GL45.glCreateProgram();
        Globals.renderingEngine.checkError();

        //attach shader to program
        GL45.glAttachShader(rVal.programId, shaderId);
        Globals.renderingEngine.checkError();

        //link
        GL45.glLinkProgram(rVal.programId);
        success = GL45.glGetProgrami(rVal.programId, GL45.GL_LINK_STATUS);
        if (success != GL45.GL_TRUE) {
            throw new RuntimeException(GL45.glGetProgramInfoLog(rVal.programId));
        }
        

        return rVal;
    }

    /**
     * Dispatches the compute shader for execution
     * @param x The X grid size
     * @param y The Y grid size
     * @param z The Z grid size
     */
    public void dispatch(int x, int y, int z){
        GL45.glDispatchCompute(x, y, z);
        Globals.renderingEngine.checkError();
    }

    /**
     * Tries to set a uniform
     * @param uniformName The name of the uniform
     * @param value The value to set the uniform to
     */
    public void setUniform(OpenGLState openGLState, String uniformName, Object value){
        //
        //Error checking
        if(uniformName == null || uniformName.equals("")){
            throw new IllegalArgumentException("Trying to set invalid uniform name");
        }
        if(this.getId() != openGLState.getActiveShader().getId()){
            throw new IllegalStateException("Trying to set uniform on shader that is not active");
        }

        //
        //get uniform location
        int uniformLocation = this.getUniformLocation(uniformName);
        int glErrorCode = Globals.renderingEngine.getError();
        if(glErrorCode != 0){
            LoggerInterface.loggerRenderer.DEBUG_LOOP(RenderingEngine.getErrorInEnglish(glErrorCode));
            LoggerInterface.loggerRenderer.DEBUG_LOOP("Shader id: " + this.getId());
        }

        //
        //set the uniform
        if(uniformLocation == INVALID_UNIFORM_NAME){
            LoggerInterface.loggerRenderer.DEBUG_LOOP("Searched for uniform in a shader that does not contain it. Uniform name: \"" + uniformName + "\"");
        } else {
            ShaderUtils.setUniform(openGLState, this.uniformMap, uniformLocation, value);
        }
    }

    /**
     * Gets the location of a given uniform
     * @param uniformName The name of the uniform
     * @return The location of the uniform
     */
    public int getUniformLocation(String uniformName){
        int rVal = GL40.glGetUniformLocation(this.getId(), uniformName);
        Globals.renderingEngine.checkError();
        return rVal;
    }

    

    @Override
    public int getId() {
        return programId;
    }

}
