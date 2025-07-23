package electrosphere.renderer.ui.elements;

import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.Event;

import static org.lwjgl.opengl.GL11.*;

public class ScrollableContainer extends BufferedStandardDrawableContainerElement {

    /**
     * The default width of a slider
     */
    static final int DEFAULT_HEIGHT = 50;

    /**
     * The default height of a slider
     */
    static final int DEFAULT_WIDTH = 50;

    /**
     * The color associated with the scrollable
     */
    Vector4f color = new Vector4f(1.0f);

    Vector3f boxPosition = new Vector3f();
    Vector3f boxDimensions = new Vector3f();
    Vector3f texPosition = new Vector3f(0,0,0);
    Vector3f texScale = new Vector3f(1,1,0);

    @Deprecated
    public ScrollableContainer(OpenGLState openGLState, int positionX, int positionY, int width, int height){
        super();

        float ndcX =      (float)positionX/Globals.WINDOW_WIDTH;
        float ndcY =      (float)positionY/Globals.WINDOW_HEIGHT;
        float ndcWidth =  (float)width/Globals.WINDOW_WIDTH;
        float ndcHeight = (float)height/Globals.WINDOW_HEIGHT;
        setWidth(width);
        setHeight(height);
        boxPosition = new Vector3f(ndcX,ndcY,0);
        boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
    }

    /**
     * Constructor
     */
    private ScrollableContainer(){
        super();
        this.setMinWidth(DEFAULT_WIDTH);
        this.setMinHeight(DEFAULT_HEIGHT);
    }

    /**
     * Creates a scrollable container element
     * @return The scrollable
     */
    public static ScrollableContainer createScrollable(){
        return new ScrollableContainer();
    }


    @Override
    public boolean handleEvent(Event event) {
        return false;
    }

    //recursively check if focused element is child of input element or is input element
    boolean containsFocusedElement(Element parent){
        Element focusedElement = Globals.elementService.getFocusedElement();
        if(parent == focusedElement){
            return true;
        } else if(parent instanceof ContainerElement){
            ContainerElement container = (ContainerElement)parent;
            for(Element child : container.getChildren()){
                if(containsFocusedElement(child)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ) {
        if(this.elementBuffer != null){
            //figure out if currently focused element is a child or subchild of this container
            // if(containsFocusedElement(this)){
            //     //if it is, if it is offscreen, calculate offset to put it onscreen
            //     Element focused = Globals.elementService.getFocusedElement();
            //     if(
            //         focused.getRelativeX() + focused.getWidth() > this.width ||
            //         focused.getRelativeY() + focused.getHeight() > this.height ||
            //         focused.getRelativeX() < 0 ||
            //         focused.getRelativeY() < 0
            //     ){
            //         int neededOffsetX = 0;
            //         int neededOffsetY = 0;
            //         //basically if we're offscreen negative, pull to positive
            //         //if we're offscreen positive and we're not as large as the screen, pull from the positive into focus
            //         //if we are larger than the screen, set position to 0
            //         if(focused.getRelativeX() < 0){
            //             neededOffsetX = -focused.getRelativeX();
            //         } else if(focused.getRelativeX() + focused.getWidth() > this.width){
            //             if(focused.getWidth() > this.width){
            //                 neededOffsetX = -focused.getRelativeX();
            //             } else {
            //                 neededOffsetX = -((focused.getRelativeX() - this.width) + focused.getWidth());
            //             }
            //         }
            //         if(focused.getRelativeY() < 0){
            //             neededOffsetY = -focused.getRelativeY();
            //         } else if(focused.getRelativeY() + focused.getHeight() > this.height){
            //             if(focused.getHeight() > this.height){
            //                 neededOffsetY = -focused.getRelativeY();
            //             } else {
            //                 neededOffsetY = -((focused.getRelativeY() - this.height) + focused.getHeight());
            //                 // System.out.println(focused.getPositionY() + " " + this.height + " " + focused.getHeight());
            //             }
            //         }
            //         //apply offset to all children
            //         for(Element child : childList){
            //             int newX = child.getRelativeX() + neededOffsetX;
            //             int newY = child.getRelativeY() + neededOffsetY;
            //             child.setPositionX(newX);
            //             child.setPositionY(newY);
            //             // System.out.println(currentX + " " + currentY);
            //         }
            //     }
            // }

            float ndcWidth =  (float)getWidth()/framebuffer.getWidth();
            float ndcHeight = (float)getHeight()/framebuffer.getHeight();
            float ndcX =      (float)this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX)/framebuffer.getWidth();
            float ndcY =      (float)this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY)/framebuffer.getHeight();
            boxPosition = new Vector3f(ndcX,ndcY,0);
            boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);

            //grab assets required to render window
            Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);


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
                        framebuffer,
                        framebufferPosX,
                        framebufferPosY
                    );
                }
            }
            //this call binds the screen as the "texture" we're rendering to
            //have to call before actually rendering
            framebuffer.bind(openGLState);
            openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

            if(planeModel != null){
                planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
                planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
                planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
                planeModel.pushUniformToMesh("plane", "tDimension", texScale);
                planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", color);
                elementMat.setDiffuse(elementBuffer.getTexture());
                planeModel.getMeshes().get(0).setMaterial(elementMat);
                planeModel.drawUI();
            } else {
                LoggerInterface.loggerRenderer.ERROR("ScrollableContainer unable to find plane model!!", new Exception());
            }
        }
    }
    
}
