package electrosphere.renderer.ui.elements;

import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.texture.Texture;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * A tooltip
 */
public class Tooltip extends StandardDrawableContainerElement {

    //color of the decorations for the tooltip popout
    Vector4f backgroundColor = new Vector4f(0.2f,0.2f,0.2f,1.0f);

    Vector3f boxPosition = new Vector3f();
    Vector3f boxDimensions = new Vector3f();
    Vector3f texPosition = new Vector3f(0,0,0);
    Vector3f texScale = new Vector3f(1,1,0);
    Material customMat = new Material();

    /**
     * Optional callback for aligning the tooltip to a given location
     */
    Consumer<Tooltip> alignmentCallback;

    /**
     * Creates a tooltip
     * @param alignmentCallback The logic for aligning the tooltip positionally
     * @param children The children of the tooltip
     * @return The tooltip
     */
    public static Tooltip create(Consumer<Tooltip> alignmentCallback, Element ... children){
        Tooltip tooltip = new Tooltip();
        tooltip.setAbsolutePosition(true);
        tooltip.setPositionX(0);
        tooltip.setPositionY(0);
        tooltip.alignmentCallback = alignmentCallback;
        for(Element child : children){
            tooltip.addChild(child);
        }
        Element windowElement = Globals.elementService.getWindow(WindowStrings.TOOLTIP_WINDOW);
        if(windowElement instanceof Window){
            Window windowView = (Window)windowElement;
            windowView.addChild(tooltip);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,windowView);
        }
        return tooltip;
    }

    /**
     * Creates a tooltip
     * @param children The children of the tooltip
     * @return The tooltip
     */
    public static Tooltip create(Element ... children){
        Tooltip tooltip = new Tooltip();
        tooltip.setAbsolutePosition(true);
        tooltip.setPositionX(0);
        tooltip.setPositionY(0);
        for(Element child : children){
            tooltip.addChild(child);
        }
        Element windowElement = Globals.elementService.getWindow(WindowStrings.TOOLTIP_WINDOW);
        if(windowElement instanceof Window){
            Window windowView = (Window)windowElement;
            windowView.addChild(tooltip);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,windowView);
        }
        return tooltip;
    }
    

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ) {
        //calculate alignment
        if(this.alignmentCallback != null){
            this.alignmentCallback.accept(this);
        }

        //
        //Draw decorations
        float ndcWidth =  (float)getWidth()/framebuffer.getWidth();
        float ndcHeight = (float)getHeight()/framebuffer.getHeight();
        float ndcX =      (float)this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX)/framebuffer.getWidth();
        float ndcY =      (float)this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY)/framebuffer.getHeight();
        boxPosition = new Vector3f(ndcX,ndcY,0);
        boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
        
        Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
        Texture windowFrame = null;
        windowFrame = Globals.assetManager.fetchTexture("Textures/b1.png");

        //this call binds the screen as the "texture" we're rendering to
        //have to call before actually rendering
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

        //error if assets are null
        if(planeModel == null || windowFrame == null){
            LoggerInterface.loggerRenderer.ERROR("Window unable to find plane model or window frame!!", new Exception());
        }

        //render background of window
        if(planeModel != null && windowFrame != null){
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
            planeModel.pushUniformToMesh("plane", "tDimension", texScale);
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", backgroundColor);
            customMat.setDiffuse(windowFrame);
            planeModel.getMeshes().get(0).setMaterial(customMat);
            planeModel.drawUI();
        }


        //
        //Draw children elements
        for(Element child : childList){
            ((DrawableElement)child).draw(renderPipelineState, openGLState, framebuffer, framebufferPosX, framebufferPosY);
        }
    }

    /**
     * Destroys the tooltip
     * @param target The tooltip to destroy
     */
    public static void destroy(Tooltip target){
        if(target == null){
            throw new Error("Tooltip is null");
        }
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,()->{
            Window tooltipWindow = (Window)Globals.elementService.getWindow(WindowStrings.TOOLTIP_WINDOW);
            tooltipWindow.removeChild(target);
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, target);
        });
    }

}
