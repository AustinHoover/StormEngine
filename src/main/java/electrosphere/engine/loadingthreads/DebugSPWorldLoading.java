package electrosphere.engine.loadingthreads;

import java.util.concurrent.TimeUnit;

import electrosphere.auth.AuthenticationManager;
import electrosphere.client.ui.menu.MenuGenerators;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.scene.SceneGenerator;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.server.saves.SaveUtils;

public class DebugSPWorldLoading {
    

    protected static void loadDebugSPWorld(Object[] params){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, ()->{
            Window loadingWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING);
            //show loading
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN), false);
            WindowUtils.replaceMainMenuContents(MenuGenerators.createEmptyMainMenu());
            loadingWindow.setVisible(true);
        });

        String saveName = "random_sp_world";
        if(!SaveUtils.getSaves().contains(saveName)){
            //
            //the juicy server GENERATION part
            //
            //init save structure
            SaveUtils.createOrOverwriteSave(saveName, SceneGenerator.createProceduralSceneFile(saveName, ""));
        }
        //load just-created save
        SaveUtils.loadSave(saveName, false);
        //initialize the "virtual" objects simulation
        LoadingUtils.initMacroSimulation();


        LoggerInterface.loggerEngine.INFO("run server: " + EngineState.EngineFlags.RUN_SERVER + " run client: " + EngineState.EngineFlags.RUN_CLIENT);
        //init authentication
        LoadingUtils.initAuthenticationManager(false);
        //initialize the local connection
        Globals.clientState.clientUsername = "testuser";
        Globals.clientState.clientPassword = AuthenticationManager.getHashedString("testpass");
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
        LoggerInterface.loggerEngine.INFO("[Server]Finished loading main world");

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
