package electrosphere.renderer.framebuffer;

import org.lwjgl.opengl.GL45;

/**
 * A renderbuffer
 */
public class Renderbuffer {
    /**
     * The pointer for the renderbuffer
     */
    int renderbuffer;
    
    /**
     * Constructor
     */
    public Renderbuffer(){
        renderbuffer = GL45.glGenRenderbuffers();
    }
    
    /**
     * Binds the renderbuffer
     */
    public void bind(){
        GL45.glBindRenderbuffer(GL45.GL_RENDERBUFFER, renderbuffer);
    }
    
    /**
     * Gets the render buffer id
     * @return
     */
    public int getFramebufferID(){
        return renderbuffer;
    }
    
    /**
     * Frees the render buffer
     */
    public void free(){
        GL45.glDeleteRenderbuffers(renderbuffer);
    }
}
