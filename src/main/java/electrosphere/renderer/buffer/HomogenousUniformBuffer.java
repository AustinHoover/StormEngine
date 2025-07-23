package electrosphere.renderer.buffer;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.renderer.RenderPipelineState;

/**
 * A buffer object of a single data type.
 * A data type in this case meaning a float, int, vec3f, etc..
 * 
 * The idea with this class is that in, for example instanced actors, we want to be able to dynamically declare buffers outside of the standard mesh flow.
 * That way the updating logic can be handled in the location concerned with the buffer.
 */
public class HomogenousUniformBuffer {
    
    /**
     * The different types of homogenous uniform buffers available
     */
    public static enum HomogenousBufferTypes {
        VEC3F,
        VEC3D,
        VEC4F,
        VEC4D,
        MAT4F,
        MAT4D,
        INT,
        FLOAT,
        DOUBLE,
    }

    /**
     * The type of this buffer
     */
    private HomogenousBufferTypes type;

    /**
     * The pointer for this buffer
     */
    private int bufferPointer = -1;

    /**
     * the bind point in the instance shader program for this buffer
     */
    private int capacity = -1;

    /**
     * uniform name
     */
    private String uniformName;

    /**
     * Constructor
     * @param uniformName The name of the uniform for this homogenous buffer
     * @param type The type of data in the buffer
     * @param capacity The capacity of the buffer
     */
    private HomogenousUniformBuffer(String uniformName, HomogenousBufferTypes type, int capacity){
        this.uniformName = uniformName;
        this.type = type;
        this.capacity = capacity;
    }

    /**
     * Creates a homogenous buffer
     * @param uniformName The name of the uniform in the shader that this buffer will be sent to
     * @param type The type of data in the buffer
     * @param capacity The capacity of the buffer
     * @return The HomogenousBuffer
     */
    public static HomogenousUniformBuffer createHomogenousBuffer(String uniformName, HomogenousBufferTypes type, int capacity){
        HomogenousUniformBuffer buffer = new HomogenousUniformBuffer(uniformName, type, capacity);
        Globals.assetManager.addHomogenousBufferToQueue(buffer);
        return buffer;
    }

    /**
     * Creates the buffer on the gpu
     */
    public void allocate(){
        //create buffer
        bufferPointer = GL45.glGenBuffers();
        //bind
        GL45.glBindBuffer(GL45.GL_UNIFORM_BUFFER, bufferPointer);
        //allocate space for the buffer
        GL45.glBufferData(GL45.GL_UNIFORM_BUFFER,calculateSize(),GL45.GL_STATIC_DRAW);
        //unbind
        GL45.glBindBuffer(GL45.GL_UNIFORM_BUFFER, 0);
    }

    /**
     * Calculates the number of bytes required by this buffer
     * @return The number of bytes
     */
    private int calculateSize(){
        switch(type){
            case VEC3F: {
                return capacity * 3 * 4;
            }
            case VEC3D: {
                return capacity * 3 * 8;
            }
            case VEC4F: {
                return capacity * 4 * 4;
            }
            case VEC4D: {
                return capacity * 4 * 8;
            }
            case DOUBLE: {
                return capacity * 1 * 8;
            }
            case FLOAT: {
                return capacity * 1 * 4;
            }
            case INT: {
                return capacity * 1 * 4;
            }
            case MAT4F: {
                return capacity * 4 * 4 * 4;
            }
            case MAT4D: {
                return capacity * 4 * 4 * 8;
            }
        }
        return 0;
    }

    /**
     * Calculates the number of bytes required by this type
     * @return The number of bytes
     */
    public static int calculateTypeSize(HomogenousBufferTypes type){
        switch(type){
            case VEC3F: {
                return 3 * 4;
            }
            case VEC3D: {
                return 3 * 8;
            }
            case VEC4F: {
                return 4 * 4;
            }
            case VEC4D: {
                return 4 * 8;
            }
            case DOUBLE: {
                return 1 * 8;
            }
            case FLOAT: {
                return 1 * 4;
            }
            case INT: {
                return 1 * 4;
            }
            case MAT4F: {
                return 4 * 4 * 4;
            }
            case MAT4D: {
                return 4 * 4 * 8;
            }
        }
        return 0;
    }

    /**
     * Returns whether this buffer is ready to stream data to or bind
     * @return True if ready, false otherwise
     */
    public boolean isReady(){
        return bufferPointer > 0;
    }

    /**
     * Binds this buffer
     */
    public void bind(RenderPipelineState renderPipelineState){
        //get the binding point of the ubo
        int bindPoint = GL45.glGetUniformBlockIndex(renderPipelineState.getCurrentShaderPointer(), uniformName);
        GL45.glUniformBlockBinding(renderPipelineState.getCurrentShaderPointer(), bindPoint, 2);
        //bind it
        GL45.glBindBufferBase(GL45.GL_UNIFORM_BUFFER, bindPoint, bufferPointer); 

        // GL45.glBindBuffer(GL45.GL_UNIFORM_BUFFER, bufferPointer);
    }

    /**
     * Updates the content of the buffer
     * @param object The buffer object (for instance FloatBuffer, IntBuffer, etc)
     * @param startIndex The start index to start buffering at
     */
    public void updateBuffer(Object object, int startIndex){
        //bind the buffer
        GL45.glBindBuffer(GL45.GL_UNIFORM_BUFFER, bufferPointer);
        switch(type){
            case VEC3F: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
            } break;
            case VEC3D: {
                DoubleBuffer buffer = (DoubleBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
            } break;
            case VEC4F: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
            } break;
            case VEC4D: {
                DoubleBuffer buffer = (DoubleBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
            } break;
            case DOUBLE: {
                DoubleBuffer buffer = (DoubleBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
            } break;
            case FLOAT: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
            } break;
            case INT: {
                IntBuffer buffer = (IntBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
            } break;
            case MAT4F: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer, startIndex, buffer);
            } break;
            case MAT4D: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer, startIndex, buffer);
            } break;
        }
        //unbind the buffer
        // GL45.glBindBuffer(GL45.GL_UNIFORM_BUFFER, 0);
    }

}
