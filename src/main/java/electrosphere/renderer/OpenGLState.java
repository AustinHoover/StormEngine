package electrosphere.renderer;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.buffer.OpenGLBuffer;
import electrosphere.renderer.buffer.UniformBlockBinding;
import electrosphere.renderer.shader.Shader;
import electrosphere.renderer.shader.VisualShader;

/**
 * Encapsulates the state of opengl.
 * The main function of this class is to sit between any consuming classes and opengl.
 * It can then deduplicate calls based on the state that is already set.
 */
public class OpenGLState {

    /**
     * tracks whether caching should be used or not (to deduplicate opengl calls)
     */
    public static final boolean DISABLE_CACHING = false;

    /**
     * the max texture allowed by the current environment
     */
    private int MAX_TEXTURE_WIDTH;
    
    /**
     * the current viewport dimensions
     */
    private Vector2i viewport;

    /**
     * whether depth test is enabled or not
     */
    private boolean depthTest;

    /**
     * the current depth function
     */
    private int depthFunction;

    /**
     * whether to blend or not
     */
    private boolean blendTest;

    /**
     * the current blend func
     * map is (texture unit) -> [sfactor,dfactor]
     */
    private Map<Integer,int[]> blendFuncMap;

    /**
     * the key that contains the value of glBlendFunc (which would affect all buffers)
     */
    private static final int ALL_BUFFERS_KEY = -1;

    /**
     * the currently active texture
     */
    private int activeTexture;

    /**
     * Currently bound framebuffer type
     */
    private int framebufferType;

    /**
     * Pointer for currently bound framebuffer
     */
    private int framebufferPointer;

    /**
     * active shader
     */
    private Shader activeShader;

    /**
     * map of texture units and their corresponding texture pointers
     */
    private Map<Integer,Integer> unitToPointerMap;

    /**
     * A map of index -> uniform buffer bound to that index
     */
    private Map<Integer,UniformBlockBinding> indexBlockMap;

    /**
     * The current VAO pointer
     */
    private int currentVaoPtr = 0;


