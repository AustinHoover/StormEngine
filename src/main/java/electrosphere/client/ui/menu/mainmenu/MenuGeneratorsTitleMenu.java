package electrosphere.client.ui.menu.mainmenu;

import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.worldgen.MenuWorldSelect;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.NavigationEvent;

/**
 * Menu generators for the title menu
 */
public class MenuGeneratorsTitleMenu {

    /**
     * spacing between each button
     */
    static final int BUTTON_SPACING = 25;

    /**
     * Creates the main title menu
     * @return The menu element
     */
    public static Element createTitleMenu(){

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            return false;
        });

        Div rVal = Div.createDiv();
        //top-bottom
        rVal.setJustifyContent(YogaJustification.Between);
        //left-right
        rVal.setAlignItems(YogaAlignment.Center);
        rVal.setFlexGrow(1.0f);
        rVal.setFlexDirection(YogaFlexDirection.Column);


        //main option panel
        {
            Div optionPanel = Div.createCol();
            optionPanel.setAlignItems(YogaAlignment.Center);
            optionPanel.setJustifyContent(YogaJustification.Center);
            optionPanel.setFlexGrow(1.0f);


            //label (title)
            Label titleLabel = Label.createLabel("ORPG",3.0f);
            optionPanel.addChild(titleLabel);

            //button (multiplayer)
            {
                Button button = Button.createButtonCentered("Singleplayer", 1.0f, () -> {
                    WindowUtils.replaceMainMenuContents(MenuWorldSelect.createWorldSelectMenu());
                }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
                button.setMarginTop(BUTTON_SPACING);
                optionPanel.addChild(button);
            }

            //button (multiplayer)
            {
                Button button = Button.createButtonCentered("Multiplayer", 1.0f, () -> {
                    WindowUtils.replaceMainMenuContents(MenuGeneratorsMultiplayer.createMultiplayerMenu());
                }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
                button.setMarginTop(BUTTON_SPACING);
                optionPanel.addChild(button);
            }

            //button (static level)
            {
                Button button = Button.createButtonCentered("Level Editor", 1.0f, () -> {
                    WindowUtils.replaceMainMenuContents(MenuGeneratorsLevelEditor.createLevelEditorTopMenu());
                }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
                button.setMarginTop(BUTTON_SPACING);
                optionPanel.addChild(button);
            }

            //button (options)
            {
                Button button = Button.createButtonCentered("Options", 1.0f, () -> {
                    WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleOptions.createOptionsMainMenu());
                }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
                button.setMarginTop(BUTTON_SPACING);
                optionPanel.addChild(button);
            }

            //button (ui testing)
            {
                Button button = Button.createButtonCentered("UI Testing", 1.0f, () -> {
                    WindowUtils.replaceMainMenuContents(MenuGeneratorsUITesting.createUITestMenu());
                }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
                button.setMarginTop(BUTTON_SPACING);
                optionPanel.addChild(button);
            }

            //button (Viewport Test)
            {
                Button button = Button.createButtonCentered("Viewport Test", 1.0f, () -> {
                    Globals.engineState.threadManager.start(new LoadingThread(LoadingThreadType.LOAD_VIEWPORT));
                }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
                button.setMarginTop(BUTTON_SPACING);
                optionPanel.addChild(button);
            }


            rVal.addChild(optionPanel);
        }

        //footer
        {
            Div footer = Div.createRow();
            footer.setJustifyContent(YogaJustification.End);
            footer.setWidthPercent(100.0f);

            ImagePanel engineLogoPanel = ImagePanel.createImagePanel(AssetDataStrings.UI_ENGINE_LOGO_1);
            engineLogoPanel.setMinWidth(50);
            engineLogoPanel.setMinHeight(50);
            footer.addChild(engineLogoPanel);

            rVal.addChild(footer);
        }

        return rVal;
    }
}
