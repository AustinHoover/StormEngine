package electrosphere.engine;

import electrosphere.audio.AudioEngine;
import electrosphere.auth.AuthenticationManager;
import electrosphere.client.ClientState;
import electrosphere.client.block.cells.BlockTextureAtlas;
import electrosphere.client.entity.particle.ParticleService;
import electrosphere.client.terrain.cells.VoxelTextureAtlas;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.CameraHandler;
import electrosphere.controls.ControlCallback;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.MouseCallback;
import electrosphere.controls.ScrollCallback;
import electrosphere.controls.cursor.CursorState;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.assetmanager.AssetManager;
import electrosphere.engine.profiler.Profiler;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.config.NetConfig;
import electrosphere.net.monitor.NetMonitor;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.instance.InstanceManager;
import electrosphere.renderer.loading.ModelPretransforms;
import electrosphere.renderer.meshgen.EngineMeshgen;
import electrosphere.renderer.meshgen.FluidChunkModelGeneration;
import electrosphere.renderer.meshgen.GeometryModelGen;
import electrosphere.renderer.shader.ShaderOptionMap;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.renderer.texture.TextureMap;
import electrosphere.renderer.ui.ElementService;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.font.FontManager;
import electrosphere.server.ServerState;
import electrosphere.server.entity.poseactor.PoseModel;
import electrosphere.util.FileUtils;

/**
 * Global values
 */
public class Globals {

    /**
     * State for the engine
     */
    public static EngineState engineState;

    /**
     * The engine and game configuration
     */
    public static electrosphere.data.Config gameConfigCurrent;

    /**
     * State for the client
     */
    public static ClientState clientState;

    /**
     * State for the server
     */
    public static ServerState serverState;
    
    /**
     * The rendering engine
     */
    public static RenderingEngine renderingEngine;
    
    /**
     * The audio engine
     */
    public static AudioEngine audioEngine;

    /**
     * The asset manager
     */
    public static AssetManager assetManager;

    /**
     * Authentication manager
     */
    public static AuthenticationManager authenticationManager;
    
    
    //
    //Controls Handler
    //
    public static ControlHandler controlHandler;
    public static boolean updateCamera = true;
    public static ControlCallback controlCallback = new ControlCallback();
    public static MouseCallback mouseCallback = new MouseCallback();
    public static ScrollCallback scrollCallback = new ScrollCallback();
    public static CursorState cursorState = new CursorState();
    
    
    
    //
    //OpenGL - Other
    //
    public static int WINDOW_WIDTH;
    public static int WINDOW_HEIGHT;

    
    //
    //Renderer-adjacent data I need to move into config at some point
    //
    public static TextureMap textureMapDefault;
    public static ModelPretransforms modelPretransforms;
    public static ShaderOptionMap shaderOptionMap;
    public static FontManager fontManager;
    public static CameraHandler cameraHandler = new CameraHandler();

    //
    //Renderer-adjacent data I need to move to rendering engine at some point
    //
    public static VoxelTextureAtlas voxelTextureAtlas = new VoxelTextureAtlas();
    public static BlockTextureAtlas blockTextureAtlas = new BlockTextureAtlas();
    public static ElementService elementService;
    public static InstanceManager clientInstanceManager = new InstanceManager();
    public static ParticleService particleService;  

    //
    //To compress into a single "performance" object
    //
    public static final Profiler profiler = new Profiler();
    public static NetMonitor netMonitor;
    
    
    //
    //OpenGL - Abstracted engine objects
    //
    public static VisualShader defaultMeshShader;
    public static VisualShader terrainShaderProgram;
    public static VisualShader blockShader;
    
    
    /**
     * Inits globals
     */
    public static void initGlobals(){
        //load user settings
        Globals.WINDOW_WIDTH = 1920;
        Globals.WINDOW_HEIGHT = 1080;


        //spin up engine state
        Globals.engineState = new EngineState();

        //game config
        gameConfigCurrent = electrosphere.data.Config.loadDefaultConfig();
        NetConfig.readNetConfig();

        //client state
        Globals.clientState = new ClientState();

        //server state
        Globals.serverState = new ServerState();

        //load in default texture map
        textureMapDefault = TextureMap.construct("Textures/default_texture_map.json");
        //load model pretransforms
        modelPretransforms = FileUtils.loadObjectFromAssetPath("Models/modelPretransforms.json", ModelPretransforms.class);
        modelPretransforms.init();
        //load in shader options map
        shaderOptionMap = FileUtils.loadObjectFromAssetPath("Shaders/shaderoptions.json", ShaderOptionMap.class);
        shaderOptionMap.debug();
        //load asset manager
        assetManager = new AssetManager();

        //
        //Values that depend on the loaded config
        Globals.clientState.clientSelectedVoxelType = (VoxelType)gameConfigCurrent.getVoxelData().getTypes().toArray()[1];
        //net monitor
        if(Globals.gameConfigCurrent.getSettings().getNetRunNetMonitor()){
            netMonitor = new NetMonitor();
        }


        //add services here
        Globals.elementService = (ElementService)Globals.engineState.serviceManager.registerService(new ElementService());
        Globals.particleService = (ParticleService)Globals.engineState.serviceManager.registerService(new ParticleService());
        Globals.engineState.serviceManager.instantiate();
        //
        //End service manager


    }
    
