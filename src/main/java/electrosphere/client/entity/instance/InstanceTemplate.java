package electrosphere.client.entity.instance;

import java.util.Map;

import electrosphere.renderer.actor.instance.StridedInstanceData;
import electrosphere.renderer.buffer.ShaderAttribute;
import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;

/**
 * A template for creating an instance entity. Stores thing like capacity and the model
 */
public class InstanceTemplate {

    /**
     * The path for the model for this instance
     */
    protected String modelPath;

    /**
     * The total number of instances to draw at a given time
     */
    protected int capacity;

    /**
     * The map of all attributes for instanced foliage
     */
    protected Map<ShaderAttribute,HomogenousBufferTypes> attributes;

    /**
     * The strided instance data
     */
    protected StridedInstanceData stridedInstanceData;

    /**
     * The bind point for the data
     */
    int dataBindPoint;

    /**
     * Vertex shader path
     */
    protected String vertexPath;

    /**
     * Fragment shader path
     */
    protected String fragmentPath;

    /**
     * Creates a template to create instanced actors off of
     * @param capacity The number of actors that can be drawn per frame (going over this limit will result in drawing the closest ${capacity} number of actors)
     * @param modelPath The path for the model of the instance
     * @param vertexPath The vertex shader path
     * @param fragmentPath The fragment shader path
     * @param attributes The attributes used on this instance
     * @return The template
     */
    public static InstanceTemplate createInstanceTemplate(int capacity, String modelPath, String vertexPath, String fragmentPath, Map<ShaderAttribute,HomogenousBufferTypes> attributes){
        InstanceTemplate rVal = new InstanceTemplate();
        rVal.capacity = capacity;
        rVal.modelPath = modelPath;
        rVal.vertexPath = vertexPath;
        rVal.fragmentPath = fragmentPath;
        rVal.attributes = attributes;
        return rVal;
    }

    /**
     * Creates a template to create instanced actors off of
     * @param capacity The number of actors that can be drawn per frame (going over this limit will result in drawing the closest ${capacity} number of actors)
     * @param modelPath The path for the model of the instance
     * @param vertexPath The vertex shader path
     * @param fragmentPath The fragment shader path
     * @param stridedInstanceData The strided instance data to use
     * @return The template
     */
    public static InstanceTemplate createInstanceTemplate(int capacity, String modelPath, String vertexPath, String fragmentPath, StridedInstanceData stridedInstanceData, int bindPoint){
        InstanceTemplate rVal = new InstanceTemplate();
        rVal.capacity = capacity;
        rVal.modelPath = modelPath;
        rVal.vertexPath = vertexPath;
        rVal.fragmentPath = fragmentPath;
        rVal.stridedInstanceData = stridedInstanceData;
        rVal.dataBindPoint = bindPoint;
        return rVal;
    }


    
}
