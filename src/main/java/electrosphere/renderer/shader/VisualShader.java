package electrosphere.renderer.shader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.logger.Logger.LogLevel;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.util.FileUtils;

/**
 * A visual shader program
 */
public class VisualShader implements Shader {

    /**
     * The vertex shader location
     */
    private int vertexShader;

    /**
     * The fragment shader location
     */
    private int fragmentShader;

    /**
     * The shader's ID
     */
    private int shaderId;
    
    /**
     * Map of uniform name -> data about the uniform
     */
    private Map<String,ShaderUniform> uniformNameMap = new HashMap<String,ShaderUniform>();

    /**
     * Map of uniform name -> location of uniform
     */
    public Map<String,Integer> uniformNameLocMap = new HashMap<String,Integer>();
    
    /**
     * The map of uniform location -> current value of uniform
     */
    public Map<Integer,Object> uniformValueMap = new HashMap<Integer,Object>();

    /**
     * The map of path -> already compiled shader
     */
    static Map<String,VisualShader> alreadyCompiledMap = new HashMap<String,VisualShader>();


    /**
     * Recursively preprocesses a file
     * @param currentFile The file to preprocess
     * @return The contents of the file
     */
    private static String recursivelyPreprocessFile(String input){
        return VisualShader.recursivelyPreprocessFile(FileUtils.getAssetFile(input), new LinkedList<String>());
    }

    /**
     * Recursively preprocesses a file
     * @param currentFile The file to preprocess
     * @return The contents of the file
     */
    private static String recursivelyPreprocessFile(File currentFile, List<String> includes){
        String contents = null;
        try {
            contents = Files.readString(currentFile.toPath());
        } catch (IOException e) {
            LoggerInterface.loggerRenderer.ERROR(e);
        }
        Pattern includePattern = Pattern.compile("#include \"(.*)\"");
        Matcher matcher = includePattern.matcher(contents);
        while(matcher.find()){
            String group = matcher.group(1);
            if(!includes.contains(group)){
                File directory = currentFile.getParentFile();
                File newFile = new File(directory.getPath() + "/" + group);
                String includeContent = VisualShader.recursivelyPreprocessFile(newFile, includes);
                contents = contents.replace("#include \"" + group + "\"", includeContent);
            }
        }
        //remove strings that we don't want to include
        contents = contents.replace("#extension GL_ARB_shading_language_include : require","");
        return contents;
    }
    
    /**
     * Smart assembles a shader
     * @return The visual shader
     */
    public static VisualShader smartAssembleShader(){

        //return shader if it has already been compiled
        String vertShaderPath = "/Shaders/VertexShader.vs";
        String fragShaderPath = "/Shaders/FragmentShader.fs";
        String key = VisualShader.getShaderKey(vertShaderPath, fragShaderPath);
        if(alreadyCompiledMap.containsKey(key)){
            return alreadyCompiledMap.get(key);
        }

        VisualShader rVal = VisualShader.loadSpecificShader(vertShaderPath, fragShaderPath);
        alreadyCompiledMap.put(key,rVal);

        //check program status
        if(!GL45.glIsProgram(rVal.shaderId)){
            throw new Error("Failed to build program!");
        }
        
        return rVal;
    }


    /**
     * Intelligently assembles a shader for use in OIT part of render pipeline
     * @param ContainsBones True if contains bones
     * @param apply_lighting True if should apply lighting
     * @return The int-pointer to the shader compiled
     */
    public static VisualShader smartAssembleOITProgram(){

        //return shader if it has already been compiled
        String vertShaderPath = "/Shaders/core/oit/general/VertexShader.vs";
        String fragShaderPath = "/Shaders/core/oit/general/FragmentShader.fs";
        String key = "oit" + VisualShader.getShaderKey(vertShaderPath, fragShaderPath);
        if(alreadyCompiledMap.containsKey(key)){
            return alreadyCompiledMap.get(key);
        }


        VisualShader rVal = VisualShader.loadSpecificShader(vertShaderPath, fragShaderPath);
        alreadyCompiledMap.put(key,rVal);

        //check program status
        rVal.validate();
        
        return rVal;
    }
    
