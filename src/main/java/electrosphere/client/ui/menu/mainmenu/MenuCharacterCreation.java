package electrosphere.client.ui.menu.mainmenu;

import electrosphere.client.entity.character.CharacterDescriptionDTO;
import electrosphere.client.ui.components.CharacterCustomizer;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.StringCarousel;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.util.Utilities;

/**
 * The menu for character creation
 */
public class MenuCharacterCreation {

    /**
     * The currently selected race
     */
    static String selectedRace = "";

    /**
     * Creates the menu for selecting/creating a character
     * @return The menu element
     */
    public static Element createCharacterSelectionWindow(){
        FormElement rVal = new FormElement();

        //the list of characters
        Div selectContainer = Div.createCol();
        if(Globals.clientState.clientCharacterManager.getCharacterList() != null){
            for(CharacterDescriptionDTO description : Globals.clientState.clientCharacterManager.getCharacterList().getCharacters()){
                String buttonTitle = "Character " + description.getId();
                Div charNameContainer = Div.createRow(Button.createButton(buttonTitle, () -> {
                    Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructRequestSpawnCharacterMessage(description.getId()));
                    Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestMetadataMessage());
                    Globals.engineState.threadManager.start(new LoadingThread(LoadingThreadType.CLIENT_WORLD));
                }));
                selectContainer.addChild(charNameContainer);
            }
        }
        
        //button (create)
        Div createContainer = Div.createDiv();
        createContainer.addChild(Button.createButton("Create Character", () -> {
            WindowUtils.replaceMainMenuContents(MenuCharacterCreation.createRaceSelectionMenu());
        }));


        //main layout
        Div mainLayout = Div.createCol(
            selectContainer,
            createContainer
        );
        mainLayout.setAlignItems(YogaAlignment.Center);
        rVal.addChild(mainLayout);

        return rVal;
    }

    /**
     * Creates the menu for selecting a character's race
     * @return The menu element
     */
    public static Element createRaceSelectionMenu(){
        FormElement rVal = new FormElement();

        //select race
        rVal.addChild(StringCarousel.create(Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces(), (ValueChangeEvent event) -> {
            selectedRace = event.getAsString();
        }));

        selectedRace = Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces().get(0);

        //button (create)
        rVal.addChild(Button.createButton("Confirm", () -> {
            WindowUtils.replaceMainMenuContents(MenuCharacterCreation.createCharacterCustomizationMenu());
        }));

        return rVal;
    }

    /**
     * Creates the menu for customizing the character's appearance
     * @return The menu element
     */
    public static Element createCharacterCustomizationMenu(){
        FormElement rVal = new FormElement();

        rVal.addChild(CharacterCustomizer.createCharacterCustomizerPanel(selectedRace, (ObjectTemplate template) -> {
            Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructRequestCreateCharacterMessage(Utilities.stringify(template)));
        }));

        return rVal;
    }

}
