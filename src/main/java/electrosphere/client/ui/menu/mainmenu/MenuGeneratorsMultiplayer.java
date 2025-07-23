package electrosphere.client.ui.menu.mainmenu;

import electrosphere.auth.AuthenticationManager;
import electrosphere.client.ui.components.CharacterCustomizer;
import electrosphere.client.ui.components.InputMacros;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.worldgen.MenuWorldSelect;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.net.NetUtils;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.StringCarousel;
import electrosphere.renderer.ui.elements.TextInput;
import electrosphere.renderer.ui.elementtypes.ClickableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.ValueElement.ValueChangeEventCallback;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.util.Utilities;

public class MenuGeneratorsMultiplayer {

    public static Element createMultiplayerCharacterSelectionWindow(){
        FormElement rVal = new FormElement();

        //button (create)
        Button createButton = new Button();
        Label createLabel = Label.createLabel("Create Character");
        createButton.addChild(createLabel);
        rVal.addChild(createButton);
        createButton.setOnClick(new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldCreationMenu());
            return false;
        }});

        return rVal;
    }

    static String selectedRace = "";
    public static Element createMultiplayerCharacterCreationWindow(){
        FormElement rVal = new FormElement();

        //select race
        StringCarousel raceCarousel = new StringCarousel(100, 125, 1.0f);
        raceCarousel.setOnValueChangeCallback(new ValueChangeEventCallback() {public void execute(ValueChangeEvent event){
            selectedRace = event.getAsString();
        }});
        for(String raceName : Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces()){
            raceCarousel.addOption(raceName);
        }
        rVal.addChild(raceCarousel);

        //button (create)
        Button createButton = new Button();
        Label createLabel = Label.createLabel("Select Race");
        createButton.addChild(createLabel);
        rVal.addChild(createButton);
        createButton.setOnClick(new ClickableElement.ClickEventCallback(){public boolean execute(ClickEvent event){
            WindowUtils.replaceMainMenuContents(CharacterCustomizer.createCharacterCustomizerPanel(selectedRace, (ObjectTemplate template) -> {
                Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructRequestCreateCharacterMessage(Utilities.stringify(template)));
            }));
            return false;
        }});

        return rVal;
    }

    /**
     * The multiplayer mode selection menu
     * @return The element containing the menu
     */
    public static Element createMultiplayerMenu(){
        FormElement rVal = new FormElement();

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
            return false;
        });

        //button (host)
        rVal.addChild(Button.createButton("Host", () -> {
            LoadingThread clientThread = new LoadingThread(LoadingThreadType.CHARACTER_SERVER);
            LoadingThread serverThread = new LoadingThread(LoadingThreadType.MAIN_GAME);
            EngineState.EngineFlags.RUN_CLIENT = true;
            EngineState.EngineFlags.RUN_SERVER = true;
            Globals.engineState.threadManager.start(serverThread);
            Globals.engineState.threadManager.start(clientThread);
        }));

        //button (join)
        rVal.addChild(Button.createButton("Join", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsMultiplayer.createIPMenu());
        }));
        

        //button (back)
        rVal.addChild(Button.createButton("Back", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
        }));

        return rVal;
    }
    
    /**
     * The ip input menu
     * @return The element containing the menu
     */
    public static Element createIPMenu(){
        FormElement rVal = new FormElement();

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsMultiplayer.createMultiplayerMenu());
            return false;
        });
        
        //
        //Address input
        //
        String ipAddress = "";
        if(Globals.gameConfigCurrent.getNetConfig() != null && Globals.gameConfigCurrent.getNetConfig().getAddress() != null){
            ipAddress = Globals.gameConfigCurrent.getNetConfig().getAddress();
        } else {
            ipAddress = NetUtils.getAddress();
        }
        Div addressControl = InputMacros.createTextInputVertical("IP Address", ipAddress);
        TextInput addressInput = (TextInput)addressControl.getChildren().get(1);
        rVal.addChild(addressControl);

        //
        //Port input
        //
        String port = "";
        if(Globals.gameConfigCurrent.getNetConfig() != null && Globals.gameConfigCurrent.getNetConfig().getPort() != null){
            port = Globals.gameConfigCurrent.getNetConfig().getPort();
        } else {
            port = NetUtils.getPort() + "";
        }
        Div portControl = InputMacros.createTextInputVertical("Port", port);
        TextInput portInput = (TextInput)portControl.getChildren().get(1);
        rVal.addChild(portControl);

        //
        //Username input
        //
        String username = "";
        if(Globals.gameConfigCurrent.getNetConfig() != null && Globals.gameConfigCurrent.getNetConfig().getPort() != null){
            username = Globals.gameConfigCurrent.getNetConfig().getUsername();
        } else {
            username =  "";
        }
        Div usernameControl = InputMacros.createTextInputVertical("Username", username);
        TextInput usernameInput = (TextInput)usernameControl.getChildren().get(1);
        rVal.addChild(usernameControl);


        //
        //Password input
        //
        String password = "";
        if(Globals.gameConfigCurrent.getNetConfig() != null && Globals.gameConfigCurrent.getNetConfig().getPort() != null){
            password = Globals.gameConfigCurrent.getNetConfig().getPassword();
        } else {
            password =  "";
        }
        Div passwordControl = InputMacros.createTextInputVertical("Password", password);
        TextInput passwordInput = (TextInput)passwordControl.getChildren().get(1);
        rVal.addChild(passwordControl);

        //button (connect)
        rVal.addChild(Button.createButton("Connect", () -> {
            NetUtils.setAddress(addressInput.getText());
            NetUtils.setPort(Integer.parseInt(portInput.getText()));
            Globals.clientState.clientUsername = usernameInput.getText();
            Globals.clientState.clientPassword = AuthenticationManager.getHashedString(passwordInput.getText());
            LoadingThread clientThread = new LoadingThread(LoadingThreadType.CHARACTER_SERVER);
            EngineState.EngineFlags.RUN_CLIENT = true;
            EngineState.EngineFlags.RUN_SERVER = false;
            Globals.engineState.threadManager.start(clientThread);
        }));

        //button (back)
        rVal.addChild(Button.createButton("Back", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsMultiplayer.createMultiplayerMenu());
        }));

        return rVal;
    }

}
