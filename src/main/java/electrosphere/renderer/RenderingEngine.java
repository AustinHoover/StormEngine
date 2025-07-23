package electrosphere.renderer;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.os.OSDragAndDrop;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.debug.DebugRendering;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.framebuffer.FramebufferUtils;
import electrosphere.renderer.framebuffer.Renderbuffer;
import electrosphere.renderer.hw.HardwareData;
import electrosphere.renderer.light.LightManager;
import electrosphere.renderer.meshgen.EngineMeshgen;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.pipelines.CompositePipeline;
import electrosphere.renderer.pipelines.FirstPersonItemsPipeline;
import electrosphere.renderer.pipelines.FoliagePipeline;
import electrosphere.renderer.pipelines.ImGuiPipeline;
import electrosphere.renderer.pipelines.MainContentNoOITPipeline;
import electrosphere.renderer.pipelines.MainContentPipeline;
import electrosphere.renderer.pipelines.NormalsForOutlinePipeline;
import electrosphere.renderer.pipelines.OutlineNormalsPipeline;
import electrosphere.renderer.pipelines.PostProcessingPipeline;
import electrosphere.renderer.pipelines.RenderScreenPipeline;
import electrosphere.renderer.pipelines.ShadowMapPipeline;
import electrosphere.renderer.pipelines.UIPipeline;
import electrosphere.renderer.pipelines.VolumeBufferPipeline;
import electrosphere.renderer.pipelines.debug.DebugContentPipeline;
import electrosphere.renderer.shader.StandardUniformManager;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.renderer.target.DrawTargetEvaluator;
import electrosphere.renderer.texture.Texture;

/**
 * The main object for the rendering engine
 */
public class RenderingEngine {
    


    /**
     * Handle for the window created by glfw
     */
    private long windowPtr = -1;

    /**
     * The glfw hardware data
     */
    public HardwareData hardwareData;
    
    
    public static final int GL_DEFAULT_FRAMEBUFFER = 0;
    public static final int GL_DEFAULT_RENDERBUFFER = 0;
    public static Texture screenTextureColor;
    public static Texture screenTextureDepth;
    public static Framebuffer screenFramebuffer;
    public static Renderbuffer screenRenderbuffer;
    public static int screenTextureVAO;
    public static VisualShader screenTextureShaders;
    public static VisualShader drawChannel;
    public Framebuffer defaultFramebuffer;


    //
    //The rendering engine config flags
    //
    public boolean RENDER_FLAG_RENDER_SHADOW_MAP = false;
    public boolean RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT = false;
    public boolean RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER = false;
    public boolean RENDER_FLAG_RENDER_BLACK_BACKGROUND = true;
    public boolean RENDER_FLAG_RENDER_WHITE_BACKGROUND = false;
    public boolean RENDER_FLAG_RENDER_UI = true;
    public boolean RENDER_FLAG_RENDER_UI_BOUNDS = false;
    /**
     * used to control whether the window is created with decorations or not (ie for testing)
     */
    public static boolean WINDOW_DECORATED = true;
    /**
     * used to control whether the window is created fullscreen or not (ie for testing)
     */
    public static boolean WINDOW_FULLSCREEN = false;




    
    //the version of glsl to init imgui with
    private static String glslVersion = null;


    //shadow stuff
    public static Texture lightBufferDepthTexture;


    
    //depth framebuffer/shader for shadow mapping
    public static VisualShader lightDepthShaderProgram;
    public static Framebuffer lightDepthBuffer;

    //framebuffers for transparent textures
    public static float[] transparencyAccumulatorClear;
    public static Texture transparencyAccumulatorTexture;
    public static float[] transparencyRevealageClear;
    public static Texture transparencyRevealageTexture;
    public static Framebuffer transparencyBuffer;
    public static VisualShader oitCompositeProgram;

    /*
    render normals
    */
    public static Texture gameImageNormalsTexture;
    public static Framebuffer gameImageNormalsFramebuffer;
    public static VisualShader renderNormalsShader;

    /*
    Perspective volumetrics
    */
    public static Matrix4d nearVolumeProjectionMatrix = new Matrix4d();
    public static Matrix4d midVolumeProjectionMatrix = new Matrix4d();
    public static Matrix4d farVolumeProjectionMatrix = new Matrix4d();
    public static VisualShader volumeDepthShaderProgram;
    public static Framebuffer volumeDepthBackfaceFramebuffer;
    public static Texture volumeDepthBackfaceTexture;
    public static Framebuffer volumeDepthFrontfaceFramebuffer;
    public static Texture volumeDepthFrontfaceTexture;
    public static float volumeDepthLinearCoef = 0.1f;
    public static float volumeDepthQuadCoef = 0.01f;

