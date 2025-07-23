package electrosphere.client.ui.components;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.visualattribute.AttributeVariant;
import electrosphere.data.entity.creature.visualattribute.VisualAttribute;
import electrosphere.engine.Globals;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.ActorStaticMorph;
import electrosphere.renderer.actor.ActorUtils;
import electrosphere.renderer.light.DirectionalLight;
import electrosphere.renderer.ui.elements.ActorPanel;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.ScrollableContainer;
import electrosphere.renderer.ui.elements.Slider;
import electrosphere.renderer.ui.elements.StringCarousel;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.ValueChangeEvent;

/**
 * Panel to customize a character
 */
public class CharacterCustomizer {

    /**
     * Minimum width of the component
     */
    public static final int MIN_WIDTH = 500;

    /**
     * Minimum height of the component
     */
    public static final int MIN_HEIGHT = 500;

    /**
     * The direction of the light in the actor panel
     */
    static final Vector3f LIGHT_DIRECTION = new Vector3f(-0.2f,-0.8f,-0.2f).normalize();

    /**
     * Margin around each control container
     */
    static final int CONTROL_CONTAINER_MARGIN = 15;
    
    /**
     * Creates a character customizer panel
     * @param race The race of the character
     * @return The panel component
     */
    public static Element createCharacterCustomizerPanel(String race, Consumer<ObjectTemplate> onConfirm){
        //figure out race data
        CreatureData selectedRaceType = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(race);

        //spawn camera so renderer doesn't crash (once render pipeline is modularized this shouldn't be necessary)
        Globals.clientState.playerCamera = CameraEntityUtils.spawnBasicCameraEntity(new Vector3d(0,0,0), new Vector3d(0,0.3f,1).normalize());
        Globals.renderingEngine.getViewMatrix().set(CameraEntityUtils.getCameraViewMatrix(Globals.clientState.playerCamera));

        //create actor panel
        Actor characterActor = ActorUtils.createActorFromModelPath(selectedRaceType.getGraphicsTemplate().getModel().getPath());
        ActorPanel actorPanel = ActorPanel.create(characterActor);
        actorPanel.setAnimation(selectedRaceType.getGraphicsTemplate().getModel().getIdleData().getAnimation().getNameThirdPerson());
        actorPanel.setPosition(new Vector3f(0));
        actorPanel.setScale(new Vector3f(1.0f));
        actorPanel.setClearColor(new Vector4d(0));

        //Set lighting
        DirectionalLight directionalLight = Globals.renderingEngine.getLightManager().getDirectionalLight();
        directionalLight.setDirection(LIGHT_DIRECTION);

        //have to build static morph while looping through attributes
        ActorStaticMorph staticMorph = new ActorStaticMorph();

        //create creature template
        ObjectTemplate template = ObjectTemplate.create(EntityType.CREATURE, race);

        //create scrollable
        ScrollableContainer scrollable = ScrollableContainer.createScrollable();
        scrollable.setMinWidth(MIN_WIDTH);
        scrollable.setMinHeight(MIN_HEIGHT);

        //create edit controls here
        for(VisualAttribute attribute : selectedRaceType.getVisualAttributes()){
            if(attribute.getType().equals(VisualAttribute.TYPE_BONE)){
                //add label for slider
                Label sliderName = Label.createLabel(attribute.getAttributeId());
                sliderName.setMinWidth(200);
                //add a slider
                Slider boneSlider = Slider.createSlider((ValueChangeEvent event) -> {
                    if(characterActor.getAnimationData().getStaticMorph() != null){
                        staticMorph.updateValue(attribute.getSubtype(), attribute.getPrimaryBone(), event.getAsFloat());
                        if(attribute.getMirrorBone() != null){
                            staticMorph.updateValue(attribute.getSubtype(), attribute.getMirrorBone(), event.getAsFloat());
                        }
                        template.getAttributeValue(attribute.getAttributeId()).setValue(event.getAsFloat());
                    }
                });
                float min = attribute.getMinValue();
                float max = attribute.getMaxValue();
                float defaultValue = min + (max - min)/2.0f;
                boneSlider.setMinimum(min);
                boneSlider.setMaximum(max);
                boneSlider.setValue(defaultValue);
                boneSlider.setMinWidth(200);
                //actually add attributes to static morph
                if(attribute.getPrimaryBone() != null && staticMorph.getBoneTransforms(attribute.getPrimaryBone()) == null){
                    staticMorph.initBoneTransforms(attribute.getPrimaryBone());
                }
                if(attribute.getMirrorBone() != null && staticMorph.getBoneTransforms(attribute.getMirrorBone()) == null){
                    staticMorph.initBoneTransforms(attribute.getMirrorBone());
                }
                //add attribute to creature template
                template.putAttributeValue(attribute.getAttributeId(), defaultValue);

                Div controlContainer = Div.createRow(sliderName,boneSlider);
                controlContainer.setMarginTop(CONTROL_CONTAINER_MARGIN);
                controlContainer.setMarginBottom(CONTROL_CONTAINER_MARGIN);
                controlContainer.setMarginLeft(CONTROL_CONTAINER_MARGIN);
                controlContainer.setMarginRight(CONTROL_CONTAINER_MARGIN);
                scrollable.addChild(controlContainer);
            } else if(attribute.getType().equals(VisualAttribute.TYPE_REMESH)){
                //add label for carousel
                Label scrollableName = Label.createLabel(attribute.getAttributeId());
                scrollableName.setMinWidth(200);
                //add a carousel
                StringCarousel variantCarousel = StringCarousel.create(
                    // attribute.getVariants().stream().filter(variant -> ),
                    attribute.getVariants().stream().map(variant -> variant.getId()).collect(Collectors.toList()),
                    (ValueChangeEvent event) -> {
                        //TODO: implement updating visuals
                        template.getAttributeValue(attribute.getAttributeId()).setVariantId(event.getAsString());
                        AttributeVariant variant = null;
                        for(AttributeVariant variantCurrent : attribute.getVariants()){
                            if(variantCurrent.getId().equals(event.getAsString())){
                                variant = variantCurrent;
                                break;
                            }
                        }
                        if(variant != null){
                            Globals.assetManager.addModelPathToQueue(variant.getModel());
                            for(String mesh : variant.getMeshes()){
                                characterActor.getMeshMask().queueMesh(variant.getModel(), mesh);
                            }
                        }
                    }
                );
                variantCarousel.setMinWidth(200);
                //set the current attrib for the template
                template.putAttributeValue(attribute.getAttributeId(), attribute.getVariants().get(0).getId());

                Div controlContainer = Div.createRow(scrollableName,variantCarousel);
                controlContainer.setMarginTop(CONTROL_CONTAINER_MARGIN);
                controlContainer.setMarginBottom(CONTROL_CONTAINER_MARGIN);
                controlContainer.setMarginLeft(CONTROL_CONTAINER_MARGIN);
                controlContainer.setMarginRight(CONTROL_CONTAINER_MARGIN);
                scrollable.addChild(controlContainer);
            }
        }
        //finally set static morph
        characterActor.getAnimationData().setActorStaticMorph(staticMorph);

        //character create button
        Div createButtonContainer = Div.createDiv();
        createButtonContainer.setMarginTop(CONTROL_CONTAINER_MARGIN);
        createButtonContainer.setMarginBottom(CONTROL_CONTAINER_MARGIN);
        createButtonContainer.setMarginLeft(CONTROL_CONTAINER_MARGIN);
        createButtonContainer.setMarginRight(CONTROL_CONTAINER_MARGIN);
        createButtonContainer.addChild(Button.createButton("Create", () -> {
            onConfirm.accept(template);
        }));

        //create layout
        Div rVal = Div.createCol(
            Div.createRow(
                scrollable,
                actorPanel
            ),
            createButtonContainer
        );

        return rVal;
    }

}
