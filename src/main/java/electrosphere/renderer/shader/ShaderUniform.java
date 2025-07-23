package electrosphere.renderer.shader;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A uniform in a shader program
 */
public class ShaderUniform {

    /**
     * Pattern for grabbing uniforms
     */
    private static final Pattern uniformCapture = Pattern.compile("^\\s*uniform\\s+([a-zA-Z0-9]+)\\s+([a-zA-Z]+)\\s*;\\s*$", Pattern.MULTILINE);

    /**
     * Constant for an unknown location
     */
    public static final int LOCATION_NOT_KNOWN = -1;
    
    /**
     * The name of the uniform
     */
    private String name;

    /**
     * The type of the uniform
     */
    private String type;

    /**
     * The location of this uniform
     */
    private int location = ShaderUniform.LOCATION_NOT_KNOWN;

    /**
     * Creates a shader uniform
     * @param name The name of the uniform
     * @param type The type of the uniform
     */
    public ShaderUniform(String name, String type){
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the name of the uniform
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of the uniform
     * @return The type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the location of the uniform
     * @return The location
     */
    public int getLocation() {
        return location;
    }

    /**
     * Sets the location of the uniform
     * @param location The location
     */
    public void setLocation(int location) {
        this.location = location;
    }

    /**
     * Parses the uniforms from a source file's content
     * @param sourceContent The source file's content
     * @return The list of uniforms
     */
    public static List<ShaderUniform> parseUniforms(String sourceContent){
        List<ShaderUniform> rVal = new LinkedList<ShaderUniform>();
        Matcher matcher = uniformCapture.matcher(sourceContent);
        while(matcher.find()){
            String uniformType = matcher.group(1);
            String uniformName = matcher.group(2);
            if(uniformType.equals("Material")){
                //special flow for materials
                ShaderUniform newUniform;
                newUniform = new ShaderUniform("material.diffuse", "sampler2D");
                rVal.add(newUniform);
                newUniform = new ShaderUniform("material.specular", "sampler2D");
                rVal.add(newUniform);
            } else {
                ShaderUniform newUniform = new ShaderUniform(uniformName, uniformType);
                rVal.add(newUniform);
            }
        }
        return rVal;
    }

}
