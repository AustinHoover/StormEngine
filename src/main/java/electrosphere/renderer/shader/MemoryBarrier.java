package electrosphere.renderer.shader;

import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;

/**
 * Memory barrier operations
 */
public class MemoryBarrier {
    
    /**
     * Types of memory barriers
     */
    public static enum Barrier {

        /**
         * <p>
         * Vertex data sources from buffer objects after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * The set of buffer objects affected by this bit is derived from the buffer object bindings used for generic vertex attributes derived from the GL_VERTEX_ATTRIB_ARRAY_BUFFER bindings.
         * </p>
         */
        GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT,

        /**
         * <p>
         * Vertex array indices sourced from buffer objects after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * The buffer objects affected by this bit are derived from the GL_ELEMENT_ARRAY_BUFFER binding.
         * </p>
         */
        GL_ELEMENT_ARRAY_BARRIER_BIT,

        /**
         * <p>
         * Shader uniforms sourced from buffer objects after the barrier will reflect data written by shaders prior to the barrier. 
         * </p>
         */
        GL_UNIFORM_BARRIER_BIT,

        /**
         * <p>
         * Texture fetches from shaders, including fetches from buffer object memory via buffer textures, after the barrier will reflect data written by shaders prior to the barrier. 
         * </p>
         */
        GL_TEXTURE_FETCH_BARRIER_BIT,

        /**
         * <p>
         * Memory accesses using shader image load, store, and atomic built-in functions issued after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Additionally, image stores and atomics issued after the barrier will not execute until all memory accesses (e.g., loads, stores, texture fetches, vertex fetches) initiated prior to the barrier complete. 
         * </p>
         */
        GL_SHADER_IMAGE_ACCESS_BARRIER_BIT,

        /**
         * <p>
         * Command data sourced from buffer objects by Draw*Indirect commands after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * The buffer objects affected by this bit are derived from the GL_DRAW_INDIRECT_BUFFER binding.
         * </p>
         */
        GL_COMMAND_BARRIER_BIT,

        /**
         * <p>
         * Reads and writes of buffer objects via the GL_PIXEL_PACK_BUFFER and GL_PIXEL_UNPACK_BUFFER bindings (via glReadPixels, glTexSubImage1D, etc.) after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Additionally, buffer object writes issued after the barrier will wait on the completion of all shader writes initiated prior to the barrier.
         * </p>
         */
        GL_PIXEL_BUFFER_BARRIER_BIT,

        /**
         * <p>
         * Writes to a texture via glTex(Sub)Image*, glCopyTex(Sub)Image*, glCompressedTex(Sub)Image*, and reads via glGetTexImage after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Additionally, texture writes from these commands issued after the barrier will not execute until all shader writes initiated prior to the barrier complete.
         * </p>
         */
        GL_TEXTURE_UPDATE_BARRIER_BIT,

        /**
         * <p>
         * Reads or writes via glBufferSubData, glCopyBufferSubData, or glGetBufferSubData, or to buffer object memory mapped by glMapBuffer or glMapBufferRange after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Additionally, writes via these commands issued after the barrier will wait on the completion of any shader writes to the same memory initiated prior to the barrier.
         * </p>
         */
        GL_BUFFER_UPDATE_BARRIER_BIT,

        /**
         * <p>
         * Access by the client to persistent mapped regions of buffer objects will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Note that this may cause additional synchronization operations.
         * </p>
         */
        GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT,

        /**
         * <p>
         * Reads and writes via framebuffer object attachments after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Additionally, framebuffer writes issued after the barrier will wait on the completion of all shader writes issued prior to the barrier.
         * </p>
         */
        GL_FRAMEBUFFER_BARRIER_BIT,

        /**
         * <p>
         * Writes via transform feedback bindings after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Additionally, transform feedback writes issued after the barrier will wait on the completion of all shader writes issued prior to the barrier.
         * </p>
         */
        GL_TRANSFORM_FEEDBACK_BARRIER_BIT,

        /**
         * <p>
         * Accesses to atomic counters after the barrier will reflect writes prior to the barrier.
         * </p>
         */
        GL_ATOMIC_COUNTER_BARRIER_BIT,

        /**
         * <p>
         * Accesses to shader storage blocks after the barrier will reflect writes prior to the barrier.
         * </p>
         */
        GL_SHADER_STORAGE_BARRIER_BIT,

        /**
         * <p>
         * Writes of buffer objects via the GL_QUERY_BUFFER binding after the barrier will reflect data written by shaders prior to the barrier.
         * </p>
         * <p>
         * Additionally, buffer object writes issued after the barrier will wait on the completion of all shader writes initiated prior to the barrier.
         * </p>
         */
        GL_QUERY_BUFFER_BARRIER_BIT,
    }

    /**
     * Sets the memory barrier.
     * Essentially halts CPU execution until the given barrier is cleared.
     * IE, if GL_SHADER_STORAGE_BARRIER_BIT is passed, the CPU will wait until the SSBO values are fully set before continuing execution.
     * @param barrier The barrier to set
     */
    public static void glMemoryBarrier(Barrier barrier){
        int barrierVal = 0;
        switch(barrier){
            case GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT: {
                barrierVal = GL45.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT;
            } break;
            case GL_ELEMENT_ARRAY_BARRIER_BIT: {
                barrierVal = GL45.GL_ELEMENT_ARRAY_BARRIER_BIT;
            } break;
            case GL_UNIFORM_BARRIER_BIT: {
                barrierVal = GL45.GL_UNIFORM_BARRIER_BIT;
            } break;
            case GL_TEXTURE_FETCH_BARRIER_BIT: {
                barrierVal = GL45.GL_TEXTURE_FETCH_BARRIER_BIT;
            } break;
            case GL_SHADER_IMAGE_ACCESS_BARRIER_BIT: {
                barrierVal = GL45.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
            } break;
            case GL_COMMAND_BARRIER_BIT: {
                barrierVal = GL45.GL_COMMAND_BARRIER_BIT;
            } break;
            case GL_PIXEL_BUFFER_BARRIER_BIT: {
                barrierVal = GL45.GL_PIXEL_BUFFER_BARRIER_BIT;
            } break;
            case GL_TEXTURE_UPDATE_BARRIER_BIT: {
                barrierVal = GL45.GL_TEXTURE_UPDATE_BARRIER_BIT;
            } break;
            case GL_BUFFER_UPDATE_BARRIER_BIT: {
                barrierVal = GL45.GL_BUFFER_UPDATE_BARRIER_BIT;
            } break;
            case GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT: {
                barrierVal = GL45.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT;
            } break;
            case GL_FRAMEBUFFER_BARRIER_BIT: {
                barrierVal = GL45.GL_FRAMEBUFFER_BARRIER_BIT;
            } break;
            case GL_TRANSFORM_FEEDBACK_BARRIER_BIT: {
                barrierVal = GL45.GL_TRANSFORM_FEEDBACK_BARRIER_BIT;
            } break;
            case GL_ATOMIC_COUNTER_BARRIER_BIT: {
                barrierVal = GL45.GL_ATOMIC_COUNTER_BARRIER_BIT;
            } break;
            case GL_SHADER_STORAGE_BARRIER_BIT: {
                barrierVal = GL45.GL_SHADER_STORAGE_BARRIER_BIT;
            } break;
            case GL_QUERY_BUFFER_BARRIER_BIT: {
                barrierVal = GL45.GL_QUERY_BUFFER_BARRIER_BIT;
            } break;
        }
        GL45.glMemoryBarrier(barrierVal);
        Globals.renderingEngine.checkError();
    }

}
