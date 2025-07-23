package electrosphere.client.ui.menu.ingame;

import org.joml.Vector3d;
import org.joml.Vector3f;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.collision.CollisionEngine;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.item.Item;
import electrosphere.data.macro.units.UnitDefinition;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.Entity;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.light.DirectionalLight;
import electrosphere.renderer.light.LightManager;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Slider;
import electrosphere.renderer.ui.elements.VirtualScrollable;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.NavigableElement.NavigationEventCallback;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.entity.unit.UnitUtils;

/**
 * Menu generators for level editor
 */
public class MenuGeneratorsLevelEditor {

    //
    //side panel
    static Window mainSidePanel;
    //width of the side panel
    static final int SIDE_PANEL_WIDTH = 500;

    //is the voxel selection window open
    protected static boolean voxelWindowOpen = false;

    //vertical offset from cursor position to spawn things at
    static final Vector3d cursorVerticalOffset = new Vector3d(0,0.05,0);


    /**
     * Creates the level editor side panel top view
     * @return
     */
    public static Window createLevelEditorSidePanel(){
        //setup window
        mainSidePanel = Window.create(Globals.renderingEngine.getOpenGLState(), 0, 0, SIDE_PANEL_WIDTH, Globals.WINDOW_HEIGHT, true);
        mainSidePanel.setParentAlignContent(YogaAlignment.End);
        mainSidePanel.setParentJustifyContent(YogaJustification.End);
        mainSidePanel.setParentAlignItem(YogaAlignment.End);
        mainSidePanel.setAlignContent(YogaAlignment.Start);
        mainSidePanel.setAlignItems(YogaAlignment.Start);
        mainSidePanel.setJustifyContent(YogaJustification.Start);

        //scrollable
        VirtualScrollable scrollable = new VirtualScrollable(SIDE_PANEL_WIDTH, Globals.WINDOW_HEIGHT);
        mainSidePanel.addChild(scrollable);
        mainSidePanel.setOnNavigationCallback(new NavigationEventCallback() {public boolean execute(NavigationEvent event){
            WindowUtils.closeWindow(WindowStrings.LEVEL_EDTIOR_SIDE_PANEL);
            return false;
        }});

        fillInDefaultContent(scrollable);
        

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);

