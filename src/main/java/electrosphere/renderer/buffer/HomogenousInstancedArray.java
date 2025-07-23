package electrosphere.renderer.buffer;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;

public class HomogenousInstancedArray {

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
     * attribute index for a regular buffer
     */
    private int attributeIndex;

    /**
     * attribute indices for a matrix buffer
     */
    private int[] matrixAttributeIndices;

    /**
     * Constructor
     * @param attributeIndex The attribute index of this buffer
     * @param type The type of data in the buffer
     * @param capacity The capacity of the buffer
     */
    private HomogenousInstancedArray(int attributeIndex, HomogenousBufferTypes type, int capacity){
        this.attributeIndex = attributeIndex;
        this.type = type;
        this.capacity = capacity;
    }

    /**
     * Constructor
     * @param matrixAttributeIndices The attribute indices of this buffer
     * @param type The type of data in the buffer
     * @param capacity The capacity of the buffer
     */
    private HomogenousInstancedArray(int[] matrixAttributeIndices, HomogenousBufferTypes type, int capacity){
        this.matrixAttributeIndices = matrixAttributeIndices;
        this.type = type;
        this.capacity = capacity;
    }

    /**
     * Creates a homogenous buffer
     * @param attributeIndex The attribute index of this buffer
     * @param type The type of data in the buffer
     * @param capacity The capacity of the buffer
     * @return The HomogenousBuffer
     */
    public static HomogenousInstancedArray createHomogenousInstancedArray(int attributeIndex, HomogenousBufferTypes type, int capacity){
        HomogenousInstancedArray buffer = new HomogenousInstancedArray(attributeIndex, type, capacity);
        Globals.assetManager.addInstanceArrayBufferToQueue(buffer);
        return buffer;
    }

    /**
     * Creates a homogenous buffer
     * @param matrixAttributeIndices The attribute indices of this buffer
     * @param type The type of data in the buffer
     * @param capacity The capacity of the buffer
     * @return The HomogenousBuffer
     */
    public static HomogenousInstancedArray createHomogenousInstancedArray(int[] matrixAttributeIndices, HomogenousBufferTypes type, int capacity){
        HomogenousInstancedArray buffer = new HomogenousInstancedArray(matrixAttributeIndices, type, capacity);
        Globals.assetManager.addInstanceArrayBufferToQueue(buffer);
        return buffer;
    }

