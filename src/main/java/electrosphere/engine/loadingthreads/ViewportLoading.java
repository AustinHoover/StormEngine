package electrosphere.engine.loadingthreads;

import java.util.concurrent.TimeUnit;

import org.joml.Vector3d;

import electrosphere.auth.AuthenticationManager;
import electrosphere.client.ui.menu.MenuGenerators;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.server.player.Player;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.server.db.DatabaseController;
import electrosphere.server.db.DatabaseUtils;

/**
 * Loads the viewport
 */
public class ViewportLoading {
    
    /**
     * Loads the viewport
     */
    protected static void loadViewport(Object[] params){
        //
        //show loading
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, ()->{
            Window loadingWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING);
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN), false);
            WindowUtils.replaceMainMenuContents(MenuGenerators.createEmptyMainMenu());
            loadingWindow.setVisible(true);
        });

        //
        //init realm manager with viewport realm
        Globals.serverState.realmManager.createViewportRealm(new Vector3d(0,0,0), new Vector3d(16,16,16));

        //
        //Disable LOD service
        Globals.serverState.lodEmitterService.setDisable(true);

        //
        //connect client to server
        LoggerInterface.loggerEngine.INFO("run server: " + EngineState.EngineFlags.RUN_SERVER + " run client: " + EngineState.EngineFlags.RUN_CLIENT);
        ViewportLoading.initInMemoryDB();
        LoadingUtils.initAuthenticationManager(true);
        Globals.clientState.clientUsername = "leveleditor";
        Globals.clientState.clientPassword = AuthenticationManager.getHashedString("leveleditor");
        LoadingUtils.initLocalConnection(true);
        //wait for player object creation
        while(Globals.serverState.playerManager.getPlayers().size() < 1){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new Error("Loading thread was interrupted - failed to initialize player!");
            }
        }
        //add player to viewport realm
        Player localPlayer = Globals.serverState.playerManager.getFirstPlayer();
        Globals.serverState.realmManager.first().getDataCellManager().addPlayerToRealm(localPlayer);

        //initialize the "real" objects simulation
        LoadingUtils.setSimulationsToReady();
        LoggerInterface.loggerEngine.INFO("[Server]Finished loading level editor");

        //request terrain data
        Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestMetadataMessage());

        //block for client world data
        while(Globals.clientState.clientWorldData == null){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new Error("Loading thread was interrupted - failed to get world data on client!");
            }
        }

        //Run client startup process
        ClientLoading.loadViewport(params);
    }

    /**
     * Initializes an in-memory db
     */
    private static void initInMemoryDB(){
        Globals.serverState.dbController.connect(DatabaseController.IN_MEMORY_PATH);
        DatabaseUtils.runScript(Globals.serverState.dbController,"createTables.sql");
    }

}
