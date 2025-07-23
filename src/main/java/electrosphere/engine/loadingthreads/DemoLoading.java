package electrosphere.engine.loadingthreads;

// import electrosphere.engine.Globals;
// import electrosphere.menu.WindowStrings;
// import electrosphere.menu.WindowUtils;
// import electrosphere.menu.mainmenu.MenuGeneratorsDemo;
// import electrosphere.renderer.ui.elements.Window;

/**
 * Loading routines for the demo version of the game
 */
public class DemoLoading {

    //the name of the save for the demo version of the game
    public static final String DEMO_LEVEL_PATH = "demo";

    /**
     * Loads the title menu elements for the demo version of the engine
     */
    public static void loadDemoMenu(Object[] params){
        throw new UnsupportedOperationException("Need to find a way to load just the demo level path");
        // String savePath = DEMO_LEVEL_PATH;

        // WindowUtils.replaceMainMenuContents(MenuGeneratorsDemo.createTitleMenu());

        // Window loadingWindow = (Window)Globals.elementManager.getWindow(WindowStrings.WINDOW_LOADING);
        // WindowUtils.recursiveSetVisible(loadingWindow,false);
        // WindowUtils.focusWindow(WindowStrings.WINDOW_MENU_MAIN);
        // WindowUtils.recursiveSetVisible(Globals.elementManager.getWindow(WindowStrings.WINDOW_MENU_MAIN), true);
    }

}