    /**
     * Creates the buffer on the gpu
     */
    public void allocate(){
        //create buffer
        bufferPointer = GL45.glGenBuffers();
        //bind
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
        //allocate space for the buffer
        GL45.glBufferData(GL45.GL_ARRAY_BUFFER,calculateSize(),GL45.GL_STATIC_DRAW);
        //unbind
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
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
     * Returns whether this buffer is ready to stream data to or bind
     * @return True if ready, false otherwise
     */
    public boolean isReady(){
        return bufferPointer > 0;
    }

    /**
     * Gets the type of this array buffer
     * @return The type
     */
    public HomogenousBufferTypes getType(){
        return type;
    }

    /**
     * Binds this buffer
     */
    public void bind(RenderPipelineState renderPipelineState){
        if(type == HomogenousBufferTypes.MAT4F){
            //https://solhsa.com/instancing.html
            //https://stackoverflow.com/questions/17355051/using-a-matrix-as-vertex-attribute-in-opengl3-core-profile
            //"opengl matrix attribute"
            //https://learnopengl.com/code_viewer_gh.php?code=src/4.advanced_opengl/10.3.asteroids_instanced/asteroids_instanced.cpp
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //enable attributes
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[0]);
            Globals.renderingEngine.checkError();
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[1]);
            Globals.renderingEngine.checkError();
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[2]);
            Globals.renderingEngine.checkError();
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[3]);
            Globals.renderingEngine.checkError();
            //update attribute to point to buffer at correct offset + stride
            GL45.glVertexAttribPointer(matrixAttributeIndices[0], 4, GL45.GL_FLOAT, false, 64, 0 * 4);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribPointer(matrixAttributeIndices[1], 4, GL45.GL_FLOAT, false, 64, 4 * 4);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribPointer(matrixAttributeIndices[2], 4, GL45.GL_FLOAT, false, 64, 4 * 8);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribPointer(matrixAttributeIndices[3], 4, GL45.GL_FLOAT, false, 64, 4 * 12);
            Globals.renderingEngine.checkError();
            //bind buffer
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //tell opengl to send a new value from buffer for each instance (instead of whole buffer for every instance)
            GL45.glVertexAttribDivisor(matrixAttributeIndices[0], 1);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribDivisor(matrixAttributeIndices[1], 1);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribDivisor(matrixAttributeIndices[2], 1);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribDivisor(matrixAttributeIndices[3], 1);
            Globals.renderingEngine.checkError();
        } else if(type == HomogenousBufferTypes.MAT4D){
            //https://solhsa.com/instancing.html
            //https://stackoverflow.com/questions/17355051/using-a-matrix-as-vertex-attribute-in-opengl3-core-profile
            //"opengl matrix attribute"
            //https://learnopengl.com/code_viewer_gh.php?code=src/4.advanced_opengl/10.3.asteroids_instanced/asteroids_instanced.cpp
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //enable attributes
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[0]);
            Globals.renderingEngine.checkError();
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[1]);
            Globals.renderingEngine.checkError();
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[2]);
            Globals.renderingEngine.checkError();
            GL45.glEnableVertexAttribArray(matrixAttributeIndices[3]);
            Globals.renderingEngine.checkError();
            //update attribute to point to buffer at correct offset + stride
            GL45.glVertexAttribPointer(matrixAttributeIndices[0], 4, GL45.GL_FLOAT, false, 64, 0 * 4);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribPointer(matrixAttributeIndices[1], 4, GL45.GL_FLOAT, false, 64, 4 * 4);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribPointer(matrixAttributeIndices[2], 4, GL45.GL_FLOAT, false, 64, 4 * 8);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribPointer(matrixAttributeIndices[3], 4, GL45.GL_FLOAT, false, 64, 4 * 12);
            Globals.renderingEngine.checkError();
            //bind buffer
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //tell opengl to send a new value from buffer for each instance (instead of whole buffer for every instance)
            GL45.glVertexAttribDivisor(matrixAttributeIndices[0], 1);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribDivisor(matrixAttributeIndices[1], 1);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribDivisor(matrixAttributeIndices[2], 1);
            Globals.renderingEngine.checkError();
            GL45.glVertexAttribDivisor(matrixAttributeIndices[3], 1);
            Globals.renderingEngine.checkError();
        } else if(type == HomogenousBufferTypes.FLOAT){
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //enable attributes
            GL45.glEnableVertexAttribArray(attributeIndex);
            Globals.renderingEngine.checkError();
            //update attribute to point to buffer at correct offset + stride
            GL45.glVertexAttribPointer(attributeIndex, 1, GL45.GL_FLOAT, false, 0, 0);
            Globals.renderingEngine.checkError();
            //bind buffer
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //tell opengl to send a new value from buffer for each instance (instead of whole buffer for every instance)
            GL45.glVertexAttribDivisor(attributeIndex, 1);
            Globals.renderingEngine.checkError();
        } else if(type == HomogenousBufferTypes.VEC3F){
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //enable attributes
            GL45.glEnableVertexAttribArray(attributeIndex);
            Globals.renderingEngine.checkError();
            //update attribute to point to buffer at correct offset + stride
            GL45.glVertexAttribPointer(attributeIndex, 3, GL45.GL_FLOAT, false, 0, 0);
            Globals.renderingEngine.checkError();
            //bind buffer
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            Globals.renderingEngine.checkError();
            //tell opengl to send a new value from buffer for each instance (instead of whole buffer for every instance)
            GL45.glVertexAttribDivisor(attributeIndex, 1);
            Globals.renderingEngine.checkError();
        } else {
            LoggerInterface.loggerRenderer.ERROR("Unsupported operation", new Exception());
        }
        // GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
    }

    /**
     * Updates the content of the buffer
     * @param object The buffer object (for instance FloatBuffer, IntBuffer, etc)
     * @param startIndex The start index to start buffering at
     */
    public void updateBuffer(Object object, int startIndex){
        //bind the buffer
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
        Globals.renderingEngine.checkError();
        switch(type){
            case VEC3F: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
                Globals.renderingEngine.checkError();
            } break;
            case VEC3D: {
                DoubleBuffer buffer = (DoubleBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
                Globals.renderingEngine.checkError();
            } break;
            case VEC4F: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
                Globals.renderingEngine.checkError();
            } break;
            case VEC4D: {
                DoubleBuffer buffer = (DoubleBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
                Globals.renderingEngine.checkError();
            } break;
            case DOUBLE: {
                DoubleBuffer buffer = (DoubleBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
                Globals.renderingEngine.checkError();
            } break;
            case FLOAT: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
                Globals.renderingEngine.checkError();
            } break;
            case INT: {
                IntBuffer buffer = (IntBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer,startIndex,buffer);
                Globals.renderingEngine.checkError();
            } break;
            case MAT4F: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer, startIndex, buffer);
                Globals.renderingEngine.checkError();
            } break;
            case MAT4D: {
                FloatBuffer buffer = (FloatBuffer)object;
                GL45.glNamedBufferSubData(bufferPointer, startIndex, buffer);
                Globals.renderingEngine.checkError();
            } break;
        }
        //unbind the buffer
        GL33.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
        Globals.renderingEngine.checkError();
    }
}
