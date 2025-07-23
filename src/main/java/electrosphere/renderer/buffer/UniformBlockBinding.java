package electrosphere.renderer.buffer;

/**
 * A buffer that can be bound to a uniform block
 */
public interface UniformBlockBinding extends OpenGLBuffer {
    
    /**
     * The address to use to unbind a buffer
     */
    static final int UNBIND_ADDRESS = 0;
    
}
