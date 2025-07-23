package electrosphere.engine.loadingthreads;

import java.util.concurrent.TimeUnit;

import electrosphere.auth.AuthenticationManager;
import electrosphere.client.ui.menu.MenuGenerators;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.server.saves.SaveUtils;

/**
 * Loads a given level
 */
public class LevelLoading {

    /**
     * Loads the level editor
     */
    protected static void loadLevel(Object[] params){
        if(params.length < 1){
            throw new IllegalStateException("Trying to load level editor with insufficient params");
        }
        String saveName = (String)params[0];

        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, ()->{
            Window loadingWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING);
            //show loading
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN), false);
            WindowUtils.replaceMainMenuContents(MenuGenerators.createEmptyMainMenu());
            loadingWindow.setVisible(true);
        });

        //load save
        SaveUtils.loadSave(saveName, false);

        LoggerInterface.loggerEngine.INFO("run server: " + EngineState.EngineFlags.RUN_SERVER + " run client: " + EngineState.EngineFlags.RUN_CLIENT);
        //init authentication
        LoadingUtils.initAuthenticationManager(false);
        //initialize the local connection
        Globals.clientState.clientUsername = "leveleditor";
        Globals.clientState.clientPassword = AuthenticationManager.getHashedString("leveleditor");
        ServerConnectionHandler serverPlayerConnection = LoadingUtils.initLocalConnection(true);
        //wait for player object creation
        while(Globals.serverState.playerManager.getPlayers().size() < 1){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
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
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ex) {}
        }
        
        //spawn player character
        LoadingUtils.spawnLocalPlayerTestEntity(serverPlayerConnection, false);

        //request terrain data
        Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestMetadataMessage());

        //Run client startup process
        ClientLoading.loadClientWorld(params);
    }


}
