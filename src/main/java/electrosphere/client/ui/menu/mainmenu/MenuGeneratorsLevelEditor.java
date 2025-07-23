package electrosphere.client.ui.menu.mainmenu;

import java.util.List;
import java.util.stream.Collectors;

import electrosphere.client.ui.components.InputMacros;
import electrosphere.client.ui.components.VoxelSelectionPanel;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.scene.SceneFile;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Panel;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import electrosphere.server.saves.SaveUtils;

/**
 * Menu generators for the main menu interactions with the level editor
 */
public class MenuGeneratorsLevelEditor {

    /**
     * The default grid size when creating a level
     */
    static final int DEFAULT_GRID_SIZE = 2;

    /**
     * The maximum selectable size for the grid
     */
    static final int MAX_SELECTABLE_SIZE = 128;

    /**
     * Margin between each panel
     */
    static final int PANEL_MARGIN = 15;

    /**
     * Padding of each panel
     */
    static final int PANEL_PADDING = 50;

    /**
     * Width of a panel
     */
    static final int PANEL_WIDTH = 500;

    /**
     * Height of a panel
     */
    static final int PANEL_HEIGHT = 150;

    /**
     * Creates the top level menu for the level editor
     * @return The actual element containing the menu
     */
    public static Element createLevelEditorTopMenu(){
        FormElement rVal = new FormElement();
        //top-bottom
        rVal.setJustifyContent(YogaJustification.Start);
        //left-right
        rVal.setAlignItems(YogaAlignment.Center);
        rVal.setAlignContent(YogaAlignment.Center);

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
            return false;
        });
        

        //
        //title
        //
        Div titleRow = Div.createRow(
            Label.createLabel("Select Level"),
            Button.createButton("Back", () -> {
                WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
            })
        );
        titleRow.setMarginTop(30);
        titleRow.setMarginBottom(30);
        rVal.addChild(titleRow);


        //
        //button (create level)
        //
        Button createLevelButton = Button.createButton("Create Level", () -> {
            //go to create level flow
            WindowUtils.replaceMainMenuContents(MenuGeneratorsLevelEditor.createLevelEditorCreationMenu());
        });
        createLevelButton.setMarginBottom(30);
        Div createWrapper = Div.createDiv();
        createWrapper.addChild(createLevelButton);
        rVal.addChild(createWrapper);


        //
        //the buttons to load existing levels
        //
        Div existingLevelColumn = Div.createCol();
        existingLevelColumn.setAlignContent(YogaAlignment.Start);
        rVal.addChild(existingLevelColumn);

        List<String> saveNames = SaveUtils.getSaves().stream().filter((String saveName) -> {
            return !SaveUtils.isProcedural(saveName) && !SaveUtils.isTestScene(saveName);
        }).collect(Collectors.toList());
        for(String saveName : saveNames){

            //delete level button
            Button deleteButton = Button.createButton(" X ", () -> {
                SaveUtils.deleteSave(saveName);
                WindowUtils.replaceMainMenuContents(MenuGeneratorsLevelEditor.createLevelEditorTopMenu());
            }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
            deleteButton.setMarginRight(10);


            //button (launch level editor)
            Button launchButton = Button.createButton(saveName, () -> {
                //launch level
                LoadingThread loadingThread = new LoadingThread(LoadingThreadType.LEVEL, saveName);
                EngineState.EngineFlags.RUN_CLIENT = true;
                EngineState.EngineFlags.RUN_SERVER = true;
                Globals.engineState.threadManager.start(loadingThread);
            }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);

            //
            //button (edit Level)
            //
            Button editButton = Button.createButton("Edit", () -> {
                //launch level editor
                LoadingThread loadingThread = new LoadingThread(LoadingThreadType.LEVEL_EDITOR, saveName);
                Globals.engineState.threadManager.start(loadingThread);
            }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);

            //create row
            Div row = Div.createRow(
                deleteButton,
                launchButton,
                editButton
            );
            row.setJustifyContent(YogaJustification.Between);
            row.setFlexGrow(1.0f);
            row.setMaxHeight(30);

            //create panel to hold the row
            Panel panel = Panel.createPanel();
            panel.setWidth(PANEL_WIDTH);
            panel.setHeight(PANEL_HEIGHT);
            panel.setMarginBottom(PANEL_MARGIN);
            panel.setMarginLeft(PANEL_MARGIN);
            panel.setMarginRight(PANEL_MARGIN);
            panel.setMarginTop(PANEL_MARGIN);
            panel.setPaddingBottom(PANEL_PADDING);
            panel.setPaddingLeft(PANEL_PADDING);
            panel.setPaddingRight(PANEL_PADDING);
            panel.setPaddingTop(PANEL_PADDING);
            panel.addChild(row);

            existingLevelColumn.addChild(panel);
        }

        return rVal;
    }


    /**
     * Creates the level creation menu
     * @return The element containing the level creation menu
     */
    public static Element createLevelEditorCreationMenu(){

        //set nav callback
        WindowUtils.setMainMenuBackoutCallback((NavigationEvent event) -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsLevelEditor.createLevelEditorTopMenu());
            return false;
        });


        //values to creat the level with
        LevelDescription inFlightLevel = new LevelDescription();
        SceneFile sceneFile = SceneFile.createSceneFile();
        sceneFile.setCreateSaveInstance(true);
        inFlightLevel.setSceneFile(sceneFile);


        //
        //Top level element
        //
        FormElement rVal = new FormElement();
        //top-bottom
        rVal.setJustifyContent(YogaJustification.Start);
        //left-right
        rVal.setAlignItems(YogaAlignment.Center);
        rVal.setAlignContent(YogaAlignment.Start);

        //
        //Title
        //
        Div titleRow = Div.createRow(
            Label.createLabel("Create Level"),
            Button.createButton("Back", () -> {
                WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
            })
        );
        titleRow.setMarginTop(30);
        titleRow.setMarginBottom(30);
        rVal.addChild(titleRow);




        //
        //Level name input
        //
        //generate default level name
        List<String> saveNames = SaveUtils.getSaves();
        int i = 0;
        String defaultSaveName = "defaultLevel_" + i;
        while(saveNames.contains(defaultSaveName)){
            i++;
            defaultSaveName = "defaultLevel_" + i;
        }
        inFlightLevel.setName(defaultSaveName);
        //input for the name of the save
        Div levelNameInput = InputMacros.createTextInput("Level Name:", defaultSaveName, (ValueChangeEvent event) -> {
            inFlightLevel.setName(event.getAsString());
        });
        levelNameInput.setMaxWidth(400);
        levelNameInput.setMarginBottom(30);
        rVal.addChild(levelNameInput);
        

        //
        //Gridded realm controls
        //
        Div griddedRealmControls = Div.createCol();
        {
            Label gridSizeLabel = Label.createLabel("" + DEFAULT_GRID_SIZE);

            //add actual slider
            griddedRealmControls.addChild(
                InputMacros.createSliderInput("Realm Size", (ValueChangeEvent event) -> {
                    float value = event.getAsFloat() * MenuGeneratorsLevelEditor.MAX_SELECTABLE_SIZE;
                    sceneFile.getRealmDescriptor().setGriddedRealmSize((int)value);
                    Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,() -> {
                        gridSizeLabel.setText("" + (int)value);
                        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY, Globals.elementService.getWindow(WindowStrings.WINDOW_MENU_MAIN));
                    });
                }, DEFAULT_GRID_SIZE / (float)GriddedDataCellManager.MAX_GRID_SIZE)
            );

            //add size label
            griddedRealmControls.addChild(gridSizeLabel);

            //add voxel type selection
            sceneFile.getRealmDescriptor().setGriddedRealmSize(DEFAULT_GRID_SIZE);
            griddedRealmControls.addChild(VoxelSelectionPanel.createVoxelTypeSelectionPanel((VoxelType type) -> {
                sceneFile.getRealmDescriptor().setBaseVoxel(type.getId());
            }));
        }
        rVal.addChild(griddedRealmControls);



        //
        //Create level button
        //
        Button createButton = Button.createButton("Create Level", () -> {
            //launch level editor
            LoadingThread loadingThread = new LoadingThread(LoadingThreadType.LEVEL_EDITOR, inFlightLevel);
            Globals.engineState.threadManager.start(loadingThread);
        }).setOnClickAudio(AssetDataStrings.UI_TONE_BUTTON_TITLE);
        createButton.setAlignSelf(YogaAlignment.Center);
        rVal.addChild(createButton);


        return rVal;
    }

    /**
     * A level that is currently having its parameters defined
     */
    public static class LevelDescription {

        /**
         * The name of the new level
         */
        String name;

        /**
         * The scene file
         */
        SceneFile sceneFile;

        /**
         * Sets the name of the level
         * @param name The name
         */
        public void setName(String name){
            this.name = name;
        }

        /**
         * Sets the scene file
         * @param sceneFile The scene file
         */
        public void setSceneFile(SceneFile sceneFile){
            this.sceneFile = sceneFile;
        }

        /**
         * Gets the name of the level
         * @return The level name
         */
        public String getName(){
            return name;
        }

        /**
         * Gets the scene file
         * @return The scene file
         */
        public SceneFile getSceneFile(){
            return sceneFile;
        }

    }
    
}
