package electrosphere.renderer.ui.elements;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import org.joml.Vector3f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.framebuffer.FramebufferUtils;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.texture.Texture;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * A drawable container component that draws to an internal framebuffer before rendering that framebuffer on its own draw call**
 */
public class BufferedStandardDrawableContainerElement  extends StandardDrawableContainerElement {
    
    /**
     * The default width of an actor panel
     */
    public static final int DEFAULT_WIDTH = 50;

    /**
     * The default height of an actor panel
     */
    public static final int DEFAULT_HEIGHT = 50;

    /**
     * The material that contains the internal framebuffer's texture
     */
    Material elementMat;
    
    /**
     * The internal framebuffer for this component (all children are rendered to this, then this texture is rendered to a quad itself)
     */
    Framebuffer elementBuffer;
    
    /**
     * Constructor
     */
    public BufferedStandardDrawableContainerElement(){
        super();
        elementMat = new Material();
        this.regenerateFramebuffer(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
        ) {

        float ndcWidth =  (float)getWidth()/framebuffer.getWidth();
        float ndcHeight = (float)getHeight()/framebuffer.getHeight();
        float ndcX =      (float)this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX)/framebuffer.getWidth();
        float ndcY =      (float)this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY)/framebuffer.getHeight();
        Vector3f boxPosition = new Vector3f(ndcX,ndcY,0);
        Vector3f boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
        Vector3f texPosition = new Vector3f(0,0,0);
        Vector3f texScale = new Vector3f(1,1,0);

        //grab assets required to render window
        Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
        Texture windowFrame = Globals.assetManager.fetchTexture("Textures/ui/uiFrame1.png");


        elementBuffer.bind(openGLState);
        openGLState.glViewport(elementBuffer.getWidth(), elementBuffer.getHeight());
        
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        for(Element child : childList){
            if(child instanceof DrawableElement){
                DrawableElement drawableChild = (DrawableElement) child;
                drawableChild.draw(
                    renderPipelineState,
                    openGLState,
                    this.elementBuffer,
                    this.getAbsoluteX(),
                    this.getAbsoluteY()
                );
            }
        }
        //this call binds the screen as the "texture" we're rendering to
        //have to call before actually rendering
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

        //render background of window
        if(planeModel != null && windowFrame != null){
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
            planeModel.pushUniformToMesh("plane", "tDimension", texScale);
            elementMat.setDiffuse(windowFrame);
            planeModel.getMeshes().get(0).setMaterial(elementMat);
            planeModel.drawUI();
        }
        
        if(planeModel != null){
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
            planeModel.pushUniformToMesh("plane", "tDimension", texScale);
            elementMat.setDiffuse(elementBuffer.getTexture());
            planeModel.getMeshes().get(0).setMaterial(elementMat);
            planeModel.drawUI();
        } else {
            LoggerInterface.loggerRenderer.ERROR("ScrollableContainer unable to find plane model!!", new Exception());
        }
    }

    @Override
    public void applyYoga(int parentX, int parentY){
        super.applyYoga(parentX, parentY);
        this.regenerateFramebuffer(this.getWidth(), this.getHeight());
    }

    /**
     * Regenerates the backing framebuffer
     * @param width The width of the new buffer
     * @param height The height of the new buffer
     */
    protected void regenerateFramebuffer(int width, int height){
        if(width <= 1 || height <= 1){
            throw new Error("Invalid dimensions set! " + width + " " + height);
        }
        if(elementBuffer != null){
            elementBuffer.free();
        }
        try {
            elementBuffer = FramebufferUtils.generateTextureFramebuffer(Globals.renderingEngine.getOpenGLState(), width, height);
        } catch(Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }
        elementMat.setDiffuse(elementBuffer.getTexture());
    }


}
