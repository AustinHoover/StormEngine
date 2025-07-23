package electrosphere.client.ui.menu.ingame;

import org.joml.Vector4f;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.controls.ControlHandler.ControlsState;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.visualattribute.VisualAttribute;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.ActorStaticMorph;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.ScrollableContainer;
import electrosphere.renderer.ui.elements.Slider;
import electrosphere.renderer.ui.elements.TextInput;
import electrosphere.renderer.ui.elements.VirtualScrollable;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ClickableElement;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;
import electrosphere.renderer.ui.elementtypes.ValueElement.ValueChangeEventCallback;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.server.saves.SaveUtils;

/**
 * Menu generators for in game menus
 */
public class MenuGeneratorsInGame {

    /**
     * Margin for buttons
     */
    static final int BUTTON_MARGIN = 30;

    /**
     * Width of window
     */
    static final int WINDOW_WIDTH = 500;
    
    /**
     * Height of window
     */
    static final int WINDOW_HEIGHT = 500;
    
    /**
     * Creates the main in game menu that shows up when you (typically) hit the escape key
     * @return The window for the menu
     */
    public static Window createInGameMainMenu(){
        Window rVal = Window.createExpandableCenterAligned(Globals.renderingEngine.getOpenGLState());

        Div div = Div.createDiv();
        rVal.addChild(div);
        div.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN), false);
            Globals.elementService.unregisterWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN);
            if(Globals.cameraHandler.getTrackPlayerEntity()){
                Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
            } else {
                Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_FREE_CAMERA);
            }
            Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(false);
            return false;
        }});
        
        //Back
        {
            Button button = Button.createButton("Back", () -> {
                WindowUtils.recursiveSetVisible(Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN), false);
                Globals.elementService.unregisterWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN);
                if(Globals.cameraHandler.getTrackPlayerEntity()){
                    Globals.controlHandler.hintUpdateControlState(ControlsState.MAIN_GAME);
                } else {
                    Globals.controlHandler.hintUpdateControlState(ControlsState.IN_GAME_FREE_CAMERA);
                }
                Globals.renderingEngine.getPostProcessingPipeline().setApplyBlur(false);
            });
            button.setMarginTop(BUTTON_MARGIN);
            button.setMarginLeft(BUTTON_MARGIN);
            div.addChild(button);
        }

        //Return to main menu
        {
            Button button = Button.createButton("Return To Main Menu", () -> {
                Globals.engineState.signalSystem.post(SignalType.ENGINE_RETURN_TO_TITLE);
            });
            button.setMarginTop(BUTTON_MARGIN);
            button.setMarginLeft(BUTTON_MARGIN);
            div.addChild(button);
        }
        
        
        //Save
        {
            Button button = Button.createButton("Save and Quit", () -> {
                SaveUtils.overwriteSave(Globals.serverState.currentSave.getName());
                Globals.engineState.signalSystem.post(SignalType.ENGINE_RETURN_TO_TITLE);
            });
            button.setMarginTop(BUTTON_MARGIN);
            button.setMarginLeft(BUTTON_MARGIN);
            div.addChild(button);
        }

        //Quit
        {
            Button button = Button.createButton("Shutdown", () -> {
                Main.running = false;
            });
            button.setMarginTop(BUTTON_MARGIN);
            button.setMarginLeft(BUTTON_MARGIN);
            div.addChild(button);
        }

        //checking macro data is a poor man's check for whether we're arena or full gamemode
        {
            Button button = Button.createButton("Debug", () -> {
                WindowUtils.replaceWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, createInGameDebugMainMenu());
            });
            button.setMarginTop(BUTTON_MARGIN);
            button.setMarginLeft(BUTTON_MARGIN);
            div.addChild(button);
        }

        if(MenuGeneratorsInGame.shouldShowLevelEditor()){
            Button button = Button.createButton("Open Level Editor Tools", () -> {
                WindowUtils.replaceWindow(WindowStrings.LEVEL_EDTIOR_SIDE_PANEL,MenuGeneratorsLevelEditor.createLevelEditorSidePanel());
            });
            button.setMarginTop(BUTTON_MARGIN);
            button.setMarginLeft(BUTTON_MARGIN);
            div.addChild(button);
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,rVal);

        return rVal;
    }

    /**
     * Creates the debug menu
     * @return
     */
    public static Window createInGameDebugMainMenu(){
        Window rVal = Window.createExpandableCenterAligned(Globals.renderingEngine.getOpenGLState());

        VirtualScrollable scrollable = new VirtualScrollable(WINDOW_WIDTH, WINDOW_HEIGHT);
        rVal.addChild(scrollable);
        rVal.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.replaceWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, createInGameMainMenu());
            return false;
        }});
        
        //label 1 (back)
        Button backButton = Button.createButton("Back", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            WindowUtils.replaceWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, createInGameMainMenu());
            return false;
        }});
        backButton.setMarginTop(BUTTON_MARGIN);
        backButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(backButton);
        

        //text entry (port)
        TextInput modelDebugInput = TextInput.createTextInput();
        scrollable.addChild(modelDebugInput);
        modelDebugInput.setText("Model path goes here");
        
        //label 3 (load model and debug)
        Button debugModelButton = Button.createButton("Print Model Debug Info", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Model targetModel = null;
            if((targetModel = Globals.assetManager.fetchModel(modelDebugInput.getText())) != null){
                targetModel.describeHighLevel();
            } else {
                Globals.assetManager.addModelPathToQueue(modelDebugInput.getText());
                Globals.assetManager.loadAssetsInQueue();
                if((targetModel = Globals.assetManager.fetchModel(modelDebugInput.getText())) != null){
                    targetModel.describeHighLevel();
                }
            }
            return false;
        }});
        debugModelButton.setMarginTop(BUTTON_MARGIN);
        debugModelButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(debugModelButton);

        //label 4 (reload all shaders)
        Button reloadShaderButton = Button.createButton("Reload all shaders", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.assetManager.forceReloadAllShaders();
            return false;
        }});
        reloadShaderButton.setMarginTop(BUTTON_MARGIN);
        reloadShaderButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(reloadShaderButton);

        //reload all models
        Button reloadModelButton = Button.createButton("Reload all models", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.assetManager.forceReloadAllModels();
            return false;
        }});
        reloadModelButton.setMarginTop(BUTTON_MARGIN);
        reloadModelButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(reloadModelButton);

        //disable drawing player character
        Button toggleDrawPlayerButton = Button.createButton("Toggle draw character", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            if(Globals.clientState.playerEntity != null){
                if(Globals.clientState.playerEntity.containsKey(EntityDataStrings.DATA_STRING_DRAW)){
                    boolean draw = (boolean)Globals.clientState.playerEntity.getData(EntityDataStrings.DATA_STRING_DRAW);
                    Globals.clientState.playerEntity.putData(EntityDataStrings.DATA_STRING_DRAW, !draw);
                }
                // if(Globals.clientState.playerEntity.containsKey(EntityDataStrings.DRAW_CAST_SHADOW)){
                //     boolean drawShadow = (boolean)Globals.clientState.playerEntity.getData(EntityDataStrings.DRAW_CAST_SHADOW);
                //     Globals.clientState.playerEntity.putData(EntityDataStrings.DRAW_CAST_SHADOW, !drawShadow);
                // }
            }
            return false;
        }});
        toggleDrawPlayerButton.setMarginTop(BUTTON_MARGIN);
        toggleDrawPlayerButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(toggleDrawPlayerButton);

        //pull up character editor
        Button characterSliderMenuButton = Button.createButton("Character slider menu", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            WindowUtils.replaceWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, createInGameCharacterSliderMenu());
            return false;
        }});
        characterSliderMenuButton.setMarginTop(BUTTON_MARGIN);
        characterSliderMenuButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(characterSliderMenuButton);

        //label (switch framebuffer)
        Button switchFramebufferButton = Button.createButton("Switch Active Framebuffer", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            RenderingEngine.incrementOutputFramebuffer();
            return false;
        }});
        switchFramebufferButton.setMarginTop(BUTTON_MARGIN);
        switchFramebufferButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(switchFramebufferButton);

        //label (toggle draw client collision spheres)
        Button toggleClientCollisionSpheresButton = Button.createButton("Toggle draw client collision spheres", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawCollisionSpheresClient(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawCollisionSpheresClient());
            return false;
        }});
        toggleClientCollisionSpheresButton.setMarginTop(BUTTON_MARGIN);
        toggleClientCollisionSpheresButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(toggleClientCollisionSpheresButton);

        //label (toggle draw server collision spheres)
        Button toggleServerCollisionSpheresButton = Button.createButton("Toggle draw server collision spheres", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawCollisionSpheresServer(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawCollisionSpheresServer());
            return false;
        }});
        toggleServerCollisionSpheresButton.setMarginTop(BUTTON_MARGIN);
        toggleServerCollisionSpheresButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(toggleServerCollisionSpheresButton);

        //label (toggle draw physics objects)
        Button togglePhysicsObjectsClientButton = Button.createButton("Toggle draw physics objects (client)", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawPhysicsObjectsClient(!Globals.gameConfigCurrent.getSettings().graphicsDebugDrawPhysicsObjectsClient());
            return false;
        }});
        togglePhysicsObjectsClientButton.setMarginTop(BUTTON_MARGIN);
        togglePhysicsObjectsClientButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(togglePhysicsObjectsClientButton);

        //label (toggle draw physics objects)
        Button togglePhysicsObjectsServerButton = Button.createButton("Toggle draw physics objects (server)", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawPhysicsObjectsServer(!Globals.gameConfigCurrent.getSettings().graphicsDebugDrawPhysicsObjectsServer());
            return false;
        }});
        togglePhysicsObjectsServerButton.setMarginTop(BUTTON_MARGIN);
        togglePhysicsObjectsServerButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(togglePhysicsObjectsServerButton);

        //toggle draw grid alignment data
        Button toggleDrawGridAlignmentDataButton = Button.createButton("Toggle draw grid alignment data", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawGridAlignment(!Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawGridAlignment());
            return false;
        }});
        toggleDrawGridAlignmentDataButton.setMarginTop(BUTTON_MARGIN);
        toggleDrawGridAlignmentDataButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(toggleDrawGridAlignmentDataButton);

        //label (toggle draw movement vectors)
        Button toggleMovementVectorsButton = Button.createButton("Toggle draw movement vectors", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawMovementVectors(!Globals.gameConfigCurrent.getSettings().graphicsDebugDrawMovementVectors());
            return false;
        }});
        toggleMovementVectorsButton.setMarginTop(BUTTON_MARGIN);
        toggleMovementVectorsButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(toggleMovementVectorsButton);

        //label (toggle draw navmesh)
        Button toggleNavmeshButton = Button.createButton("Toggle draw navmesh", new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            // Main.running = false;
            Globals.gameConfigCurrent.getSettings().setGraphicsDebugDrawNavmesh(!Globals.gameConfigCurrent.getSettings().graphicsDebugDrawNavmesh());
            return false;
        }});
        toggleNavmeshButton.setMarginTop(BUTTON_MARGIN);
        toggleNavmeshButton.setMarginLeft(BUTTON_MARGIN);
        scrollable.addChild(toggleNavmeshButton);

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,rVal);

        return rVal;
    }

    /**
     * Creates the in-game character appearance slider menu
     * @return
     */
    public static Window createInGameCharacterSliderMenu(){
        int width = 500;
        int height = 500;
        Window rVal = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, width, height, true);
        ScrollableContainer scrollable = ScrollableContainer.createScrollable();
        rVal.addChild(scrollable);
        rVal.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.replaceWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, createInGameDebugMainMenu());
            return false;
        }});
        
        //black texture background
        ImagePanel imagePanel = ImagePanel.createImagePanelAbsolute(0,0,width,height + 1000,AssetDataStrings.TEXTURE_BLACK);
        scrollable.addChild(imagePanel);
        
        //label 1 (back)
        Button backButton = new Button();
        Label backLabel = Label.createLabel("Back");
        backButton.addChild(backLabel);
        scrollable.addChild(backButton);
        backButton.setOnClick(new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            WindowUtils.replaceWindow(WindowStrings.WINDOW_MENU_INGAME_MAIN, createInGameDebugMainMenu());
            return false;
        }});

        Entity playerEntity = Globals.clientState.playerEntity;
        Actor playerActor = EntityUtils.getActor(playerEntity);
        ActorStaticMorph staticMorph = playerActor.getAnimationData().getStaticMorph();
        CreatureData playeCreatureType = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(CreatureUtils.getType(playerEntity));
        int offset = 0;
        for(VisualAttribute attribute : playeCreatureType.getVisualAttributes()){
            int posY = offset * 350 + 100;
            if(attribute.getType().equals("bone")){
                Slider attributeSlider = new Slider(50,posY,400,100,new Vector4f(0.1f,0.1f,0.1f,1.0f),new Vector4f(1.0f,0,0,1.0f));
                attributeSlider.setOnValueChangeCallback(new ValueChangeEventCallback() {public void execute(ValueChangeEvent event) {
                    // float value = event.getAsFloat();
                    // float minVal = attribute.getMinValue();
                    // float range = attribute.getMaxValue() - minVal;
                    // float actualValue = minVal + range * value;
                    staticMorph.updateValue(attribute.getSubtype(), attribute.getPrimaryBone(), event.getAsFloat());
                    if(attribute.getMirrorBone() != null){
                        staticMorph.updateValue(attribute.getSubtype(), attribute.getMirrorBone(), event.getAsFloat());
                    }
                }});
                scrollable.addChild(attributeSlider);
            }
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,rVal);

        return rVal;
    }

    /**
     * Checks if should show the level editor button
     * @return True if should show button, false otherwise
     */
    private static boolean shouldShowLevelEditor(){
        return Globals.serverState.server != null;
    }

}
