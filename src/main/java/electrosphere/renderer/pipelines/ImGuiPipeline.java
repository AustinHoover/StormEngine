package electrosphere.renderer.pipelines;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import electrosphere.client.ui.menu.debug.ImGuiWindowMacros;
import electrosphere.client.ui.menu.editor.ImGuiEditorWindows;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.filediag.ImGuiFileDialogManager;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGuiContext;

/**
 * ImGui rendering pipeline
 */
public class ImGuiPipeline implements RenderPipeline {

    //
    //imgui related
    //
    //imgui internal objects
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl13 = new ImGuiImplGl3();

    //the context pointer for the core imgui objects
    private ImGuiContext imGuiContext = null;
    //if set to true, will render imgui windows
    private boolean imGuiShouldRender = true;
    //All imgui windows that should be displayed
    private List<ImGuiWindow> imGuiWindows = new CopyOnWriteArrayList<ImGuiWindow>();

    /**
     * Constructor for the pipeline
     * @param windowId The glfw window id
     * @param glfwVersion the glfw version
     */
    public ImGuiPipeline(long windowId, String glslVersion){
        //init imgui (must happen after gl.createCapabilities)
        imGuiContext = ImGui.createContext();
        if(!imGuiContext.isValidPtr()){
            throw new IllegalStateException("Imgui failed to initialize.");
        }
        ImPlot.createContext();
        imGuiGlfw.init(Globals.renderingEngine.getWindowPtr(),true);
        imGuiGl13.init(glslVersion);
    }

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        /**
         * Render imgui
         */
        if(imGuiShouldRender){
            imGuiGlfw.newFrame();
            ImGui.newFrame();
            if(ImGuiPipeline.shouldRenderDragAndDropTarget()){
                ImGuiPipeline.renderDragAndDropTarget();
            }
            ImGuiFileDialogManager.handleFileDialogs();
            for(ImGuiWindow window : imGuiWindows){
                window.draw();
            }
            ImGui.render();
            imGuiGl13.renderDrawData(ImGui.getDrawData());
            ImGuiWindowMacros.synchronizeMainDebugMenuVisibility();
            ImGuiEditorWindows.synchronizeMainEditorMenuVisibility();
        }
    }


    /**
     * Adds a n imgui window to the pipeline
     * @param window The window
     */
    public void addImGuiWindow(ImGuiWindow window){
        imGuiWindows.add(window);
    }

    /**
     * Removes an imgui window from the pipeline
     * @param window The window
     */
    public void removeImGuiWindow(ImGuiWindow window){
        imGuiWindows.remove(window);
    }

    /**
     * Checks if an imgui window is open that should capture controls
     * @return true if there is a control-capturing window open, false otherwise
     */
    public boolean shouldCaptureControls(){
        for(ImGuiWindow window : imGuiWindows){
            if(window == ImGuiWindowMacros.getMainDebugWindow() && window.isOpen()){
                return true;
            }
        }
        return false;
    }

    /**
     * Renders the drag and drop target window
     */
    private static void renderDragAndDropTarget(){
        //drag and drop target
        ImGui.begin("dndTargetWindow",
            0
            |
            ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBackground
            | ImGuiWindowFlags.NoNav | ImGuiWindowFlags.NoDecoration
            // | ImGuiWindowFlags.NoInputs
        );
        ImGuiViewport viewport = ImGui.getWindowViewport();
        ImGui.setWindowSize(viewport.getSizeX(), viewport.getSizeY());
        ImGui.setWindowPos(0, 0);
        ImGui.invisibleButton("dndTargetButton", viewport.getSizeX(), viewport.getSizeY());
        if(ImGui.beginDragDropTarget()){
            Object payload = ImGui.getDragDropPayload();
            if(payload != null){
                String text = ImGui.acceptDragDropPayload(String.class);
                if(text != null){
                    LoggerInterface.loggerUI.WARNING("ImGui received drag'n'drop: " + text + " - " + payload);
                    LoggerInterface.loggerUI.WARNING(Globals.controlHandler.getMousePositionNormalized() + "");
                }
            }
            ImGui.endDragDropTarget();
        }
        ImGui.end();
    }

    /**
     * Checks if the drag and drop target should be rendered
     * @return true if it should be rendered, false otherwise
     */
    private static boolean shouldRenderDragAndDropTarget(){
        return ImGuiEditorWindows.getHierarchyWindow().isOpen();
    }


    
}
