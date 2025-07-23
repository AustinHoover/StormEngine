package electrosphere.renderer.actor.instance;

import java.nio.ByteBuffer;
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

import electrosphere.renderer.buffer.HomogenousUniformBuffer;
import electrosphere.renderer.buffer.ShaderAttribute;
import electrosphere.renderer.buffer.ShaderStorageBuffer;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.buffer.BufferEnums.BufferAccess;
import electrosphere.renderer.buffer.BufferEnums.BufferUsage;

/**
 * Instance data that uses a single buffer with strided input
 */
public class StridedInstanceData implements InstanceData {
    
    //the capacity (number of instances) of this data block. Defaults to 1000
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
    List<ShaderAttribute> attributes;

    //the SSBO
    ShaderStorageBuffer buffer;

    /**
     * The index to bind to
     */
    int bindPoint;


    /**
     * Constructor
     * @param capacity Capacity of the buffer (number of elements) backing this data
     */
    public StridedInstanceData(int capacity, int bindPoint, List<ShaderAttribute> attributes, String vertexPath, String fragmentPath){
        int entrySize = 0;
        boolean hitMat = false;
        boolean hitVec = false;
        boolean hitPrimitive = false;
        for(ShaderAttribute attribute : attributes){
            entrySize = entrySize + HomogenousUniformBuffer.calculateTypeSize(attribute.getType());
            switch(attribute.getType()){
                case MAT4D:
                case MAT4F: {
                    hitMat = true;
                } break;
                case VEC3D:
                case VEC3F:
                case VEC4D:
                case VEC4F: {
                    hitVec = true;
                } break;
                case INT:
                case DOUBLE:
                case FLOAT: {
                    hitPrimitive = true;
                } break;
            }
        }
        if(hitPrimitive && (hitMat || hitVec)){
            String message = "Warning! You are mixing a larger alignment type (vec, mat) with primitives (int, float)!\n" +
            "This can potentially cause alignment bugs. See hhttps://learnopengl.com/Advanced-OpenGL/Advanced-GLSL or \n" + 
            "https://www.khronos.org/opengl/wiki/Interface_Block_(GLSL)#Memory_layout for details on memory layout!"
            ;
            LoggerInterface.loggerRenderer.WARNING(message);
        }
        this.attributes = attributes;
        this.capacity = capacity;
        this.vertexShaderPath = vertexPath;
        this.fragmentShaderPath = fragmentPath;
        actorQueue = new LinkedList<InstancedActor>();
        this.buffer = new ShaderStorageBuffer(capacity * entrySize, BufferUsage.STREAM, BufferAccess.DRAW);
        this.bindPoint = bindPoint;
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
        ByteBuffer byteBuff = this.buffer.getBuffer();
        byteBuff.limit(byteBuff.capacity());
        //buffer data
        for(InstancedActor actor : actorQueue){
            //push values to attribute buffers
            for(ShaderAttribute attribute : attributes){
                if(actor.getAttributeValue(attribute) != null){
                    switch(attribute.getType()){
                        case VEC3F: {
                            Vector3f vec = (Vector3f)actor.getAttributeValue(attribute);
                            byteBuff.putFloat(vec.x);
                            byteBuff.putFloat(vec.y);
                            byteBuff.putFloat(vec.z);
                        } break;
                        case VEC3D: {
                            Vector3d vec = (Vector3d)actor.getAttributeValue(attribute);
                            byteBuff.putDouble(vec.x);
                            byteBuff.putDouble(vec.y);
                            byteBuff.putDouble(vec.z);
                        } break;
                        case VEC4F: {
                            Vector4f vec = (Vector4f)actor.getAttributeValue(attribute);
                            byteBuff.putFloat(vec.w);
                            byteBuff.putFloat(vec.x);
                            byteBuff.putFloat(vec.y);
                            byteBuff.putFloat(vec.z);
                        } break;
                        case VEC4D: {
                            Vector4d vec = (Vector4d)actor.getAttributeValue(attribute);
                            byteBuff.putDouble(vec.w);
                            byteBuff.putDouble(vec.x);
                            byteBuff.putDouble(vec.y);
                            byteBuff.putDouble(vec.z);
                        } break;
                        case MAT4F: {
                            Matrix4f mat = (Matrix4f)actor.getAttributeValue(attribute);
                            byteBuff.putFloat(mat.m00());
                            byteBuff.putFloat(mat.m01());
                            byteBuff.putFloat(mat.m02());
                            byteBuff.putFloat(mat.m03());

                            byteBuff.putFloat(mat.m10());
                            byteBuff.putFloat(mat.m11());
                            byteBuff.putFloat(mat.m12());
                            byteBuff.putFloat(mat.m13());
                            
                            byteBuff.putFloat(mat.m20());
                            byteBuff.putFloat(mat.m21());
                            byteBuff.putFloat(mat.m22());
                            byteBuff.putFloat(mat.m23());

                            byteBuff.putFloat(mat.m30());
                            byteBuff.putFloat(mat.m31());
                            byteBuff.putFloat(mat.m32());
                            byteBuff.putFloat(mat.m33());
                        } break;
                        case MAT4D: {
                            Matrix4d mat = (Matrix4d)actor.getAttributeValue(attribute);
                            byteBuff.putDouble((float)mat.m00());
                            byteBuff.putDouble((float)mat.m01());
                            byteBuff.putDouble((float)mat.m02());
                            byteBuff.putDouble((float)mat.m03());

                            byteBuff.putDouble((float)mat.m10());
                            byteBuff.putDouble((float)mat.m11());
                            byteBuff.putDouble((float)mat.m12());
                            byteBuff.putDouble((float)mat.m13());
                            
                            byteBuff.putDouble((float)mat.m20());
                            byteBuff.putDouble((float)mat.m21());
                            byteBuff.putDouble((float)mat.m22());
                            byteBuff.putDouble((float)mat.m23());

                            byteBuff.putDouble((float)mat.m30());
                            byteBuff.putDouble((float)mat.m31());
                            byteBuff.putDouble((float)mat.m32());
                            byteBuff.putDouble((float)mat.m33());
                        } break;
                        case DOUBLE: {
                            byteBuff.putDouble((Double)actor.getAttributeValue(attribute));
                        } break;
                        case FLOAT: {
                            byteBuff.putFloat((Float)actor.getAttributeValue(attribute));
                        } break;
                        case INT: {
                            byteBuff.putInt((Integer)actor.getAttributeValue(attribute));
                        } break;
                        default: {
                            throw new Error("Unhandled attribute type!");
                        }
                    }
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
        this.buffer.getBuffer().flip();
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
        this.buffer.upload(openGLState);
        openGLState.glBindBufferBase(this.bindPoint, buffer);
        renderPipelineState.setInstanceCount(this.getDrawCount());
    }

    @Override
    public void destroy() {
        if(this.buffer != null){
            this.buffer.destroy();
        }
    }

}