    /**
     * Initializes the opengl state
     */
    public void init(){
        this.MAX_TEXTURE_WIDTH = 0;
        this.viewport = new Vector2i(Globals.WINDOW_WIDTH,Globals.WINDOW_HEIGHT);
        GL45.glViewport(0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        this.depthTest = false;
        GL45.glDisable(GL45.GL_DEPTH_TEST);
        this.depthFunction = -1;
        this.blendTest = false;
        GL45.glDisable(GL45.GL_BLEND);
        this.blendFuncMap = new HashMap<Integer,int[]>();
        activeTexture = -1;
        framebufferType = GL45.GL_FRAMEBUFFER;
        framebufferPointer = 0;
        GL45.glBindFramebuffer(this.framebufferType, this.framebufferPointer);
        activeShader = null;
        GL45.glUseProgram(Shader.UNBIND_SHADER_ID);
        this.unitToPointerMap = new HashMap<Integer,Integer>();
        this.indexBlockMap = new HashMap<Integer,UniformBlockBinding>();
        this.storeCurrentEnvironmentContraints();
    }

    /**
     * Gets the constraints of the current environment (ie how large can the max texture be)
     */
    private void storeCurrentEnvironmentContraints(){

        //the array used to store values fetched from opengl
        int[] intFetchArray = new int[1];

        //get max texture size
        GL40.glGetIntegerv(GL40.GL_MAX_TEXTURE_SIZE, intFetchArray);
        MAX_TEXTURE_WIDTH = intFetchArray[0];

        //get current framebuffer data
        GL40.glGetIntegerv(GL40.GL_DRAW_FRAMEBUFFER_BINDING, intFetchArray);
        this.framebufferPointer = intFetchArray[0];

    }

    /**
     * Sets the viewport
     * @param x the width
     * @param y the height
     */
    public void glViewport(int x, int y){
        if(DISABLE_CACHING || x != viewport.x || y != viewport.y){
            viewport.x = x;
            viewport.y = y;
            GL40.glViewport(0, 0, viewport.x, viewport.y);
            Globals.renderingEngine.checkError();
        }
    }

    /**
     * Gets the viewport's dimensions
     * @return The viewport's dimensions
     */
    public Vector2i getViewport(){
        return new Vector2i(viewport);
    }

    /**
     * Sets the depth test
     * @param depthTest the depth test state
     */
    public void glDepthTest(boolean depthTest){
        // if(this.depthTest != depthTest){
            this.depthTest = depthTest;
            if(this.depthTest){
                GL40.glEnable(GL40.GL_DEPTH_TEST);
            } else {
                GL40.glDisable(GL40.GL_DEPTH_TEST);
            }
        // }
    }

    /**
     * Sets the depth function
     * @param depthFunction The depth function
     */
    public void glDepthFunc(int depthFunction){
        if(DISABLE_CACHING || this.depthFunction != depthFunction){
            this.depthFunction = depthFunction;
            GL40.glDepthFunc(this.depthFunction);
        }
    }

    /**
     * Sets the active texture
     * @param texture The active texture
     */
    public void glActiveTexture(int texture){
        if(DISABLE_CACHING || this.activeTexture != texture){
            this.activeTexture = texture;
            GL40.glActiveTexture(this.activeTexture);
            Globals.renderingEngine.checkError();
        }
    }

    /**
     * Binds a texture
     * @param textureType The type of texture
     * @param textureValue The texture pointer
     */
    public void glBindTexture(int textureType, int texturePointer){
        this.glBindTextureUnit(this.activeTexture, texturePointer, textureType);
    }

    /**
     * Binds a texture to a given texture unit if the texture hasn't already been bound to that unit
     * @param textureUnit The texture unit
     * @param texturePointer The texture pointer
     * @param textureType the type of texture (2d, 3d, etc)
     */
    public void glBindTextureUnit(int textureUnit, int texturePointer, int textureType){
        if(DISABLE_CACHING || !unitToPointerMap.containsKey(textureUnit) || unitToPointerMap.get(textureUnit)!=texturePointer){
            unitToPointerMap.put(textureUnit,texturePointer);
            this.glActiveTexture(textureUnit);
            GL40.glBindTexture(textureType,texturePointer);
            Globals.renderingEngine.checkError();
        }
    }

    /**
     * Binds a texture to a given texture unit if the texture hasn't already been bound to that unit
     * @param textureUnit The texture unit
     * @param texturePointer The texture pointer
     * @param textureType the type of texture (2d, 3d, etc)
     */
    public void glBindTextureUnitForce(int textureUnit, int texturePointer, int textureType){
        unitToPointerMap.put(textureUnit,texturePointer);
        this.activeTexture = textureUnit;
        GL40.glActiveTexture(this.activeTexture);
        Globals.renderingEngine.checkError();
        GL40.glBindTexture(textureType,texturePointer);
        Globals.renderingEngine.checkError();
    }

    /**
     * Binds a framebuffer
     * @param framebufferType the type of framebuffer (vanilla, renderbuffer, etc)
     * @param framebufferPointer the pointer to the framebuffer
     */
    public void glBindFramebuffer(int framebufferType, int framebufferPointer){
        if(DISABLE_CACHING || this.framebufferType != framebufferType || this.framebufferPointer != framebufferPointer){
            this.framebufferType = framebufferType;
            this.framebufferPointer = framebufferPointer;
            GL40.glBindFramebuffer(this.framebufferType,this.framebufferPointer);
            Globals.renderingEngine.checkError();
        }
    }

    /**
     * Gets the currently bound framebuffer's pointer
     * @return The pointer
     */
    public int getBoundFramebuffer(){
        return this.framebufferPointer;
    }

    /**
     * Sets the currently active shader program for the renderer
     * @param renderPipelineState The render pipeline state object
     * @param program The shader program to bind
     */
    public void setActiveShader(RenderPipelineState renderPipelineState, Shader program){
        if(DISABLE_CACHING || program != activeShader){
            //error check
            if(program.getId() != Shader.UNBIND_SHADER_ID && !GL40.glIsProgram(program.getId())){
                throw new Error("Tried to bind shader that is not an active program! " + program.getId());
            }

            //actually bind
            activeShader = program;
            GL40.glUseProgram(activeShader.getId());
            int glErrorCode = Globals.renderingEngine.getError();
            if(glErrorCode != 0){
                LoggerInterface.loggerRenderer.DEBUG_LOOP(RenderingEngine.getErrorInEnglish(glErrorCode));
            }
            Globals.renderingEngine.checkError();
            renderPipelineState.setCurrentShaderPointer(activeShader.getId());
        }
    }

    /**
     * Gets the active shader program
     * @return The active shader
     */
    public Shader getActiveShader(){
        return activeShader;
    }

    /**
     * Checks whether the provided shader program is the active shader program
     * @param program The program to check
     * @return true if the provided program is the active program, false otherwise
     */
    public boolean isCurrentShader(VisualShader program){
        return this.activeShader == program;
    }
    
    /**
     * Gets MAX_TEXTURE_WIDTH
     * @return MAX_TEXTURE_WIDTH
     */
    public int getMAX_TEXTURE_WIDTH(){
        return MAX_TEXTURE_WIDTH;
    }

    /**
     * 
     * @param sfactor
     * @param dfactor
     */
    public void glBlendFunc(int sfactor, int dfactor){
        GL40.glBlendFunc(sfactor, dfactor);
        //set all other keys
        for(int keyCurrent : this.blendFuncMap.keySet()){
            if(keyCurrent != ALL_BUFFERS_KEY){
                int[] funcs = this.blendFuncMap.get(keyCurrent);
                funcs[0] = sfactor;
                funcs[1] = dfactor;
            }
        }
    }

    /**
     * Sets the blend function for opengl
     * @param drawBufferIndex The draw buffer index that will have this function set
     * @param sfactor The source factor
     * @param dfactor The destination factor
     */
    public void glBlendFunci(int drawBufferIndex, int sfactor, int dfactor){
        if(!DISABLE_CACHING && this.blendFuncMap.containsKey(drawBufferIndex)){
            int[] funcs = this.blendFuncMap.get(drawBufferIndex);
            int sFactorCurr = funcs[0];
            int dFactorCurr = funcs[1];
            if(sfactor != sFactorCurr || dfactor != dFactorCurr){
                funcs[0] = sfactor;
                funcs[1] = dfactor;
                this.blendFuncMap.put(drawBufferIndex,funcs);
                GL40.glBlendFunci(drawBufferIndex, sfactor, dfactor);
            }
        } else {
            int[] funcs = new int[]{
                sfactor, dfactor
            };
            this.blendFuncMap.put(drawBufferIndex,funcs);
            GL40.glBlendFunci(drawBufferIndex, sfactor, dfactor);
        }

    }

    /**
     * Sets the blending status
     * @param blend true to blend, false otherwise
     */
    public void glBlend(boolean blend){
        // if(this.blendTest != blend){
            this.blendTest = blend;
            if(this.blendTest){
                GL40.glEnable(GL40.GL_BLEND);
            } else {
                GL40.glDisable(GL40.GL_BLEND);
            }
        // }
    }

    /**
     * Binds a buffer to a given uniform buffer block index
     * @param index the index
     * @param buffer the buffer
     */
    public void glBindBufferBase(int index, UniformBlockBinding buffer){
        if(this.indexBlockMap.containsKey(index)){
            UniformBlockBinding currentBuffer = this.indexBlockMap.get(index);
            if(currentBuffer == null || currentBuffer != buffer){
                //does not already contain index, should bind
                this.indexBlockMap.put(index,buffer);
                GL45.glBindBufferBase(OpenGLBuffer.getTypeInt(buffer), index, buffer.getId());
                Globals.renderingEngine.checkError();
            }
        } else {
            //does not already contain index, should bind
            this.indexBlockMap.put(index,buffer);
            GL45.glBindBufferBase(OpenGLBuffer.getTypeInt(buffer), index, buffer.getId());
            Globals.renderingEngine.checkError();
        }
    }

    /**
     * Unbinds a buffer from a given uniform buffer block index
     * @param index the index
     */
    public void glUnbindBufferBase(int index, UniformBlockBinding buffer){
        if(this.indexBlockMap.containsKey(index)){
            this.indexBlockMap.remove(index);
            GL45.glBindBufferBase(OpenGLBuffer.getTypeInt(buffer), index, UniformBlockBinding.UNBIND_ADDRESS);
            Globals.renderingEngine.checkError();
        }
    }

    /**
     * Binds a vertex array object buffer
     * @param vaoPointer The pointer to the VAO
     */
    public void glBindVertexArray(int vaoPointer){
        if(
            DISABLE_CACHING ||
            currentVaoPtr != vaoPointer
        ){
            this.currentVaoPtr = vaoPointer;
            GL45.glBindVertexArray(vaoPointer);
        }
    }

}
