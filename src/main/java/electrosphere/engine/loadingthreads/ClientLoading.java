package electrosphere.engine.loadingthreads;

import java.util.concurrent.TimeUnit;

import org.joml.Vector3d;

import electrosphere.client.block.cells.ClientBlockCellManager;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.entity.crosshair.Crosshair;
import electrosphere.client.fluid.cells.FluidCellManager;
import electrosphere.client.sim.ClientSimulation;
import electrosphere.client.terrain.cells.ClientDrawCellManager;
import electrosphere.client.terrain.foliage.FoliageCellManager;
import electrosphere.client.ui.menu.MenuGenerators;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.MenuCharacterCreation;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.cursor.CursorState;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.threads.LabeledThread.ThreadLabel;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.NetUtils;
import electrosphere.net.client.ClientNetworking;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.net.parser.net.message.LoreMessage;
import electrosphere.renderer.meshgen.GeometryMeshGen;

public class ClientLoading {
    

    /**
     * Number of frames to wait before updating status of draw cell manager loading
     */
    private static final int DRAW_CELL_UPDATE_RATE = 60;

    /**
     * Number of frames to wait before re-sending query for lore
     */
    private static final int LORE_RESEND_FRAMES = 1000;

    /**
     * Maximum time to wait for the draw cell to load before warning that it is taking a long time
     */
    private static final int MAX_DRAW_CELL_WAIT = 1000;

    /**
     * Slices in skysphere
     */
    private static final int SKYSPHERE_SLICES = 20;

    /**
     * stack in skysphere
     */
    private static final int SKYSPHERE_STACKS = 20;

    /**
     * Scale of skysphere
     */
    private static final float SKYSPHERE_SCALE = 5000.0f;