    /*
    Necessary static variables for drawing
     */
    public static Matrix4d modelTransformMatrix = new Matrix4d();

    /*
    Post processing effects (ie kernels) textures, framebuffers, shaders
    */
    public static Texture normalsOutlineTexture;
    public static Framebuffer normalsOutlineFrambuffer;
    public static VisualShader normalsOutlineShader;

    /*
    compositing functions
    */
    public static VisualShader compositeAnimeOutline;

    /**
     * The light manager for the rendering engine
     */
    LightManager lightManager;

    /**
     * The standard uniform manager
     */
    private StandardUniformManager standardUniformManager;
    
    /**
     * The output framebuffer
     */
    public static int outputFramebuffer = 0;

    /**
     * Height of the titlebar
     */
    protected int titlebarHeight = 0;

    //used in calculating projection matrix
    float aspectRatio = 1.0f;
    float verticalFOV = 90.0f;
    float nearClip = 0.01f;

    //matrices for drawing models
    private Matrix4d viewMatrix = new Matrix4d();
    private Matrix4d projectionMatrix = new Matrix4d();
    private Matrix4d lightDepthMatrix = new Matrix4d();

    /**
     * The default material
     */
    private Material materialDefault = Material.createExisting(AssetDataStrings.TEXTURE_DEFAULT);

    /**
     * the current state of the rendering pipeline
     */
    static RenderPipelineState renderPipelineState = new RenderPipelineState();

    /**
     * the opengl state
     */
    OpenGLState openGLState = new OpenGLState();

    /**
     * The opengl context the rendering engine is running within
     */
    OpenGLContext openGLContext;

    //render pipelines
    MainContentPipeline mainContentPipeline = new MainContentPipeline();
    MainContentNoOITPipeline mainContentNoOITPipeline = new MainContentNoOITPipeline();
    DebugContentPipeline debugContentPipeline = new DebugContentPipeline();
    FirstPersonItemsPipeline firstPersonItemsPipeline = new FirstPersonItemsPipeline();
    ShadowMapPipeline shadowMapPipeline = new ShadowMapPipeline();
    VolumeBufferPipeline volumeBufferPipeline = new VolumeBufferPipeline();
    NormalsForOutlinePipeline normalsForOutlinePipeline = new NormalsForOutlinePipeline();
    FoliagePipeline foliagePipeline = new FoliagePipeline();
    OutlineNormalsPipeline outlineNormalsPipeline = new OutlineNormalsPipeline();
    CompositePipeline compositePipeline = new CompositePipeline();
    PostProcessingPipeline postProcessingPipeline = new PostProcessingPipeline();
    UIPipeline uiPipeline = new UIPipeline();
    RenderScreenPipeline renderScreenPipeline = new RenderScreenPipeline();
    ImGuiPipeline imGuiPipeline;
    
