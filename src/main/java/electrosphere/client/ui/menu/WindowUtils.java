package electrosphere.client.ui.menu;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.ui.menu.ingame.CraftingWindow;
import electrosphere.client.ui.menu.ingame.InteractionTargetMenu;
import electrosphere.client.ui.menu.ingame.MenuGeneratorsInventory;
import electrosphere.client.ui.menu.ingame.InventoryMainWindow;
import electrosphere.client.ui.menu.mainmenu.MenuGeneratorsTitleMenu;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;

/**
 * Utils for native windowing framework
 */
public class WindowUtils {
    
    /**
     * Replaces the main menu contents
     * @param newMenu The new contents
     */
    public static void replaceMainMenuContents(Element newMenu){
        Element mainMenuEl = Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN);
        if(mainMenuEl != null && mainMenuEl instanceof Window){
            Window mainMenu = (Window) mainMenuEl;
            //todo: destroy elements as well
            mainMenu.clear();
            mainMenu.addChild(newMenu);
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, mainMenu);
            Globals.elementService.focusFirstElement();
        }
    }

    /**
     * Recursively sets a window as visible or not
     * @param topLevelMenu The window element
     * @param visible true for visible, false for invisible
     */
    public static void recursiveSetVisible(Element topLevelMenu, boolean visible){
        if(topLevelMenu instanceof DrawableElement){
            ((DrawableElement)topLevelMenu).setVisible(visible);
        }
        if(topLevelMenu instanceof ContainerElement){
            for(Element child : ((ContainerElement)topLevelMenu).getChildren()){
                recursiveSetVisible(child, visible);
            }
        }
        if(Globals.elementService.getFocusedElement() == null){
            Globals.elementService.focusFirstElement();
        }
        if(visible){
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,topLevelMenu);
        }
    }

    /**
     * Recursively sets a window as visible or not
     * @param topLevelMenu The window string
     * @param visible true for visible, false for invisible
     */
    public static void recursiveSetVisible(String windowString, boolean visible){
        WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(windowString), visible);
    }

    /**
     * Checks if the window string is visible
     * @param windowString The window string
     * @return true if is visible, false otherwise
     */
    public static boolean windowIsVisible(String windowString){
        Element windowElement = Globals.elementService.getWindow(windowString);
        if(windowElement instanceof DrawableElement){
            return ((DrawableElement)windowElement).getVisible();
        }
        return false;
    }

    /**
     * Checks if the window element is visible
     * @param windowElement The window element
     * @return true if is visible, false otherwise
     */
    public static boolean windowIsVisible(Element windowElement){
        if(windowElement instanceof DrawableElement){
            return ((DrawableElement)windowElement).getVisible();
        }
        return false;
    }

    /**
     * Checks whether the window registered to the provided string is open
     * @param windowString The window string
     * @return true if the window is open, false otherwise
     */
    public static boolean windowIsOpen(String windowString){
        Element windowElement = Globals.elementService.getWindow(windowString);
        return Globals.elementService.getWindowList().contains(windowElement);
    }

    /**
     * Checks if a window is open or not
     * @return The window
     */
    public static boolean controlBlockingWindowIsOpen(){
        return windowIsOpen(WindowStrings.LEVEL_EDTIOR_SIDE_PANEL);
    }

    /**
     * Gets an inventory window string by the id of the inventory
     * @param id the id
     * @return the window string for said inventory window
     */
    @Deprecated
    public static String getInventoryWindowID(int id){
        return "INVENTORY-" + id;
    }

    public static void replaceWindowContents(String window, Element content){
        Element mainMenuEl = Globals.elementService.getWindow(window);
        if(mainMenuEl != null && mainMenuEl instanceof Window){
            Window mainMenu = (Window) mainMenuEl;
            //todo: destroy elements as well
            mainMenu.getChildren().clear();
            mainMenu.addChild(content);
            Globals.elementService.focusFirstElement();
        }
    }

    public static void pushItemIconToItemWindow(Element itemIcon){
        Window targetWindow = (Window) Globals.elementService.getWindow(WindowStrings.WINDOW_ITEM_DRAG_CONTAINER);
        targetWindow.addChild(itemIcon);
        recursiveSetVisible(targetWindow,true);
        Globals.elementService.pushWindowToFront(targetWindow);
    }

    public static void cleanItemDraggingWindow(){
        Window targetWindow = (Window) Globals.elementService.getWindow(WindowStrings.WINDOW_ITEM_DRAG_CONTAINER);
        targetWindow.getChildren().clear();
        recursiveSetVisible(targetWindow, false);
    }

    public static void replaceWindow(String window, Window windowEl){
        Globals.elementService.unregisterWindow(window);
        Globals.elementService.registerWindow(window, windowEl);
        WindowUtils.recursiveSetVisible(windowEl, true);
        Globals.elementService.focusFirstElement();
    }

    /**
     * Sets the backout callback for the main menu window
     * @param onNav The callback
     */
    public static void setMainMenuBackoutCallback(NavigationEventCallback onNav){
        Window mainMenu = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN);
        mainMenu.setOnNavigationCallback(onNav);
    }

    /**
     * Cleans up a window visually and removes it from the element manager
     * @param window the window to clean up
     */
    public static void closeWindow(String window){
        Element windowEl = Globals.elementService.getWindow(window);
        recursiveSetVisible(windowEl, false);
        Globals.elementService.unregisterWindow(window);
    }

    /**
     * Tries to clear all tooltips
     */
    public static void clearTooltips(){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,()->{
            Window tooltipWindow = (Window)Globals.elementService.getWindow(WindowStrings.TOOLTIP_WINDOW);
            for(Element child : tooltipWindow.getChildren()){
                Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, child);
            }
            tooltipWindow.clearChildren();
        });
    }



    /**
     * [CLIENT ONLY] Attempts to redraw open player character inventory screens (ie when there's an inventory update over network)
     */
    public static void attemptRedrawInventoryWindows(){
        //make sure we're client and the player entity exists
        if(EngineState.EngineFlags.RUN_CLIENT){
            if(Globals.clientState.playerEntity != null){
                if(Globals.elementService.containsWindow(WindowStrings.WINDOW_CHARACTER)){
                    //redraw if necessary
                    WindowUtils.replaceWindow(WindowStrings.WINDOW_CHARACTER, InventoryMainWindow.createInventoryWindow(Globals.clientState.playerEntity));
                }
            }
            if(Globals.clientState.targetContainer != null){
                if(Globals.elementService.containsWindow(WindowStrings.WINDOW_INVENTORY_TARGET)){
                    //redraw if necessary
                    WindowUtils.replaceWindow(WindowStrings.WINDOW_INVENTORY_TARGET, InventoryMainWindow.createInventoryWindow(Globals.clientState.targetContainer));
                }
            }
        }
    }


    /**
     * Inits all base windows
     */
    public static void initBaseWindows(){
        WindowUtils.initLoadingWindow();
        WindowUtils.initItemDropWindow();
        WindowUtils.initItemDragContainerWindow();
        WindowUtils.initMainMenuWindow();
        WindowUtils.initTooltipWindow();
        Globals.elementService.registerWindow(WindowStrings.TARGET_TOOLTIP, InteractionTargetMenu.createInteractionTargetTooltipWindow());
    }

    /**
     * Inits the loading window
     */
    static void initLoadingWindow(){
        Window loadingWindow = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, false);
        Label loadingLabel = Label.createLabel("LOADING");
        loadingWindow.addChild(loadingLabel);
        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,loadingWindow);
        Globals.elementService.registerWindow(WindowStrings.WINDOW_LOADING, loadingWindow);
        WindowUtils.recursiveSetVisible(loadingWindow, true);
    }

    /**
     * Inits the main menu
     */
    public static void initMainMenuWindow(){
        Window mainMenuWindow = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, false);
        Globals.elementService.registerWindow(WindowStrings.WINDOW_MENU_MAIN, mainMenuWindow);
        WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
    }

    /**
     * Creates the window to receive item drop events
     */
    static void initItemDropWindow(){
        Element itemDropWindow = MenuGeneratorsInventory.worldItemDropCaptureWindow();
        Globals.elementService.registerWindow(WindowStrings.WINDDOW_ITEM_DROP, itemDropWindow);
        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, itemDropWindow);
    }

    static void initItemDragContainerWindow(){
        Window itemDragContainerWindow = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, false);
        Globals.elementService.registerWindow(WindowStrings.WINDOW_ITEM_DRAG_CONTAINER, itemDragContainerWindow);
    }

    /**
     * Updates the loading window
     */
    public static void updateLoadingWindow(String message){
        Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,() -> {
            Window loadingWindow = (Window)Globals.elementService.getWindow(WindowStrings.WINDOW_LOADING);
            loadingWindow.clear();
            loadingWindow.addChild(Label.createLabel(message));
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,loadingWindow);
        });
    }

    /**
     * Creates the tooltip window
     */
    static void initTooltipWindow(){
        Window tooltipWindow = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT, false);
        Globals.elementService.registerWindow(WindowStrings.TOOLTIP_WINDOW, tooltipWindow);
    }

    /**
     * Focuses a window
     * @param window The window to focus
     */
    public static void focusWindow(String window){
        Element windowEl = Globals.elementService.getWindow(window);
        if(windowEl != null){
            Globals.elementService.unregisterWindow(window);
            Globals.elementService.registerWindow(window, windowEl);
        }
    }

    /**
     * Checks if the window is an inventory window or not
     * @param windowId The window id
     * @return true if is an inventory window, false otherwise
     */
    public static boolean isInventoryWindow(String windowId){
        return windowId.contains("INVENTORY") || windowId.equals(WindowStrings.WINDOW_MENU_INVENTORY) || windowId.equals(WindowStrings.WINDOW_CHARACTER);
    }
    
    /**
     * Opens an interaction menu
     * @param windowName The name of the window
     * @param windowData The data for the window
     */
    public static void openInteractionMenu(String windowName, String windowData){
        switch(windowName){
            case WindowStrings.CRAFTING: {
                Window craftingWindow = CraftingWindow.createCraftingWindow(windowData);
                Globals.elementService.registerWindow(WindowStrings.CRAFTING, craftingWindow);
                WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.CRAFTING), true);
                Globals.elementService.focusFirstElement();
                Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_MAIN_MENU);
                //play sound effect
                if(Globals.audioEngine != null){
                    Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.UI_TONE_CONFIRM_PRIMARY, VirtualAudioSourceType.UI, false);
                }
                Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(true);
            } break;
            default: {
                throw new Error("Tried to open unsupported window name! " + windowName);
            }
        }
    }

}
