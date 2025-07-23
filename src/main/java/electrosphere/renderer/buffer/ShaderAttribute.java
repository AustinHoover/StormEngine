package electrosphere.renderer.buffer;

import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;

/**
 * Represents an attribute of a shader.
 * When working with shader attributes, almost all data types are 1-1 where one vec4 can be stored in 1 attribute.
 * This is not the case for matrix attributes. In the interest of having a single, central location that related to a shader attribute,
 * This class stores either a single index or a list of indices based on the data type of the attribute that is being worked with.
 */
public class ShaderAttribute {

    /**
     * for single data types that map 1-1 with attributes (float, vec3, vec4, etc)
     */
    private int attributeIndex = -1;

    /**
     * for multi-attribute index types (mat4f, mat4d, etc)
     */
    private int[] attributeIndices;

    /**
     * The name of the attribute
     */
    private String name;

    /**
     * The type of the attribute
     */
    private HomogenousBufferTypes type;


    /**
     * Constructor for 1-1 attribute
     * @param attributeIndex The attribute index
     */
    public ShaderAttribute(int attributeIndex){
        this.attributeIndex = attributeIndex;
    }

    /**
     * Constructor for many-1 attribute
     * @param attributeIndices The attribute indices
     */
    public ShaderAttribute(int[] attributeIndices){
        this.attributeIndices = attributeIndices;
    }

    /**
     * Constructor for 1-1 attribute
     * @param name The name of the attribute
     * @param type The type of attribute
     */
    public ShaderAttribute(String name, HomogenousBufferTypes type){
        this.name = name;
        this.type = type;
    }

    /**
     * Checks if the attribute is a 1-1 relation
     * @return True if 1-1, false otherwise
     */
    public boolean isSingleIndex(){
        return attributeIndex > -1;
    }

    /**
     * Returns the index of this attribute
     * @return The index
     */
    public int getIndex(){
        return attributeIndex;
    }

    /**
     * Returns the array of indices for this attribute
     * @return The array of indices
     */
    public int[] getIndices(){
        return attributeIndices;
    }

    /**
     * Gets the type of the attribute
     * @return The type
     */
    public HomogenousBufferTypes getType(){
        return type;
    }

    /**
     * Gets the name of the attribute
     * @return The name
     */
    public String getName(){
        return name;
    }

}
