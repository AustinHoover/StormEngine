package electrosphere.renderer.debug;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glPolygonMode;

import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;


public class DebugRendering {

    public static boolean RENDER_DEBUG_OUTLINE_WINDOW = true;
    public static boolean RENDER_DEBUG_OUTLINE_DIV = false;
    public static boolean RENDER_DEBUG_OUTLINE_BUTTON = false;
    public static boolean RENDER_DEBUG_OUTLINE_LABEL = false;
    public static boolean RENDER_DEBUG_OUTLINE_ACTOR_PANEL = false;
    public static boolean RENDER_DEBUG_OUTLINE_SLIDER = false;
    public static boolean RENDER_DEBUG_UI_TREE = false;

    static int outlineMaskPosition = 0;
    
    public static void drawUIBoundsWireframe(){
        glDisable(GL_DEPTH_TEST);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        for(Element currentElement : Globals.elementService.getWindowList()){
            if(currentElement instanceof DrawableElement){
                DrawableElement drawable = (DrawableElement) currentElement;
                if(drawable.getVisible()){
                    drawable.draw(
                        Globals.renderingEngine.getRenderPipelineState(),
                        Globals.renderingEngine.getOpenGLState(),
                        Globals.renderingEngine.defaultFramebuffer,
                        0,
                        0
                    );
                }
            }
        }
        
        drawRect(0,0,Globals.WINDOW_WIDTH,Globals.WINDOW_HEIGHT,Globals.WINDOW_WIDTH,Globals.WINDOW_HEIGHT);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    static VisualShader windowDrawDebugProgram = null;
    static VisualShader elementDrawDebugProgram = null;
    static Model planeModel = null;
    public static void drawUIBounds(Framebuffer parentFramebuffer, Vector3f boxPosition, Vector3f boxDimensions, Vector4f color){
        if(Globals.renderingEngine.RENDER_FLAG_RENDER_UI_BOUNDS){
            if(planeModel == null){
                planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
            }
            if(elementDrawDebugProgram == null){
                elementDrawDebugProgram = Globals.assetManager.fetchShader("Shaders/ui/debug/windowContentBorder/windowContentBound.vs", "Shaders/ui/debug/windowContentBorder/windowContentBound.fs");
            }
            if(elementDrawDebugProgram != null && planeModel != null){
                parentFramebuffer.bind(Globals.renderingEngine.getOpenGLState());
                Globals.renderingEngine.getOpenGLState().setActiveShader(Globals.renderingEngine.getRenderPipelineState(), elementDrawDebugProgram);
                planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
                planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
                planeModel.pushUniformToMesh("plane", "color", color);
                // planeModel.drawUI();
                // if(Globals.assetManager.fetchShader("Shaders/plane/plane.vs", null, "Shaders/plane/plane.fs") != null){
                //     Globals.renderingEngine.setActiveShader(Globals.assetManager.fetchShader("Shaders/plane/plane.vs", null, "Shaders/plane/plane.fs"));
                // }
                //drawUI sets shader so overriding window bound shader
                planeModel.draw(Globals.renderingEngine.getRenderPipelineState(),Globals.renderingEngine.getOpenGLState());
            }
        }
    }

    public static void drawUIBoundsWindow(Framebuffer parentFramebuffer, Vector3f boxPosition, Vector3f boxDimensions, Vector4f color){
        if(Globals.renderingEngine.RENDER_FLAG_RENDER_UI_BOUNDS){
            if(planeModel == null){
                planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
            }
            if(windowDrawDebugProgram == null){
                windowDrawDebugProgram = Globals.assetManager.fetchShader("Shaders/ui/debug/windowBorder/windowBound.vs", "Shaders/ui/debug/windowBorder/windowBound.fs");
            }
            if(windowDrawDebugProgram != null && planeModel != null){
                parentFramebuffer.bind(Globals.renderingEngine.getOpenGLState());
                Globals.renderingEngine.getOpenGLState().setActiveShader(Globals.renderingEngine.getRenderPipelineState(), windowDrawDebugProgram);
                planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
                planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
                planeModel.pushUniformToMesh("plane", "color", color);
                // planeModel.drawUI();
                // if(Globals.assetManager.fetchShader("Shaders/plane/plane.vs", null, "Shaders/plane/plane.fs") != null){
                //     Globals.renderingEngine.setActiveShader(Globals.assetManager.fetchShader("Shaders/plane/plane.vs", null, "Shaders/plane/plane.fs"));
                // }
                //drawUI sets shader so overriding window bound shader
                planeModel.draw(Globals.renderingEngine.getRenderPipelineState(),Globals.renderingEngine.getOpenGLState());
            }
        }
    }

    static void drawRect(int posX, int posY, int width, int height, int parentWidth, int parentHeight){
        float ndcX = (float)posX/parentWidth;
        float ndcY = (float)posY/parentHeight;
        float ndcWidth = (float)width/parentWidth;
        float ndcHeight = (float)height/parentHeight;
        
        Vector3f boxPosition = new Vector3f(ndcX,ndcY,0);
        Vector3f boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
        
        Model planeModel = Globals.assetManager.fetchModel(ImagePanel.imagePanelModelPath);
        if(planeModel != null){
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.drawUI();
        } else {
            LoggerInterface.loggerRenderer.ERROR("DebugRendering.drawRect unable to find plane model!!", new Exception());
        }
    }

    public static void dumpUITree(){
        for(Element currentElement : Globals.elementService.getWindowList()){
            dumpUITree(currentElement, 1);
        }
    }

    public static void dumpUITree(Element currentElement, int increment){
        for(int i = 0; i < increment; i++){
            System.out.print("  ");
        }
        System.out.println(currentElement.getClass() + " pos:(" + currentElement.getAbsoluteX() + "," + currentElement.getAbsoluteY() + ") dims:(" + currentElement.getWidth() + "," + currentElement.getHeight() + ")");
        if(currentElement instanceof ContainerElement){
            ContainerElement container = (ContainerElement)currentElement;
            for(Element child : container.getChildren()){
                dumpUITree(child,increment+1);
            }
        }
    }


    public static void toggleOutlineMask(){
        outlineMaskPosition = outlineMaskPosition + 1;
        RENDER_DEBUG_OUTLINE_WINDOW = false;
        RENDER_DEBUG_OUTLINE_DIV = false;
        RENDER_DEBUG_OUTLINE_BUTTON = false;
        RENDER_DEBUG_OUTLINE_LABEL = false;
        RENDER_DEBUG_OUTLINE_ACTOR_PANEL = false;
        switch(outlineMaskPosition){
            case 1:
            RENDER_DEBUG_OUTLINE_DIV = true;
            break;
            case 2:
            RENDER_DEBUG_OUTLINE_LABEL = true;
            break;
            case 3:
            RENDER_DEBUG_OUTLINE_BUTTON = true;
            break;
            case 4:
            RENDER_DEBUG_OUTLINE_ACTOR_PANEL = true;
            break;
            case 5:
            RENDER_DEBUG_OUTLINE_SLIDER = true;
            break;
            default:
            case 0:
            outlineMaskPosition = 0;
            RENDER_DEBUG_OUTLINE_WINDOW = true;
            break;
        }
    }

}
