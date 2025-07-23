package electrosphere.renderer.ui.elements;

import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.texture.Texture;
import electrosphere.renderer.ui.elementtypes.DraggableElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.HoverableElement;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.events.DragEvent;
import electrosphere.renderer.ui.events.DragEvent.DragEventType;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.HoverEvent;

/**
 * A UI element that is a single, uninteractable image
 */
public class ImagePanel extends StandardElement implements DrawableElement, DraggableElement, HoverableElement {

    Vector4f color = new Vector4f(1.0f);
    
    //Asset path for the model data that is used to draw the image panel
    public static String imagePanelModelPath;

    //the path to the texture to use for this panel
    String texturePath;
    //the material that links the texture to draw
    Material customMat = new Material();
    //tracks whether the texture has been loaded or not
    boolean hasLoadedTexture = false;
    //the texture to use
    Texture texture = null;
    
    
    //rendering data for positioning the model
    Vector3f texPosition = new Vector3f(1,1,0);
    Vector3f texScale = new Vector3f(1,1,0);
    Vector3f boxPosition = new Vector3f();
    Vector3f boxDimensions = new Vector3f();

    //callbacks for different events this can accept
    DragEventCallback onDragStart;
    DragEventCallback onDrag;
    DragEventCallback onDragRelease;
    HoverEventCallback onHover;

    static final Vector3f windowDrawDebugColor = new Vector3f(0.0f,0.5f,1.0f);

    /**
     * Creates an image panel
     * @param texturePath the path to the texture
     * @return The image panel
     */
    public static ImagePanel createImagePanel(String texturePath){
        ImagePanel rVal = new ImagePanel(texturePath);
        rVal.setAlignSelf(YogaAlignment.Start);
        return rVal;
    }

    /**
     * Private constructor
     */
    private ImagePanel(String texturePath){
        super();
        this.texturePath = texturePath;
        if(texturePath != null){
            texture = Globals.assetManager.fetchTexture(this.texturePath);
        }
        if(texture != null){
            customMat.setDiffuse(texture);
            hasLoadedTexture = true;
        } else if(Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_BLACK) != null) {
            customMat.setDiffuse(Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_BLACK));
        }
    }

    /**
     * Creates an absolutely positioned image panel
     * @param x The x position
     * @param y The y position
     * @param width The width of the panel
     * @param height The height of the panel
     * @param texturePath The texture path to display in the panel
     */
    public static ImagePanel createImagePanelAbsolute(int x, int y, int width, int height, String texturePath){
        ImagePanel rVal = new ImagePanel(texturePath);
        rVal.setPositionX(x);
        rVal.setPositionY(y);
        rVal.setWidth(width);
        rVal.setHeight(height);
        rVal.setAbsolutePosition(true);
        return rVal;
    }
    
    /**
     * Sets the texture for this image panel
     * @param texture The texture to use
     */
    public void setTexture(Texture texture){
        customMat.setDiffuse(texture);
    }
    
    /**
     * Gets the texture being used by this image panel
     * @return The texture
     */
    public Texture getTexture(){
        return texture;
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
        boxPosition = new Vector3f(ndcX,ndcY,0);
        boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
        
        Model planeModel = Globals.assetManager.fetchModel(imagePanelModelPath);
        if(texture != null){
            customMat.setDiffuse(texture);
        } else if(this.texturePath != null){
            texture = Globals.assetManager.fetchTexture(this.texturePath);
        }

        //this call binds the screen as the "texture" we're rendering to
        //have to call before actually rendering
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

        renderPipelineState.setUseMaterial(true);
        renderPipelineState.setBufferNonStandardUniforms(true);

        if(planeModel != null){
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
            planeModel.pushUniformToMesh("plane", "tDimension", texScale);
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", color);
            planeModel.getMeshes().get(0).setMaterial(customMat);
            planeModel.drawUI();
        } else {
            LoggerInterface.loggerRenderer.ERROR("Image Panel unable to find plane model!!", new Exception());
        }

    }

    //controls whether the image panel is visible or not
    public boolean visible = false;

    @Override
    public boolean getVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean draw) {
        this.visible = draw;
    }
    
    @Override
    public boolean handleEvent(Event event){
        boolean propagate = true;
        if(event instanceof DragEvent){
            if(onDragStart != null && ((DragEvent)event).getType() == DragEventType.START){
                if(!onDragStart.execute((DragEvent)event)){
                    propagate = false;
                }
            }
            if(onDrag != null && ((DragEvent)event).getType() == DragEventType.DRAG){
                if(!onDrag.execute((DragEvent)event)){
                    propagate = false;
                }
            }
            if(onDragRelease != null && ((DragEvent)event).getType() == DragEventType.RELEASE){
                if(!onDragRelease.execute((DragEvent)event)){
                    propagate = false;
                }
            }
        }
        if(event instanceof HoverEvent){
            if(onHover != null){
                if(!onHover.execute((HoverEvent)event)){
                    propagate = false;
                }
            }
        }
        return propagate;
    }

    @Override
    public void setOnDragStart(DragEventCallback callback) {
        onDragStart = callback;
    }

    @Override
    public void setOnDrag(DragEventCallback callback) {
        onDrag = callback;
    }

    @Override
    public void setOnDragRelease(DragEventCallback callback) {
        onDragRelease = callback;
    }

    @Override
    public void setOnHoverCallback(HoverEventCallback callback) {
        onHover = callback;
    }
    
}
