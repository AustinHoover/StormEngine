package electrosphere.client.ui.menu.mainmenu;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.ui.components.CharacterCustomizer;
import electrosphere.client.ui.components.CraftingPanel;
import electrosphere.client.ui.components.EquipmentInventoryPanel;
import electrosphere.client.ui.components.InputMacros;
import electrosphere.client.ui.components.NaturalInventoryPanel;
import electrosphere.client.ui.components.SpawnSelectionPanel;
import electrosphere.client.ui.components.VoxelSelectionPanel;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.ingame.CraftingWindow;
import electrosphere.data.crafting.RecipeData;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.renderer.actor.ActorUtils;
import electrosphere.renderer.ui.elements.ActorPanel;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.FormElement;
import electrosphere.renderer.ui.elements.ImagePanel;
import electrosphere.renderer.ui.elements.Slider;
import electrosphere.renderer.ui.elements.StringCarousel;
import electrosphere.renderer.ui.elements.VirtualScrollable;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.ValueChangeEvent;

/**
 * Menu generators for creating test visualizations for ui elements
 */
public class MenuGeneratorsUITesting {

    /**
     * Title menu ui testing window
     * @return
     */
    public static Element createUITestMenu(){
        FormElement rVal = new FormElement();

        //button (back)
        Button backButton = Button.createButton("Back", () -> {
            WindowUtils.replaceMainMenuContents(MenuGeneratorsTitleMenu.createTitleMenu());
        });
        rVal.addChild(backButton);

        StringCarousel displayCarousel = StringCarousel.create(
            Arrays.asList(new String[]{
                "Generic",
                "Slider",
                "Button",
                "ImagePanel1",
                "CharacterCustomizer",
                "NaturalInventoryPanel",
                "EquipInventoryPanel",
                "VoxelPicker",
                "EntitySpawnPicker",
                "CraftingPanel",
            }),
            (ValueChangeEvent event) -> {
                attachComponent(rVal,event.getAsString());
        });
        rVal.addChild(displayCarousel);

        attachComponent(rVal,"Generic");

        return rVal;
    }

    /**
     * Adds the elements currently to display to the form element
     * @param formEl The form element
     * @param type The type of elements to display
     */
    private static void attachComponent(ContainerElement formEl, String type){
        Element backButton = formEl.getChildren().get(0);
        Element selector = formEl.getChildren().get(1);
        formEl.clearChildren();
        formEl.addChild(backButton);
        formEl.addChild(selector);
        switch(type){
            case "Generic": {
                //toggle input
                formEl.addChild(InputMacros.createToggle("Test Toggle", false, null));

                //actor panel
                if(Globals.clientState.playerCamera == null){
                    Globals.clientState.playerCamera = CameraEntityUtils.spawnBasicCameraEntity(new Vector3d(0,0,0), new Vector3d(-1,0,0));
                }
                ActorPanel actorPanel = ActorPanel.create(ActorUtils.createActorFromModelPath(AssetDataStrings.UNITCUBE));
                formEl.addChild(actorPanel);


                //
                //Virtual scrollable test
                VirtualScrollable virtualScrollable = new VirtualScrollable(300, 75);
                //add a ton of children
                for(int i = 0; i < 10; i++){
                    virtualScrollable.addChild(Button.createButton("Test button " + i, () -> {}));
                }

                // slider test
                Slider slider = Slider.createSlider((ValueChangeEvent event) -> {
                });
                virtualScrollable.addChild(slider);
                
                formEl.addChild(virtualScrollable);
            } break;
            case "Slider": {
                formEl.addChild(Slider.createSlider((ValueChangeEvent event) -> {
                }));
            } break;
            case "Button": {
                formEl.addChild(Button.createButton("test", () -> {}));
            } break;
            case "ImagePanel1": {
                Globals.assetManager.addTexturePathtoQueue("Textures/default_diffuse.png");
                ImagePanel panel = ImagePanel.createImagePanel("Textures/default_diffuse.png");
                panel.setMinWidth(100);
                panel.setMinHeight(100);
                panel.setWidth(100);
                panel.setHeight(100);
                formEl.addChild(panel);
            } break;
            case "CharacterCustomizer": {
                formEl.addChild(CharacterCustomizer.createCharacterCustomizerPanel("human", (ObjectTemplate template) ->{}));
            } break;
            case "NaturalInventoryPanel": {
                Entity ent = EntityCreationUtils.TEST_createEntity();
                UnrelationalInventoryState invent = UnrelationalInventoryState.createUnrelationalInventory(5);
                InventoryUtils.setNaturalInventory(ent, invent);
                formEl.addChild(NaturalInventoryPanel.createNaturalInventoryPanel(ent));
            } break;
            case "EquipInventoryPanel": {
                Entity ent = EntityCreationUtils.TEST_createEntity();
                List<EquipPoint> points = new LinkedList<EquipPoint>();
                for(int i = 0; i < 5; i++){
                    EquipPoint equipPoint = new EquipPoint();
                    equipPoint.setEquipPointId("equip point " + i);
                    points.add(equipPoint);
                }
                RelationalInventoryState invent = RelationalInventoryState.buildRelationalInventoryStateFromEquipList(points);
                InventoryUtils.setEquipInventory(ent, invent);
                formEl.addChild(EquipmentInventoryPanel.createEquipmentInventoryPanel(ent));
            } break;
            case "VoxelPicker": {
                formEl.addChild(VoxelSelectionPanel.createVoxelTypeSelectionPanel((VoxelType voxelType) -> {
                    System.out.println(voxelType.getName());
                }));
            } break;
            case "EntitySpawnPicker": {
                formEl.addChild(SpawnSelectionPanel.createEntityTypeSelectionPanel((CommonEntityType entType) -> {
                    System.out.println(entType.getId());
                }));
            } break;
            case "CraftingPanel": {
                formEl.addChild(CraftingPanel.createCraftingPanelComponent(
                    CraftingWindow.HAND_CRAFTING_DATA,
                    (RecipeData recipe) -> {
                    System.out.println("Craft " + recipe.getDisplayName());
                }));
            } break;
        }
    }
    
}
