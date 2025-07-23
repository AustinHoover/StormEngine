package electrosphere.renderer.actor.instance;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.buffer.HomogenousInstancedArray;
import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;
import electrosphere.renderer.buffer.ShaderAttribute;

/**
 * An instance data that uses multiple homogenous buffers
 */
public class HomogenousInstanceData implements InstanceData {
    
    //the capacity (number of instanced) of this data block. Defaults to 100
    int capacity = 1000;

    //shader paths
    String vertexShaderPath;
    String fragmentShaderPath;

    //the number of draw calls since the last clear operation
    int drawCalls = 0;
    
    //The set of instanced actors to draw
    List<InstancedActor> actorQueue = null;
    //Map of actor to index in the buffers that are emitted
    Map<InstancedActor,Integer> actorIndexMap = new HashMap<InstancedActor,Integer>();
    //Map of index -> actor used for buffer evictions
    Map<Integer,InstancedActor> indexActorMap = new HashMap<Integer,InstancedActor>();

    //list of all attribute indices in use by this instance data
    List<ShaderAttribute> attributeIndices = new LinkedList<ShaderAttribute>();
    //map of attribute -> buffer of attribute data
    Map<ShaderAttribute,Object> attributeCpuBufferMap = new HashMap<ShaderAttribute,Object>();
    //map of attribute -> gl HomogenousBuffer
    Map<ShaderAttribute,HomogenousInstancedArray> attributeGlBufferMap = new HashMap<ShaderAttribute,HomogenousInstancedArray>();


    /**
     * Constructor
     * @param capacity Capacity of the buffer (number of elements) backing this data
     */
    protected HomogenousInstanceData(int capacity, String vertexPath, String fragmentPath){
        this.capacity = capacity;
        this.vertexShaderPath = vertexPath;
        this.fragmentShaderPath = fragmentPath;
        actorQueue = new LinkedList<InstancedActor>();
    }

