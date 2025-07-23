package electrosphere.client.ui.menu.mainmenu.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import electrosphere.auth.AuthenticationManager;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.MenuGeneratorsTitleMenu;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.scene.RealmDescriptor;
import electrosphere.entity.scene.SceneFile;
import electrosphere.entity.scene.SceneGenerator;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Panel;
import electrosphere.renderer.ui.elements.StringCarousel;
import electrosphere.renderer.ui.elements.TextInput;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.server.saves.SaveUtils;

public class MenuWorldSelect {

    private static WorldGenUIParams params;
    
    /**
     * Creates the world selection menu content
     * @return The menu content
     */
    public static Element createWorldSelectMenu(){
        FormElement rVal = new FormElement();

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
            return false;
        });

        //create save button column
        Div saveButtonContainer = Div.createCol();
        saveButtonContainer.setMarginRight(50);
        List<String> saveNames = SaveUtils.getSaves();
        for(String saveName : saveNames){
            if(!saveName.startsWith(".") && SaveUtils.isProcedural(saveName)){

                Div spacer = Div.createDiv();
                spacer.addChild(Button.createButton(saveName.toUpperCase(), () -> {
                    if(SaveUtils.saveHasWorldFile(saveName.toLowerCase())){
                        //need to log client in
                        Globals.clientState.clientUsername = "username";
                        Globals.clientState.clientPassword = AuthenticationManager.getHashedString("password");
                        LoadingThread serverThread = new LoadingThread(LoadingThreadType.MAIN_GAME, saveName, Globals.clientState.clientUsername, Globals.clientState.clientPassword);
                        EngineState.EngineFlags.RUN_CLIENT = true;
                        EngineState.EngineFlags.RUN_SERVER = true;
                        Globals.engineState.threadManager.start(serverThread);
                    } else {
                        SaveUtils.loadSave(saveName.toLowerCase(), false);
                        WindowUtils.replaceMainMenuContents(MenuWorldSelect.createSaveCreationMenu());
                    }
                }));
                spacer.setMarginBottom(30);

                //button (select save)
                saveButtonContainer.addChild(spacer);
            }
        }

        Div createButtonContainer = Div.createCol();
        createButtonContainer.setMarginLeft(50);
        //button (create)
        createButtonContainer.addChild(Button.createButton("Create World", () -> {
            params = new WorldGenUIParams();
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldCreationMenu());
        }));


        //layout
        Div mainLayout = Div.createRow(
            saveButtonContainer,
            createButtonContainer
        );
        mainLayout.setMarginTop(100);
        mainLayout.setJustifyContent(YogaJustification.Center);
        rVal.addChild(mainLayout);

        return rVal;
    }
    
    /**
     * World creation menu
     * @return The world creation menu element
     */
    public static Element createWorldCreationMenu(){
        FormElement rVal = new FormElement();

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldSelectMenu());
            return false;
        });

        //text entry (world name)
        Div worldNameInputContainer = Div.createRow();
        worldNameInputContainer.setMarginBottom(20);
        Label worldNameLabel = Label.createLabel("Input Name: ");
        worldNameInputContainer.addChild(worldNameLabel);
        TextInput worldNameInput = TextInput.createTextInput();
        worldNameInput.setMinWidth(150);
        worldNameInput.setMaxWidthPercent(50);
        worldNameInput.setText(params.name);
        worldNameInput.setOnValueChangeCallback((value) -> {
            params.name = value.getAsString();
        });
        worldNameInputContainer.addChild(worldNameInput);

        //text entry (world seed)
        Div worldSeedInputContainer = Div.createRow();
        worldSeedInputContainer.setMarginBottom(20);
        Label worldSeedLabel = Label.createLabel("Input Seed: ");
        worldSeedInputContainer.addChild(worldSeedLabel);
        TextInput worldSeedInput = TextInput.createTextInput();
        worldSeedInput.setMinWidth(150);
        worldSeedInput.setMaxWidthPercent(50);
        worldSeedInput.setText(params.seed);
        worldSeedInput.setOnValueChangeCallback((value) -> {
            params.seed = value.getAsString();
        });
        worldSeedInputContainer.addChild(worldSeedInput);

        Div worldTypeMenuButtonContainer = Div.createRow();
        {
            Button worldTypeButton = Button.createButton("World Type", () -> {
                WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldTypeDefinitionMenu());
            });
            worldTypeMenuButtonContainer.addChild(worldTypeButton);
        }

        
        //button (create)
        Div createButtonContainer = Div.createCol();
        createButtonContainer.setMarginTop(20);
        createButtonContainer.addChild(Button.createButton("Create", () -> {
            String saveName = params.name;
            String seed = params.seed;
            //create scene file
            SceneFile sceneFile = SceneGenerator.createProceduralSceneFile(saveName, seed);
            switch(params.worldType){
                case 0: {
                    sceneFile.getRealmDescriptor().setWorldType(RealmDescriptor.PROCEDURAL_TYPE_DEFAULT);
                } break;
                case 1: {
                    sceneFile.getRealmDescriptor().setWorldType(RealmDescriptor.PROCEDURAL_TYPE_HOMOGENOUS);

                    //set biome as well
                    List<String> biomes = Globals.gameConfigCurrent.getBiomeMap().getSurfaceBiomes().stream().map((biome) -> {
                        return biome.getId();
                    }).collect(Collectors.toList());
                    sceneFile.getRealmDescriptor().setBiomeType(biomes.get(params.biomeType));
                } break;
                default: {
                    throw new Error("Unhandled world type!");
                }
            }
            //create save dir
            SaveUtils.createOrOverwriteSave(saveName, sceneFile);
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldSelectMenu());
        }));


        //layout content
        Div mainLayout = Div.createCol(
            worldNameInputContainer,
            worldSeedInputContainer,
            worldTypeMenuButtonContainer,
            createButtonContainer
        );
        mainLayout.setMarginTop(300);
        mainLayout.setAlignItems(YogaAlignment.Center);
        rVal.addChild(mainLayout);

        return rVal;
    }

    /**
     * World type definition menu
     * @return The world type definition menu element
     */
    public static Element createWorldTypeDefinitionMenu(){
        FormElement rVal = new FormElement();

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldCreationMenu());
            return false;
        });

        //select world type
        Div worldTypeCarousel = Div.createRow();
        {
            Panel panel = Panel.createPanel();
            panel.setWidth(500);
            panel.setJustifyContent(YogaJustification.Between);
            worldTypeCarousel.addChild(panel);

            Label label = Label.createLabel("Type: ");
            panel.addChild(label);
            List<String> options = Arrays.asList(new String[]{
                "Default",
                "Homogenous",
            });
            
            StringCarousel typeCarousel = StringCarousel.create(options, (event) -> {
                int index = options.indexOf(event.getAsString());
                params.worldType = index;
                WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldTypeDefinitionMenu());
            });
            typeCarousel.setOption(params.worldType);
            panel.addChild(typeCarousel);
        }

        //select biome type (for homogenous worlds)
        Div biomeTypeCarousel = Div.createRow();
        if(params.worldType == 1){
            Panel panel = Panel.createPanel();
            panel.setWidth(500);
            panel.setJustifyContent(YogaJustification.Between);
            biomeTypeCarousel.addChild(panel);

            Label label = Label.createLabel("Biome: ");
            panel.addChild(label);


            List<String> biomes = Globals.gameConfigCurrent.getBiomeMap().getSurfaceBiomes().stream().map((biome) -> {
                return biome.getDisplayName();
            }).collect(Collectors.toList());

            StringCarousel typeCarousel = StringCarousel.create(biomes, (event) -> {
                int index = biomes.indexOf(event.getAsString());
                params.biomeType = index;
                Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,rVal);
            });
            typeCarousel.setOption(params.biomeType);
            panel.addChild(typeCarousel);
        }

        
        //button (create)
        Div createButtonContainer = Div.createCol();
        createButtonContainer.setMarginTop(20);
        createButtonContainer.addChild(Button.createButton("Return", () -> {
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldCreationMenu());
        }));


        //layout content
        Div mainLayout = Div.createCol(
            worldTypeCarousel,
            biomeTypeCarousel,
            createButtonContainer
        );
        mainLayout.setMarginTop(300);
        mainLayout.setAlignItems(YogaAlignment.Center);
        rVal.addChild(mainLayout);

        return rVal;
    }
    
    /**
     * Save creation menu
     * @return The save creation menu element
     */
    public static Element createSaveCreationMenu(){
        FormElement rVal = new FormElement();

        //button (save)
        rVal.addChild(Button.createButton("Save", () -> {
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldSelectMenu());
            throw new UnsupportedOperationException("Need to update to use new save flow");
        }));

        //button (cancel)
        rVal.addChild(Button.createButton("Cancel", () -> {
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldSelectMenu());
        }));

        return rVal;
    }

    static class WorldGenUIParams {
        int worldType = 0;
        int biomeType = 0;
        String name = "World name";
        String seed = System.currentTimeMillis() + "";
    }

}
