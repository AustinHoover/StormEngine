package electrosphere.engine.loadingthreads;

import java.util.concurrent.TimeUnit;

import electrosphere.auth.AuthenticationManager;
import electrosphere.client.ui.menu.MenuGenerators;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.MenuGeneratorsLevelEditor.LevelDescription;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.scene.SceneFile;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.server.saves.SaveUtils;

/**
 * Loads the level editor
 */
public class LevelEditorLoading {

    /**
     * Loads the level editor
     */
    protected static void loadLevelEditor(Object[] params){

        //
        // Get params to create the level with
        //
        if(params.length < 1){
            throw new IllegalStateException("Trying to load level editor with insufficient params");
        }

        String saveName = null;
        SceneFile sceneFile = null;
        //figure out scene stuff
        if(params[0] instanceof LevelDescription){
            //fires when creating a level for the first time
            LevelDescription description = (LevelDescription)params[0];
            saveName = description.getName();
            sceneFile= description.getSceneFile();
        } else {
            //fires when subsequently editing
            saveName = (String)params[0];
        }

        //
        //Set params we would expect to run with this thread
        //
        EngineState.EngineFlags.RUN_CLIENT = true;
        EngineState.EngineFlags.RUN_SERVER = true;
        Globals.serverState.aiManager.setActive(false);

        
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, ()->{
            Window loadingWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING);
            //show loading
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN), false);
            WindowUtils.replaceMainMenuContents(MenuGenerators.createEmptyMainMenu());
            loadingWindow.setVisible(true);
        });

        if(!SaveUtils.getSaves().contains(saveName)){
            //init save structure
            SaveUtils.createOrOverwriteSave(saveName,sceneFile);
        }
        //load just-created save
        SaveUtils.loadSave(saveName, true);

        LoggerInterface.loggerEngine.INFO("run server: " + EngineState.EngineFlags.RUN_SERVER + " run client: " + EngineState.EngineFlags.RUN_CLIENT);
        //init authentication
        LoadingUtils.initAuthenticationManager(false);
        //initialize the local connection
        Globals.clientState.clientUsername = "leveleditor";
        Globals.clientState.clientPassword = AuthenticationManager.getHashedString("leveleditor");
        ServerConnectionHandler serverPlayerConnection = LoadingUtils.initLocalConnection(true);
        //wait for player object creation
        while(Globals.serverState.playerManager.getPlayers().size() < 1 || !Globals.clientState.clientConnection.isInitialized()){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //init game specific stuff (ie different skybox colors)
        LoadingUtils.initGameGraphicalEntities();
        //set simulations to ready if they exist
        LoadingUtils.setSimulationsToReady();
        //log
        LoggerInterface.loggerEngine.INFO("[Server]Finished loading level editor");

        //the less juicy client setup part
        while(Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces().size() == 0){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ex) {}
        }
        
        //spawn player character
        LoadingUtils.spawnLocalPlayerTestEntity(serverPlayerConnection, true);

        //request terrain data
        Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestMetadataMessage());

        //Run client startup process
        ClientLoading.loadClientWorld(params);
    }
    
}
