package electrosphere.engine.loadingthreads;

import electrosphere.client.ClientState;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.MenuGeneratorsTitleMenu;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.threads.LabeledThread.ThreadLabel;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.server.ServerState;

/**
 * Loading thread that returns the client to the main menu
 */
public class MainMenuLoading {
    

    /**
     * Resets all state to main menu
     * @param params not necessary
     */
    public static void returnToMainMenu(Object[] params){
        //
        //stop rendering game
        Globals.renderingEngine.RENDER_FLAG_RENDER_SHADOW_MAP = false;
        Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER_CONTENT = false;
        Globals.renderingEngine.RENDER_FLAG_RENDER_SCREEN_FRAMEBUFFER = true;
        Globals.renderingEngine.RENDER_FLAG_RENDER_UI = true;
        Globals.renderingEngine.RENDER_FLAG_RENDER_BLACK_BACKGROUND = true;

        //
        //reset state
        MainMenuLoading.resetClientState();
        MainMenuLoading.resetServerState();
        Globals.unloadScene();
        Globals.engineState.threadManager.interruptLabel(ThreadLabel.NETWORKING_CLIENT);
        Globals.engineState.threadManager.interruptLabel(ThreadLabel.NETWORKING_SERVER);
        Globals.engineState.threadManager.awaitThreadClose(ThreadLabel.NETWORKING_CLIENT);
        Globals.engineState.threadManager.awaitThreadClose(ThreadLabel.NETWORKING_SERVER);

        //
        //reveal in game main menu
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, ()->{
            WindowUtils.closeWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN);
        });
        MainMenuLoading.loadMainMenu(params);
    }

    /**
     * Resets the client state
     */
    private static void resetClientState(){
        Globals.clientState.clientConnection.setShouldDisconnect(true);
        Globals.clientState = new ClientState();
        Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(false);
    }

    /**
     * Resets the server state
     */
    private static void resetServerState(){
        Globals.serverState.server.close();
        Globals.serverState = new ServerState();
    }

    /**
     * Loads the main menu
     * @param params Params (this thread type does not accept any)
     */
    protected static void loadMainMenu(Object[] params){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION, ()->{
            Window loadingWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING);
            if(loadingWindow != null){
                WindowUtils.recursiveSetVisible(loadingWindow,false);
            }
            WindowUtils.focusWindow(WindowStrings.WINDOW_MENU_MAIN);
            Window mainMenuWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN);
            if(mainMenuWindow == null){
                WindowUtils.initMainMenuWindow();
            }
            mainMenuWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN);
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
            if(mainMenuWindow != null){
                WindowUtils.recursiveSetVisible(mainMenuWindow, true);
            }
        });
    }

}
