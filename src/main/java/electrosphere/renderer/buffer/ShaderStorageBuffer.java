package electrosphere.renderer.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.buffer.BufferEnums.BufferAccess;
import electrosphere.renderer.buffer.BufferEnums.BufferUsage;

/**
 * A shader storage buffer
 */
public class ShaderStorageBuffer implements UniformBlockBinding {

    /**
     * The id for the buffer
     */
    private int id;

    /**
     * The java buffer associated with the SSBO
     */
    private ByteBuffer buffer;

    /**
     * Constructor
     * @param capacity The capacity of the SSBO
     * @param usage The usage of the buffer
     * @param access The access of the buffer
     */
    public ShaderStorageBuffer(int capacity, BufferUsage usage, BufferAccess access){
        //create the buffer java-side
        buffer = BufferUtils.createByteBuffer(capacity);

        //create the buffer opengl-side
        id = GL45.glGenBuffers();
        Globals.renderingEngine.checkError();
        GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, id);
        Globals.renderingEngine.checkError();
        GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, buffer, BufferEnums.getBufferUsage(usage, access));
        Globals.renderingEngine.checkError();
        GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER,id);
        Globals.renderingEngine.checkError();
        GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER,UNBIND_ADDRESS);
        Globals.renderingEngine.checkError();
    }

    /**
     * Uploads the java side buffer to opengl
     */
    public void upload(OpenGLState openGLState){
        long offset = 0;
        if(buffer.limit() > buffer.position()){
            //bind
            openGLState.glBindBufferBase(0, this);
            Globals.renderingEngine.checkError();

            //upload
            GL45.glBufferSubData(OpenGLBuffer.getTypeInt(this), offset, buffer);
            Globals.renderingEngine.checkError();
            
            //unbind
            openGLState.glUnbindBufferBase(0, this);
            Globals.renderingEngine.checkError();
        }
    }

    /**
     * Destroys the buffer
     */
    public void destroy(){
        if(buffer != null){
            //destroy opengl-side buffer
            GL45.glDeleteBuffers(id);
            
            //set the java-side buffer to be null
            buffer = null;
        }
    }

    /**
     * Gets the java-side buffer
     * @return The java-side buffer
     */
    public ByteBuffer getBuffer(){
        return this.buffer;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public BufferType getType() {
        return BufferType.GL_SHADER_STORAGE_BUFFER;
    }



}