    /**
     * Inits default audio resources
     */
    public static void initDefaultAudioResources(){
        String[] audioToInit = new String[]{
            "/Audio/ambienceWind1SeamlessMono.ogg",
            "/Audio/weapons/swordUnsheath1.ogg",
            "/Audio/weapons/swoosh-03.ogg",
            "/Audio/movement/Equip A.wav",
            "/Audio/weapons/collisions/FleshWeaponHit1.wav",
            "/Audio/weapons/collisions/Massive Punch A.wav",
            "/Audio/weapons/collisions/Massive Punch B.wav",
            "/Audio/weapons/collisions/Massive Punch C.wav",
            "Audio/weapons/collisions/Sword Hit A.wav",
            "Audio/weapons/collisions/Sword Hit B.wav",
            "Audio/weapons/collisions/Sword Hit C.wav",
            "Audio/weapons/collisions/Sword Hit D.wav",
            "Audio/weapons/collisions/Sword Hit E.wav",
            AssetDataStrings.UI_TONE_CONFIRM_PRIMARY,
            AssetDataStrings.UI_TONE_CONFIRM_SECONDARY,
            AssetDataStrings.UI_TONE_CURSOR_PRIMARY,
            AssetDataStrings.UI_TONE_CURSOR_SECONDARY,
            AssetDataStrings.UI_TONE_BACK_PRIMARY,
            AssetDataStrings.UI_TONE_BACK_SECONDARY,
            AssetDataStrings.UI_TONE_ERROR_PRIMARY,
            AssetDataStrings.UI_TONE_ERROR_SECONDARY,
            AssetDataStrings.UI_TONE_BUTTON_TITLE,
            AssetDataStrings.UI_SFX_ITEM_GRAB,
            AssetDataStrings.UI_SFX_ITEM_RELEASE,
            AssetDataStrings.UI_SFX_INVENTORY_OPEN,
            AssetDataStrings.UI_SFX_INVENTORY_CLOSE,
            AssetDataStrings.INTERACT_SFX_BLOCK_PICKUP,
            AssetDataStrings.INTERACT_SFX_BLOCK_PLACE,
            AssetDataStrings.INTERACT_SFX_DIG,
        };
        LoggerInterface.loggerStartup.INFO("Loading default audio resources");
        for(String path : audioToInit){
            Globals.assetManager.addAudioPathToQueue(path);
        }
        Globals.audioEngine.movementAudioService.init();
    }

    /**
     * Texture paths to be loaded when renderer inits
     */
    private static String[] defaultTexturePaths = new String[]{
        AssetDataStrings.TEXTURE_DEFAULT,
        "Textures/default_diffuse.png",
        "Textures/default_specular.png",
        "Textures/b1.png",
        "Textures/w1.png",
        "Textures/ow1.png",
        "Textures/ui/WindowBorder.png",
        "Textures/ui/uiOutline1.png",
        AssetDataStrings.UI_ENGINE_LOGO_1,
        AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_1,
        AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_2,
        AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_3,
        "Textures/ui/circle.png",
        "Textures/ui/square.png",
        "Textures/color/transparent_green.png",
        "Textures/color/transparent_magenta.png",
        "Textures/color/transparent_orange.png",
        "Textures/color/transparent_teal.png",
        "Textures/color/transparent_yellow.png",
        "Textures/bloodsplat1.png",
    };

    /**
     * The set of models who should correspond to no pose model
     */
    private static String[] defaultModelsWithNoPose = new String[]{
        AssetDataStrings.POSE_EMPTY,
        AssetDataStrings.UNITSPHERE,
        AssetDataStrings.UNITCYLINDER,
        AssetDataStrings.UNITCAPSULE,
        AssetDataStrings.UNITCUBE,
        AssetDataStrings.MODEL_BLOCK_SINGLE,
    };
    