    /**
     * Adds a new type of attribute to this instance data block
     * @param shaderAttribute The shader attribute
     * @param type The type of the attribute
     */
    protected void addDataType(ShaderAttribute shaderAttribute, HomogenousBufferTypes type){
        attributeIndices.add(shaderAttribute);
        switch(type){
            case VEC3F: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createFloatBuffer(capacity * 3));
            } break;
            case VEC3D: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createDoubleBuffer(capacity * 3));
            } break;
            case VEC4F: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createFloatBuffer(capacity * 4));
            } break;
            case VEC4D: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createDoubleBuffer(capacity * 4));
            } break;
            case DOUBLE: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createDoubleBuffer(capacity));
            } break;
            case FLOAT: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createFloatBuffer(capacity));
            } break;
            case INT: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createIntBuffer(capacity));
            } break;
            case MAT4F: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createFloatBuffer(capacity * 4 * 4));
            } break;
            case MAT4D: {
                attributeCpuBufferMap.put(shaderAttribute, BufferUtils.createFloatBuffer(capacity * 4 * 8));
            } break;
        }
        if(shaderAttribute.isSingleIndex()){
            attributeGlBufferMap.put(shaderAttribute,HomogenousInstancedArray.createHomogenousInstancedArray(shaderAttribute.getIndex(), type, capacity));
        } else {
            attributeGlBufferMap.put(shaderAttribute,HomogenousInstancedArray.createHomogenousInstancedArray(shaderAttribute.getIndices(), type, capacity));
        }
    }


    /**
     * Adds an actor to be sorted in the queue
     * @param actor The actor to be sorted
     */
    public void addInstance(InstancedActor actor){
        actorQueue.add(actor);
        drawCalls++;
    }

    /**
     * Gets the number of entries that are to be drawn
     * @return The number of entries to be drawn
     */
    public int getDrawCount(){
        return drawCalls;
    }

    /**
     * Clears the queue
     */
    public void clearDrawQueue(){
        actorQueue.clear();
        drawCalls = 0;
    }

    /**
     * Fills the buffers for the upcoming render call. The intention is to make this emberassingly parallel.
     */
    public void fillBuffers(){
        int i = 0;
        //for some reason the limit is not being set correctly. This explicitly forces it for each buffer
        for(ShaderAttribute attribute : attributeIndices){
            switch(attributeGlBufferMap.get(attribute).getType()){
                case VEC3F: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
                case VEC3D: {
                    DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
                case VEC4F: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
                case VEC4D: {
                    DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
                case MAT4F: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                    // System.out.println(buffer.position() + " " + buffer.limit());
                } break;
                case MAT4D: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
                case DOUBLE: {
                    DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
                case FLOAT: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
                case INT: {
                    IntBuffer buffer = ((IntBuffer)attributeCpuBufferMap.get(attribute));
                    buffer.limit(buffer.capacity());
                } break;
            }
        }
        // actorQueue.sort(Comparator.naturalOrder());
        //buffer data
        for(InstancedActor actor : actorQueue){
            //push values to attribute buffers
            for(ShaderAttribute attribute : attributeIndices){
                switch(attributeGlBufferMap.get(attribute).getType()){
                    case VEC3F: {
                        FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                        Vector3f vec = (Vector3f)actor.getAttributeValue(attribute);
                        buffer.put(vec.x);
                        buffer.put(vec.y);
                        buffer.put(vec.z);
                    } break;
                    case VEC3D: {
                        DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                        Vector3d vec = (Vector3d)actor.getAttributeValue(attribute);
                        buffer.put(vec.x);
                        buffer.put(vec.y);
                        buffer.put(vec.z);
                    } break;
                    case VEC4F: {
                        FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                        Vector4f vec = (Vector4f)actor.getAttributeValue(attribute);
                        buffer.put(vec.w);
                        buffer.put(vec.x);
                        buffer.put(vec.y);
                        buffer.put(vec.z);
                    } break;
                    case VEC4D: {
                        DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                        Vector4d vec = (Vector4d)actor.getAttributeValue(attribute);
                        buffer.put(vec.w);
                        buffer.put(vec.x);
                        buffer.put(vec.y);
                        buffer.put(vec.z);
                    } break;
                    case MAT4F: {
                        FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                        Matrix4f mat = (Matrix4f)actor.getAttributeValue(attribute);
                        buffer.put(mat.m00());
                        buffer.put(mat.m01());
                        buffer.put(mat.m02());
                        buffer.put(mat.m03());

                        buffer.put(mat.m10());
                        buffer.put(mat.m11());
                        buffer.put(mat.m12());
                        buffer.put(mat.m13());
                        
                        buffer.put(mat.m20());
                        buffer.put(mat.m21());
                        buffer.put(mat.m22());
                        buffer.put(mat.m23());

                        buffer.put(mat.m30());
                        buffer.put(mat.m31());
                        buffer.put(mat.m32());
                        buffer.put(mat.m33());
                    } break;
                    case MAT4D: {
                        FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                        Matrix4d mat = (Matrix4d)actor.getAttributeValue(attribute);
                        buffer.put((float)mat.m00());
                        buffer.put((float)mat.m01());
                        buffer.put((float)mat.m02());
                        buffer.put((float)mat.m03());

                        buffer.put((float)mat.m10());
                        buffer.put((float)mat.m11());
                        buffer.put((float)mat.m12());
                        buffer.put((float)mat.m13());
                        
                        buffer.put((float)mat.m20());
                        buffer.put((float)mat.m21());
                        buffer.put((float)mat.m22());
                        buffer.put((float)mat.m23());

                        buffer.put((float)mat.m30());
                        buffer.put((float)mat.m31());
                        buffer.put((float)mat.m32());
                        buffer.put((float)mat.m33());
                    } break;
                    case DOUBLE: {
                        DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                        buffer.put((Double)actor.getAttributeValue(attribute));
                    } break;
                    case FLOAT: {
                        FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                        buffer.put((Float)actor.getAttributeValue(attribute));
                    } break;
                    case INT: {
                        IntBuffer buffer = ((IntBuffer)attributeCpuBufferMap.get(attribute));
                        buffer.put((Integer)actor.getAttributeValue(attribute));
                    } break;
                }
            }
            //increment
            i++;
            if(i >= capacity){
                break;
            }
        }
        //reset all buffers
        flip();
    }

    /**
     * Flips the data buffer(s)
     */
    public void flip(){
        //reset all buffers
        for(ShaderAttribute attribute : attributeIndices){
            switch(attributeGlBufferMap.get(attribute).getType()){
                case VEC3F: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case VEC3D: {
                    DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case VEC4F: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case VEC4D: {
                    DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case MAT4F: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case MAT4D: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case DOUBLE: {
                    DoubleBuffer buffer = ((DoubleBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case FLOAT: {
                    FloatBuffer buffer = ((FloatBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
                case INT: {
                    IntBuffer buffer = ((IntBuffer)attributeCpuBufferMap.get(attribute));
                    if(buffer.position() > 0){
                        buffer.flip();
                    }
                } break;
            }
        }
    }

    /**
     * Gets a map of all attributes to the buffers of data for that attribute
     * @return The data buffers
     */
    public Map<ShaderAttribute,Object> getCpuBufferMap(){
        return attributeCpuBufferMap;
    }

    /**
     * Gets a map of attribute name to gl homogenous buffer object
     * @return The map
     */
    public Map<ShaderAttribute,HomogenousInstancedArray> getGlBufferMap(){
        return attributeGlBufferMap;
    }

    @Override
    public String getVertexShader(){
        return vertexShaderPath;
    }

    @Override
    public String getFragmentShader(){
        return fragmentShaderPath;
    }

    @Override
    public void upload(OpenGLState openGLState, RenderPipelineState renderPipelineState){
        Map<ShaderAttribute,Object> buffers = this.getCpuBufferMap();
        Map<ShaderAttribute,HomogenousInstancedArray> glBufferMap = this.getGlBufferMap();
        for(ShaderAttribute attribute : buffers.keySet()){
            HomogenousInstancedArray buffer = glBufferMap.get(attribute);
            buffer.updateBuffer(buffers.get(attribute), 0);
            buffer.bind(renderPipelineState);
        }
        renderPipelineState.setInstanceCount(this.getDrawCount());
        Globals.renderingEngine.checkError();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Unimplemented method 'destroy'");
    }

}