    /**
     * Loads a specific shader
     * @param vertexPath The vertex shader's path
     * @param fragmentPath The fragment shader's path
     * @return The visual shader
     */
    public static VisualShader loadSpecificShader(String vertexPath, String fragmentPath){
        VisualShader rVal = new VisualShader();
        
        //
        //Read in shader programs
        //
        String vertexShaderSource = VisualShader.recursivelyPreprocessFile(vertexPath);
        String fragmentShaderSource = VisualShader.recursivelyPreprocessFile(fragmentPath);
        //Creates a new shader object and assigns its 'pointer' to the integer "vertexShader"
        rVal.vertexShader = GL40.glCreateShader(GL40.GL_VERTEX_SHADER);
        //This alerts openGL to the presence of a vertex shader and points the shader at its source
        GL40.glShaderSource(rVal.vertexShader, vertexShaderSource);
        //Compiles the source for the vertex shader object
        GL40.glCompileShader(rVal.vertexShader);
        //The following tests if the vertex shader compiles successfully
        int success;
        success = GL40.glGetShaderi(rVal.vertexShader, GL40.GL_COMPILE_STATUS);
        if (success != GL40.GL_TRUE) {
            List<Object> errorLines = new LinkedList<Object>();
            LoggerInterface.loggerRenderer.WARNING("Failed to load " + vertexPath + " ... attempting alternatives");
            //report failed to load shader
            errorLines.add("Vertex Shader failed to compile!");
            errorLines.add("Source File is: " + vertexPath);
            errorLines.add("Source is: ");
            errorLines.add(GL40.glGetShaderSource(rVal.vertexShader));
            errorLines.add(new RuntimeException(GL40.glGetShaderInfoLog(rVal.vertexShader)));
            //attempt loading alternative shaders
            List<String> availableAlternatives = Globals.shaderOptionMap.getAlternativesForFile(vertexPath);
            int alternativesAttempted = 0;
            if(availableAlternatives != null){
                for(String alternative : availableAlternatives){
                    alternativesAttempted++;
                    //load file
                    try {
                        vertexShaderSource = FileUtils.getAssetFileAsString(alternative);
                    } catch (IOException e) {
                        LoggerInterface.loggerEngine.ERROR("Failed to load shader alternative " + alternative, e);
                    }
                    //Creates a new shader object and assigns its 'pointer' to the integer "vertexShader"
                    rVal.vertexShader = GL40.glCreateShader(GL40.GL_VERTEX_SHADER);
                    //This alerts openGL to the presence of a vertex shader and points the shader at its source
                    GL40.glShaderSource(rVal.vertexShader, vertexShaderSource);
                    //Compiles the source for the vertex shader object
                    GL40.glCompileShader(rVal.vertexShader);
                    //The following tests if the vertex shader compiles successfully
                    success = GL40.glGetShaderi(rVal.vertexShader, GL40.GL_COMPILE_STATUS);
                    if (success == GL40.GL_TRUE) {
                        LoggerInterface.loggerRenderer.WARNING("Successfully loaded alternative shader " + alternative);
                        break;
                    } else {
                        errorLines.add("Vertex Shader failed to compile!");
                        errorLines.add("Source File is: " + vertexPath);
                        errorLines.add("Source is: ");
                        errorLines.add(GL40.glGetShaderSource(rVal.vertexShader));
                        errorLines.add(new RuntimeException(GL40.glGetShaderInfoLog(rVal.vertexShader)));
                    }
                }
                if(success != GL40.GL_TRUE){
                    for(Object object : errorLines){
                        if(object instanceof String){
                            LoggerInterface.loggerRenderer.WARNING((String)object);
                        } else if(object instanceof RuntimeErrorException){
                            LoggerInterface.loggerRenderer.ERROR("Runtime Exception", (RuntimeErrorException)object);
                        }
                    }
                }
                LoggerInterface.loggerRenderer.WARNING("Attempted " + alternativesAttempted + " alternative shaders");
            }
        }
        //Creates and opengl object for a fragment shader and assigns its 'pointer' to the integer fragmentShader
        rVal.fragmentShader = GL40.glCreateShader(GL40.GL_FRAGMENT_SHADER);
        //This points the opengl shadder object to its proper source
        GL40.glShaderSource(rVal.fragmentShader, fragmentShaderSource);
        //This compiles the shader object
        GL40.glCompileShader(rVal.fragmentShader);
        //This tests for the success of the compile attempt
        success = GL40.glGetShaderi(rVal.fragmentShader, GL40.GL_COMPILE_STATUS);
        if (success != GL40.GL_TRUE) {
            List<Object> errorLines = new LinkedList<Object>();
            LoggerInterface.loggerRenderer.WARNING("Failed to load " + fragmentPath + " ... attempting alternatives");
            //report failed to load shader
            errorLines.add("Fragment Shader failed to compile!");
            errorLines.add("Source File is: " + fragmentPath);
            errorLines.add("Source is: ");
            errorLines.add(GL40.glGetShaderSource(rVal.fragmentShader));
            errorLines.add(new RuntimeException(GL40.glGetShaderInfoLog(rVal.fragmentShader)));
            //attempt loading alternative shaders
            List<String> availableAlternatives = Globals.shaderOptionMap.getAlternativesForFile(fragmentPath);
            int alternativesAttempted = 0;
            if(availableAlternatives != null){
                for(String alternative : availableAlternatives){
                    alternativesAttempted++;
                    //load file
                    try {
                        fragmentShaderSource = FileUtils.getAssetFileAsString(alternative);
                    } catch (IOException e) {
                        LoggerInterface.loggerEngine.ERROR("Failed to load shader alternative " + alternative, e);
                    }
                    //Creates a new shader object and assigns its 'pointer' to the integer "vertexShader"
                    rVal.fragmentShader = GL40.glCreateShader(GL40.GL_FRAGMENT_SHADER);
                    //This alerts openGL to the presence of a vertex shader and points the shader at its source
                    GL40.glShaderSource(rVal.fragmentShader, fragmentShaderSource);
                    //Compiles the source for the vertex shader object
                    GL40.glCompileShader(rVal.fragmentShader);
                    //The following tests if the vertex shader compiles successfully
                    success = GL40.glGetShaderi(rVal.fragmentShader, GL40.GL_COMPILE_STATUS);
                    if (success == GL40.GL_TRUE) {
                        LoggerInterface.loggerRenderer.WARNING("Successfully loaded alternative shader " + alternative);
                        break;
                    } else {
                        errorLines.add("Fragment Shader failed to compile!");
                        errorLines.add("Source File is: " + fragmentPath);
                        errorLines.add("Source is: ");
                        errorLines.add(GL40.glGetShaderSource(rVal.fragmentShader));
                        errorLines.add(new RuntimeException(GL40.glGetShaderInfoLog(rVal.fragmentShader)));
                    }
                }
                if(success != GL40.GL_TRUE){
                    for(Object object : errorLines){
                        if(object instanceof String){
                            LoggerInterface.loggerRenderer.WARNING((String)object);
                        } else if(object instanceof RuntimeErrorException){
                            LoggerInterface.loggerRenderer.ERROR("Runtime Exception", (RuntimeErrorException)object);
                        }
                    }
                }
                LoggerInterface.loggerRenderer.WARNING("Attempted " + alternativesAttempted + " alternative shaders");
            }
        }
        //This creates a shader program opengl object and assigns its 'pointer' to the integer shaderProgram
        rVal.shaderId = GL40.glCreateProgram();
        //This attaches the vertex and fragment shaders to the program
        GL40.glAttachShader(rVal.shaderId, rVal.vertexShader);
        GL40.glAttachShader(rVal.shaderId, rVal.fragmentShader);
        //This links the program to the GPU (I think its to the GPU anyway)
        GL40.glLinkProgram(rVal.shaderId);
        //Tests for the success of the shader program creation
        success = GL40.glGetProgrami(rVal.shaderId, GL40.GL_LINK_STATUS);
        if (success != GL40.GL_TRUE) {
            LoggerInterface.loggerRenderer.ERROR(GL40.glGetProgramInfoLog(rVal.shaderId), new RuntimeException(GL40.glGetProgramInfoLog(rVal.shaderId)));
            LoggerInterface.loggerRenderer.WARNING("Shader sources: " + vertexPath + " " + fragmentPath);
            return Globals.defaultMeshShader;
            // throw new RuntimeException(glGetProgramInfoLog(rVal.shaderProgram));
        }        
        
        //Deletes the individual shader objects to free up memory
        GL40.glDeleteShader(rVal.vertexShader);
        GL40.glDeleteShader(rVal.fragmentShader);
        Globals.renderingEngine.checkError();

        //Parse all the uniformns from the source code
        for(ShaderUniform uniform : ShaderUniform.parseUniforms(vertexShaderSource)){
            rVal.uniformNameMap.put(uniform.getName(),uniform);
        }
        for(ShaderUniform uniform : ShaderUniform.parseUniforms(fragmentShaderSource)){
            rVal.uniformNameMap.put(uniform.getName(),uniform);
        }
        
        //check program status
        if(!GL45.glIsProgram(rVal.shaderId)){
            throw new Error("Failed to build program!");
        }
        
        return rVal;
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
            throw new Error("Trying to set invalid uniform name");
        }
        if(this.getId() != openGLState.getActiveShader().getId()){
            throw new Error("Trying to set uniform on shader that is not active");
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
            if(LoggerInterface.loggerRenderer.getLevel() == LogLevel.LOOP_DEBUG){
                LoggerInterface.loggerRenderer.DEBUG_LOOP("Searched for uniform in a shader that does not contain it. Uniform name: \"" + uniformName + "\"");
            }
        } else {
            ShaderUtils.setUniform(openGLState, this.uniformValueMap, uniformLocation, value);
        }
    }

    /**
     * Gets the location of a given uniform
     * @param uniformName The name of the uniform
     * @return The location of the uniform
     */
    public int getUniformLocation(String uniformName){
        if(this.uniformNameMap.containsKey(uniformName)){
            ShaderUniform uniform = this.uniformNameMap.get(uniformName);
            if(uniform.getLocation() == ShaderUniform.LOCATION_NOT_KNOWN){
                int rVal = GL40.glGetUniformLocation(this.getId(), uniformName);
                if(Globals.renderingEngine.checkError()){
                    LoggerInterface.loggerRenderer.WARNING("Uniform failed with shader id " + this.getId());
                }
                uniform.setLocation(rVal);
            }
            return uniform.getLocation();
        }
        if(this.uniformNameLocMap.containsKey(uniformName)){
            return uniformNameLocMap.get(uniformName);
        }
        int rVal = GL40.glGetUniformLocation(this.getId(), uniformName);
        uniformNameLocMap.put(uniformName,rVal);
        if(Globals.renderingEngine.checkError()){
            LoggerInterface.loggerRenderer.WARNING("Uniform failed with shader id " + this.getId());
        }
        return rVal;
    }

    /**
     * Gets a shader key
     * @param vertShader The vertex shader
     * @param fragShader The fragment shader
     * @return The key
     */
    private static String getShaderKey(String vertShader, String fragShader){
        return vertShader + "_" + fragShader;
    }


    @Override
    public int getId() {
        return this.shaderId;
    }

    /**
     * Validates the shader
     */
    public void validate(){
        if(!GL40.glIsProgram(this.getId())){
            throw new Error("Shader is not a program!");
        }
    }

    /**
     * Frees the shader
     */
    public void free(){
        GL40.glDeleteProgram(this.getId());
        this.shaderId = VisualShader.INVALID_UNIFORM_NAME;
    }

    /**
     * Clears the map of already-compiled shaders
     */
    public static void clearAlreadyCompiledMap(){
        VisualShader.alreadyCompiledMap.clear();
    }
    
}