    /**
     * Loads the race data from the server
     * @param params no params
     */
    protected static void loadCharacterServer(Object[] params){
        if(params.length < 1){
            throw new Error("Expected 1 params!");
        }

        boolean useLocalConnection = (boolean)params[0];

        WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN), false);
        WindowUtils.replaceMainMenuContents(MenuGenerators.createEmptyMainMenu());
        WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING), true);
        WindowUtils.updateLoadingWindow("WAITING ON SERVER");
        //disable menu input
        Globals.controlHandler.hintUpdateControlState(ControlHandler.ControlsState.NO_INPUT);
        //initialize the client thread (client)
        if(useLocalConnection){
            LoadingUtils.initLocalConnection(true);
        } else {
            ClientLoading.initClientThread();
        }
        //while we don't know what races are playable, wait
        WindowUtils.updateLoadingWindow("WAITING ON LORE");
        int framesWaited = 0;
        while(Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces().size() == 0){
            if(framesWaited % LORE_RESEND_FRAMES == (LORE_RESEND_FRAMES - 1)){
                //request playable races
                Globals.clientState.clientConnection.queueOutgoingMessage(LoreMessage.constructRequestRacesMessage());
            }
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ex) {}
            framesWaited++;
        }
        WindowUtils.updateLoadingWindow("WAITING ON CHARACTERS");
        framesWaited = 0;
        while(Globals.clientState.clientCharacterManager.isWaitingOnList()){
            if(framesWaited % LORE_RESEND_FRAMES == (LORE_RESEND_FRAMES - 1)){
                //request characters available to this player
                Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructRequestCharacterListMessage());
            }
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ex) {}
            framesWaited++;
        }
        //once we have them, bring up the character creation interface
        //init character creation window
        //eventually should replace with at ui to select an already created character or create a new one
        WindowUtils.replaceMainMenuContents(MenuCharacterCreation.createCharacterSelectionWindow());
        //make loading dialog disappear
        WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING), false);
        //make character creation window visible
        WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN), true);
        //recapture window
        Globals.controlHandler.setRecapture(true);
        //log
        LoggerInterface.loggerEngine.INFO("[Client]Finished loading character creation menu");
        //set menu controls again
        Globals.controlHandler.hintUpdateControlState(ControlHandler.ControlsState.TITLE_MENU);
    }


    /**
     * Loads the client's world data
     */
    protected static void loadClientWorld(Object[] params){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, () -> {
            WindowUtils.closeWindow(WindowStrings.WINDOW_MENU_MAIN);
            WindowUtils.recursiveSetVisible(WindowStrings.WINDOW_LOADING, true);
            WindowUtils.updateLoadingWindow("LOADING");
        });
        //disable menu input
        Globals.controlHandler.hintUpdateControlState(ControlHandler.ControlsState.NO_INPUT);
        //initialize the "real" objects simulation
        initClientSimulation();
        LoadingUtils.setSimulationsToReady();
        //initialize the gridded managers (client)
        initDrawCellManager(true);
        initFoliageManager();
        initFluidCellManager(true);
        initBlockCellManager(true);
        //initialize the basic graphical entities of the world (skybox, camera)
        initWorldBaseGraphicalEntities();
        //sets micro and macro sims to ready if they exist
        setSimulationsToReady();
        //set simulations to ready if they exist
        LoadingUtils.setSimulationsToReady();
        //make loading window disappear
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, () -> {
            WindowUtils.recursiveSetVisible(WindowStrings.WINDOW_LOADING, false);
            Globals.renderingEngine.RENDER_FLAG_RENDER_SHADOW_MAP = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_UI = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_BLACK_BACKGROUND = false;
            Globals.renderingEngine.RENDER_FLAG_RENDER_WHITE_BACKGROUND = false;
        });
        //recapture screen
        Globals.controlHandler.setRecapture(true);
        LoggerInterface.loggerEngine.INFO("[Client]Finished loading main game");
        //set controls state
        Globals.controlHandler.hintUpdateControlState(ControlHandler.ControlsState.MAIN_GAME);
    }

    /**
     * Loads the viewport
     */
    protected static void loadViewport(Object[] params){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, () -> {
            WindowUtils.closeWindow(WindowStrings.WINDOW_MENU_MAIN);
            WindowUtils.recursiveSetVisible(WindowStrings.WINDOW_LOADING, true);
        });
        //disable menu input
        Globals.controlHandler.hintUpdateControlState(ControlHandler.ControlsState.NO_INPUT);

        //init camera
        Globals.clientState.playerCamera = CameraEntityUtils.spawnBasicCameraEntity(new Vector3d(0,0,0), new Vector3d(-1,0,0));
        Globals.cameraHandler.setTrackPlayerEntity(false);
        Globals.cameraHandler.setUpdate(false);
        //initialize the "real" objects simulation
        initClientSimulation();
        //initialize the cell managers (client)
        initDrawCellManager(false);
        initFluidCellManager(false);
        initBlockCellManager(false);
        initFoliageManager();

        //sets micro and macro sims to ready if they exist
        setSimulationsToReady();
        //make loading window disappear
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, () -> {
            WindowUtils.recursiveSetVisible(WindowStrings.WINDOW_LOADING, false);
            Globals.renderingEngine.RENDER_FLAG_RENDER_SHADOW_MAP = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_UI = true;
            Globals.renderingEngine.RENDER_FLAG_RENDER_BLACK_BACKGROUND = false;
            Globals.renderingEngine.RENDER_FLAG_RENDER_WHITE_BACKGROUND = false;
        });
        //recapture screen
        Globals.controlHandler.setRecapture(true);
        LoggerInterface.loggerEngine.INFO("[Client]Finished loading main game");
        //set controls state
        Globals.controlHandler.hintUpdateControlState(ControlHandler.ControlsState.MAIN_GAME);
    }










    /**
     * Inits the client networking thread and socket
     */
    private static void initClientThread(){
        //start client networking
        if(EngineState.EngineFlags.RUN_CLIENT){
            Globals.clientState.clientConnection = new ClientNetworking(NetUtils.getAddress(),NetUtils.getPort());
            Globals.engineState.threadManager.start(ThreadLabel.NETWORKING_CLIENT, new Thread(Globals.clientState.clientConnection));
        }
    }

    /**
     * Creates client simulation object
     */
    private static void initClientSimulation(){
        if(Globals.clientState.clientSimulation == null){
            Globals.clientState.clientSimulation = new ClientSimulation();
        }
    }

    /**
     * Sets client simulation object state to ready
     */
    private static void setSimulationsToReady(){
        Globals.clientState.clientSimulation.setReady(true);
    }


    /**
     * The mose basic graphical entities
     */
    private static void initWorldBaseGraphicalEntities(){
        /*
        
        Player Camera
        
        */
        CameraEntityUtils.initCamera();

        
        /*
        Targeting crosshair
        */
        Crosshair.initCrossHairEntity();

        /*
         * Skybox
         */
        Entity skybox = EntityCreationUtils.createClientSpatialEntity();
        DrawableUtils.makeEntityDrawable(skybox, () -> {
            return GeometryMeshGen.genSphere(SKYSPHERE_SLICES, SKYSPHERE_STACKS);
        });
        DrawableUtils.disableCulling(skybox);
        EntityUtils.getScale(skybox).mul(SKYSPHERE_SCALE);
        Globals.clientState.clientScene.registerBehaviorTree(() -> {
            EntityUtils.setPosition(skybox, EntityUtils.getPosition(Globals.clientState.playerEntity));
        });
        Globals.assetManager.queueOverrideMeshShader(EntityUtils.getActor(skybox).getBaseModelPath(), GeometryMeshGen.SPHERE_MESH_NAME, AssetDataStrings.SHADER_SKYBOX_VERT, AssetDataStrings.SHADER_SKYBOX_FRAG);

        /**
         * Cursors
         */
        CursorState.createCursorEntities();
    }

    /**
     * Inits the drawcell manager
     * @param blockForInit Blocks the thread until the draw cell manager is ready
     */
    static void initDrawCellManager(boolean blockForInit){
        int iterations = 0;
        WindowUtils.updateLoadingWindow("WAITING ON WORLD DATA");
        while(blockForInit && (Globals.clientState.clientWorldData == null || InitialAssetLoading.atlasQueuedTexture == null || !InitialAssetLoading.atlasQueuedTexture.hasLoaded()) && Globals.engineState.threadManager.shouldKeepRunning()){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
                iterations++;
            } catch (InterruptedException ex) {
                LoggerInterface.loggerEngine.ERROR(ex);
            }
            if(iterations > MAX_DRAW_CELL_WAIT){
                String message = "Draw cell took too long to init!\n" +
                Globals.clientState.clientWorldData + "\n" +
                InitialAssetLoading.atlasQueuedTexture.hasLoaded();
                throw new IllegalStateException(message);
            }
        }
        //initialize draw cell manager
        Globals.clientState.clientDrawCellManager = new ClientDrawCellManager(Globals.voxelTextureAtlas, Globals.clientState.clientWorldData.getWorldDiscreteSize());
        //Alerts the client simulation that it should start loading terrain
        Globals.clientState.clientSimulation.setLoadingTerrain(true);
        //wait for all the terrain data to arrive
        int i = 0;
        while(
            blockForInit &&
            !Globals.clientState.clientDrawCellManager.isInitialized() &&
            Globals.engineState.threadManager.shouldKeepRunning()
        ){
            i++;
            if(i % DRAW_CELL_UPDATE_RATE == 0){
                WindowUtils.updateLoadingWindow("WAITING ON SERVER TO SEND TERRAIN (" + 
                    Globals.clientState.clientDrawCellManager.getWaitingOnNetworkCount() + "/" + 
                    Globals.clientState.clientDrawCellManager.getPartitionLastFrameCount() + "/" + 
                    Globals.clientState.clientDrawCellManager.getRequestLastFrameCount() + "/" +
                    Globals.clientState.clientDrawCellManager.getGenerationLastFrameCount() + 
                ")");
            }
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Inits the fluid cell manager
     * @param blockForInit Blocks the thread until the fluid cell manager is ready
     */
    static void initFluidCellManager(boolean blockForInit){

        //wait for world data
        WindowUtils.updateLoadingWindow("WAITING ON WORLD DATA");
        while(blockForInit && Globals.clientState.clientWorldData == null && Globals.engineState.threadManager.shouldKeepRunning()){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
            }
        }

        //initialize draw cell manager
        Globals.clientState.fluidCellManager = new FluidCellManager(Globals.clientState.clientTerrainManager, 0, 0, 0);
        Globals.clientState.fluidCellManager.setGenerateDrawables(true);
        Globals.clientState.clientSimulation.setLoadingTerrain(true);

        //wait for all the terrain data to arrive
        WindowUtils.updateLoadingWindow("REQUESTING FLUID CHUNKS FROM SERVER (" + Globals.clientState.fluidCellManager.getUnrequestedSize() + ")");
        while(blockForInit && Globals.clientState.fluidCellManager.containsUnrequestedCell() && Globals.engineState.threadManager.shouldKeepRunning() && EngineState.EngineFlags.RUN_FLUIDS){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        //wait for undrawable cells
        // WindowUtils.updateLoadingWindow("WAITING ON SERVER TO SEND FLUID CHUNKS (" + Globals.fluidCellManager.getUndrawableSize() + ")");
        // while(blockForInit && Globals.fluidCellManager.containsUndrawableCell() && Globals.threadManager.shouldKeepRunning()){
        //     try {
        //         TimeUnit.MILLISECONDS.sleep(10);
        //     } catch (InterruptedException ex) {
        //         ex.printStackTrace();
        //     }
        // }
    }

    /**
     * Inits the block cell manager
     * @param blockForInit Blocks the thread until the block cell manager is ready
     */
    static void initBlockCellManager(boolean blockForInit){
        int iterations = 0;
        WindowUtils.updateLoadingWindow("WAITING ON WORLD DATA");
        while(blockForInit && (Globals.clientState.clientWorldData == null || InitialAssetLoading.atlasQueuedTexture == null || !InitialAssetLoading.atlasQueuedTexture.hasLoaded()) && Globals.engineState.threadManager.shouldKeepRunning()){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
                iterations++;
            } catch (InterruptedException ex) {
                LoggerInterface.loggerEngine.ERROR(ex);
            }
            if(iterations > MAX_DRAW_CELL_WAIT){
                String message = "Draw cell took too long to init!\n" +
                Globals.clientState.clientWorldData + "\n" +
                InitialAssetLoading.atlasQueuedTexture.hasLoaded();
                throw new IllegalStateException(message);
            }
        }
        Globals.clientState.clientBlockCellManager = new ClientBlockCellManager(Globals.blockTextureAtlas, Globals.clientState.clientWorldData.getWorldDiscreteSize());
        //Alerts the client simulation that it should start loading blocks
        Globals.clientState.clientSimulation.setLoadingTerrain(true);
        //wait for all the block data to arrive
        int i = 0;
        while(
            blockForInit &&
            !Globals.clientState.clientBlockCellManager.isInitialized() &&
            Globals.engineState.threadManager.shouldKeepRunning()
        ){
            i++;
            if(i % DRAW_CELL_UPDATE_RATE == 0){
                WindowUtils.updateLoadingWindow("WAITING ON SERVER TO SEND BLOCKS (" + 
                    Globals.clientState.clientBlockCellManager.getWaitingOnNetworkCount() + "/" + 
                    Globals.clientState.clientBlockCellManager.getPartitionLastFrameCount() + "/" + 
                    Globals.clientState.clientBlockCellManager.getRequestLastFrameCount() + "/" +
                    Globals.clientState.clientBlockCellManager.getGenerationLastFrameCount() + 
                ")");
            }
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Starts up the foliage manager
     */
    private static void initFoliageManager(){
        Globals.clientState.foliageCellManager = new FoliageCellManager(Globals.clientState.clientWorldData.getWorldDiscreteSize());
        Globals.clientState.foliageCellManager.init();
        // Globals.foliageCellManager.start();
    }




}
