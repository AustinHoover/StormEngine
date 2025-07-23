package electrosphere.client.ui.components;

import java.util.function.Consumer;

import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elements.Slider;
import electrosphere.renderer.ui.elements.TextInput;
import electrosphere.renderer.ui.elements.ToggleInput;
import electrosphere.renderer.ui.events.ValueChangeEvent;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ValueElement.ValueChangeEventCallback;

/**
 * Macros for creating rich ui elements (ie an input with an included label)
 */
public class InputMacros {

    /**
     * Default margin for a label in a labeled input
     */
    static final int LABEL_MARGIN = 10;


    /**
     * Creates a back button
     * @param onClick The action to perform on backing
     * @return The button
     */
    public static Button createBackButton(Runnable onClick){
        Button rVal = Button.createButton("Back", onClick);
        rVal.setOnClickAudio(AssetDataStrings.UI_TONE_BACK_PRIMARY);
        return rVal;
    }

    /**
     * Creates a text input that has a label and optional placeholder
     * @param label The label for the text input
     * @param placeholder The placeholder (can be null if no placeholder desired)
     * @return The div encapsulating all the individual elements
     */
    public static Div createTextInputVertical(String label, String placeholder){
        Div rVal = Div.createCol();

        //the label
        Label labelEl = Label.createLabel(label);
        labelEl.setMarginRight(LABEL_MARGIN);
        rVal.addChild(labelEl);

        //the actual input
        TextInput inputControl = TextInput.createTextInput();
        if(placeholder != null){
            inputControl.setText(placeholder);
        }
        rVal.addChild(inputControl);

        return rVal;
    }

    /**
     * Creates a text input that has a label and optional placeholder
     * @param label The label for the text input
     * @param placeholder The placeholder (can be null if no placeholder desired)
     * @param callback A callback fired when the text input changes value
     * @return The div encapsulating all the individual elements
     */
    public static Div createTextInput(String label, String placeholder, Consumer<ValueChangeEvent> callback){
        Div rVal = Div.createDiv();
        rVal.setFlexDirection(YogaFlexDirection.Row);

        //the label
        Label labelEl = Label.createLabel(label);
        labelEl.setMarginRight(LABEL_MARGIN);
        rVal.addChild(labelEl);

        //the actual input
        TextInput inputControl = TextInput.createTextInput();
        if(placeholder != null){
            inputControl.setText(placeholder);
        }
        inputControl.setOnValueChangeCallback(new ValueChangeEventCallback() {public void execute(ValueChangeEvent event) {
            callback.accept(event);
        }});
        rVal.addChild(inputControl);

        return rVal;
    }

    /**
     * Creates a labeled slider input
     * @param label The label
     * @param defaultValue The default value for the slider (between 0.0 and 1.0)
     * @return The slider element
     */
    public static Div createSliderInput(String label, Consumer<ValueChangeEvent> onChange, float defaultValue){
        Div rVal = Div.createDiv();
        rVal.setFlexDirection(YogaFlexDirection.Row);

        //the label
        Label labelEl = Label.createLabel(label);
        labelEl.setMarginRight(LABEL_MARGIN);
        rVal.addChild(labelEl);

        //the actual input
        Slider sliderControl = Slider.createSlider(onChange);
        sliderControl.setValue(defaultValue);
        rVal.addChild(sliderControl);

        return rVal;
    }

    /**
     * Creates a toggle with a label
     * @param label The label
     * @param defaultValue The default value
     * @param onChange The on change callback
     * @return The div containing a labeled toggle
     */
    public static Div createToggle(String label, boolean defaultValue, Consumer<ValueChangeEvent> onChange){
        Div rVal = Div.createDiv();
        rVal.setFlexDirection(YogaFlexDirection.Row);
        rVal.setAlignItems(YogaAlignment.Center);

        //the label
        Label labelEl = Label.createLabel(label);
        labelEl.setMarginRight(LABEL_MARGIN);
        rVal.addChild(labelEl);

        //the actual input
        ToggleInput toggleInput = ToggleInput.createToggleInput();
        toggleInput.setValue(defaultValue);
        toggleInput.setOnValueChangeCallback(new ValueChangeEventCallback() {public void execute(ValueChangeEvent event) {
            onChange.accept(event);
        }});
        rVal.addChild(toggleInput);

        return rVal;
    }
    
}