    /**
     * Inits default graphical resources
     */
    public static void initDefaultGraphicalResources(){
        LoggerInterface.loggerStartup.INFO("Loading default graphical resources");

        //load default textures
        for(String defaultTexturePath: defaultTexturePaths){
            Globals.assetManager.addTexturePathtoQueue(defaultTexturePath);
        }

        //create font manager
        fontManager = new FontManager();
        fontManager.loadFonts();
        assetManager.registerModelWithPath(EngineMeshgen.createBitmapCharacter(), AssetDataStrings.BITMAP_CHARACTER_MODEL);
        //particle billboard model
        assetManager.registerModelWithPath(EngineMeshgen.createParticleModel(), AssetDataStrings.MODEL_PARTICLE);
        //initialize required windows
        WindowUtils.initBaseWindows();
        //init default shaderProgram
        defaultMeshShader = VisualShader.smartAssembleShader();
        //init terrain shader program
        terrainShaderProgram = VisualShader.loadSpecificShader("/Shaders/entities/terrain2/terrain2.vs", "/Shaders/entities/terrain2/terrain2.fs");
        blockShader = VisualShader.loadSpecificShader("/Shaders/entities/block/block.vs", "/Shaders/entities/block/block.fs");
        //init fluid shader program
        FluidChunkModelGeneration.fluidChunkShaderProgram = VisualShader.loadSpecificShader("/Shaders/entities/fluid2/fluid2.vs", "/Shaders/entities/fluid2/fluid2.fs");
        //init models
        assetManager.registerModelWithPath(GeometryModelGen.createUnitSphere(), AssetDataStrings.UNITSPHERE);
        assetManager.registerModelWithPath(GeometryModelGen.createUnitCylinder(), AssetDataStrings.UNITCYLINDER);
        assetManager.registerModelWithPath(GeometryModelGen.createUnitCube(), AssetDataStrings.UNITCUBE);
        assetManager.registerModelWithPath(GeometryModelGen.createBlockSingleModel(), AssetDataStrings.MODEL_BLOCK_SINGLE);
        assetManager.addModelPathToQueue("Models/basic/geometry/SmallCube.fbx");
        assetManager.addModelPathToQueue("Models/basic/geometry/unitcapsule.glb");
        assetManager.addModelPathToQueue("Models/basic/geometry/unitplane.fbx");
        assetManager.addModelPathToQueue("Models/basic/geometry/unitcube.fbx");
        assetManager.registerModelWithPath(GeometryModelGen.createPlaneModel("Shaders/core/plane/plane.vs", "Shaders/core/plane/plane.fs"), AssetDataStrings.MODEL_IMAGE_PLANE);
        assetManager.addShaderToQueue("Shaders/core/plane/plane.vs", "Shaders/core/plane/plane.fs");

        //init pose models for basic shapes
        PoseModel emptyPoseModel = PoseModel.createEmpty();
        for(String modelPath : defaultModelsWithNoPose){
            assetManager.registerPoseModelWithPath(emptyPoseModel, modelPath);
        }

        //image panel
        ImagePanel.imagePanelModelPath = assetManager.registerModel(GeometryModelGen.createPlaneModel("Shaders/core/imagepanel/imagepanel.vs", "Shaders/core/imagepanel/imagepanel.fs"));

        Globals.assetManager.addShaderToQueue("Shaders/ui/plainBox/plainBox.vs", "Shaders/ui/plainBox/plainBox.fs");
        
        //window content shader
        assetManager.addShaderToQueue("Shaders/ui/windowContent/windowContent.vs", "Shaders/ui/windowContent/windowContent.fs");
        
        //debug shaders
        assetManager.addShaderToQueue("Shaders/ui/debug/windowBorder/windowBound.vs", "Shaders/ui/debug/windowBorder/windowBound.fs");
        assetManager.addShaderToQueue("Shaders/ui/debug/windowContentBorder/windowContentBound.vs", "Shaders/ui/debug/windowContentBorder/windowContentBound.fs");

        //compute shaders
        assetManager.addComputeShaderToQueue(AssetDataStrings.COMPUTE_LIGHT_CLUSTER);
        assetManager.addComputeShaderToQueue(AssetDataStrings.COMPUTE_LIGHT_CULL);

        //as these assets are required for the renderer to work, we go ahead and
        //load them into memory now. The loading time penalty is worth it I think.
        Globals.assetManager.loadAssetsInQueue();
    }

    /**
     * Unloads scene
     */
    public static void unloadScene(){
        if(Globals.serverState != null){
            Globals.serverState.aiManager.shutdown();
            Globals.serverState.realmManager.reset();
            Globals.serverState.dbController.disconnect();
        }
        Globals.engineState.serviceManager.unloadScene();

        Globals.clientState = new ClientState();
        Globals.serverState = new ServerState();
    }

    /**
     * Resets global values
     */
    public static void resetGlobals(){
        if(Globals.serverState != null){
            Globals.serverState.aiManager.shutdown();
            Globals.serverState.realmManager.reset();
            Globals.serverState.dbController.disconnect();
        }
        //
        //Actual globals to destroy
        Globals.assetManager = null;
        Globals.elementService = null;
        Globals.clientState = null;
        Globals.serverState = null;
        Globals.audioEngine = null;
        Globals.engineState = null;
        Globals.renderingEngine = null;
        LoggerInterface.destroyLoggers();
    }

    

}