    /**
     * Initializes the opengl context
     */
    public void createOpenglContext(){
        LoggerInterface.loggerRenderer.INFO("Create OpenGL Context");

        //
        //set error callback
        //
        GLFW.glfwSetErrorCallback((int error, long descriptionPtr) -> {
            String description = GLFWErrorCallback.getDescription(descriptionPtr);
            System.err.println(description);
        });

        //Initializes opengl
        boolean glfwInited = GLFW.glfwInit();
        if(!glfwInited){
            String message = "Failed to initialize glfw!\n" +
            "Error code: " + this.getGLFWErrorMessage(this.getGLFWError());
            throw new IllegalStateException(message);
        }

        //grab hardware data
        this.hardwareData = new HardwareData();

        //Gives hints to glfw to control how opengl will be used
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
        glslVersion = "#version 450";
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        //headless option
        if(EngineState.EngineFlags.RUN_HIDDEN){
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        }
        if(!RenderingEngine.WINDOW_DECORATED){
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        }
        if(EngineState.EngineFlags.ENGINE_DEBUG){
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }

        if(Globals.gameConfigCurrent.getSettings().getDisplayWidth() <= 0 || Globals.gameConfigCurrent.getSettings().getDisplayHeight() <= 0){
            throw new Error("Trying to create window with width or height less than 1! " + Globals.gameConfigCurrent.getSettings().getDisplayWidth() + " " + Globals.gameConfigCurrent.getSettings().getDisplayHeight());
        }
        //Creates the window reference object
        if(Globals.gameConfigCurrent.getSettings().displayFullscreen() || RenderingEngine.WINDOW_FULLSCREEN){
            //below line is for fullscreen
            this.windowPtr = GLFW.glfwCreateWindow(Globals.gameConfigCurrent.getSettings().getDisplayWidth(), Globals.gameConfigCurrent.getSettings().getDisplayHeight(), "ORPG", GLFW.glfwGetPrimaryMonitor(), NULL);
        } else {
            this.windowPtr = GLFW.glfwCreateWindow(Globals.gameConfigCurrent.getSettings().getDisplayWidth(), Globals.gameConfigCurrent.getSettings().getDisplayHeight(), "ORPG", NULL, NULL);
        }
        // Errors for failure to create window (IE: No GUI mode on linux ?)
        if (this.windowPtr == NULL) {
            String message = "Failed to create window!\n" +
            "Error code: " + this.getGLFWErrorMessage(this.getGLFWError());
            ;
            GLFW.glfwTerminate();
            throw new Error(message);
        }
        
        //set resize callback
        GLFW.glfwSetWindowSizeCallback(this.windowPtr, (long window, int width, int height) -> {
            Globals.WINDOW_HEIGHT = height;
            Globals.WINDOW_WIDTH = width;
        });
        //Makes the window that was just created the current OS-level window context
        GLFW.glfwMakeContextCurrent(this.windowPtr);
        //Maximize it
        GLFW.glfwMaximizeWindow(this.windowPtr);
        GLFW.glfwPollEvents();
        //grab actual framebuffer 
        IntBuffer xBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer yBuffer = BufferUtils.createIntBuffer(1);
        GLFW.glfwGetFramebufferSize(this.windowPtr, xBuffer, yBuffer);
        
        int bufferWidth = xBuffer.get();
        int bufferHeight = yBuffer.get();

        //get title bar size
        this.titlebarHeight = Globals.WINDOW_HEIGHT - bufferHeight;
        
        Globals.WINDOW_WIDTH = bufferWidth;
        Globals.WINDOW_HEIGHT = bufferHeight;
        if(bufferWidth == 0 || bufferHeight == 0){
            throw new Error("Failed to get width or height! " + Globals.WINDOW_WIDTH + " " + Globals.WINDOW_HEIGHT);
        }

        //
        // Attach controls callbacks
        //
        //set key callback
        GLFW.glfwSetKeyCallback(this.windowPtr, Globals.controlCallback);
        GLFW.glfwSetMouseButtonCallback(this.windowPtr, Globals.mouseCallback);
        GLFW.glfwSetScrollCallback(this.windowPtr, Globals.scrollCallback);

        //get title bar dimensions
//        setTitleBarDimensions();
        
        //Creates the OpenGL capabilities for the program.)
        GL.createCapabilities();

        GL45.glEnable(GL45.GL_DEBUG_OUTPUT);
        //register error callback
        GL45.glDebugMessageCallback((int source, int type, int id, int severity, int length, long messagePtr, long userParam) -> {
            if(type == GL45.GL_DEBUG_TYPE_ERROR){
                String message = GLDebugMessageCallback.getMessage(length, messagePtr);
                System.err.println(message);
            }
        }, bufferHeight);

        //get environment constraints
        openGLState.init();
        openGLContext = new OpenGLContext();

        //init imgui pipeline
        imGuiPipeline = new ImGuiPipeline(this.windowPtr, glslVersion);

        //This enables Z-buffering so that farther-back polygons are not drawn over nearer ones
        openGLState.glDepthTest(true);
        
        // Support for transparency
        openGLState.glBlend(true);
        openGLState.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //this disables vsync to make game run faster
        //https://stackoverflow.com/questions/55598376/glfwswapbuffers-is-slow
        if(!Globals.gameConfigCurrent.getSettings().graphicsPerformanceEnableVSync()){
            GLFW.glfwSwapInterval(0);
        }

        //clear screen
        GL45.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL45.glClear(GL45.GL_COLOR_BUFFER_BIT | GL45.GL_DEPTH_BUFFER_BIT);
        
        
        
        //init screen rendering quadrant
        screenTextureVAO = EngineMeshgen.createScreenTextureVAO(this.openGLState);
        screenTextureShaders = VisualShader.loadSpecificShader("/Shaders/core/screentexture/simple1/simple1.vs", "/Shaders/core/screentexture/simple1/simple1.fs");

        //default framebuffer
        defaultFramebuffer = new Framebuffer(GL_DEFAULT_FRAMEBUFFER);

        //generate framebuffers
        Texture screenTextureColor = FramebufferUtils.generateScreenTextureColorAlpha(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
        RenderingEngine.screenTextureColor = screenTextureColor;
        Texture screenTextureDepth = FramebufferUtils.generateScreenTextureDepth(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
        RenderingEngine.screenTextureDepth = screenTextureDepth;
        try {
            Framebuffer screenFramebuffer = FramebufferUtils.generateScreenTextureFramebuffer(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY(), screenTextureColor, screenTextureDepth);
            RenderingEngine.screenFramebuffer = screenFramebuffer;
        } catch (Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }

        defaultFramebuffer.bind(openGLState);
        glBindRenderbuffer(GL_RENDERBUFFER, GL_DEFAULT_RENDERBUFFER);
        Globals.renderingEngine.checkError();

        //
        //Channel debug program
        //
        drawChannel = VisualShader.loadSpecificShader("/Shaders/core/screentexture/drawChannel/drawChannel.vs", "/Shaders/core/screentexture/drawChannel/drawChannel.fs");

        //
        //create light depth framebuffer/shader for shadowmapping
        //
        lightDepthShaderProgram = VisualShader.loadSpecificShader("/Shaders/core/lightDepth/lightDepth.vs", "/Shaders/core/lightDepth/lightDepth.fs");
        try {
            Framebuffer lightDepthBuffer = FramebufferUtils.generateDepthBuffer(openGLState);
            RenderingEngine.lightDepthBuffer = lightDepthBuffer;
        } catch(Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }
        Texture lightBufferDepthTexture = lightDepthBuffer.getDepthTexture();
        RenderingEngine.lightBufferDepthTexture = lightBufferDepthTexture;

        //
        //create volume depth framebuffer/shader for volumetric rendering
        //
        try {
            volumeDepthShaderProgram = VisualShader.loadSpecificShader("/Shaders/core/volumeBuffer/volumetric.vs", "/Shaders/core/volumeBuffer/volumetric.fs");
            volumeDepthBackfaceTexture = FramebufferUtils.generateDepthBufferTexture(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            volumeDepthBackfaceFramebuffer = FramebufferUtils.generateDepthBuffer(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY(), volumeDepthBackfaceTexture);
            volumeDepthFrontfaceTexture = FramebufferUtils.generateDepthBufferTexture(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            volumeDepthFrontfaceFramebuffer = FramebufferUtils.generateDepthBuffer(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY(), volumeDepthFrontfaceTexture);
        } catch(Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }

        //
        //Game normals
        //
        try {
            gameImageNormalsTexture = FramebufferUtils.generateScreenTextureColorAlpha(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            Texture gameImageNormalsDepthTexture = FramebufferUtils.generateScreenTextureDepth(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            gameImageNormalsFramebuffer = FramebufferUtils.generateScreenTextureFramebuffer(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY(), gameImageNormalsTexture, gameImageNormalsDepthTexture);
            renderNormalsShader = VisualShader.loadSpecificShader("Shaders/core/anime/renderNormals.vs", "Shaders/core/anime/renderNormals.fs");
        } catch(Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }

        //
        //Transparency framebuffers
        //
        try {
            transparencyAccumulatorClear = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
            transparencyAccumulatorTexture = FramebufferUtils.generateOITAccumulatorTexture(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            transparencyRevealageClear = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
            transparencyRevealageTexture = FramebufferUtils.generateOITRevealageTexture(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            transparencyBuffer = FramebufferUtils.generateOITFramebuffer(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY(), transparencyAccumulatorTexture, transparencyRevealageTexture, screenTextureDepth);
            oitCompositeProgram = VisualShader.loadSpecificShader("Shaders/core/oit/composite.vs", "Shaders/core/oit/composite.fs");
        } catch(Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }

        //projection matrices
        nearVolumeProjectionMatrix.setPerspective((float)(Globals.gameConfigCurrent.getSettings().getGraphicsFOV() * Math.PI /180.0f), (float)Globals.WINDOW_WIDTH / (float)Globals.WINDOW_HEIGHT, 0.1f, 100);

        //
        //Compositing textures and buffers
        //
        try {
            normalsOutlineTexture = FramebufferUtils.generateScreenTextureColorAlpha(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            normalsOutlineFrambuffer = FramebufferUtils.generateScreenTextureFramebuffer(openGLState, Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY(), normalsOutlineTexture);
            Globals.assetManager.addShaderToQueue("Shaders/core/anime/outlineNormals.vs", "Shaders/core/anime/outlineNormals.fs");
        } catch(Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }

        //
        //Compositing shaders
        //
        compositeAnimeOutline = VisualShader.loadSpecificShader("Shaders/core/anime/compositeAnimeOutline.vs", "Shaders/core/anime/compositeAnimeOutline.fs");

        //
        //Post processing pipeline init
        //
        postProcessingPipeline.init(openGLState);


        //instantiate light manager
        lightManager = LightManager.create();

        //
        //instantiate standard uniform manager
        standardUniformManager = StandardUniformManager.create();
        
        //
        //Fog
        //
        //enable fog
//        glEnable(GL_FOG);
//        //set the equation to use for fog
//        glFogf(GL_FOG_MODE,GL_LINEAR);
////        glFogf(GL_FOG_MODE,GL_EXP2);
//        //set the density of the fog
//        glFogf(GL_FOG_DENSITY,1.0f);
//        //these are applicable for the linear equation
//        glFogf(GL_FOG_START,0.8f);
//        glFogf(GL_FOG_END,1.0f);
//        //fog color
//        FloatBuffer fogColor = FloatBuffer.allocate(4);
//        fogColor.put(1.0f);
//        fogColor.put(1.0f);
//        fogColor.put(1.0f);
//        fogColor.put(1.0f);
//        fogColor.flip();
//        GL11.glFogfv(GL_FOG_COLOR, fogColor);

        //
        //Set file drag-and-drop for app
        //
        GLFW.glfwSetDropCallback(this.windowPtr, (long window, int count, long names) -> {
            PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
            List<String> paths = new LinkedList<String>();
            for(int i = 0; i < count; i++){
                String name = MemoryUtil.memUTF8(charPointers.get(i));
                paths.add(name);
            }
            OSDragAndDrop.handleDragAndDrop(paths);
        });

        //
        //Init pipelines
        //
        mainContentPipeline.setFirstPersonPipeline(firstPersonItemsPipeline);
        
        //
        // Projection and View matrix creation
        //
        this.verticalFOV = (float)(Globals.gameConfigCurrent.getSettings().getGraphicsFOV() * Math.PI /180.0f);
        //set local aspect ratio and global aspect ratio at the same time
        this.aspectRatio = Globals.WINDOW_WIDTH / (float)Globals.WINDOW_HEIGHT;
        this.projectionMatrix.setPerspective(this.verticalFOV, this.aspectRatio, this.nearClip, Globals.gameConfigCurrent.getSettings().getGraphicsViewDistance());
        this.viewMatrix.translation(new Vector3d(0.0f,0.0f,-3.0f));

        /**
         * Alert everyone that the rendering engine is ready
         */
        if(Globals.engineState.signalSystem != null){
            Globals.engineState.signalSystem.post(SignalType.RENDERING_ENGINE_READY);
        }
    }
    
    
    /**
     * Main function to draw the screen
     */
    public void drawScreen(){

        //element manager handle outstanding signals
        Globals.elementService.handleAllSignals();
        
        //calculate render angle for frustum culling
        if(this.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT){
            this.updateFrustumBox();
        }

        //update standard uniforms
        this.standardUniformManager.update();

        //determine draw targets
        if(this.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT && shouldRunPipelines()){
            DrawTargetEvaluator.evaluate();
        }
        
        //generate depth map
        if(this.RENDER_FLAG_RENDER_SHADOW_MAP && shouldRunPipelines()){
            shadowMapPipeline.render(openGLState, renderPipelineState);
        }

        //render volume buffer
        if(this.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT && shouldRunPipelines()){
            volumeBufferPipeline.render(openGLState, renderPipelineState);
        }
        
        //Update light buffer
        lightManager.update(renderPipelineState,openGLState,Globals.clientState.playerCamera);
        this.checkError();

        
        //Render content to the game framebuffer
        if(this.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT && shouldRunPipelines()){
            if(Globals.gameConfigCurrent.getSettings().getGraphicsPerformanceOIT()){
                mainContentPipeline.render(openGLState, renderPipelineState);
            } else {
                mainContentNoOITPipeline.render(openGLState, renderPipelineState);
            }
            this.checkError();
            debugContentPipeline.render(openGLState, renderPipelineState);
            this.checkError();
            normalsForOutlinePipeline.render(openGLState, renderPipelineState);
            this.checkError();
            firstPersonItemsPipeline.render(openGLState, renderPipelineState);
            this.checkError();
            outlineNormalsPipeline.render(openGLState, renderPipelineState);
            this.checkError();
            compositePipeline.render(openGLState, renderPipelineState);
            this.checkError();
            postProcessingPipeline.render(openGLState, renderPipelineState);
            this.checkError();
        }
        
        
        
        
        //Render the game framebuffer texture to a quad
        if(this.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER){
            renderScreenPipeline.render(openGLState, renderPipelineState);
            this.checkError();
        }
        
        //render ui
        uiPipeline.render(openGLState, renderPipelineState);
        this.checkError();
        
        //Render boundaries of ui elements
        if(this.RENDER_FLAG_RENDER_UI_BOUNDS){
            DebugRendering.drawUIBoundsWireframe();
        }

        /**
         * Render imgui
         */
        imGuiPipeline.render(openGLState, renderPipelineState);
        this.checkError();
        
        
        //check for errors
        // checkError();
        
        //check and call events and swap the buffers
        LoggerInterface.loggerRenderer.DEBUG_LOOP("GLFW Swap buffers");
        GLFW.glfwSwapBuffers(this.windowPtr);
        LoggerInterface.loggerRenderer.DEBUG_LOOP("GLFW Poll Events");
        GLFW.glfwPollEvents();
        LoggerInterface.loggerRenderer.DEBUG_LOOP("Check OpenGL Errors");
        this.checkError();
    }

    /**
     * Updates the frustum box of the render pipeline
     */
    private void updateFrustumBox(){
        renderPipelineState.updateFrustumIntersection(this.projectionMatrix, this.viewMatrix);
    }
    
    public void bindFramebuffer(int framebufferPointer){
        openGLState.glBindFramebuffer(GL_FRAMEBUFFER, framebufferPointer);
    }
    
    public void setTitleBarDimensions(){
        IntBuffer tLeft = BufferUtils.createIntBuffer(1);
        IntBuffer tTop = BufferUtils.createIntBuffer(1);
        IntBuffer tRight = BufferUtils.createIntBuffer(1);
        IntBuffer tBottom = BufferUtils.createIntBuffer(1);

        // Get the title bar dims
        GLFW.glfwGetWindowFrameSize(this.windowPtr, tLeft, tTop, tRight, tBottom);
        this.titlebarHeight = tTop.get();
//        System.out.println(tLeft.get() + " " + tTop.get() + " " + tRight.get() + " " + tBottom.get());
    }

    public Texture getVolumeBackfaceTexture(){
        return volumeDepthBackfaceTexture;
    }

    public Texture getVolumeFrontfaceTexture(){
        return volumeDepthFrontfaceTexture;
    }

    public LightManager getLightManager(){
        return lightManager;
    }

    public static void incrementOutputFramebuffer(){
        outputFramebuffer++;
        if(outputFramebuffer > 8){
            outputFramebuffer = 0;
        }
    }

    public static void setFOV(float verticalFOV){
        Globals.renderingEngine.verticalFOV = verticalFOV;
        Globals.renderingEngine.calculateProjectionMatrix();
    }

    public static void setAspectRatio(float aspectRatio){
        Globals.renderingEngine.aspectRatio = aspectRatio;
        Globals.renderingEngine.calculateProjectionMatrix();
    }

    /**
     * Calculates the projection matrix
     */
    public void calculateProjectionMatrix(){
        float radVerticalFOV = (float)(this.verticalFOV * Math.PI /180.0f);
        float nearClip = 0.001f;
        this.projectionMatrix.setPerspective(radVerticalFOV, this.aspectRatio, nearClip, Globals.gameConfigCurrent.getSettings().getGraphicsViewDistance());
    }

    /**
     * Gets the imgui pipeline
     * @return The imgui pipeline
     */
    public ImGuiPipeline getImGuiPipeline(){
        return this.imGuiPipeline;
    }

    /**
     * Gets the debug content pipeline
     * @return The debug content pipeline
     */
    public DebugContentPipeline getDebugContentPipeline(){
        return this.debugContentPipeline;
    }

    /**
     * Gets the current render pipeline state
     * @return The current render pipeline state
     */
    public RenderPipelineState getRenderPipelineState(){
        return renderPipelineState;
    }

    /**
     * Gets the shadow map pipeline
     * @return The shadow map pipeline
     */
    public ShadowMapPipeline getShadowMapPipeline(){
        return this.shadowMapPipeline;
    }

    /**
     * Gets the normals-for-outline pipeline
     * @return The normals-for-outline pipeline
     */
    public NormalsForOutlinePipeline getNormalsForOutlinePipeline(){
        return this.normalsForOutlinePipeline;
    }

    /**
     * Gets the main content pipeline
     * @return The main content pipeline
     */
    public MainContentPipeline getMainContentPipeline(){
        return this.mainContentPipeline;
    }

    /**
     * Gets the post processing pipeline
     * @return The post processing pipeline
     */
    public PostProcessingPipeline getPostProcessingPipeline(){
        return this.postProcessingPipeline;
    }

    /**
     * Gets the foliage pipeline
     * @return The foliage pipeline
     */
    public FoliagePipeline getFoliagePipeline(){
        return foliagePipeline;
    }

    /**
     * Gets the current opengl state
     * @return
     */
    public OpenGLState getOpenGLState(){
        return openGLState;
    }

    /**
     * Gets the opengl context that the rendering engine is running within
     * @return The opengl context
     */
    public OpenGLContext getOpenGLContext(){
        return openGLContext;
    }

    /**
     * Gets the default material
     * @return The default material
     */
    public Material getDefaultMaterial(){
        return this.materialDefault;
    }

    /**
     * Gets the view matrix
     * @return The view matrix
     */
    public Matrix4d getViewMatrix(){
        return viewMatrix;
    }

    /**
     * Gets the projection matrix
     * @return The projection matrix
     */
    public Matrix4d getProjectionMatrix(){
        return projectionMatrix;
    }

    /**
     * Gets the light depth matrix
     * @return The light depth matrix
     */
    public Matrix4d getLightDepthMatrix(){
        return lightDepthMatrix;
    }

    /**
     * Gets the near clip of the engine
     * @return The near clip
     */
    public float getNearClip(){
        return this.nearClip;
    }

    /**
     * Gets the window pointer of the rendering engine
     * @return The window pointer
     */
    public long getWindowPtr(){
        return this.windowPtr;
    }

    /**
     * Gets the standard uniform manager
     * @return The standard uniform manager
     */
    public StandardUniformManager getStandardUniformManager(){
        return this.standardUniformManager;
    }

    /**
     * Tries to recapture the screen
     */
    public static void recaptureIfNecessary(){
        if(Globals.controlHandler.shouldRecapture()){
            //Makes the window that was just created the current OS-level window context
            GLFW.glfwMakeContextCurrent(Globals.renderingEngine.windowPtr);
            // //Maximize it
            GLFW.glfwMaximizeWindow(Globals.renderingEngine.windowPtr);
            //grab focus
            GLFW.glfwFocusWindow(Globals.renderingEngine.windowPtr);
            //apply mouse controls state
            if(Globals.controlHandler.isMouseVisible()){
                Globals.controlHandler.showMouse();
            } else {
                Globals.controlHandler.hideMouse();
            }
            Globals.controlHandler.setRecapture(false);
        }
    }

    /**
     * Checks for any errors currently caught by OpenGL.
     * Refer: https://docs.gl/gl4/glGetError
     */
    public boolean checkError(){
        if(EngineState.EngineFlags.ERROR_CHECK_OPENGL){
            int error = this.getError();
            if(error != GL11.GL_NO_ERROR){
                LoggerInterface.loggerRenderer.ERROR("checkError - " + getErrorInEnglish(error), new Exception("OpenGL Error"));
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the current error code
     * @return The error code
     */
    public int getError(){
        int lastCode = 0;
        if(EngineState.EngineFlags.ERROR_CHECK_OPENGL){
            lastCode = GL11.glGetError();
            int currentCode = lastCode;
            while((currentCode = GL11.glGetError()) != GL11.GL_NO_ERROR){
                lastCode = currentCode;
            }
        }
        return lastCode;
    }

    /**
     * Gets the most recent GLFW Error
     * @return The most recent GLFW error
     */
    public int getGLFWError(){
        int lastCode = 0;
        if(EngineState.EngineFlags.ERROR_CHECK_OPENGL){
            try (MemoryStack stack = MemoryStack.stackPush()){
                lastCode = GLFW.glfwGetError(stack.callocPointer(1));
            }
        }
        return lastCode;
    }

    /**
     * Decodes the glfw error code
     * @param code The code
     * @return The decoded message
     */
    public String getGLFWErrorMessage(int code){
        switch(code){
            case GLFW.GLFW_INVALID_ENUM:
                return "GLFW_INVALID_ENUM";
            case GLFW.GLFW_INVALID_VALUE:
                return "GLFW_INVALID_VALUE";
            default:
                return "Unhandled value!";
        }
    }

    /**
     * Checks if pipelines should run
     * @return true if should render, false otherwise
     */
    private boolean shouldRunPipelines(){
        boolean rVal =
        Globals.clientState.playerCamera != null
        ;
        return rVal;
    }

    /**
     * Destroys the rendering engine
     */
    public void destroy(){

        //free framebuffers
        if(screenFramebuffer != null){
            screenFramebuffer.free();
        }
        if(screenRenderbuffer != null){
            screenRenderbuffer.free();
        }
        if(gameImageNormalsFramebuffer != null){
            gameImageNormalsFramebuffer.free();
        }
        if(lightDepthBuffer != null){
            lightDepthBuffer.free();
        }
        if(transparencyBuffer != null){
            transparencyBuffer.free();
        }
        if(volumeDepthBackfaceFramebuffer != null){
            volumeDepthBackfaceFramebuffer.free();
        }
        if(volumeDepthFrontfaceFramebuffer != null){
            volumeDepthFrontfaceFramebuffer.free();
        }
        if(normalsOutlineFrambuffer != null){
            normalsOutlineFrambuffer.free();
        }

        //null out
        screenFramebuffer = null;
        screenRenderbuffer = null;
        gameImageNormalsFramebuffer = null;
        lightDepthBuffer = null;
        transparencyBuffer = null;
        volumeDepthBackfaceFramebuffer = null;
        volumeDepthFrontfaceFramebuffer = null;
        normalsOutlineFrambuffer = null;


        //free textures
        if(screenTextureColor != null){
            screenTextureColor.free();
        }
        if(screenTextureDepth != null){
            screenTextureDepth.free();
        }
        if(gameImageNormalsTexture != null){
            gameImageNormalsTexture.free();
        }
        if(lightBufferDepthTexture != null){
            lightBufferDepthTexture.free();
        }
        if(transparencyRevealageTexture != null){
            transparencyRevealageTexture.free();
        }
        if(volumeDepthBackfaceTexture != null){
            volumeDepthBackfaceTexture.free();
        }
        if(volumeDepthFrontfaceTexture != null){
            volumeDepthFrontfaceTexture.free();
        }
        if(normalsOutlineTexture != null){
            normalsOutlineTexture.free();
        }
        
        //null out
        screenTextureColor = null;
        screenTextureDepth = null;
        gameImageNormalsTexture = null;
        lightBufferDepthTexture = null;
        transparencyRevealageTexture = null;
        volumeDepthBackfaceTexture = null;
        volumeDepthFrontfaceTexture = null;
        normalsOutlineTexture = null;

        //reset shader loading
        VisualShader.clearAlreadyCompiledMap();

        //destroy loaded resources
        Globals.assetManager.queueAllModelsForDeletion();
        Globals.assetManager.queueAllTexturesForDeletion();
        Globals.assetManager.queueAllShadersForDeletion();
        Globals.assetManager.handleDeleteQueue();

        //end glfw
        GLFW.glfwDestroyWindow(this.windowPtr);
        GLFW.glfwTerminate();
    }

    /**
     * Checks for any errors currently caught by OpenGL.
     * Refer: https://docs.gl/gl4/glGetError
     * @param errorCode The error code
     * @return The message
     */
    public static String getErrorInEnglish(int errorCode){
        switch(errorCode){
            case GL11.GL_NO_ERROR: {
                return null;
            }
            case GL11.GL_INVALID_ENUM: {
                return "GL_INVALID_ENUM";
            }
            case GL11.GL_INVALID_VALUE: {
                return "GL_INVALID_VALUE";
            }
            case GL11.GL_INVALID_OPERATION: {
                return "GL_INVALID_OPERATION";
            }
            case GL30.GL_INVALID_FRAMEBUFFER_OPERATION: {
                return "GL_INVALID_FRAMEBUFFER_OPERATION";
            }
            case GL11.GL_OUT_OF_MEMORY: {
                return "GL_OUT_OF_MEMORY";
            }
            case GL11.GL_STACK_UNDERFLOW: {
                return "GL_STACK_UNDERFLOW";
            }
            case GL11.GL_STACK_OVERFLOW: {
                return "GL_STACK_OVERFLOW";
            }
            default: {
                return "Un-enum'd error or no error. Code: " + errorCode;
            }
        }
    }
    
}
