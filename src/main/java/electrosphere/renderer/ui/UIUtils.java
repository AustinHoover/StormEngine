package electrosphere.renderer.ui;

import org.joml.Vector3f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * Utilities for working with the ui
 */
public class UIUtils {

    //the material that links the texture to draw
    static Material customMat = Material.createExisting("Textures/ui/uiOutline1.png");
    
    /**
     * Renders the outline of the provided element and all child elements of the rootEl
     * @param rootEl The top level element to parse downwards from
     */
    public static void renderOutlineTree(OpenGLState openGLState, RenderPipelineState renderPipelineState, Element rootEl){
        //draw this element
        float ndcWidth =  (float)rootEl.getWidth()/Globals.WINDOW_WIDTH;
        float ndcHeight = (float)rootEl.getHeight()/Globals.WINDOW_HEIGHT;
        float ndcX =      (float)(rootEl.getAbsoluteX())/Globals.WINDOW_WIDTH;
        float ndcY =      (float)(rootEl.getAbsoluteY())/Globals.WINDOW_HEIGHT;
        Vector3f boxPosition = new Vector3f(ndcX,ndcY,0);
        Vector3f boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
        Vector3f texPosition = new Vector3f(1,1,0);
        Vector3f texScale = new Vector3f(ndcWidth,ndcHeight,0);
        
        Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);

        openGLState.setActiveShader(renderPipelineState, RenderingEngine.screenTextureShaders);
        openGLState.glViewport(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);

        renderPipelineState.setUseMaterial(true);
        renderPipelineState.setBufferNonStandardUniforms(true);

        if(planeModel != null){
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
            planeModel.pushUniformToMesh("plane", "tDimension", texScale);
            planeModel.getMeshes().get(0).setMaterial(customMat);
            planeModel.drawUI();
        } else {
            LoggerInterface.loggerRenderer.ERROR("Image Panel unable to find plane model!!", new Exception());
        }
        //draw children
        if(rootEl instanceof ContainerElement){
            ContainerElement containerView = (ContainerElement)rootEl;
            for(Element child : containerView.getChildren()){
                renderOutlineTree(openGLState, renderPipelineState, child);
            }
        }
    }
}
