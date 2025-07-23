package electrosphere.engine.loadingthreads;

import electrosphere.logger.LoggerInterface;

/**
 * Threads for loading engine state
 */
public class LoadingThread extends Thread {
    
    /**
     * The types of threads available
     */
    public static enum LoadingThreadType {

        /**
         * Loads the main game title menu
         */
        TITLE_MENU,

        /**
         * Loads the main game title menu
         */
        RETURN_TITLE_MENU,

        /**
         * Loads the main game
         */
        MAIN_GAME,

        /**
         * Loads the character creation menus on the client
         */
        CHARACTER_SERVER,

        /**
         * Loads the client world
         */
        CLIENT_WORLD,

        /**
         * Loads a random singleplayer debug world
         */
        DEBUG_RANDOM_SP_WORLD,

        /**
         * Loads a chunk generation testing realm
         */
        CHUNK_GENERATION_REALM,

        /**
         * Loads the level editor
         */
        LEVEL_EDITOR,

        /**
         * Loads a level
         */
        LEVEL,

        /**
         * Loads the main menu ui for the demo version of the client
         */
        DEMO_MENU,

        /**
         * Loads the script engine code from disk
         */
        SCRIPT_ENGINE,

        /**
         * Load viewport
         */
        LOAD_VIEWPORT,

        /**
         * Loads initial assets
         */
        INIT_ASSETS,

    }
    
    /**
     * The type of loading to run
     */
    LoadingThreadType threadType;

    //the params provided to this thread in particular
    Object[] params;

    //tracks whether the thread is done loading or not
    boolean isDone = false;
    
    /**
     * Creates the work for a loading thread
     * @param type The type of thread
     * @param params The params provided to the thread
     */
    public LoadingThread(LoadingThreadType type, Object ... params){
        threadType = type;
        this.params = params;
    }
    
    @Override
    public void run(){
        try {
            LoadingThread.execSync(this.threadType, this.params);
        } catch(Throwable e){
            LoggerInterface.loggerEngine.ERROR("Loading thread failed!", e);
        }
        isDone = true;
    }

    /**
     * Executes a loading task in the current thread
     * @param type The type of loading task
     * @param params The params for the loading task
     */
    public static void execSync(LoadingThreadType type, Object ... params){
        switch(type){
            
            case TITLE_MENU: {
                MainMenuLoading.loadMainMenu(params);
            } break;

            case RETURN_TITLE_MENU: {
                MainMenuLoading.returnToMainMenu(params);
            } break;
                
            case MAIN_GAME: {
                ServerLoading.loadMainGameServer(params);
            } break;
                
            case CHARACTER_SERVER: {
                ClientLoading.loadCharacterServer(params);
            } break;
                
            case CLIENT_WORLD: {
                ClientLoading.loadClientWorld(params);
            } break;


            //intended to act like you went through the steps of setting up a vanilla settings SP world
            case DEBUG_RANDOM_SP_WORLD: {
                DebugSPWorldLoading.loadDebugSPWorld(params);
            } break;

            //Loads a realm used for chunk generation testing
            case CHUNK_GENERATION_REALM: {
                ChunkGenerationTestLoading.loadChunkGenerationTesting(params);
            } break;

            //loads the level editor
            case LEVEL_EDITOR: {
                LevelEditorLoading.loadLevelEditor(params);
            } break;

            //loads the save in Globals.currentSave as a level
            case LEVEL: {
                LevelLoading.loadLevel(params);
            } break;

            //the demo menu ui
            case DEMO_MENU: {
                DemoLoading.loadDemoMenu(params);
            } break;

            //Inits the script engine
            case SCRIPT_ENGINE: {
                EngineInitLoading.loadScriptingEngine(params);
            } break;

            //Loads the viewport
            case LOAD_VIEWPORT: {
                ViewportLoading.loadViewport(params);
            } break;

            //Load the initial assets
            case INIT_ASSETS: {
                InitialAssetLoading.loadData();
            } break;
            
        }
    }
    
    /**
     * Checks if the thread has finished loading
     * @return true if it has finished, false otherwise
     */
    public boolean isDone(){
        return isDone;
    }

    /**
     * Gets the type of the loading thread
     * @return The type
     */
    public LoadingThreadType getType(){
        return this.threadType;
    }
    
}
