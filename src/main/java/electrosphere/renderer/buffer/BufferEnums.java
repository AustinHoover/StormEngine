package electrosphere.renderer.buffer;

import org.lwjgl.opengl.GL45;

/**
 * Enums for various buffer data
 */
public class BufferEnums {
    
    /**
     * Buffer usage enum
     */
    public static enum BufferUsage {
        
        /**
         * The data store contents will be modified once and used many times.
         */
        STATIC,

        /**
         * The data store contents will be modified once and used at most a few times.
         */
        STREAM,

        /**
         * The data store contents will be modified repeatedly and used many times.
         */
        DYNAMIC,
    }

    /**
     * Buffer access enum
     */
    public static enum BufferAccess {
        
        /**
         * The data store contents are modified by the application, and used as the source for GL drawing and image specification commands.
         */
        DRAW,

        /**
         * The data store contents are modified by reading data from the GL, and used to return that data when queried by the application.
         */
        READ,

        /**
         * The data store contents are modified by reading data from the GL, and used as the source for GL drawing and image specification commands.
         */
        COPY,
    }

    /**
     * Gets the constant for the given usage and access combination
     * @param usage The usage value
     * @param access The access value
     * @return The constant
     */
    public static int getBufferUsage(BufferUsage usage, BufferAccess access){
        if(usage == null || access == null){
            throw new IllegalArgumentException("Passed null value into getBufferUsage! " + usage + " " + access);
        }
        switch(usage){
            case STATIC: {
                switch(access){
                    case DRAW:
                    return GL45.GL_STATIC_DRAW;
                    case READ:
                    return GL45.GL_STATIC_READ;
                    case COPY:
                    return GL45.GL_STATIC_COPY;
                }
            } break;
            case STREAM: {
                switch(access){
                    case DRAW:
                    return GL45.GL_STREAM_DRAW;
                    case READ:
                    return GL45.GL_STREAM_READ;
                    case COPY:
                    return GL45.GL_STREAM_COPY;
                }
            } break;
            case DYNAMIC: {
                switch(access){
                    case DRAW:
                    return GL45.GL_DYNAMIC_DRAW;
                    case READ:
                    return GL45.GL_DYNAMIC_READ;
                    case COPY:
                    return GL45.GL_DYNAMIC_COPY;
                }
            } break;
        }
        throw new IllegalStateException("Somehow hit unreachable code! " + access + " " + usage);
    }

}
