package electrosphere.engine.loadingthreads;

import electrosphere.client.ui.menu.MenuGenerators;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.scene.SceneGenerator;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.server.saves.SaveUtils;

public class ServerLoading {
    

    protected static void loadMainGameServer(Object[] params){
        if(params.length < 3){
            throw new Error("Invalid number of parameters!");
        }

        String saveName = (String)params[0];
        String username = (String)params[1];
        String password = (String)params[2];

        //setup ui for loading
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, ()->{
            Window loadingWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING);
            //show loading
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN), false);
            WindowUtils.replaceMainMenuContents(MenuGenerators.createEmptyMainMenu());
            loadingWindow.setVisible(true);
        });

        //load the server
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
        Globals.clientState.clientUsername = username;
        Globals.clientState.clientPassword = password;
        //init game specific stuff (ie different skybox colors)
        LoadingUtils.initGameGraphicalEntities();
        //log
        LoggerInterface.loggerEngine.INFO("[Server]Finished loading main world");

        //load client side
        Object[] subParams = new Object[]{
            true
        };
        ClientLoading.loadCharacterServer(subParams);
    }

}