        return mainSidePanel;
    }

    /**
     * Fills in the default content for the scrollable
     * @param scrollable
     */
    private static void fillInDefaultContent(VirtualScrollable scrollable){

        scrollable.clearChildren();

        //close button
        scrollable.addChild(Button.createButton("Close", () -> {
            WindowUtils.closeWindow(WindowStrings.LEVEL_EDTIOR_SIDE_PANEL);
        }));

        //spawn creature button
        scrollable.addChild(Button.createButton("Spawn Creature", () -> {
            fillInSpawnCreatureContent(scrollable);
        }));

        //spawn unit button
        scrollable.addChild(Button.createButton("Spawn Unit", () -> {
            fillInSpawnUnitContent(scrollable);
        }));

        //spawn foliage button
        scrollable.addChild(Button.createButton("Spawn Foliage", () -> {
            fillInSpawnFoliageContent(scrollable);
        }));

        //spawn foliage button
        scrollable.addChild(Button.createButton("Spawn Item", () -> {
            fillInSpawnItemContent(scrollable);
        }));

        //spawn object button
        scrollable.addChild(Button.createButton("Spawn Object", () -> {
            fillInSpawnObjectContent(scrollable);
        }));

        //select voxel button
        scrollable.addChild(Button.createButton("Select Voxel Type", () -> {
            if(voxelWindowOpen){
                voxelWindowOpen = false;
                WindowUtils.closeWindow(WindowStrings.VOXEL_TYPE_SELECTION);
            } else {
                voxelWindowOpen = true;
                WindowUtils.replaceWindow(WindowStrings.VOXEL_TYPE_SELECTION,MenuGeneratorsTerrainEditing.createVoxelTypeSelectionPanel());
            }
        }));

        //entity tree view
        scrollable.addChild(Button.createButton("View Entity Tree", () -> {
            fillInEntityTreeContent(scrollable);
        }));


        //entity tree view
        scrollable.addChild(Button.createButton("Atmospheric Control", () -> {
            fillInAtmosphericControlContent(scrollable);
        }));

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);

    }

    /**
     * Fills in the content for spawning entities
     * @param scrollable
     */
    private static void fillInSpawnCreatureContent(VirtualScrollable scrollable){

        scrollable.clearChildren();

        //back button
        scrollable.addChild(Button.createButton("Back", () -> {
            fillInDefaultContent(scrollable);
        }));

        //button for spawning all creatures
        for(CreatureData data : Globals.gameConfigCurrent.getCreatureTypeLoader().getTypes()){
            //spawn creature button
            scrollable.addChild(Button.createButton("Spawn " + data.getId(), () -> {
                LoggerInterface.loggerEngine.INFO("spawn " + data.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE).add(cursorVerticalOffset);
                CreatureUtils.serverSpawnBasicCreature(realm, cursorPos, data.getId(), null);
            }));
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
    }

    /**
     * Fills in the content for spawning units
     * @param scrollable
     */
    private static void fillInSpawnUnitContent(VirtualScrollable scrollable){

        scrollable.clearChildren();

        //back button
        scrollable.addChild(Button.createButton("Back", () -> {
            fillInDefaultContent(scrollable);
        }));

        //button for spawning all creatures
        for(UnitDefinition unitDefinition : Globals.gameConfigCurrent.getUnitLoader().getUnits()){
            //spawn creature button
            scrollable.addChild(Button.createButton("Spawn " + unitDefinition.getId(), () -> {
                LoggerInterface.loggerEngine.INFO("spawn " + unitDefinition.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
                if(cursorPos == null){
                    cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
                }
                cursorPos = cursorPos.add(cursorVerticalOffset);
                UnitUtils.spawnUnit(realm, cursorPos, unitDefinition.getId());
            }));
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
    }

    /**
     * Level editor content for spawning foliage
     * @param scrollable
     */
    private static void fillInSpawnFoliageContent(VirtualScrollable scrollable){
        scrollable.clearChildren();

        //back button
        scrollable.addChild(Button.createButton("Back", () -> {
            fillInDefaultContent(scrollable);
        }));

        //button for spawning all foliage types
        for(FoliageType data : Globals.gameConfigCurrent.getFoliageMap().getTypes()){
            //spawn foliage button
            scrollable.addChild(Button.createButton("Spawn " + data.getId(), () -> {
                LoggerInterface.loggerEngine.INFO("spawn " + data.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE).add(cursorVerticalOffset);
                FoliageUtils.serverSpawnTreeFoliage(realm, cursorPos, data.getId());
            }));
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
    }

    /**
     * Level editor menu content for spawning items
     * @param scrollable
     */
    private static void fillInSpawnItemContent(VirtualScrollable scrollable){
        scrollable.clearChildren();

        //back button
        scrollable.addChild(Button.createButton("Back", () -> {
            fillInDefaultContent(scrollable);
        }));

        //button for spawning all foliage types
        for(Item item : Globals.gameConfigCurrent.getItemMap().getTypes()){
            //spawn foliage button
            scrollable.addChild(Button.createButton("Spawn " + item.getId(), () -> {
                LoggerInterface.loggerEngine.INFO("spawn " + item.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE).add(cursorVerticalOffset);
                ItemUtils.serverSpawnBasicItem(realm, cursorPos, item.getId());
            }));
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
    }


    /**
     * Level editor menu content for spawning objects
     * @param scrollable
     */
    private static void fillInSpawnObjectContent(VirtualScrollable scrollable){
        scrollable.clearChildren();

        //back button
        scrollable.addChild(Button.createButton("Back", () -> {
            fillInDefaultContent(scrollable);
        }));

        //button for spawning all object types
        for(CommonEntityType object : Globals.gameConfigCurrent.getObjectTypeMap().getTypes()){
            //spawn object button
            scrollable.addChild(Button.createButton("Spawn " + object.getId(), () -> {
                LoggerInterface.loggerEngine.INFO("spawn " + object.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE).add(cursorVerticalOffset);
                CommonEntityUtils.serverSpawnBasicObject(realm, cursorPos, object.getId());
            }));
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
    }


    /**
     * Creates tree view of entities in server
     * @param scrollable
     */
    private static void fillInEntityTreeContent(VirtualScrollable scrollable){
        scrollable.clearChildren();

        //back button
        scrollable.addChild(Button.createButton("Close", () -> {
            fillInDefaultContent(scrollable);
        }));

        //elements for the entity
        for(Entity entity : EntityLookupUtils.getAllEntities()){
            if(
                CreatureUtils.isCreature(entity) ||
                ItemUtils.isItem(entity) ||
                FoliageUtils.isFoliage(entity)
            ){
                Div div = Div.createDiv();
                div.setFlexDirection(YogaFlexDirection.Row);
                div.setMaxHeight(30);
                div.setMarginBottom(5);
                div.setMarginLeft(5);
                div.setMarginRight(5);
                div.setMarginTop(5);

                //delete button
                Button deleteButton = Button.createButton("X", () -> {
                    LoggerInterface.loggerEngine.INFO("Delete " + entity.getId());
                    ServerEntityUtils.destroyEntity(entity);
                    fillInEntityTreeContent(scrollable);
                });
                deleteButton.setMarginRight(5);
                deleteButton.setMarginLeft(5);
                div.addChild(deleteButton);


                Label entityName = Label.createLabel("(" + entity.getId() + ") " + getEntityString(entity));
                div.addChild(entityName);


                scrollable.addChild(div);
            }
        }

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
    }

    /**
     * Gets the string to display for this entity in the entity tree view
     * @param e the entity
     * @return the string to display
     */
    private static String getEntityString(Entity e){
        if(CreatureUtils.isCreature(e)){
            return "Object - " + CreatureUtils.getType(e);
        } else if(ItemUtils.isItem(e)){
            return "Object - " + ItemUtils.getType(e);
        } else if(FoliageUtils.isFoliage(e)){
            return "Object - " + FoliageUtils.getFoliageType(e).getId();
        }
        return "Entity Unknown Type";
    }



    /**
     * Creates atmospheric controls
     * @param scrollable
     */
    private static void fillInAtmosphericControlContent(VirtualScrollable scrollable){
        scrollable.clearChildren();

        //back button
        scrollable.addChild(Button.createButton("Close", () -> {
            fillInDefaultContent(scrollable);
        }));

        LightManager lightManager = Globals.renderingEngine.getLightManager();
        DirectionalLight directionalLight = lightManager.getDirectionalLight();

        Label dirLabel = Label.createLabel("" + directionalLight.getDirection());

        //global light direction
        scrollable.addChild(Label.createLabel("Global Light Direction"));
        Div xDiv = Div.createDiv();
        xDiv.setMaxHeight(50);
        xDiv.setFlexDirection(YogaFlexDirection.Row);
        xDiv.addChild(Label.createLabel("X: "));
        xDiv.addChild(Slider.createSlider((ValueChangeEvent event) -> {
            Vector3f direction = directionalLight.getDirection();
            direction.x = event.getAsFloat() * 2 - 1;
            directionalLight.setDirection(direction);
            dirLabel.setText("" + directionalLight.getDirection());
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
        }));
        scrollable.addChild(xDiv);

        Div yDiv = Div.createDiv();
        yDiv.setMaxHeight(50);
        yDiv.setFlexDirection(YogaFlexDirection.Row);
        yDiv.addChild(Label.createLabel("Y: "));
        yDiv.addChild(Slider.createSlider((ValueChangeEvent event) -> {
            Vector3f direction = directionalLight.getDirection();
            direction.y = event.getAsFloat() * 2 - 1;
            directionalLight.setDirection(direction);
            dirLabel.setText("" + directionalLight.getDirection());
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
        }));
        scrollable.addChild(yDiv);

        Div zDiv = Div.createDiv();
        zDiv.setMaxHeight(50);
        zDiv.setFlexDirection(YogaFlexDirection.Row);
        zDiv.addChild(Label.createLabel("Z: "));
        zDiv.addChild(Slider.createSlider((ValueChangeEvent event) -> {
            Vector3f direction = directionalLight.getDirection();
            direction.z = event.getAsFloat() * 2 - 1;
            directionalLight.setDirection(direction);
            dirLabel.setText("" + directionalLight.getDirection());
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
        }));
        scrollable.addChild(zDiv);

        scrollable.addChild(dirLabel);


        Label colorLabel = Label.createLabel("" + directionalLight.getColor());

        Div rDiv = Div.createDiv();
        rDiv.setMaxHeight(50);
        rDiv.setFlexDirection(YogaFlexDirection.Row);
        rDiv.addChild(Label.createLabel("R: "));
        rDiv.addChild(Slider.createSlider((ValueChangeEvent event) -> {
            Vector3f color = directionalLight.getColor();
            color.x = event.getAsFloat() * 2 - 1;
            directionalLight.setColor(color);
            colorLabel.setText("" + directionalLight.getColor());
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
        }));
        scrollable.addChild(rDiv);

        Div gDiv = Div.createDiv();
        gDiv.setMaxHeight(50);
        gDiv.setFlexDirection(YogaFlexDirection.Row);
        gDiv.addChild(Label.createLabel("G: "));
        gDiv.addChild(Slider.createSlider((ValueChangeEvent event) -> {
            Vector3f color = directionalLight.getColor();
            color.y = event.getAsFloat() * 2 - 1;
            directionalLight.setColor(color);
            colorLabel.setText("" + directionalLight.getColor());
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
        }));
        scrollable.addChild(gDiv);

        Div bDiv = Div.createDiv();
        bDiv.setMaxHeight(50);
        bDiv.setFlexDirection(YogaFlexDirection.Row);
        bDiv.addChild(Label.createLabel("B: "));
        bDiv.addChild(Slider.createSlider((ValueChangeEvent event) -> {
            Vector3f color = directionalLight.getColor();
            color.z = event.getAsFloat() * 2 - 1;
            directionalLight.setColor(color);
            colorLabel.setText("" + directionalLight.getColor());
            Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);
        }));
        scrollable.addChild(bDiv);

        scrollable.addChild(colorLabel);
        

        Globals.engineState.signalSystem.post(SignalType.YOGA_APPLY,mainSidePanel);

    }


}
